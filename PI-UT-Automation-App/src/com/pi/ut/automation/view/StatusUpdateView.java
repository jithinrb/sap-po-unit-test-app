package com.pi.ut.automation.view;

import java.util.Observable;
import java.util.Observer;

import com.pi.ut.automation.model.UnitTestAuditLogModel;

public class StatusUpdateView implements Observer{

	@Override
	public void update(Observable o, Object oText) {
		if(o instanceof UnitTestAuditLogModel){
			System.out.println(oText);
		} 
	}

}
