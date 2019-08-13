package org.akaza.openclinica.service.randomize;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.dto.randomize.RandomizationConfiguration;

import java.util.Map;

public interface RandomizationService extends ModuleProcessor {
    boolean isEnabled(String studyEnvUuid);
    String STRATIFICATION_FACTOR = "stratificationFactor";
    RandomizationConfiguration getStudyConfig(String studyEnvUuid);
    void processRandomization(StudyBean parentPublicStudy, String accessToken, String studySubjectOID, ItemData... optionalItemData);
    boolean refreshConfigurations(String accessToken, Map<String, String> results);
}
