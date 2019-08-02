package org.akaza.openclinica.service.randomize;

import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.enumsupport.ModuleStatus;

public interface ModuleProcessor {
    public enum Modules {
        PARTICIPATE("participate"), RANDOMIZE("randomize");
        private String name;
        Modules(String name) {
            this.name = name;
        }
    }
    void processModule(Study study, String isModuleEnabled, String accessToken);
}
