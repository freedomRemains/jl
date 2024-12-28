package com.jc.service.proc;

import com.jc.param.GenericParam;

/**
 * テキストファイルを1行ずつ処理するためのインターフェースです。
 */
public interface TextFileProcInterface {

	void doProc(GenericParam input, GenericParam output, String filePath);
}
