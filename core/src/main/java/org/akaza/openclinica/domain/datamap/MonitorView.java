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
@Table(name = "view_monitor")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MonitorView extends DataMapDomainObject{


private int itemDataId;
//private Study study;
private int parentStudyId;
private int studyId;
private String studySubjectLabel;
private String studyEventDefn;
private int eventRepeatOrdinal;
private String crfName;
private String groupName;
private int groupOrdinal;
private String leftItemText;
private String value;

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

/*
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_study_id")
public Study getStudy() {
    return study;
}
public void setStudy(Study study) {
    this.study = study;
}
*/

    
    
    
}
