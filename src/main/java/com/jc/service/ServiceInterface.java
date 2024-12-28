package com.jc.service;

import com.jc.param.GenericParam;

public interface ServiceInterface {

	/** パス区切り文字 */
	public static final String PATH_DELM = "/";

	/**
	 * サービスを実行します。
	 *
	 * @param input 入力パラメータ
	 * @param output 出力パラメータ
	 */
	void doService(GenericParam input, GenericParam output);
}
