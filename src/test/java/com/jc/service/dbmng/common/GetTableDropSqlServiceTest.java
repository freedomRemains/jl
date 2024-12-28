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
 * 各テーブルのSQLを取得するクラスです。 <br>
 * <br>
 * [inputに必要なパラメータ]<br>
 * dirPath ディレクトリパス<br>
 * sqlPath SQLパス<br>
 * tableName テーブル名<br>
 * tableDefFilePath テーブル定義ファイルパス<br>
 * <br>
 * 各パラメータの例<br>
 * dirPath new JwProp().get("base.dir") + PATH_DELM + getResourcePath()<br>
 * sqlPath "30_sql/20_auto_created"<br>
 * tableName "TDICTITEM"<br>
 * tableDefFilePath = input.getString("dirPath") + PATH_DELM<br>
 * + input.getString("defPath") + PATH_DELM + tableName + ".txt";<br>
 */
public class GetTableDropSqlServiceTest extends TestBase {

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
		GetTableDropSqlService service = new GetTableDropSqlService();
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
		String tableName = "TSCR";

		// (カバレッジ)存在しない出力先を指定するパターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("sqlPath", "noexistpath"); // 存在しない出力先
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDropSqlService service = new GetTableDropSqlService();
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
		String sqlPath = "30_sql/20_auto_created";
		String tableName = "TSCR";

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("sqlPath", sqlPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDropSqlService service = new GetTableDropSqlService();
		service.doService(input, output);

		// ファイルが生成されていることを確認する
		String outputPath = dirPath + "/" + sqlPath + "/";
		assertTrue(new File(outputPath + "DROP_TSCR.txt").exists());
		assertTrue(new File(outputPath + "DROP_TSCR.txt").length() > 0);
	}
}
