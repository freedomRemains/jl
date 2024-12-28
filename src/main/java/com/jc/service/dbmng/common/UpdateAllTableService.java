package com.jc.service.dbmng.common;

import com.jc.db.DbInterface;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.service.fs.DirRecursiveService;
import com.jc.util.InputCheckUtil;

public class UpdateAllTableService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "dataPath");
		inputCheckUtil.checkParam(input, "sqlPath");
		inputCheckUtil.checkParam(input, "authorizedPath");
		inputCheckUtil.checkParam(input, "autoCreatedPath");
		inputCheckUtil.checkParam(input, "forUpdatePath");

		// ディレクトリ再帰処理を実行する(autoCreatedPath)
		String sqlFilePath = input.getString("dirPath") + PATH_DELM + input.getString("sqlPath")
				+ PATH_DELM + input.getString("autoCreatedPath");
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), "DROP_.*", sqlFilePath);
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), "CREATE_.*", sqlFilePath);
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), "INSERT_.*", sqlFilePath);

		// ディレクトリ再帰処理を実行する(authorizedPath)
		sqlFilePath = input.getString("dirPath") + PATH_DELM + input.getString("sqlPath")
				+ PATH_DELM + input.getString("authorizedPath");
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), "DROP_.*", sqlFilePath);
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), "CREATE_.*", sqlFilePath);
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), "INSERT_.*", sqlFilePath);

		// ディレクトリ再帰処理を実行する(forUpdatePath)
		sqlFilePath = input.getString("dirPath") + PATH_DELM + input.getString("sqlPath")
				+ PATH_DELM + input.getString("forUpdatePath");
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), "DROP_.*", sqlFilePath);
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), "CREATE_.*", sqlFilePath);
		executeDirRecursiveService(input.getDb(), input.getString("dirPath"), "INSERT_.*", sqlFilePath);
	}

	private void executeDirRecursiveService(DbInterface db, String dirPath, String fileNamePattern,
			String sqlFilePath) {

		// ディレクトリ再帰サービスを実行する
		var input = new GenericParam();
		input.setDb(db);
		input.putString("dirPath", dirPath);
		input.putString("fileNamePattern", fileNamePattern);
		input.putString("sqlFilePath", sqlFilePath);
		input.putString("procName", "com.jc.service.proc.PatternMatchSqlExecuteProc");
		var output = new GenericParam();
		var service = new DirRecursiveService();
		service.doService(input, output);
	}
}
