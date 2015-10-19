package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;

public class ItemDao extends AbstractDomainDao<Item> {

    @Override
    Class<Item> domainClass() {
        // TODO Auto-generated method stub
        return Item.class;
    }

    public Item findByOcOID(String OCOID) {
        String query = "from " + getDomainClassName() + " item  where item.ocOid = :ocoid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("ocoid", OCOID);
        return (Item) q.uniqueResult();
    }

    public Item findByNameCrfId(String name, Integer crfId) {
        String query = "select distinct i.* from item i, item_form_metadata ifm,crf_version cv " + "where i.name= '" + name + "' and i.item_id= ifm.item_id "
                + "and ifm.crf_version_id=cv.crf_version_id " + "and cv.crf_id=" + crfId;
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(Item.class);
        return ((Item) q.uniqueResult());
    }

    public int getItemDataTypeId(Item item) {
        String query = "select item_data_type_id from item where item_id = " + item.getItemId();
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query);
        return ((Number) q.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Item> findByItemGroupCrfVersionOrdered(Integer itemGroupId, Integer crfVersionId) {
        String query = "select distinct i.* from item i, item_group fg, item_group_metadata fgim " + " where fg.item_group_id= " + String.valueOf(itemGroupId)
                + " and fg.item_group_id=fgim.item_group_id and fgim.crf_version_id= " + String.valueOf(crfVersionId)
                + " and fgim.item_id=i.item_id order by i.item_id";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(Item.class);
        return (ArrayList<Item>) q.list();

    }
}