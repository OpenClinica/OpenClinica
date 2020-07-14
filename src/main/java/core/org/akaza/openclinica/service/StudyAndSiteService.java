package core.org.akaza.openclinica.service;

import core.org.akaza.openclinica.domain.datamap.Study;

public interface StudyAndSiteService {

    Study validateStudyExists(String studyOid, CustomRuntimeException validationErrors);
    Study validateSiteExists(String siteOid, CustomRuntimeException validationErrors);
}
