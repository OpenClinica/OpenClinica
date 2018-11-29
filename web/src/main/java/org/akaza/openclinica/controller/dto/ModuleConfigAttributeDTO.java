package org.akaza.openclinica.controller.dto;



import org.akaza.openclinica.service.AbstractAuditingDTO;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the ModuleConfigAttribute entity.
 */
public class ModuleConfigAttributeDTO extends AbstractAuditingDTO implements Serializable {

    @NotNull
    private String key;

    @NotNull
    private String value;

    private String studyEnvironmentUuid;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    public String getStudyEnvironmentUuid() {
        return studyEnvironmentUuid;
    }

    public void setStudyEnvironmentUuid(String studyEnvironmentUuid) {
        this.studyEnvironmentUuid = studyEnvironmentUuid;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, "value");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, "value");
    }

    @Override
    public String toString() {
        return "ModuleConfigAttributeDTO{" +
                ", key='" + key + "'" +
                ", value='" + value + "'" +
                ", studyEnvironmentUuid='" + studyEnvironmentUuid + "'" +
                '}';
    }
}

