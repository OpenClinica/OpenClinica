package core.org.akaza.openclinica.domain.datamap;


import core.org.akaza.openclinica.domain.DataMapDomainObject;
import core.org.akaza.openclinica.domain.enumsupport.JobStatus;
import core.org.akaza.openclinica.domain.enumsupport.JobType;
import core.org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table( name = "job_detail" )
@GenericGenerator( name = "id-generator", strategy = "native", parameters = {@org.hibernate.annotations.Parameter( name = "sequence_name", value = "job_detail_job_detail_id_seq" )} )
public class JobDetail extends DataMapDomainObject {

    private int jobDetailId;
    private String uuid;

    private JobStatus status;
    private JobType type;


    private UserAccount createdBy;
    private UserAccount updatedBy;
    private Study site;
    private Study study;

    private Date dateCreated;
    private Date dateUpdated;
    private Date dateCompleted;

    private String logPath;
    private String sourceFileName;


    @Id
    @Column( name = "job_detail_id", unique = true, nullable = false )
    @GeneratedValue( generator = "id-generator" )
    public int getJobDetailId() {
        return jobDetailId;
    }

    public void setJobDetailId(int jobDetailId) {
        this.jobDetailId = jobDetailId;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    public UserAccount getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserAccount createdBy) {
        this.createdBy = createdBy;
    }


    @Temporal(TemporalType.TIMESTAMP)
    @Column( name = "date_created" )
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }


    @Temporal(TemporalType.TIMESTAMP)
    @Column( name = "date_completed" )
    public Date getDateCompleted() {
        return dateCompleted;
    }


    public void setDateCompleted(Date dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column( name = "date_updated" )
    public Date getDateUpdated() {
        return this.dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    public UserAccount getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UserAccount updatedBy) {
        this.updatedBy = updatedBy;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id")
    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( name = "site_id" )
    public Study getSite() {
        return site;
    }

    public void setSite(Study site) {
        this.site = site;
    }

    @Column( name = "log_path" )
    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    @Enumerated( EnumType.STRING )
    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    @Enumerated( EnumType.STRING )
    public JobType getType() {
        return type;
    }

    public void setType(JobType type) {
        this.type = type;
    }

    @Column( name = "uuid" )
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Column( name = "source_file_name" )
    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }
}
