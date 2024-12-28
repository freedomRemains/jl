package com.jc.service.mail;

import java.lang.reflect.InvocationTargetException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;

import com.jc.exception.ApplicationInternalException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.service.proc.MailProcInterface;
import com.jc.util.InputCheckUtil;
import com.jc.util.LogUtil;

/**
 * メール取得サービスです。
 */
public class GetAllMailService implements ServiceInterface {

	/** ロガー */
	//private Logger logger = Logger.getLogger(this.getClass().getName());

	/** メールセッション */
	private Session mailSession = null;

	/** ストア */
	private Store store = null;

	/** メールボックスのINBOX */
	private Folder inbox = null;

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkDb(input);
		inputCheckUtil.checkParam(input, "mailStoreKind");
		inputCheckUtil.checkParam(input, "mailServer");
		inputCheckUtil.checkParam(input, "mailAccount");
		inputCheckUtil.checkParam(input, "mailPassword");
		inputCheckUtil.checkArrayParam(input, "mailProcs");

		// 全てのメールを取得する
		doGetAllMail(input, output);
	}

	private void doGetAllMail(GenericParam input, GenericParam output) {

		try {
			// メールボックスを開く
			openInbox(input);

			// 全てのメールを処理するまでループ
			int seqNum = 0;
			Message[] mails = inbox.getMessages();
			for (Message mail : mails) {

				// シーケンス番号をインクリメントし、文字列化して入力パラメータに設定する
				seqNum++;
				input.putString("seqNum", String.format("%05d", seqNum));

				// 入力パラメータ内の全てのメールプロシージャを処理するまでループ
				for (String mailProc : input.getStringArray("mailProcs")) {

					// プロシージャ名からプロシージャを取得できなかった場合は、エラーとする
					MailProcInterface proc = null;
					try {
						proc = (MailProcInterface) Class.forName(mailProc).getConstructor().newInstance();

					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
							| NoSuchMethodException | SecurityException | ClassNotFoundException e) {
						throw new ApplicationInternalException(new LogUtil().handleException(e));
					}

					// メールプロシージャを呼び出す
					proc.doMailProc(input, output, mail);
				}
			}

		} catch (Exception e) {

			// 例外をスローする 
			throw new ApplicationInternalException(new LogUtil().handleException(e));

		} finally {

			// メールボックスをクローズする
			try {
				closeInbox();
			} catch (Exception e) {
				throw new ApplicationInternalException(new LogUtil().handleException(e));
			}
		}
	}

	private void openInbox(GenericParam input) throws Exception {

		// メールプロパティに基づき、メールセッションを取得する
		mailSession = Session.getInstance(System.getProperties(), null);

		// 選択可能プロバイダをログに記録させる場合は、次のコードを有効にする
		//for (Provider provider : mailSession.getProviders()) {
		//	logger.info("【プロバイダ】" + provider.toString());
		//}

		// IMAPもしくはPOP3サーバのストアを取得する
		store = mailSession.getStore(input.getString("mailStoreKind"));

		// メールサーバ／メールユーザ名／メールパスワードを指定して、ストアに接続する
		store.connect(input.getString("mailServer"), input.getString("mailAccount"), input.getString("mailPassword"));

		// INBOXを開く
		inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_WRITE);
	}

	private void closeInbox() throws Exception {

		// INBOXを開いている場合はクローズする
		if (inbox != null) {
			inbox.close(true);
		}

		// ストアを開いている場合はクローズする
		if (store != null) {
			store.close();
		}
	}
}
