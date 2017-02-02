
package org.akaza.openclinica.service.crfdata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "ocoid", "name", "description", "previewURL", "artifactURL", "fileLinks" })
public class Version {

    @JsonProperty("id")
    private Object id;
    @JsonProperty("ocoid")
    private String ocoid;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private Object description;
    @JsonProperty("previewURL")
    private String previewURL;
    @JsonProperty("artifactURL")
    private String artifactURL;
    @JsonProperty("fileLinks")
    private List<String> fileLinks = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public Object getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Object id) {
        this.id = id;
    }

    @JsonProperty("ocoid")
    public String getOcoid() {
        return ocoid;
    }

    @JsonProperty("ocoid")
    public void setOcoid(String ocoid) {
        this.ocoid = ocoid;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("description")
    public Object getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(Object description) {
        this.description = description;
    }

    @JsonProperty("previewURL")
    public String getPreviewURL() {
        return previewURL;
    }

    @JsonProperty("previewURL")
    public void setPreviewURL(String previewURL) {
        this.previewURL = previewURL;
    }

    @JsonProperty("artifactURL")
    public String getArtifactURL() {
        return artifactURL;
    }

    @JsonProperty("artifactURL")
    public void setArtifactURL(String artifactURL) {
        this.artifactURL = artifactURL;
    }

    @JsonProperty("fileLinks")
    public List<String> getFileLinks() {
        return fileLinks;
    }

    @JsonProperty("fileLinks")
    public void setFileLinks(List<String> fileLinks) {
        this.fileLinks = fileLinks;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
