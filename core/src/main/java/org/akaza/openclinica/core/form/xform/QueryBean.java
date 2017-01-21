
package org.akaza.openclinica.core.form.xform;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "type", "id", "date_time", "comment", "status", "assigned_to", "notify" })
public class QueryBean {

    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("date_time")
    private String date_time;
    @JsonProperty("comment")
    private String comment;
    @JsonProperty("status")
    private String status;
    @JsonProperty("user")
    private String user;
    @JsonProperty("assigned_to")
    private String assigned_to;
    @JsonProperty("notify")
    private Boolean notify;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *         The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id
     *            The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 
     * @return
     *         The date_time
     */
    @JsonProperty("date_time")
    public String getDate_time() {
        return date_time;
    }

    /**
     * 
     * @param date_time
     *            The date_time
     */
    @JsonProperty("date_time")
    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    /**
     * 
     * @return
     *         The comment
     */
    @JsonProperty("comment")
    public String getComment() {
        return comment;
    }

    /**
     * 
     * @param comment
     *            The comment
     */
    @JsonProperty("comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * 
     * @return
     *         The status
     */
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     *            The status
     */
    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 
     * @return
     *         The assigned_to
     */
    @JsonProperty("assigned_to")
    public String getAssigned_to() {
        return assigned_to;
    }

    /**
     * 
     * @param assigned_to
     *            The assigned_to
     */
    @JsonProperty("assigned_to")
    public void setAssigned_to(String assigned_to) {
        this.assigned_to = assigned_to;
    }

    /**
     *
     * @return
     *         The user
     */
    @JsonProperty("user")
    public String getUser() {
        return user;
    }

    /**
     *
     * @param user
     *            The user
     */
    @JsonProperty("user")
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * 
     * @return
     *         The notify
     */
    @JsonProperty("notify")
    public Boolean getNotify() {
        return notify;
    }

    /**
     * 
     * @param notify
     *            The notify
     */
    @JsonProperty("notify")
    public void setNotify(Boolean notify) {
        this.notify = notify;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
