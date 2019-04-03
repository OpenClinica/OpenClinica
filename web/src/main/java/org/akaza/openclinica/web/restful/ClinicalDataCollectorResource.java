package org.akaza.openclinica.web.restful;

import java.util.LinkedHashMap;
import java.util.Locale;

import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.service.dto.ODMFilterDTO;
import org.akaza.openclinica.service.extract.GenerateClinicalDataService;

/**
 * 
 * @author jnyayapathi
 *
 */
public class ClinicalDataCollectorResource {
	private GenerateClinicalDataService generateClinicalDataService;
	
	public LinkedHashMap<String,OdmClinicalDataBean> generateClinicalData(String studyOID, String studySubjOID, String studyEventOID, String formVersionOID, Locale locale, int userId, ODMFilterDTO odmFilter){
	
	return getGenerateClinicalDataService().getClinicalData(studyOID, studySubjOID,studyEventOID,formVersionOID, locale, userId, odmFilter);
		
		
	}

	public GenerateClinicalDataService getGenerateClinicalDataService() { return generateClinicalDataService;
	}

	public void setGenerateClinicalDataService(
			GenerateClinicalDataService generateClinicalDataService) {
		this.generateClinicalDataService = generateClinicalDataService;
	}
}
