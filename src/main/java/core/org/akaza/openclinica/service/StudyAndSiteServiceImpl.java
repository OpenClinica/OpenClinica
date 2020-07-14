package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.crfdata.ErrorObj;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("StudyAndSiteService")
public class StudyAndSiteServiceImpl implements StudyAndSiteService{

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    StudyDao studyDao;

    public static final String FAILED = "Failed";

    @Override
    public Study validateStudyExists(String studyOid, CustomRuntimeException validationErrors) {
        Study study = studyDao.findByOcOID(studyOid);
        if (study == null){
            validationErrors.addError(new ErrorObj(FAILED, ErrorConstants.ERR_STUDY_NOT_EXIST));
            return null;
        }

        if (!study.getStatus().isAvailable()) {
            validationErrors.addError(new ErrorObj(FAILED, ErrorConstants.ERR_STUDY_NOT_AVAILABLE));
        }

        return study;
    }

    @Override
    public Study validateSiteExists(String siteOid, CustomRuntimeException validationErrors) {
        Study site = studyDao.findByOcOID(siteOid);
        if (site == null){
            validationErrors.addError(new ErrorObj(FAILED, ErrorConstants.ERR_SITE_NOT_EXIST));
        }

        if (!site.getStatus().isAvailable()) {
            validationErrors.addError(new ErrorObj(FAILED, ErrorConstants.ERR_SITE_NOT_AVAILABLE));
        }

        return site;
    }

}
