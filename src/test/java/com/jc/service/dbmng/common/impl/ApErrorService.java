package com.jc.service.dbmng.common.impl;

import com.jc.exception.ApplicationInternalException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;

public class ApErrorService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {
		throw new ApplicationInternalException("ApplicationErrorDetected");
	}
}
