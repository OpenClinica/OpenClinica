package org.akaza.openclinica.control.submit;

import java.util.ArrayList;

public class ImportCRFInfoSummary {

	private ArrayList<String> detailMessages;
	private int passCnt;
	private int failCnt;
	private int totalCnt;
	
	public ArrayList<String> getDetailMessages() {
		if(detailMessages == null) {
			detailMessages =  new ArrayList<String>(); 
		}
		return detailMessages;
	}
	public void setDetailMessages(ArrayList<String> detailMessages) {
		this.detailMessages = detailMessages;
	}
	public int getPassCnt() {
		return passCnt;
	}
	public void setPassCnt(int passCnt) {
		this.passCnt = passCnt;
	}
	public int getFailCnt() {
		return failCnt;
	}
	public void setFailCnt(int failCnt) {
		this.failCnt = failCnt;
	}

	public String getSummaryMsg() {
		return "Successfully Imported:" 
	            + this.getPassCnt() 
	            + "\n Successfully Skipped:" 
	            + "\n Failed records:" + this.getFailCnt();

				 


	}
	public int getTotalCnt() {
		return totalCnt;
	}
	public void setTotalCnt(int totalCnt) {
		this.totalCnt = totalCnt;
	}
}
