package org.akaza.openclinica.bean.submit.crfdata;

import java.util.ArrayList;
import java.util.Comparator;

public class ImportItemGroupDataBean {
    private ArrayList<ImportItemDataBean> itemData;
    private String itemGroupOID;
    private String itemGroupRepeatKey;
    private int itemRGkey;
    
    public ImportItemGroupDataBean() {
        itemData = new ArrayList<ImportItemDataBean>();
    }

    public String getItemGroupRepeatKey() {
        return itemGroupRepeatKey;
    }


	public int getItemRGkey() {
		return itemRGkey;
	}

	public void setItemRGkey(int itemRGkey) {
		this.itemRGkey = itemRGkey;
	}

	public void setItemGroupRepeatKey(String itemGroupRepeatKey) {
        this.itemGroupRepeatKey = itemGroupRepeatKey;
    }

    public String getItemGroupOID() {
        return itemGroupOID;
    }

    public void setItemGroupOID(String itemGroupOID) {
        this.itemGroupOID = itemGroupOID;
    }

    public ArrayList<ImportItemDataBean> getItemData() {
        return itemData;
    }

    public void setItemData(ArrayList<ImportItemDataBean> itemData) {
        this.itemData = itemData;
    }
    

       public static Comparator<ImportItemGroupDataBean> importItemGroupOID = new Comparator<ImportItemGroupDataBean>() {
       	public int compare(ImportItemGroupDataBean imp1, ImportItemGroupDataBean imp2) {
       	   String rollno1 = imp1.getItemGroupOID();
       	   String rollno2 = imp2.getItemGroupOID();
        	  int rollno3 = imp1.getItemRGkey();
  	          int rollno4 = imp2.getItemRGkey();
  	   
         	int result= rollno1.compareTo(rollno2);
    	    return ((result == 0) ? rollno3-rollno4 : result);

       	   /*For ascending order*/
//       	   return rollno1.compareTo(rollno2); 
          }};

          
}
