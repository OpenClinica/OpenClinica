/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;

import java.util.Date;
import java.util.HashMap;

public interface EventServiceInterface {

    public HashMap<String, String> scheduleEvent(UserAccountBean user, Date startDateTime, Date endDateTime, String location, String studyUniqueId,
            String siteUniqueId, String eventDefinitionOID, String studySubjectId) throws OpenClinicaSystemException;

}