
package org.akaza.openclinica.core.form.xform;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "queries",
    "logs"
})
public class QueriesBean {

    @JsonProperty("queries")
    private List<QueryBean> queries = new ArrayList<QueryBean>();
    @JsonProperty("logs")
    private List<LogBean> logs = new ArrayList<LogBean>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The queries
     */
    @JsonProperty("queries")
    public List<QueryBean> getQueries() {
        return queries;
    }

    /**
     * 
     * @param queries
     *     The queries
     */
    @JsonProperty("queries")
    public void setQueries(List<QueryBean> queries) {
        this.queries = queries;
    }

    /**
     * 
     * @return
     *     The logs
     */
    @JsonProperty("logs")
    public List<LogBean> getLogs() {
        return logs;
    }

    /**
     * 
     * @param logs
     *     The logs
     */
    @JsonProperty("logs")
    public void setLogs(List<LogBean> logs) {
        this.logs = logs;
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
