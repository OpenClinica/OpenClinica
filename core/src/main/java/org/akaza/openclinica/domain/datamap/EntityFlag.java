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
@Table(name = "entity_flag")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class EntityFlag extends DataMapDomainObject{

private int entityFlagId;
private String entityName;
private int entityId;
private String tagStatus;


@Id
@Column(name = "entity_flag_id")
public int getEntityFlagId() {
    return entityFlagId;
}

public void setEntityFlagId(int entityFlagId) {
    this.entityFlagId = entityFlagId;
}

@Column(name = "entity_name")
public String getEntityName() {
    return entityName;
}

public void setEntityName(String entityName) {
    this.entityName = entityName;
}

@Column(name = "entity_id")
public int getEntityId() {
    return entityId;
}

public void setEntityId(int entityId) {
    this.entityId = entityId;
}


@Column(name = "tag_status")
public String getTagStatus() {
    return tagStatus;
}


public void setTagStatus(String tagStatus) {
    this.tagStatus = tagStatus;
}

    

    
}
