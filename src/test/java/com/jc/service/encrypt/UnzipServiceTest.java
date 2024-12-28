package com.jc.service.encrypt;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jc.TestBase;
import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.util.FileUtil;
import com.jc.util.Mu;

/**
 * zipを解凍します。
 */
public class UnzipServiceTest extends TestBase {

	@BeforeEach
	void beforeEach() {

		// テストフォルダを作成する
		new FileUtil().createDirIfNotExists(RESOURCE_PATH + "service/encrypt/ZipServiceTest/dirToZip/subDir1");
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "ZipServiceTest");
		new FileUtil().createDirIfNotExists(OUTPUT_PATH + "ZipServiceTest/unzip");
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

		// 必須パラメータなしのパターン
		var input = new GenericParam();
		var output = new GenericParam();
		var service = new UnzipService();
		try {
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "targetZip"), e.getLocalizedMessage());
		}

		try {
			input.putString("targetZip", OUTPUT_PATH + "ZipServiceTest/" + "dirToZip");
			service.doService(input, output);
			fail();
		} catch (BusinessRuleViolationException e) {
			assertEquals(new Mu().msg("msg.common.noParam", "outputDir"), e.getLocalizedMessage());
		}
	}

	@Test
	void test02() {

		// zipファイルを作成する
		String outputDir = OUTPUT_PATH + "ZipServiceTest/";
		createZip(RESOURCE_PATH + "service/encrypt/ZipServiceTest/dirToZip", outputDir);

		// 正常系
		var input = getNormalInput("dirToZip.zip");
		var output = new GenericParam();
		var service = new UnzipService();
		service.doService(input, output);

		// 正常系のassertを行う
		assertNormal();
	}

	@Test
	void test03() {

		// カバレッジ(存在しない出力先を指定)
		var input = getNormalInput("dirToZip.zip");
		input.putString("outputDir", OUTPUT_PATH + "ZipServiceTest/nowhere");
		var output = new GenericParam();
		var service = new UnzipService();
		try {
			service.doService(input, output);
			fail();
		} catch (ApplicationInternalException e) {
			assertTrue(e.getLocalizedMessage().contains("FileNotFoundException"));
		}
	}

	private void createZip(String targetDirOrFile, String outputDir) {

		// zipファイルを解凍する
		var input = new GenericParam();
		input.putString("targetDirOrFile", targetDirOrFile);
		input.putString("outputDir", outputDir);
		var output = new GenericParam();
		var service = new ZipService();
		service.doService(input, output);

		// zipファイルが存在することを確認する
		assertNotBlankFile(outputDir + "dirToZip.zip");
	}

	private GenericParam getNormalInput(String targetZip) {

		// 正常系の入力パラメータを生成し、呼び出し側に返却する
		var input = new GenericParam();
		input.putString("targetZip", OUTPUT_PATH + "ZipServiceTest/" + targetZip);
		input.putString("outputDir", OUTPUT_PATH + "ZipServiceTest/unzip");
		return input;
	}

	private void assertNormal() {

		// zipファイルを解凍した結果の構成を確認する
		String outputDir = OUTPUT_PATH + "ZipServiceTest/unzip/";
		assertDir(outputDir + "dirToZip");
		assertDir(outputDir + "dirToZip/subDir1");
		assertDir(outputDir + "dirToZip/subDir2");
		assertDir(outputDir + "dirToZip/subDir3");
		assertDir(outputDir + "dirToZip/subDir3/subDir3_1");
		assertBlankFile(outputDir + "dirToZip/subBlankFile1.txt");
		assertBlankFile(outputDir + "dirToZip/subBlankFile2.txt");
		assertNotBlankFile(outputDir + "dirToZip/subFile1.txt");
		assertNotBlankFile(outputDir + "dirToZip/subFile2.txt");
		assertBlankFile(outputDir + "dirToZip/subDir2/subBlankFile2_1.txt");
		assertBlankFile(outputDir + "dirToZip/subDir2/subBlankFile2_2.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir2/subFile2_1.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir2/subFile2_2.txt");
		assertBlankFile(outputDir + "dirToZip/subDir3/subBlankFile3_1.txt");
		assertBlankFile(outputDir + "dirToZip/subDir3/subBlankFile3_2.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir3/subFile3_1.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir3/subFile3_2.txt");
		assertBlankFile(outputDir + "dirToZip/subDir3/subDir3_1/subBlankFile3_1_1.txt");
		assertBlankFile(outputDir + "dirToZip/subDir3/subDir3_1/subBlankFile3_1_2.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir3/subDir3_1/subFile3_1_1.txt");
		assertNotBlankFile(outputDir + "dirToZip/subDir3/subDir3_1/subFile3_1_2.txt");
	}

	private void assertBlankFile(String filePath) {
		File file = new File(filePath);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		assertTrue(file.length() == 0);
	}

	private void assertNotBlankFile(String filePath) {
		File file = new File(filePath);
		assertTrue(file.exists());
		assertTrue(file.isFile());
		assertTrue(file.length() > 0);
	}

	private void assertDir(String filePath) {
		File dir = new File(filePath);
		assertTrue(dir.exists());
		assertTrue(dir.isDirectory());
	}
}
