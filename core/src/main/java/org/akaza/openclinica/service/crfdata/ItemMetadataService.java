package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
// import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.AbstractDomainDao;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
// import org.akaza.openclinica.service.rule.Logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemMetadataService implements MetadataServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private DynamicsItemFormMetadataDao dynamicsMetadataDao;
    
    public boolean hide(Object metadataBean, EventCRFBean eventCrfBean) {
        // TODO -- interesting problem, where is the SpringServletAccess object going to live now? tbh 03/2010
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        itemFormMetadataBean.setShowItem(false);
        // DynamicsItemFormMetadataDao dynamicsMetadataDao = (DynamicsItemFormMetadataDao) metadataDao;
        // DynamicsItemFormMetadataDao metadataDao = (DynamicsItemFormMetadataDao) SpringServletAccess.getApplicationContext(context).getBean("dynamicsItemFormMetadataDao");
        DynamicsItemFormMetadataBean dynamicsMetadataBean = new DynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean);
        dynamicsMetadataBean.setShowItem(false);
        getDynamicsMetadataDao().saveOrUpdate(dynamicsMetadataBean);
        return true;
    }

    public boolean isShown(Object metadataBean, EventCRFBean eventCrfBean) {
        // do we check against the database, or just against the object? prob against the db
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        // return itemFormMetadataBean.isShowItem();
        DynamicsItemFormMetadataBean dynamicsMetadataBean = getDynamicsMetadataDao().findByMetadataBean(itemFormMetadataBean, eventCrfBean);
        return dynamicsMetadataBean.isShowItem();
        // return false;
    }

    public boolean show(Object metadataBean, EventCRFBean eventCrfBean) {
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        itemFormMetadataBean.setShowItem(true);
        DynamicsItemFormMetadataBean dynamicsMetadataBean = new DynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean);
        dynamicsMetadataBean.setShowItem(true);
        getDynamicsMetadataDao().saveOrUpdate(dynamicsMetadataBean);
        return true;
    }

    public DynamicsItemFormMetadataDao getDynamicsMetadataDao() {
        return dynamicsMetadataDao;
    }

    public void setDynamicsMetadataDao(DynamicsItemFormMetadataDao dynamicsMetadataDao) {
        this.dynamicsMetadataDao = dynamicsMetadataDao;
    }

}
