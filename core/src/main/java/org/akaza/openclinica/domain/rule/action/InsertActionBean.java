package org.akaza.openclinica.domain.rule.action;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("4")
public class InsertActionBean extends RuleActionBean {

    private List<PropertyBean> properties;

    public InsertActionBean() {
        setActionType(ActionType.INSERT);
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "rule_action_id", nullable = false)
    public List<PropertyBean> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyBean> properties) {
        this.properties = properties;
    }

    @Override
    @Transient
    public String getSummary() {
        return null;
    }
}
