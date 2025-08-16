package com.jc.util;

import java.sql.SQLException;

import com.jc.TestBase;
import com.jc.db.DbInterface;
import com.jc.param.GenericParam;
import com.jc.service.dbmng.common.DbUpdateBySqlFileService;
import com.jc.service.dbmng.common.GetAllTableDefService;
import com.jc.service.dbmng.common.GetTableNameListService;
import com.jc.service.script.ScriptService;

/**
 * データベースの準備を行うユーティリティクラスです。
 */
public class DbPrepareUtil extends TestBase {

	/** DB名 */
	private String dbName;

	/** カウンタ変数 */
	private static int cnt = 0;

	public DbPrepareUtil() {
		dbName = getDbName();
	}

	public String getDbName() {

		// プロパティファイルからDB名を取得する
		String dbName = "mysql";
		if ("com.jc.db.H2Db".equals(new InnerClassPathProp("jl.properties").get("db.type"))) {
			dbName = "h2";
		}

		// DB名を呼び出し側に返却する
		return dbName;
	}

	/**
	 * DB構成更新サービス実行により、DBを初期化します。
	 * 
	 * @param db DB
	 * @throws SQLException SQL例外
	 */
	public void prepareDbIfNotYet(DbInterface db) throws SQLException {

		// カウンタをインクリメントする
		cnt++;

		// h2以外の場合、本処理は最初の1回のみ実施する
		// (オンメモリDBのh2では、クラスごとにDBが消えてしまうため毎回作り直す必要がある)
		if (!"h2".equals(dbName) && cnt > 1) {
			return;
		}

		// DB構成更新サービスを実行し、DB構成を復元する
		restoreDb(db);
	}

	/**
	 * DB構成更新サービス実行により、DB構成を初期状態に復元します。
	 * 
	 * @param db DB
	 * @throws SQLException SQL例外
	 */
	public String restoreDb(DbInterface db) throws SQLException {

		try {

			// DB構成更新処理を実行する
			updateDbStructure(RESOURCE_PATH + "service/script/dbmng/");

			// エラーが発生しなければコミットする
			getDb().commit();

		} catch (Exception e) {

			// エラー発生時はロールバックする
			getDb().rollback();
		}

		// DB名を呼び出し側に返却する
		return dbName;
	}

	private void updateDbStructure(String basePath) {

		// 初期化SQLを実行する(DB構成更新関連のDBレコードを投入する)
		executeInitSql();

		// 必要なパラメータを準備する
		String scriptId = "1000002"; // DB構成更新
		String dirPath = basePath + dbName;
		String defPath = "10_dbdef";
		String dataPath = "20_dbdata";
		String sqlPath = "30_sql";
		String authorizedPath = "10_authorized";
		String autoCreatedPath = "20_auto_created";
		String forUpdatePath = "30_for_update";

		// 正常系動作確認に必要なパラメータを作成する
		var input = new GenericParam();
		input.setDb(getDb());
		input.putString("scriptId", scriptId);
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("authorizedPath", authorizedPath);
		input.putString("autoCreatedPath", autoCreatedPath);
		input.putString("forUpdatePath", forUpdatePath);

		// DB構成更新サービスを実行する
		var output = new GenericParam();
		var service = new ScriptService();
		service.doService(input, output);
	}

	private void executeInitSql() {

		// 初期化SQLを実行する
		String initSqlPath = RESOURCE_PATH + "service/script/init/10_init.sql";
		var input = new GenericParam();
		input.setDb(getDb());
		input.putString("sqlFilePath", initSqlPath);
		input.putString("resultKey", "resultKey");
		var output = new GenericParam();
		var service = new DbUpdateBySqlFileService();
		service.doService(input, output);
	}

	public String beforeEach() {

		// テストに必要なフォルダを作成する
		prepareOutputDir();

		// テーブル名リストファイルを出力する
		getTableNameList();

		// テーブル定義ファイルを出力する
		getAllTableDef();

		// DB名を呼び出し側に返却する
		return dbName;
	}

	public void prepareOutputDir() {

		// テストに必要なフォルダを作成する
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created");
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/" + dbName + "/20_dbdata/20_auto_created");
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/" + dbName + "/30_sql/20_auto_created");	
	}

	private void getTableNameList() {

		// テーブル定義を出力するためのパラメータを生成する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String getTableNameListSql = createGetTableNameListSql();

		// 処理の前提となるテーブル定義ファイルを出力する
		var input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("getTableNameListSql", getTableNameListSql);
		var output = new GenericParam();
		var service = new GetTableNameListService();
		service.doService(input, output);
	}

	private void getAllTableDef() {

		// テーブル定義を出力するためのパラメータを生成する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String getTableDefSql = createGetTableDefSql();
		String tableNameListFilePath = RESOURCE_PATH + "service/dbmng/common/GetAllTableDefServiceTest/tableNameList.txt";

		// 処理の前提となるテーブル定義ファイルを出力する
		var input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("getTableDefSql", getTableDefSql);
		input.putString("tableNameListFilePath", tableNameListFilePath);
		var output = new GenericParam();
		var service = new GetAllTableDefService();
		service.doService(input, output);
	}

	public static String createGetTableNameListSql() {

		// MySQLかH2かによってSQLを分ける
		String getTableNameListSql = """
				SELECT TABLE_NAME
				  FROM INFORMATION_SCHEMA.TABLES
				  WHERE TABLE_TYPE = 'BASE TABLE'
				  AND TABLE_SCHEMA = 'PUBLIC'
				  ORDER BY TABLE_NAME
				  ;
				""";
		if ("com.jc.db.MysqlDb".equals(new JlProp().get("db.type"))) {
			getTableNameListSql = "show tables;";
		}

		return getTableNameListSql;
	}

	public static String createGetTableDefSql(String dbName) {

		// DB定義取得用SQLを生成する
		String getTableDefSql = "desc #TABLE_NAME#";
		if ("h2".equals(dbName)) {
			getTableDefSql = "SELECT * FROM TBLDEF WHERE TABLE_NAME = '#TABLE_NAME#'";
		}

		// DB定義取得用SQLを呼び出し側に返却する
		return getTableDefSql;
	}

	public static String createGetTableDefSql() {

		// MySQLかH2かによってSQLを分ける
		String dbName = "mysql";
		if ("com.jc.db.H2Db".equals(new JlProp().get("db.type"))) {
			dbName = "h2";
		}

		return createGetTableDefSql(dbName);
	}

	public void afterEach() {
		clearOutputDir();
	}

	public void clearOutputDir() {

		// テストフォルダを削除する
		new FileUtil().deleteDirIfExists(OUTPUT_PATH);
	}
}
