package org.akaza.openclinica.service.extract;

import java.util.LinkedHashMap;
import java.util.Locale;

import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;

public interface GenerateClinicalDataService {

	
	
	public LinkedHashMap<String, OdmClinicalDataBean> getClinicalData(String studyOID,String studySubjectOID,String studyEventOID,String formVersionOID,Boolean collectDNS,Boolean collectAudit, Locale locale, int userId);
}
