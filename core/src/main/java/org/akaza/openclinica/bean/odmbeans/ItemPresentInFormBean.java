/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 
 *
 */

package org.akaza.openclinica.bean.odmbeans;




public class ItemPresentInFormBean {
    //attributes
    private String formOid;
    private String parentItemOid;
    private Integer ColumnNumber;
    private String pageNumber;
    private String defaultValue;
    private String phi;
    private String showItem;
    //elements (since they're simple now, not set classes for them)
    private String leftItemText;
    private String rightItemText;
    private String itemHeader;
    private String itemSubHeader;
    private String sectionLabel;
    private ItemResponseBean itemResponse;
    private SimpleConditionalDisplayBean simpleConditionalDisplay;
    
    
    private Integer orderInForm;
    
    public Integer getOrderInForm() {
		return orderInForm;
	}

	public void setOrderInForm(Integer orderInForm) {
		this.orderInForm = orderInForm;
	}

	public ItemPresentInFormBean() {
        itemResponse = new ItemResponseBean();
        simpleConditionalDisplay = new SimpleConditionalDisplayBean();
    }
    
    public String getFormOid() {
        return formOid;
    }
    public void setFormOid(String formOid) {
        this.formOid = formOid;
    }
    public String getParentItemOid() {
        return parentItemOid;
    }
    public void setParentItemOid(String parentItemOid) {
        this.parentItemOid = parentItemOid;
    }
    public Integer getColumnNumber() {
        return ColumnNumber;
    }
    public void setColumnNumber(Integer columnNumber) {
        ColumnNumber = columnNumber;
    }
    public String getPageNumber() {
        return pageNumber;
    }
    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }
    public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    public String getPhi() {
        return phi;
    }
    public void setPhi(String phi) {
        this.phi = phi;
    }
    public String getLeftItemText() {
        return leftItemText;
    }
    public void setLeftItemText(String leftItemText) {
        this.leftItemText = leftItemText;
    }
    public String getRightItemText() {
        return rightItemText;
    }
    public void setRightItemText(String rightItemText) {
        this.rightItemText = rightItemText;
    }
    public String getItemHeader() {
        return itemHeader;
    }
    public void setItemHeader(String itemHeader) {
        this.itemHeader = itemHeader;
    }
    public String getItemSubHeader() {
        return itemSubHeader;
    }
    public void setItemSubHeader(String itemSubHeader) {
        this.itemSubHeader = itemSubHeader;
    }
    public String getSectionLabel() {
        return sectionLabel;
    }
    public void setSectionLabel(String sectionLabel) {
        this.sectionLabel = sectionLabel;
    }
    public ItemResponseBean getItemResponse() {
        return itemResponse;
    }
    public void setItemResponse(ItemResponseBean itemResponse) {
        this.itemResponse = itemResponse;
    }
    public String getShowItem() {
        return showItem;
    }
    public void setShowItem(String showItem) {
        this.showItem = showItem;
    }
    public SimpleConditionalDisplayBean getSimpleConditionalDisplay() {
        return simpleConditionalDisplay;
    }
    public void setSimpleConditionalDisplay(SimpleConditionalDisplayBean simpleConditionalDisplay) {
        this.simpleConditionalDisplay = simpleConditionalDisplay;
    }
}