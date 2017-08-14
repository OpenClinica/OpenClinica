/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.bean.oid.StudyOidGenerator;
import org.akaza.openclinica.bean.service.StudyParameterConfig;
import org.akaza.openclinica.domain.datamap.StudyEnvEnum;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.dao.core.CoreResources;

import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * @author thickerson
 * 
 * 
 */
public class StudyBean extends AuditableEntityBean {
    private int parentStudyId = 0;
    // YW << The original reason to add this is being able to list on
    // userbox.jsp the study name to which a site belong.
    // This property doesn't exist in the database table <Study>, so it might
    // not has value if it hasn't been assigned
    private String parentStudyName = "";
    // >> YW 06-12-2007
    private String officialTitle = "";
    private String identifier = "";
    private String secondaryIdentifier = "";
    private String summary = "";// need to be removed

    private Date datePlannedStart;
    private Date datePlannedEnd;

    // to designate genetic/non-genetic:
    private StudyType type = StudyType.NONGENETIC;// default type
    // private int typeId = 1;

    /**
     * <code>true</code> if the study manages pedigrees, <code>false</code>
     * otherwise Always equal to type.equals(StudyType.GENETIC). Not in the
     * database.
     */
    private boolean genetic = false;

    // private int statusId = 0;//?

    private String principalInvestigator = "";
    private String facilityName = "";
    private String facilityCity = "";
    private String facilityState = "";
    private String facilityZip = "";
    private String facilityCountry = "";
    private String facilityRecruitmentStatus = "";
    private String facilityContactName = "";
    private String facilityContactDegree = "";
    private String facilityContactPhone = "";
    private String facilityContactEmail = "";
    private String protocolType = "";
    private String protocolDescription = "";
    private Date protocolDateVerification;
    private String phase = "";
    private int expectedTotalEnrollment = 0;
    private String sponsor = "n_a";// ResourceBundleProvider.getAdminBundle().getString("n_a");
    private String collaborators = "";
    private String medlineIdentifier = "";
    private boolean resultsReference = false;
    private String schemaName = "";
    private StudyEnvEnum envType;
    private String studyEnvSiteUuid;
    private String studyEnvUuid;

    public String getStudyEnvUuid() {
        return studyEnvUuid;
    }

    public void setStudyEnvUuid(String studyEnvUuid) {
        this.studyEnvUuid = studyEnvUuid;
    }

    // private boolean usingDOB = false;
    // private boolean discrepancyManagement = false;
    private String oid;
    private OidGenerator oidGenerator = new StudyOidGenerator();

    private StudyParameterConfig studyParameterConfig = new StudyParameterConfig();

    private ArrayList studyParameters = new ArrayList();

    /**
     * @return Returns the studyParameters.
     */
    public ArrayList getStudyParameters() {
        return studyParameters;
    }

    /**
     * @param studyParameters
     *            The studyParameters to set.
     */
    public void setStudyParameters(ArrayList studyParameters) {
        this.studyParameters = studyParameters;
    }

    /**
     * @return Returns the officialTitle.
     */
    public String getOfficialTitle() {
        return officialTitle;
    }

    /**
     * @param officialTitle
     *            The officialTitle to set.
     */
    public void setOfficialTitle(String officialTitle) {
        this.officialTitle = officialTitle;
    }

    /**
     * @return Returns the resultsReference.
     */
    public boolean isResultsReference() {
        return resultsReference;
    }

    /**
     * @param resultsReference
     *            The resultsReference to set.
     */
    public void setResultsReference(boolean resultsReference) {
        this.resultsReference = resultsReference;
    }

    private String url = "";
    private String urlDescription = "";
    private String conditions = "";
    private String keywords = "";
    private String eligibility = "";
    private String gender = "both";
    private String ageMax = "";
    private String ageMin = "";
    private boolean healthyVolunteerAccepted = false;
    private String purpose = "";
    private String allocation = "";
    private String masking = "";
    private String control = "";
    private String assignment = "";
    private String endpoint = "";
    private String interventions = "";
    private String duration = "";
    private String selection = "";
    private String timing = "";

    public String getAbbreviatedName() {
        String name = getName();
        if (name.length() > 30) {
            name = name.substring(0, 27) + "...";
        }
        return name;
    }

    public String getAbbreviatedParentStudyName() {
        String parentStudyName = getParentStudyName();
        if (parentStudyName.length() > 30) {
            parentStudyName = parentStudyName.substring(0, 27) + "...";
        }
        return parentStudyName;
    }

