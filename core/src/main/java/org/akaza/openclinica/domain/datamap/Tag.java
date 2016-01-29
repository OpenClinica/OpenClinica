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
@Table(name = "tag")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "tag_tag_id_seq") })

public class Tag extends DataMapDomainObject{

private int tagId;
private String tag_name;


@Id
@Column(name = "tag_id", unique = true, nullable = false)
@GeneratedValue(generator = "id-generator")
public int getTagId() {
    return tagId;
}
public void setTagId(int tagId) {
    this.tagId = tagId;
}


@Column(name = "tag_name")
public String getTag_name() {
    return tag_name;
}
public void setTag_name(String tag_name) {
    this.tag_name = tag_name;
}



    
}
