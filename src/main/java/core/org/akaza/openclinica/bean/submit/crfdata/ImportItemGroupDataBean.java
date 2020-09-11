package core.org.akaza.openclinica.bean.submit.crfdata;

import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.ImportValidationService;
import org.akaza.openclinica.service.ImportValidationServiceImpl;
import org.akaza.openclinica.web.restful.errors.ErrorConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class ImportItemGroupDataBean {
    private ArrayList<ImportItemDataBean> itemData;
    private String itemGroupOID;
    private String itemGroupRepeatKey;
    private String itemGroupName;
    private String removed;

    public ImportItemGroupDataBean() {
        itemData = new ArrayList<ImportItemDataBean>();
    }

    public String getItemGroupRepeatKey() {
        return itemGroupRepeatKey;
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

    public String getItemGroupName() {
        return itemGroupName;
    }

    public void setItemGroupName(String itemGroupName) {
        this.itemGroupName = itemGroupName;
    }

    public Boolean isRemoved() {
        try {
            return ImportValidationServiceImpl.getStatusAttribute(this.removed);
        } catch (OpenClinicaSystemException ose){
            throw new OpenClinicaSystemException("Failed", ErrorConstants.ERR_ITEMGROUP_REMOVED_ATTRIBUTE_INVALID);
        }
    }

    public String getRemoved() {
        return removed;
    }

    public void setRemoved(String removed) {
        this.removed = removed;
    }
}
