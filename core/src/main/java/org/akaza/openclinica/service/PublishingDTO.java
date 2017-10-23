package org.akaza.openclinica.service;

import java.util.Set;

import org.akaza.openclinica.domain.datamap.StudyEnvEnum;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PublishingDTO {

    @JsonProperty("versionIds")
    private Set<Long> versionIds;
    @JsonProperty("publishedEnvType")
    private StudyEnvEnum publishedEnvType;

    public Set<Long> getVersionIds() {
        return versionIds;
    }

    public void setVersionIds(Set<Long> versionIds) {
        this.versionIds = versionIds;
    }

    public StudyEnvEnum getPublishedEnvType() {
        return publishedEnvType;
    }

    public void setPublishedEnvType(StudyEnvEnum publishedEnvType) {
        this.publishedEnvType = publishedEnvType;
    }

}
