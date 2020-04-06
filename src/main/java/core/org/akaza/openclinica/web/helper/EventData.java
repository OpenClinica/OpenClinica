package core.org.akaza.openclinica.web.helper;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class EventData {
    private String eventOid;
    private String repeatKey;
    private Set<FormData> formDatas;

    public EventData(String eventOid) {
        this.eventOid = eventOid;
    }

    public String getEventOid() {
        return eventOid;
    }

    public void setEventOid(String eventOid) {
        this.eventOid = eventOid;
    }

    public Set<FormData> getFormDatas() {
        return formDatas;
    }

    public void setFormDatas(Set<FormData> formDatas) {
        this.formDatas = formDatas;
    }

    public String getRepeatKey() {
        return repeatKey;
    }

    public void setRepeatKey(String repeatKey) {
        this.repeatKey = repeatKey;
    }

    public void addFormData(FormData formData){
        if (getFormDatas() == null)
            formDatas = new HashSet<>();
        formDatas.add(formData);
    }

    public void removeAllFormData(){
        formDatas=null;
    }

}
