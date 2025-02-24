package com.jw.service.web;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.DbPrepareUtil;
import com.jc.util.Mu;

public class DeleteRecordServiceTest extends TestBase {

	@BeforeAll
	static void beforeAll() throws Exception {

		// DBの準備を行う
		new DbPrepareUtil().prepareDbIfNotYet(getDb());
	}

	@Test
	void test01() {

		// パラメータ指定なしパターン
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new DeleteRecordService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "tableName"), e.getLocalizedMessage());
		}

		try {
			input.putString("tableName", "MVIEWDEF");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "recordId"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws SQLException {

		// 正常系
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new DeleteRecordService();
		input.setDb(getDb());
		input.putString("tableName", "MVIEWDEF");
		input.putString("recordId", "1000020");

		service.doService(input, output);
		assertEquals("MVIEWDEF", output.getString("tableName"));
		assertEquals("1000020", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM MVIEWDEF WHERE MVIEWDEF_ID = 1000020");
		assertEquals(0, recordList.size());

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test03() throws SQLException {

		// カバレッジ(SQLで例外発生)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new DeleteRecordService();
		input.setDb(getDb());
		input.putString("tableName", "NOTEXISTTABLE");
		input.putString("recordId", "1");

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("SQLException"));
		}

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test04() throws SQLException {

		// カバレッジ(削除エラー)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new DeleteRecordService();
		input.setDb(getDb());
		input.putString("tableName", "MVIEWDEF");
		input.putString("recordId", "1000020");

		service.doService(input, output);
		assertEquals("MVIEWDEF", output.getString("tableName"));
		assertEquals("1000020", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM MVIEWDEF WHERE MVIEWDEF_ID = 1000020");
		assertEquals(0, recordList.size());

		// もう一度削除(削除エラーを起こさせる)
		input.putString("errMsgKey", "9999999");
		service.doService(input, output);
		assertEquals("redirect", output.getString("respKind"));
		assertEquals("tableDataMainte.html?tableName=MVIEWDEF&errMsgKey=1",
				output.getString("destination"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}
}
