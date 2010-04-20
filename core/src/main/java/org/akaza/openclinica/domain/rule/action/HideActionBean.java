package org.akaza.openclinica.domain.rule.action;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("5")
public class HideActionBean extends RuleActionBean {

    private String message;
    private List<PropertyBean> properties;

    public HideActionBean() {
        setActionType(ActionType.HIDE);
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

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "rule_action_id", nullable = true)
    public List<PropertyBean> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyBean> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "HideActionBean [message=" + message + ", properties=" + properties + "]";
    }

}
