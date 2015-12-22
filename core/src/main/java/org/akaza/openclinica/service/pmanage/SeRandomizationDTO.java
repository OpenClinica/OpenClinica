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
    private String ocUser_username;
    private String ocUser_name;
    private String ocUser_lastname;
    private String ocUser_emailAddress;
    private String studyName;
    
    
    
    public String getStudyName() {
        return studyName;
    }
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }
    
    
    
    public String getOcUser_emailAddress() {
        return ocUser_emailAddress;
    }
    public void setOcUser_emailAddress(String ocUser_emailAddress) {
        this.ocUser_emailAddress = ocUser_emailAddress;
    }
 
    
    
    public String getOcUser_username() {
        return ocUser_username;
    }
    public void setOcUser_username(String ocUser_username) {
        this.ocUser_username = ocUser_username;
    }
    public String getOcUser_name() {
        return ocUser_name;
    }
    public void setOcUser_name(String ocUser_name) {
        this.ocUser_name = ocUser_name;
    }
    public String getOcUser_lastname() {
        return ocUser_lastname;
    }
    public void setOcUser_lastname(String ocUser_lastname) {
        this.ocUser_lastname = ocUser_lastname;
    }
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
