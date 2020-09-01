package core.org.akaza.openclinica.service.modules;

import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.controller.dto.ModuleConfigDTO;

import java.util.List;

public interface ModuleProcessor {
    enum Modules {
        PARTICIPATE("Participate"), RANDOMIZE("Randomize"), INSIGHT ("Insight"), STUDY_CALENDAR("Study Calendar");
        private String name;
        Modules(String name) {
            this.name = name;
        }
    }
    void processModuleConfig(List<ModuleConfigDTO> moduleConfigDTOs, Study study);
}
