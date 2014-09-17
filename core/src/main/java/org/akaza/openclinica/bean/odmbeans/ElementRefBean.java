/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

/**
 * 
 * @author ywang (May, 2008)
 * 
 */

public class ElementRefBean {
    private String elementDefOID;
    private String mandatory;
    private int orderNumber;
    private String userName = "";
    private String fullName ="";
    public void setElementDefOID(String oid) {
        this.elementDefOID = oid;
    }

    public String getElementDefOID() {
        return this.elementDefOID;
    }

    public void setMandatory(String yesOrNo) {
        this.mandatory = yesOrNo;
    }

    public String getMandatory() {
        return this.mandatory;
    }
    
    public void setOrderNumber(int order) {
        this.orderNumber = order;
    }
    
    public int getOrderNumber() {
        return this.orderNumber;
    }

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}