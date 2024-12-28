package com.jw.service.web;

import java.util.ArrayList;

import com.jc.db.DbInterface;
import com.jc.exception.ApplicationInternalException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.util.Cu;
import com.jc.util.InputCheckUtil;
import com.jc.util.LogUtil;
import com.jw.exception.RoleRestrictionException;
import com.jw.util.AuthUtil;

public class GetAccountService implements ServiceInterface {

	/** デフォルトアカウントID(ゲストアカウント) */
	private static final String DEFAULT_TACCOUNT_ID = "1000001";

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "requestKind");
		inputCheckUtil.checkParam(input, "requestUri");

		try {
			// HTMLを生成する
			doGetAccount(input, output);

		} catch (RoleRestrictionException e) {

			// ロール制約例外を検出した場合は、ログを記録して例外をそのままスローする
			new LogUtil().handleException(e);
			throw e;

		} catch (Exception e) {
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}

	private void doGetAccount(GenericParam input, GenericParam output) throws Exception {

		// 入力パラメータにアカウントIDがない場合は、デフォルト値を適用する
		if (Cu.isEmpty(input.getString("accountId"))) {
			input.putString("accountId", DEFAULT_TACCOUNT_ID);
		}

		// アカウント情報を取得する
		getAccount(input.getDb(), input.getString("accountId"), output);

		// アカウントに紐づく権限を取得し、出力パラメータに設定する
		var authList = new AuthUtil().getAuthByAccountId(input.getDb(), input.getString("accountId"));
		output.putRecordList("authList", authList);

		// アクセスしようとしているURLにロールによる制約があるか確認する
		checkRequireRole(input.getDb(), input.getString("requestUri"), input.getString("accountId"),
				output);
	}

	private void getAccount(DbInterface db, String accountId, GenericParam output) throws Exception {

		// アカウント情報を取得する
		String sql = """
				SELECT
					A.TACCOUNT_ID, A.ACCOUNT_NAME, A.MAIL_ADDRESS,
					A.VERSION, A.DEL_FLG, A.CREATE_USER, A.CREATE_DATE,
					A.UPDATE_USER, A.UPDATE_DATE
				FROM TACCOUNT A
				WHERE A.TACCOUNT_ID = ?
				""";
		var paramList = new ArrayList<String>();
		paramList.add(accountId);
		var recordList = db.select(sql, paramList);

		// 出力パラメータにアカウント情報を設定する
		output.putRecordList("account", recordList);
	}

	private void checkRequireRole(DbInterface db, String requestUri, String accountId,
			GenericParam output) throws Exception {

		// ページに対するロール制約を取得する
		String sql = """
				SELECT
					B.MROLE_ID
				FROM MHTMLPAGE A
				LEFT JOIN MREQUIREROLE B ON A.MHTMLPAGE_ID = B.MHTMLPAGE_ID
				LEFT JOIN MURIPATTERN C ON A.MURIPATTERN_ID = C.MURIPATTERN_ID
				WHERE C.URI_PATTERN = ?
				GROUP BY B.MROLE_ID
				ORDER BY B.MROLE_ID
				""";
		var paramList = new ArrayList<String>();
		paramList.add(requestUri);
		var recordList = db.select(sql, paramList);

		// アカウントに紐づくロールを取得する
		var authUtil = new AuthUtil();
		var roleList = authUtil.getRoleByAccountId(db, accountId);

		// 取得した全てのロール制約のうち、いずれかのロールを持っている場合は即時終了する
		for (var columnMap : recordList) {

			// そもそもロール制約がない場合はロール制約違反なしと判断し、即時終了する
			if (Cu.isEmpty(columnMap.get("MROLE_ID"))) {
				return;
			}

			// アカウントがロールを持っていればロール制約違反なしと判断し、即時終了する
			if (authUtil.hasRole(columnMap.get("MROLE_ID"), roleList)) {
				return;
			}
		}

		// ロール制約内のいずれのロールも持っていない場合は、例外をスローする
		throw new RoleRestrictionException(recordList.toString());
	}
}
