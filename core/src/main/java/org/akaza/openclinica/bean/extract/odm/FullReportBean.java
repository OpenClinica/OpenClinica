/* OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */
package org.akaza.openclinica.bean.extract.odm;

import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.odmbeans.OdmAdminDataBean;
import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.bean.odmbeans.OdmStudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfPermissionTagDao;
import org.akaza.openclinica.service.PermissionService;

/**
 * Create one ODM XML file.
 * 
 * @author ywang (May, 2008)
 */

public class FullReportBean extends OdmXmlReportBean {
    private LinkedHashMap<String, OdmStudyBean> odmStudyMap;
    private LinkedHashMap<String, OdmClinicalDataBean> clinicalDataMap;
    private LinkedHashMap<String, OdmAdminDataBean> adminDataMap;
    private OdmClinicalDataBean clinicaldata;
    private CoreResources coreResources;

    /**
     * Create one ODM XML This method is still under construction. Right now it is for Snapshot filetype only.
     */
    public void createOdmXml(boolean isDataset, boolean clinical, DataSource dataSource, UserAccountBean userBean,String[] permissionTagsStringArray) {
        this.addHeading();
        this.addRootStartLine();

        // add the contents here in order
        // 1) the information about Study
        Iterator<OdmStudyBean> itm = this.odmStudyMap.values().iterator();
        while (itm.hasNext()) {
            OdmStudyBean s = itm.next();
            addNodeStudy(s, isDataset);
        }
        // 2) the information about administrative data
        String ODMVersion = this.getODMVersion();
        if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
            Iterator<OdmAdminDataBean> ita = this.adminDataMap.values().iterator();
            while (ita.hasNext()) {
                OdmAdminDataBean a = ita.next();
                addNodeAdminData(a);
            }
        }
        // 3) the information about reference data
        // addNodeReferenceData();
        // 4) the information about clinical Data
        if (this.clinicalDataMap != null) {
            Iterator<OdmClinicalDataBean> itc = this.clinicalDataMap.values().iterator();
            while (itc.hasNext()) {
                OdmClinicalDataBean c = itc.next();
                if (c.getExportSubjectData().size() > 0) {
                    addNodeClinicalData(c, clinical, dataSource, userBean,permissionTagsStringArray);
                }
            }
        }
        this.addRootEndLine();
    }

    /**
     * Currently, it incudes <MetadataVersion> and <AdminData>
     * 
     * @param isDataset
     */
    public void createStudyMetaOdmXml(boolean isDataset) {
        this.addHeading();
        this.addRootStartLine();

        // add the contents here in order
        // 1) the information about Study
        Iterator<OdmStudyBean> itm = this.odmStudyMap.values().iterator();
        while (itm.hasNext()) {
            OdmStudyBean s = itm.next();
            addNodeStudy(s, isDataset);
        }
        // 2) the information about administrative data
        Iterator<OdmAdminDataBean> ita = this.adminDataMap.values().iterator();
        while (ita.hasNext()) {
            OdmAdminDataBean a = ita.next();
            addNodeAdminData(a);
        }

        this.addRootEndLine();
    }

    public void createChunkedOdmXml(boolean isDataset, boolean header, boolean footer, DataSource dataSource, UserAccountBean userBean ,String[] permissionTagsStringArray) {
        ClinicalDataReportBean data = new ClinicalDataReportBean(this.clinicaldata, dataSource, userBean,false,permissionTagsStringArray);
        data.setXmlOutput(this.getXmlOutput());
        data.setODMVersion(this.getODMVersion());
        data.addNodeClinicalData(header, footer);
    }

    public void addNodeStudy(OdmStudyBean odmstudy, boolean isDataset) {
        MetaDataReportBean meta = new MetaDataReportBean(odmstudy, getCoreResources());
        meta.setODMVersion(this.getODMVersion());
        meta.setXmlOutput(this.getXmlOutput());
        meta.addNodeStudy(isDataset);
    }

    /*
     * Currently, this only be called for OpenClinica ODM extension
     */
    private void addNodeAdminData(OdmAdminDataBean adminData) {
        AdminDataReportBean admin = new AdminDataReportBean(adminData);
        admin.setODMVersion(this.getODMVersion());
        admin.setXmlOutput(this.getXmlOutput());
        admin.addNodeAdminData();
    }

    public void addNodeClinicalData(OdmClinicalDataBean clinicaldata, boolean crossForm, DataSource dataSource, UserAccountBean userBean,String[] permissionTagsStringArray) {
        ClinicalDataReportBean data = new ClinicalDataReportBean(clinicaldata, dataSource, userBean,crossForm,permissionTagsStringArray);
        data.setODMVersion(this.getODMVersion());
        data.setXmlOutput(this.getXmlOutput());
        data.addNodeClinicalData(true, true);
    }

    public LinkedHashMap<String, OdmStudyBean> getOdmStudyMap() {
        return odmStudyMap;
    }

    public void setOdmStudyMap(LinkedHashMap<String, OdmStudyBean> odmStudyMap) {
        this.odmStudyMap = odmStudyMap;
    }

    public LinkedHashMap<String, OdmClinicalDataBean> getClinicalDataMap() {
        return clinicalDataMap;
    }

    public void setClinicalDataMap(LinkedHashMap<String, OdmClinicalDataBean> clinicalDataMap) {
        this.clinicalDataMap = clinicalDataMap;
    }

    public LinkedHashMap<String, OdmAdminDataBean> getAdminDataMap() {
        return adminDataMap;
    }

    public void setAdminDataMap(LinkedHashMap<String, OdmAdminDataBean> adminDataMap) {
        this.adminDataMap = adminDataMap;
    }

    public void setClinicalData(OdmClinicalDataBean clinicaldata) {
        this.clinicaldata = clinicaldata;
    }

    public CoreResources getCoreResources() {
        return coreResources;
    }

    public void setCoreResources(CoreResources coreResources) {
        this.coreResources = coreResources;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.akaza.openclinica.bean.extract.odm.OdmXmlReportBean#createOdmXml(boolean)
     */
    @Override
    public void createOdmXml(boolean isDataset) {
        // TODO Auto-generated method stub

    }

}