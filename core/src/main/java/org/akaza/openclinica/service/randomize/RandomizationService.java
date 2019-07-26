package org.akaza.openclinica.service.randomize;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.hibernate.RandomizeQueryResult;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.randomize.RandomizationConfiguration;
import org.akaza.openclinica.domain.randomize.RandomizationDTO;

import java.util.List;
import java.util.Map;

public interface RandomizationService extends ModuleProcessor {
    String ENABLED = "enabled";
    String DISABLED = "disabled";
    boolean isEnabled(String studyEnvUuid);
    String STRATIFICATION_FACTOR = "stratificationFactor";
    RandomizationConfiguration getStudyConfig(String studyEnvUuid);
    void processRandomization(ItemData thisItemData, StudyBean parentPublicStudy, String accessToken, String studySubjectOID);
}
