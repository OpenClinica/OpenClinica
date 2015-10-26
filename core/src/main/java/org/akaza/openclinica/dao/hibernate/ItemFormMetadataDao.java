package org.akaza.openclinica.dao.hibernate;

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

}
