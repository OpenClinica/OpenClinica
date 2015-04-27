package org.akaza.openclinica.domain.rule.action;

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("7")
public class NotificationActionBean extends RuleActionBean {

    private String message;
    private String emailTo;

    @Column(name = "email_to")
    public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	public NotificationActionBean() {
        setActionType(ActionType.NOTIFICATION);
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
        p.put("rule_action_to", getEmailTo());
        p.put("rule_action_message", "\"" + getMessage() + "\"");

        return p;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((emailTo == null) ? 0 : emailTo.hashCode());
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
        NotificationActionBean other = (NotificationActionBean) obj;
        if (message == null) {
            if (other.message != null)
                return false;
        } else if (!message.equals(other.message))
            return false;
        if (emailTo == null) {
            if (other.emailTo != null)
                return false;
        } else if (!emailTo.equals(other.emailTo))
            return false;
        return true;
    }

}
