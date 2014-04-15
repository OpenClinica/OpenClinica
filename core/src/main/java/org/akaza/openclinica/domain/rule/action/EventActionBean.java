package org.akaza.openclinica.domain.rule.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * 
 * @author jnyayapathi
 *
 */
@Entity
public class EventActionBean extends RuleActionBean {

	
	private String oc_oid_reference;
    private List<PropertyBean> properties;

	
	 public EventActionBean() {
	        setActionType(ActionType.EVENT);
	        setRuleActionRun(new RuleActionRunBean(true, true, true, false, false));
	    }

	public String getOc_oid_reference() {
		return oc_oid_reference;
	}

	public void setOc_oid_reference(String oc_oid_reference) {
		this.oc_oid_reference = oc_oid_reference;
	}
	
	 @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	    @Fetch(value = FetchMode.SUBSELECT)
	    @JoinColumn(name = "rule_action_id", nullable = true)
	    public List<PropertyBean> getProperties() {
	        return properties;
	    }

	    public void setProperties(List<PropertyBean> properties) {
	        this.properties = properties;
	    }

	    @Transient
	    public void addProperty(PropertyBean property) {
	        if (properties == null) {
	            properties = new ArrayList<PropertyBean>();
	        }
	        properties.add(property);
	    }

	    @Override
	    @Transient
	    public String getSummary() {
	        return "";
	    }

	    @Override
	    @Transient
	    public HashMap<String, Object> getPropertiesForDisplay() {
	        LinkedHashMap<String, Object> p = new LinkedHashMap<String, Object>();
	        p.put("rule_action_type", getActionType().getDescription());
	        return p;
	    }

}
