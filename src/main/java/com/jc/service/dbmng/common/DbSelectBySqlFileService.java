package com.jc.service.dbmng.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.jc.db.DbInterface;
import com.jc.exception.ApplicationInternalException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.service.fs.TextFileProcedureService;
import com.jc.util.InputCheckUtil;
import com.jc.util.LogUtil;

/**
 * ファイルとして用意されているselectのSQLを実行するサービスです。
 */
public class DbSelectBySqlFileService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "sqlFilePath");
		inputCheckUtil.checkParam(input, "recordListKey");

		try {
			// SQLファイルに基づくselectを実行する
			output.putRecordList(input.getString("recordListKey"),
					selectBySqlFile(input.getDb(), input.getString("sqlFilePath")));

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private ArrayList<LinkedHashMap<String, String>> selectBySqlFile(
			DbInterface db, String sqlFilePath) throws Exception {

		// SQLファイルからSQLを取得する
		String sql = getSqlFromFile(sqlFilePath);

		// SQLを実行し、結果を呼び出し側に返却する
		return db.select(sql);
	}

	private String getSqlFromFile(String sqlFilePath) {

		// SQLファイルからSQLを取得する
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		TextFileProcedureService service = new TextFileProcedureService();
		input.putString("filePath", sqlFilePath);
		input.putString("procName", "com.jc.service.proc.SqlFromFileProc");
		service.doService(input, output);
		return output.getString("sqlFromFile");
	}
}
