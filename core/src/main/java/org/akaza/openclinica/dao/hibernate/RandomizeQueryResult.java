package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.StudyEvent;

public class RandomizeQueryResult {
    private StudyEvent studyEvent;
    private ItemData itemData;

    public RandomizeQueryResult(StudyEvent studyEvent, ItemData itemData) {
        this.studyEvent = studyEvent;
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
}