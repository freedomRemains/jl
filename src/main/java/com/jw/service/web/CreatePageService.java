package com.jw.service.web;

import java.util.ArrayList;

import com.jc.db.DbInterface;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.util.InputCheckUtil;
import com.jc.util.LogUtil;
import com.jw.util.ErrMsgUtil;

public class CreatePageService implements ServiceInterface {

	/** マイページのリンクグループID */
	private static final String MYPAGE_LINK_GRP_ID = "1000101";

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "accountId");
		inputCheckUtil.checkParam(input, "tableName");
		inputCheckUtil.checkParam(input, "PAGE_NAME");
		inputCheckUtil.checkParam(input, "URI_PATTERN");

		try {
			// HTMLページを作成する
			doCreatePage(input, output);

		} catch (BusinessRuleViolationException e) {

			// エラーメッセージIDを取得する(例外を発行する側がメッセージを編集している)
			String errMsgKey = new ErrMsgUtil().getErrMsgKeyByMsg(input.getDb(),
					input.getString("sessionId"), input.getString("accountId"),
					e.getMessage());

			// ページ編集にリダイレクトする
			output.putString("respKind", "redirect");
			output.putString("destination", "editPage.html?errMsgKey=" + errMsgKey);

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private void doCreatePage(GenericParam input, GenericParam output) throws Exception {

		// キー重複の業務エラーがないか、あらかじめチェックする
		preCheckDuplicateKey(input.getDb(), input.getString("URI_PATTERN"),
				input.getString("PAGE_NAME"));

		// URIパターンのレコードを作成する
		String uriPatternId = createUriPattern(input.getDb(), input.getString("accountId"),
				input.getString("PAGE_NAME"), input.getString("URI_PATTERN"));

		// リンクのレコードを作成する
		createLink(input.getDb(), input.getString("accountId"),
				input.getString("PAGE_NAME"), uriPatternId);

		// スクリプトのレコードを作成する
		String scriptId = createScript(input.getDb(), input.getString("accountId"),
				input.getString("PAGE_NAME"));

		// スクリプトエレメントのレコードを作成する
		createScriptElement(input.getDb(), input.getString("accountId"), scriptId);

		// HTMLページのレコードを作成する
		String htmlPageId = createHtmlPage(input.getDb(), input.getString("accountId"),
				input.getString("PAGE_NAME"), uriPatternId, scriptId);

		// ページ内パーツのレコードを作成する
		String partsInPageId = createPartsInPage(input.getDb(), input.getString("accountId"), htmlPageId);

		// 要求ロールのレコードを作成する(当初はマスタ or グランドマスタだけ表示可能なページとする)
		createRequireRole(input.getDb(), input.getString("accountId"), htmlPageId);

		// パーツ項目のレコードを作成する
		createPartsItem(input.getDb(), input.getString("accountId"), partsInPageId);
	}

	private void preCheckDuplicateKey(DbInterface db, String uriPattern, String pageName)
			throws Exception {

		// 同一のURIパターンの登録は禁止する
		checkDuplicateKey(db, "MURIPATTERN", "URI_PATTERN", uriPattern);

		// 同一のスクリプト名の登録は禁止する
		checkDuplicateKey(db, "TSCR", "SCR_NAME", pageName);

		// 同一のページ名の登録は禁止する
		checkDuplicateKey(db, "MHTMLPAGE", "PAGE_NAME", pageName);
	}

	private String createUriPattern(DbInterface db, String accountId, String pageName,
			String uriPattern) throws Exception {

		// レコード作成に必要な入力値を作成する
		GenericParam input = new GenericParam();
		input.setDb(db);
		input.putString("requireRuledNumber", "true"); // 規則的採番をリクエスト
		input.putString("accountId", accountId);
		input.putString("tableName", "MURIPATTERN");
		input.putString("URI_PATTERN_NAME", pageName);
		input.putString("URI_PATTERN", uriPattern);

		// レコードを作成する
		createRecord(input);

		// 登録したレコードを取得する
		String sql = "SELECT MURIPATTERN_ID FROM MURIPATTERN WHERE URI_PATTERN_NAME = ? AND URI_PATTERN = ?";
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add(pageName);
		paramList.add(uriPattern);
		var recordList = db.select(sql, paramList);

		// URIパターンIDを呼び出し側に返却する
		return recordList.get(0).get("MURIPATTERN_ID");
	}

	private void createLink(DbInterface db, String accountId, String pageName, String uriPatternId)
			throws Exception {

		// マイページのリンクグループID内の順番を決める
		var recordList = db.select("SELECT MAX(ORD_IN_GRP) FROM MLINK WHERE MLINKGRP_ID = " + MYPAGE_LINK_GRP_ID);
		int maxOrdInGrp = Integer.parseInt(recordList.get(0).get("MAX(ORD_IN_GRP)"));
		String ordInGrp = Integer.toString(maxOrdInGrp + 1);

		// レコード作成に必要な入力値を作成する
		GenericParam input = new GenericParam();
		input.setDb(db);
		input.putString("requireRuledNumber", "true"); // 規則的採番をリクエスト
		input.putString("accountId", accountId);
		input.putString("tableName", "MLINK");
		input.putString("LINK_NAME", pageName);
		input.putString("MURIPATTERN_ID", uriPatternId);
		input.putString("IS_POST", "0");
		input.putString("MLINKGRP_ID", MYPAGE_LINK_GRP_ID);
		input.putString("ORD_IN_GRP", ordInGrp);

		// レコードを作成する
		createRecord(input);
	}

	private String createScript(DbInterface db, String accountId, String pageName) throws Exception {

		// レコード作成に必要な入力値を作成する
		GenericParam input = new GenericParam();
		input.setDb(db);
		input.putString("requireRuledNumber", "true"); // 規則的採番をリクエスト
		input.putString("accountId", accountId);
		input.putString("tableName", "TSCR");
		input.putString("SCR_NAME", pageName);

		// レコードを作成する
		createRecord(input);

		// 登録したレコードを取得する
		String sql = "SELECT TSCR_ID FROM TSCR WHERE SCR_NAME = ?";
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add(pageName);
		var recordList = db.select(sql, paramList);

		// URIパターンIDを呼び出し側に返却する
		return recordList.get(0).get("TSCR_ID");
	}

	private void createScriptElement(DbInterface db, String accountId, String scriptId)
			throws Exception {

		// レコード作成に必要な入力値を作成する(1レコード目)
		GenericParam input = new GenericParam();
		input.setDb(db);
		input.putString("requireRuledNumber", "true"); // 規則的採番をリクエスト
		input.putString("accountId", accountId);
		input.putString("tableName", "TSCRELM");
		input.putString("SERVICE_NAME", "com.jw.service.web.GetAccountService");
		input.putString("ADAPTER", "");
		input.putString("PREPARE_INPUT", "");
		input.putString("TSCR_ID", scriptId);
		input.putString("ORD_IN_GRP", "1");

		// レコードを作成する
		createRecord(input);

		// レコード作成に必要な入力値を作成する(2レコード目、入力は作り直す)
		input = new GenericParam();
		input.setDb(db);
		input.putString("accountId", accountId);
		input.putString("tableName", "TSCRELM");
		input.putString("SERVICE_NAME", "com.jw.service.web.CreateHtmlService");
		input.putString("ADAPTER", "");
		input.putString("PREPARE_INPUT", "");
		input.putString("TSCR_ID", scriptId);
		input.putString("ORD_IN_GRP", "2");

		// レコードを作成する
		createRecord(input);
	}

	private String createHtmlPage(DbInterface db, String accountId, String pageName,
			String uriPatternId, String scriptId) throws Exception {

		// レコード作成に必要な入力値を作成する
		GenericParam input = new GenericParam();
		input.setDb(db);
		input.putString("requireRuledNumber", "true"); // 規則的採番をリクエスト
		input.putString("accountId", accountId);
		input.putString("tableName", "MHTMLPAGE");
		input.putString("PAGE_NAME", pageName);
		input.putString("MURIPATTERN_ID", uriPatternId);
		input.putString("TSCR_ID_GET", scriptId);
		input.putString("RESP_KIND_GET", "forward");
		input.putString("DESTINATION_GET", "10000_contents.jsp");
		input.putString("TSCR_ID_POST", "0");
		input.putString("RESP_KIND_POST", "redirect");
		input.putString("DESTINATION_POST", "top.html");
		input.putString("TSCR_ID_PUT", "0");
		input.putString("RESP_KIND_PUT", "redirect");
		input.putString("DESTINATION_PUT", "top.html");
		input.putString("TSCR_ID_DELETE", "0");
		input.putString("RESP_KIND_DELETE", "redirect");
		input.putString("DESTINATION_DELETE", "top.html");

		// レコードを作成する
		createRecord(input);

		// 登録したレコードを取得する
		String sql = "SELECT MHTMLPAGE_ID FROM MHTMLPAGE WHERE PAGE_NAME = ? AND MURIPATTERN_ID = ? AND TSCR_ID_GET = ?";
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add(pageName);
		paramList.add(uriPatternId);
		paramList.add(scriptId);
		var recordList = db.select(sql, paramList);

		// URIパターンIDを呼び出し側に返却する
		return recordList.get(0).get("MHTMLPAGE_ID");
	}

	private String createPartsInPage(DbInterface db, String accountId, String htmlPageId)
			throws Exception {

		// レコード作成に必要な入力値を作成する(1レコード目)
		GenericParam input = new GenericParam();
		input.setDb(db);
		input.putString("requireRuledNumber", "true"); // 規則的採番をリクエスト
		input.putString("accountId", accountId);
		input.putString("tableName", "TPARTSINPAGE");
		input.putString("MHTMLPAGE_ID", htmlPageId);
		input.putString("MHTMLPARTS_ID", "1000001");
		input.putString("ORD_IN_GRP", "1");

		// レコードを作成する
		createRecord(input);

		// レコード作成に必要な入力値を作成する(2レコード目、入力は作り直す)
		input = new GenericParam();
		input.setDb(db);
		input.putString("accountId", accountId);
		input.putString("tableName", "TPARTSINPAGE");
		input.putString("MHTMLPAGE_ID", htmlPageId);
		input.putString("MHTMLPARTS_ID", "1000002");
		input.putString("ORD_IN_GRP", "2");

		// レコードを作成する
		createRecord(input);

		// レコード作成に必要な入力値を作成する(3レコード目、入力は直前を流用)
		input.putString("MHTMLPARTS_ID", "1001101");

		// レコードを作成する
		createRecord(input);

		// 登録したレコードを取得する
		String sql = "SELECT TPARTSINPAGE_ID FROM TPARTSINPAGE WHERE MHTMLPAGE_ID = ? AND MHTMLPARTS_ID = 1000001";
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add(htmlPageId);
		var recordList = db.select(sql, paramList);

		// URIパターンIDを呼び出し側に返却する
		return recordList.get(0).get("TPARTSINPAGE_ID");
	}

	private void createRequireRole(DbInterface db, String accountId, String htmlPageId)
			throws Exception {

		// レコード作成に必要な入力値を作成する(1レコード目)
		GenericParam input = new GenericParam();
		input.setDb(db);
		input.putString("requireRuledNumber", "true"); // 規則的採番をリクエスト
		input.putString("accountId", accountId);
		input.putString("tableName", "MREQUIREROLE");
		input.putString("MHTMLPAGE_ID", htmlPageId);
		input.putString("MROLE_ID", "1000301");

		// レコードを作成する
		createRecord(input);

		// レコード作成に必要な入力値を作成する(2レコード目、入力は作り直す)
		input = new GenericParam();
		input.setDb(db);
		input.putString("accountId", accountId);
		input.putString("tableName", "MREQUIREROLE");
		input.putString("MHTMLPAGE_ID", htmlPageId);
		input.putString("MROLE_ID", "1000401");

		// レコードを作成する
		createRecord(input);
	}

	private void createPartsItem(DbInterface db, String accountId, String partsInPageId)
			throws Exception {

		// ページ内パーツのIDを整数値に変換しておく
		int partsInPageIdNum = Integer.parseInt(partsInPageId);

		// レコード作成に必要な入力値を作成する(1レコード目)
		GenericParam input = new GenericParam();
		input.setDb(db);
		input.putString("requireRuledNumber", "true"); // 規則的採番をリクエスト
		input.putString("accountId", accountId);
		input.putString("tableName", "TPARTSITEM");
		input.putString("ITEM_KEY", "systemName");
		input.putString("ITEM_QUERY", "SELECT GNR_VAL FROM MGNRKEYVAL WHERE GNR_KEY = 'systemName'");
		input.putString("TPARTSINPAGE_ID", Integer.toString(partsInPageIdNum));
		input.putString("ORD_IN_GRP", "1");

		// レコードを作成する
		createRecord(input);

		// レコード作成に必要な入力値を作成する(2レコード目、入力は作り直す)
		input = new GenericParam();
		input.setDb(db);
		input.putString("accountId", accountId);
		input.putString("tableName", "TPARTSITEM");
		input.putString("ITEM_KEY", "urlLink");
		input.putString("ITEM_QUERY", "SELECT A.PAGE_NAME, B.URI_PATTERN FROM MHTMLPAGE A LEFT JOIN MURIPATTERN B ON A.MURIPATTERN_ID = B.MURIPATTERN_ID WHERE A.TSCR_ID_GET = 1100001 OR TSCR_ID_GET = 1100201");
		input.putString("TPARTSINPAGE_ID", Integer.toString(partsInPageIdNum));
		input.putString("ORD_IN_GRP", "2");

		// レコードを作成する
		createRecord(input);

		// レコード作成に必要な入力値を作成する(3レコード目、入力は直前を流用)
		input.putString("ITEM_KEY", "errMsgList");
		input.putString("ITEM_QUERY", "SELECT ERR_MSG FROM TERRMSG WHERE TERRMSG_ID = #{errMsgKey}");
		input.putString("TPARTSINPAGE_ID", Integer.toString(partsInPageIdNum));
		input.putString("ORD_IN_GRP", "3");

		// レコードを作成する
		createRecord(input);
	}

	private void checkDuplicateKey(DbInterface db, String tableName, String targetColumn,
			String columnValue) throws Exception {

		// キー重複がある場合は例外とする
		String sql = "SELECT " + targetColumn + " FROM " + tableName + " WHERE " + targetColumn + " = ?";
		ArrayList<String> paramList = new ArrayList<String>();
		paramList.add(columnValue);
		var recordList = db.select(sql, paramList);
		if (!recordList.isEmpty()) {
			throw new BusinessRuleViolationException(new ErrMsgUtil().getErrMsg(db, "1000501",
					tableName, targetColumn, columnValue));
		}
	}

	private GenericParam createRecord(GenericParam input) throws Exception {

		// レコード作成サービスを実行する
		GenericParam output = new GenericParam();
		CreateRecordService service = new CreateRecordService();
		service.doService(input, output);
		return output;
	}
}
