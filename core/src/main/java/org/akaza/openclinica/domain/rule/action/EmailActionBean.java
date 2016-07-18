package org.akaza.openclinica.domain.rule.action;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Entity
@DiscriminatorValue("2")
public class EmailActionBean extends RuleActionBean {

    private String message;
    private String to;

    public EmailActionBean() {
        setActionType(ActionType.EMAIL);
        setRuleActionRun(new RuleActionRunBean(true, true, true, false, true));
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Column(name = "email_to")
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
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
        p.put("rule_action_to", getTo());
        p.put("rule_action_message", "\"" + getMessage() + "\"");

        return p;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((to == null) ? 0 : to.hashCode());
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
        EmailActionBean other = (EmailActionBean) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (to == null) {
            if (other.to != null)
                return false;
        } else if (!to.equals(other.to))
            return false;
        return true;
    }

}
