package com.jc.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

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
 * 各テーブルのSQLを取得するクラスです。
 */
public class GetTableSelectSqlServiceTest extends TestBase {

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
	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		new DbPrepareUtil().afterEach();
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// 必須パラメータがないパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		GetTableSelectSqlService service = new GetTableSelectSqlService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "defPath"), e.getLocalizedMessage());
		}

		String defPath = "10_dbdef/20_auto_created";
		input.putString("defPath", defPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "sqlPath"), e.getLocalizedMessage());
		}

		String sqlPath = "30_sql/20_auto_created";
		input.putString("sqlPath", sqlPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "tableName"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// 必要なパラメータを準備する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String tableName = "TSCR";

		// (カバレッジ)存在しない出力先を指定するパターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", "noexistpath"); // 存在しない出力先
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableSelectSqlService service = new GetTableSelectSqlService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	@Test
	void test03() {

		// 必要なパラメータを準備する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "TSCR";

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableSelectSqlService service = new GetTableSelectSqlService();
		service.doService(input, output);

		// ファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		assertTrue(new File(outputPath + "SELECT_TSCR.txt").exists());
		assertTrue(new File(outputPath + "SELECT_TSCR.txt").length() > 0);
	}

	@Test
	void test04() {

		// 必要なパラメータを準備する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "TSCR";

		// カバレッジ(オンメモリにテーブルリストがある状態で実行)
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService preExecService = new GetTableDefByFileService();
		preExecService.doService(input, output);
		var tableDef = output.getRecordList("tableDef" + tableName);
		input.putRecordList("tableDef" + tableName, tableDef);
		GetTableSelectSqlService service = new GetTableSelectSqlService();
		service.doService(input, output);

		// ファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		assertTrue(new File(outputPath + "SELECT_TSCR.txt").exists());
		assertTrue(new File(outputPath + "SELECT_TSCR.txt").length() > 0);

		// カバレッジ(サイズが0のtableDef)
		tableDef.clear();
		input.putRecordList("tableDef" + tableName, tableDef);
		service.doService(input, output);

		// オンメモリの空情報は使わず、ファイルが生成されていることを確認する
		assertTrue(new File(outputPath + "SELECT_TSCR.txt").exists());
		assertTrue(new File(outputPath + "SELECT_TSCR.txt").length() > 0);
	}
}
