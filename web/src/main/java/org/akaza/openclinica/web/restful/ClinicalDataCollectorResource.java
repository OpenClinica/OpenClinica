package org.akaza.openclinica.web.restful;

import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.service.extract.GenerateClinicalDataService;

/**
 * 
 * @author jnyayapathi
 *
 */
public class ClinicalDataCollectorResource {
	private GenerateClinicalDataService generateClinicalDataService;
	
	public String generateClinicalData(String studyOID,String studySubjOID){
	
	return getGenerateClinicalDataService().getClinicalData(studyOID, studySubjOID);
		
		
	}

	public GenerateClinicalDataService getGenerateClinicalDataService() {
		return generateClinicalDataService;
	}

	public void setGenerateClinicalDataService(
			GenerateClinicalDataService generateClinicalDataService) {
		this.generateClinicalDataService = generateClinicalDataService;
	}
}
