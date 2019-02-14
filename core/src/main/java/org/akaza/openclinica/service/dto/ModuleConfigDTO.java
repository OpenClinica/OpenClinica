package org.akaza.openclinica.service.dto;


import org.akaza.openclinica.domain.enumsupport.ModuleStatus;
import org.akaza.openclinica.service.AbstractAuditingDTO;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * A DTO for the ModuleConfig entity.
 */
public class ModuleConfigDTO extends AbstractAuditingDTO implements Serializable {

    private String uuid;

    @NotNull
    private ModuleStatus status;

    @NotNull
    private String studyUuid;

    private String moduleUuid;

    private String moduleName;

    private Set<ModuleConfigAttributeDTO> attributes;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public ModuleStatus getStatus() {
        return status;
    }

    public void setStatus(ModuleStatus status) {
        this.status = status;
    }
    public String getStudyUuid() {
        return studyUuid;
    }

    public void setStudyUuid(String studyUuid) {
        this.studyUuid = studyUuid;
    }

    public String getModuleUuid() {
        return moduleUuid;
    }

    public void setModuleUuid(String moduleUuid) {
        this.moduleUuid = moduleUuid;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Set<ModuleConfigAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<ModuleConfigAttributeDTO> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModuleConfigDTO moduleConfigDTO = (ModuleConfigDTO) o;
        if ( ! Objects.equals(uuid, moduleConfigDTO.getUuid())) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

    @Override
    public String toString() {
        return "ModuleConfigDTO{" +
                ", uuid='" + uuid + "'" +
                ", status='" + status + "'" +
                ", studyUuid='" + studyUuid + "'" +
                ", moduleUuid='" + moduleUuid + "'" +
                '}';
    }
}
