package org.akaza.openclinica.service;

import core.org.akaza.openclinica.bean.odmbeans.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.submit.crfdata.FormDataBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import core.org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import core.org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.controller.helper.table.ItemCountInForm;

public interface ImportValidationService {

    void validateQuery(DiscrepancyNoteBean discrepancyNotesBean);

    void validateItem(ImportItemDataBean itemDataBean, CrfBean crf, ImportItemGroupDataBean itemGroupDataBean, ItemCountInForm itemCountInForm);

    void validateEventCrf(StudySubject studySubject, StudyEvent studyEvent, FormLayout formLayout, EventDefinitionCrf edc);

    void validateEventDefnCrf(Study tenantStudy, StudyEventDefinition studyEventDefinition, CrfBean crf);

    void validateForm(FormDataBean formDataBean, Study tenantStudy, StudyEventDefinition studyEventDefinition);
}
