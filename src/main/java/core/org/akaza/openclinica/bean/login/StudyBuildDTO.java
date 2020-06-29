package core.org.akaza.openclinica.bean.login;

public class StudyBuildDTO {
    private String uniqueStudyID;
    private String briefTitle;
    private String uuid;
    private String studyEnvOid;
    private String studyEnvUuid;
    private String description;
    private String expectedStartDate;
    private String expectedEndDate;
    private String type;
    private String collectDateOfBirth;
    private boolean collectSex;
    private String collectPersonId;
    private boolean showSecondaryId;
    private String phase;
    private Integer expectedTotalEnrollment;
    private boolean enforceEnrollmentCap;
    private String status;
    private String participantIdTemplate;
    private String currentBoardUrl;

    /**
     * Default constructor
     */
    public StudyBuildDTO() {}

    /**
     * @return unique identifier of the study.
     */
    public String getUniqueStudyID() {
        return uniqueStudyID;
    }

    /**
     * Sets the unique identifier of the study.
     * @param uniqueStudyID the value to set
     */
    public void setUniqueStudyID(String uniqueStudyID) {
        this.uniqueStudyID = uniqueStudyID;
    }

    /**
     * @return title of the study being created.
     */
    public String getBriefTitle() {
        return briefTitle;
    }

    /**
     * Sets the title of the study being created.
     * @param briefTitle the value to set
     */
    public void setBriefTitle(String briefTitle) {
        this.briefTitle = briefTitle;
    }

    /**
     * @return the uuid of the study being created.
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Set the uuid of the study.
     * @param uuid the value to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return oid of the environment in which the study is being created.
     */
    public String getStudyEnvOid() {
        return studyEnvOid;
    }

    /**
     * Sets the study environment's oid.
     * @param studyEnvOid the value to set
     */
    public void setStudyEnvOid(String studyEnvOid) {
        this.studyEnvOid = studyEnvOid;
    }

    /**
     * @return uuid of the environment in which the study is being created.
     */
    public String getStudyEnvUuid() {
        return studyEnvUuid;
    }

    /**
     * Sets the study environment's uuid.
     * @param studyEnvUuid the value to set
     */
    public void setStudyEnvUuid(String studyEnvUuid) {
        this.studyEnvUuid = studyEnvUuid;
    }

    /**
     * @return description of the study.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description of the study.
     * @param description description of the study.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return expected start date of the study.
     */
    public String getExpectedStartDate() {
        return expectedStartDate;
    }

    /**
     * Sets expected start date of the study.
     * @param expectedStartDate expected start date of the study
     */
    public void setExpectedStartDate(String expectedStartDate) {
        this.expectedStartDate = expectedStartDate;
    }

    /**
     * @return expected end date of the study.
     */
    public String getExpectedEndDate() {
        return expectedEndDate;
    }

    /**
     * Sets expected end date of the study.
     * @param expectedEndDate expected end date of the study
     */
    public void setExpectedEndDate(String expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    /**
     * @return type of the study.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type of the study.
     * @param type type of the study
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return configuration value for how the date of birth should be collected during study.
     */
    public String getCollectDateOfBirth() {
        return collectDateOfBirth;
    }

    /**
     * Sets configuration value for how the date of birth should be collected during study.
     * @param collectDateOfBirth configuration value for how the date of birth should be collected during study
     */
    public void setCollectDateOfBirth(String collectDateOfBirth) {
        this.collectDateOfBirth = collectDateOfBirth;
    }

    /**
     * @return whether sex should be collected during study.
     */
    public boolean getCollectSex() {
        return collectSex;
    }

    /**
     * Sets whether sex should be collected during study.
     * @param collectSex whether sex should be collected during study
     */
    public void setCollectSex(boolean collectSex) {
        this.collectSex = collectSex;
    }

    /**
     * @return configuration value for how the Person ID should be collected.
     */
    public String getCollectPersonId() {
        return collectPersonId;
    }

    /**
     * Sets configuration value for how the Person ID should be collected.
     * @param collectPersonId configuration value for how the Person ID should be collected
     */
    public void setCollectPersonId(String collectPersonId) {
        this.collectPersonId = collectPersonId;
    }

    /**
     * @return whether the Secondary ID should be shown.
     */
    public boolean isShowSecondaryId() {
        return showSecondaryId;
    }

    /**
     * Sets whether the Secondary ID should be shown.
     * @param showSecondaryId whether the Secondary ID should be shown
     */
    public void setShowSecondaryId(boolean showSecondaryId) {
        this.showSecondaryId = showSecondaryId;
    }

    /**
     * @return phase of the study.
     */
    public String getPhase() {
        return phase;
    }

    /**
     * Sets phase of the study.
     * @param phase phase of the study
     */
    public void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * @return expected total enrollment for the study.
     */
    public Integer getExpectedTotalEnrollment() {
        return expectedTotalEnrollment;
    }

    /**
     * Sets expected total enrollment for the study.
     * @param expectedTotalEnrollment expected total enrollment for the study
     */
    public void setExpectedTotalEnrollment(Integer expectedTotalEnrollment) {
        this.expectedTotalEnrollment = expectedTotalEnrollment;
    }

    /**
     * @return status of the study environment.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets status of the study environment.
     * @param status status of the study environment
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return whether the enrollment cap defined by {@link #getExpectedTotalEnrollment()} should be enforced.
     */
    public boolean isEnforceEnrollmentCap() {
        return enforceEnrollmentCap;
    }

    /**
     * Sets whether the enrollment cap defined by {@link #getExpectedTotalEnrollment()} should be enforced.
     * @param enforceEnrollmentCap the value to set
     */
    public void setEnforceEnrollmentCap(boolean enforceEnrollmentCap) {
        this.enforceEnrollmentCap = enforceEnrollmentCap;
    }

    /**
     * @return template used for generating Participant ID.
     */
    public String getParticipantIdTemplate() {
        return participantIdTemplate;
    }

    /**
     * Sets template used for generating Participant ID.
     * @param participantIdTemplate the value to set
     */
    public void setParticipantIdTemplate(String participantIdTemplate) {
        this.participantIdTemplate = participantIdTemplate;
    }

    /**
     * @return currentBoardUrl the design page url
     */
    public String getCurrentBoardUrl() {
        return currentBoardUrl;
    }

    /**
     * Sets design page url
     * @param currentBoardUrl the design page url
     */
    public void setCurrentBoardUrl(String currentBoardUrl) {
        this.currentBoardUrl = currentBoardUrl;
    }
}