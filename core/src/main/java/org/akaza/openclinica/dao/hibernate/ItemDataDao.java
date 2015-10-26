package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ItemData;

public class ItemDataDao extends AbstractDomainDao<ItemData> {

    @Override
    Class<ItemData> domainClass() {
        // TODO Auto-generated method stub
        return ItemData.class;
    }

    public ItemData findByItemEventCrfOrdinal(Integer itemId, Integer eventCrfId, Integer ordinal) {
        String query = "from " + getDomainClassName()
                + " item_data where item_data.item.itemId = :itemid and item_data.eventCrf.eventCrfId = :eventcrfid and item_data.ordinal = :ordinal";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("itemid", itemId);
        q.setInteger("eventcrfid", eventCrfId);
        q.setInteger("ordinal", ordinal);
        return (ItemData) q.uniqueResult();
    }

    public int getMaxGroupRepeat(Integer eventCrfId, Integer itemId) {
        String query = "select max(ordinal) from item_data where event_crf_id = " + eventCrfId + " and item_id = " + itemId;
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query);
        return ((Number) q.uniqueResult()).intValue();
    }
}
