package com.jc.service.dbmng.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.util.Cu;
import com.jc.util.FileUtil;
import com.jc.util.InputCheckUtil;
import com.jc.util.LogUtil;
import com.jc.util.Mu;

/**
 * テーブルの定義情報を取得するクラスです。
 */
public class GetTableDefByFileService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "defPath");
		inputCheckUtil.checkParam(input, "tableName");

		// テーブル定義を取得する
		getTableDef(input, output);
	}

	private void getTableDef(GenericParam input, GenericParam output) {

		// 入力パラメータからテーブル名を取得する
		String tableName = input.getString("tableName");

		// テーブル定義ファイルを開く
		String tableDefFilePath = input.getString("dirPath") + PATH_DELM + input.getString("defPath")
				+ PATH_DELM + tableName + ".txt";
		try (BufferedReader tableDefFile = new FileUtil().getBufferedReader(tableDefFilePath)) {

			// 先頭はヘッダ行として扱う(ヘッダ行がない場合はエラーとする)
			String line = tableDefFile.readLine();
			if (line == null) {
				throw new BusinessRuleViolationException(
						new Mu().msg("msg.err.common.invalidTableDef", tableDefFilePath));
			}
			String[] tsvHeaderValues = line.split("\t");

			// ファイル内の全行を処理するまでループ
			ArrayList<LinkedHashMap<String, String>> recordList = new ArrayList<LinkedHashMap<String, String>>();
			while ((line = tableDefFile.readLine()) != null) {

				// 空行は処理せず、ループの先頭に戻る
				if (Cu.isEmpty(line)) {
					continue;
				}

				// TSVファイルとなっているので、タブで区切る
				String[] tsvValues = line.split("\t");

				// TSV値に基づいてテーブル定義を作成し、リストに追加する
				LinkedHashMap<String, String> columnMap = new LinkedHashMap<String, String>();
				for (int i = 0; i < tsvHeaderValues.length; i++) {
					if (tsvValues.length > i) {
						columnMap.put(tsvHeaderValues[i], tsvValues[i]);
					} else {
						columnMap.put(tsvHeaderValues[i], "");
					}
				}
				recordList.add(columnMap);
			}

			// テーブル定義は出力パラメータに書き込む
			output.putRecordList("tableDef" + tableName, recordList);

		} catch (IOException e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}
}
