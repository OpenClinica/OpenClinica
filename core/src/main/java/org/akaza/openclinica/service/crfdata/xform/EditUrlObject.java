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
    StudyEvent studyEvent;
    String mode;
    EventDefinitionCrf edc;
    EventCrf eventCrf;

    public EditUrlObject(FormLayout formLayout, String crfOid, String instance, String ecid, String redirect, boolean markComplete, String studyOid,
            List<FormLayoutMedia> mediaList, String goTo, String flavor, Role role, Study parentStudy, StudyEvent studyEvent, String mode,
            EventDefinitionCrf edc, EventCrf eventCrf) {
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
    }

}
