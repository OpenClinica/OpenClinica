package org.akaza.openclinica.controller.openrosa.processor;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import org.w3c.dom.Node;

/**
 * Created by yogi on 11/23/16.
 */
public class QueryServiceHelperBean {
    private Node itemNode;
    private SubmissionContainer container;
    private ItemData itemData;
    private int itemOrdinal;
    private ResolutionStatus resStatus;
    private DiscrepancyNote dn;
    private String parentElementName;
    private UserAccount userAccount;

    private DiscrepancyNote parentDn;

    public Node getItemNode() {
        return itemNode;
    }

    public SubmissionContainer getContainer() {
        return container;
    }

    public void setContainer(SubmissionContainer container) {
        this.container = container;
    }

    public ItemData getItemData() {
        return itemData;
    }

    public void setItemData(ItemData itemData) {
        this.itemData = itemData;
    }

    public int getItemOrdinal() {
        return itemOrdinal;
    }

    public void setItemOrdinal(int itemOrdinal) {
        this.itemOrdinal = itemOrdinal;
    }

    public ResolutionStatus getResStatus() {
        return resStatus;
    }

    public void setResStatus(ResolutionStatus resStatus) {
        this.resStatus = resStatus;
    }

    public void setItemNode(Node itemNode) {
        this.itemNode = itemNode;
    }

    public DiscrepancyNote getDn() {
        return dn;
    }

    public void setDn(DiscrepancyNote dn) {
        this.dn = dn;
    }

    public String getParentElementName() {
        return parentElementName;
    }

    public void setParentElementName(String parentElementName) {
        this.parentElementName = parentElementName;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public DiscrepancyNote getParentDn() {
        return parentDn;
    }

    public void setParentDn(DiscrepancyNote parentDn) {
        this.parentDn = parentDn;
    }
}
