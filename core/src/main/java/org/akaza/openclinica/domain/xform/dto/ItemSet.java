package org.akaza.openclinica.domain.xform.dto;

public class ItemSet {
    private String nodeSet;
    private Value value;
    private Label label;

    public String getNodeSet() {
        return nodeSet;
    }

    public void setNodeSet(String nodeSet) {
        this.nodeSet = nodeSet;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        this.label = label;
    }

}
