package org.akaza.openclinica.core.util;

import java.util.ArrayList;
import java.util.List;

public class ItemGroupCrvVersionUtil {

	private String itemName;
	
	private String groupName;
	private String groupOID;
	private String crfVersionName;
	private int crfVersionStatus;
	//for display in veiwCRFView page
	private String itemOID;
	private String itemDescription;
	private String itemDataType;
	private String versions;
	private String errorMesages;
	private List arrErrorMesages;
	
	private int    id;
	
	
	public ItemGroupCrvVersionUtil(){arrErrorMesages = new ArrayList<String>();}
	public ItemGroupCrvVersionUtil(String itemName,String groupName,String groupOID , 
			String crfVersionName,  int crfVersionStatus){
		this.itemName= itemName;
		this.groupName =  groupName;
		this.groupOID= groupOID;
		this.crfVersionName=  crfVersionName;
		this.crfVersionStatus= crfVersionStatus;
		arrErrorMesages = new ArrayList<String>();
	}
	public ItemGroupCrvVersionUtil(String itemName,String groupName,String groupOID , 
			String crfVersionName,  int crfVersionStatus,  
			String itemOID, String itemDescription,String itemDataType	, int    id){
		this(itemName, groupName, groupOID ,  crfVersionName,   crfVersionStatus);
		
		this.itemOID = itemOID;
		this.itemDescription= itemDescription;
		this.itemDataType =  itemDataType;
		this.id =    id;
	}
				
	/**
	 * @return the itemName
	 */
	public String getItemName() {
		return itemName;
	}
	/**
	 * @param itemName the itemName to set
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
	}
	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	/**
	 * @return the groupOID
	 */
	public String getGroupOID() {
		return groupOID;
	}
	/**
	 * @param groupOID the groupOID to set
	 */
	public void setGroupOID(String groupOID) {
		this.groupOID = groupOID;
	}
	/**
	 * @return the crfVersion
	 */
	public String getCrfVersionName() {
		return crfVersionName;
	}
	/**
	 * @param crfVersion the crfVersion to set
	 */
	public void setCrfVersionName(String crfVersionName) {
		this.crfVersionName = crfVersionName;
	}
	/**
	 * @return the crfVersionStatus
	 */
	public int getCrfVersionStatus() {
		return crfVersionStatus;
	}
	/**
	 * @param crfVersionStatus the crfVersionStatus to set
	 */
	public void setCrfVersionStatus(int crfVersionStatus) {
		this.crfVersionStatus = crfVersionStatus;
	}
	/**
	 * @return the itemOID
	 */
	public String getItemOID() {
		return itemOID;
	}
	/**
	 * @param itemOID the itemOID to set
	 */
	public void setItemOID(String itemOID) {
		this.itemOID = itemOID;
	}
	/**
	 * @return the itemDescription
	 */
	public String getItemDescription() {
		return itemDescription;
	}
	/**
	 * @param itemDescription the itemDescription to set
	 */
	public void setItemDescription(String itemDescription) {
		this.itemDescription = itemDescription;
	}
	/**
	 * @return the itemDataType
	 */
	public String getItemDataType() {
		return itemDataType;
	}
	/**
	 * @param itemDataType the itemDataType to set
	 */
	public void setItemDataType(String itemDataType) {
		this.itemDataType = itemDataType;
	}
	/**
	 * @return the i_Versions
	 */
	public String getVersions() {
		return versions;
	}
	/**
	 * @param i_Versions the i_Versions to set
	 */
	public void setVersions(String i_Versions) {
		this.versions = i_Versions;
	}
	/**
	 * @return the i_errorMesages
	 */
	public String getErrorMesages() {
		return errorMesages;
	}
	/**
	 * @param i_errorMesages the i_errorMesages to set
	 */
	public void setErrorMesages(String i_errorMesages) {
		this.errorMesages = i_errorMesages;
	}
	/**
	 * @return the i_id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param i_id the i_id to set
	 */
	public void setId(int i_id) {
		this.id = i_id;
	}
	public List getArrErrorMesages() {
		return arrErrorMesages;
	}
	public void setArrErrorMesages(List arrErrorMesages) {
		this.arrErrorMesages = arrErrorMesages;
	}
}
