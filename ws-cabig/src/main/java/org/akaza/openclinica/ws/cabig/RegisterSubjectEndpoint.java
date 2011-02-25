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

public class RegisterSubjectEndpoint extends AbstractCabigDomEndpoint {

    private final RegisterSubjectService subjectService;

    public RegisterSubjectEndpoint(DataSource dataSource, MessageSource messages, CoreResources coreResources) {

        super(dataSource, messages, coreResources);
        this.subjectService = new RegisterSubjectService();
    }

    protected Element invokeInternal(Element requestElement, Document document) throws Exception {
        // System.out.println("Request text ");
        SubjectBean finalSubjectBean = new SubjectBean();
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        finalSubjectBean.setUniqueIdentifier("");
        // the above line is for the return statement
        NodeList subjects = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "studySubject");
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
                    if (r != null
                        && (r.equals(Role.COORDINATOR) || r.equals(Role.STUDYDIRECTOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.RESEARCHASSISTANT) || r
                                .equals(Role.ADMIN))) {
                        // you may pass
                    } else {
                        throw new CCSystemFaultException("You do not possess the correct privileges to create a subject.");
                    }

                    RegisterSubjectBean subjectBean = subjectService.generateSubjectBean(user, childNode, getStudyDao(), getStudyParamValueDao());
                    // performedSubjectMilestone
                    NodeList performedMilestones = requestElement.getElementsByTagNameNS(CONNECTOR_NAMESPACE_V1, "performedSubjectMilestone");
                    if (performedMilestones.getLength() > 0) {
                        for (int j = 0; j < performedMilestones.getLength(); j++) {
                            Node milestone = performedMilestones.item(j);
                            subjectBean = subjectService.attachStudyIdentifiers(subjectBean, milestone);
                            // always auto-generated and non-editable, tbh
                            int nextLabel = getStudySubjectDao().findTheGreatestLabel() + 1;
                            subjectBean.setStudySubjectLabel(new Integer(nextLabel).toString());
                        }
                    }
                    // will there ever be more than one subject-study pair sent in a message? no

                    // are there errors here? if so, throw a ccbusiness fault
                    finalSubjectBean = subjectService.generateSubjectBean(subjectBean);

                    SubjectBean testSubjectBean = getSubjectDao().findByUniqueIdentifier(subjectBean.getUniqueIdentifier());

                    boolean updateMe = false;
                    if (testSubjectBean.getId() > 0) {
                        // if its identical, restore and renew
                        if (subjectService.isSubjectIdentical(subjectBean, testSubjectBean)) {
                            testSubjectBean.setUpdater(user);
                            if (finalSubjectBean.getDateOfBirth() != null) {
                                testSubjectBean.setDobCollected(true);// change?
                                testSubjectBean.setDateOfBirth(finalSubjectBean.getDateOfBirth());
                            }
                            testSubjectBean.setStatus(Status.AVAILABLE);
                            updateMe = true;
                            finalSubjectBean = testSubjectBean;
                            // reset the final subject bean with test subject bean
                            // set a boolean flag to update instead of creation
                        } else {
                            // otherwise, throw an error
                            throw new CCBusinessFaultException("You already have a subject in the database with the unique identifier of "
                                + subjectBean.getUniqueIdentifier() + ".  Please review your data and re-submit your request.");
                        }
                    }
                    studySubjectBean = subjectService.generateStudySubjectBean(subjectBean, finalSubjectBean, subjectBean.getStudyBean());

                    // we only really examine study subject bean for duplicates if the subject is a duplicate itself

                    StudySubjectBean testStudySubjectBean = // getStudySubjectDao().findTheGreatestLabel()
                        getStudySubjectDao().findByLabelAndStudy(subjectBean.getStudySubjectLabel(), subjectBean.getStudyBean());
                    if (updateMe) {
                        testStudySubjectBean = getStudySubjectDao().findBySubjectIdAndStudy(finalSubjectBean.getId(), subjectBean.getStudyBean());
                        subjectBean.setStudySubjectLabel(testStudySubjectBean.getLabel());
                        System.out.println("set ssid to " + testStudySubjectBean.getLabel());
                    }
                    boolean updateStudySubject = false;
                    if (testStudySubjectBean.getId() > 0) {
                        // same check here, if its identical, restore and renew, otherwise throw the error
                        if (subjectService.isStudySubjectIdentical(subjectBean, finalSubjectBean, testStudySubjectBean, subjectBean.getStudyBean())) {
                            testStudySubjectBean.setUpdater(user);
                            testStudySubjectBean.setStatus(Status.AVAILABLE);
                            updateStudySubject = true;
                            studySubjectBean = testStudySubjectBean;
                        } else {
                            // also renew all the CRFs and Items?

                            throw new CCBusinessFaultException("You already have a study subject in the database with the SSID of "
                                + subjectBean.getStudySubjectLabel() + ".  Please change it and try your request again.");
                        }
                    }
                    // ///////////////////////////////////////////////////////////////////////////////////////////////
                    // below is point of no return - we have caught all the errors and are committing to the database
                    // ///////////////////////////////////////////////////////////////////////////////////////////////
                    if (updateMe) {
                        finalSubjectBean = (SubjectBean) getSubjectDao().update(finalSubjectBean);
                        // if it is an update, update all events that are previously taken away too
                        finalSubjectBean =
                            subjectService.changeStatus(Status.AUTO_DELETED, Status.AVAILABLE, finalSubjectBean, user, getSubjectDao(), getStudySubjectDao(),
                                    getEventCrfDao(), getItemDataDao(), getStudyEventDao());
                    } else {
                        finalSubjectBean = getSubjectDao().create(finalSubjectBean);
                    }

                    // needs to be generated with an id, so we set it here, for creation purposes
                    studySubjectBean.setSubjectId(finalSubjectBean.getId());

                    if (updateStudySubject) {
                        studySubjectBean = (StudySubjectBean) getStudySubjectDao().update(studySubjectBean);
                        System.out.println("just updated ssid");
                    } else {
                        studySubjectBean = getStudySubjectDao().create(studySubjectBean, false);
                    }
                    System.out.println("finished creation");
                }
            }
            // return success message here
            // return mapRegisterSubjectConfirmation(finalSubjectBean.getUniqueIdentifier());
            return mapRegisterSubjectConfirmation(studySubjectBean.getLabel());
            // TODO is it actually primary key? nta
        } catch (Exception npe) {
            npe.printStackTrace();
            // TODO figure out exception and send response
            if (npe.getClass().getName().startsWith("org.akaza.openclinica.ws.cabig.exception")) {
                System.out.println("found " + npe.getClass().getName());
                OpenClinicaException ope = (OpenClinicaException) npe;
                return mapSubjectErrorConfirmation("", ope);
            } else {
                System.out.println(" did not find openclinica exception, found " + npe.getClass().getName());
                return mapSubjectErrorConfirmation(npe.getMessage());
            }
        }

    }

}
