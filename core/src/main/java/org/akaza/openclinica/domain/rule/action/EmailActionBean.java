package org.akaza.openclinica.domain.rule.action;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

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
}
