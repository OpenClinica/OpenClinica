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
package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.ws.bean.RegisterSubjectBean;
import org.akaza.openclinica.ws.cabig.exception.CCBusinessFaultException;
import org.akaza.openclinica.ws.cabig.exception.CCDataValidationFaultException;
import org.w3c.dom.Node;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class RegisterSubjectService {

    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";

    public RegisterSubjectService() {

    }

    public RegisterSubjectBean generateSubjectBean(UserAccountBean user, Node subject, StudyDAO studyDao, StudyParameterValueDAO studyParamDao)
            throws Exception {
        RegisterSubjectBean subjectBean = new RegisterSubjectBean(user);
        DomParsingService xmlService = new DomParsingService();
        String studyIdentifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "studyIdentifier", "extension");
        String studySiteIdentifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "studySiteIdentifier", "extension");
        subjectBean.setSiteUniqueIdentifier(studySiteIdentifier);
        subjectBean.setStudyUniqueIdentifier(studyIdentifier);
        // lookup study here, get parameters to affect birthday
        StudyBean studyBean = new StudyBean();
        StudyBean siteBean = new StudyBean();

        if (subjectBean.getSiteUniqueIdentifier() != null) {
            siteBean = studyDao.findByUniqueIdentifier(subjectBean.getSiteUniqueIdentifier());
        }
        studyBean = studyDao.findByUniqueIdentifier(subjectBean.getStudyUniqueIdentifier());

        // dry
        // throws a NPE when there is no study
        try {
            if (studyBean.getId() <= 0) {
                // if no study exists with that name, there is an error
                throw new CCBusinessFaultException("No study exists with that name, " + "please review your information and re-submit the request.", "CC10110");
            }
            if (siteBean.getId() > 0) {
                // if there is a site bean, the study bean should be its parent, otherwise there is an error
                if ((siteBean.getParentStudyId() != studyBean.getId()) && (siteBean.getParentStudyId() != 0)) {
                    throw new CCBusinessFaultException("Your parent and child study relationship is mismatched."
                        + "  Please enter correct study and site information.", "CC10110");
                }
                studyBean = siteBean;
            }
        } catch (NullPointerException npe) {
            // 7464/7463, need to provide meaningful information if they are null
            throw new CCBusinessFaultException("No study or site exists with that name, " + "please review your information and re-submit the request",
                    "CC10110");
        }
        // bugs 7441, 7440, 7438 - need to check to see if the study requires this data before checking the xml node
        // ArrayList<StudyParameterValueBean> valueBeans = studyParamDao.findAllParameterValuesByStudy(studyBean);

        StudyParameterValueBean subjectDOBRequired = studyParamDao.findByHandleAndStudy(studyBean.getId(), "collectDOB");
        System.out.println("found dob: " + subjectDOBRequired.getValue());
        if ("1".equals(subjectDOBRequired.getValue()) || ("".equals(subjectDOBRequired.getValue()))) {
            // dry
            System.out.println("got to 1");
            String subjectDOB = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "birthDate", "value");
            // no dashes in dates?
            if (subjectDOB.contains("-")) {
                throw new CCDataValidationFaultException("Problem parsing subject date of birth. " + "Please remove all dashes from " + subjectDOB
                    + " and re-submit your data.", "CC10210");
            }
            SimpleDateFormat local_df = new SimpleDateFormat("yyyyMMdd");
            // figure out the study params here; date only? no date?
            try {
                Date dateOfBirth = local_df.parse(subjectDOB);
                subjectBean.setDateOfBirth(dateOfBirth);
            } catch (ParseException pe) {
                // throw the data fault exception
                throw new CCDataValidationFaultException("Problem parsing subject date of birth, it should be in YYYYMMDD format and not " + subjectDOB + ".",
                        "CC10210");
            }
        } else if ("2".equals(subjectDOBRequired.getValue())) {
            // dry
            System.out.println("got to 2");
            String subjectDOB = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "birthDate", "value");
            // no dashes in dates?
            if (subjectDOB.contains("-")) {
                throw new CCDataValidationFaultException("Problem parsing subject date of birth. " + "Please remove all dashes from " + subjectDOB
                    + " and re-submit your data.", "CC10210");
            }
            SimpleDateFormat local_df = new SimpleDateFormat("yyyy");
            // figure out the study params here; date only? no date?
            try {
                Date dateOfBirth = local_df.parse(subjectDOB);
                subjectBean.setDateOfBirth(dateOfBirth);
            } catch (ParseException pe) {
                // throw the data fault exception
                throw new CCDataValidationFaultException("Problem parsing subject date of birth, it should be in YYYY format and not " + subjectDOB + ".",
                        "CC10210");
            }
        } // else, no date is set

        StudyParameterValueBean genderRequired = studyParamDao.findByHandleAndStudy(studyBean.getId(), "genderRequired");
        if ("true".equals(genderRequired.getValue())) {
            String subjectGender = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "sexCode", "code");
            if (!"male".equals(subjectGender.toLowerCase()) && !"female".equals(subjectGender.toLowerCase())) {
                throw new CCDataValidationFaultException("Problem parsing sex, it should be either 'Male' or 'Female'.", "CC10210");
            }
            if ("Male".equals(subjectGender) || "male".equals(subjectGender)) {
                subjectBean.setGender("m");
            } else {
                subjectBean.setGender("f");
            }

        }
        // subjectPersonIdRequired = required, or nothing
        String identifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "identifier", "extension");

        subjectBean.setStudyBean(studyBean);
        subjectBean.setUniqueIdentifier(identifier);
        // throw an error if we dont get male or female as an answer

        return subjectBean;
    }

    public RegisterSubjectBean attachStudyIdentifiers(RegisterSubjectBean rsbean, Node milestone) throws CCDataValidationFaultException {
        DomParsingService xmlService = new DomParsingService();
        // <ns2:informedConsentDate value="20080101"/>
        // <ns2:registrationDate xsi:type="ns1:TS" value="20080825"/>
        // <ns2:registrationSiteIdentifier extension
        // String consentDateStr = xmlService.getElementValue(milestone,
        // CONNECTOR_NAMESPACE_V1, "informedConsentDate", "value");
        String registrationDateStr = xmlService.getElementValue(milestone, CONNECTOR_NAMESPACE_V1, "registrationDate", "value");
        String registrationSiteIdentifier = xmlService.getElementValue(milestone, CONNECTOR_NAMESPACE_V1, "registrationSiteIdentifier", "extension");

        SimpleDateFormat local_df = new SimpleDateFormat("yyyyMMdd");
        try {
            Date enrollmentDate = local_df.parse(registrationDateStr);
            rsbean.setEnrollmentDate(enrollmentDate);
        } catch (ParseException pe) {
            // throw the data fault exception
            throw new CCDataValidationFaultException("Problem parsing date, it should be in YYYYMMDD format.", "CC10200");
        }
        // rsbean.setStudySubjectLabel(registrationSiteIdentifier);

        return rsbean;
    }

    public SubjectBean generateSubjectBean(RegisterSubjectBean rsbean) {
        SubjectBean sbean = new SubjectBean();
        sbean.setStatus(Status.AVAILABLE);
        if (rsbean.getDateOfBirth() != null) {
            sbean.setDateOfBirth(rsbean.getDateOfBirth());
            sbean.setDobCollected(true);
        } else {
            sbean.setDobCollected(false);
        }
        sbean.setCreatedDate(new Date(System.currentTimeMillis()));
        if (rsbean.getGender() != null) {
            char gender = rsbean.getGender().charAt(0);
            sbean.setGender(gender);
        }
        sbean.setLabel(rsbean.getUniqueIdentifier());
        sbean.setName(rsbean.getUniqueIdentifier());
        sbean.setOwner(rsbean.getUser());
        sbean.setStudyIdentifier(rsbean.getStudyUniqueIdentifier());
        sbean.setUniqueIdentifier(rsbean.getUniqueIdentifier());
        return sbean;
    }

    public StudySubjectBean generateStudySubjectBean(RegisterSubjectBean subjectBean, SubjectBean finalSubjectBean, StudyBean studyBean) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        studySubjectBean.setEnrollmentDate(subjectBean.getEnrollmentDate());
        studySubjectBean.setStatus(Status.AVAILABLE);
        studySubjectBean.setLabel(subjectBean.getStudySubjectLabel());
        studySubjectBean.setSubjectId(finalSubjectBean.getId());
        studySubjectBean.setStudyId(studyBean.getId());
        // studySubjectBean.setSecondaryLabel(subjectBean.getStudySubjectLabel());
        studySubjectBean.setSecondaryLabel("");
        studySubjectBean.setOwner(subjectBean.getUser());
        return studySubjectBean;
    }

    public boolean isSubjectIdentical(RegisterSubjectBean registerBean, SubjectBean subjectBean) {
        // char gender = registerBean.getGender().charAt(0);
        // if (subjectBean.getGender() != gender) {
        // System.out.println("gender fail");
        // return false;
        // }
        // is below necessary?
        // if (!subjectBean.getLabel().equals(registerBean.getUniqueIdentifier())) {
        // System.out.println("label fail");
        // return false;
        // }
        if (!subjectBean.getUniqueIdentifier().equals(registerBean.getUniqueIdentifier())) {
            System.out.println("ident fail");
            return false;
        }
        // if (!subjectBean.getDateOfBirth().equals(registerBean.getDateOfBirth())) {
        // System.out.println("dob fail");
        // return false;
        // }
        return true;
    }

    /**
     * isStudySubjectIdentical, determines if there is more than one ssid in the database
     * 
     * @param subjectBean
     * @param finalSubjectBean
     * @param studySubjectBean
     * @param studyBean
     * @return
     */
    public boolean isStudySubjectIdentical(RegisterSubjectBean subjectBean, SubjectBean finalSubjectBean, StudySubjectBean studySubjectBean, StudyBean studyBean) {
        // if (!studySubjectBean.getEnrollmentDate().equals(subjectBean.getEnrollmentDate())) {
        // System.out.println("enroll fail");
        // return false;
        // }
        if (studySubjectBean.getSubjectId() != finalSubjectBean.getId()) {
            System.out.println("subj id fail");
            return false;
        }
        if (!studySubjectBean.getLabel().equals(subjectBean.getStudySubjectLabel())) {
            System.out.println("ss label fail");
            return false;
        }
        if (studySubjectBean.getStudyId() != studyBean.getId()) {
            System.out.println("study fail");
            return false;
        }
        return true;
    }

    /**
     * changeStatus, which will allow us to change from AVAILABLE to AUTO_DELETED and vice versa.
     * 
     * @param oldStatus
     * @param newStatus
     * @param subjectBean
     * @param user
     *            , the updater
     * @param subjectDao
     * @param studySubjectDao
     * @param eventCrfDao
     * @param itemDataDao
     * @param studyEventDao
     * @return
     */
    public SubjectBean changeStatus(Status oldStatus, Status newStatus, SubjectBean subjectBean, UserAccountBean user, SubjectDAO subjectDao,
            StudySubjectDAO studySubjectDao, EventCRFDAO eventCrfDao, ItemDataDAO itemDataDao, StudyEventDAO studyEventDao) {
        ArrayList<StudySubjectBean> studySubs = studySubjectDao.findAllBySubjectId(subjectBean.getId());
        for (int i = 0; i < studySubs.size(); i++) {
            StudySubjectBean studySub = (StudySubjectBean) studySubs.get(i);
            if (studySub.getStatus().equals(oldStatus)) {
                studySub.setStatus(newStatus);
                studySub.setUpdater(user);
                studySub.setUpdatedDate(new Date());
                studySubjectDao.update(studySub);
            }
        }
        ArrayList<StudyEventBean> events = studyEventDao.findAllBySubjectId(subjectBean.getId());
        // EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());

        for (int j = 0; j < events.size(); j++) {
            StudyEventBean event = (StudyEventBean) events.get(j);
            if (event.getStatus().equals(oldStatus)) {
                event.setStatus(newStatus);
                event.setUpdater(user);
                event.setUpdatedDate(new Date());
                studyEventDao.update(event);

                ArrayList eventCRFs = eventCrfDao.findAllByStudyEvent(event);

                // ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
                for (int k = 0; k < eventCRFs.size(); k++) {
                    EventCRFBean eventCRF = (EventCRFBean) eventCRFs.get(k);
                    if (eventCRF.getStatus().equals(oldStatus)) {
                        eventCRF.setStatus(newStatus);
                        eventCRF.setUpdater(user);
                        eventCRF.setUpdatedDate(new Date());
                        eventCrfDao.update(eventCRF);
                        // restore all the item data
                        ArrayList<ItemDataBean> itemDatas = itemDataDao.findAllByEventCRFId(eventCRF.getId());
                        for (int a = 0; a < itemDatas.size(); a++) {
                            ItemDataBean item = (ItemDataBean) itemDatas.get(a);
                            if (item.getStatus().equals(oldStatus)) {
                                item.setStatus(newStatus);
                                item.setUpdater(user);
                                item.setUpdatedDate(new Date());
                                itemDataDao.update(item);
                            }
                        }
                    }
                }
            }
        }

        return subjectBean;
    }
}
