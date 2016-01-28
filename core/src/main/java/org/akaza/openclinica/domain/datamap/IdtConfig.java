package org.akaza.openclinica.domain.datamap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.akaza.openclinica.domain.DataMapDomainObject;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;


@Entity
@Table(name = "item_data_toolkit_config")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "item_data_toolkit_config_config_id_seq") })
public class IdtConfig extends DataMapDomainObject{

private int configId;
private String path;
private Tag tag;
private boolean active;


@Id
@Column(name = "config_id", unique = true, nullable = false)
@GeneratedValue(generator = "id-generator")
public int getConfigId() {
    return configId;
}

public void setConfigId(int configId) {
    this.configId = configId;
}

@Column(name = "path")
public String getPath() {
    return path;
}

public void setPath(String path) {
    this.path = path;
}


@Column(name = "active")
public boolean isActive() {
    return active;
}

public void setActive(boolean active) {
    this.active = active;
}

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "tag_id")
public Tag getTag() {
    return tag;
}

public void setTag(Tag tag) {
    this.tag = tag;
}




    
}
