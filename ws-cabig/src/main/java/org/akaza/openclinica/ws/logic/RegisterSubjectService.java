package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.ws.bean.RegisterSubjectBean;

import org.w3c.dom.Node;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;

public class RegisterSubjectService {
    
    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";
    
    public RegisterSubjectService() {
        
    }
    public RegisterSubjectBean generateSubjectBean(UserAccountBean user, Node subject) {
        RegisterSubjectBean subjectBean = new RegisterSubjectBean(user);
        DomParsingService xmlService = new DomParsingService();
        String subjectDOB = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "birthDate", "value");
        String subjectGender = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "sexCode", "code");
        String identifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "identifier", "extension");
        String studyIdentifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "studyIdentifier", "extension");
        String studySiteIdentifier = xmlService.getElementValue(subject, CONNECTOR_NAMESPACE_V1, "studySiteIdentifier", "extension");
        System.out.println("dob: " + subjectDOB + " gender " + subjectGender + 
                " identifier " + identifier + " study " + studyIdentifier + " studysite " + studySiteIdentifier);
        subjectBean.setSiteUniqueIdentifier(studySiteIdentifier);
        subjectBean.setStudyUniqueIdentifier(studyIdentifier);
        subjectBean.setUniqueIdentifier(identifier);
        if ("Male".equals(subjectGender) || "male".equals(subjectGender)) {
            subjectBean.setGender("m");
        } else {
            subjectBean.setGender("f");
        }
        SimpleDateFormat local_df = new SimpleDateFormat("yyyyMMdd");
        try {
            Date dateOfBirth = local_df.parse(subjectDOB);
            subjectBean.setDateOfBirth(dateOfBirth);
        } catch (ParseException pe) {
            // do nothing here
        }
        return subjectBean;
    }

}
