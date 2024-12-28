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

public class UpdateRecordServiceTest extends TestBase {

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
		var service = new UpdateRecordService();
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

		try {
			input.putString("requestUri", "/jl/service/top.html");
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
		var service = new UpdateRecordService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/tableDataMainte/updateRecord.html");
		input.putString("tableName", "MVIEWDEF");
		input.putString("recordId", "1000020");
		input.putString("FOREIGN_TABLE", "TEST_TABLE");

		service.doService(input, output);
		assertEquals("MVIEWDEF", output.getString("tableName"));
		assertEquals("1000020", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM MVIEWDEF WHERE MVIEWDEF_ID = 1000020");
		assertEquals("TEST_TABLE", recordList.get(0).get("FOREIGN_TABLE"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test03() throws SQLException {

		// カバレッジ(SQLで例外発生)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/tableDataMainte/updateRecord.html");
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

		// カバレッジ(VERSIONカラムあり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/tableDataMainte/updateRecord.html");
		input.putString("tableName", "MGNRGRP");
		input.putString("recordId", "1000001");
		input.putString("GNR_GRP_NAME", "システムプロパティ2");
		input.putString("VERSION", "1");

		service.doService(input, output);
		assertEquals("MGNRGRP", output.getString("tableName"));
		assertEquals("1000001", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM MGNRGRP WHERE MGNRGRP_ID = 1000001");
		assertEquals("システムプロパティ2", recordList.get(0).get("GNR_GRP_NAME"));
		assertEquals("2", recordList.get(0).get("VERSION"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test05() throws SQLException {

		// カバレッジ(UPDATE_DATEカラムあり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/tableDataMainte/updateRecord.html");
		input.putString("tableName", "MGNRGRP");
		input.putString("recordId", "1000001");
		input.putString("GNR_GRP_NAME", "システムプロパティ3");
		input.putString("UPDATE_DATE", "2021-07-12 00:00:00");

		service.doService(input, output);
		assertEquals("MGNRGRP", output.getString("tableName"));
		assertEquals("1000001", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM MGNRGRP WHERE MGNRGRP_ID = 1000001");
		assertEquals("システムプロパティ3", recordList.get(0).get("GNR_GRP_NAME"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test06() throws SQLException {

		// カバレッジ(UPDATE_USERカラムあり)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/tableDataMainte/updateRecord.html");
		input.putString("tableName", "MGNRGRP");
		input.putString("recordId", "1000001");
		input.putString("accountId", "dbadmin");
		input.putString("GNR_GRP_NAME", "システムプロパティ4");
		input.putString("UPDATE_USER", "dbadmin");

		service.doService(input, output);
		assertEquals("MGNRGRP", output.getString("tableName"));
		assertEquals("1000001", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM MGNRGRP WHERE MGNRGRP_ID = 1000001");
		assertEquals("システムプロパティ4", recordList.get(0).get("GNR_GRP_NAME"));
		assertEquals("dbadmin", recordList.get(0).get("UPDATE_USER"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}

	@Test
	void test07() throws SQLException {

		// カバレッジ(楽観ロックエラー)
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateRecordService();
		input.setDb(getDb());
		input.putString("requestKind", "GET");
		input.putString("requestUri", "/jl/service/tableDataMainte/updateRecord.html");
		input.putString("tableName", "MGNRGRP");
		input.putString("recordId", "1000001");
		input.putString("GNR_GRP_NAME", "システムプロパティ2");
		input.putString("VERSION", "1");

		service.doService(input, output);
		assertEquals("MGNRGRP", output.getString("tableName"));
		assertEquals("1000001", output.getString("recordId"));
		assertEquals("1", output.getString("updateCnt"));
		var recordList = input.getDb().select("SELECT * FROM MGNRGRP WHERE MGNRGRP_ID = 1000001");
		assertEquals("システムプロパティ2", recordList.get(0).get("GNR_GRP_NAME"));
		assertEquals("2", recordList.get(0).get("VERSION"));

		// バージョンを変えずにもう一度更新(楽観ロックエラーを起こさせる)
		input.putString("errMsgKey", "1000301");
		input.putString("GNR_GRP_NAME", "システムプロパティ2");
		service.doService(input, output);
		assertEquals("redirect", output.getString("respKind"));
		assertEquals("tableDataMainte/editRecord.html?tableName=MGNRGRP&recordId=1000001&errMsgKey=1",
				output.getString("destination"));

		// DB更新をロールバックする
		input.getDb().rollback();
	}
}
