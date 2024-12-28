package com.jc.service.mail;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.jc.exception.ApplicationInternalException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;
import com.jc.util.Cu;
import com.jc.util.InputCheckUtil;
import com.jc.util.JlProp;
import com.jc.util.LogUtil;

public class SendMailService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {

		// 必要なパラメータが入力されていなければエラーとする
		InputCheckUtil inputCheckUtil = new InputCheckUtil();
		inputCheckUtil.checkParam(input, "username");
		inputCheckUtil.checkParam(input, "password");
		inputCheckUtil.checkParam(input, "subject");
		inputCheckUtil.checkParam(input, "honbun");
		inputCheckUtil.checkParam(input, "to");

		// メールを送信する
		sendMail(input);
	}

	public void sendMail(GenericParam input) {

		try {
			// メールホストとポートはinputにあるものを優先する
			JlProp jlProp = new JlProp();
			String host = jlProp.get("mail.smtp.host");
			if (Cu.isNotEmpty(input.getString("mail.smtp.host"))) {
				host = input.getString("mail.smtp.host");
			}
			String port = jlProp.get("mail.smtp.port");
			if (Cu.isNotEmpty(input.getString("mail.smtp.port"))) {
				port = input.getString("mail.smtp.port");
			}

			// メール送信に必要なプロパティを設定する
			Properties props = new Properties();
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);
			props.put("mail.smtp.auth", jlProp.get("mail.smtp.auth"));
			props.put("mail.smtp.starttls.enable", jlProp.get("mail.smtp.starttls.enable"));
			props.put("mail.smtp.connectiontimeout", jlProp.get("mail.smtp.connectiontimeout"));
			props.put("mail.smtp.timeout", jlProp.get("mail.smtp.timeout"));
			props.put("mail.debug", jlProp.get("mail.debug"));

			// メールセッションを開始する
			String username = input.getString("username");
			String password = input.getString("password");
			Session mailSession = Session.getInstance(props,
					new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			// メールを送信する
			String from = jlProp.get("mail.from");
			MimeMessage message = new MimeMessage(mailSession);
			message.setFrom(new InternetAddress(from, "aliasName"));
			message.setReplyTo(new Address[] {new InternetAddress(from)});
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(input.getString("to")));
			message.setSubject(input.getString("subject"), jlProp.get("default.charset"));
			message.setText(input.getString("honbun"), jlProp.get("default.charset"));
			Transport.send(message);

		} catch (UnsupportedEncodingException | MessagingException e) {

			// 例外をスローする 
			throw new ApplicationInternalException(new LogUtil().handleException(e));
		}
	}
}
