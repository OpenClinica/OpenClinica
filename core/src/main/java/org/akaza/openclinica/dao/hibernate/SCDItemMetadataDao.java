/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2010 Akaza Research
 */
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.crfdata.SCDItemMetadataBean;

import java.util.ArrayList;
import java.util.List;

public class SCDItemMetadataDao extends AbstractDomainDao<SCDItemMetadataBean>{
    
    @Override
    Class<SCDItemMetadataBean> domainClass() {
        return SCDItemMetadataBean.class;
    }
    
    
    @SuppressWarnings("unchecked")
    public ArrayList<SCDItemMetadataBean> findAllBySectionId(Integer sectionId) {
        String query = "select scd.* from scd_item_metadata scd where scd.scd_item_form_metadata_id in ("
            + "select ifm.item_form_metadata_id from item_form_metadata ifm where ifm.section_id = :sectionId)";
        org.hibernate.Query q = this.getCurrentSession().createSQLQuery(query).addEntity(this.domainClass());
        q.setInteger("sectionId", sectionId);
        return (ArrayList<SCDItemMetadataBean>) q.list();  
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> findAllSCDItemFormMetadataIdsBySectionId(Integer sectionId) {
        String query = "select scd.scd_item_form_metadata_id from scd_item_metadata scd where scd.scd_item_form_metadata_id in ("
        + "select ifm.item_form_metadata_id from item_form_metadata ifm where ifm.section_id = :sectionId)";
        org.hibernate.Query q = this.getCurrentSession().createSQLQuery(query);
        q.setInteger("sectionId", sectionId);
        return q.list();
    }
    @SuppressWarnings("unchecked")
    public ArrayList<SCDItemMetadataBean> findAllSCDByItemFormMetadataId(Integer itemFormMetadataId) {
        String query = "select scd.* from scd_item_metadata scd where scd.scd_item_form_metadata_id = :itemFormMetadataId)";
        org.hibernate.Query q = this.getCurrentSession().createSQLQuery(query);
        q.setInteger("itemFormMetadataId", itemFormMetadataId);
        return (ArrayList<SCDItemMetadataBean>) q.list();
    }
}