package org.akaza.openclinica.service.randomize;

import org.akaza.openclinica.domain.datamap.Study;

public interface ModuleProcessor {
    enum Modules {
        PARTICIPATE("participate"), RANDOMIZE("randomize");
        private String name;
        Modules(String name) {
            this.name = name;
        }
    }
    enum ModuleStatus {
        ENABLED("enabled"),
        DISABLED("disabled"),
        ACTIVE("active");
        private String name;
        ModuleStatus(String name) {
            this.name = name;
        }
    }
    void processModule(Study study, String isModuleEnabled, String accessToken);
}
