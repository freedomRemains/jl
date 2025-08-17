package com.jc.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.DbPrepareUtil;
import com.jc.util.Mu;

/**
 * テーブルへのレコード追加のSQLを生成するクラスです。
 */
public class GetAllTableInsertSqlServiceTest extends TestBase {

	@BeforeAll
	static void beforeAll() throws Exception {

		// DBの準備を行う
		new DbPrepareUtil().prepareDbIfNotYet(getDb());
	}

	private static String dbName;

	@BeforeEach
	void beforeEach() {

		// テストに必要な準備処理を実行する
		dbName = new DbPrepareUtil().beforeEach();

		// テーブルデータを取得するために必要なSELECTのSQL、データのTSVファイルを生成する
		createAllTableSelectSql(dbName);
		createAllTableDataFile(dbName);
	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		new DbPrepareUtil().afterEach();
	}

	public static void createAllTableSelectSql(String dbName) {

		// 全てのテーブルについて、SELECTのSQLを出力する
		createTableSelectSql(dbName, "GNR_GRP");
		createTableSelectSql(dbName, "GNR_KEY_VAL");
		createTableSelectSql(dbName, "TBL_DEF");
		createTableSelectSql(dbName, "ACCNT");
		createTableSelectSql(dbName, "MAIL");
		createTableSelectSql(dbName, "SCR");
		createTableSelectSql(dbName, "SCR_ELM");
	}

	private static void createTableSelectSql(String dbName, String tableName) {

		// 必要なパラメータを準備する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableDefFilePath = OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/SCR.txt";

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		input.putString("tableDefFilePath", tableDefFilePath);
		GenericParam output = new GenericParam();
		GetTableSelectSqlService service = new GetTableSelectSqlService();
		service.doService(input, output);
	}

	private static void createAllTableDataFile(String dbName) {

		// 全てのテーブルについて、データのTSVファイルを出力する
		createTableDataFile(dbName, "GNR_GRP");
		createTableDataFile(dbName, "GNR_KEY_VAL");
		createTableDataFile(dbName, "TBL_DEF");
		createTableDataFile(dbName, "ACCNT");
		createTableDataFile(dbName, "MAIL");
		createTableDataFile(dbName, "SCR");
		createTableDataFile(dbName, "SCR_ELM");
	}

	private static void createTableDataFile(String dbName, String tableName) {

		// 必要なパラメータを準備する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDataService service = new GetTableDataService();
		service.doService(input, output);
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// 必須パラメータがないパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		GetAllTableInsertSqlService service = new GetAllTableInsertSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "db"), e.getLocalizedMessage());
		}

		input.setDb(getDb());
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "dirPath"), e.getLocalizedMessage());
		}

		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		input.putString("dirPath", dirPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "dataPath"), e.getLocalizedMessage());
		}

		String dataPath = "20_dbdata/20_auto_created";
		input.putString("dataPath", dataPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "sqlPath"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// テーブル名リストファイルのパスが指定されているケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableNameListFilePath = RESOURCE_PATH + "service/dbmng/common/GetAllTableDefServiceTest/tableNameList.txt";
		doService(dirPath, defPath, dataPath, sqlPath, tableNameListFilePath);

		// DB定義ファイルが出力されていることを確認する
		assertFileOutput();
	}

	@Test
	void test03() {

		// テーブル名リストが指定されているケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		addMapToList(tableNameList, "GNR_GRP");
		addMapToList(tableNameList, "GNR_KEY_VAL");
		addMapToList(tableNameList, "ACCNT");
		addMapToList(tableNameList, "SCR");
		addMapToList(tableNameList, "SCR_ELM");
		doServiceByTableNameList(dirPath, defPath, dataPath, sqlPath, tableNameList);

		// DB定義ファイルが出力されていることを確認する
		assertFileOutput();
	}

	@Test
	void test04() {

		// (カバレッジ)テーブル名リストがnullのケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		doServiceByTableNameList(dirPath, defPath, dataPath, sqlPath, null);

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	@Test
	void test05() {

		// (カバレッジ)テーブル名リストが空のケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		doServiceByTableNameList(dirPath, defPath, dataPath, sqlPath, tableNameList);

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	@Test
	void test06() {

		// (カバレッジ)存在しないテーブル名リストファイルのパスが指定されているケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableNameListFilePath = "nowhere.txt";
		try {
			doService(dirPath, defPath, dataPath, sqlPath, tableNameListFilePath);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	private void doService(String dirPath, String defPath, String dataPath, String sqlPath,
			String tableNameListFilePath) {

		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableNameListFilePath", tableNameListFilePath);
		GenericParam output = new GenericParam();
		GetAllTableInsertSqlService service = new GetAllTableInsertSqlService();
		service.doService(input, output);
	}

	private void doServiceByTableNameList(String dirPath, String defPath, String dataPath,
			String sqlPath, ArrayList<LinkedHashMap<String, String>> tableNameList) {

		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putRecordList("tableNameList", tableNameList);
		GenericParam output = new GenericParam();
		GetAllTableInsertSqlService service = new GetAllTableInsertSqlService();
		service.doService(input, output);
	}

	private void addMapToList(ArrayList<LinkedHashMap<String, String>> tableNameList, String tableName) {
		var tableNameMap = new LinkedHashMap<String, String>();
		tableNameMap.put("", tableName);
		tableNameList.add(tableNameMap);
	}

	private void assertFileOutput() {

		// ファイルが出力されていることを確認する
		String outputPath = OUTPUT_PATH + "dbmng/" + dbName + "/30_sql/20_auto_created/";
		assertTrue(new File(outputPath + "INSERT_GNR_GRP.txt").exists());
		assertTrue(new File(outputPath + "INSERT_GNR_GRP.txt").length() > 0);
		assertTrue(new File(outputPath + "INSERT_GNR_KEY_VAL.txt").exists());
		assertTrue(new File(outputPath + "INSERT_GNR_KEY_VAL.txt").length() > 0);
		assertTrue(new File(outputPath + "INSERT_ACCNT.txt").exists());
		assertTrue(new File(outputPath + "INSERT_ACCNT.txt").length() > 0);
		assertTrue(new File(outputPath + "INSERT_SCR.txt").exists());
		assertTrue(new File(outputPath + "INSERT_SCR.txt").length() > 0);
		assertTrue(new File(outputPath + "INSERT_SCR_ELM.txt").exists());
		assertTrue(new File(outputPath + "INSERT_SCR_ELM.txt").length() > 0);
	}

	private void assertNoFileOutput() {

		// ファイルが出力されていないことを確認する
		String outputPath = OUTPUT_PATH + "dbmng/" + dbName + "/30_sql/20_auto_created/";
		assertFalse(new File(outputPath + "INSERT_GNR_GRP.txt").exists());
		assertFalse(new File(outputPath + "INSERT_GNR_GRP.txt").length() > 0);
		assertFalse(new File(outputPath + "INSERT_GNR_KEY_VAL.txt").exists());
		assertFalse(new File(outputPath + "INSERT_GNR_KEY_VAL.txt").length() > 0);
		assertFalse(new File(outputPath + "INSERT_ACCNT.txt").exists());
		assertFalse(new File(outputPath + "INSERT_ACCNT.txt").length() > 0);
		assertFalse(new File(outputPath + "INSERT_SCR.txt").exists());
		assertFalse(new File(outputPath + "INSERT_SCR.txt").length() > 0);
		assertFalse(new File(outputPath + "INSERT_SCR_ELM.txt").exists());
		assertFalse(new File(outputPath + "INSERT_SCR_ELM.txt").length() > 0);
	}
}
