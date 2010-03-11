package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class ItemMetadataService implements MetadataServiceInterface {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    DataSource ds;
    private EventCRFDAO eventCRFDAO;
    private ItemDataDAO itemDataDAO;
    private ItemDAO itemDAO;
    private ItemFormMetadataDAO itemFormMetadataDAO;

    public ItemMetadataService(DataSource ds) {
        this.ds = ds;
    }

    public boolean hide(Object metadataBean, EventCRFBean eventCrfBean) {
        // TODO -- interesting problem, where is the SpringServletAccess object going to live now? tbh 03/2010
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        itemFormMetadataBean.setShowItem(false);
        // DynamicsItemFormMetadataDao dynamicsMetadataDao = (DynamicsItemFormMetadataDao) metadataDao;
        // DynamicsItemFormMetadataDao metadataDao = (DynamicsItemFormMetadataDao) SpringServletAccess.getApplicationContext(context).getBean("dynamicsItemFormMetadataDao");
        DynamicsItemFormMetadataBean dynamicsMetadataBean = new DynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean);
        dynamicsMetadataBean.setShowItem(false);
        getDynamicsItemFormMetadataDao().saveOrUpdate(dynamicsMetadataBean);
        return true;
    }

    public boolean isShown(Object metadataBean, EventCRFBean eventCrfBean) {
        // do we check against the database, or just against the object? prob against the db
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        // return itemFormMetadataBean.isShowItem();
        DynamicsItemFormMetadataBean dynamicsMetadataBean = getDynamicsItemFormMetadataDao().findByMetadataBean(itemFormMetadataBean, eventCrfBean);
        return dynamicsMetadataBean.isShowItem();
        // return false;
    }

    public boolean show(Object metadataBean, EventCRFBean eventCrfBean) {
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        itemFormMetadataBean.setShowItem(true);
        DynamicsItemFormMetadataBean dynamicsMetadataBean = new DynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean);
        dynamicsMetadataBean.setShowItem(true);
        getDynamicsItemFormMetadataDao().saveOrUpdate(dynamicsMetadataBean);
        return true;
    }

    public void show(Integer itemDataId, String[] oids) {
        ItemDataBean itemDataBean = (ItemDataBean) getItemDataDAO().findByPK(itemDataId);
        EventCRFBean eventCrfBean = (EventCRFBean) getEventCRFDAO().findByPK(itemDataBean.getEventCRFId());
        for (String oid : oids) {
            ItemBean itemBean = getItemDAO().findByOid(oid).get(0);
            ItemFormMetadataBean itemFormMetadataBean = getItemFormMetadataDAO().findByItemIdAndCRFVersionId(itemBean.getId(), eventCrfBean.getCRFVersionId());
            show(itemFormMetadataBean, eventCrfBean);
        }
    }

    public DynamicsItemFormMetadataDao getDynamicsItemFormMetadataDao() {
        return dynamicsItemFormMetadataDao;
    }

    public void setDynamicsItemFormMetadataDao(DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao) {
        this.dynamicsItemFormMetadataDao = dynamicsItemFormMetadataDao;
    }

    private EventCRFDAO getEventCRFDAO() {
        eventCRFDAO = this.eventCRFDAO != null ? eventCRFDAO : new EventCRFDAO(ds);
        return eventCRFDAO;
    }

    private ItemDataDAO getItemDataDAO() {
        itemDataDAO = this.itemDataDAO != null ? itemDataDAO : new ItemDataDAO(ds);
        return itemDataDAO;
    }

    private ItemDAO getItemDAO() {
        itemDAO = this.itemDAO != null ? itemDAO : new ItemDAO(ds);
        return itemDAO;
    }

    private ItemFormMetadataDAO getItemFormMetadataDAO() {
        itemFormMetadataDAO = this.itemFormMetadataDAO != null ? itemFormMetadataDAO : new ItemFormMetadataDAO(ds);
        return itemFormMetadataDAO;
    }

}
