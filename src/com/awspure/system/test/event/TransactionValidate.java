package com.awspure.system.test.event;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.awspure.system.util.D;

public class TransactionValidate extends WorkFlowStepRTClassA {

	public TransactionValidate(UserContext uc) {
		super(uc);
		super.setVersion("1.0");
		super.setProvider("作者");
		super.setDescription("TransactionValidate!");
	}
	
	@Override
	public boolean execute() {
		D.out("TransactionValidate Invoke");
		return true;
	}

}
