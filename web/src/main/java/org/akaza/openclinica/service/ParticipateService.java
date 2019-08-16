/**
 * 
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.service.randomize.ModuleProcessor;
import org.cdisc.ns.odm.v130.ODM;

import java.util.List;

/**
 * @author joekeremian
 *
 */
public interface ParticipateService extends ModuleProcessor {


	 boolean mayProceed(String studyOid) throws Exception ;

	 ODM getODM(String studyOID, String subjectKey,UserAccountBean ub);

	 ODM getOdmHeader(ODM odm , StudyBean currentStudy, StudySubjectBean subjectBean);

	 StudyBean getStudy(String oid);

	 StudyBean getStudyById(int id);

	 StudyBean getParentStudy(String oid);

	 void completeData(StudyEvent studyEvent, List<EventDefinitionCrf> eventDefCrfs, List<EventCrf> eventCrfs, String acessToken, String studyOid, String subjectOid) throws Exception;

	}
