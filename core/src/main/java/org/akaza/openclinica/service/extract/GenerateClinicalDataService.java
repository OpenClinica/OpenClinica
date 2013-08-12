package org.akaza.openclinica.service.extract;

import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.Study;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To generate CDISC-ODM clinical data without data set.
 * @author jnyayapathi
 *
 */

public class GenerateClinicalDataService {
	 protected final static Logger logger = LoggerFactory.getLogger("org.akaza.openclinica.service.extract.GenerateClinicalDataService");
	 
	 private StudyDao studyDao;
	 
	 public GenerateClinicalDataService(){
		 
	 }
	 public GenerateClinicalDataService(String StudyOID){
		 
		 
		 
		 
	 }
	 
	 public void getClinicalData(String studyOID){
		 Study study = new Study();
		 study.setOc_Oid(studyOID);
		 studyDao.findByOcOID(studyOID);
		 
	 }
	public StudyDao getStudyDao() {
		return studyDao;
	}
	public void setStudyDao(StudyDao studyDao) {
		this.studyDao = studyDao;
	}
}
