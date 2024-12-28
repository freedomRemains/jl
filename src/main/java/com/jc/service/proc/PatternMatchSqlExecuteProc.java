package com.jc.service.proc;

import java.io.File;

import org.apache.log4j.Logger;

import com.jc.db.DbInterface;
import com.jc.param.GenericParam;
import com.jc.service.dbmng.common.DbUpdateBySqlFileService;
import com.jc.util.InputCheckUtil;
import com.jc.util.Mu;

public class PatternMatchSqlExecuteProc extends DirProcBase {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	protected void doFileProc(GenericParam input, GenericParam output, String dirPath, File parentDir,
			File currentFile) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "fileNamePattern");
		inputCheckUtil.checkParam(input, "sqlFilePath");

		// ファイルサイズが0の場合は、何もせず即時終了する
		if (currentFile.length() == 0) {
			return ;
		}

		// ファイル名がパターンと一致しない場合は、何もせず即時終了する
		if (!currentFile.getName().matches(input.getString("fileNamePattern"))) {
			return ;
		}

		// SQLを実行する
		executeSql(input.getDb(), currentFile);
	}

	private void executeSql(DbInterface db, File targetFile) {

		// SQLファイルの内容に基づき、DB更新のSQLを実行する
		String resultKey = targetFile.getName() + "_result";
		var input = new GenericParam();
		input.setDb(db);
		input.putString("sqlFilePath", targetFile.getAbsolutePath());
		input.putString("resultKey", resultKey);
		var output = new GenericParam();
		var service = new DbUpdateBySqlFileService();
		service.doService(input, output);

		// 結果をログに記録する
		logger.info(new Mu().msg("msg.updateSqlResult", targetFile.getName(), output.getString(resultKey)));
	}
}
