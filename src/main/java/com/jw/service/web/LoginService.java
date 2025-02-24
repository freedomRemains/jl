package com.jw.service.web;

import java.util.ArrayList;

import com.jc.exception.ApplicationInternalException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.util.InputCheckUtil;
import com.jc.util.Mu;
import com.jw.util.ErrMsgUtil;

public class LoginService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "MAIL_ADDRESS");
		inputCheckUtil.checkParam(input, "PASSWORD");

		try {
			// 認証を行う
			doAuthAccount(input, output);

		} catch (Exception e) {

			// エラーメッセージIDを取得する
			String errMsgKey = new ErrMsgUtil().getErrMsgKey(input.getDb(),
					input.getString("sessionId"), input.getString("accountId"),
					"1000401");

			// 認証失敗の場合は元の画面にエラーを表示させる
			String uri = "myPage.html?errMsgKey=" + errMsgKey;
			output.putString("respKind", "redirect");
			output.putString("destination", uri);
		}
	}

	private void doAuthAccount(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータからメールアドレスとパスワードを取得する
		String mailAddress = input.getString("MAIL_ADDRESS");
		String password = input.getString("PASSWORD");

		// ユーザが存在しない場合はエラーとする
		String sql = """
				SELECT
					A.TACCOUNT_ID, A.ACCOUNT_NAME, A.MAIL_ADDRESS, A.PASSWORD,
					A.VERSION, A.DEL_FLG, A.CREATE_USER, A.CREATE_DATE,
					A.UPDATE_USER, A.UPDATE_DATE
				FROM TACCOUNT A
				WHERE A.MAIL_ADDRESS = ?
				""";
		var paramList = new ArrayList<String>();
		paramList.add(mailAddress);
		var recordList = input.getDb().select(sql, paramList);
		if (recordList.size() == 0 || recordList.size() > 1) {
			throw new ApplicationInternalException(new Mu().msg("msg.err.userauth"));
		}

		// TODO ハッシュ化した値を比較する
		if (!recordList.get(0).get("PASSWORD").equals(password)) {
			throw new ApplicationInternalException(new Mu().msg("msg.err.userauth"));
		}

		// 入力パラメータに上書きでアカウントIDを設定する
		input.putString("accountId", recordList.get(0).get("TACCOUNT_ID"));
	}
}
