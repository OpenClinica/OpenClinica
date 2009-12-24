/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.managestudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.domain.SourceDataVerification;

/**
 * @author jxu
 */
public class EventDefinitionCRFBean extends AuditableEntityBean implements Comparable {
    private int studyEventDefinitionId = 0;

    // issue 3212: the Event CRF is hidden from views in the application
    // when the user is associated with a site, not top-level study
    private boolean hideCrf = false;
    // A value that is transient, not part of the persistent domain object
    private boolean hidden = false;

    private int studyId = 0;

    private int crfId = 0;

    private boolean requiredCRF = true;

    private boolean doubleEntry = false;

    private boolean electronicSignature = false;

    private boolean requireAllTextFilled = false;// not on page for now

    private boolean decisionCondition = true;

    private int defaultVersionId = 0;
    //This value must match what is in the database presently, which are mostly blank values
    private SourceDataVerification sourceDataVerification = null;

    //private SourceDataVerification sourceDataVerification = SourceDataVerification.NOTREQUIRED;
    private String selectedVersionIds = "";
    private int parentId = 0;

    public String getSelectedVersionIds() {
        return selectedVersionIds;
    }

    public void setSelectedVersionIds(String selectedVersionIds) {
        this.selectedVersionIds = selectedVersionIds;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHideCrf() {
        return hideCrf;
    }

    public void setHideCrf(boolean hideCrf) {
        this.hideCrf = hideCrf;
    }

    /**
     * @return Returns the ordinal.
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * @param ordinal
     *            The ordinal to set.
     */
    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    private int ordinal = 1;

    /**
     * A comma-separated list of null values allowed. Getting this value is
     * deprecated. Use nullValuesList instead.
     */
    private String nullValues = "";

    /**
     * An array of null values allowed. Each element is a NullValue object. This
     * property is set in setNullValues.
     */
    private ArrayList nullValuesList = new ArrayList();

    private String crfName = ""; // not in table

    private ArrayList versions = new ArrayList();// not in table

    private CRFBean crf = new CRFBean(); // not in table

    private HashMap nullFlags = new LinkedHashMap(); // not in table

    private String defaultVersionName = "";// not in DB

    private String selectedVersionNames = "";// not in DB
    private ArrayList<Integer> selectedVersionIdList = new ArrayList<Integer>(); // not

    // in
    // DB

    /**
     * @return Returns the crfId.
     */
    public int getCrfId() {
        return crfId;
    }

    /**
     * @param crfId
     *            The crfId to set.
     */
    public void setCrfId(int crfId) {
        this.crfId = crfId;
    }

    /**
     * @return Returns the crfName.
     */
    public String getCrfName() {
        return crfName;
    }

    /**
     * @param crfName
     *            The crfName to set.
     */
    public void setCrfName(String crfName) {
        this.crfName = crfName;
    }

    /**
     * @return Returns the decisionCondition.
     */
    public boolean isDecisionCondition() {
        return decisionCondition;
    }

    /**
     * @param decisionCondition
     *            The decisionCondition to set.
     */
    public void setDecisionCondition(boolean decisionCondition) {
        this.decisionCondition = decisionCondition;
    }

    /**
     * @return Returns the defaultVersionId.
     */
    public int getDefaultVersionId() {
        return defaultVersionId;
    }

    /**
     * @param defaultVersionId
     *            The defaultVersionId to set.
     */
    public void setDefaultVersionId(int defaultVersionId) {
        this.defaultVersionId = defaultVersionId;
    }

    /**
     * @return Returns the doubleEntry.
     */
    public boolean isDoubleEntry() {
        return doubleEntry;
    }

    /**
     * @param doubleEntry
     *            The doubleEntry to set.
     */
    public void setDoubleEntry(boolean doubleEntry) {
        this.doubleEntry = doubleEntry;
    }

    /**
     * @return Returns the electronicSignature.
     */
    public boolean isElectronicSignature() {
        return electronicSignature;
    }

    /**
     * @param setElectronicSignature
     *            The electronicSignature to set.
     */
    public void setElectronicSignature(boolean setElectronicSignature) {
        this.electronicSignature = setElectronicSignature;
    }

    /**
     * @return Returns the requireAllTextFilled.
     */
    public boolean isRequireAllTextFilled() {
        return requireAllTextFilled;
    }

    /**
     * @param requireAllTextFilled
     *            The requireAllTextFilled to set.
     */
    public void setRequireAllTextFilled(boolean requireAllTextFilled) {
        this.requireAllTextFilled = requireAllTextFilled;
    }

    /**
     * @return Returns the requiredCRF.
     */
    public boolean isRequiredCRF() {
        return requiredCRF;
    }

    /**
     * @param requiredCRF
     *            The requiredCRF to set.
     */
    public void setRequiredCRF(boolean requiredCRF) {
        this.requiredCRF = requiredCRF;
    }

    /**
     * @return Returns the studyEventDefinitionId.
     */
    public int getStudyEventDefinitionId() {
        return studyEventDefinitionId;
    }

    /**
     * @param studyEventDefinitionId
     *            The studyEventDefinitionId to set.
     */
    public void setStudyEventDefinitionId(int studyEventDefinitionId) {
        this.studyEventDefinitionId = studyEventDefinitionId;
    }

    /**
     * @return Returns the studyId.
     */
    public int getStudyId() {
        return studyId;
    }

    /**
     * @param studyId
     *            The studyId to set.
     */
    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    /**
     * @deprecated
     * @return Returns the nullValues.
     */
    @Deprecated
    public String getNullValues() {
        return nullValues;
    }

    /**
     * @param nullValues
     *            The nullValues to set.
     */
    public void setNullValues(String nullValues) {
        this.nullValues = nullValues;
        String[] nullValuesSeparated = nullValues.split(",");

        nullValuesList = new ArrayList();
        if (nullValuesSeparated != null) {
            for (String val : nullValuesSeparated) {
                org.akaza.openclinica.bean.core.NullValue nv = org.akaza.openclinica.bean.core.NullValue.getByName(val);
                if (nv.isActive()) {
                    nullValuesList.add(nv);
                }
            }
        }
    }

    /**
     * @return Returns the versions.
     */
    public ArrayList getVersions() {
        return versions;
    }

    /**
     * @param versions
     *            The versions to set.
     */
    public void setVersions(ArrayList versions) {
        this.versions = versions;
    }

    /**
     * @return Returns the crf.
     */
    public CRFBean getCrf() {
        return crf;
    }

    /**
     * @param crf
     *            The crf to set.
     */
    public void setCrf(CRFBean crf) {
        this.crf = crf;
    }

    /**
     * @return Returns the nullFlags.
     */
    public HashMap getNullFlags() {
        if (nullFlags.size() == 0) {
            nullFlags.put("NI", "0");
            nullFlags.put("NA", "0");
            nullFlags.put("UNK", "0");
            nullFlags.put("NASK", "0");
            nullFlags.put("NAV", "0");
            nullFlags.put("ASKU", "0");
            nullFlags.put("NAV", "0");
            nullFlags.put("OTH", "0");
            nullFlags.put("PINF", "0");
            nullFlags.put("NINF", "0");
            nullFlags.put("MSK", "0");
            nullFlags.put("NPE", "0");


        }
        return nullFlags;
    }

    /**
     * @param nullFlags
     *            The nullFlags to set.
     */
    public void setNullFlags(HashMap nullFlags) {
        this.nullFlags = nullFlags;
    }

    /**
     * @return Returns the nullValuesList.
     */
    public ArrayList getNullValuesList() {
        return nullValuesList;
    }

    /**
     * @param nullValuesList
     *            The nullValuesList to set.
     */
    public void setNullValuesList(ArrayList nullValuesList) {
        this.nullValuesList = nullValuesList;
    }

    /**
     * @return Returns the defaultVersionName.
     */
    public String getDefaultVersionName() {
        return defaultVersionName;
    }

    /**
     * @param defaultVersionName
     *            The defaultVersionName to set.
     */
    public void setDefaultVersionName(String defaultVersionName) {
        this.defaultVersionName = defaultVersionName;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (o == null || !o.getClass().equals(this.getClass())) {
            return 0;
        }

        EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) o;
        return this.ordinal - edcb.ordinal;
    }

    public SourceDataVerification getSourceDataVerification() {
        return sourceDataVerification;
    }

    public void setSourceDataVerification(SourceDataVerification sourceDataVerification) {
        this.sourceDataVerification = sourceDataVerification;
    }

    public String getSelectedVersionNames() {
        return selectedVersionNames;
    }

    public void setSelectedVersionNames(String selectedVersionNames) {
        this.selectedVersionNames = selectedVersionNames;
    }

    public ArrayList<Integer> getSelectedVersionIdList() {
        return selectedVersionIdList;
    }

    public void setSelectedVersionIdList(ArrayList<Integer> selectedVersionIdList) {
        this.selectedVersionIdList = selectedVersionIdList;
    }
}