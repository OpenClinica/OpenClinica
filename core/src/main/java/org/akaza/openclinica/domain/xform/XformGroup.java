package org.akaza.openclinica.domain.xform;

import java.util.ArrayList;

public class XformGroup {
    private String groupPath = null;
    private String groupName = null;
    private String groupDescription = null;
    private ArrayList<XformItem> items = null;
    private boolean isRepeating = false;

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

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(boolean isRepeating) {
        this.isRepeating = isRepeating;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        XformGroup other = (XformGroup) obj;
        if (groupDescription == null) {
            if (other.groupDescription != null)
                return false;
        } else if (!groupDescription.equals(other.groupDescription))
            return false;
        if (groupName == null) {
            if (other.groupName != null)
                return false;
        } else if (!groupName.equals(other.groupName))
            return false;
        if (groupPath == null) {
            if (other.groupPath != null)
                return false;
        } else if (!groupPath.equals(other.groupPath))
            return false;
        if (isRepeating != other.isRepeating)
            return false;
        return true;
    }

}
