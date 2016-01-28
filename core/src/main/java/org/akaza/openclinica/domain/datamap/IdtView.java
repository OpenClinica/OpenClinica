package org.akaza.openclinica.domain.datamap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.akaza.openclinica.domain.DataMapDomainObject;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;


@Entity
@Table(name = "view_item_data_toolkit_filter2")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class IdtView extends DataMapDomainObject{


private int itemDataId;
//private Study study;
private int parentStudyId;
private int studyId;
private String studySubjectLabel;
private String studyEventDefn;
private String sedOid;
private String crfOid;
private String iOid;
private int eventRepeatOrdinal;
private String crfName;
private String groupName;
private int groupOrdinal;
private String leftItemText;
private String value;
private int eventCrfId;
private int eventCrfStatusId;

private String path;
private String entityName;
private String tagStatus;


@Id
@Column(name = "item_data_id")
public int getItemDataId() {
    return itemDataId;
}
public void setItemDataId(int itemDataId) {
    this.itemDataId = itemDataId;
}
@Column(name = "study_subject_label")
public String getStudySubjectLabel() {
    return studySubjectLabel;
}
public void setStudySubjectLabel(String studySubjectLabel) {
    this.studySubjectLabel = studySubjectLabel;
}

@Column(name = "study_event_defn")
public String getStudyEventDefn() {
    return studyEventDefn;
}
public void setStudyEventDefn(String studyEventDefn) {
    this.studyEventDefn = studyEventDefn;
}
@Column(name = "event_repeat_ordinal")
public int getEventRepeatOrdinal() {
    return eventRepeatOrdinal;
}
public void setEventRepeatOrdinal(int eventRepeatOrdinal) {
    this.eventRepeatOrdinal = eventRepeatOrdinal;
}

@Column(name = "crf_name")
public String getCrfName() {
    return crfName;
}

public void setCrfName(String crfName) {
    this.crfName = crfName;
}

@Column(name = "group_name")
public String getGroupName() {
    return groupName;
}
public void setGroupName(String groupName) {
    this.groupName = groupName;
}

@Column(name = "group_ordinal")
public int getGroupOrdinal() {
    return groupOrdinal;
}
public void setGroupOrdinal(int groupOrdinal) {
    this.groupOrdinal = groupOrdinal;
}

@Column(name = "left_item_text")
public String getLeftItemText() {
    return leftItemText;
}
public void setLeftItemText(String leftItemText) {
    this.leftItemText = leftItemText;
}

@Column(name = "value")
public String getValue() {
    return value;
}
public void setValue(String value) {
    this.value = value;
}

@Column(name = "study_id")
public int getStudyId() {
    return studyId;
}
public void setStudyId(int studyId) {
    this.studyId = studyId;
}

@Column(name = "parent_study_id")
public int getParentStudyId() {
    return parentStudyId;
}
public void setParentStudyId(int parentStudyId) {
    this.parentStudyId = parentStudyId;
}

@Column(name = "event_crf_id")
public int getEventCrfId() {
    return eventCrfId;
}
public void setEventCrfId(int eventCrfId) {
    this.eventCrfId = eventCrfId;
}

@Column(name = "event_crf_status_id")
public int getEventCrfStatusId() {
    return eventCrfStatusId;
}
public void setEventCrfStatusId(int eventCrfStatusId) {
    this.eventCrfStatusId = eventCrfStatusId;
}

@Column(name = "sed_oid")
public String getSedOid() {
    return sedOid;
}
public void setSedOid(String sedOid) {
    this.sedOid = sedOid;
}
@Column(name = "crf_oid")
public String getCrfOid() {
    return crfOid;
}
public void setCrfOid(String crfOid) {
    this.crfOid = crfOid;
}
@Column(name = "i_oid")
public String getiOid() {
    return iOid;
}
public void setiOid(String iOid) {
    this.iOid = iOid;
}
@Column(name = "path")
public String getPath() {
    return path;
}
public void setPath(String path) {
    this.path = path;
}
@Column(name = "entity_name")
public String getEntityName() {
    return entityName;
}
public void setEntityName(String entityName) {
    this.entityName = entityName;
}
@Column(name = "tag_status")
public String getTagStatus() {
    return tagStatus;
}
public void setTagStatus(String tagStatus) {
    this.tagStatus = tagStatus;
}

    
    
}
