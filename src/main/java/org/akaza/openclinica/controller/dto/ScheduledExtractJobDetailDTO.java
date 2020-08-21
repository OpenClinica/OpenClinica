package org.akaza.openclinica.controller.dto;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ScheduledExtractJobDetailDTO {

    private Date dateCreated;
    private String jobExecutionUuid;

    public Date getDateCreated() {
        return dateCreated;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT")
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getJobExecutionUuid() {
        return jobExecutionUuid;
    }

    public void setJobExecutionUuid(String jobExecutionUuid) {
        this.jobExecutionUuid = jobExecutionUuid;
    }

}
