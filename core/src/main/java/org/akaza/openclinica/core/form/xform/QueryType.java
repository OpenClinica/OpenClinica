package org.akaza.openclinica.core.form.xform;

public enum QueryType {
    QUERY("comment", 3), REASON("reason", 4);

    private String name;
    private int value;

    private QueryType(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

}
