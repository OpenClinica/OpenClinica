package org.akaza.openclinica.domain.datamap;

import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.domain.DataMapDomainObject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "archived_dataset_file_permission_tag")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "archived_dataset_file_permission_tag_id_seq") })
public class ArchivedDatasetFilePermissionTag extends DataMapDomainObject {


    private static final long serialVersionUID = 1L;
    private Integer id;
    private int archivedDatasetFileId;
    private String permissionTagId;
    private UserAccount userAccount;
    private Date dateCreated;
    private Date dateUpdated;
    private Integer updateId;

    public ArchivedDatasetFilePermissionTag() {
    }

    public ArchivedDatasetFilePermissionTag(int archivedDatasetFileId, String permissionTagId) {
        this.archivedDatasetFileId = archivedDatasetFileId;
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

    @Column(name = "archived_dataset_file_id")
    public int getArchivedDatasetFileId() {
        return archivedDatasetFileId;
    }

    public void setArchivedDatasetFileId(int archivedDatasetFileId) {
        this.archivedDatasetFileId = archivedDatasetFileId;
    }
}
