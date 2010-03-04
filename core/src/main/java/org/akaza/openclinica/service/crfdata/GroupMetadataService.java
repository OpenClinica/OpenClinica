package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;

public class GroupMetadataService implements MetadataServiceInterface {

    public boolean hide(Object metadataBean) {
        ItemGroupMetadataBean itemGroupMetadataBean = (ItemGroupMetadataBean) metadataBean;
        itemGroupMetadataBean.setShowGroup(false);
        
        return false;
    }

    public boolean isShown(Object metadataBean) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean show(Object metadataBean) {
        // TODO Auto-generated method stub
        return false;
    }

}
