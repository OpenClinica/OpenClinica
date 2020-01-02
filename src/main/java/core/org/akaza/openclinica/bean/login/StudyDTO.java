package core.org.akaza.openclinica.bean.login;

import core.org.akaza.openclinica.domain.datamap.Study;

import java.util.ArrayList;
import java.util.Date;

public class StudyDTO {
	private String uniqueProtocolID;
	private String briefTitle;
	private String principalInvestigator;
	private String briefSummary;
	private String sponsor;
	private String protocolType;
	private String startDate;
	private String expectedTotalEnrollment;
	private String status;
	private String studyOid;
    private ArrayList<ErrorObject> errors;
    private String message;
    private ArrayList<UserRole> assignUserRoles;
	private String studyCreationLink;
	private String facilityName;
	private String facilityCity;
	private String facilityState;
	private String facilityCountry;
	private String facilityZipcode;
	private Date createdDate;
	private String uniqueIdentifier;

	public StudyDTO() {
		super();
	}


	public String getUniqueProtocolID() {
		return uniqueProtocolID;
	}

	public void setUniqueProtocolID(String uniqueProtocolID) {
		this.uniqueProtocolID = uniqueProtocolID;
	}


	public String getBriefTitle() {
		return briefTitle;
	}


	public void setBriefTitle(String briefTitle) {
		this.briefTitle = briefTitle;
	}


	public String getPrincipalInvestigator() {
		return principalInvestigator;
	}

	public void setPrincipalInvestigator(String principalInvestigator) {
		this.principalInvestigator = principalInvestigator;
	}

	public String getBriefSummary() {
		return briefSummary;
	}

	public void setBriefSummary(String briefSummary) {
		this.briefSummary = briefSummary;
	}

	public String getSponsor() {
		return sponsor;
	}

	public void setSponsor(String sponsor) {
		this.sponsor = sponsor;
	}

	public String getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}


	public String getStartDate() {
		return startDate;
	}


	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}


	public String getExpectedTotalEnrollment() {
		return expectedTotalEnrollment;
	}


	public void setExpectedTotalEnrollment(String expectedTotalEnrollment) {
		this.expectedTotalEnrollment = expectedTotalEnrollment;
	}

	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public String getStudyOid() {
		return studyOid;
	}


	public void setStudyOid(String studyOid) {
		this.studyOid = studyOid;
	}


	public ArrayList<ErrorObject> getErrors() {
		return errors;
	}


	public void setErrors(ArrayList<ErrorObject> errors) {
		this.errors = errors;
	}


	public String getMessage() {
		return message;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public ArrayList<UserRole> getAssignUserRoles() {
		return assignUserRoles;
	}


	public void setAssignUserRoles(ArrayList<UserRole> assignUserRoles) {
		this.assignUserRoles = assignUserRoles;
	}

	public String getStudyCreationLink() {
		return studyCreationLink;
	}

	public void setStudyCreationLink(String studyCreationLink) {
		this.studyCreationLink = studyCreationLink;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public String getFacilityCity() {
		return facilityCity;
	}

	public void setFacilityCity(String facilityCity) {
		this.facilityCity = facilityCity;
	}

	public String getFacilityState() {
		return facilityState;
	}

	public void setFacilityState(String facilityState) {
		this.facilityState = facilityState;
	}

	public String getFacilityZipcode() {
		return facilityZipcode;
	}

	public void setFacilityZipcode(String facilityZipcode) {
		this.facilityZipcode = facilityZipcode;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	public String getFacilityCountry() {
		return facilityCountry;
	}

	public void setFacilityCountry(String facilityCountry) {
		this.facilityCountry = facilityCountry;
	}

	public static StudyDTO studyToStudyDTO(Study s){
		StudyDTO sDTO = new StudyDTO();
		if(s != null) {
			sDTO.setUniqueProtocolID(s.getStudyId() + "");
			sDTO.setBriefTitle(s.getName());
			sDTO.setBriefSummary(s.getSummary());
			sDTO.setSponsor(s.getSponsor());
			sDTO.setProtocolType(s.getProtocolType());
			if(s.getDatePlannedStart() != null)
				sDTO.setStartDate((s.getDatePlannedStart()).toString());
			else
				sDTO.setStartDate(null);
			sDTO.setExpectedTotalEnrollment(s.getExpectedTotalEnrollment().toString());
			sDTO.setStatus(s.getStatus().getName());
			sDTO.setStudyOid(s.getOc_oid());
			sDTO.setFacilityName(s.getFacilityName());
			sDTO.setFacilityCity(s.getFacilityCity());
			sDTO.setFacilityState(s.getFacilityState());
			sDTO.setFacilityCountry(s.getFacilityCountry());
			sDTO.setFacilityZipcode(s.getFacilityZip());
			sDTO.setCreatedDate(s.getDateCreated());
			sDTO.setUniqueIdentifier(s.getUniqueIdentifier());
		}
		return  sDTO;
	}
}
