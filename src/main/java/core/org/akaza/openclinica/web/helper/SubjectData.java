package core.org.akaza.openclinica.web.helper;

import core.org.akaza.openclinica.service.dto.FormVersion;

import java.util.HashSet;
import java.util.Set;

public class SubjectData {
    private Set <EventData> eventDatas;

    public Set<EventData> getEventDatas() {
        return eventDatas;
    }

    public void setEventDatas(Set<EventData> eventDatas) {
        this.eventDatas = eventDatas;
    }

    public void addEventData(EventData eventData){
        if (getEventDatas() == null)
            eventDatas = new HashSet<>();
 //       eventData.setSubjectData(this);
        eventDatas.add(eventData);
    }
}
