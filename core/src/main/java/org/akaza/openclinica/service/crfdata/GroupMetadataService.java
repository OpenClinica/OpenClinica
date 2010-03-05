package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.AbstractDomainDao;

public class GroupMetadataService implements MetadataServiceInterface {

    public boolean hide(Object metadataBean, EventCRFBean eventCrfBean, AbstractDomainDao metadataDao) {
        ItemGroupMetadataBean itemGroupMetadataBean = (ItemGroupMetadataBean) metadataBean;
        DynamicsItemGroupMetadataDao dynamicsMetadataDao = (DynamicsItemGroupMetadataDao) metadataDao;
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
