package core.org.akaza.openclinica.service.crfdata.xform;

import java.util.List;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import core.org.akaza.openclinica.domain.datamap.FormLayout;
import core.org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;

public class ActionUrlObject {
    FormLayout formLayout;
    String crfOid;
    String instance;
    String ecid;
    String redirect;
    boolean markComplete;
    String studyOid;
    List<FormLayoutMedia> mediaList;
    String goTo;
    String flavor;
    Role role;
    Study parentStudy;
    Study site;
    StudyEvent studyEvent;
    String mode;
    EventDefinitionCrf edc;
    EventCrf eventCrf;
    String loadWarning;
    boolean formLocked;
    String iface;

    public ActionUrlObject(FormLayout formLayout, String crfOid, String instance, String ecid, String redirect, boolean markComplete, String studyOid,
                           List<FormLayoutMedia> mediaList, String goTo, String flavor, Role role, Study parentStudy, Study site, StudyEvent studyEvent, String mode,
                           EventDefinitionCrf edc, EventCrf eventCrf, String loadWarning, boolean formLocked) {
        super();
        this.formLayout = formLayout;
        this.crfOid = crfOid;
        this.instance = instance;
        this.ecid = ecid;
        this.redirect = redirect;
        this.markComplete = markComplete;
        this.studyOid = studyOid;
        this.mediaList = mediaList;
        this.goTo = goTo;
        this.flavor = flavor;
        this.role = role;
        this.parentStudy = parentStudy;
        this.studyEvent = studyEvent;
        this.mode = mode;
        this.edc = edc;
        this.eventCrf = eventCrf;
        this.site = site;
        this.loadWarning = loadWarning;
        this.formLocked = formLocked;
    }

    public ActionUrlObject(FormLayout formLayout, String crfOid, String instance, String ecid, String redirect, boolean markComplete, String studyOid,
                           List<FormLayoutMedia> mediaList, String goTo, String flavor, Role role, Study parentStudy, Study site, StudyEvent studyEvent, String mode,
                           EventDefinitionCrf edc, EventCrf eventCrf, String loadWarning, boolean formLocked, String iface) {
        this(formLayout, crfOid, instance, ecid, redirect, markComplete, studyOid, mediaList, goTo, flavor, role, parentStudy, site, studyEvent, mode, edc, eventCrf, loadWarning, formLocked);
        this.iface = iface;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("FormLayout" + formLayout == null? null: formLayout.getName());
        buffer.append("crfOid:" + crfOid);
        buffer.append("instance:" + instance);
        buffer.append("ecdd:" + ecid);
        buffer.append("redirect:" + redirect);
        buffer.append("markComplete:" + markComplete);
        buffer.append("studyOid:" + studyOid);
        buffer.append("List<FormLayoutMedia> mediaList:" + mediaList.size());
        buffer.append("goTo:" + goTo);
        buffer.append("flavor:" + flavor);
        buffer.append("role:" + role);
        buffer.append("parentStudy:" + parentStudy);
        buffer.append("site:" + site);
        buffer.append("studyEvent:" + studyEvent);
        buffer.append("mode:" + mode);
        buffer.append("edc:" + edc);
        buffer.append("eventCrf:" + eventCrf);
        buffer.append("loadWarning:" + loadWarning);
        buffer.append("formLocked:" + formLocked);
        buffer.append("iface:" + iface);
        return buffer.toString();
    }
}
