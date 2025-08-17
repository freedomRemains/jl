package com.jc.service.script;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
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
 * スクリプトで複数のサービスを連続実行します。
 */
public class ScriptServiceTest extends TestBase {

	/** ロガー */
	private Logger logger = Logger.getLogger(this.getClass().getName());

	@BeforeAll
	static void beforeAll() throws Exception {

		// DBの準備を行う
		new DbPrepareUtil().prepareDbIfNotYet(getDb());
	}

	private String dbName;

	@BeforeEach
	void beforeEach() throws Exception {

		// テストに必要な準備処理を実行する
		dbName = new DbPrepareUtil().beforeEach();
	}

	@AfterEach
	void afterEach() throws Exception {

		// テストフォルダを削除する
		new DbPrepareUtil().afterEach();
	}

	@AfterAll
	static void afterAll() throws Exception {

		// DB接続を一度クローズする
		closeDb();

		// DROP-CREATEはロールバックできないため、DB構成更新を複数回実施するこのクラスでは
		// コミットしなくても、あるいはロールバックしても、DBは消えた状態になる。
		// そのためテストの最後でDBを復元する必要がある
		new DbPrepareUtil().restoreDb(getDb());
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// 正常系動作確認に必要なパラメータを作成する
		var input = createNormalInput();

		// 正常系
		var output = new GenericParam();
		var service = new ScriptService();
		service.doService(input, output);
		assertOutput(dbName);

		// 期待値ファイルを上書きする場合は、次のコードを有効にする
		//String dirPath = RESOURCE_PATH + "service/script/dbmng/" + dbName;
		//input.putString("dirPath", dirPath);
		//service.doService(input,  output);
	}

	@Test
	void test02() {

		// 事前にDB構成取得処理を実行する
		var input = createNormalInput();
		var output = new GenericParam();
		var service = new ScriptService();
		service.doService(input, output);
		assertOutput(dbName);

		// 正常系動作確認に必要なパラメータを作成する
		input = createNormalInputUpdateAll();

		// 期待値ファイルを上書きする場合は、次のコードを有効にする
		//String dirPath = RESOURCE_PATH + "service/script/dbmng/" + dbName;
		//input.putString("dirPath", dirPath);

		// 正常系(DB構成更新)
		output = new GenericParam();
		service.doService(input, output);

		// DB構成更新後に改めてDB構成取得を実施し、完全一致していれば正常と判断する
		input = createNormalInput();
		output = new GenericParam();
		service.doService(input, output);
		assertOutput(dbName);

		// 期待値ファイルを上書きする場合は、次のコードを有効にする
		//dirPath = RESOURCE_PATH + "service/script/dbmng/" + dbName;
		//input.putString("dirPath", dirPath);
		//service.doService(input,  output);
	}

	@Test
	void test03() {

		// SCR_PRMを使用し、パラメータ指定を最小限とするパターンのテスト
		// 最初にDB構成取得を実施し、output配下に各種SQLを出力する
		// (こうしないとDB構成更新の元ネタであるDB定義ファイルやDBデータがoutput配下にない状態となるため)
		var input = new GenericParam();
		input.setDb(getDb());
		input.putString("scriptId", "1000001");
		input.putString("basePath", OUTPUT_PATH);
		input.putString("dbName", dbName);
		input.putString("getTableNameListSql", DbPrepareUtil.createGetTableNameListSql());
		input.putString("getTableDefSql", DbPrepareUtil.createGetTableDefSql());
		var output = new GenericParam();
		var service = new ScriptService();
		service.doService(input, output);
		assertOutput(dbName);

		// 正常系動作確認に必要なパラメータを作成する
		input.setDb(getDb());
		input.putString("scriptId", "1000002");
		input.putString("basePath", OUTPUT_PATH);
		input.putString("dbName", dbName);

		// 正常系(DB構成更新)
		service.doService(input, output);

		// DB構成更新後に改めてDB構成取得を実施し、妥当性検証を行う
		input.putString("scriptId", "1000001");
		output = new GenericParam();
		service.doService(input, output);
		assertOutput(dbName);
	}

