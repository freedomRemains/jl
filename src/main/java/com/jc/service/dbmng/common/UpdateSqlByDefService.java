package com.jc.service.dbmng.common;

import com.jc.db.DbInterface;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.service.fs.DirRecursiveService;
import com.jc.util.InputCheckUtil;

public class UpdateSqlByDefService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "defPath");
		inputCheckUtil.checkParam(input, "sqlPath");
		inputCheckUtil.checkParam(input, "authorizedPath");
		inputCheckUtil.checkParam(input, "autoCreatedPath");
		inputCheckUtil.checkParam(input, "forUpdatePath");

		// ディレクトリ再帰処理を実行する
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), input.getString("defPath"),
				input.getString("sqlPath"), input.getString("autoCreatedPath"));
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), input.getString("defPath"),
				input.getString("sqlPath"), input.getString("authorizedPath"));
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), input.getString("defPath"),
				input.getString("sqlPath"), input.getString("forUpdatePath"));
	}

	private void executeDirRecursiveService(DbInterface db, String dirPath, String defPath,
			String sqlPath, String subPath) {

		// ディレクトリ再帰サービスを実行する
		var input = new GenericParam();
		input.setDb(db);
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath + PATH_DELM + subPath);
		input.putString("sqlPath", sqlPath + PATH_DELM + subPath);
		input.putString("procName", "com.jc.service.proc.CreateSqlByDefProc");
		var output = new GenericParam();
		var service = new DirRecursiveService();
		service.doService(input, output);
	}
}
