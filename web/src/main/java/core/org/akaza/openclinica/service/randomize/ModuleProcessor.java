package core.org.akaza.openclinica.service.randomize;

import core.org.akaza.openclinica.domain.datamap.Study;

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
        private String value;
        ModuleStatus(String value) {
            this.value = value;
        }
        public String getValue() {return value;}
    }
    void processModule(Study study, String isModuleEnabled, String accessToken);
}
