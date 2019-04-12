package org.akaza.openclinica.domain.managestudy;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * @author: Shamim
 * Date: Feb 17, 2009
 * Time: 8:17:02 PM
 * There will be a single instance of this class which will maintain completion status of
 * different entities of study module.
 */
@Entity
@Table(name = "study_module_status")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "study_module_status_id_seq") })
public class StudyModuleStatus extends AbstractAuditableMutableDomainObject {
    public static final int NOT_STARTED = 1;
    public static final int IN_PROGRESS = 2;
    public static final int COMPLETED = 3; 

    private int studyId;
    private int study;
    private int crf;
    private int eventDefinition;
    private int subjectGroup;
    private int rule;
    private int site;
    private int users;

    private transient int studyStatus;

    public int getStudyId() {
        return studyId;
    }

    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    public int getStudy() {
        return study;
    }

    public void setStudy(int study) {
        this.study = study;
    }

    public int getCrf() {
        return crf;
    }

    public void setCrf(int crf) {
        this.crf = crf;
    }

    public int getEventDefinition() {
        return eventDefinition;
    }

    public void setEventDefinition(int eventDefinition) {
        this.eventDefinition = eventDefinition;
    }

    public int getSubjectGroup() {
        return subjectGroup;
    }

    public void setSubjectGroup(int subjectGroup) {
        this.subjectGroup = subjectGroup;
    }

    public int getRule() {
        return rule;
    }

    public void setRule(int rule) {
        this.rule = rule;
    }

    public int getSite() {
        return site;
    }

    public void setSite(int site) {
        this.site = site;
    }

    public int getUsers() {
        return users;
    }

    public void setUsers(int user) {
        this.users = user;
    }

    @Transient
    public int getStudyStatus() {
        return studyStatus;
    }

    public void setStudyStatus(int studyStatus) {
        this.studyStatus = studyStatus;
    }
}
