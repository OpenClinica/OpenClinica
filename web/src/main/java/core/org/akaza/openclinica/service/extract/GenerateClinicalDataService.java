package core.org.akaza.openclinica.service.extract;

import java.util.LinkedHashMap;
import java.util.Locale;

import core.org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import core.org.akaza.openclinica.service.dto.ODMFilterDTO;

public interface GenerateClinicalDataService {

	
	
	public LinkedHashMap<String, OdmClinicalDataBean> getClinicalData(String studyOID, String studySubjectOID, String studyEventOID, String formVersionOID, Locale locale, int userId, ODMFilterDTO odmFilter);
}
