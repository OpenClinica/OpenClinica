package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.domain.datamap.ItemFormMetadata;

public class ItemFormMetadataDao extends AbstractDomainDao<ItemFormMetadata> {

    @Override
    Class<ItemFormMetadata> domainClass() {
        return ItemFormMetadata.class;
    }

    public ItemFormMetadata findByItemCrfVersion(Integer itemId, Integer crfVersionId) {
        String query = "SELECT distinct m.* " + " FROM item_form_metadata m" + " WHERE m.item_id= " + String.valueOf(itemId) + " AND m.crf_version_id= "
                + String.valueOf(crfVersionId);
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemFormMetadata.class);
        return (ItemFormMetadata) q.uniqueResult();

    }

    @SuppressWarnings("unchecked")
    public List<ItemFormMetadata> findAllByCrfVersion(int crf_version_id) {
        String query = "select distinct * from item_form_metadata ifm where ifm.crf_version_id = " + crf_version_id;
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemFormMetadata.class);
        return (List<ItemFormMetadata>) q.list();
    }

}