	@Test
	void test11() {

		// 必須パラメータなしのパターン
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		ScriptService service = new ScriptService();
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
			assertEquals(new Mu().msg("msg.common.noParam", "scriptId"), e.getLocalizedMessage());
		}
	}

	@Test
	void test12() throws SQLException {

		// 正常系動作確認に必要なパラメータを作成する
		GenericParam input = createNormalInput();

		// アダプタクラスと準備クラスを設定に追加する
		String adapterClassName = "com.jc.service.dbmng.common.impl.BlankAdapter";
		String prepareClassName = "com.jc.service.dbmng.common.impl.BlankPrepare";
		String sql = "UPDATE SCR_ELM SET ADAPTER = '" + adapterClassName + "', PREPARE_INPUT = '"
				+ prepareClassName + "' WHERE SCR_ELM_ID = 1000001";
		getDb().update(sql);

		// カバレッジ(アダプタ、準備クラス指定あり)
		GenericParam output = new GenericParam();
		ScriptService service = new ScriptService();
		service.doService(input, output);
		assertOutput(dbName);
	}

	@Test
	void test13() throws SQLException {

		// 正常系動作確認に必要なパラメータを作成する
		GenericParam input = createNormalInput();

		// サービス名に存在しないクラスを指定する
		String sql = "UPDATE SCR_ELM SET SERVICE_NAME = 'notExistService' WHERE SCR_ELM_ID = 1000001";
		getDb().update(sql);

		// カバレッジ(存在しないサービスクラス)
		GenericParam output = new GenericParam();
		ScriptService service = new ScriptService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noClass", "notExistService"), e.getLocalizedMessage());
		}
	}

	@Test
	void test14() throws SQLException {

		// 正常系動作確認に必要なパラメータを作成する
		GenericParam input = createNormalInput();

		// エラーが起きるサービスを設定する
		String sql = "UPDATE SCR_ELM SET SERVICE_NAME = 'com.jc.service.dbmng.common.impl.ErrorService' WHERE SCR_ELM_ID = 1000001";
		getDb().update(sql);

		// カバレッジ(サービス内で例外発生)
		GenericParam output = new GenericParam();
		ScriptService service = new ScriptService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("notExistFile"));
		}
	}

	@Test
	void test15() {
		var input = new GenericParam();
		String after = ScriptService.convertVariable(input, "", logger);
		assertEquals("", after);
		after = ScriptService.convertVariable(input, null, logger);
		assertEquals(null, after);
	}

	@Test
	void test16() throws SQLException {

		// 正常系動作確認に必要なパラメータを作成する
		GenericParam input = createNormalInput();

		// エラーが起きるサービスを設定する
		String sql = "UPDATE SCR_ELM SET SERVICE_NAME = 'com.jc.service.dbmng.common.impl.ApErrorService' WHERE SCR_ELM_ID = 1000001";
		getDb().update(sql);

		// カバレッジ(サービス内で例外発生)
		GenericParam output = new GenericParam();
		ScriptService service = new ScriptService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("ApplicationErrorDetected"));
		}
	}

	@Test
	void test17() throws SQLException {

		// 正常系動作確認に必要なパラメータを作成する
		GenericParam input = createNormalInput();

		// エラーが起きるサービスを設定する
		String sql = "UPDATE SCR_ELM SET SERVICE_NAME = 'com.jc.service.dbmng.common.impl.BrErrorService' WHERE SCR_ELM_ID = 1000001";
		getDb().update(sql);

		// カバレッジ(サービス内で例外発生)
		GenericParam output = new GenericParam();
		ScriptService service = new ScriptService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertTrue(e.getLocalizedMessage().contains("BusinessRuleViolationErrorDetected"));
		}
	}

	/**
	 * DB構成取得
	 * scriptId : 1000001
	 * dirPath : [任意のフォルダ]/dbmng/[db名]
	 * defPath : "10_dbdef/20_auto_created"固定
	 * dataPath : "20_dbdata/20_auto_created"固定
	 * sqlPath : "30_sql/20_auto_created"固定
	 * getTableNameListSql : テーブルリスト取得のSQL
	 * getTableDefSql : テーブル定義取得のSQL
	 */
	private GenericParam createNormalInput() {

		// 必要なパラメータを準備する
		String scriptId = "1000001";
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef/20_auto_created";
		String dataPath = "20_dbdata/20_auto_created";
		String sqlPath = "30_sql/20_auto_created";
		String getTableNameListSql = DbPrepareUtil.createGetTableNameListSql();
		String getTableDefSql = DbPrepareUtil.createGetTableDefSql();

		// 正常系動作確認に必要なパラメータを作成する
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("scriptId", scriptId);
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("getTableNameListSql", getTableNameListSql);
		input.putString("getTableDefSql", getTableDefSql);

		// 正常系動作確認に必要な入力パラメータを呼び出し側に返却する
		return input;
	}

	/**
	 * DB構成更新
	 * scriptId : 1000002
	 * dirPath : [任意のフォルダ]/dbmng/[db名]
	 * defPath : "10_dbdef"固定
	 * dataPath : "20_dbdata"固定
	 * sqlPath : "30_sql"固定
	 * authorizedPath : "10_authorized"固定
	 * autoCreatedPath : "20_auto_created"固定
	 * forUpdatePath : "30_for_update"固定
	 */
	private GenericParam createNormalInputUpdateAll() {

		// 必要なパラメータを準備する
		String scriptId = "1000002";
		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		String defPath = "10_dbdef";
		String dataPath = "20_dbdata";
		String sqlPath = "30_sql";
		String authorizedPath = "10_authorized";
		String autoCreatedPath = "20_auto_created";
		String forUpdatePath = "30_for_update";

		// 正常系動作確認に必要なパラメータを作成する
		GenericParam input = new GenericParam();
		input.setDb(getDb());
		input.putString("scriptId", scriptId);
		input.putString("dirPath", dirPath);
		input.putString("defPath", defPath);
		input.putString("dataPath", dataPath);
		input.putString("sqlPath", sqlPath);
		input.putString("authorizedPath", authorizedPath);
		input.putString("autoCreatedPath", autoCreatedPath);
		input.putString("forUpdatePath", forUpdatePath);

		// 正常系動作確認に必要な入力パラメータを呼び出し側に返却する
		return input;
	}

	private void assertOutput(String dbName) {

		String outputPath = OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created/";
		assertTrue(new File(outputPath + "GNR_GRP.txt").exists());
		assertTrue(new File(outputPath + "GNR_GRP.txt").length() > 0);
		assertTrue(new File(outputPath + "GNR_KEY_VAL.txt").exists());
		assertTrue(new File(outputPath + "GNR_KEY_VAL.txt").length() > 0);
		assertTrue(new File(outputPath + "tableNameList.txt").exists());
		assertTrue(new File(outputPath + "tableNameList.txt").length() > 0);
		assertTrue(new File(outputPath + "ACCNT.txt").exists());
		assertTrue(new File(outputPath + "ACCNT.txt").length() > 0);
		assertTrue(new File(outputPath + "SCR.txt").exists());
		assertTrue(new File(outputPath + "SCR.txt").length() > 0);
		assertTrue(new File(outputPath + "SCR_ELM.txt").exists());
		assertTrue(new File(outputPath + "SCR_ELM.txt").length() > 0);

		outputPath = OUTPUT_PATH + "dbmng/" + dbName + "/20_dbdata/20_auto_created/";
		assertTrue(new File(outputPath + "GNR_GRP.txt").exists());
		assertTrue(new File(outputPath + "GNR_GRP.txt").length() > 0);
		assertTrue(new File(outputPath + "GNR_KEY_VAL.txt").exists());
		assertTrue(new File(outputPath + "GNR_KEY_VAL.txt").length() > 0);
		assertTrue(new File(outputPath + "ACCNT.txt").exists());
		assertTrue(new File(outputPath + "ACCNT.txt").length() > 0);
		assertTrue(new File(outputPath + "SCR.txt").exists());
		assertTrue(new File(outputPath + "SCR.txt").length() > 0);
		assertTrue(new File(outputPath + "SCR_ELM.txt").exists());
		assertTrue(new File(outputPath + "SCR_ELM.txt").length() > 0);

		outputPath = OUTPUT_PATH + "dbmng/" + dbName + "/30_sql/20_auto_created/";
		assertTrue(new File(outputPath + "DROP_GNR_GRP.txt").exists());
		assertTrue(new File(outputPath + "DROP_GNR_GRP.txt").length() > 0);
		assertTrue(new File(outputPath + "DROP_GNR_KEY_VAL.txt").exists());
		assertTrue(new File(outputPath + "DROP_GNR_KEY_VAL.txt").length() > 0);
		assertTrue(new File(outputPath + "DROP_ACCNT.txt").exists());
		assertTrue(new File(outputPath + "DROP_ACCNT.txt").length() > 0);
		assertTrue(new File(outputPath + "DROP_SCR.txt").exists());
		assertTrue(new File(outputPath + "DROP_SCR.txt").length() > 0);
		assertTrue(new File(outputPath + "DROP_SCR_ELM.txt").exists());
		assertTrue(new File(outputPath + "DROP_SCR_ELM.txt").length() > 0);

		assertTrue(new File(outputPath + "INSERT_GNR_GRP.txt").exists());
		assertTrue(new File(outputPath + "INSERT_GNR_GRP.txt").length() > 0);
		assertTrue(new File(outputPath + "INSERT_GNR_KEY_VAL.txt").exists());
		assertTrue(new File(outputPath + "INSERT_GNR_KEY_VAL.txt").length() > 0);
		assertTrue(new File(outputPath + "INSERT_ACCNT.txt").exists());
		assertTrue(new File(outputPath + "INSERT_ACCNT.txt").length() > 0);
		assertTrue(new File(outputPath + "INSERT_SCR.txt").exists());
		assertTrue(new File(outputPath + "INSERT_SCR.txt").length() > 0);
		assertTrue(new File(outputPath + "INSERT_SCR_ELM.txt").exists());
		assertTrue(new File(outputPath + "INSERT_SCR_ELM.txt").length() > 0);

		assertTrue(new File(outputPath + "SELECT_GNR_GRP.txt").exists());
		assertTrue(new File(outputPath + "SELECT_GNR_GRP.txt").length() > 0);
		assertTrue(new File(outputPath + "SELECT_GNR_KEY_VAL.txt").exists());
		assertTrue(new File(outputPath + "SELECT_GNR_KEY_VAL.txt").length() > 0);
		assertTrue(new File(outputPath + "SELECT_ACCNT.txt").exists());
		assertTrue(new File(outputPath + "SELECT_ACCNT.txt").length() > 0);
		assertTrue(new File(outputPath + "SELECT_SCR.txt").exists());
		assertTrue(new File(outputPath + "SELECT_SCR.txt").length() > 0);
		assertTrue(new File(outputPath + "SELECT_SCR_ELM.txt").exists());
		assertTrue(new File(outputPath + "SELECT_SCR_ELM.txt").length() > 0);
	}
}
