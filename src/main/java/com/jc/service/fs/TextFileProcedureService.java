package com.jc.service.fs;

import java.lang.reflect.InvocationTargetException;

import com.jc.exception.ApplicationInternalException;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.service.proc.TextFileProcInterface;
import com.jc.util.Cu;
import com.jc.util.InputCheckUtil;
import com.jc.util.LogUtil;
import com.jc.util.Mu;

/**
 * テキストファイルを1行ずつ処理していくサービスです。<br>
 * このサービスはテキストファイル1行ごとにinput、outputの内容を取り回して処理するため、<br>
 * 呼び出し側でinputとoutputを上位から引き継がずに呼び出すと、混乱やバグを防ぎやすいです。<br>
 * <br>
 * [input][必須][filePath]ファイルパス<br>
 * [input][必須][dirPath]ディレクトリパス<br>
 * [input][必須][fileName]ファイル名<br>
 * [input][必須][procName]プロシージャ名(テキストファイルごとの処理を記述するクラスの名前)<br>
 * [output]inputのprocNameで指定したクラスにより、出力内容が変わる。<br>
 * <br>
 * "filePath"単独で指定した場合は"dirPath"と"fileName"の組み合わせは無視される。<br>
 * "filePath"が空で"dirPath"と"fileName"の両方がある場合は、組み合わせて"filePath"と扱う。<br>
 * "procName"はテキストファイル読み込み処理クラスの完全修飾名を指定する。<br>
 */
public class TextFileProcedureService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "procName");

		// 必要なパラメータが入力されていなければエラーとする
		String filePath = input.getString("filePath");
		String dirPath = input.getString("dirPath");
		String fileName = input.getString("fileName");

		// filePath、dirPath、fileNameの全てのパラメータがない場合はエラーとする
		if (Cu.isEmpty(filePath) && Cu.isEmpty(dirPath) && Cu.isEmpty(fileName)) {
			throw new BusinessRuleViolationException(
					new Mu().msg("msg.common.noParam", "filePath or (dirPath, fileName)"));
		}

		// dirPath指定ありでfileName指定なしはエラーとする
		if (Cu.isNotEmpty(dirPath) && Cu.isEmpty(fileName)) {
			throw new BusinessRuleViolationException(
					new Mu().msg("msg.common.noParam", "filePath or (dirPath, fileName)"));
		}

		// filePath指定なしの場合はdirPath + "/" + fileNameをfilePathとする
		if (Cu.isEmpty(filePath)) {
			filePath = dirPath + "/" + fileName;
		}

		// プロシージャ名からプロシージャを取得できなかった場合は、エラーとする
		TextFileProcInterface proc = null;
		try {
			proc = (TextFileProcInterface) Class.forName(input.getString("procName")).getConstructor().newInstance();

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}

		// テキストファイルプロシージャを呼び出す
		proc.doProc(input, output, filePath);
	}
}
