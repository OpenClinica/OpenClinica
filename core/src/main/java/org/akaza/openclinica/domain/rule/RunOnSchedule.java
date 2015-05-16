package org.akaza.openclinica.domain.rule;

import java.security.Timestamp;

public class RunOnSchedule {

	private String runTime;

	
	public RunOnSchedule() {
		super();
	}


	public RunOnSchedule(String runTime){
		this.runTime=runTime;
		
	}


	public String getRunTime() {
		return runTime;
	}


	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}


	
	

	
	
	
	
}
