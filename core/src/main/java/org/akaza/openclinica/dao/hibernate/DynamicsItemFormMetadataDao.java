package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;

public class DynamicsItemFormMetadataDao extends AbstractDomainDao<DynamicsItemFormMetadataBean>{

    @Override 
    public Class<DynamicsItemFormMetadataBean> domainClass() {
        return DynamicsItemFormMetadataBean.class;
    }
}
