package com.jw.service.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.jc.exception.ApplicationInternalException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.service.script.ScriptService;
import com.jc.util.Cu;
import com.jc.util.InputCheckUtil;
import com.jc.util.LogUtil;
import com.jc.util.Mu;

public class CreateHtmlService implements ServiceInterface {

	/** ロガー */
	private transient Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "requestKind");
		inputCheckUtil.checkParam(input, "requestUri");

		// 入力パラメータに通知キーが含まれていない場合は、ダミーの値を設定しておく
		if (Cu.isEmpty(input.getString("errMsgKey"))) {
			input.putString("errMsgKey", "0");
		}

		try {
			// HTMLを生成する
			doCreateHtml(input, output);

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private void doCreateHtml(GenericParam input, GenericParam output) throws Exception {

		// ページネーション関連の制御を行う
		controlPage(input, output);

		// リクエストURIに対応するHTMLページのデータを取得する
		String sql = """
				SELECT
					A.TPARTSINPAGE_ID, B.MHTMLPAGE_ID, B.PAGE_NAME, B.RESP_KIND_GET,
					B.RESP_KIND_POST, B.RESP_KIND_PUT, B.RESP_KIND_DELETE,
					B.DESTINATION_GET, B.DESTINATION_POST, B.DESTINATION_PUT,
					B.DESTINATION_DELETE, C.MHTMLPARTS_ID, C.PARTS_NAME,
					D.TPARTSITEM_ID, D.ITEM_KEY, D.ITEM_QUERY
				FROM TPARTSINPAGE A
				LEFT JOIN MHTMLPAGE B ON A.MHTMLPAGE_ID = B.MHTMLPAGE_ID
				LEFT JOIN MHTMLPARTS C ON A.MHTMLPARTS_ID = C.MHTMLPARTS_ID
				LEFT JOIN TPARTSITEM D ON A.TPARTSINPAGE_ID = D.TPARTSINPAGE_ID
				LEFT JOIN MURIPATTERN E ON B.MURIPATTERN_ID = E.MURIPATTERN_ID
				WHERE E.URI_PATTERN = ?
				ORDER BY A.ORD_IN_GRP, D.ORD_IN_GRP
				""";
		var paramList = new ArrayList<String>();
		paramList.add(input.getString("requestUri"));
		var recordList = input.getDb().select(sql, paramList);
		logger.info(new Mu().msg("msg.pageQuerySql", sql));

		// クエリ結果をHTMLページ情報として出力パラメータに設定する
		output.putRecordList("htmlPage", recordList);

		// 取得したレコードを全て処理するまでループ
		for (LinkedHashMap<String, String> columnMap : recordList) {

			// 項目クエリが存在する場合はクエリを実行して出力パラメータに追加する
			if (Cu.isNotEmpty(columnMap.get("ITEM_QUERY"))) {
				output.putRecordList(columnMap.get("ITEM_KEY"), select(input, columnMap));
			}
		}

		// 応答種別と遷移先を設定する
		String requestKind = input.getString("requestKind");
		output.putStringIfNotExists("respKind", recordList.get(0).get("RESP_KIND_" + requestKind));
		output.putStringIfNotExists("destination", ScriptService.convertVariable(
				input, recordList.get(0).get("DESTINATION_" + requestKind), logger));
	}

	private void controlPage(GenericParam input, GenericParam output) {

		// 新規ページのGETの際にページネーションでエラーが起きないよう、デフォルト値を設定する
		if (Cu.isEmpty(input.getString("limit"))) {
			input.putString("limit", "10");
		}
		if (Cu.isEmpty(input.getString("offset"))) {
			input.putString("offset", "0");
		}

		// 出力パラメータに現在のlimit値を設定する
		output.putString("currentLimit", input.getString("limit"));

		// limitとoffsetからページ数を求め、出力パラメータを設定する
		if ("0".equals(input.getString("offset"))) {
			output.putString("currentPage", "1");
		} else {
			int offset = Integer.parseInt(input.getString("offset"));
			int limit = Integer.parseInt(input.getString("limit"));
			int page = (offset / limit) + 1;
			output.putString("currentPage", Integer.toString(page));
		}
	}

	private ArrayList<LinkedHashMap<String, String>> select(GenericParam input,
			LinkedHashMap<String, String> columnMap) throws Exception {

		// 実行するクエリをログに記録する
		String sql = columnMap.get("ITEM_QUERY");
		sql = ScriptService.convertVariable(input, sql, logger);
		logger.info(new Mu().msg("msg.pageItemQuerySql", sql));

		// クエリを実行する
		var recordList = input.getDb().select(sql);

		// クエリ結果に変換可能文字列がある場合は変換する
		for (LinkedHashMap<String, String> columnMapInQueryResult : recordList) {
			for (Map.Entry<String, String> entry : columnMapInQueryResult.entrySet()) {
				columnMapInQueryResult.put(entry.getKey(), ScriptService.convertVariable(
						input, entry.getValue(), logger));
			}
		}

		// SELECTの結果を呼び出し側に返却する
		return recordList;
	}
}
