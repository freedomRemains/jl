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
import com.jc.util.FileUtil;
import com.jc.util.Mu;

/**
 * テーブルデータを取得するクラスです。
 */
public class GetAllTableDataServiceTest extends TestBase {

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

		// テーブルデータを取得するために必要なSELECTのSQLを生成する
		GetAllTableInsertSqlServiceTest.createAllTableSelectSql(dbName);
	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		new FileUtil().deleteDirIfExists(OUTPUT_PATH);
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// 必須パラメータがないパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		GetAllTableDataService service = new GetAllTableDataService();
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
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableNameListFilePath = RESOURCE_PATH + "service/dbmng/common/GetAllTableDefServiceTest/tableNameList.txt";
		doService(dirPath, dataPath, sqlPath, tableNameListFilePath);

		// DB定義ファイルが出力されていることを確認する
		assertFileOutput();
	}

	@Test
	void test03() {

		// テーブル名リストが指定されているケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		addMapToList(tableNameList, "MGNRGRP");
		addMapToList(tableNameList, "MGNRKEYVAL");
		addMapToList(tableNameList, "TACCOUNT");
		addMapToList(tableNameList, "TSCR");
		addMapToList(tableNameList, "TSCRELM");
		doServiceByTableNameList(dirPath, dataPath, sqlPath, tableNameList);

		// DB定義ファイルが出力されていることを確認する
		assertFileOutput();
	}

	@Test
	void test04() {

		// (カバレッジ)テーブル名リストがnullのケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		doServiceByTableNameList(dirPath, dataPath, sqlPath, null);

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	@Test
	void test05() {

		// (カバレッジ)テーブル名リストが空のケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		var tableNameList = new ArrayList<LinkedHashMap<String, String>>();
		doServiceByTableNameList(dirPath, dataPath, sqlPath, tableNameList);

		// DB定義ファイルが出力されていないことを確認する
		assertNoFileOutput();
	}

	@Test
	void test06() {

		// (カバレッジ)存在しないテーブル名リストファイルのパスが指定されているケース
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableNameListFilePath = "nowhere.txt";
		try {
			doService(dirPath, dataPath, sqlPath, tableNameListFilePath);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	private void doService(String dirPath, String dataPath, String sqlPath,
			String tableNameListFilePath) {

		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableNameListFilePath", tableNameListFilePath);
		GenericParam output = new GenericParam();
		GetAllTableDataService service = new GetAllTableDataService();
		service.doService(input, output);
	}

	private void doServiceByTableNameList(String dirPath, String dataPath, String sqlPath,
			ArrayList<LinkedHashMap<String, String>> tableNameList) {

		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putRecordList("tableNameList", tableNameList);
		GenericParam output = new GenericParam();
		GetAllTableDataService service = new GetAllTableDataService();
		service.doService(input, output);
	}

	private void addMapToList(ArrayList<LinkedHashMap<String, String>> tableNameList, String tableName) {
		var tableNameMap = new LinkedHashMap<String, String>();
		tableNameMap.put("", tableName);
		tableNameList.add(tableNameMap);
	}

	private void assertFileOutput() {

		// ファイルが出力されていることを確認する
		String outputPath = OUTPUT_PATH + "dbmng/" + dbName + "/20_dbdata/20_auto_created/";
		assertTrue(new File(outputPath + "MGNRGRP.txt").exists());
		assertTrue(new File(outputPath + "MGNRGRP.txt").length() > 0);
		assertTrue(new File(outputPath + "MGNRKEYVAL.txt").exists());
		assertTrue(new File(outputPath + "MGNRKEYVAL.txt").length() > 0);
		assertTrue(new File(outputPath + "TACCOUNT.txt").exists());
		assertTrue(new File(outputPath + "TACCOUNT.txt").length() > 0);
		assertTrue(new File(outputPath + "TSCR.txt").exists());
		assertTrue(new File(outputPath + "TSCR.txt").length() > 0);
		assertTrue(new File(outputPath + "TSCRELM.txt").exists());
		assertTrue(new File(outputPath + "TSCRELM.txt").length() > 0);
	}

	private void assertNoFileOutput() {

		// ファイルが出力されていないことを確認する
		String outputPath = OUTPUT_PATH + "dbmng/" + dbName + "/20_dbdata/20_auto_created/";
		assertFalse(new File(outputPath + "MGNRGRP.txt").exists());
		assertFalse(new File(outputPath + "MGNRGRP.txt").length() > 0);
		assertFalse(new File(outputPath + "MGNRKEYVAL.txt").exists());
		assertFalse(new File(outputPath + "MGNRKEYVAL.txt").length() > 0);
		assertFalse(new File(outputPath + "TACCOUNT.txt").exists());
		assertFalse(new File(outputPath + "TACCOUNT.txt").length() > 0);
		assertFalse(new File(outputPath + "TSCR.txt").exists());
		assertFalse(new File(outputPath + "TSCR.txt").length() > 0);
		assertFalse(new File(outputPath + "TSCRELM.txt").exists());
		assertFalse(new File(outputPath + "TSCRELM.txt").length() > 0);
	}
}
