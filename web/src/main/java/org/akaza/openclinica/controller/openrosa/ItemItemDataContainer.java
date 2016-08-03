package org.akaza.openclinica.controller.openrosa;

import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;

public class ItemItemDataContainer {
    private Item item;
    private ItemData itemData;
    private Integer responseTypeId;
    
    public ItemItemDataContainer(Item item, ItemData itemData , Integer responseTypeId) {
        super();
        this.item = item;
        this.itemData = itemData;
        this.responseTypeId=responseTypeId;
    }
    public Item getItem() {
        return item;
    }
    public void setItem(Item item) {
        this.item = item;
    }
    public ItemData getItemData() {
        return itemData;
    }
    public void setItemData(ItemData itemData) {
        this.itemData = itemData;
    }
    public Integer getResponseTypeId() {
        return responseTypeId;
    }
    public void setResponseTypeId(Integer responseTypeId) {
        this.responseTypeId = responseTypeId;
    }
    
}
