package org.akaza.openclinica.domain.crfdata;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * DynamicsItemFormMetadataBean
 * @author thickerson
 *
 */
@Entity
@Table(name = "dynamics_item_form_metadata")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "dynamics_item_form_metadata_id_seq") })
public class DynamicsItemFormMetadataBean extends AbstractMutableDomainObject {
    
    boolean showItem;
    int eventCrfId;
    int itemId;
    int itemFormMetadataId;
    int crfVersionId;
    
    public DynamicsItemFormMetadataBean() {
        
    }
    
    public DynamicsItemFormMetadataBean(ItemFormMetadataBean metadataBean, EventCRFBean eventCRFBean) {
        setItemId(metadataBean.getItemId());
        setItemFormMetadataId(metadataBean.getId());
        setCrfVersionId(metadataBean.getCrfVersionId());
        setEventCrfId(eventCRFBean.getId());
    }

    public boolean isShowItem() {
        return showItem;
    }

    public void setShowItem(boolean showItem) {
        this.showItem = showItem;
    }

    public int getEventCrfId() {
        return eventCrfId;
    }

    public void setEventCrfId(int eventCrfId) {
        this.eventCrfId = eventCrfId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemFormMetadataId() {
        return itemFormMetadataId;
    }

    public void setItemFormMetadataId(int itemFormMetadataId) {
        this.itemFormMetadataId = itemFormMetadataId;
    }

    public int getCrfVersionId() {
        return crfVersionId;
    }

    public void setCrfVersionId(int crfVersionId) {
        this.crfVersionId = crfVersionId;
    }
}
