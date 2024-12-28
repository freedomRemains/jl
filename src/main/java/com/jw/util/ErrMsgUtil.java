package com.jw.util;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jc.db.DbInterface;
import com.jc.util.Cu;

public class ErrMsgUtil {

	public String getErrMsgKey(DbInterface db, String sessionId, String accountId,
			String mgnrgrpId) {

		try {
			// 汎用グループIDをキーとして、エラーメッセージを取得する
			var errMsgList = db.select("SELECT GNR_VAL FROM MGNRKEYVAL WHERE MGNRGRP_ID = " + mgnrgrpId);
			String errMsg = errMsgList.get(0).get("GNR_VAL");

			// エラーメッセージIDの ( 最大値 + 1 ) を取得する
			var maxIdRecord = db.select("SELECT MAX(TERRMSG_ID) FROM TERRMSG");
			String maxErrMsgIdStr = maxIdRecord.get(0).get("MAX(TERRMSG_ID)");
			if (Cu.isEmpty(maxErrMsgIdStr)) {
				maxErrMsgIdStr = "0";
			}
			int maxErrMsgId = Integer.parseInt(maxErrMsgIdStr) + 1;

			// 現在日付を文字列として取得する
			var dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDate = dateFormat.format(new Date());

			// エラーメッセージテーブルにレコードを追加する
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO TERRMSG(TERRMSG_ID, SESSION_ID, TACCOUNT_ID, ERR_MSG, VERSION, DEL_FLG, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE) VALUES(");
			sql.append(Integer.toString(maxErrMsgId) + ", '" + sessionId + "', " + accountId + ", '" + errMsg + "', 1, 0, '" + accountId + "', '" + currentDate + "', '" + accountId + "', '" + currentDate + "')");
			db.update(sql.toString());

			// エラーメッセージテーブルに登録したレコードのIDを呼び出し側に返却する
			return Integer.toString(maxErrMsgId);

		} catch (SQLException e) {

			// 固定値を返却する
			return "0";
		}
	}
}
