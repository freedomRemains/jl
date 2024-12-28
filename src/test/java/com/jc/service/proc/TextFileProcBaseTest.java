package com.jc.service.proc;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.exception.ApplicationInternalException;
import com.jc.param.GenericParam;

public class TextFileProcBaseTest extends TestBase {

	@BeforeEach
	void beforeEach() {

		//
		// テストメソッドはprivateを付けてはいけない。
		// 各テストを実施する前の開始処理を記述する。
		//
	}

	@AfterEach
	void afterEach() {

		//
		// テストメソッドはprivateを付けてはいけない。
		// 各テストを実施する前の開始処理を記述する。
		//
	}

	//
	// 各テストメソッドはstatic、private禁止、戻り値も返却してはならない
	//

	@Test
	void test01() {

		// ファイルが存在しないパターン
		TextFileProcBase proc = new TextFileProcBase();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("filePath", "nowhere");
			proc.doProc(input, output, input.getString("filePath"));
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	private static final String RESOURCE_PATH = "src/test/resources/";

	@Test
	void test02() {

		// ファイルではなくディレクトリを指定するパターン
		TextFileProcBase proc = new TextFileProcBase();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("filePath", RESOURCE_PATH);
			proc.doProc(input, output, input.getString("filePath"));
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	@Test
	void test03() {

		// 正常系
		TextFileProcBase proc = new TextFileProcBase();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.putString("filePath", RESOURCE_PATH + "service/proc/TextFileProcBaseTest/test03.txt");
		proc.doProc(input, output, input.getString("filePath"));
		assertNull(output.getDb());
	}
}
