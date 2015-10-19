package org.akaza.openclinica.dao.hibernate;

import java.util.ArrayList;

import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;

public class ItemGroupMetadataDao extends AbstractDomainDao<ItemGroupMetadata> {

    @Override
    Class<ItemGroupMetadata> domainClass() {
        // TODO Auto-generated method stub
        return ItemGroupMetadata.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ItemGroupMetadata> findByItemGroupCrfVersion(Integer itemGroupId, Integer crfVersionId) {
        String query = "select distinct igm.* from item_group_metadata igm, item_group ig where igm.crf_version_id = " + String.valueOf(crfVersionId)
                + " and ig.item_group_id = igm.item_group_id and ig.item_group_id = " + String.valueOf(itemGroupId) + " order by igm.ordinal asc";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemGroupMetadata.class);
        return (ArrayList<ItemGroupMetadata>) q.list();
    }

}
