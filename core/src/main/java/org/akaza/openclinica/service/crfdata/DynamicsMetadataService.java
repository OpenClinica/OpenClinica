package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;

import java.util.List;

import javax.sql.DataSource;

public class DynamicsMetadataService implements MetadataServiceInterface {

    // protected final java.util.logging.Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String ESCAPED_SEPERATOR = "\\.";
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    DataSource ds;
    private EventCRFDAO eventCRFDAO;
    private ItemDataDAO itemDataDAO;
    private ItemDAO itemDAO;
    private ItemGroupDAO itemGroupDAO;
    private ItemFormMetadataDAO itemFormMetadataDAO;
    private ItemGroupMetadataDAO itemGroupMetadataDAO;

    public DynamicsMetadataService(DataSource ds) {
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
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        DynamicsItemFormMetadataBean dynamicsMetadataBean = getDynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean, null);
        if (dynamicsMetadataBean != null) {
            return dynamicsMetadataBean.isShowItem();
        } else {
            System.out.println("did not find a row in the db for " + itemFormMetadataBean.getId());
            return false;
        }
        // return false;
    }

    public boolean isShown(Integer itemId, EventCRFBean eventCrfBean) {
        // do we check against the database, or just against the object? prob against the db
        // ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        // return itemFormMetadataBean.isShowItem();
        ItemFormMetadataBean itemFormMetadataBean = getItemFormMetadataDAO().findByItemIdAndCRFVersionId(itemId, eventCrfBean.getCRFVersionId());
        DynamicsItemFormMetadataBean dynamicsMetadataBean = getDynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean, null);
        // DynamicsItemFormMetadataBean dynamicsMetadataBean = getDynamicsItemFormMetadataDao().findByMetadataBean(itemFormMetadataBean, eventCrfBean);
        if (dynamicsMetadataBean != null) {
            return dynamicsMetadataBean.isShowItem();
        } else {
            System.out.println("did not find a row in the db for " + itemFormMetadataBean.getId());
            return false;
        }
        // return false;
    }

    /**
     * 
     * TODO: remove the @deprecated call. The reason it is there now is to accommodate the call being made from the DataEntryServlet
     * 
     * @param metadataBean
     * @param eventCrfBean
     * @param itemDataBean
     * @return DynamicsItemFormMetadataBean
     */
    private DynamicsItemFormMetadataBean getDynamicsItemFormMetadataBean(ItemFormMetadataBean metadataBean, EventCRFBean eventCrfBean, ItemDataBean itemDataBean) {
        ItemFormMetadataBean itemFormMetadataBean = metadataBean;
        DynamicsItemFormMetadataBean dynamicsMetadataBean = null;
        if (itemDataBean == null) {
            dynamicsMetadataBean = getDynamicsItemFormMetadataDao().findByMetadataBean(itemFormMetadataBean, eventCrfBean);
        } else {
            dynamicsMetadataBean = getDynamicsItemFormMetadataDao().findByMetadataBean(itemFormMetadataBean, eventCrfBean, itemDataBean);
        }

        return dynamicsMetadataBean;

    }

    public boolean showItem(ItemFormMetadataBean metadataBean, EventCRFBean eventCrfBean, ItemDataBean itemDataBean) {
        ItemFormMetadataBean itemFormMetadataBean = metadataBean;
        itemFormMetadataBean.setShowItem(true);
        DynamicsItemFormMetadataBean dynamicsMetadataBean = new DynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean);
        dynamicsMetadataBean.setItemDataId(itemDataBean.getId());
        dynamicsMetadataBean.setShowItem(true);
        getDynamicsItemFormMetadataDao().saveOrUpdate(dynamicsMetadataBean);
        return true;
    }

    public boolean hideItem(ItemFormMetadataBean metadataBean, EventCRFBean eventCrfBean, ItemDataBean itemDataBean) {
        ItemFormMetadataBean itemFormMetadataBean = metadataBean;
        DynamicsItemFormMetadataBean dynamicsMetadataBean = new DynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean);
        dynamicsMetadataBean.setItemDataId(itemDataBean.getId());
        dynamicsMetadataBean.setShowItem(false);
        getDynamicsItemFormMetadataDao().saveOrUpdate(dynamicsMetadataBean);
        return true;
    }

    public boolean showGroup(ItemGroupMetadataBean metadataBean, EventCRFBean eventCrfBean) {
        ItemGroupMetadataBean itemGroupMetadataBean = metadataBean;
        return true;
    }

    public void show(Integer itemDataId, String[] oids) {
        ItemDataBean itemDataBean = (ItemDataBean) getItemDataDAO().findByPK(itemDataId);
        EventCRFBean eventCrfBean = (EventCRFBean) getEventCRFDAO().findByPK(itemDataBean.getEventCRFId());
        for (String oid : oids) {
            ItemOrItemGroupHolder itemOrItemGroup = getItemOrItemGroup(oid);
            // OID is an item
            if (itemOrItemGroup.getItemBean() != null) {
                ItemDataBean oidBasedItemData = getItemData(itemOrItemGroup.getItemBean(), eventCrfBean, itemDataBean.getOrdinal());
                ItemFormMetadataBean itemFormMetadataBean =
                    getItemFormMetadataDAO().findByItemIdAndCRFVersionId(itemOrItemGroup.getItemBean().getId(), eventCrfBean.getCRFVersionId());
                DynamicsItemFormMetadataBean dynamicsMetadataBean = getDynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean, oidBasedItemData);
                if (dynamicsMetadataBean == null) {
                    showItem(itemFormMetadataBean, eventCrfBean, oidBasedItemData);
                } else if (dynamicsMetadataBean != null && !dynamicsMetadataBean.isShowItem()) {
                    dynamicsMetadataBean.setShowItem(true);
                    getDynamicsItemFormMetadataDao().saveOrUpdate(dynamicsMetadataBean);
                }
            }
            // OID is a group
            else {

            }
        }
    }

    public void hide(Integer itemDataId, String[] oids) {
        ItemDataBean itemDataBean = (ItemDataBean) getItemDataDAO().findByPK(itemDataId);
        EventCRFBean eventCrfBean = (EventCRFBean) getEventCRFDAO().findByPK(itemDataBean.getEventCRFId());
        for (String oid : oids) {
            ItemOrItemGroupHolder itemOrItemGroup = getItemOrItemGroup(oid);
            // OID is an item
            if (itemOrItemGroup.getItemBean() != null) {
                ItemDataBean oidBasedItemData = getItemData(itemOrItemGroup.getItemBean(), eventCrfBean, itemDataBean.getOrdinal());
                ItemFormMetadataBean itemFormMetadataBean =
                    getItemFormMetadataDAO().findByItemIdAndCRFVersionId(itemOrItemGroup.getItemBean().getId(), eventCrfBean.getCRFVersionId());
                DynamicsItemFormMetadataBean dynamicsMetadataBean = getDynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean, oidBasedItemData);
                if (dynamicsMetadataBean == null && oidBasedItemData.getValue().equals("")) {
                    showItem(itemFormMetadataBean, eventCrfBean, oidBasedItemData);
                } else if (dynamicsMetadataBean != null && dynamicsMetadataBean.isShowItem() && oidBasedItemData.getValue().equals("")) {
                    dynamicsMetadataBean.setShowItem(false);
                    getDynamicsItemFormMetadataDao().saveOrUpdate(dynamicsMetadataBean);
                }
            }
            // OID is a group
            else {

            }
        }
    }

    public void insert(Integer itemDataId, List<PropertyBean> properties) {
        ItemDataBean itemDataBean = (ItemDataBean) getItemDataDAO().findByPK(itemDataId);
        EventCRFBean eventCrfBean = (EventCRFBean) getEventCRFDAO().findByPK(itemDataBean.getEventCRFId());
        for (PropertyBean propertyBean : properties) {

            ItemBean itemBean = getItemDAO().findByOid(propertyBean.getOid()).get(0);
            ItemDataBean oidBasedItemData = getItemData(itemBean, eventCrfBean, itemDataBean.getOrdinal());
            oidBasedItemData.setValue(propertyBean.getValue());
            getItemDataDAO().updateValue(oidBasedItemData, "yyyy-MM-dd");
        }
    }

    private ItemDataBean getItemData(ItemBean itemBean, EventCRFBean eventCrfBean, Integer ordinal) {
        return getItemDataDAO().findByItemIdAndEventCRFIdAndOrdinal(itemBean.getId(), eventCrfBean.getId(), ordinal);

    }

    private ItemOrItemGroupHolder getItemOrItemGroup(String oid) {

        String[] theOid = oid.split(ESCAPED_SEPERATOR);
        if (theOid.length == 2) {
            ItemGroupBean itemGroup = getItemGroupDAO().findByOid(theOid[0]);
            if (itemGroup != null) {
                ItemBean item = getItemDAO().findItemByGroupIdandItemOid(itemGroup.getId(), theOid[1]);
                if (item != null) {
                    return new ItemOrItemGroupHolder(item, itemGroup);
                }
            }
        }
        if (theOid.length == 1) {
            ItemGroupBean itemGroup = getItemGroupDAO().findByOid(oid);
            if (itemGroup != null) {
                return new ItemOrItemGroupHolder(null, itemGroup);
            }

            List<ItemBean> items = getItemDAO().findByOid(oid);
            ItemBean item = items.size() > 0 ? items.get(0) : null;
            if (item != null) {
                return new ItemOrItemGroupHolder(item, null);
            }
        }

        return null;
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

    private ItemGroupDAO getItemGroupDAO() {
        itemGroupDAO = this.itemGroupDAO != null ? itemGroupDAO : new ItemGroupDAO(ds);
        return itemGroupDAO;
    }

    private ItemFormMetadataDAO getItemFormMetadataDAO() {
        itemFormMetadataDAO = this.itemFormMetadataDAO != null ? itemFormMetadataDAO : new ItemFormMetadataDAO(ds);
        return itemFormMetadataDAO;
    }

    private ItemGroupMetadataDAO getItemGroupMetadataDAO() {
        itemGroupMetadataDAO = this.itemGroupMetadataDAO != null ? itemGroupMetadataDAO : new ItemGroupMetadataDAO(ds);
        return itemGroupMetadataDAO;
    }

    class ItemOrItemGroupHolder {

        ItemBean itemBean;
        ItemGroupBean itemGroupBean;

        public ItemOrItemGroupHolder(ItemBean itemBean, ItemGroupBean itemGroupBean) {
            this.itemBean = itemBean;
            this.itemGroupBean = itemGroupBean;
        }

        public ItemBean getItemBean() {
            return itemBean;
        }

        public void setItemBean(ItemBean itemBean) {
            this.itemBean = itemBean;
        }

        public ItemGroupBean getItemGroupBean() {
            return itemGroupBean;
        }

        public void setItemGroupBean(ItemGroupBean itemGroupBean) {
            this.itemGroupBean = itemGroupBean;
        }

    }

}
