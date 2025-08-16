package com.jc.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.HashMap;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.db.DbInterface;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.DbPrepareUtil;
import com.jc.util.DbUtil;
import com.jc.util.FileUtil;
import com.jc.util.InnerClassPathProp;
import com.jc.util.Mu;

/**
 * テーブルの定義情報を取得するクラスです。
 */
public class GetTableDefServiceTest extends TestBase {

	private static DbInterface dbMysql;
	private static DbInterface dbH2;

	@BeforeAll
	static void beforeAll() throws Exception {

		// DBの準備を行う
		dbMysql = getDb("mysql");
		dbH2 = getDb("h2");
		new DbPrepareUtil().prepareDbIfNotYet(dbMysql);
		new DbPrepareUtil().prepareDbIfNotYet(dbH2);
	}

	private static DbInterface getDb(String dbName) {

		// DB名に対応するDBインターフェースを呼び出し側に返却する
		DbInterface db = getDb();
		if ("mysql".equals(dbName)) {
			if (dbMysql != null) {
				return dbMysql;
			}
			db = new DbUtil().getDb(new InnerClassPathProp("service/dbmng/common/GetTableDefServiceTest/jlMysql.properties"));
		} else if ("h2".equals(dbName)) {
			if (dbH2 != null) {
				return dbH2;
			}
			db = new DbUtil().getDb(new InnerClassPathProp("service/dbmng/common/GetTableDefServiceTest/jlH2.properties"));
		}

		// DBインターフェースを呼び出し側に返却する
		return db;
	}

	@BeforeEach
	void beforeEach() {

		// テストに必要なフォルダを作成する
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/mysql/10_dbdef/20_auto_created");
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/h2/10_dbdef/20_auto_created");
	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		new FileUtil().deleteDirIfExists(OUTPUT_PATH);
	}

	@AfterAll
	static void afterAll() throws Exception {

		// DB接続をクローズする
		closeDb();
		getDb("mysql").close();
		getDb("h2").close();
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	private static final String TEST_RESOURCE_PATH = RESOURCE_PATH + "service/dbmng/common/GetTableDefServiceTest/";

	@Test
	void test01() throws Exception {
		doTest01("mysql");
		doTest01("h2");
	}

	private void doTest01(String dbName) {

		// DB名の文字列を生成する
		String getTableDefSql = "SELECT NOT_EXIST_COLUMN FROM NOT_EXIST_TABLE";

		// SQLエラーパターン
		String tableName = "TSCR";
		String tableDefFilePath = OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/" + tableName + ".txt";
		try {
			doService(dbName, tableName, tableDefFilePath, getTableDefSql);
			fail();
		} catch (ApplicationInternalException e) {

			// SQLシンタックス例外が起きていることを確認する
			assertTrue(e.getLocalizedMessage().contains("SQLSyntaxErrorException"));
		}
	}

	@Test
	void test02() throws Exception {
		doTest02("mysql");
		doTest02("h2");
	}

	private void doTest02(String dbName) {

		// DB定義取得用SQLを生成する
		String getTableDefSql = DbPrepareUtil.createGetTableDefSql(dbName);

		// DBの準備を行う
		prepareDb(dbName);

		// 正常系パターン
		String tableName = "TSCR";
		String tableDefFilePath = OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/" + tableName + ".txt";
		doService(dbName, tableName, tableDefFilePath, getTableDefSql);

		// DB定義ファイルが出力されていることを確認する
		assertTrue(new File(tableDefFilePath).exists());
		assertTrue(new File(tableDefFilePath).length() > 0);
	}

	@Test
	void test03() throws Exception {
		doTest03("mysql");
		doTest03("h2");
	}

	private void doTest03(String dbName) {

		// DB定義取得用SQLを生成する
		String getTableDefSql = DbPrepareUtil.createGetTableDefSql(dbName);

		// DBの準備を行う
		prepareDb(dbName);

		// カバレッジ(MySQL向け)
		var convertMap = new HashMap<String, String>();
		convertMap.put("Field", "Field");
		convertMap.put("Type", "Type");
		convertMap.put("Null", "Null");
		convertMap.put("Key", "Key");
		convertMap.put("Default", "Default");
		convertMap.put("Extra", "Extra");

		String tableName = "TSCR";
		String tableDefFilePath = OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/" + tableName + ".txt";

		// convertMapを設定するため、doServiceメソッドを呼び出さずにサービスを実行する
		GenericParam input = new GenericParam();
		input.setDb(getDb(dbName));
		input.putString("tableName", tableName);
		input.putString("tableDefFilePath", tableDefFilePath);
		input.putString("getTableDefSql", getTableDefSql);
		GenericParam output = new GenericParam();
		GetTableDefService service = new GetTableDefService();
		service.setConvertMap(convertMap); // ここがカバレッジ向けコード(convertMapを指定している)
		service.doService(input, output);

		// DB定義ファイルが出力されていることを確認する
		assertTrue(new File(tableDefFilePath).exists());
		assertTrue(new File(tableDefFilePath).length() > 0);
	}

	@Test
	void test04() throws Exception {
		doTest04("h2");
	}

	private void doTest04(String dbName) {

		// DB定義取得用SQLを生成する
		String getTableDefSql = "SELECT * FROM TBLDEF WHERE TABLE_NAME = 'NOT_EXIST_TABLE'";

		// DBの準備を行う
		prepareDb(dbName);

		// カバレッジ(テーブル定義取得SQLでデータが取得できない)
		String tableName = "TSCR";
		String tableDefFilePath = OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/" + tableName + ".txt";
		try {
			doService(dbName, tableName, tableDefFilePath, getTableDefSql);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.err.common.invalidTableDef", tableName),
					e.getLocalizedMessage());
		}
	}

	private void prepareDb(String dbName) {

		// DB名に対応するDBインターフェースを取得する
		DbInterface db = getDb(dbName);

		// DBがH2の場合は、追加のSQLを実行する
		if ("h2".equals(dbName)) {
			GenericParam input = new GenericParam();
			input.setDb(db);
			input.putString("sqlFilePath", TEST_RESOURCE_PATH + "10_addSqlForH2.sql");
			input.putString("resultKey", "ret");
			GenericParam output = new GenericParam();
			DbUpdateBySqlFileService service = new DbUpdateBySqlFileService();
			service.doService(input, output);
		}
	}

	private void doService(String dbName, String tableName, String tableDefFilePath,
			String getTableDefSql) {

		GenericParam input = new GenericParam();
		input.setDb(getDb(dbName));
		input.putString("tableName", tableName);
		input.putString("tableDefFilePath", tableDefFilePath);
		input.putString("getTableDefSql", getTableDefSql);
		GenericParam output = new GenericParam();
		GetTableDefService service = new GetTableDefService();
		service.doService(input, output);
	}
}
