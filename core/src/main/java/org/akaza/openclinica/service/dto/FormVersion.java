
package org.akaza.openclinica.service.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class FormVersion {

    @JsonProperty("artifactURL")
    private String artifactURL;
    @JsonProperty("description")
    private String description;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("ocoid")
    private String ocoid;
    @JsonProperty("previewURL")
    private String previewURL;
    @JsonProperty("fileLinks")
    private List<String> fileLinks;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *         The artifactURL
     */
    @JsonProperty("artifactURL")
    public String getArtifactURL() {
        return artifactURL;
    }

    /**
     * 
     * @param artifactURL
     *            The artifactURL
     */
    @JsonProperty("artifactURL")
    public void setArtifactURL(String artifactURL) {
        this.artifactURL = artifactURL;
    }

    /**
     * 
     * @return
     *         The description
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * 
     * @param description
     *            The description
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * @return
     *         The id
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * 
     * @param id
     *            The id
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *         The name
     */
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    /**
     * 
     * @param name
     *            The name
     */
    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     * @return
     *         The ocoid
     */
    @JsonProperty("ocoid")
    public String getOcoid() {
        return ocoid;
    }

    /**
     * 
     * @param ocoid
     *            The ocoid
     */
    @JsonProperty("ocoid")
    public void setOcoid(String ocoid) {
        this.ocoid = ocoid;
    }

    /**
     * 
     * @return
     *         The previewURL
     */
    @JsonProperty("previewURL")
    public String getPreviewURL() {
        return previewURL;
    }

    /**
     * 
     * @param previewURL
     *            The previewURL
     */
    @JsonProperty("previewURL")
    public void setPreviewURL(String previewURL) {
        this.previewURL = previewURL;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @JsonAnyGetter
    public List<String> getFileLinks() {
        return fileLinks;
    }

    @JsonAnySetter
    public void setFileLinks(List<String> fileLinks) {
        this.fileLinks = fileLinks;
    }

}
