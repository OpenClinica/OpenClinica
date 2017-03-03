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
import org.akaza.openclinica.bean.submit.FormLayoutBean;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((crf == null) ? 0 : crf.hashCode());
        result = prime * result + crfId;
        result = prime * result + ((crfName == null) ? 0 : crfName.hashCode());
        result = prime * result + (decisionCondition ? 1231 : 1237);
        result = prime * result + defaultVersionId;
        result = prime * result + ((defaultVersionName == null) ? 0 : defaultVersionName.hashCode());
        result = prime * result + (doubleEntry ? 1231 : 1237);
        result = prime * result + (electronicSignature ? 1231 : 1237);
        result = prime * result + ((eventName == null) ? 0 : eventName.hashCode());
        result = prime * result + (hidden ? 1231 : 1237);
        result = prime * result + (hideCrf ? 1231 : 1237);
        result = prime * result + ((nullFlags == null) ? 0 : nullFlags.hashCode());
        result = prime * result + ((nullValues == null) ? 0 : nullValues.hashCode());
        result = prime * result + ((nullValuesList == null) ? 0 : nullValuesList.hashCode());
        result = prime * result + ordinal;
        result = prime * result + parentId;
        result = prime * result + (requireAllTextFilled ? 1231 : 1237);
        result = prime * result + (requiredCRF ? 1231 : 1237);
        result = prime * result + ((selectedVersionIdList == null) ? 0 : selectedVersionIdList.hashCode());
        result = prime * result + ((selectedVersionIds == null) ? 0 : selectedVersionIds.hashCode());
        result = prime * result + ((selectedVersionNames == null) ? 0 : selectedVersionNames.hashCode());
        result = prime * result + ((sourceDataVerification == null) ? 0 : sourceDataVerification.hashCode());
        result = prime * result + studyEventDefinitionId;
        result = prime * result + studyId;
        result = prime * result + ((versions == null) ? 0 : versions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        EventDefinitionCRFBean other = (EventDefinitionCRFBean) obj;
        if (crf == null) {
            if (other.crf != null)
                return false;
        } else if (!crf.equals(other.crf))
            return false;
        if (crfId != other.crfId)
            return false;
        if (crfName == null) {
            if (other.crfName != null)
                return false;
        } else if (!crfName.equals(other.crfName))
            return false;
        if (decisionCondition != other.decisionCondition)
            return false;
        if (defaultVersionId != other.defaultVersionId)
            return false;
        if (defaultVersionName == null) {
            if (other.defaultVersionName != null)
                return false;
        } else if (!defaultVersionName.equals(other.defaultVersionName))
            return false;
        if (doubleEntry != other.doubleEntry)
            return false;
        if (electronicSignature != other.electronicSignature)
            return false;
        if (eventName == null) {
            if (other.eventName != null)
                return false;
        } else if (!eventName.equals(other.eventName))
            return false;
        if (hidden != other.hidden)
            return false;
        if (hideCrf != other.hideCrf)
            return false;
        if (nullFlags == null) {
            if (other.nullFlags != null)
                return false;
        } else if (!nullFlags.equals(other.nullFlags))
            return false;
        if (nullValues == null) {
            if (other.nullValues != null)
                return false;
        } else if (!nullValues.equals(other.nullValues))
            return false;
        if (nullValuesList == null) {
            if (other.nullValuesList != null)
                return false;
        } else if (!nullValuesList.equals(other.nullValuesList))
            return false;
        if (ordinal != other.ordinal)
            return false;
        if (parentId != other.parentId)
            return false;
        if (requireAllTextFilled != other.requireAllTextFilled)
            return false;
        if (requiredCRF != other.requiredCRF)
            return false;
        if (selectedVersionIdList == null) {
            if (other.selectedVersionIdList != null)
                return false;
        } else if (!selectedVersionIdList.equals(other.selectedVersionIdList))
            return false;
        if (selectedVersionIds == null) {
            if (other.selectedVersionIds != null)
                return false;
        } else if (!selectedVersionIds.equals(other.selectedVersionIds))
            return false;
        if (selectedVersionNames == null) {
            if (other.selectedVersionNames != null)
                return false;
        } else if (!selectedVersionNames.equals(other.selectedVersionNames))
            return false;
        if (sourceDataVerification != other.sourceDataVerification)
            return false;
        if (studyEventDefinitionId != other.studyEventDefinitionId)
            return false;
        if (studyId != other.studyId)
            return false;
        if (versions == null) {
            if (other.versions != null)
                return false;
        } else if (!versions.equals(other.versions))
            return false;
        return true;
    }

    private int crfId = 0;

    private boolean requiredCRF = true;

    private boolean doubleEntry = false;

    private boolean electronicSignature = false;

    private boolean requireAllTextFilled = false;// not on page for now

    private boolean decisionCondition = true;

    private int defaultVersionId = 0;
    // This value must match what is in the database presently, which are mostly blank values
    private SourceDataVerification sourceDataVerification = null;

    // private SourceDataVerification sourceDataVerification = SourceDataVerification.NOTREQUIRED;
    private String selectedVersionIds = "";
    private int parentId = 0;
    private boolean participantForm;
    private boolean allowAnonymousSubmission;
    private String submissionUrl;
    private boolean offline;

    // Not in db
    private String eventName;

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

    private FormLayoutBean defaultCRF;

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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public FormLayoutBean getDefaultCRF() {
        return defaultCRF;
    }

    public void setDefaultCRF(FormLayoutBean defaultCRF) {
        this.defaultCRF = defaultCRF;
    }

    public boolean isParticipantForm() {
        return participantForm;
    }

    public void setParticipantForm(boolean participantForm) {
        this.participantForm = participantForm;
    }

    public boolean isAllowAnonymousSubmission() {
        return allowAnonymousSubmission;
    }

    public void setAllowAnonymousSubmission(boolean allowAnonymousSubmission) {
        this.allowAnonymousSubmission = allowAnonymousSubmission;
    }

    public String getSubmissionUrl() {
        return submissionUrl;
    }

    public void setSubmissionUrl(String submissionUrl) {
        this.submissionUrl = submissionUrl;

    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

}