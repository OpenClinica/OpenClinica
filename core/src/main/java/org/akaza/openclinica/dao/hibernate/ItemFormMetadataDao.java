package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ItemFormMetadata;

import java.util.List;

public class ItemFormMetadataDao extends AbstractDomainDao<ItemFormMetadata> {

    @Override
    Class<ItemFormMetadata> domainClass() {
        return ItemFormMetadata.class;
    }

    public ItemFormMetadata findByItemCrfVersion(Integer itemId, Integer version) {
        String query = "SELECT distinct m.* " + " FROM item_form_metadata m" + " WHERE m.item_id= " + String.valueOf(itemId) + " AND m.crf_version_id= "
                + String.valueOf(version);
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemFormMetadata.class);
        return (ItemFormMetadata) q.uniqueResult();

    }

    public List<ItemFormMetadata> findAllByCrfVersion(Integer version) {
        String query = "SELECT distinct m.* " + " FROM item_form_metadata m" + " WHERE  m.crf_version_id= :version ";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(ItemFormMetadata.class);
        q.setInteger("version", version);
        return ((List<ItemFormMetadata>) q.list());
    }
}
