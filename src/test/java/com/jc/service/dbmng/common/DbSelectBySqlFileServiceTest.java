package com.jc.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.DbPrepareUtil;
import com.jc.util.Mu;

/**
 * ファイルとして用意されているselectのSQLを実行するサービスです。
 */
public class DbSelectBySqlFileServiceTest extends TestBase {

	@BeforeAll
	static void beforeAll() throws Exception {

		// DBの準備を行う
		new DbPrepareUtil().prepareDbIfNotYet(getDb());
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	private static final String TEST_RESOURCE_PATH = RESOURCE_PATH + "service/dbmng/common/DbSelectBySqlFileServiceTest/";

	@Test
	void test01() {

		// パラメータ指定なしパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		DbSelectBySqlFileService service = new DbSelectBySqlFileService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "sqlFilePath"), e.getLocalizedMessage());
		}

		try {
			input.putString("sqlFilePath", TEST_RESOURCE_PATH + "10_ng.sql");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "recordListKey"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// SQLエラーパターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("sqlFilePath", TEST_RESOURCE_PATH + "10_ng.sql");
		input.putString("recordListKey", "selectResult");
		GenericParam output = new GenericParam();
		DbSelectBySqlFileService service = new DbSelectBySqlFileService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("SQLSyntaxErrorException"));
		}
	}

	@Test
	void test03() throws Exception {

		// 正常系パターン
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("sqlFilePath", TEST_RESOURCE_PATH + "20_ok.sql");
		input.putString("recordListKey", "selectResult");
		GenericParam output = new GenericParam();
		DbSelectBySqlFileService service = new DbSelectBySqlFileService();

		service.doService(input, output);

		String sql = "SELECT * FROM SCR";
		ArrayList<LinkedHashMap<String, String>> recordList = input.getDb().select(sql);
		assertEquals(recordList.size(), output.getRecordList("selectResult").size());
	}
}
