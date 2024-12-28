package com.jc.service.encrypt;

import java.nio.charset.Charset;
import java.util.Base64;

import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.util.InputCheckUtil;
import com.jc.util.JlProp;

public class Base64UrlEncodeService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "target");

		// Base64のURLエンコードを実行する
		doBase64UrlEncode(input, output);
	}

	private void doBase64UrlEncode(GenericParam input, GenericParam output) {

		// 入力パラメータにchaset指定がない場合は、デフォルト値を適用する
		String charset = input.getString("charset");
		if (charset == null) {
			charset = new JlProp().get("default.charset");
		}

		// Base64のURLエンコードを実行し、出力パラメータに結果を設定する
		byte[] urlEncodeResult = Base64.getUrlEncoder().encode(
				input.getString("target").getBytes(Charset.forName(charset)));
		output.putString("target", new String(urlEncodeResult, Charset.forName(charset)));
	}
}
