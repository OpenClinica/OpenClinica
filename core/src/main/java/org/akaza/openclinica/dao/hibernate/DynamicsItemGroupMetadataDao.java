package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
import org.akaza.openclinica.domain.crfdata.DynamicsItemGroupMetadataBean;

public class DynamicsItemGroupMetadataDao extends AbstractDomainDao<DynamicsItemGroupMetadataBean>{

    @Override 
    public Class<DynamicsItemGroupMetadataBean> domainClass() {
        return DynamicsItemGroupMetadataBean.class;
    }
    
    public DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, EventCRFBean eventCrfBean) {
        String query =
            "from " + getDomainClassName()
                + " metadata where metadata.itemGroupMetadataId = :id and metadata.itemGroupId = :item_group_id and metadata.eventCrfId = :event_crf_id ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("id", new Integer(metadataBean.getId()));
        q.setInteger("item_group_id", new Integer(metadataBean.getItemGroupId()));
        q.setInteger("event_crf_id", new Integer(eventCrfBean.getId()));
        return (DynamicsItemGroupMetadataBean) q.uniqueResult();
    }
    
    public DynamicsItemGroupMetadataBean findByMetadataBean(ItemGroupMetadataBean metadataBean, int eventCrfBeanId) {
        String query =
            "from " + getDomainClassName()
                + " metadata where metadata.itemGroupMetadataId = :id and metadata.itemGroupId = :item_group_id and metadata.eventCrfId = :event_crf_id ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("id", new Integer(metadataBean.getId()));
        q.setInteger("item_group_id", new Integer(metadataBean.getItemGroupId()));
        q.setInteger("event_crf_id", new Integer(eventCrfBeanId));
        return (DynamicsItemGroupMetadataBean) q.uniqueResult();
    }
}
