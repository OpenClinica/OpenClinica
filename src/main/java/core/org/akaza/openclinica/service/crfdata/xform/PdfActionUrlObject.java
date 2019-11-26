package core.org.akaza.openclinica.service.crfdata.xform;

import java.util.List;

import javax.servlet.ServletContext;

import core.org.akaza.openclinica.bean.core.Role;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import core.org.akaza.openclinica.domain.datamap.FormLayout;
import core.org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import core.org.akaza.openclinica.domain.datamap.JobDetail;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;

public class PdfActionUrlObject extends ActionUrlObject {

    private String studySubjectOID;
    private String format;
    private String margin;
    private String landscape;

    public String getStudySubjectOID() {
        return studySubjectOID;
    }

    public void setStudySubjectOID(String studySubjectOID) {
        this.studySubjectOID = studySubjectOID;
    }


    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getMargin() {
        return margin;
    }

    public void setMargin(String margin) {
        this.margin = margin;
    }

    public String getLandscape() {
        return landscape;
    }

    public void setLandscape(String landscape) {
        this.landscape = landscape;
    }

    public PdfActionUrlObject(FormLayout formLayout, String crfOid, String instance, String ecid, String redirect,
                              boolean markComplete, String studyOid, List<FormLayoutMedia> mediaList, String goTo, String flavor,
                              Role role, Study parentStudy, Study site, StudyEvent studyEvent, String mode, EventDefinitionCrf edc,
                              EventCrf eventCrf, String loadWarning, boolean formLocked, String studySubjectOID, String format,
                              String margin, String landscape) {
        super(formLayout, crfOid, instance, ecid, redirect, markComplete, studyOid, mediaList, goTo, flavor, role,
                parentStudy, site, studyEvent, mode, edc, eventCrf, loadWarning, formLocked);
        this.studySubjectOID = studySubjectOID;
        this.format = format;
        this.margin = margin;
        this.landscape = landscape;
    }



}