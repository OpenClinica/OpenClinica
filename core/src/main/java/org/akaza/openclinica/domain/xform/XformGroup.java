package org.akaza.openclinica.domain.xform;

import java.util.ArrayList;

public class XformGroup {
    private String groupPath = null;
    private String groupName = null;
    private String groupDescription = null;
    private ArrayList<XformItem> items = null;

    public XformGroup() {
        items = new ArrayList<XformItem>();
    }

    public String getGroupPath() {
        return groupPath;
    }

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDescription() {
        return groupDescription;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public ArrayList<XformItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<XformItem> items) {
        this.items = items;
    }

}
