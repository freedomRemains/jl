package com.jc.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.DbPrepareUtil;
import com.jc.util.FileUtil;
import com.jc.util.Mu;

/**
 * テーブル物理名取得が実行可能なDBで、全テーブルの名前を取得するクラスです。
 */
public class GetTableNameListServiceTest extends TestBase {

	@BeforeAll
	static void beforeAll() throws Exception {

		// DBの準備を行う
		new DbPrepareUtil().prepareDbIfNotYet(getDb());
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// 必須パラメータなしのパターン
		GetTableNameListService service = new GetTableNameListService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "db"), e.getLocalizedMessage());
		}

		try {
			input.setDb(getDb());
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "dirPath"), e.getLocalizedMessage());
		}

		try {
			input.putString("dirPath", OUTPUT_PATH + "dbmng/mysql");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "defPath"), e.getLocalizedMessage());
		}

		try {
			input.putString("defPath", "10_dbdef/20_auto_created");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "getTableNameListSql"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// SQLエラー
		String getTableNameListSql = """
				SELECT NOT_EXIST_COLUMN
				  FROM NOT_EXIST_TABLE
				  ;
				""";

		// 必要なディレクトリがなければ作成する
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/h2/10_dbdef/20_auto_created");

		// SQLエラーのパターン
		GetTableNameListService service = new GetTableNameListService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", OUTPUT_PATH + "dbmng/h2");
		input.putString("defPath", "10_dbdef/20_auto_created");
		input.putString("getTableNameListSql", getTableNameListSql);
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("SQLSyntaxErrorException"));
		}

		// 生成したディレクトリを削除する
		new FileUtil().deleteDirIfExists(OUTPUT_PATH);
	}

	@Test
	void test03() {

		// MySQLかH2かによってSQLを分ける
		String dbName = "h2";
		String getTableNameListSql = DbPrepareUtil.createGetTableNameListSql();

		// 必要なディレクトリがなければ作成する
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created");

		// 正常系のパターン
		GetTableNameListService service = new GetTableNameListService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.setDb(getDb());
		input.putString("dirPath", OUTPUT_PATH + "dbmng/" + dbName);
		input.putString("defPath", "10_dbdef/20_auto_created");
		input.putString("getTableNameListSql", getTableNameListSql);
		service.doService(input, output);
		assertTrue(new File(OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/tableNameList.txt").exists());

		// 生成したディレクトリを削除する
		new FileUtil().deleteDirIfExists(OUTPUT_PATH);
	}
}
