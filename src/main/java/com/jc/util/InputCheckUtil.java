package com.jc.util;

import com.jc.db.DbInterface;
import com.jc.exception.BusinessRuleViolationException;
import com.jc.param.GenericParam;

public class InputCheckUtil {

	public void checkDb(GenericParam input) {
		checkInput(input);
		DbInterface db = input.getDb();
		if (db == null) {
			throw new BusinessRuleViolationException(new Mu().msg("msg.common.noParam", "db"));
		}
	}

	public void checkParam(GenericParam input, String key) {
		checkInput(input);
		String value = input.getString(key);
		if (value == null || value.length() == 0) {
			throw new BusinessRuleViolationException(new Mu().msg("msg.common.noParam", key));
		}
	}

	public void checkArrayParam(GenericParam input, String key) {
		checkInput(input);
		String[] values = input.getStringArray(key);
		if (values == null || values.length == 0) {
			throw new BusinessRuleViolationException(new Mu().msg("msg.common.noParam", key));
		}
	}

	private void checkInput(GenericParam input) {
		if (input == null) {
			throw new BusinessRuleViolationException(new Mu().msg("msg.common.noParam", "input"));
		}
	}
}
