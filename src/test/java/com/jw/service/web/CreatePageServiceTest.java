package com.jw.service.web;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.DbPrepareUtil;
import com.jc.util.Mu;

public class CreatePageServiceTest extends TestBase {

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
		var service = new CreatePageService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "accountId"), e.getLocalizedMessage());
		}

		try {
			input.putString("accountId", "data_loader");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "tableName"), e.getLocalizedMessage());
		}

		try {
			input.putString("tableName", "MHTMLPAGE");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "PAGE_NAME"), e.getLocalizedMessage());
		}

		try {
			input.putString("PAGE_NAME", "新規パーツ追加");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			// TODO 「URI_PATTERN」に変更する
			assertEquals(new Mu().msg("msg.common.noParam", "MURIPATTERN_ID"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() throws SQLException {

		// 正常系
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreatePageService();
		input.setDb(getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "MHTMLPAGE");
		input.putString("PAGE_NAME", "新規パーツ追加");
		// TODO 「URI_PATTERN」に変更する
		input.putString("MURIPATTERN_ID", "/newHtmlParts");

		service.doService(input, output);
		var recordList = input.getDb().select("SELECT * FROM MURIPATTERN WHERE URI_PATTERN = '/newHtmlParts'");
		assertEquals(1, recordList.size());

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test03() throws SQLException {

		// カバレッジ(キー重複)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new CreatePageService();
		input.setDb(getDb());
		input.putString("accountId", "data_loader");
		input.putString("tableName", "MHTMLPAGE");
		input.putString("PAGE_NAME", "TOP");
		// TODO 「URI_PATTERN」に変更する
		input.putString("MURIPATTERN_ID", "/jl/service/top.html");

		// サービスを実行する
		service.doService(input, output);

		// エラーメッセージキーを含むリダイレクトのレスポンスになっていることを確認する
		assertEquals("redirect", output.getString("respKind"));
		assertTrue(output.getString("destination").startsWith("editPage.html?errMsgKey="));

		// DB更新をロールバックする
		input.getDb().rollback();
	}
}
