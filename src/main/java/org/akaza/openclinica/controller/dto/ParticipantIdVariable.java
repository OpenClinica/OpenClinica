package org.akaza.openclinica.controller.dto;

public class ParticipantIdVariable {
    private String name;
    private String description;
    private Object sampleValue;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getSampleValue() {
        return sampleValue;
    }

    public void setSampleValue(Object sampleValue) {
        this.sampleValue = sampleValue;
    }
}
