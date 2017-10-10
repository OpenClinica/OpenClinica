package org.akaza.openclinica.dao.jpa;

import javax.persistence.*;
import java.util.Date;
import java.util.Arrays;

/**
 * Created by yogi on 4/13/17.
 */
@Entity @Table(name = "archived_study", schema = "public", catalog = "oc_archive")
public class ArchivedStudyEntity {
    private String uniqueIdentifier;
    private String name;
    private String summary;
    private Date datePlannedStart;
    private Date datePlannedEnd;
    private Date dateCreated;
    private Date dateUpdated;
    private Date dateArchived;
    private String schemaName;
    private byte[] studyData;

    @Id @Column(name = "unique_identifier") public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Basic @Column(name = "name") public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic @Column(name = "summary") public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @Basic @Column(name = "date_planned_start") public Date getDatePlannedStart() {
        return datePlannedStart;
    }

    public void setDatePlannedStart(Date datePlannedStart) {
        this.datePlannedStart = datePlannedStart;
    }

    @Basic @Column(name = "date_planned_end") public Date getDatePlannedEnd() {
        return datePlannedEnd;
    }

    public void setDatePlannedEnd(Date datePlannedEnd) {
        this.datePlannedEnd = datePlannedEnd;
    }

    @Basic @Column(name = "date_created") public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Basic @Column(name = "date_updated") public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Basic @Column(name = "date_archived") public Date getDateArchived() {
        return dateArchived;
    }

    public void setDateArchived(Date dateArchived) {
        this.dateArchived = dateArchived;
    }

    @Basic @Column(name = "schema_name") public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }


    @Lob @Column(name = "study_data") public byte[] getStudyData() {
        return studyData;
    }

    public void setStudyData(byte[] studyData) {
        this.studyData = studyData;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ArchivedStudyEntity that = (ArchivedStudyEntity) o;

        if (uniqueIdentifier != null ? !uniqueIdentifier.equals(that.uniqueIdentifier) : that.uniqueIdentifier != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        if (summary != null ? !summary.equals(that.summary) : that.summary != null)
            return false;
        if (datePlannedStart != null ? !datePlannedStart.equals(that.datePlannedStart) : that.datePlannedStart != null)
            return false;
        if (datePlannedEnd != null ? !datePlannedEnd.equals(that.datePlannedEnd) : that.datePlannedEnd != null)
            return false;
        if (dateCreated != null ? !dateCreated.equals(that.dateCreated) : that.dateCreated != null)
            return false;
        if (dateUpdated != null ? !dateUpdated.equals(that.dateUpdated) : that.dateUpdated != null)
            return false;
        if (dateArchived != null ? !dateArchived.equals(that.dateArchived) : that.dateArchived != null)
            return false;
        if (schemaName != null ? !schemaName.equals(that.schemaName) : that.schemaName != null)
            return false;
        if (!Arrays.equals(studyData, that.studyData))
            return false;

        return true;
    }

    @Override public int hashCode() {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (datePlannedStart != null ? datePlannedStart.hashCode() : 0);
        result = 31 * result + (datePlannedEnd != null ? datePlannedEnd.hashCode() : 0);
        result = 31 * result + (dateCreated != null ? dateCreated.hashCode() : 0);
        result = 31 * result + (dateUpdated != null ? dateUpdated.hashCode() : 0);
        result = 31 * result + (dateArchived != null ? dateArchived.hashCode() : 0);
        result = 31 * result + (schemaName != null ? schemaName.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(studyData);
        return result;
    }
}
