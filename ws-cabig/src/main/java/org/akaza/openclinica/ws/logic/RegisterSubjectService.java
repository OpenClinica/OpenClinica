package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.ws.bean.RegisterSubjectBean;
import org.akaza.openclinica.ws.cabig.exception.CCDataValidationFaultException;

import org.w3c.dom.Node;

import java.lang.CharSequence;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

public class RegisterSubjectService {
    
    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";
    
    public RegisterSubjectService() {
        
    }
    
    public RegisterSubjectBean generateSubjectBean(UserAccountBean user, Node subject) throws CCDataValidationFaultException {
        RegisterSubjectBean subjectBean = new RegisterSubjectBean(user);
        DomParsingService xmlService = new DomParsingService();
        String subjectDOB = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "birthDate", "value");
        String subjectGender = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "sexCode", "code");
        String identifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "identifier", "extension");
        String studyIdentifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "studyIdentifier", "extension");
        String studySiteIdentifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "studySiteIdentifier", "extension");
//        System.out.println("dob: " + subjectDOB + " gender " + subjectGender + 
//                " identifier " + identifier + " study " + studyIdentifier + " studysite " + studySiteIdentifier);
        subjectBean.setSiteUniqueIdentifier(studySiteIdentifier);
        subjectBean.setStudyUniqueIdentifier(studyIdentifier);
        subjectBean.setUniqueIdentifier(identifier);
        // throw an error if we dont get male or female as an answer
        if (!"male".equals(subjectGender.toLowerCase()) && ! "female".equals(subjectGender.toLowerCase())) {
            throw new CCDataValidationFaultException("Problem parsing sex, it should be either 'Male' or 'Female'.");
        }
        if ("Male".equals(subjectGender) || "male".equals(subjectGender)) {
            subjectBean.setGender("m");
        } else {
            subjectBean.setGender("f");
        }
        // no dases in dates?
        if (subjectDOB.contains("-")) {
            throw new CCDataValidationFaultException("Problem parsing date. Please remove all dashes and re-submit your data.");
        }
        SimpleDateFormat local_df = new SimpleDateFormat("yyyyMMdd");
        try {
            Date dateOfBirth = local_df.parse(subjectDOB);
            subjectBean.setDateOfBirth(dateOfBirth);
        } catch (ParseException pe) {
            // throw the data fault exception
            throw new CCDataValidationFaultException("Problem parsing date, it should be in YYYYMMDD format.");
        }
        return subjectBean;
    }
    
    public RegisterSubjectBean attachStudyIdentifiers(RegisterSubjectBean rsbean, Node milestone) throws CCDataValidationFaultException {
        DomParsingService xmlService = new DomParsingService();
        // <ns2:informedConsentDate value="20080101"/>
        // <ns2:registrationDate xsi:type="ns1:TS" value="20080825"/>
        // <ns2:registrationSiteIdentifier extension
//        String consentDateStr = xmlService.getElementValue(milestone, 
//                CONNECTOR_NAMESPACE_V1, "informedConsentDate", "value");
        String registrationDateStr = xmlService.getElementValue(milestone, 
                CONNECTOR_NAMESPACE_V1, "registrationDate", "value");
        String registrationSiteIdentifier = xmlService.getElementValue(milestone, 
                CONNECTOR_NAMESPACE_V1, "registrationSiteIdentifier", "extension");
        
        SimpleDateFormat local_df = new SimpleDateFormat("yyyyMMdd");
        try {
            Date enrollmentDate = local_df.parse(registrationDateStr);
            rsbean.setEnrollmentDate(enrollmentDate);
        } catch (ParseException pe) {
            // throw the data fault exception
            throw new CCDataValidationFaultException("Problem parsing date, it should be in YYYYMMDD format.");
        }
        rsbean.setStudySubjectLabel(registrationSiteIdentifier);
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
        char gender = rsbean.getGender().charAt(0);
        sbean.setGender(gender);
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

}
