package org.akaza.openclinica.domain.rule.action;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

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

@Entity
@DiscriminatorValue("3")
public class ShowActionBean extends RuleActionBean {

    private String message;
    private List<PropertyBean> properties;
    private List<PropertyBean> lazyProperties = LazyList.decorate(new ArrayList<PropertyBean>(), FactoryUtils.instantiateFactory(PropertyBean.class));

    public ShowActionBean() {
        setActionType(ActionType.SHOW);
        setRuleActionRun(new RuleActionRunBean(true, true, true, false, false));
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    @Transient
    public String getSummary() {
        return this.message;
    }

    @Override
    @Transient
    public HashMap<String, Object> getPropertiesForDisplay() {
        LinkedHashMap<String, Object> p = new LinkedHashMap<String, Object>();
        p.put("rule_action_type", getActionType().getDescription());
        p.put("rule_action_message", "\"" + getMessage() + "\"");
        return p;
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

    @Transient
    public List<PropertyBean> getLazyProperties() {
        return lazyProperties;
    }

    public void setLazyProperties(List<PropertyBean> lazyProperties) {
        this.lazyProperties = lazyProperties;
    }

    @Override
    public String toString() {
        return "ShowActionBean [message=" + message + ", properties=" + properties + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (message == null ? 0 : message.hashCode());
        result = prime * result + (properties == null ? 0 : properties.hashCode());
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
        ShowActionBean other = (ShowActionBean) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else {// if (!properties.equals(other.properties))
            if (properties.size() != other.properties.size())
                return false;
            for (PropertyBean propertyBean : other.properties) {
                if (!properties.contains(propertyBean))
                    return false;
            }
        }
        return true;
    }

}
