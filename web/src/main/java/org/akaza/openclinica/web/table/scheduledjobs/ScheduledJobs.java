package org.akaza.openclinica.web.table.scheduledjobs;
/**
 * This is a row element for each scheduled bean.
 * @author jnyayapathi
 *
 */
public class ScheduledJobs {
    private String datasetId; //misnomer its actually dataset name
    private String scheduledFireTime;
    private String checkbox;
    private String fireTime;
    private String action;
    private String exportFileName;
    private String jobStatus;
    public ScheduledJobs(){
        fireTime="";
        datasetId= "";
        scheduledFireTime="";
        checkbox="";
    }
    
    

public String getFireTime() {
    return fireTime;
}
public void setFireTime(String fireTime) {
    this.fireTime = fireTime;
}
public String getDatasetId() {
    return datasetId;
}
public void setDatasetId(String datasetId) {
    this.datasetId = datasetId;
}
public String getScheduledFireTime() {
    return scheduledFireTime;
}
public void setScheduledFireTime(String scheduledFireTime) {
    this.scheduledFireTime = scheduledFireTime;
}

public String getCheckbox() {
    return checkbox;
}
public void setCheckbox(String checkbox) {
    this.checkbox = checkbox;
}



public void setAction(String action) {
    this.action = action;
}



public String getAction() {
    return action;
}



public void setExportFileName(String exportFileName) {
    this.exportFileName = exportFileName;
}



public String getExportFileName() {
    return exportFileName;
}



public void setJobStatus(String jobStatus) {
    this.jobStatus = jobStatus;
}



public String getJobStatus() {
    return jobStatus;
}

}
