package core.org.akaza.openclinica.domain;

import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.domain.datamap.SubjectEventStatus;
import core.org.akaza.openclinica.domain.enumsupport.CodedEnum;

import java.util.HashMap;

/*
* This code value can be mapped with the status in the Event_Crf table
* Locked, Skipped, Stopped should be cascaded and showed as locked in eventCrf level for UI, but event_crf table won't get updated, Hence marked it as 0 in this enum
* */
public enum EventCrfStatusEnum {
    REMOVED(5, "invalid"), DATA_ENTRY_STARTED(1, "data_entry_started"), DATA_ENTRY_COMPLETED(2, "data_entry_complete"), EVENT_REMOVED(7, "invalid"), LOCKED (0, "locked");
    int code;
    String description;

    /*studyEventStatusId is subjectEventStatusId from StudyEvent table*/
    public static EventCrfStatusEnum getByCode(int code, int studyEventStatusId){

        SubjectEventStatus studyEventStatus = SubjectEventStatus.getByCode(studyEventStatusId);
        if(studyEventStatus.equals(SubjectEventStatus.LOCKED) || studyEventStatus.equals(SubjectEventStatus.SKIPPED) || studyEventStatus.equals(SubjectEventStatus.STOPPED))
                return LOCKED;
        HashMap<Integer, EventCrfStatusEnum> enumObjects = new HashMap<Integer, EventCrfStatusEnum>();
        for (EventCrfStatusEnum theEnum : EventCrfStatusEnum.values()) {
            enumObjects.put(theEnum.getCode(), theEnum);
        }
        return enumObjects.get(Integer.valueOf(code));
    }

    public Integer getCode() {
        return this.code;
    }


    public String getDescription(){
        return this.description;
    }
    EventCrfStatusEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }
}
