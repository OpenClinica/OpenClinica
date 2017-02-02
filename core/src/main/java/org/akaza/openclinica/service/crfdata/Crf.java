
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
@JsonPropertyOrder({
    "id",
    "ocoid",
    "protocolId",
    "name",
    "description",
    "versions"
})
public class Crf {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("ocoid")
    private String ocoid;
    @JsonProperty("protocolId")
    private String protocolId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("versions")
    private List<Version> versions = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
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

    @JsonProperty("protocolId")
    public String getProtocolId() {
        return protocolId;
    }

    @JsonProperty("protocolId")
    public void setProtocolId(String protocolId) {
        this.protocolId = protocolId;
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
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("versions")
    public List<Version> getVersions() {
        return versions;
    }

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
