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

public class GetRelatedRecordServiceTest extends TestBase {

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
		var service = new GetRelatedRecordService();
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
			input.putString("tableName", "HTML_PAGE");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "recordId"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// 正常系
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetRelatedRecordService();
		input.setDb(getDb());
		input.putString("tableName", "HTML_PAGE");
		input.putString("recordId", "1000301");

		service.doService(input, output);
		assertEquals("PARTS_IN_PAGE", output.getRecordList("relatedTableList").get(0).get("TABLE_NAME"));
		assertEquals("REQUIRE_APROLE", output.getRecordList("relatedTableList").get(1).get("TABLE_NAME"));
	}

	@Test
	void test03() {

		// カバレッジ(組み合わせテーブルではない関連)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetRelatedRecordService();
		input.setDb(getDb());
		input.putString("tableName", "GNR_GRP");
		input.putString("recordId", "1000001");

		service.doService(input, output);
		assertEquals("GNR_KEY_VAL", output.getRecordList("relatedTableList").get(0).get("TABLE_NAME"));
	}

	@Test
	void test04() {

		// カバレッジ(存在しないテーブル)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetRelatedRecordService();
		input.setDb(getDb());
		input.putString("tableName", "NOTEXISTTABLE");
		input.putString("recordId", "9999999");

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("SQLException"));
		}
	}

	@Test
	void test05() {

		// カバレッジ(VERSION以降のカラムがなく、DB定義のループが回りきるテーブル)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetRelatedRecordService();
		input.setDb(getDb());
		input.putString("tableName", "VIEW_DEF");
		input.putString("recordId", "1000001");

		service.doService(input, output);
		assertTrue(output.getRecordList("relatedTableList").size() == 0);
	}

	@Test
	void test06() {

		// カバレッジ
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetRelatedRecordService();
		input.setDb(getDb());
		input.putString("tableName", "SCR");
		input.putString("recordId", "1100301");

		service.doService(input, output);
		assertEquals("HTML_PAGE", output.getRecordList("relatedTableList").get(2).get("TABLE_NAME"));
	}

	@Test
	void test07() throws SQLException {

		// カバレッジ(処理対象レコードなし)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetRelatedRecordService();
		input.setDb(getDb());
		input.putString("tableName", "HTML_PAGE");
		input.putString("recordId", "9999999");

		service.doService(input, output);
		assertEquals("redirect", output.getString("respKind"));
		assertEquals("tableDataMainte.html?tableName=HTML_PAGE&errMsgKey=1",
				output.getString("destination"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test08() throws SQLException {

		// カバレッジ(関連テーブルレコードなし)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetRelatedRecordService();
		input.setDb(getDb());
		input.putString("tableName", "HTML_PAGE");
		input.putString("recordId", "1000001");

		service.doService(input, output);
		assertTrue(output.getRecordList("foreignTableRecordList_REQUIRE_APROLE").isEmpty());
	}
}
