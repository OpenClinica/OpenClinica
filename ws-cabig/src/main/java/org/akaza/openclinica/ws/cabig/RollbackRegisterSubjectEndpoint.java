/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2010-2011 Akaza Research

 * Development of this web service or portions thereof has been funded
 * by Federal Funds from the National Cancer Institute, 
 * National Institutes of Health, under Contract No. HHSN261200800001E.
 * In addition to the GNU LGPL license, this code is also available
 * from NCI CBIIT repositories under the terms of the caBIG Software License. 
 * For details see: https://cabig.nci.nih.gov/adopt/caBIGModelLicense
 */
package org.akaza.openclinica.ws.cabig;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.ws.bean.RegisterSubjectBean;
import org.akaza.openclinica.ws.cabig.abst.AbstractCabigDomEndpoint;
import org.akaza.openclinica.ws.cabig.exception.CCBusinessFaultException;
import org.akaza.openclinica.ws.cabig.exception.CCSystemFaultException;
import org.akaza.openclinica.ws.logic.RegisterSubjectService;
import org.springframework.context.MessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.sql.DataSource;

public class RollbackRegisterSubjectEndpoint extends AbstractCabigDomEndpoint {

    private final RegisterSubjectService subjectService;

    // private final RegisterSubjectService subjectService;

    public RollbackRegisterSubjectEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {
        super(dataSource, messages, coreResources);
        this.subjectService = new RegisterSubjectService();
    }

    protected Element invokeInternal(Element requestElement, Document document) throws Exception {
        System.out.println("hit rollback");
        NodeList subjects = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "studySubject");
        SubjectBean finalSubjectBean = new SubjectBean();
        finalSubjectBean.setLabel("");
        try {
            if (subjects.getLength() > 0) {
                System.out.println("found study subject: " + subjects.getLength());
                // logNodeList(subjects);
                for (int i = 0; i < subjects.getLength(); i++) {
                    // will subjects always be sent one at a time? nta
                    Node childNode = subjects.item(i);

                    // get user account bean from security here
                    UserAccountBean user = this.getUserAccount();
                    // is this user allowed to create subjects? if not, throw a ccsystem fault exception
                    Role r = user.getActiveStudyRole();
                    if (!this.canUserRegisterSubject(user)) {
                        throw new CCSystemFaultException("You do not possess the correct privileges to create a subject.");
                    }
                    RegisterSubjectBean subjectBean = subjectService.generateSubjectBean(user, childNode, getStudyDao());
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
                        throw new CCBusinessFaultException("This subject does not exist in the database.  " + "Please check your data and re-submit.");

                    }

                    StudySubjectBean studySubjectBean = subjectService.generateStudySubjectBean(subjectBean, finalSubjectBean, subjectBean.getStudyBean());
                    StudySubjectBean checkStudySubjectBean = getStudySubjectDao().findByLabelAndStudy(studySubjectBean.getLabel(), subjectBean.getStudyBean());
                    if (checkStudySubjectBean.getId() <= 0) {
                        throw new CCBusinessFaultException("No relationship with this SSID currently exists.  "
                            + "Please check your information and re-submit the form.");
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
                    checkStudySubjectBean = (StudySubjectBean) getStudySubjectDao().update(checkStudySubjectBean);
                    checkSubjectBean = (SubjectBean) getSubjectDao().update(checkSubjectBean);
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

}
