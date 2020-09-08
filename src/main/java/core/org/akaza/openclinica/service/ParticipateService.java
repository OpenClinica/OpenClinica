/**
 * 
 */
package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.service.randomize.ModuleProcessor;
import org.cdisc.ns.odm.v130.ODM;

import java.util.List;

/**
 * @author joekeremian
 *
 */
public interface ParticipateService extends ModuleProcessor {


	 boolean mayProceed(String studyOid) throws Exception ;

	 ODM getODM(String studyOID, String subjectKey,UserAccountBean ub);

	 ODM getOdmHeader(ODM odm , Study currentStudy, StudySubjectBean subjectBean);

	 Study getStudy(String oid);

	 Study getStudyById(int id);

	 Study getParentStudy(String oid);

	 void completeData(StudyEvent studyEvent, List<EventDefinitionCrf> eventDefCrfs, List<EventCrf> eventCrfs, String acessToken, String studyOid, String subjectOid, UserAccountBean ub) throws Exception;

	}
