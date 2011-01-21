package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.ws.bean.RegisterSubjectBean;
import org.akaza.openclinica.ws.cabig.abst.AbstractCabigDomEndpoint;
import org.akaza.openclinica.ws.cabig.exception.CCSystemFaultException;
import org.akaza.openclinica.ws.cabig.exception.CCBusinessFaultException;
import org.akaza.openclinica.ws.logic.RegisterSubjectService;
import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.sql.DataSource;

public class RollbackRegisterSubjectEndpoint extends AbstractCabigDomEndpoint {

    private final RegisterSubjectService subjectService;
    SubjectDAO subjectDao;
    StudyDAO studyDao;
    StudySubjectDAO studySubjectDao;
    // private final RegisterSubjectService subjectService;
    
    public RollbackRegisterSubjectEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {
        super(dataSource, messages, coreResources);
        this.subjectService = new RegisterSubjectService();
    }
    
    protected Element invokeInternal(
            Element requestElement,
            Document document) throws Exception {
        System.out.println("hit rollback");
        NodeList subjects = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "studySubject");
        try {
            if (subjects.getLength() > 0) {
                System.out.println("found study subject: " + subjects.getLength());
                logNodeList(subjects);
                for (int i=0; i < subjects.getLength(); i++) {
                    // will subjects always be sent one at a time? nta
                    Node childNode = subjects.item(i);
                    
                    // get user account bean from security here
                    UserAccountBean user = this.getUserAccount();
                    // is this user allowed to create subjects? if not, throw a ccsystem fault exception
                    Role r = user.getActiveStudyRole();
                    if (!this.canUserRegisterSubject(user)) {
                        throw new CCSystemFaultException("You do not possess the correct privileges to create a subject.");
                    }
                    RegisterSubjectBean subjectBean = subjectService.generateSubjectBean(user, childNode);
                    // performedSubjectMilestone
                    NodeList performedMilestones = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "performedSubjectMilestone");
                    if (performedMilestones.getLength() > 0) {
                        for (int j = 0; j < performedMilestones.getLength(); j++) {
                            Node milestone = performedMilestones.item(j);
                            subjectBean = subjectService.attachStudyIdentifiers(subjectBean, milestone);
                        }
                    }
                    // check to make sure the subject is already created
                    SubjectBean finalSubjectBean = subjectService.generateSubjectBean(subjectBean);
                    SubjectBean checkSubjectBean = getSubjectDao().findByUniqueIdentifier(finalSubjectBean.getUniqueIdentifier());
                    // does the subject already exist? if so, continue, otherwise, exit out
                    if (checkSubjectBean.getId() <= 0) {
                        throw new CCBusinessFaultException("This subject does not exist in the database.  " +
                                "Please check your data and re-submit");
                        
                    }
                }
            }
        } catch (Exception npe) {
            if (npe.getClass().getName().startsWith("org.akaza.openclinica.ws.cabig.exception")) {
                System.out.println("found " + npe.getClass().getName());
                OpenClinicaException ope = (OpenClinicaException) npe;
                return mapSubjectErrorConfirmation("", ope);
            } else {
                System.out.println(" did not find openclinica exception, found " + npe.getClass().getName());
                return mapSubjectErrorConfirmation(npe.getMessage());
            }
        }
        return this.mapRegisterSubjectConfirmation("test");
    }
     
    /**
     * the three dao getters, have not put this into the abstract class as each endpoint
     * will have different dao accessors.
     * @return
     */
    public SubjectDAO getSubjectDao() {
        subjectDao = subjectDao != null ? subjectDao : new SubjectDAO(dataSource);
        return subjectDao;
    }
    
    public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }
    
    public StudySubjectDAO getStudySubjectDao() {
        studySubjectDao = studySubjectDao != null ? studySubjectDao : new StudySubjectDAO(dataSource);
        return studySubjectDao;
    }
}
