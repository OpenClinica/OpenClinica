
package org.akaza.openclinica.core.form.xform;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "type",
    "message",
    "user",
    "assigned_to",
    "date_time",
    "status"
})
public class LogBean {

    @JsonProperty("type")
    private String type;
    @JsonProperty("message")
    private String message;
    @JsonProperty("user")
    private String user;
    @JsonProperty("assigned_to")
    private String assigned_to;
    @JsonProperty("date_time")
    private String date_time;
    @JsonProperty("status")
    private String status;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The type
     */
    @JsonProperty("type")
    public String getType() {
        return type;
    }

    /**
     * 
     * @param type
     *     The type
     */
    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 
     * @return
     *     The message
     */
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    /**
     * 
     * @param message
     *     The message
     */
    @JsonProperty("message")
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 
     * @return
     *     The user
     */
    @JsonProperty("user")
    public String getUser() {
        return user;
    }

    /**
     * 
     * @param user
     *     The user
     */
    @JsonProperty("user")
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * 
     * @return
     *     The assigned_to
     */
    @JsonProperty("assigned_to")
    public String getAssigned_to() {
        return assigned_to;
    }

    /**
     * 
     * @param assigned_to
     *     The assigned_to
     */
    @JsonProperty("assigned_to")
    public void setAssigned_to(String assigned_to) {
        this.assigned_to = assigned_to;
    }

    /**
     * 
     * @return
     *     The date_time
     */
    @JsonProperty("date_time")
    public String getDate_time() {
        return date_time;
    }

    /**
     * 
     * @param date_time
     *     The date_time
     */
    @JsonProperty("date_time")
    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    /**
     * 
     * @return
     *     The status
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     *     The status
     */
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
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
