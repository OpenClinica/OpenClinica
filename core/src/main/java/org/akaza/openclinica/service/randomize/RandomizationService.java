package org.akaza.openclinica.service.randomize;

import org.akaza.openclinica.domain.randomize.RandomizationConfiguration;
import org.akaza.openclinica.domain.randomize.RandomizationDTO;

public interface RandomizationService extends ModuleProcessor {
    public static final String ENABLED = "enabled";
    public static final String DISABLED = "disabled";
    boolean isEnabled(String studyEnvUuid);
    public static final String STRATIFICATION_FACTOR = "stratificationFactor";
    RandomizationConfiguration getStudyConfig(String studyEnvUuid);
    void sendStratificationFactors(RandomizationDTO randomizationDTO, String accessToken);
}
