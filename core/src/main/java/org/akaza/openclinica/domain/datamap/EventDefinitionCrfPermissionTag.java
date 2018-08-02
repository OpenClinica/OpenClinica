package org.akaza.openclinica.domain.datamap;

import org.akaza.openclinica.domain.DataMapDomainObject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "event_definition_crf_permission_tag")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "event_definition_crf_permission_tag_id_seq") })
public class EventDefinitionCrfPermissionTag extends DataMapDomainObject {


    private static final long serialVersionUID = 1L;
    private Integer id;
    private EventDefinitionCrf eventDefinitionCrf;
    private String permissionTagId;
    private UserAccount userAccount;
    private Date dateCreated;
    private Date dateUpdated;
    private Integer updateId;

    public EventDefinitionCrfPermissionTag() {
    }

    public EventDefinitionCrfPermissionTag(EventDefinitionCrf eventDefinitionCrf, String permissionTagId) {
        this.eventDefinitionCrf = eventDefinitionCrf;
        this.permissionTagId = permissionTagId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    @Column(name = "date_created", length = 4)
    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Column(name = "date_updated", length = 4)
    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Column(name = "update_id")
    public Integer getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }



    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(generator = "id-generator")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }



    @Column(name = "permission_tag_id")
    public String getPermissionTagId() {
        return permissionTagId;
    }

    public void setPermissionTagId(String permissionTagId) {
        this.permissionTagId = permissionTagId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_definition_crf_id")
    public EventDefinitionCrf getEventDefinitionCrf() {
        return eventDefinitionCrf;
    }

    public void setEventDefinitionCrf(EventDefinitionCrf eventDefinitionCrf) {
        this.eventDefinitionCrf = eventDefinitionCrf;
    }
}
