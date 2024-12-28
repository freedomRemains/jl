package com.jc.service.fs;

import java.lang.reflect.InvocationTargetException;

import com.jc.exception.ApplicationInternalException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.service.proc.DirProcInterface;
import com.jc.util.InputCheckUtil;
import com.jc.util.LogUtil;

/**
 * ディレクトリを再帰的に処理していくサービスです。<br>
 * このサービスはディレクトリごとにinput、outputの内容を取り回して処理するため、<br>
 * 呼び出し側でinputとoutputを上位から引き継がずに呼び出すと、混乱やバグを防ぎやすいです。<br>
 * <br>
 * [input][必須][dirPath]ディレクトリパス<br>
 * [input][必須][procName]プロシージャ名(ディレクトリごとの処理を記述するクラスの名前)<br>
 * [output]inputのprocNameで指定したクラスにより、出力内容が変わる。<br>
 */
public class DirRecursiveService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "dirPath");
		inputCheckUtil.checkParam(input, "procName");

		// プロシージャ名からプロシージャを取得できなかった場合は、エラーとする
		DirProcInterface proc = null;
		try {
			proc = (DirProcInterface) Class.forName(input.getString("procName")).getConstructor().newInstance();

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}

		// ファイルプロシージャを呼び出す
		proc.doProc(input, output, input.getString("dirPath"));
	}
}
