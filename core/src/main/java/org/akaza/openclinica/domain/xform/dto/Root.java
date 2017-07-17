package org.akaza.openclinica.domain.xform.dto;

import java.util.List;

public class Root {
    private List<RootItem> item;

    public List<RootItem> getItem() {
        return item;
    }

    public void setItem(List<RootItem> item) {
        this.item = item;
    }

}
