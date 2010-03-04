package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.crfdata.DynamicsItemGroupMetadataBean;

public class DynamicsItemGroupMetadataDao extends AbstractDomainDao<DynamicsItemGroupMetadataBean>{

    @Override 
    public Class<DynamicsItemGroupMetadataBean> domainClass() {
        return DynamicsItemGroupMetadataBean.class;
    }
}
