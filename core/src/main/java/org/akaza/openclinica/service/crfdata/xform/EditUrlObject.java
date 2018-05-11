package org.akaza.openclinica.service.crfdata.xform;

import java.util.List;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;

public class EditUrlObject {
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
    boolean lockModeReadOnly = false;

    public EditUrlObject(FormLayout formLayout, String crfOid, String instance, String ecid, String redirect, boolean markComplete, String studyOid,
            List<FormLayoutMedia> mediaList, String goTo, String flavor, Role role, Study parentStudy, Study site, StudyEvent studyEvent, String mode,
            EventDefinitionCrf edc, EventCrf eventCrf, boolean lockModeReadOnly) {
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
        this.lockModeReadOnly = lockModeReadOnly;
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
        buffer.append("lockModeReadOnly:" + lockModeReadOnly);
        return buffer.toString();
    }
}
