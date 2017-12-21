package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.ItemData;

public class ItemDataDao extends AbstractDomainDao<ItemData> {

    Class<ItemData> domainClass() {
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

    public List<ItemData> findAllByEventCrf(Integer eventCrfId) {
        String query = "select * from item_data where event_crf_id = " + eventCrfId;
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemData.class);
        
        return (List<ItemData>) q.list();
      
    }

    public List<ItemData> findByEventCrfGroup(Integer eventCrfId, Integer itemGroupId) {
        String query = "select id.* " + 
            "from item_data id " + 
            "join item i on id.item_id = i.item_id " + 
            "join event_crf ec on id.event_crf_id=ec.event_crf_id " + 
            "join item_group_metadata igm on i.item_id=igm.item_id and igm.crf_version_id = ec.crf_version_id " + 
            "where id.event_crf_id = " + eventCrfId + " and igm.item_group_id = " + itemGroupId + " " + 
            "order by id.ordinal, igm.ordinal";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemData.class);
        
        return (List<ItemData>) q.list();
      
    }
    
    public List<ItemData> findByEventCrfId(Integer eventCrfId) {
        String query = "from " + getDomainClassName() + " item_data where item_data.eventCrf.eventCrfId = :eventcrfid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("eventcrfid", eventCrfId);
        return (List<ItemData>) q.list();
      
    }
    
    public int getMaxGroupRepeat(Integer eventCrfId, Integer itemId) {
        getCurrentSession().flush();
        String query = "select max(ordinal) from item_data where event_crf_id = " + eventCrfId + " and item_id = " + itemId;
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query);
        Number result = (Number) q.uniqueResult();
        if (result == null) return 0;
        else return result.intValue();
    }
}
