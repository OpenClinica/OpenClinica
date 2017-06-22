package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.hibernate.Query;

public class ItemFormMetadataDao extends AbstractDomainDao<ItemFormMetadata> {

    @Override
    Class<ItemFormMetadata> domainClass() {
        return ItemFormMetadata.class;
    }

    public ItemFormMetadata findByItemCrfVersion(Integer itemId, Integer crfVersionId) {
        String query = "SELECT distinct m.* " + " FROM item_form_metadata m" + " WHERE m.item_id= " + String.valueOf(itemId) + " AND m.crf_version_id= "
                + String.valueOf(crfVersionId);
        Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemFormMetadata.class);
        return (ItemFormMetadata) q.uniqueResult();

    }

    public static final String findAllByCrfVersionQuery = "select distinct * from item_form_metadata ifm where ifm.crf_version_id = :crfversionid";

    @SuppressWarnings("unchecked")
    public List<ItemFormMetadata> findAllByCrfVersion(int crf_version_id) {
        org.hibernate.Query q = getCurrentSession().createSQLQuery(findAllByCrfVersionQuery).addEntity(ItemFormMetadata.class);
        q.setInteger("crfversionid", crf_version_id);
        return (List<ItemFormMetadata>) q.list();
    }

}