    public String getAbbreviatedIdentifier() {
        String identifier = getIdentifier();
        if (identifier.length() > 25) {
            identifier = identifier.substring(0, 22) + "...";
        }
        return identifier;
    }

    /**
     * @return Returns the ageMax.
     */
    public String getAgeMax() {
        return ageMax;
    }

    /**
     * @param ageMax
     *            The ageMax to set.
     */
    public void setAgeMax(String ageMax) {
        this.ageMax = ageMax;
    }

    /**
     * @return Returns the ageMin.
     */
    public String getAgeMin() {
        return ageMin;
    }

    /**
     * @param ageMin
     *            The ageMin to set.
     */
    public void setAgeMin(String ageMin) {
        this.ageMin = ageMin;
    }

    /**
     * @return Returns the allocation.
     */
    public String getAllocation() {
        return ResourceBundleProvider.getResAdmin(allocation);
    }

    /**
     * @return Returns the allocation key, should be used when storing the Study
     *         in the database.
     */
    public String getAllocationKey() {
        return allocation;
    }

    /**
     * @param allocation
     *            The allocation to set.
     */
    public void setAllocation(String allocation) {
        this.allocation = allocation;
    }

    /**
     * @return Returns the assignment.
     */
    public String getAssignment() {
        return ResourceBundleProvider.getResAdmin(assignment);
    }

    /**
     * @return Returns the assignment key, should be used when storing the Study
     *         in the database.
     */
    public String getAssignmentKey() {
        return assignment;
    }

    /**
     * @param assignment
     *            The assignment to set.
     */
    public void setAssignment(String assignment) {
        this.assignment = assignment;
    }

    /**
     * @return Returns the collaborators.
     */
    public String getCollaborators() {
        return collaborators;
    }

