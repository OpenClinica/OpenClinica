// default package
// Generated Jul 31, 2013 2:03:33 PM by Hibernate Tools 3.4.0.CR1
package core.org.akaza.openclinica.domain.datamap;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import core.org.akaza.openclinica.domain.DataMapDomainObject;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.user.UserAccount;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * Item generated by hbm2java
 */
@Entity
@Table(name = "item", uniqueConstraints = @UniqueConstraint(columnNames = "oc_oid"))
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence_name", value = "item_item_id_seq") })
public class Item  extends DataMapDomainObject{

	private int itemId;
	private UserAccount userAccount;
	private ItemReferenceType itemReferenceType;
	private Status status;
	private ItemDataType itemDataType;
	private String name;
	private String description;
	private String units;
	private Boolean phiStatus;
	private Date dateCreated;
	private Date dateUpdated;
	private Integer updateId;
	private String ocOid;
	private Set<ItemFormMetadata> itemFormMetadatas;
	private List<ItemData> itemDatas;
	//private Set dcSummaryItemMaps = new HashSet(0);
	private List<VersioningMap> versioningMaps ;
	//private Set dcSubstitutionEvents;
	private List<ItemGroupMetadata> itemGroupMetadatas ;
	//private Set dcPrimitivesForItemId ;
	//private Set dcPrimitivesForDynamicValueItemId ;

	public Item() {
	}

	public Item(int itemId, String ocOid) {
		this.itemId = itemId;
		this.ocOid = ocOid;
	}

	public Item(int itemId, UserAccount userAccount,
			ItemReferenceType itemReferenceType, Status status,
			ItemDataType itemDataType, String name, String description,
			String units, Boolean phiStatus, Date dateCreated,
			Date dateUpdated, Integer updateId, String ocOid,
			Set<ItemFormMetadata> itemFormMetadatas, List<ItemData>  itemDatas, /*Set dcSummaryItemMaps,*/
//			List<VersioningMap>  versioningMaps, Set dcSubstitutionEvents,
			 List<ItemGroupMetadata> itemGroupMetadatas/*, Set dcPrimitivesForItemId,
			Set dcPrimitivesForDynamicValueItemId*/) {
		this.itemId = itemId;
		this.userAccount = userAccount;
		this.itemReferenceType = itemReferenceType;
		this.status = status;
		this.itemDataType = itemDataType;
		this.name = name;
		this.description = description;
		this.units = units;
		this.phiStatus = phiStatus;
		this.dateCreated = dateCreated;
		this.dateUpdated = dateUpdated;
		this.updateId = updateId;
		this.ocOid = ocOid;
		this.itemFormMetadatas = itemFormMetadatas;
		this.itemDatas = itemDatas;
		//this.dcSummaryItemMaps = dcSummaryItemMaps;
		this.versioningMaps = versioningMaps;
		//this.dcSubstitutionEvents = dcSubstitutionEvents;
		this.itemGroupMetadatas = itemGroupMetadatas;
		//this.dcPrimitivesForItemId = dcPrimitivesForItemId;
		//this.dcPrimitivesForDynamicValueItemId = dcPrimitivesForDynamicValueItemId;
	}

	@Id
	@Column(name = "item_id", unique = true, nullable = false)
	@GeneratedValue(generator = "id-generator")

