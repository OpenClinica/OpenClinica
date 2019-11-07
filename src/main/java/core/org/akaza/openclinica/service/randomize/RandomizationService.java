package core.org.akaza.openclinica.service.randomize;

import core.org.akaza.openclinica.domain.datamap.ItemData;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.dto.randomize.RandomizationConfiguration;

import java.util.Map;

public interface RandomizationService extends ModuleProcessor {
    boolean isEnabled(String studyEnvUuid);
    String STRATIFICATION_FACTOR = "stratificationFactor";
    RandomizationConfiguration getStudyConfig(String studyEnvUuid);
    void processRandomization(Study parentPublicStudy, String accessToken, String studySubjectOID, ItemData... optionalItemData);
    boolean refreshConfigurations(String accessToken, Map<String, String> results);
}
