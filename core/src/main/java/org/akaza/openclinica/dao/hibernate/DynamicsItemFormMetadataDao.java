package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;

import java.util.ArrayList;
import java.util.List;

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
    
    @SuppressWarnings("unchecked")
    public List<Integer> findItemIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String query = "select distinct ditem.item_id from dyn_item_form_metadata ditem" 
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )";
        
        org.hibernate.Query q = this.getCurrentSession().createSQLQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        q.setInteger("groupId", groupId);
        q.setInteger("crfVersionId", crfVersionId);
        q.setInteger("sectionId", sectionId);
        q.setInteger("crfVersionId", crfVersionId);
        return new ArrayList<Integer>(q.list());
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> findShowItemIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String query = "select distinct ditem.item_id from dyn_item_form_metadata ditem" 
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='true'";
        
        org.hibernate.Query q = this.getCurrentSession().createSQLQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        q.setInteger("groupId", groupId);
        q.setInteger("crfVersionId", crfVersionId);
        q.setInteger("sectionId", sectionId);
        q.setInteger("crfVersionId", crfVersionId);
        return new ArrayList<Integer>(q.list());
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> findShowItemDataIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String query = "select ditem.item_data_id from dyn_item_form_metadata ditem" 
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='true'";
        
        org.hibernate.Query q = this.getCurrentSession().createSQLQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        q.setInteger("groupId", groupId);
        q.setInteger("crfVersionId", crfVersionId);
        q.setInteger("sectionId", sectionId);
        q.setInteger("crfVersionId", crfVersionId);
        return new ArrayList<Integer>(q.list());
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> findHideItemDataIdsForAGroupInSection(int groupId, int sectionId, int crfVersionId, int eventCrfId) {
        String query = "select ditem.item_data_id from dyn_item_form_metadata ditem" 
            + " where ditem.item_data_id in (select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select distinct igm.item_id from item_group_metadata igm"
            + " where igm.item_group_id = :groupId and igm.crf_version_id = :crfVersionId and igm.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId))"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='false'";
        
        org.hibernate.Query q = this.getCurrentSession().createSQLQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        q.setInteger("groupId", groupId);
        q.setInteger("crfVersionId", crfVersionId);
        q.setInteger("sectionId", sectionId);
        q.setInteger("crfVersionId", crfVersionId);
        return new ArrayList<Integer>(q.list());
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> findShowItemDataIdsInSection(int sectionId, int crfVersionId, int eventCrfId) {
        String query = "select ditem.item_data_id from dyn_item_form_metadata ditem" 
            + " where ditem.item_data_id in ( select idata.item_data_id from item_data idata"
            + " where idata.event_crf_id = :eventCrfId and idata.item_id in ("
            + " select ifm.item_id from item_form_metadata ifm where ifm.show_item='false' and ifm.section_id = :sectionId"
            + " and ifm.crf_version_id = :crfVersionId)"
            + " and (idata.status_id != 5 and idata.status_id != 7) )"
            + " and ditem.show_item='true'";
        
        org.hibernate.Query q = this.getCurrentSession().createSQLQuery(query);
        q.setInteger("eventCrfId", eventCrfId);
        q.setInteger("sectionId", sectionId);
        q.setInteger("crfVersionId", crfVersionId);
        return new ArrayList<Integer>(q.list());
    }

}
