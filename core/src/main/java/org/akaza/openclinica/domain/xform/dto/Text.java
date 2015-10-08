package org.akaza.openclinica.domain.xform.dto;

import java.util.List;

public class Text {
    private String id;
    private List<Value> value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Value> getValue() {
        return value;
    }

    public void setValue(List<Value> value) {
        this.value = value;
    }

}
