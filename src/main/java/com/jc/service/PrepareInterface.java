package com.jc.service;

import com.jc.param.GenericParam;

public interface PrepareInterface {

	/**
	 * 入力パラメータと出力パラメータから、次に呼び出すサービスのパラメータを準備します。
	 * (入力パラメータもしくは出力パラメータの内容を追加／変更／削除する調整処理です)
	 *
	 * @param input 入力パラメータ
	 * @param output 出力パラメータ
	 */
	void doPrepare(GenericParam input, GenericParam output);
}
