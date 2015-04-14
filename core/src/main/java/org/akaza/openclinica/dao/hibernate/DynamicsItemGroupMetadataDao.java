package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.core.CoreResources;
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
    
    public Boolean hasShowingInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String query = "";
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            query = "select dg.item_group_id from dyn_item_group_metadata dg where dg.event_crf_id = :eventCrfId and dg.item_group_metadata_id in ("
                + " select distinct igm.item_group_metadata_id from item_group_metadata igm where igm.crf_version_id = :crfVersionId"
                + " and igm.show_group = 0"
                + " and igm.item_id in (select im.item_id from item_form_metadata im where im.section_id = :sectionId and im.crf_version_id = :crfVersionId))"
                + " and dg.show_group = 1 and rownum = 1";
        } else {
        query = "select dg.item_group_id from dyn_item_group_metadata dg where dg.event_crf_id = :eventCrfId and dg.item_group_metadata_id in ("
                + " select distinct igm.item_group_metadata_id from item_group_metadata igm where igm.crf_version_id = :crfVersionId"
                + " and igm.show_group = 'false'"
                + " and igm.item_id in (select im.item_id from item_form_metadata im where im.section_id = :sectionId and im.crf_version_id = :crfVersionId))"
                + " and dg.show_group = 'true' limit 1";
        }
        
        org.hibernate.Query q = this.getCurrentSession().createSQLQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        q.setInteger("crfVersionId", crfVersionId);
        q.setInteger("sectionId", sectionId);
        q.setInteger("crfVersionId", crfVersionId);
        return q.list() != null && q.list().size() > 0;
    }
    public  void delete(int eventCrfId){
        String query = " delete from " + getDomainClassName() +  "  where eventCrfId =:eventCrfId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        q.executeUpdate();
    }

}
