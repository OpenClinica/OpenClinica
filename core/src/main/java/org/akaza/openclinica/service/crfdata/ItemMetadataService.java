package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;

public class ItemMetadataService implements MetadataServiceInterface {

    public boolean hide(Object metadataBean) {
        // TODO Auto-generated method stub
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        itemFormMetadataBean.setShowItem(false);
        
        return true;
    }

    public boolean isShown(Object metadataBean) {
        // do we check against the database, or just against the object? prob against the db
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        return itemFormMetadataBean.isShowItem();
        // return false;
    }

    public boolean show(Object metadataBean) {
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        itemFormMetadataBean.setShowItem(true);
        return false;
    }

}
