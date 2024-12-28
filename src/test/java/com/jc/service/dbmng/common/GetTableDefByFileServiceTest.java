package com.jc.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

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
 * テーブルの定義情報を取得するクラスです。
 */
public class GetTableDefByFileServiceTest extends TestBase {

	private static final String TEST_PATH = RESOURCE_PATH + "service/dbmng/common";

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
		GetTableDefByFileService service = new GetTableDefByFileService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "tableName"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// 必要なパラメータを準備する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created/notExistPath";
		String tableName = "TSCR";

		// (カバレッジ)存在しないファイルを指定するパターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService service = new GetTableDefByFileService();
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
		String dirPath = TEST_PATH;
		String defPath = "GetTableDefByFileServiceTest";
		String tableName = "NO_HEADER";
		String tableDefFilePath = dirPath + "/" + defPath + "/" + tableName + ".txt";

		// (カバレッジ)ヘッダなしの処理ルートを通るパターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService service = new GetTableDefByFileService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.err.common.invalidTableDef", tableDefFilePath),
					e.getLocalizedMessage());
		}
	}

	@Test
	void test04() {

		// 必要なパラメータを準備する
		String dirPath = TEST_PATH;
		String defPath = "GetTableDefByFileServiceTest";
		String tableName = "BLANK_LINE";

		// (カバレッジ)ヘッダなしの処理ルートを通るパターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService service = new GetTableDefByFileService();
		service.doService(input, output);
		assertNotNull(output.getRecordList("tableDef" + tableName));
		assertEquals(8, output.getRecordList("tableDef" + tableName).size());
	}

	@Test
	void test05() {

		// 必要なパラメータを準備する
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String tableName = "TSCR";

		// 正常系
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("tableName", tableName);
		GenericParam output = new GenericParam();
		GetTableDefByFileService service = new GetTableDefByFileService();
		service.doService(input, output);
		assertNotNull(output.getRecordList("tableDef" + tableName));
		assertEquals(8, output.getRecordList("tableDef" + tableName).size());
	}
}
