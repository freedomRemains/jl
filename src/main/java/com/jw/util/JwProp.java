package com.jw.util;

import com.jc.util.InnerClassPathProp;

/**
 * JavaWebプロパティ
 */
public class JwProp extends InnerClassPathProp {

	/**
	 * コンストラクタ
	 */
	public JwProp() {

		// プロパティファイル名を固定としてプロパティを取得する
		super("jw.properties");
	}
}
