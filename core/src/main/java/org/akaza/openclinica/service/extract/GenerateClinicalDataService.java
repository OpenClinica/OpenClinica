package org.akaza.openclinica.service.extract;

public interface GenerateClinicalDataService {

	
	public String getClinicalData(String studyOID,String studySubjectOID,String studyEventOID,String formVersionOID);
}
