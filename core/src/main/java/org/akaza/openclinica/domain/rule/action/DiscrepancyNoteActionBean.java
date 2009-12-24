package org.akaza.openclinica.domain.rule.action;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue("1")
public class DiscrepancyNoteActionBean extends RuleActionBean {

    private String message;

    public DiscrepancyNoteActionBean() {
        setActionType(ActionType.FILE_DISCREPANCY_NOTE);
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
