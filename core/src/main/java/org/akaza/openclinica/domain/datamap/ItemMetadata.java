package org.akaza.openclinica.domain.datamap;

/**
 * Created by yogi on 11/2/16.
 */
public class ItemMetadata {
    private ItemGroupMetadata igm;

    public ItemFormMetadata getIfm() {
        return ifm;
    }

    public void setIfm(ItemFormMetadata ifm) {
        this.ifm = ifm;
    }

    private ItemFormMetadata ifm;

    public ItemGroupMetadata getIgm() {
        return igm;
    }

    public void setIgm(ItemGroupMetadata igm) {
        this.igm = igm;
    }

    public ItemMetadata(ItemGroupMetadata igm) {
        this.igm = igm;
    }
}
