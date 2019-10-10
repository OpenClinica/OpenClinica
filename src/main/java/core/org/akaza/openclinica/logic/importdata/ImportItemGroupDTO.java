package core.org.akaza.openclinica.logic.importdata;

import java.util.ArrayList;


public class ImportItemGroupDTO {
	private ArrayList<String> itemOIDs;	
	private String itemGroupOID;
	
	public ImportItemGroupDTO() {
		super();
		itemOIDs = new ArrayList<String>();
	}
	
	
	public ArrayList<String> getItemOIDs() {
		return itemOIDs;
	}


	public void setItemOIDs(ArrayList<String> itemOIDs) {
		this.itemOIDs = itemOIDs;
	}


	public String getItemGroupOID() {
		return itemGroupOID;
	}
	public void setItemGroupOID(String itemGroupOID) {
		this.itemGroupOID = itemGroupOID;
	}
}
