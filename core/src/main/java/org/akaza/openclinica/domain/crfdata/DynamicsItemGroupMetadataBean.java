package org.akaza.openclinica.domain.crfdata;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "dyn_item_group_metadata")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "dyn_item_group_metadata_id_seq") })
public class DynamicsItemGroupMetadataBean extends AbstractMutableDomainObject {

    boolean showGroup;
    int eventCrfId;
    int itemGroupMetadataId;
    int itemGroupId;

    public DynamicsItemGroupMetadataBean() {

    }

    public DynamicsItemGroupMetadataBean(ItemGroupMetadataBean metadataBean, EventCRFBean eventCrfBean) {
        setEventCrfId(eventCrfBean.getId());
        setItemGroupMetadataId(metadataBean.getId());
        setItemGroupId(metadataBean.getItemGroupId());
    }

    public boolean isShowGroup() {
        return showGroup;
    }

    public void setShowGroup(boolean showGroup) {
        this.showGroup = showGroup;
    }

    public int getEventCrfId() {
        return eventCrfId;
    }

    public void setEventCrfId(int eventCrfId) {
        this.eventCrfId = eventCrfId;
    }

    public int getItemGroupMetadataId() {
        return itemGroupMetadataId;
    }

    public void setItemGroupMetadataId(int itemGroupMetadataId) {
        this.itemGroupMetadataId = itemGroupMetadataId;
    }

    public int getItemGroupId() {
        return itemGroupId;
    }

    public void setItemGroupId(int itemGroupId) {
        this.itemGroupId = itemGroupId;
    }

}
