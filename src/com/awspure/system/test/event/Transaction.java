package com.awspure.system.test.event;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.loader.core.WorkFlowStepRTClassA;
import com.awspure.system.util.D;

public class Transaction extends WorkFlowStepRTClassA {

	public Transaction(UserContext uc) {
		super(uc);
		super.setVersion("1.0");
		super.setProvider("作者");
		super.setDescription("Transaction!");
	}
	
	@Override
	public boolean execute() {
		D.out("Transaction Invoke");
		return true;
	}

}
