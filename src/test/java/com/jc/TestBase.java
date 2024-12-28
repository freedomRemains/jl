package com.jc;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.jc.db.DbInterface;
import com.jc.util.DbUtil;
import com.jc.util.InnerClassPathProp;

/**
 * テストの基本クラスです。
 */
public class TestBase {

	protected static final String RESOURCE_PATH = "src/test/resources/";

	protected static final String OUTPUT_PATH = "output/";

	private static DbInterface db;

	protected static DbInterface getDb() {

		// インスタンスがまだ生成されていない場合のみ生成する
		if (db == null) {
			db = new DbUtil().getDb(new InnerClassPathProp("jl.properties"));
		}

		// DBインスタンスを呼び出し側に返却する
		return db;
	}

	protected static void closeDb() throws Exception {

		// DBをクローズし、変数もnullとする
		if (db != null) {
			db.close();
			db = null;
		}
	}

	@BeforeAll
	static void beforeAll() {

		//------------------------------------------------------------------//
		// このメソッドはstatic必須。privateも付けてはいけない。
		// 全てのテストを実施する前の開始処理を記述する。
		//------------------------------------------------------------------//

		// DBと接続する
		getDb();
	}

	@AfterAll
	static void afterAll() throws Exception {

		//------------------------------------------------------------------//
		// このメソッドはstatic必須。privateも付けてはいけない。
		// 全てのテストを実施した後の終了処理を記述する。
		//------------------------------------------------------------------//

		// DB接続をクローズする
		closeDb();
	}

	protected void assertFileContains(String filePath, String targetString) throws Exception {
		assertTrue(Files.readString(Paths.get(filePath)).contains(targetString));
	}

	protected void assertFileNotContains(String filePath, String targetString) throws Exception {
		assertFalse(Files.readString(Paths.get(filePath)).contains(targetString));
	}
}
