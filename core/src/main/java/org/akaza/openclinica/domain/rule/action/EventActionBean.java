package org.akaza.openclinica.domain.rule.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
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
@DiscriminatorValue("6")
public class EventActionBean extends RuleActionBean {

	
	private String oc_oid_reference;
    private List<PropertyBean> properties;

	
	 public EventActionBean() {
	        setActionType(ActionType.EVENT);
	        setRuleActionRun(new RuleActionRunBean(null, null, null, null, null, true,true,false,false,false,false));
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

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime
					* result
					+ ((oc_oid_reference == null) ? 0 : oc_oid_reference
							.hashCode());
			result = prime * result
					+ ((properties == null) ? 0 : properties.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			EventActionBean other = (EventActionBean) obj;
			if (oc_oid_reference == null) {
				if (other.oc_oid_reference != null)
					return false;
			} else if (!oc_oid_reference.equals(other.oc_oid_reference))
				return false;
			if (properties == null) {
				if (other.properties != null)
					return false;
			} else if (!properties.equals(other.properties))
				return false;
			return true;
		}

	    
}
