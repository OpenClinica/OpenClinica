package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public interface EventServiceInterface {

    public HashMap<String, String> scheduleEvent(UserAccountBean user, Date startDateTime, Date endDateTime, String location, String studyUniqueId,
            String siteUniqueId, String eventDefinitionOID, String studySubjectId) throws OpenClinicaSystemException;

    public ArrayList<StudyEventBean> retrieveEventInformation(UserAccountBean user, String studyUniqueId,
                                                              String siteUniqueId, String eventDefinitionOID, String studySubjectId) throws OpenClinicaSystemException;

}