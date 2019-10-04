package org.akaza.openclinica.dto.randomize;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.org.akaza.openclinica.service.AbstractAuditingDTO;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * A DTO for RandomizeService entity.
 */
public class RandomizationDTO extends AbstractAuditingDTO implements Serializable {

    /**
     * Unique identifier of the randomization.
     */

    private String uuid;

    /**
     * Current status of the randomization request.
     */
    private Status status;

    /**
     * The subject OID for the participant that is being randomized.
     */
    private String subjectOid;

    /**
     * The randomized value retrieved from Sealed Envelope to populate
     */
    private String randomizeValue;

    /**
     * The corresponding study environment UUID that this randomization belongs to.
     */
    private String studyEnvironmentUuid;

    /**
     * The corresponding study UUID that this randomization belongs to.
     */
    private String studyUuid;

    /**
     * A JSON representation of the stratification factors for this randomization.
     */
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
                ", randomizeValue='" + getRandomizeValue() + "'" +
                ", studyEnvironmentUuid='" + getStudyEnvironmentUuid() + "'" +
                ", studyUuid='" + getStudyUuid() + "'" +
                ", stratificationFactors='" + getStratificationFactors() + "'" +
                "}";
    }
}