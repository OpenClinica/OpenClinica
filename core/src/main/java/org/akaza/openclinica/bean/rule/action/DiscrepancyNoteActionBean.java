package org.akaza.openclinica.bean.rule.action;

public class DiscrepancyNoteActionBean extends RuleActionBean {

    private static final long serialVersionUID = -2315041919657806316L;
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
    public String getSummary() {
        return this.message;
    }
}