	public int getItemId() {
		return this.itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	public UserAccount getUserAccount() {
		return this.userAccount;
	}

	public void setUserAccount(UserAccount userAccount) {
		this.userAccount = userAccount;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_reference_type_id")
	public ItemReferenceType getItemReferenceType() {
		return this.itemReferenceType;
	}

	public void setItemReferenceType(ItemReferenceType itemReferenceType) {
		this.itemReferenceType = itemReferenceType;
	}

	@Type(type = "status")
    @Column(name = "status_id")
    public Status getStatus() {
        if (status != null) {
            return status;
        } else
            return Status.AVAILABLE;
    }

	public void setStatus(Status status) {
		this.status = status;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_data_type_id")
	public ItemDataType getItemDataType() {
		return this.itemDataType;
	}

	public void setItemDataType(ItemDataType itemDataType) {
		this.itemDataType = itemDataType;
	}

	@Column(name = "name")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "description", length = 4000)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "units", length = 64)
	public String getUnits() {
		return this.units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	@Column(name = "phi_status")
	public Boolean getPhiStatus() {
		return this.phiStatus;
	}

	public void setPhiStatus(Boolean phiStatus) {
		this.phiStatus = phiStatus;
	}

    @Temporal(TemporalType.DATE)
    @Column(name = "date_created", length = 4, updatable = false)
    public Date getDateCreated() {
        if (dateCreated != null) {
            return dateCreated;
        } else
            return new Date();
    }

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Temporal(TemporalType.DATE)
	@Column(name = "date_updated", length = 4)
	public Date getDateUpdated() {
		return this.dateUpdated;
	}

	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	@Column(name = "update_id")
	public Integer getUpdateId() {
		return this.updateId;
	}

	public void setUpdateId(Integer updateId) {
		this.updateId = updateId;
	}

	@Column(name = "oc_oid", unique = true, nullable = false, length = 40)
	public String getOcOid() {
		return this.ocOid;
	}

	public void setOcOid(String ocOid) {
		this.ocOid = ocOid;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item")
	public Set<ItemFormMetadata>  getItemFormMetadatas() {
		return this.itemFormMetadatas;
	}

	public void setItemFormMetadatas(Set<ItemFormMetadata>  itemFormMetadatas) {
		this.itemFormMetadatas = itemFormMetadatas;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item")
	public List<ItemData> getItemDatas() {
		return this.itemDatas;
	}

	public void setItemDatas(List<ItemData>  itemDatas) {
		this.itemDatas = itemDatas;
	}

//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item")
//	public Set getDcSummaryItemMaps() {
//		return this.dcSummaryItemMaps;
//	}
//
//	public void setDcSummaryItemMaps(Set dcSummaryItemMaps) {
//		this.dcSummaryItemMaps = dcSummaryItemMaps;
//	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item")
	public List<VersioningMap>  getVersioningMaps() {
		return this.versioningMaps;
	}

	public void setVersioningMaps(List<VersioningMap>  versioningMaps) {
		this.versioningMaps = versioningMaps;
	}

/*	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item")
	public Set getDcSubstitutionEvents() {
		return this.dcSubstitutionEvents;
	}

	public void setDcSubstitutionEvents(Set dcSubstitutionEvents) {
		this.dcSubstitutionEvents = dcSubstitutionEvents;
	}
*/
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item")
	public  List<ItemGroupMetadata> getItemGroupMetadatas() {
		return this.itemGroupMetadatas;
	}

public void setItemGroupMetadatas( List<ItemGroupMetadata> itemGroupMetadatas) {
		this.itemGroupMetadatas = itemGroupMetadatas;
	}

/*		//@OneToMany(fetch = FetchType.LAZY, mappedBy = "itemByItemId")
	public Set getDcPrimitivesForItemId() {
		return this.dcPrimitivesForItemId;
	}*/

//	public void setDcPrimitivesForItemId(Set dcPrimitivesForItemId) {
//		this.dcPrimitivesForItemId = dcPrimitivesForItemId;
//	}
//
//	@OneToMany(fetch = FetchType.LAZY, mappedBy = "itemByDynamicValueItemId")
//	public Set getDcPrimitivesForDynamicValueItemId() {
//		return this.dcPrimitivesForDynamicValueItemId;
//	}

/*	public void setDcPrimitivesForDynamicValueItemId(
			Set dcPrimitivesForDynamicValueItemId) {
		this.dcPrimitivesForDynamicValueItemId = dcPrimitivesForDynamicValueItemId;
	}*/



}
