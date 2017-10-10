package org.akaza.openclinica.ws.validator;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;

import javax.sql.DataSource;

/**
 * Created by yogi on 4/7/17.
 */
public class AbstractValidator {
    protected DataSource dataSource;
    protected StudyDAO studyDAO;
    protected UserAccountDAO userAccountDAO;
    protected BaseVSValidatorImplementation helper;
    protected StudyBean getPublicStudy(String uniqueId) {
        StudyDAO studyDAO = new StudyDAO(dataSource);
        String studySchema = CoreResources.getRequestSchema();
        CoreResources.setRequestSchema("public");
        StudyBean study = studyDAO.findByUniqueIdentifier(uniqueId);
        CoreResources.setRequestSchema(studySchema);
        return study;
    }
}
