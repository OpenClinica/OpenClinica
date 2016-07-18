package org.akaza.openclinica.service.extract;

import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;

import java.util.LinkedHashMap;
import java.util.Locale;

public interface GenerateClinicalDataService {

	
	
	public LinkedHashMap<String, OdmClinicalDataBean> getClinicalData(String studyOID,String studySubjectOID,String studyEventOID,String formVersionOID,Boolean collectDNS,Boolean collectAudit, Locale locale, int userId);
}
