/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

import java.util.List;

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
    private String fullName = "";
    private String name;
    private boolean defaultVersion;
    private List<ElementRefBean> formLayoutRefs;
    private ConfigurationParameters configurationParameters;
    private String url;
    private String status;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(boolean defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public List<ElementRefBean> getFormLayoutRefs() {
        return formLayoutRefs;
    }

    public void setFormLayoutRefs(List<ElementRefBean> formLayoutRefs) {
        this.formLayoutRefs = formLayoutRefs;
    }

    public ConfigurationParameters getConfigurationParameters() {
        return configurationParameters;
    }

    public void setConfigurationParameters(ConfigurationParameters configurationParameters) {
        this.configurationParameters = configurationParameters;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}