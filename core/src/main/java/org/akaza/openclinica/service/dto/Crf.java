
package org.akaza.openclinica.service.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties
public class Crf {

    @JsonProperty("description")
    private String description;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("ocoid")
    private String ocoid;
    @JsonProperty("protocolId")
    private String protocolId;
    @JsonProperty("versions")
    private List<Version> versions = new ArrayList<Version>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     *         The protocolId
     */
    @JsonProperty("protocolId")
    public String getProtocolId() {
        return protocolId;
    }

    /**
     * 
     * @param protocolId
     *            The protocolId
     */
    @JsonProperty("protocolId")
    public void setProtocolId(String protocolId) {
        this.protocolId = protocolId;
    }

    /**
     * 
     * @return
     *         The versions
     */
    @JsonProperty("versions")
    public List<Version> getVersions() {
        return versions;
    }

    /**
     * 
     * @param versions
     *            The versions
     */
    @JsonProperty("versions")
    public void setVersions(List<Version> versions) {
        this.versions = versions;
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
