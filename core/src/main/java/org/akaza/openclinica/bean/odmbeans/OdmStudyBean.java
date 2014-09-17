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
public class OdmStudyBean extends ElementOIDBean {
    private GlobalVariablesBean globalVariables;
    private BasicDefinitionsBean basicDefinitions;
    private MetaDataVersionBean metaDataVersion;
    
    private String parentStudyOID;

    public OdmStudyBean() {
        globalVariables = new GlobalVariablesBean();
        basicDefinitions = new BasicDefinitionsBean();
        metaDataVersion = new MetaDataVersionBean();
    }

    public void setGlobalVariables(GlobalVariablesBean gv) {
        this.globalVariables = gv;
    }

    public GlobalVariablesBean getGlobalVariables() {
        return this.globalVariables;
    }

    public void setMetaDataVersion(MetaDataVersionBean metadataversion) {
        this.metaDataVersion = metadataversion;
    }

    public MetaDataVersionBean getMetaDataVersion() {
        return this.metaDataVersion;
    }

    public BasicDefinitionsBean getBasicDefinitions() {
        return basicDefinitions;
    }

    public void setBasicDefinitions(BasicDefinitionsBean basicDefinitions) {
        this.basicDefinitions = basicDefinitions;
    }

    public String getParentStudyOID() {
        return parentStudyOID;
    }

    public void setParentStudyOID(String parentStudyOID) {
        this.parentStudyOID = parentStudyOID;
    }
}