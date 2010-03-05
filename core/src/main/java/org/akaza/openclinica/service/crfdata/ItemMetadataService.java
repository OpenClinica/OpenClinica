package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
// import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.AbstractDomainDao;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;

public class ItemMetadataService implements MetadataServiceInterface {

    public boolean hide(Object metadataBean, EventCRFBean eventCrfBean, AbstractDomainDao metadataDao) {
        // TODO -- interesting problem, where is the SpringServletAccess object going to live now? tbh 03/2010
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        itemFormMetadataBean.setShowItem(false);
        DynamicsItemFormMetadataDao dynamicsMetadataDao = (DynamicsItemFormMetadataDao) metadataDao;
        // DynamicsItemFormMetadataDao metadataDao = (DynamicsItemFormMetadataDao) SpringServletAccess.getApplicationContext(context).getBean("dynamicsItemFormMetadataDao");
        DynamicsItemFormMetadataBean dynamicsMetadataBean = new DynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean);
        dynamicsMetadataBean.setShowItem(false);
        dynamicsMetadataDao.saveOrUpdate(dynamicsMetadataBean);
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
