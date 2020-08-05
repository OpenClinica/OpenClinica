package org.akaza.openclinica.dto.randomize;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.akaza.openclinica.service.AbstractAuditingDTO;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * A DTO for RandomizeService entity.
 */
@ApiModel(description = "Entity that defines a single randomization request.")
public class RandomizationDTO extends AbstractAuditingDTO implements Serializable {

    /**
     * Unique identifier of the randomization.
     */

    @ApiModelProperty(value = "Unique identifier of the randomization.")
    private String uuid;

    /**
     * Current status of the randomization request.
     */
    @ApiModelProperty(value = "Current status of the randomization request.")
    private Status status;

    /**
     * The subject OID for the participant that is being randomized.
     */
    @ApiModelProperty(value = "The subject OID for the participant that is being randomized.")
    private String subjectOid;

    /**
     * The study event OID corresponding to the event that the form with the randomization result is in.
     */
    @ApiModelProperty(value = "The study event OID corresponding to the event that the form with the randomization result is in.")
    private String studyEventOid;

    /**
     * The form OID corresponding to the form that the randomization result is being stored in.
     */
    @ApiModelProperty(value = "The form OID corresponding to the form that the randomization result is being stored in.")
    private String formOid;

    /**
     * The randomized value retrieved from Sealed Envelope to populate
     */
    @ApiModelProperty(value = "The randomized value retrieved from Sealed Envelope to populate")
    private String randomizeValue;

    /**
     * The corresponding study environment UUID that this randomization belongs to.
     */
    @ApiModelProperty(value = "The corresponding study environment UUID that this randomization belongs to.")
    private String studyEnvironmentUuid;

    /**
     * The corresponding study UUID that this randomization belongs to.
     */
    @ApiModelProperty(value = "The corresponding study UUID that this randomization belongs to.")
    private String studyUuid;

    /**
     * A JSON representation of the stratification factors for this randomization.
     */
    @ApiModelProperty(value = "A JSON representation of the stratification factors for this randomization.")
    private Map<String, String> stratificationFactors;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSubjectOid() {
        return subjectOid;
    }

    public void setSubjectOid(String subjectOid) {
        this.subjectOid = subjectOid;
    }

    public String getStudyEventOid() {
        return studyEventOid;
    }

    public void setStudyEventOid(String studyEventOid) {
        this.studyEventOid = studyEventOid;
    }

    public String getFormOid() {
        return formOid;
    }

    public void setFormOid(String formOid) {
        this.formOid = formOid;
    }

    public String getRandomizeValue() {
        return randomizeValue;
    }

    public void setRandomizeValue(String randomizeValue) {
        this.randomizeValue = randomizeValue;
    }

    public String getStudyEnvironmentUuid() {
        return studyEnvironmentUuid;
    }

    public void setStudyEnvironmentUuid(String studyEnvironmentUuid) {
        this.studyEnvironmentUuid = studyEnvironmentUuid;
    }

    public String getStudyUuid() {
        return studyUuid;
    }

    public void setStudyUuid(String studyUuid) {
        this.studyUuid = studyUuid;
    }

    public Map<String, String> getStratificationFactors() {
        return stratificationFactors;
    }

    public void setStratificationFactors(Map<String, String> stratificationFactors) {
        this.stratificationFactors = stratificationFactors;
    }

    public void addStratificationFactor(String key, String value){
        this.stratificationFactors.put(key, value);
    }

    @JsonIgnore
    public String getStratificationFactorsAsString() throws JsonProcessingException {
        String jsonString = new ObjectMapper().writeValueAsString(this.stratificationFactors);
        return jsonString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RandomizationDTO randomizationDTO = (RandomizationDTO) o;
        if (randomizationDTO.getUuid() == null || getUuid() == null) {
            return false;
        }
        return Objects.equals(getUuid(), randomizationDTO.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUuid());
    }

    @Override
    public String toString() {
        return "RandomizationDTO{" +
                ", uuid='" + getUuid() + "'" +
                ", status='" + getStatus() + "'" +
                ", subjectOid='" + getSubjectOid() + "'" +
                ", studyEventOid='" + getStudyEventOid() + "'" +
                ", formOid='" + getFormOid() + "'" +
                ", randomizeValue='" + getRandomizeValue() + "'" +
                ", studyEnvironmentUuid='" + getStudyEnvironmentUuid() + "'" +
                ", studyUuid='" + getStudyUuid() + "'" +
                ", stratificationFactors='" + getStratificationFactors() + "'" +
                "}";
    }
}