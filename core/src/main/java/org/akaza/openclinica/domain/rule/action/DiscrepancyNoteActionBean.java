package org.akaza.openclinica.domain.rule.action;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Entity
@DiscriminatorValue("1")
public class DiscrepancyNoteActionBean extends RuleActionBean {

    private String message;

    public DiscrepancyNoteActionBean() {
        setActionType(ActionType.FILE_DISCREPANCY_NOTE);
        setRuleActionRun(new RuleActionRunBean(true, true, true, true, true));
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((message == null) ? 0 : message.hashCode());
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
        DiscrepancyNoteActionBean other = (DiscrepancyNoteActionBean) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        return true;
    }

}
