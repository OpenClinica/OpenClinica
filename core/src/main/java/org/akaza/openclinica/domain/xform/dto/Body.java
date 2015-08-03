package org.akaza.openclinica.domain.xform.dto;

import java.util.List;

public class Body {
    private String cssClass = null;
    private String appearance = null;
    private List<Group> group;

    public List<Group> getGroup() {
        return group;
    }

    public void setGroup(List<Group> group) {
        this.group = group;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getAppearance() {
        return appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }
}