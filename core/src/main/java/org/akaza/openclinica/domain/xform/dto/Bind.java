package org.akaza.openclinica.domain.xform.dto;

public class Bind {
    private String nodeSet;
    private String type;
    private String readOnly;
    private String calculate;
    private String constraint;
    private String constraintMsg;
    private String required;
    private String jrPreload;
    private String relevant;

    public String getRelevant() {
        return relevant;
    }

    public void setRelevant(String relevant) {
        this.relevant = relevant;
    }

    public String getNodeSet() {
        return nodeSet;
    }

    public void setNodeSet(String nodeSet) {
        this.nodeSet = nodeSet;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(String readOnly) {
        this.readOnly = readOnly;
    }

    public String getCalculate() {
        return calculate;
    }

    public void setCalculate(String calculate) {
        this.calculate = calculate;
    }

    public String getConstraint() {
        return constraint;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public String getConstraintMsg() {
        return constraintMsg;
    }

    public void setConstraintMsg(String constraintMsg) {
        this.constraintMsg = constraintMsg;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public String getJrPreload() {
        return jrPreload;
    }

    public void setJrPreload(String jrPreload) {
        this.jrPreload = jrPreload;
    }
}