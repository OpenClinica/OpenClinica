package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ItemData;

public class ItemDataDao extends AbstractDomainDao<ItemData> {

    @Override
    Class<ItemData> domainClass() {
        // TODO Auto-generated method stub
        return ItemData.class;
    }

    public ItemData findByItemEventCrf(Integer itemId, Integer eventCrfId) {
        String query = "from " + getDomainClassName() + " item_data where item_data.item.itemId = :itemid and item_data.eventCrf.eventCrfId = :eventcrfid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("itemid", itemId);
        q.setInteger("eventcrfid", eventCrfId);
        return (ItemData) q.uniqueResult();
    }

}
