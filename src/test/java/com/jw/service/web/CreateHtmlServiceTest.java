package com.jw.service.web;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.DbPrepareUtil;
import com.jc.util.Mu;

public class CreateHtmlServiceTest extends TestBase {

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
		var service = new CreateHtmlService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "requestKind"), e.getLocalizedMessage());
		}

		try {
			input.putString("requestKind", "GET");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "requestUri"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// 正常系
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateHtmlService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/top.html");

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));
	}

	@Test
	void test03() throws Exception {

		// カバレッジ(ITEM_QUERYでSQLエラーが発生)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateHtmlService();
		input.setDb(getDb());
		String requestKind = "GET";
		input.putString("requestKind", requestKind);
		String requestUri = "/jl/service/top.html";
		input.putString("requestUri", requestUri);

		// リクエストを受けられるよう、DBレコードを更新する
		String sql = """
				UPDATE TPARTSITEM SET
					ITEM_QUERY = 'SELECT ERR_SQL FROM NOT_EXIST_TABLE'
					WHERE TPARTSITEM_ID = 1000001
				""";
		getDb().update(sql);

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					"SQLException"));

			// DBをロールバックする
			getDb().rollback();
		}
	}

	@Test
	void test04() throws Exception {

		// カバレッジ(ITEM_QUERYが存在しない)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateHtmlService();
		input.setDb(getDb());
		String requestKind = "GET";
		input.putString("requestKind", requestKind);
		String requestUri = "/jl/service/top.html";
		input.putString("requestUri", requestUri);

		// リクエストを受けられるよう、DBレコードを更新する
		String sql = """
				UPDATE TPARTSITEM SET
					ITEM_QUERY = ''
					WHERE TPARTSITEM_ID = 1000001
				""";
		getDb().update(sql);

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));
	}

	@Test
	void test05() {

		// カバレッジ(limit、offsetの指定あり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateHtmlService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/tableDataMainte.html");
		input.putString("tableName", "TBLDEF");
		input.putString("limit", "10");
		input.putString("offset", "10");

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));
	}

	@Test
	void test06() {

		// カバレッジ(errMsgKey指定あり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreateHtmlService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/top.html");
		input.putString("errMsgKey", "9999999");

		service.doService(input, output);
		assertEquals("forward", output.getString("respKind"));
		assertEquals("10000_contents.jsp", output.getString("destination"));
	}
}
