package org.akaza.openclinica.web.pmanage;

public class Study {
	  private String instanceUrl = null;
	  private String studyOid = null;
	  private Organization organization = null;

	  public String getInstanceUrl() {
	      return instanceUrl;
	  }

	  public void setInstanceUrl(String instanceUrl) {
	      this.instanceUrl = instanceUrl;
	  }

	  public String getStudyOid() {
	      return studyOid;
	  }

	  public void setStudyOid(String studyOid) {
	      this.studyOid = studyOid;
	  }

	  public Organization getOrganization() {
	      return organization;
	  }

	  public void setOrganization(Organization organization) {
	      this.organization = organization;
	  }
}
