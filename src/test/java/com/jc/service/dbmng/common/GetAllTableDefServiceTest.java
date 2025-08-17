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
import com.jc.db.DbInterface;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.DbPrepareUtil;
import com.jc.util.FileUtil;
import com.jc.util.JlProp;
import com.jc.util.Mu;

/**
 * テーブルの定義情報を取得するクラスです。
 */
public class GetAllTableDefServiceTest extends TestBase {

	@BeforeAll
	static void beforeAll() throws Exception {

		// DBの準備を行う
		new DbPrepareUtil().prepareDbIfNotYet(getDb());
	}

	private String dbName;

	@BeforeEach
	void beforeEach() {

		// テストに必要なフォルダを作成する
		dbName = "mysql";
		if ("com.jc.db.H2Db".equals(new JlProp().get("db.type"))) {
			dbName = "h2";
		}
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created");
	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		new FileUtil().deleteDirIfExists(OUTPUT_PATH);
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	private static final String TEST_RESOURCE_PATH = RESOURCE_PATH + "service/dbmng/common/GetAllTableDefServiceTest/";

	@Test
	void test01() throws Exception {

		// 必須パラメータがないパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		GetAllTableDefService service = new GetAllTableDefService();
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

		input.putString("dirPath", OUTPUT_PATH + "dbmng/" + dbName);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "defPath"), e.getLocalizedMessage());
		}

		input.putString("defPath", "10_dbdef/20_auto_created");
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "getTableDefSql"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws Exception {

		// DB定義取得用SQLを生成する
		String getTableDefSql = DbPrepareUtil.createGetTableDefSql();

		// DBの準備を行う
		prepareDb(getDb());

		// 存在しないテーブル名リストのパスが指定されているケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String ngTableNameListFilePath = "nowhere.txt";
		try {
			doService(dirPath, defPath, getTableDefSql, ngTableNameListFilePath);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}

		// テーブル名リストファイルのパスが指定されているケース
		String tableNameListFilePath = TEST_RESOURCE_PATH + "tableNameList.txt";
		doService(OUTPUT_PATH + "dbmng/" + dbName, "10_dbdef/20_auto_created", getTableDefSql,
				tableNameListFilePath);

		// DB定義ファイルが出力されていることを確認する
		String outputPath = OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/";
		assertTrue(new File(outputPath + "GNR_GRP.txt").exists());
		assertTrue(new File(outputPath + "GNR_GRP.txt").length() > 0);
		assertTrue(new File(outputPath + "GNR_KEY_VAL.txt").exists());
		assertTrue(new File(outputPath + "GNR_KEY_VAL.txt").length() > 0);
		assertTrue(new File(outputPath + "ACCNT.txt").exists());
		assertTrue(new File(outputPath + "ACCNT.txt").length() > 0);
		assertTrue(new File(outputPath + "SCR.txt").exists());
		assertTrue(new File(outputPath + "SCR.txt").length() > 0);
		assertTrue(new File(outputPath + "SCR_ELM.txt").exists());
		assertTrue(new File(outputPath + "SCR_ELM.txt").length() > 0);
	}

	@Test
	void test03() throws Exception {

		// DB定義取得用SQLを生成する
		String getTableDefSql = DbPrepareUtil.createGetTableDefSql();

		// DBの準備を行う
		prepareDb(getDb());

		// テーブル名リストに存在しないテーブル名が指定されているケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		addMapToList(tableNameList, "NOT_EXIST_TABLE");
		try {
			doServiceByTableNameList(dirPath, defPath, getTableDefSql, tableNameList);
			fail();
		} catch (Exception e) {

			// DB定義ファイルが出力されていないことを確認する
			assertNoFileOutput();
		}
	}

	@Test
	void test04() throws Exception {

		// DB定義取得用SQLを生成する
		String getTableDefSql = DbPrepareUtil.createGetTableDefSql();

		// DBの準備を行う
		prepareDb(getDb());

		// テーブル名リストが指定されているケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		addMapToList(tableNameList, "GNR_GRP");
		addMapToList(tableNameList, "GNR_KEY_VAL");
		addMapToList(tableNameList, "ACCNT");
		addMapToList(tableNameList, "SCR");
		addMapToList(tableNameList, "SCR_ELM");
		doServiceByTableNameList(dirPath, defPath, getTableDefSql, tableNameList);

		// DB定義ファイルが出力されていることを確認する
		String outputPath = OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/";
		assertTrue(new File(outputPath + "GNR_GRP.txt").exists());
		assertTrue(new File(outputPath + "GNR_GRP.txt").length() > 0);
		assertTrue(new File(outputPath + "GNR_KEY_VAL.txt").exists());
		assertTrue(new File(outputPath + "GNR_KEY_VAL.txt").length() > 0);
		assertTrue(new File(outputPath + "ACCNT.txt").exists());
		assertTrue(new File(outputPath + "ACCNT.txt").length() > 0);
		assertTrue(new File(outputPath + "SCR.txt").exists());
		assertTrue(new File(outputPath + "SCR.txt").length() > 0);
		assertTrue(new File(outputPath + "SCR_ELM.txt").exists());
		assertTrue(new File(outputPath + "SCR_ELM.txt").length() > 0);
	}

	@Test
	void test05() throws Exception {

		// DB定義取得用SQLを生成する
		String getTableDefSql = DbPrepareUtil.createGetTableDefSql();

		// DBの準備を行う
		prepareDb(getDb());

		// (カバレッジ)テーブル名リストにnullが指定されているケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		doServiceByTableNameList(dirPath, defPath, getTableDefSql, null);

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	@Test
	void test06() throws Exception {

		// DB定義取得用SQLを生成する
		String getTableDefSql = DbPrepareUtil.createGetTableDefSql();

		// DBの準備を行う
		prepareDb(getDb());

		// (カバレッジ)テーブル名リストに空のリストが指定されているケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		doServiceByTableNameList(dirPath, defPath, getTableDefSql,
				new ArrayList<LinkedHashMap<String, String>>());

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	private void prepareDb(DbInterface db) {

		// DBがH2の場合は、追加のSQLを実行する
		if (new JlProp().get("db.type").equals("com.jc.db.H2Db")) {
			GenericParam input = new GenericParam();
			input.setDb(db);
			input.putString("sqlFilePath", TEST_RESOURCE_PATH + "10_addSqlForH2.sql");
			input.putString("resultKey", "ret");
			GenericParam output = new GenericParam();
			DbUpdateBySqlFileService service = new DbUpdateBySqlFileService();
			service.doService(input, output);
		}
	}

	private void doService(String dirPath, String defPath, String getTableDefSql,
			String tableNameListFilePath) {

		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("getTableDefSql", getTableDefSql);
		input.putString("tableNameListFilePath", tableNameListFilePath);
		GenericParam output = new GenericParam();
		GetAllTableDefService service = new GetAllTableDefService();
		service.doService(input, output);
	}

	private void doServiceByTableNameList(String dirPath, String defPath, String getTableDefSql,
			ArrayList<LinkedHashMap<String, String>> tableNameList) {

		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("getTableDefSql", getTableDefSql);
		input.putRecordList("tableNameList", tableNameList);
		GenericParam output = new GenericParam();
		GetAllTableDefService service = new GetAllTableDefService();
		service.doService(input, output);
	}

	private void addMapToList(ArrayList<LinkedHashMap<String, String>> tableNameList, String tableName) {
		var tableNameMap = new LinkedHashMap<String, String>();
		tableNameMap.put("", tableName);
		tableNameList.add(tableNameMap);
	}

	private void assertNoFileOutput() {
		String outputPath = OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/";
		assertFalse(new File(outputPath + "GNR_GRP.txt").exists());
		assertFalse(new File(outputPath + "GNR_GRP.txt").length() > 0);
		assertFalse(new File(outputPath + "GNR_KEY_VAL.txt").exists());
		assertFalse(new File(outputPath + "GNR_KEY_VAL.txt").length() > 0);
		assertFalse(new File(outputPath + "ACCNT.txt").exists());
		assertFalse(new File(outputPath + "ACCNT.txt").length() > 0);
		assertFalse(new File(outputPath + "SCR.txt").exists());
		assertFalse(new File(outputPath + "SCR.txt").length() > 0);
		assertFalse(new File(outputPath + "SCR_ELM.txt").exists());
		assertFalse(new File(outputPath + "SCR_ELM.txt").length() > 0);
	}
}
