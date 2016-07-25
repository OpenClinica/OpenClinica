package org.akaza.openclinica.job;

import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialist class to log all export related information; for audit and accountability purposes.
 * Created by Jacob Rousseau on 16-Mar-2015.
 * Copyright CTMM-TraIT / VUmc (c) 2015
 */
public class ExportLogger {

    private static final Logger logger = LoggerFactory.getLogger(ExportLogger.class);

    public static void logAccessExportFile(UserAccountBean user, ArchivedDatasetFileBean archivedDatasetFileBean) {
        logger.info("User " + user.getName() + " accessed archived dataset file " + archivedDatasetFileBean.getName() +
                " ID: " + archivedDatasetFileBean.getId());
    }

    public static void exportExcelExportAuditLog(StudyBean currentStudy, UserAccountBean userAccountBean, StudySubjectBean studySubjectBean) {
        String studyInformation = "'" + currentStudy.getName() + "'";
        logger.info(userAccountBean.getName() + " performed audit log export for study " + studyInformation + "; subject: " + studySubjectBean.getLabel());
    }



    /**
     * Logs (on info level) the user name and the names of the fields in an export.
     * @param currentStudy the study being exported
     * @param user the user performing the export
     * @param datasetBean the dataset definition used for the export
     * @param itemDAO an item DAO object used to retrieve items from the data-base
     * @param fileDescription the type of the export as defined in the file <code>extract.properties</code>
     * @param notificationEmail the addresses of recipients to whom the notification email is sent
     *
     */
    public static void logExport(StudyBean currentStudy, UserAccountBean user, DatasetBean datasetBean, ItemDAO itemDAO, String fileDescription, String notificationEmail) {
        String studyInformation = "'" + currentStudy.getName() + "'";

        logger.info(user.getName() + " performed export '" + datasetBean.getName() +  "' for study " + studyInformation + "; format: " + fileDescription);
        logger.info("Mail notification to: " + notificationEmail);
        String exportedSubjectInfo = extractSubjectInfo(datasetBean);
        logger.info(exportedSubjectInfo);
        DatasetDAO datasetDAO = new DatasetDAO(null);
        String sql = datasetBean.getSQLStatement();
        String itemIDStr = datasetDAO.parseSQLDataset(sql, false, true);
        logger.info("Item IDs of fields exported: " + itemIDStr);
        String nameStr = "Item names: ";
        itemIDStr = StringUtils.remove(itemIDStr, '(');
        itemIDStr = StringUtils.remove(itemIDStr, ')');
        String[] itemIDStringList = StringUtils.split(itemIDStr, ',');

        for (String itemID : itemIDStringList) {
            Integer itemIDInt = Integer.valueOf(itemID);
            ItemBean item = (ItemBean) itemDAO.findByPK(itemIDInt);
            nameStr = nameStr + item.getName() + ", ";
        }
        nameStr = StringUtils.removeEnd(nameStr,", ");
        logger.info(nameStr);
    }

    private static String extractSubjectInfo(DatasetBean datasetBean) {
        String ret = "Exported subject information: ";

        ret = appendBooleanField(ret, datasetBean.isShowSubjectUniqueIdentifier(), "person ID");
        ret = appendBooleanField(ret, datasetBean.isShowSubjectSecondaryId(), "secondary ID");
        ret = appendBooleanField(ret, datasetBean.isShowSubjectDob(), "date-of-birth");
        ret = appendBooleanField(ret, datasetBean.isShowSubjectGender(), "gender");
        ret = appendBooleanField(ret, datasetBean.isShowSubjectStatus(), "subject status");
        ret = appendBooleanField(ret, datasetBean.isShowEventLocation(), "event location");
        ret = appendBooleanField(ret, datasetBean.isShowEventStart(), "event start");
        ret = appendBooleanField(ret, datasetBean.isShowEventEnd(), "event end");
        ret = appendBooleanField(ret, datasetBean.isShowEventStatus(), "event status");
        ret = appendBooleanField(ret, datasetBean.isShowSubjectAgeAtEvent(), "subject age at event");
        ret = appendBooleanField(ret, datasetBean.isShowCRFversion(), "CRF version");
        ret = appendBooleanField(ret, datasetBean.isShowCRFinterviewerName(), "CRF interviewer name");
        ret = appendBooleanField(ret, datasetBean.isShowCRFinterviewerDate(), "CRF interview date");
        ret = appendBooleanField(ret, datasetBean.isShowCRFcompletionDate(), "CRF completion date");
        ret = appendBooleanField(ret, datasetBean.isShowCRFstatus(), "CRF status");
        return ret;
    }

    private static String appendBooleanField(String outputString, boolean fieldSelected, String fieldName) {
        if (fieldSelected) {
            outputString += fieldName + ", ";
        }
        return outputString;
    }
}
