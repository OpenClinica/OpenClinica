/**
 * 
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.controller.RuleController;
import org.akaza.openclinica.controller.dto.CommonEventContainerDTO;
import org.akaza.openclinica.controller.dto.ViewStudySubjectDTO;
import org.akaza.openclinica.controller.helper.RestfulServiceHelper;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp2.BasicDataSource;
import org.cdisc.ns.odm.v130.ODM;
import org.cdisc.ns.odm.v130.ODMcomplexTypeDefinitionFormData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author joekeremian
 *
 */
public interface ParticipateService {


	 RestfulServiceHelper getRestfulServiceHelper();

	 boolean mayProceed(String studyOid) throws Exception ;

	 ODM getODM(String studyOID, String subjectKey,UserAccountBean ub);

	 ODM getOdmHeader(ODM odm , StudyBean currentStudy, StudySubjectBean subjectBean);

	 StudyBean getStudy(String oid);
	 StudyBean getStudyById(int id);


	StudyBean getParentStudy(String oid);

	 void completeData(StudyEvent studyEvent, List<EventDefinitionCrf> eventDefCrfs, List<EventCrf> eventCrfs) throws Exception;

	}
