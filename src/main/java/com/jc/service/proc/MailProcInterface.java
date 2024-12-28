package com.jc.service.proc;

import javax.mail.Message;

import com.jc.param.GenericParam;

/**
 * メールプロシージャのインターフェースです。
 */
public interface MailProcInterface {

	void doMailProc(GenericParam input, GenericParam output, Message mail) throws Exception;
}
