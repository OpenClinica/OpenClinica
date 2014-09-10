/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author ywang (May, 2008)
 * 
 */

public class OdmClinicalDataBean {
    private String studyOID;
    private String metaDataVersionOID;
    private List<ExportSubjectDataBean> exportSubjectData;

    /*
     * private List<StudyEventDataBean> studyEventData; private List<FormDataBean>
     * formData; private List<ImportItemGroupDataBean> itemGroupData; private
     * List<ImportItemDataBean> itemData;
     */

    public OdmClinicalDataBean() {
        exportSubjectData = new ArrayList<ExportSubjectDataBean>();
        /*
         * studyEventData = new ArrayList<StudyEventDataBean>(); formData = new
         * ArrayList<FormDataBean>(); itemGroupData = new ArrayList<ImportItemGroupDataBean>();
         * itemData = new ArrayList<ImportItemDataBean>();
         */
    }

    public void setStudyOID(String studyOID) {
        this.studyOID = studyOID;
    }

    public String getStudyOID() {
        return this.studyOID;
    }

    public void setMetaDataVersionOID(String metaDataVersionOID) {
        this.metaDataVersionOID = metaDataVersionOID;
    }

    public String getMetaDataVersionOID() {
        return this.metaDataVersionOID;
    }

    public void setExportSubjectData(List<ExportSubjectDataBean> subject) {
        this.exportSubjectData = subject;
    }

    public List<ExportSubjectDataBean> getExportSubjectData() {
        return this.exportSubjectData;
    }

    /*
     * public void setStudyEventData(List<StudyEventDataBean> studyEventData) {
     * this.studyEventData = studyEventData; }
     * 
     * public List<StudyEventDataBean> getStudyEventData() { return
     * this.studyEventData; }
     * 
     * public void setFormData(List<FormDataBean> formData) { this.formData =
     * formData; }
     * 
     * public List<FormDataBean> getFormData() { return this.formData; }
     * 
     * public void setItemGroupData(List<ImportItemGroupDataBean>
     * itemGroupData) { this.itemGroupData = itemGroupData; }
     * 
     * public List<ImportItemGroupDataBean> getItemGroupData() { return
     * this.itemGroupData; }
     * 
     * public void setItemData(List<ImportItemDataBean> itemData) {
     * this.itemData = itemData; }
     * 
     * public List<ImportItemDataBean> getItemData() { return this.itemData; }
     */
}