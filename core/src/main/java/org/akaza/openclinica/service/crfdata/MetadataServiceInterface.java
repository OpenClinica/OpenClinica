package org.akaza.openclinica.service.crfdata;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.hibernate.AbstractDomainDao;

/**
 * MetadataServiceInterface, our abstract interface for Dynamics
 * @author thickerson, Mar 3rd, 2010
 * initial methods: isShown, show and hide
 * (can add others later to enable/disable/color/uncolor, etc
 * initial implementations: ItemMetadataService and GroupMetadataService
 *
 */
public interface MetadataServiceInterface {

    public abstract boolean isShown(Object metadataBean);
    
    public abstract boolean hide(Object metadataBean, EventCRFBean eventCrfBean, AbstractDomainDao metadataDao);
    
    public abstract boolean show(Object metadataBean);
}
