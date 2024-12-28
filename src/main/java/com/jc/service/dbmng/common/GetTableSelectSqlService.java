package com.jc.service.dbmng.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.jc.exception.ApplicationInternalException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.util.FileUtil;
import com.jc.util.InputCheckUtil;
import com.jc.util.LogUtil;
import com.jc.util.Mu;

/**
 * 各テーブルのSQLを取得するクラスです。
 */
public class GetTableSelectSqlService implements ServiceInterface {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "defPath");
		inputCheckUtil.checkParam(input, "sqlPath");
		inputCheckUtil.checkParam(input, "tableName");

		// テーブルのSQLを取得する
		getTableSql(input, output);
	}

	private void getTableSql(GenericParam input, GenericParam output) {

		// 入力パラメータからテーブル名を取得する
		String tableName = input.getString("tableName");

		// リソースパス配下の "10_dbdef/20_auto_created" にあるテーブル定義ファイルを読み込む
		ArrayList<LinkedHashMap<String, String>> recordList = getTableDef(input, tableName);

		// リソースパス配下の "30_sql/20_auto_created" にテキストファイルを生成する
		String sqlFilePath = input.getString("dirPath") + PATH_DELM + input.getString("sqlPath") + "/SELECT_"
				+ tableName + ".txt";
		try (BufferedWriter tableSqlFile = new FileUtil().getBufferedWriter(sqlFilePath)) {

			// DB定義を取得してファイルに書き込む
			writeTableSql(output, tableName, tableSqlFile, recordList);

		} catch (SQLException | IOException e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private ArrayList<LinkedHashMap<String, String>> getTableDef(GenericParam input, String tableName) {

		// テーブル定義をオンメモリで持っている場合は、呼び出し側に返却する
		ArrayList<LinkedHashMap<String, String>> recordList = input.getRecordList("tableDef" + tableName);
		if (recordList != null && recordList.size() > 0) {
			logger.info(new Mu().msg("msg.detectedTableDefOnMemory", "tableDef" + tableName));
			return recordList;
		}

		// テーブル定義をファイルから読み込む
		GetTableDefByFileService getTableDefByFileService = new GetTableDefByFileService();
		GenericParam output = new GenericParam();
		getTableDefByFileService.doService(input, output);
		recordList = output.getRecordList("tableDef" + tableName);

		return recordList;
	}

	private void writeTableSql(GenericParam output, String tableName, BufferedWriter tableSqlFile,
			ArrayList<LinkedHashMap<String, String>> recordList) throws SQLException, IOException {

		// SQLを生成する
		String lineSepr = output.getLineSepr();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT");
		StringBuffer columnPart = new StringBuffer();
		for (LinkedHashMap<String, String> columnMap : recordList) {
			if (columnPart.length() > 0) {
				columnPart.append(",");
			}
			columnPart.append(lineSepr + "\t" + columnMap.get("Field").toUpperCase());
		}
		sql.append(columnPart + lineSepr);
		sql.append(" FROM " + tableName + lineSepr + " ORDER BY" + columnPart + lineSepr + ";");
		logger.info(new Mu().msg("msg.common.sql", sql));

		// SQLをファイルに書き込む
		tableSqlFile.write(sql.toString());
		tableSqlFile.newLine();
	}
}
