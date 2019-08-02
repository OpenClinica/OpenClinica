package org.akaza.openclinica.service.randomize;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.akaza.openclinica.domain.randomize.RandomizationTarget;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RandomizationConfigurationFile implements Serializable {
    @JsonIgnore
    private String filename;
    private String studyUuid;
    public static final String STUDY_UUID = "studyUuid";
    private String studyEnvUuid;
    public static final String STUDY_ENV_UUID = "studyEnvironmentUuid";
    @JsonIgnore
    private String runtimeURL;
    public static final String RUNTIME_URL = "runtime.url";
    @JsonIgnore
    private String managerURL;
    public static final String MANAGER_URL = "manager.url";
    private String studyOID;
    public static final String STUDY_OID = "studyOID";
    @JsonIgnore
    private String sealedEnvelopeURL;
    public static final String SEALED_ENVELOPE_URL = "sealedEnvelope.url";
    @JsonIgnore
    private String sealedEnvelopeUser;
    public static final String SEALED_ENVELOPE_USER = "sealedEnvelope.user";
    @JsonIgnore
    private String sealedEnvelopePassword;
    public static final String SEALED_ENVELOPE_PASSWORD = "sealedEnvelope.password";

    public RandomizationTarget targetField;

    private HashMap<String, String> stratificationFactor;

    public RandomizationConfigurationFile(){
        stratificationFactor = new HashMap<String, String>();
        targetField = new RandomizationTarget();
    }

    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }

    public String getStudyUuid() { return studyUuid; }
    public void setStudyUuid(String studyUuid) { this.studyUuid = studyUuid; }

    public String getStudyEnvUuid() { return studyEnvUuid; }
    public void setStudyEnvUuid(String studyEnvUuid) { this.studyEnvUuid = studyEnvUuid; }

    public String getRuntimeURL() {
        return runtimeURL;
    }
    public void setRuntimeURL(String runtimeURL) {
        this.runtimeURL = runtimeURL;
    }

    public String getManagerURL() {
        return managerURL;
    }
    public void setManagerURL(String managerURL) {
        this.managerURL = managerURL;
    }

    public String getStudyOID() { return studyOID; }
    public void setStudyOID(String studyOID) { this.studyOID = studyOID; }

    public String getSealedEnvelopeURL() { return sealedEnvelopeURL; }
    public void setSealedEnvelopeURL(String sealedEnvelopeURL) { this.sealedEnvelopeURL = sealedEnvelopeURL; }

    public String getSealedEnvelopeUser() {
        return sealedEnvelopeUser;
    }
    public void setSealedEnvelopeUser(String sealedEnvelopeUser) {
        this.sealedEnvelopeUser = sealedEnvelopeUser;
    }

    public String getSealedEnvelopePassword() {
        return sealedEnvelopePassword;
    }
    public void setSealedEnvelopePassword(String sealedEnvelopePassword) { this.sealedEnvelopePassword = sealedEnvelopePassword; }

    public HashMap<String, String> getStratificationFactor() { return stratificationFactor; }
    public void setStratificationFactor(HashMap<String, String> stratificationFactor) { this.stratificationFactor = stratificationFactor; }
    public void addFactor(String key, String value) {
        stratificationFactor.put(key, value);}

    @JsonIgnore
    public List<String> getStratificationFactorsAsList(){
        List<String> stratFactors = new ArrayList<String>();

        for (Map.Entry<String, String> entry : stratificationFactor.entrySet()){
            stratFactors.add(entry.getValue());
        }
        return stratFactors;
    }


    @Override
    public String toString() {
        return "RandomizationConfigurationFile{" +
            "filename='" + filename + '\'' +
            ", studyUuid='" + studyUuid + '\'' +
            ", studyEnvUuid='" + studyEnvUuid + '\'' +
            ", runtimeURL='" + runtimeURL + '\'' +
            ", managerURL='" + managerURL + '\'' +
            ", studyOID='" + studyOID + '\'' +
            ", sealedEnvelopeURL='" + sealedEnvelopeURL + '\'' +
            ", sealedEnvelopeUser='" + sealedEnvelopeUser + '\'' +
            ", sealedEnvelopePassword='" + sealedEnvelopePassword + '\'' +
            ", targetField=" + targetField +
            ", stratificationFactor=" + stratificationFactor +
            '}';
    }
}
