package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.Item;

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
}
