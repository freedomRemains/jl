package com.jc.service.dbmng.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.FileUtil;
import com.jc.util.JlProp;
import com.jc.util.Mu;

public class UpdateAllTableServiceTest extends TestBase {

	private static String dbName;

	@BeforeEach
	void beforeEach() {

		// テストに必要なフォルダを作成する
		dbName = "mysql";
		if ("com.jc.db.H2Db".equals(new JlProp().get("db.type"))) {
			dbName = "h2";
		}
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/" + dbName + "/10_dbdef/20_auto_created");
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "dbmng/" + dbName + "/30_sql/20_auto_created");	
	}

	@AfterEach
	void afterEach() {

		// テストフォルダを削除する
		new FileUtil().deleteDirIfExists(OUTPUT_PATH);
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// 必須パラメータがないパターン
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UpdateAllTableService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "db"), e.getLocalizedMessage());
		}

		input.setDb(getDb());
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "dirPath"), e.getLocalizedMessage());
		}

		String dirPath = OUTPUT_PATH + "dbmng/" + dbName;
		input.putString("dirPath", dirPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "dataPath"), e.getLocalizedMessage());
		}

		String dataPath = "20_dbdata";
		input.putString("dataPath", dataPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "sqlPath"), e.getLocalizedMessage());
		}

		String sqlPath = "30_sql";
		input.putString("sqlPath", sqlPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "authorizedPath"), e.getLocalizedMessage());
		}

		String authorizedPath = "10_authorized";
		input.putString("authorizedPath", authorizedPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "autoCreatedPath"), e.getLocalizedMessage());
		}

		String autoCreatedPath = "20_auto_created";
		input.putString("autoCreatedPath", autoCreatedPath);
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "forUpdatePath"), e.getLocalizedMessage());
		}
	}

	// 本クラスの正常系はScriptServiceTestで実施しているため、省略する。
	// 本格的に正常系のテストを実施する場合は、事前にDB構成取得のスクリプトサービスを実行し、
	// 所定の位置にDB定義ファイルとDBデータファイルを配置する必要がある。
}
