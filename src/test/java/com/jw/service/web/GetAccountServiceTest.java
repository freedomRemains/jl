package com.jw.service.web;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.db.GenericDb;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.DbPrepareUtil;
import com.jc.util.Mu;
import com.jw.exception.RoleRestrictionException;

public class GetAccountServiceTest extends TestBase {

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
		var service = new GetAccountService();
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

		// 正常系(アカウントID指定なし)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetAccountService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/top.html");

		service.doService(input, output);
		assertEquals("1000001", output.getRecordList("account").get(0).get("ACCNT_ID"));
		assertEquals("ゲスト", output.getRecordList("account").get(0).get("ACCOUNT_NAME"));
	}

	@Test
	void test03() {

		// 正常系(ロール制約を受けるURLにアクセス)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetAccountService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/dbMainte.html");

		try {
			service.doService(input, output);
			fail();
		} catch (RoleRestrictionException e) {
			assertTrue(e.getLocalizedMessage().contains(
					"[{APROLE_ID=1000301}, {APROLE_ID=1000401}]"));
		}
	}

	@Test
	void test04() {

		// カバレッジ(ロール制約をクリアできるアカウントID)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetAccountService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/dbMainte.html");
		input.putString("accountId", "1000301");

		service.doService(input, output);
		assertEquals("1000301", output.getRecordList("account").get(0).get("ACCNT_ID"));
		assertEquals("マスタ", output.getRecordList("account").get(0).get("ACCOUNT_NAME"));
	}

	@Test
	void test05() {

		// カバレッジ(DBアクセス時に例外発生)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new GetAccountService();
		input.setDb(new GenericDb()); // 単にnewしただけではDB接続していないので、例外が発生する
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/nowhere.html");

		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains(
					"NullPointerException"));
		}
	}
}
