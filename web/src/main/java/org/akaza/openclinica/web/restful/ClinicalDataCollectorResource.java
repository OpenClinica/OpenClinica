package org.akaza.openclinica.web.restful;

import java.util.Locale;

import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.service.extract.GenerateClinicalDataService;

/**
 * 
 * @author jnyayapathi
 *
 */
public class ClinicalDataCollectorResource {
	private GenerateClinicalDataService generateClinicalDataService;
	
	public OdmClinicalDataBean generateClinicalData(String studyOID,String studySubjOID,String studyEventOID,String formVersionOID,boolean includeDNs,boolean includeAudits,Locale locale){
	
	return getGenerateClinicalDataService().getClinicalData(studyOID, studySubjOID,studyEventOID,formVersionOID,includeDNs,includeAudits,locale);
		
		
	}

	public GenerateClinicalDataService getGenerateClinicalDataService() {
		return generateClinicalDataService;
	}

	public void setGenerateClinicalDataService(
			GenerateClinicalDataService generateClinicalDataService) {
		this.generateClinicalDataService = generateClinicalDataService;
	}
}
