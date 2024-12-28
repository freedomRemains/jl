package com.jc.service.proc;

import com.jc.param.GenericParam;

/**
 * ディレクトリを1つずつ処理するためのインターフェースです。
 */
public interface DirProcInterface {

	void doProc(GenericParam input, GenericParam output, String dirPath);
}
