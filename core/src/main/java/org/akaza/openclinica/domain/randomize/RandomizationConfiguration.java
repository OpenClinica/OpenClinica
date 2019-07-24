package org.akaza.openclinica.domain.randomize;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;

public class RandomizationConfiguration implements Serializable {
    private String studyUuid;
    private String studyEnvUuid;
    private String runtimeURL;
    private String managerURL;
    private String studyOID;

    public RandomizationTarget targetField;

    private Map<String, String> stratificationFactors;

    private HashMap<String, String> attributes;

    public RandomizationConfiguration(){
        stratificationFactors = new LinkedHashMap<String, String>();
        attributes = new HashMap<String, String>();
        targetField = new RandomizationTarget();
    }

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

    public Map<String, String> getStratificationFactors() {
        return stratificationFactors;
    }

    public void setStratificationFactors(Map<String, String> stratificationFactors) {
        this.stratificationFactors = stratificationFactors;
    }

    public void addFactor(String key, String value) {
        stratificationFactors.put(key, value);}

    public HashMap<String, String> getAttributes() { return attributes; }
    public void setAttributes(HashMap<String, String> attributes) { this.attributes = attributes; }

    public void addAttribute(String key, String value) {
        attributes.put(key, value);}

     public String findAndRemove(String key){
         String stratValue = attributes.get(key);
         attributes.remove(key);
         return stratValue;
        }

    @JsonIgnore
    public List<String> getStratificationFactorsAsList(){
        List<String> stratFactors = new ArrayList<String>();

        for (Map.Entry<String, String> entry : stratificationFactors.entrySet()){
            stratFactors.add(entry.getValue());
        }
        return stratFactors;
    }

    @JsonIgnore
    public List<String> getAttributesAsList(){
        List<String> attributeList = new ArrayList<String>();

        for (Map.Entry<String, String> entry : attributes.entrySet()){
            attributeList.add(entry.getValue());
        }
        return attributeList;
    }

    @Override
    public String toString() {
        return "RandomizationConfiguration{" +
            "studyUuid='" + studyUuid + '\'' +
            ", studyEnvUuid='" + studyEnvUuid + '\'' +
            ", runtimeURL='" + runtimeURL + '\'' +
            ", managerURL='" + managerURL + '\'' +
            ", studyOID='" + studyOID + '\'' +
            ", targetField=" + targetField +
            ", stratificationFactors=" + getStratificationFactorsAsList().toString() +
            ", attributes=" + getAttributesAsList().toString() +
            '}';
    }
}
