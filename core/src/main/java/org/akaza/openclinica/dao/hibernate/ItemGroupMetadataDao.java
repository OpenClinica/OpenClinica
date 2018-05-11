package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.ItemMetadata;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class ItemGroupMetadataDao extends AbstractDomainDao<ItemGroupMetadata> {

    static String findMetadataByItemCrfVersionQuery = "select new ItemMetadata(igm, ifm) from ItemGroupMetadata igm "
            + "join igm.item item on item.itemId = :itemid "
            + "join ItemFromMetadata ifm "
            + "join igm.crfVersion crfVersion on crfVersion.crfVersionId = :crfversionid";

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

    public ItemMetadata findMetadataByItemCrfVersion(int itemId, int crfVersionId) {
        Query q = getCurrentSession().createQuery(findMetadataByItemCrfVersionQuery);
        q.setParameter("itemid", itemId);
        q.setParameter("crfversionid", crfVersionId);
        return (ItemMetadata) q.uniqueResult();
    }
    static String findByItemCrfVersionQuery = "select igm from ItemGroupMetadata igm "
            + "join igm.item item on item.itemId = :itemid "
            + "join igm.crfVersion crfVersion on crfVersion.crfVersionId = :crfversionid";

    static String findAllByCrfVersionQuery = "select igm from ItemGroupMetadata igm "
            + "join igm.crfVersion crfVersion on crfVersion.crfVersionId = :crfversionid";


    public ItemGroupMetadata findByItemCrfVersion(int itemId, int crfVersionId) {
        Query q = getCurrentSession().createQuery(findByItemCrfVersionQuery);
        q.setParameter("itemid", itemId);
        q.setParameter("crfversionid", crfVersionId);
        return (ItemGroupMetadata) q.uniqueResult();
    }

    public List<ItemGroupMetadata> findAllByCrfVersion(int crfVersionId) {
        Query q = getCurrentSession().createQuery(findAllByCrfVersionQuery);
        q.setParameter("crfversionid", crfVersionId);
        return (List<ItemGroupMetadata>) q.list();
    }
}