    /**
     * @param collaborators
     *            The collaborators to set.
     */
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }


    public String getSchemaName() {
        return schemaName;
    }

    public StudyEnvEnum getEnvType() {
        return this.envType;
    }

    public void setEnvType(StudyEnvEnum envType) {
        this.envType = envType;
    }

    public String getStudyEnvSiteUuid() {
        return this.studyEnvSiteUuid;
    }

    public void setStudyEnvSiteUuid(String studyEnvSiteUuid) {
        this.studyEnvSiteUuid = studyEnvSiteUuid;
    }
    /**
     * @param collaborators
     *            The collaborators to set.
     */
    public void setCollaborators(String collaborators) {
        this.collaborators = collaborators;
    }


    /**
     * @return Returns the conditions.
     */
    public String getConditions() {
        return conditions;
    }

    /**
     * @param conditions
     *            The conditions to set.
     */
    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    /**
     * @return Returns the control.
     */
    public String getControl() {
        return ResourceBundleProvider.getResAdmin(control);
    }

    /**
     * @return Returns the control key, should be used when storing the Study in
     *         the database.
     */
    public String getControlKey() {
        return control;
    }

    /**
     * @param control
     *            The control to set.
     */
    public void setControl(String control) {
        this.control = control;
    }

    /**
     * @return Returns the datePlannedEnd.
     */
    public Date getDatePlannedEnd() {
        return datePlannedEnd;
    }

    /**
     * @param datePlannedEnd
     *            The datePlannedEnd to set.
     */
    public void setDatePlannedEnd(Date datePlannedEnd) {
        this.datePlannedEnd = datePlannedEnd;
    }

    /**
     * @return Returns the datePlannedStart.
     */
    public Date getDatePlannedStart() {
        return datePlannedStart;
    }

    /**
     * @param datePlannedStart
     *            The datePlannedStart to set.
     */
    public void setDatePlannedStart(Date datePlannedStart) {
        this.datePlannedStart = datePlannedStart;
    }

    /**
     * @return Returns the duration.
     */
    public String getDuration() {
        return ResourceBundleProvider.getResAdmin(duration);
    }

    /**
     * @return Returns the duration key, should be used when storing the Study
     *         in the database.
     */
    public String getDurationKey() {
        return duration;
    }

    /**
     * @param duration
     *            The duration to set.
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * @return Returns the eligibility.
     */
    public String getEligibility() {
        return eligibility;
    }

    /**
     * @param eligibility
     *            The eligibility to set.
     */
    public void setEligibility(String eligibility) {
        this.eligibility = eligibility;
    }

    /**
     * @return Returns the endpoint.
     */
    public String getEndpoint() {
        return ResourceBundleProvider.getResAdmin(endpoint);
    }

    /**
     * @return Returns the endpoint key, should be used when storing the Study
     *         in the database.
     */
    public String getEndpointKey() {
        return endpoint;
    }

    /**
     * @param endpoint
     *            The endpoint to set.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return Returns the expectedTotalEnrollment.
     */
    public int getExpectedTotalEnrollment() {
        return expectedTotalEnrollment;
    }

    /**
     * @param expectedTotalEnrollment
     *            The expectedTotalEnrollment to set.
     */
    public void setExpectedTotalEnrollment(int expectedTotalEnrollment) {
        this.expectedTotalEnrollment = expectedTotalEnrollment;
    }

    /**
     * @return Returns the facilityCity.
     */
    public String getFacilityCity() {
        return facilityCity;
    }

    /**
     * @param facilityCity
     *            The facilityCity to set.
     */
    public void setFacilityCity(String facilityCity) {
        this.facilityCity = facilityCity;
    }

    /**
     * @return Returns the facilityContactDegree.
     */
    public String getFacilityContactDegree() {
        return facilityContactDegree;
    }

    /**
     * @param facilityContactDegree
     *            The facilityContactDegree to set.
     */
    public void setFacilityContactDegree(String facilityContactDegree) {
        this.facilityContactDegree = facilityContactDegree;
    }

    /**
     * @return Returns the facilityContactEmail.
     */
    public String getFacilityContactEmail() {
        return facilityContactEmail;
    }

    /**
     * @param facilityContactEmail
     *            The facilityContactEmail to set.
     */
    public void setFacilityContactEmail(String facilityContactEmail) {
        this.facilityContactEmail = facilityContactEmail;
    }

    /**
     * @return Returns the facilityContactName.
     */
    public String getFacilityContactName() {
        return facilityContactName;
    }

    /**
     * @param facilityContactName
     *            The facilityContactName to set.
     */
    public void setFacilityContactName(String facilityContactName) {
        this.facilityContactName = facilityContactName;
    }

    /**
     * @return Returns the facilityContactPhone.
     */
    public String getFacilityContactPhone() {
        return facilityContactPhone;
    }

    /**
     * @param facilityContactPhone
     *            The facilityContactPhone to set.
     */
    public void setFacilityContactPhone(String facilityContactPhone) {
        this.facilityContactPhone = facilityContactPhone;
    }

    /**
     * @return Returns the facilityCountry.
     */
    public String getFacilityCountry() {
        return facilityCountry;
    }

    /**
     * @param facilityCountry
     *            The facilityCountry to set.
     */
    public void setFacilityCountry(String facilityCountry) {
        this.facilityCountry = facilityCountry;
    }

    /**
     * @return Returns the facilityName.
     */
    public String getFacilityName() {
        return facilityName;
    }

    /**
     * @param facilityName
     *            The facilityName to set.
     */
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    /**
     * @return Returns the facilityRecruitmentStatus.
     */
    public String getFacilityRecruitmentStatus() {
        return ResourceBundleProvider.getResAdmin(facilityRecruitmentStatus);
    }

    /**
     * @return Returns the facilityRecruitmentStatus key, should be used when
     *         storing the Study in the database.
     */
    public String getFacilityRecruitmentStatusKey() {
        return facilityRecruitmentStatus;
    }

    /**
     * @param facilityRecruitmentStatus
     *            The facilityRecruitmentStatus to set.
     */
    public void setFacilityRecruitmentStatus(String facilityRecruitmentStatus) {
        this.facilityRecruitmentStatus = facilityRecruitmentStatus;
    }

    /**
     * @return Returns the facilityState.
     */
    public String getFacilityState() {
        return facilityState;
    }

    /**
     * @param facilityState
     *            The facilityState to set.
     */
    public void setFacilityState(String facilityState) {
        this.facilityState = facilityState;
    }

    /**
     * @return Returns the facilityZip.
     */
    public String getFacilityZip() {
        return facilityZip;
    }

    /**
     * @param facilityZip
     *            The facilityZip to set.
     */
    public void setFacilityZip(String facilityZip) {
        this.facilityZip = facilityZip;
    }

    /**
     * @return Returns the gender.
     */
    public String getGender() {
        return ResourceBundleProvider.getResAdmin(gender);
    }

    /**
     * @return Returns the gender key, should be used when storing the Study in
     *         the database.
     */
    public String getGenderKey() {
        return gender;
    }

    /**
     * @param gender
     *            The gender to set.
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * @return Returns the healthyVolunteerAccepted.
     */
    public boolean getHealthyVolunteerAccepted() {
        return healthyVolunteerAccepted;
    }

    /**
     * @param healthyVolunteerAccepted
     *            The healthyVolunteerAccepted to set.
     */
    public void setHealthyVolunteerAccepted(boolean healthyVolunteerAccepted) {
        this.healthyVolunteerAccepted = healthyVolunteerAccepted;
    }

    /**
     * @return Returns the interventions, using the internationalized version of
     *         the intervention type.
     */
    public String getInterventions() {
        StringTokenizer st = new StringTokenizer(interventions, ",");
        StringBuffer sb = new StringBuffer();
        String intervention, name;
        while (st.hasMoreElements()) {
            StringTokenizer inter = new StringTokenizer(st.nextToken().toString(), "/");
            intervention = inter.nextToken();
            sb.append(ResourceBundleProvider.getResAdmin(intervention));
            sb.append("/");
            name = inter.nextToken();
            sb.append(name);
            sb.append(",");
        }
        if (sb.length() != 0)
            sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    /**
     * @return Returns the interventions, using the keys of the intervention
     *         types, should be used when storing the Study in the database.
     */
    public String getInterventionsKey() {
        return interventions;
    }

    /**
     * @param interventions
     *            The interventions to set.
     */
    public void setInterventions(String interventions) {
        this.interventions = interventions;
    }

    /**
     * @return Returns the keywords.
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * @param keywords
     *            The keywords to set.
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    /**
     * @return Returns the masking.
     */
    public String getMasking() {
        return ResourceBundleProvider.getResAdmin(masking);
    }

    /**
     * @return Returns the masking key, should be used when storing the Study in
     *         the database.
     */
    public String getMaskingKey() {
        return masking;
    }

    /**
     * @param masking
     *            The masking to set.
     */
    public void setMasking(String masking) {
        this.masking = masking;
    }

    /**
     * @return Returns the medlineIdentifier.
     */
    public String getMedlineIdentifier() {
        return medlineIdentifier;
    }

    /**
     * @param medlineIdentifier
     *            The medlineIdentifier to set.
     */
    public void setMedlineIdentifier(String medlineIdentifier) {
        this.medlineIdentifier = medlineIdentifier;
    }

    /**
     * @return Returns the objective.
     */

    /**
     * @return Returns the parentStudyId.
     */
    public int getParentStudyId() {
        return parentStudyId;
    }

    /**
     * @param parentStudyId
     *            The parentStudyId to set.
     */
    public void setParentStudyId(int parentStudyId) {
        this.parentStudyId = parentStudyId;
    }

    /**
     * @return Returns the parentStudyName
     */
    public String getParentStudyName() {
        return parentStudyName;
    }

    /**
     * @param parentStudyName
     *            String
     */
    public void setParentStudyName(String parentStudyName) {
        this.parentStudyName = parentStudyName;
    }

    /**
     * @return Returns the phase.
     */
    public String getPhase() {
        return ResourceBundleProvider.getResAdmin(phase);
    }

    /**
     * @return Returns the phase key, should be used when storing the Study in
     *         the database.
     */
    public String getPhaseKey() {
        return phase;
    }

    /**
     * @param phase
     *            The phase to set.
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * @return Returns the principalInvestigator.
     */
    public String getPrincipalInvestigator() {
        return principalInvestigator;
    }

    /**
     * @param principalInvestigator
     *            The principalInvestigator to set.
     */
    public void setPrincipalInvestigator(String principalInvestigator) {
        this.principalInvestigator = principalInvestigator;
    }

    /**
     * @return Returns the protocolDateVerification.
     */
    public Date getProtocolDateVerification() {
        return protocolDateVerification;
    }

    /**
     * @param protocolDateVerification
     *            The protocolDateVerification to set.
     */
    public void setProtocolDateVerification(Date protocolDateVerification) {
        this.protocolDateVerification = protocolDateVerification;
    }

    /**
     * @return Returns the protocolDescription.
     */
    public String getProtocolDescription() {
        return protocolDescription;
    }

    /**
     * @param protocolDescription
     *            The protocolDescription to set.
     */
    public void setProtocolDescription(String protocolDescription) {
        this.protocolDescription = protocolDescription;
    }

    /**
     * @return Returns the purpose.
     */
    public String getPurpose() {
        return ResourceBundleProvider.getResAdmin(purpose);
    }

    /**
     * @return Returns the purpose key, should be used when storing the Study in
     *         the database.
     */
    public String getPurposeKey() {
        return purpose;
    }

    /**
     * @param purpose
     *            The purpose to set.
     */
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    /**
     * @return Returns the selection.
     */
    public String getSelection() {
        return ResourceBundleProvider.getResAdmin(selection);
    }

    /**
     * @return Returns the selection key, should be used when storing the Study
     *         in the database.
     */
    public String getSelectionKey() {
        return selection;
    }

    /**
     * @param selection
     *            The selection to set.
     */
    public void setSelection(String selection) {
        this.selection = selection;
    }

    /**
     * @return Returns the sponsor.
     */
    public String getSponsor() {
        return sponsor;
    }

    /**
     * @param sponsor
     *            The sponsor to set.
     */
    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    /**
     * @deprecated
     * @return Returns the statusId.
     */
    @Deprecated
    public int getStatusId() {
        return status.getId();
    }

    /**
     * @deprecated
     * @param statusId
     *            The statusId to set.
     */
    @Deprecated
    public void setStatusId(int statusId) {
        Status s = Status.get(statusId);
        setStatus(s);
    }

    /**
     * @return Returns the timing.
     */
    public String getTiming() {
        return ResourceBundleProvider.getResAdmin(timing);
    }

    /**
     * @return Returns the timing key, should be used when storing the Study in
     *         the database.
     */
    public String getTimingKey() {
        return timing;
    }

    /**
     * @param timing
     *            The timing to set.
     */
    public void setTiming(String timing) {
        this.timing = timing;
    }

    /**
     * @return Returns the type.
     */
    public String getProtocolType() {
        return ResourceBundleProvider.getResAdmin(protocolType);
    }

    /**
     * @return Returns the type key, should be used when storing the Study in
     *         the database.
     */
    public String getProtocolTypeKey() {
        return protocolType;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setProtocolType(String type) {
        this.protocolType = type;
    }

    /**
     * @return Returns the type.
     */
    public StudyType getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set. <B>Note that this should be of type
     *            managestudy.StudyType, not core.StudyType.</B>
     */
    public void setType(StudyType type) {
        this.type = type;

        if (type.equals(StudyType.GENETIC)) {
            genetic = true;
        }
    }

    /**
     * @deprecated
     * @return Returns the typeId.
     */
    @Deprecated
    public int getTypeId() {
        return type.getId();
    }

    /**
     * @deprecated
     * @param typeId
     *            The typeId to set.
     */
    @Deprecated
    public void setTypeId(int typeId) {
        StudyType t = StudyType.get(typeId);
        setType(t);
    }

    /**
     * @return Returns the isGenetic.
     */
    public boolean isGenetic() {
        return genetic;
    }

    public void setGenetic(boolean genetic) {
        this.genetic = genetic;

        if (genetic) {
            type = StudyType.GENETIC;
        } else {
            type = StudyType.NONGENETIC;
        }
    }

    /**
     * @return Returns the uRL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            The uRL to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return Returns the uRLDescription.
     */
    public String getUrlDescription() {
        return urlDescription;
    }

    /**
     * @param description
     *            The uRLDescription to set.
     */
    public void setUrlDescription(String description) {
        urlDescription = description;
    }

    /**
     * @return Returns the identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier
     *            The identifier to set.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return Returns the secondaryIdentifier.
     */
    public String getSecondaryIdentifier() {
        return secondaryIdentifier;
    }

    /**
     * @param secondaryIdentifier
     *            The secondaryIdentifier to set.
     */
    public void setSecondaryIdentifier(String secondaryIdentifier) {
        this.secondaryIdentifier = secondaryIdentifier;
    }

    /**
     * @return Returns the summary.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * @param summary
     *            The summary to set.
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * @return Returns the studyParameterConfig.
     */
    public StudyParameterConfig getStudyParameterConfig() {
        return studyParameterConfig;
    }

    /**
     * @param studyParameterConfig
     *            The studyParameterConfig to set.
     */
    public void setStudyParameterConfig(StudyParameterConfig studyParameterConfig) {
        this.studyParameterConfig = studyParameterConfig;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public OidGenerator getOidGenerator() {
        return oidGenerator;
    }

    public void setOidGenerator(OidGenerator oidGenerator) {
        this.oidGenerator = oidGenerator;
    }

    public boolean isSite(int parentStudyId) {
        return parentStudyId > 0 ? true : false;
    }

    public String getManager() {
        return CoreResources.getStudyManager();
    }
}
