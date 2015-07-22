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

}
