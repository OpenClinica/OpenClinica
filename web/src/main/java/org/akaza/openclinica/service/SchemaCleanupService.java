package org.akaza.openclinica.service;

import org.akaza.openclinica.controller.helper.StudyInfoObject;

/**
 * Created by yogi on 5/4/17.
 */
public interface SchemaCleanupService {
    void dropSchema(StudyInfoObject studyInfoObject);
}

