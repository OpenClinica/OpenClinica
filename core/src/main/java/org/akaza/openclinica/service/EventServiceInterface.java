package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;

import java.util.Date;
import java.util.HashMap;

public interface EventServiceInterface {

    public HashMap<String, String> validateAndSchedule(String studySubjectId, String studyUniqueId, String siteUniqueId, String eventDefinitionOID,
            String location, Date startDateTime, Date endDateTime, UserAccountBean user) throws OpenClinicaSystemException;

}