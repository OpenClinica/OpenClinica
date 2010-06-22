package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;

public class DynamicsItemFormMetadataDao extends AbstractDomainDao<DynamicsItemFormMetadataBean> {

    @Override
    public Class<DynamicsItemFormMetadataBean> domainClass() {
        return DynamicsItemFormMetadataBean.class;
    }

    public DynamicsItemFormMetadataBean findByMetadataBean(ItemFormMetadataBean metadataBean, EventCRFBean eventCrfBean, ItemDataBean itemDataBean) {

        String query =
            "from "
                + getDomainClassName()
                + " metadata where metadata.itemFormMetadataId = :id and metadata.itemId = :item_id and metadata.eventCrfId = :event_crf_id and metadata.itemDataId = :item_data_id ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("id", new Integer(metadataBean.getId()));
        q.setInteger("item_id", new Integer(metadataBean.getItemId()));
        q.setInteger("event_crf_id", new Integer(eventCrfBean.getId()));
        q.setInteger("item_data_id", new Integer(itemDataBean.getId()));
        return (DynamicsItemFormMetadataBean) q.uniqueResult();
    }

    public DynamicsItemFormMetadataBean findByItemDataBean(ItemDataBean itemDataBean) {

        String query = "from " + getDomainClassName() + " metadata where metadata.itemDataId = :item_data_id ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);

        q.setInteger("item_data_id", new Integer(itemDataBean.getId()));
        return (DynamicsItemFormMetadataBean) q.uniqueResult();
    }

    /* (non-Javadoc)
     * @see org.akaza.openclinica.dao.hibernate.AbstractDomainDao#saveOrUpdate(org.akaza.openclinica.domain.DomainObject)
     * The reason we overwrite this method is to make it non transactional. and mainly to make saves faster.
     */
    @Override
    public DynamicsItemFormMetadataBean saveOrUpdate(DynamicsItemFormMetadataBean domainObject) {
        getCurrentSession().saveOrUpdate(domainObject);
        getCurrentSession().flush();
        return domainObject;
    }

}
