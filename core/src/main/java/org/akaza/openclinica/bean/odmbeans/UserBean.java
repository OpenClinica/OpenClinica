/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2010 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;


/**
 *
 * @author ywang (March, 2010)
 *
 */
public class UserBean extends ElementOIDBean {
    private String loginName;
    private String firstName;
    private String lastName;
    private String organization;
    private ElementRefBean locationRef;
    
    public String getLoginName() {
        return loginName;
    }
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getOrganization() {
        return organization;
    }
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    public ElementRefBean getLocationRef() {
        return locationRef;
    }
    public void setLocationRef(ElementRefBean locationRef) {
        this.locationRef = locationRef;
    }
}