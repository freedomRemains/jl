package com.jc.service.fs;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.Mu;

/**
 * ディレクトリを再帰的に処理していくサービスです。<br>
 * このサービスはディレクトリごとにinput、outputの内容を取り回して処理するため、<br>
 * 呼び出し側でinputとoutputを上位から引き継がずに呼び出すと、混乱やバグを防ぎやすいです。<br>
 * 
 * [inputに必要なパラメータ]<br>
 * dirPath ディレクトリパス<br>
 * procName プロシージャ名(ディレクトリごとの処理を記述するクラスの名前)<br>
 * <br>
 * "procName"はディレクトリ処理クラスの完全修飾名を指定する。<br>
 */
public class DirRecursiveServiceTest extends TestBase {

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

		// 必須パラメータなしのパターン
		DirRecursiveService service = new DirRecursiveService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "dirPath"), e.getLocalizedMessage());
		}

		try {
			input.putString("dirPath", "../log");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "procName"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// 存在しないディレクトリを指定するパターン
		DirRecursiveService service = new DirRecursiveService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("dirPath", "nowhere");
			input.putString("procName", "com.jc.service.proc.DirProcBase");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noDir", "nowhere"), e.getLocalizedMessage());
		}
	}

	private static final String RESOURCE_PATH = "src/test/resources/";

	@Test
	void test03() {

		// 該当プロシージャなし
		DirRecursiveService service = new DirRecursiveService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		try {
			input.putString("dirPath", RESOURCE_PATH + "service/fs/DirRecursiveServiceTest");
			input.putString("procName", "com.jc.service.proc.NotExistProc");
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("ClassNotFoundException"));
		}
	}

	@Test
	void test04() {

		// 正常系パターン
		DirRecursiveService service = new DirRecursiveService();
		GenericParam input = new GenericParam();
		GenericParam output = new GenericParam();
		input.putString("dirPath", RESOURCE_PATH + "service/fs/DirRecursiveServiceTest");
		input.putString("procName", "com.jc.service.proc.DirProcBase");
		service.doService(input, output);
		assertNull(output.getDb());
	}
}
