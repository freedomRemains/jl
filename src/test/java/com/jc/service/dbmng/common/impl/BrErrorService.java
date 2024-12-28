package com.jc.service.dbmng.common.impl;

import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;

public class BrErrorService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {
		throw new BusinessRuleViolationException("BusinessRuleViolationErrorDetected");
	}
}
