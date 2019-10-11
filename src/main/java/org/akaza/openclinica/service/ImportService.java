/**
 *
 */
package org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import core.org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import core.org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.controller.dto.DataImportReport;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.service.crfdata.ErrorObj;

import java.util.List;

/**
 * @author joekeremian
 *
 */

public interface ImportService {

    boolean validateAndProcessDataImport(ODMContainer odmContainer, String studyOid, String siteOid, UserAccountBean userAccountBean, String schema, JobDetail jobDetail, boolean isSystemUserImport);

    Object validateStudySubject(SubjectDataBean subjectDataBean, Study tenantStudy);

    ErrorObj validateStartAndEndDateAndOrder(StudyEventDataBean studyEventDataBean);

    ErrorObj validateEventStatus(String subjectEventStatus);

    StudyEvent updateStudyEventDates(StudyEvent studyEvent, UserAccount userAccount, String startDate, String endDate);

    ErrorObj validateEventRepeatKeyIntNumber(String repeatKey);

    StudyEvent scheduleEvent(StudyEventDataBean studyEventDataBean, StudySubject studySubject, StudyEventDefinition studyEventDefinition, UserAccount userAccount);

    ErrorObj validateEventTransition(StudyEvent studyEvent, UserAccount userAccount, String eventStatus);

     StudyEvent updateStudyEventDatesAndStatus(StudyEvent studyEvent, UserAccount userAccount, String startDate, String endDate,String eventStatus);

     StudyEvent updateStudyEvntStatus(StudyEvent studyEvent, UserAccount userAccount, String eventStatus);

     void writeToFile(List<DataImportReport> dataImportReports, String fileName, JobType jobType);

    }