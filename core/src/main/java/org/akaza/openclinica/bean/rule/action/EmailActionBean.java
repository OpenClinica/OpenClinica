package org.akaza.openclinica.bean.rule.action;

public class EmailActionBean extends RuleActionBean {

    private static final long serialVersionUID = 690522285231875517L;
    private String message;
    private String to;

    public EmailActionBean() {
        setActionType(ActionType.EMAIL);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String getSummary() {
        return this.message;
    }
}
