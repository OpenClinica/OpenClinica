package org.akaza.openclinica.job;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialist class to log all privacy related information which is exported with an OpenClinica export.
 * Created by Jacob Rousseau on 16-Mar-2015.
 * Copyright CTMM-TraIT / VUmc (c) 2015
 */
public class ExportLogger {

    private static final Logger logger = LoggerFactory.getLogger(ExportLogger.class);

    public static void logExport(StudyBean currentStudy, UserAccountBean user, DatasetBean datasetBean) {
        String studyInformation = "'" + currentStudy.getName() + "', parent study name '" + currentStudy.getParentStudyName() + "'";

        logger.info(user.getName() + " performed export '" + datasetBean.getName() +  "' + for study " + studyInformation);

        DatasetDAO datasetDAO = new DatasetDAO(null);
        String sql = datasetBean.getSQLStatement();
        String itemIDList = datasetDAO.parseSQLDataset(sql, false, true);
        logger.info("Item IDs of fields exported: " + itemIDList);
    }
}
