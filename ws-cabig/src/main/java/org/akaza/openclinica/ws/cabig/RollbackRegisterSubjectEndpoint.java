package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
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
        SubjectBean finalSubjectBean = new SubjectBean();
        finalSubjectBean.setLabel("");
        try {
            if (subjects.getLength() > 0) {
                System.out.println("found study subject: " + subjects.getLength());
                // logNodeList(subjects);
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
                    finalSubjectBean = subjectService.generateSubjectBean(subjectBean);
                    SubjectBean checkSubjectBean = getSubjectDao().findByUniqueIdentifier(finalSubjectBean.getUniqueIdentifier());
                    // does the subject already exist? if so, continue, otherwise, exit out
                    if (checkSubjectBean.getId() <= 0) {
                        throw new CCBusinessFaultException("This subject does not exist in the database.  " +
                                "Please check your data and re-submit.");
                        
                    }
                    StudyBean studyBean = getStudyDao().findByUniqueIdentifier(subjectBean.getStudyUniqueIdentifier());
                    StudyBean siteBean = getStudyDao().findByUniqueIdentifier(subjectBean.getSiteUniqueIdentifier());
                    // should it be findSiteByUniqueIdentifier?
                    // dry
                    if (studyBean.getId() <= 0) {
                        // if no study exists with that name, there is an error
                        throw new CCBusinessFaultException("No study exists with that name, please review your information and re-submit the request.");
                    }
                    if (siteBean.getId() > 0) {
                        // if there is a site bean, the study bean should be its parent, otherwise there is an error
                        if ((siteBean.getParentStudyId() != studyBean.getId()) && (siteBean.getParentStudyId() != 0)) {
                            throw new CCBusinessFaultException("Your parent and child study relationship is mismatched." + 
                                    "  Please enter correct study and site information.");
                        }
                        studyBean = siteBean;
                    }
                    // dry
                    StudySubjectBean studySubjectBean = subjectService.generateStudySubjectBean(subjectBean, finalSubjectBean, studyBean);
                    StudySubjectBean checkStudySubjectBean = getStudySubjectDao().findByLabelAndStudy(studySubjectBean.getLabel(), studyBean);
                    if (checkStudySubjectBean.getId() <= 0) {
                        throw new CCBusinessFaultException("No relationship with this SSID currently exists.  " + 
                                "Please check your information and re-submit the form.");
                    }
                    // point of no return
                    checkStudySubjectBean.setStatus(Status.DELETED);
                    checkStudySubjectBean.setUpdater(this.getUserAccount());
                    // will need to delete crfs too, per discussed logic:
                    // Removing a study subject will set the status of all 
                    // of the subject's CRFs to Removed.
                    
                    // Creating a new study subject with the 
                    // same unique identifiers as a rolled-back 
                    // subject will cause OpenClinica to Restore and Update the subject's CRFs.
                    checkSubjectBean.setStatus(Status.DELETED);
                    checkSubjectBean.setUpdater(this.getUserAccount());
                    checkStudySubjectBean = (StudySubjectBean)getStudySubjectDao().update(checkStudySubjectBean);
                    checkSubjectBean = (SubjectBean)getSubjectDao().update(checkSubjectBean);
                    System.out.println("completed updates to deleted");
                    
                }
            }
            return this.mapRegisterSubjectConfirmation(finalSubjectBean.getLabel());
            // may have to switch it to primary key
        } catch (Exception npe) {
            if (npe.getClass().getName().startsWith("org.akaza.openclinica.ws.cabig.exception")) {
                npe.printStackTrace();
                System.out.println("found " + npe.getClass().getName());
                
                OpenClinicaException ope = (OpenClinicaException) npe;
                return mapSubjectErrorConfirmation("", ope);
            } else {
                npe.printStackTrace();
                System.out.println(" did not find openclinica exception, found " + npe.getClass().getName());
                return mapSubjectErrorConfirmation(npe.getMessage());
            }
        }
        
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
