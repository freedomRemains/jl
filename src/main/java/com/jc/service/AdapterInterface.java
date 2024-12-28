package com.jc.service;

import com.jc.param.GenericParam;

public interface AdapterInterface {

	/**
	 * 入力パラメータと出力パラメータのアダプターです。
	 *
	 * @param input 入力パラメータ
	 * @param output 出力パラメータ
	 */
	void doAdapt(GenericParam input, GenericParam output);
}
