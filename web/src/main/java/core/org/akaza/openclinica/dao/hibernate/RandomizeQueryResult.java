package core.org.akaza.openclinica.dao.hibernate;

import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.ItemData;
import core.org.akaza.openclinica.domain.datamap.ItemGroup;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;

public class RandomizeQueryResult {
    private StudyEvent studyEvent;
    private EventCrf eventCrf;
    private ItemGroup itemGroup;
    private ItemData itemData;

    public RandomizeQueryResult(StudyEvent studyEvent, EventCrf eventCrf, ItemGroup itemGroup, ItemData itemData) {
        this.studyEvent = studyEvent;
        this.eventCrf = eventCrf;
        this.itemGroup = itemGroup;
        this.itemData = itemData;
    }

    public StudyEvent getStudyEvent() {
        return studyEvent;
    }

    public void setStudyEvent(StudyEvent studyEvent) {
        this.studyEvent = studyEvent;
    }

    public ItemData getItemData() {
        return itemData;
    }

    public void setItemData(ItemData itemData) {
        this.itemData = itemData;
    }

    public EventCrf getEventCrf() {
        return eventCrf;
    }

    public void setEventCrf(EventCrf eventCrf) {
        this.eventCrf = eventCrf;
    }

    public ItemGroup getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(ItemGroup itemGroup) {
        this.itemGroup = itemGroup;
    }
}