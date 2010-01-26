package org.akaza.openclinica.domain.rule.action;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("3")
public class InsertActionBean extends RuleActionBean {

    private String message;

    public InsertActionBean() {
        setActionType(ActionType.SHOW);
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
}
