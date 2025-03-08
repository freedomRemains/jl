package com.jw.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.util.DbPrepareUtil;

public class ErrMsgUtilTest extends TestBase {

	@BeforeAll
	static void beforeAll() throws Exception {

		// DBの準備を行う
		new DbPrepareUtil().prepareDbIfNotYet(getDb());
	}

	@Test
	void test01() throws Exception {

		// 正常系
		String errMsgKey = new ErrMsgUtil().getErrMsgKey(getDb(), "dummySessionId", "1000001", "1000401");
		assertEquals("1", errMsgKey);

		// 正常系(最大IDありパターンテストのため、2連続でメソッドを実行)
		errMsgKey = new ErrMsgUtil().getErrMsgKey(getDb(), "dummySessionId", "1000001", "1000401");
		assertEquals("2", errMsgKey);
	}

	@Test
	void test02() throws Exception {

		// カバレッジ(SQLException)
		String errMsgKey = new ErrMsgUtil().getErrMsgKey(getDb(), "dummySessionId", "1000001", "dummmy");
		assertEquals("0", errMsgKey);
	}

	@Test
	void test03() throws Exception {

		// カバレッジ(SQLException)
		String errMsgKey = new ErrMsgUtil().getErrMsgKeyByMsg(getDb(), "dummySessionId", "dummyAccountId", "dummmy");
		assertEquals("0", errMsgKey);
	}
}
