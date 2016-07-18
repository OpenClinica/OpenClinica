package org.akaza.openclinica.domain.rule.action;


import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Entity
@DiscriminatorValue("8")
public class RandomizeActionBean extends RuleActionBean {

    private List<PropertyBean> properties;
    private List<StratificationFactorBean> stratificationFactors;
      
    
    public RandomizeActionBean() {
        setActionType(ActionType.RANDOMIZE);
        setRuleActionRun(new RuleActionRunBean(true, true, true, false, false));
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
        RandomizeActionBean other = (RandomizeActionBean) obj;
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
        if (stratificationFactors == null) {
            if (other.stratificationFactors != null)
                return false;
        } else {// if (!properties.equals(other.properties))
            if (other.stratificationFactors==null || stratificationFactors.size() != other.stratificationFactors.size())
                return false;
            for (StratificationFactorBean stratificationFactorBean : other.stratificationFactors) {
                if (!stratificationFactors.contains(stratificationFactorBean))
                    return false;
            }
        }
        return true;
    }

    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "rule_action_id", nullable = true)
    public List<StratificationFactorBean> getStratificationFactors() {
        return stratificationFactors;
    }

    public void setStratificationFactors(List<StratificationFactorBean> stratificationFactors) {
        this.stratificationFactors = stratificationFactors;
    }
    
    public void addStratificationFactor(StratificationFactorBean stratificationFactor) {
        if (stratificationFactors == null) {
            stratificationFactors = new ArrayList<StratificationFactorBean>();
        }
        stratificationFactors.add(stratificationFactor);
    }    

}


