package org.akaza.openclinica.service.pmanage;

public class SeRandomizationDTO {

    private Long id = null;
    private String url=null;
    private String username=null;
    private String password=null;
    private Long statusId;
    private String status;
    private String instanceUrl = null;
    private String studyOid = null;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public Long getStatusId() {
        return statusId;
    }
    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
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


}
