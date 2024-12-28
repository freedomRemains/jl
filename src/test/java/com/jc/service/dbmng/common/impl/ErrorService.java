package com.jc.service.dbmng.common.impl;

import com.jc.param.GenericParam;
import com.jc.service.ServiceInterface;

public class ErrorService implements ServiceInterface {

	@Override
	public void doService(GenericParam input, GenericParam output) {
		throw new RuntimeException("notExistFile");
	}
}
