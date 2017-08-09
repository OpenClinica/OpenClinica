/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.extract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.odmbeans.AuditLogBean;
import org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import org.akaza.openclinica.bean.odmbeans.BasicDefinitionsBean;
import org.akaza.openclinica.bean.odmbeans.ChildNoteBean;
import org.akaza.openclinica.bean.odmbeans.CodeListBean;
import org.akaza.openclinica.bean.odmbeans.CodeListItemBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.odmbeans.ElementRefBean;
import org.akaza.openclinica.bean.odmbeans.EventDefinitionDetailsBean;
import org.akaza.openclinica.bean.odmbeans.FormDefBean;
import org.akaza.openclinica.bean.odmbeans.FormDetailsBean;
import org.akaza.openclinica.bean.odmbeans.ItemDefBean;
import org.akaza.openclinica.bean.odmbeans.ItemDetailsBean;
import org.akaza.openclinica.bean.odmbeans.ItemGroupDefBean;
import org.akaza.openclinica.bean.odmbeans.ItemGroupDetailsBean;
import org.akaza.openclinica.bean.odmbeans.ItemGroupRepeatBean;
import org.akaza.openclinica.bean.odmbeans.ItemPresentInFormBean;
import org.akaza.openclinica.bean.odmbeans.ItemResponseBean;
import org.akaza.openclinica.bean.odmbeans.MeasurementUnitBean;
import org.akaza.openclinica.bean.odmbeans.MetaDataVersionBean;
import org.akaza.openclinica.bean.odmbeans.MetaDataVersionProtocolBean;
import org.akaza.openclinica.bean.odmbeans.MetaDataVersionRefBean;
import org.akaza.openclinica.bean.odmbeans.MultiSelectListBean;
import org.akaza.openclinica.bean.odmbeans.MultiSelectListItemBean;
import org.akaza.openclinica.bean.odmbeans.OdmAdminDataBean;
import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.bean.odmbeans.PresentInEventDefinitionBean;
import org.akaza.openclinica.bean.odmbeans.PresentInFormBean;
import org.akaza.openclinica.bean.odmbeans.SectionDetails;
import org.akaza.openclinica.bean.odmbeans.SimpleConditionalDisplayBean;
import org.akaza.openclinica.bean.odmbeans.StudyEventDefBean;
import org.akaza.openclinica.bean.odmbeans.StudyGroupClassListBean;
import org.akaza.openclinica.bean.odmbeans.StudyGroupItemBean;
import org.akaza.openclinica.bean.odmbeans.SymbolBean;
import org.akaza.openclinica.bean.odmbeans.TranslatedTextBean;
import org.akaza.openclinica.bean.odmbeans.UserBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportFormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportStudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectGroupDataBean;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.job.JobTerminationMonitor;
import org.akaza.openclinica.logic.odmExport.ClinicalDataUtil;
import org.akaza.openclinica.logic.odmExport.MetaDataCollector;
import org.akaza.openclinica.logic.odmExport.MetadataUnit;

/**
 * Fetch odm data from database and load odm related classes.
 *
 * @author ywang (May, 2008)
 */

public class OdmExtractDAO extends DatasetDAO {

	

    public OdmExtractDAO(DataSource ds) {
        super(ds);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_ODM_EXTRACT;
    }

    public void setStudyGroupClassTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// study_group_class_id
        this.setTypeExpected(2, TypeNames.STRING);// sgc_name(study_group_class
        // name)
        this.setTypeExpected(3, TypeNames.STRING);// sgc_type(group_class_type
        // name)
        this.setTypeExpected(4, TypeNames.INT);// (study_group_class) status_id
        this.setTypeExpected(5, TypeNames.STRING);// subject_assignment
        this.setTypeExpected(6, TypeNames.INT);// study_group_id
        this.setTypeExpected(7, TypeNames.STRING);// (study_group) sg_name
        this.setTypeExpected(8, TypeNames.STRING);// study_group description
    }

    public void setStudyEventAndFormMetaTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// definition_order
        this.setTypeExpected(2, TypeNames.INT); // crf_order
        this.setTypeExpected(3, TypeNames.INT); // edc.crf_id
        this.setTypeExpected(4, TypeNames.INT); // cv.crf_version_id
        this.setTypeExpected(5, TypeNames.STRING);// definition_oid
        this.setTypeExpected(6, TypeNames.STRING);// definition_name
        this.setTypeExpected(7, TypeNames.BOOL);// definition_repeating
        this.setTypeExpected(8, TypeNames.STRING);// definition_type
        this.setTypeExpected(9, TypeNames.STRING);// cv_oid
        this.setTypeExpected(10, TypeNames.STRING);// cv_name
        this.setTypeExpected(11, TypeNames.BOOL);// cv_required
        this.setTypeExpected(12, TypeNames.STRING);// null_values
        this.setTypeExpected(13, TypeNames.STRING);// crf_name
    }

    public void setStudyEventAndFormMetaOC1_3TypesExpected() {
        this.unsetTypeExpected();
        int i=1;
        this.setTypeExpected(i, TypeNames.INT);// definition_order
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // crf_order
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // edc.crf_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // cv.crf_version_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING);// definition_oid
        ++i;
        this.setTypeExpected(i, TypeNames.STRING);// cv_oid
        ++i;
        this.setTypeExpected(i, TypeNames.STRING);// description
        ++i;
        this.setTypeExpected(i, TypeNames.STRING);// category
        ++i;
        this.setTypeExpected(i, TypeNames.STRING);// version_description
        ++i;
        this.setTypeExpected(i, TypeNames.STRING);// revision_notes
        ++i;
        this.setTypeExpected(i, TypeNames.STRING);// crf_oid
        ++i;
        this.setTypeExpected(i, TypeNames.STRING);// null_values
        ++i;
        this.setTypeExpected(i, TypeNames.INT); //default_version_id
        ++i;
        this.setTypeExpected(i, TypeNames.BOOL); //electronic_signature
        ++i;
        this.setTypeExpected(i, TypeNames.BOOL);// double_entry
        ++i;
        this.setTypeExpected(i, TypeNames.BOOL);// hide_crf
        ++i;
        this.setTypeExpected(i, TypeNames.BOOL);// participant_form
        ++i;
        this.setTypeExpected(i, TypeNames.BOOL);// allow_anonymous_submission
        ++i;
        this.setTypeExpected(i, TypeNames.STRING);// submission_url
        ++i;
        this.setTypeExpected(i, TypeNames.BOOL);// offline
        ++i;
        this.setTypeExpected(i, TypeNames.INT);// source_data_verification_code
    }

    public void setItemDataMaxLengthTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// item_id
        this.setTypeExpected(2, TypeNames.INT);// max_length
    }

    public void setItemGroupAndItemMetaWithUnitTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// crf_id
        this.setTypeExpected(2, TypeNames.INT);// crf_version_id
        this.setTypeExpected(3, TypeNames.INT);// item_group_id
        this.setTypeExpected(4, TypeNames.INT);// item_id
        this.setTypeExpected(5, TypeNames.INT);// response_set_id
        this.setTypeExpected(6, TypeNames.STRING);// crf_version_oid
        this.setTypeExpected(7, TypeNames.STRING);// item_group_oid
        this.setTypeExpected(8, TypeNames.STRING);// item_oid
        this.setTypeExpected(9, TypeNames.STRING); // item_group_name
        this.setTypeExpected(10, TypeNames.STRING);// item_name
        this.setTypeExpected(11, TypeNames.INT);// item_data_type_id
        this.setTypeExpected(12, TypeNames.STRING);// item_header
        this.setTypeExpected(13, TypeNames.STRING);// left_item_text
        this.setTypeExpected(14, TypeNames.STRING);// right_item_text
        this.setTypeExpected(15, TypeNames.BOOL);// item_required
        this.setTypeExpected(16, TypeNames.STRING);// regexp
        this.setTypeExpected(17, TypeNames.STRING);// regexp_error_msg
        this.setTypeExpected(18, TypeNames.STRING);// width_decimal
        this.setTypeExpected(19, TypeNames.INT);// response_type_id
        this.setTypeExpected(20, TypeNames.STRING);// options_text
        this.setTypeExpected(21, TypeNames.STRING);// options_values
        this.setTypeExpected(22, TypeNames.STRING);// response_label
        this.setTypeExpected(23, TypeNames.STRING);// item_group_header
        this.setTypeExpected(24,TypeNames.BOOL);//is Repeating?
        this.setTypeExpected(25, TypeNames.STRING);// item_description
        this.setTypeExpected(26, TypeNames.INT);// section_id
        this.setTypeExpected(27, TypeNames.STRING); // question_number_label
        this.setTypeExpected(28, TypeNames.STRING);// mu_oid
    }

    public void setItemGroupAndItemMetaOC1_3TypesExpected() {
        this.unsetTypeExpected();
        int i=1;    this.setTypeExpected(i, TypeNames.INT);// crf_id
        ++i;    this.setTypeExpected(i, TypeNames.INT);// crf_version_id
        ++i;    this.setTypeExpected(i, TypeNames.INT);// item_group_id
        ++i;    this.setTypeExpected(i, TypeNames.INT);// item_id
        ++i;    this.setTypeExpected(i, TypeNames.INT);// response_set_id
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// crf_version_oid
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// item_group_oid
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// item_oid
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// item_header
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// subheader
        ++i;    this.setTypeExpected(i, TypeNames.INT);// section_id
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// left_item_text
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// right_item_text
        ++i;    this.setTypeExpected(i, TypeNames.INT);// parent_id
        ++i;    this.setTypeExpected(i, TypeNames.INT);// column_number
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// page_number_label
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// response_layout
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// default_value
        ++i;    this.setTypeExpected(i, TypeNames.BOOL);// phi_status
        ++i;    this.setTypeExpected(i, TypeNames.BOOL);// show_item
        ++i;    this.setTypeExpected(i, TypeNames.INT);// response_type_id
        ++i;    this.setTypeExpected(i, TypeNames.INT);// repeat_number
        ++i;    this.setTypeExpected(i, TypeNames.INT);// repeat_max
        ++i;    this.setTypeExpected(i, TypeNames.BOOL);// show_group
        ++i;	this.setTypeExpected(i, TypeNames.INT);//item_order
        ++i;    this.setTypeExpected(i, TypeNames.STRING);// crf_version_oid
    }

    public void setSubjectEventFormDataTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.STRING);// study_subject_oid;
        this.setTypeExpected(2, TypeNames.INT);// definition_order;
        this.setTypeExpected(3, TypeNames.STRING);// definition_oid;
        this.setTypeExpected(4, TypeNames.BOOL);// definition_repeating
        this.setTypeExpected(5, TypeNames.INT);// sample_ordinal
        this.setTypeExpected(6, TypeNames.INT);// crf_order;
        this.setTypeExpected(7, TypeNames.STRING);// crf_version_oid
        this.setTypeExpected(8, TypeNames.INT);// event_crf_id
    }

    public void setSubjectEventFormDataTypesExpected(String odmVersion) {
        if(odmVersion.equalsIgnoreCase("occlinical_data"))odmVersion="oc1.3";
        if ("1.2".equals(odmVersion) || "1.3".equals(odmVersion)) {
            setSubjectEventFormDataTypesExpected();
        } else if ("oc1.2".equals(odmVersion) || "oc1.3".equals(odmVersion)) {
            this.unsetTypeExpected();
            this.setTypeExpected(1, TypeNames.STRING);// study_subject_oid;
            this.setTypeExpected(2, TypeNames.STRING);// label(study subject)
            this.setTypeExpected(3, TypeNames.STRING);// unique-identifier
            this.setTypeExpected(4, TypeNames.STRING);// secondary_label(study
            // subject)
            this.setTypeExpected(5, TypeNames.STRING);// gender
            this.setTypeExpected(6, TypeNames.DATE);// date_of_birth
            this.setTypeExpected(7, TypeNames.INT);// status_id (subject)
            this.setTypeExpected(8, TypeNames.INT);// sgc_id
            this.setTypeExpected(9, TypeNames.STRING);// sgc_name
            this.setTypeExpected(10, TypeNames.STRING);// sg_name
            this.setTypeExpected(11, TypeNames.INT);// definition_order;
            this.setTypeExpected(12, TypeNames.STRING);// definition_oid;
            this.setTypeExpected(13, TypeNames.BOOL);// definition_repeating
            this.setTypeExpected(14, TypeNames.INT);// sample_ordinal
            this.setTypeExpected(15, TypeNames.STRING);// se_location (study
            // event)
            this.setTypeExpected(16, TypeNames.DATE);// date_start (study
            // event)
            this.setTypeExpected(17, TypeNames.DATE);// date_end (study
            // event)
            this.setTypeExpected(18, TypeNames.BOOL);// start_time_flag
            this.setTypeExpected(19, TypeNames.BOOL);// end_time_flag
            this.setTypeExpected(20, TypeNames.INT);// event_status_id
            this.setTypeExpected(21, TypeNames.INT);// crf_order;
            this.setTypeExpected(22, TypeNames.STRING);// crf_version_oid
            this.setTypeExpected(23, TypeNames.STRING);// crf_version
            this.setTypeExpected(24, TypeNames.INT); // cv_status_id
            this.setTypeExpected(25, TypeNames.INT);// ec_status_id
            this.setTypeExpected(26, TypeNames.INT);// event_crf_id
            this.setTypeExpected(27, TypeNames.DATE);// date_interviewed
            this.setTypeExpected(28, TypeNames.STRING);// interviewer_name
            this.setTypeExpected(29, TypeNames.INT);// validator_id
        }
    }

    public void setEventGroupItemDataWithUnitTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// event_crf_id;
        this.setTypeExpected(2, TypeNames.INT);// item_group_id;
        this.setTypeExpected(3, TypeNames.STRING);// item_group_oid
        this.setTypeExpected(4, TypeNames.STRING);// item_group_name
        this.setTypeExpected(5, TypeNames.INT);// item_id
        this.setTypeExpected(6, TypeNames.STRING);// item_oid
        this.setTypeExpected(7, TypeNames.INT);// item_data_ordinal
        this.setTypeExpected(8, TypeNames.STRING);// value
        this.setTypeExpected(9, TypeNames.INT);// item_data_type_id
        this.setTypeExpected(10, TypeNames.INT);// item_data_id
        this.setTypeExpected(11, TypeNames.STRING);// mu_oid
    }

    public void setEventCrfIdsByItemDataTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// event_crf_id;
    }

    public void setStudyMeasurementUnitsTypesExpected() {
        // select distinct mu.oc_oid as mu_oid, mu.name
        // mus.text
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.STRING); // mu_oid
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // name
    }

    public void setNullValueCVsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;

        this.setTypeExpected(i, TypeNames.STRING); // study_event_definition oc_oid
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // crf_version oc_oid
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // null_values
    }

    public void setItemCVOIDsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.INT); // crf_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // crf_version_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // item_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // cv_oid
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // item_oid
    }

    public void setStudyUsersTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.INT); // user_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // first_name
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // last_name
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // institution_affilition
    }

    public void setOCSubjectDataAuditsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.STRING); // study_subject_oid
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // audit_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // name
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // user_id
        ++i;
        this.setTypeExpected(i, TypeNames.TIMESTAMP); // audit_date
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // reason_for_change
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // old_value
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // new_value
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // audit_log_event_type_id
    }

    public void setOCEventDataAuditsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.STRING); // study_subject_oid
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // definition_oid
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // audit_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // name
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // user_id
        ++i;
        this.setTypeExpected(i, TypeNames.TIMESTAMP); // audit_date
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // reason_for_change
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // old_value
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // new_value
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // audit_log_event_type_id
    }

    public void setOCFormDataAuditsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.INT); // event_crf_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // audit_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // name
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // user_id
        ++i;
        this.setTypeExpected(i, TypeNames.TIMESTAMP); // audit_date
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // reason_for_change
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // old_value
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // new_value
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // audit_log_event_type_id
    }

    public void setOCItemDataAuditsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.INT); // item_data_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // audit_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // name
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // user_id
        ++i;
        this.setTypeExpected(i, TypeNames.TIMESTAMP); // audit_date
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // reason_for_change
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // old_value
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // new_value
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // audit_log_event_type_id
    }

    public void setOCSubjectDataDNsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.STRING); // study_subject_oid
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // parent_dn_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // dn_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // description
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // detailed_notes
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // owner_id
        ++i;
        this.setTypeExpected(i, TypeNames.DATE); // date_created
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // status
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // discrepancy_note_type.name
    }

    public void setOCEventDataDNsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.STRING); // study_subject_oid
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // definition_oid
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // parent_dn_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // dn_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // description
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // detailed_notes
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // owner_id
        ++i;
        this.setTypeExpected(i, TypeNames.DATE); // date_created
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // status
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // discrepancy_note_type.name
    }

    public void setOCFormDataDNsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.INT); // event_crf_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // parent_dn_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // dn_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // description
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // detailed_notes
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // owner_id
        ++i;
        this.setTypeExpected(i, TypeNames.DATE); // date_created
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // status
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // discrepancy_note_type.name
    }

    public void setOCItemDataDNsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.INT); // item_data_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // parent_dn_id
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // dn_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // description
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // detailed_notes
        ++i;
        this.setTypeExpected(i, TypeNames.INT); // owner_id
        ++i;
        this.setTypeExpected(i, TypeNames.DATE); // date_created
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // status
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // discrepancy_note_type.name
    }

    public void setSectionLabelsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.INT); // section_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // label
    }

    public void setParentItemOIDsTypesExpected() {
        this.unsetTypeExpected();
        int i = 1;
        this.setTypeExpected(i, TypeNames.INT); // item_id
        ++i;
        this.setTypeExpected(i, TypeNames.STRING); // item oc_oid
    }

    public void setSCDsTypesExpected() {
        this.unsetTypeExpected();
        int i=1;
        this.setTypeExpected(i, TypeNames.INT); // crf_id
        ++i; this.setTypeExpected(i, TypeNames.INT); // crf_version_id
        ++i; this.setTypeExpected(i, TypeNames.INT); // item_id
        ++i; this.setTypeExpected(i, TypeNames.STRING); // crf_version_oid
        ++i; this.setTypeExpected(i, TypeNames.STRING); // item_oid
        ++i; this.setTypeExpected(i, TypeNames.STRING); // control_item_name
        ++i; this.setTypeExpected(i, TypeNames.STRING); // option_value
        ++i; this.setTypeExpected(i, TypeNames.STRING); // message
    }

    public void setErasedScoreItemDataIdsTypesExpected() {
        this.unsetTypeExpected();
        int i=1;
        this.setTypeExpected(i, TypeNames.INT); // item_data_id
    }

    public void getBasicDefinitions(int studyId, BasicDefinitionsBean basicDef) {
        ArrayList<MeasurementUnitBean> units = basicDef.getMeasurementUnits();
        String uprev = "";
        this.setStudyMeasurementUnitsTypesExpected();
        ArrayList rows = this.select(this.getStudyMeasurementUnitsSql(studyId));
        Iterator it = rows.iterator();
        while (it.hasNext()) {
            HashMap row = (HashMap) it.next();
            String oid = (String) row.get("mu_oid");
            String name = (String) row.get("name");
            MeasurementUnitBean u = new MeasurementUnitBean();
            SymbolBean symbol = new SymbolBean();
            ArrayList<TranslatedTextBean> texts = new ArrayList<TranslatedTextBean>();
            if (uprev.equals(oid)) {
                u = units.get(units.size() - 1);
                symbol = u.getSymbol();
                texts = symbol.getTranslatedText();
            } else {
                u.setOid(oid);
                u.setName(name);
                units.add(u);
            }
            TranslatedTextBean t = new TranslatedTextBean();
            t.setText(name);
            texts.add(t);
            symbol.setTranslatedText(texts);
            u.setSymbol(symbol);
        }
    }

    public void getBasicDefinitions(String crfVersionOID, BasicDefinitionsBean basicDef) {
        ArrayList<MeasurementUnitBean> units = basicDef.getMeasurementUnits();
        String uprev = "";
        this.setStudyMeasurementUnitsTypesExpected();
        ArrayList rows = this.select(this.getStudyMeasurementUnitsSql(crfVersionOID));
        Iterator it = rows.iterator();
        while (it.hasNext()) {
            HashMap row = (HashMap) it.next();
            String oid = (String) row.get("mu_oid");
            String name = (String) row.get("name");
            MeasurementUnitBean u = new MeasurementUnitBean();
            SymbolBean symbol = new SymbolBean();
            ArrayList<TranslatedTextBean> texts = new ArrayList<TranslatedTextBean>();
            if (uprev.equals(oid)) {
                u = units.get(units.size() - 1);
                symbol = u.getSymbol();
                texts = symbol.getTranslatedText();
            } else {
                u.setOid(oid);
                u.setName(name);
                units.add(u);
            }
            TranslatedTextBean t = new TranslatedTextBean();
            t.setText(name);
            texts.add(t);
            symbol.setTranslatedText(texts);
            u.setSymbol(symbol);
        }
    }
    public void getUpdatedSiteMetadata(int parentStudyId, int studyId, MetaDataVersionBean metadata, String odmVersion) {
        HashMap<Integer, Integer> cvIdPoses = new HashMap<Integer, Integer>();
        this.setStudyEventAndFormMetaTypesExpected();
        ArrayList rows = this.select(this.getStudyEventAndFormMetaSql(parentStudyId, studyId, true));
        Iterator it = rows.iterator();
        String sedprev = "";
        MetaDataVersionProtocolBean protocol = metadata.getProtocol();
        while (it.hasNext()) {
            HashMap row = (HashMap) it.next();
            Integer sedOrder = (Integer) row.get("definition_order");
            Integer cvId = (Integer) row.get("crf_version_id");
            String sedOID = (String) row.get("definition_oid");
            String sedName = (String) row.get("definition_name");
            Boolean sedRepeat = (Boolean) row.get("definition_repeating");
            String sedType = (String) row.get("definition_type");
            String cvOID = (String) row.get("cv_oid");
            String cvName = (String) row.get("cv_name");
            Boolean cvRequired = (Boolean) row.get("cv_required");
            String nullValue = (String) row.get("null_values");
            String crfName = (String) row.get("crf_name");

            StudyEventDefBean sedef = new StudyEventDefBean();
            if (sedprev.equals(sedOID)) {
                int p = metadata.getStudyEventDefs().size() - 1;
                sedef = metadata.getStudyEventDefs().get(p);
            } else {
                sedprev = sedOID;
                ElementRefBean sedref = new ElementRefBean();
                sedref.setElementDefOID(sedOID);
                sedref.setMandatory("Yes");
                sedref.setOrderNumber(sedOrder);
                protocol.getStudyEventRefs().add(sedref);
                sedef.setOid(sedOID);
                sedef.setName(sedName);
                sedef.setRepeating(sedRepeat ? "Yes" : "No");
                String type = sedType.toLowerCase();
                type = type.substring(0, 1).toUpperCase() + type.substring(1);
                sedef.setType(type);
                metadata.getStudyEventDefs().add(sedef);
            }

            ElementRefBean formref = new ElementRefBean();
            formref.setElementDefOID(cvOID);
            formref.setMandatory(cvRequired ? "Yes" : "No");
            sedef.getFormRefs().add(formref);

            if (!cvIdPoses.containsKey(cvId)) {
                FormDefBean formdef = new FormDefBean();
                formdef.setOid(cvOID);
                formdef.setName(crfName + " - " + cvName);
                formdef.setRepeating("No");
                metadata.getFormDefs().add(formdef);
                cvIdPoses.put(cvId, metadata.getFormDefs().size() - 1);
                if (nullValue != null && nullValue.length() > 0) {
                }
            }
        }
    }


    public void getMetadata(int parentStudyId, int studyId, MetaDataVersionBean metadata, String odmVersion) {
      if(odmVersion.equalsIgnoreCase("occlinical_data"))odmVersion = "oc1.3";

        if("oc1.3".equals(odmVersion)) {
            if(metadata.getStudy().getParentStudyId() > 0) {
              //  this.getOCMetadata(parentStudyId, studyId, metadata, odmVersion);
            	 this.getMetadataOC1_3(parentStudyId, studyId, metadata, odmVersion);
            } else {
                this.getMetadataOC1_3(parentStudyId, studyId, metadata, odmVersion);
            }
        } else if("oc1.2".equals(odmVersion)) {
            this.getOCMetadata(parentStudyId, studyId, metadata, odmVersion);
        } else {
            this.getODMMetadata(parentStudyId, studyId, metadata, odmVersion);
        }
    }

    
    /**
     * Metadata to be obtained based on formVersionOID. The studyOID and studyEventOID are irrelevant as these are the global crfs, i.e crfs that do not belong to
     * any particular CRF. In order to comply with cdisc ODM, the studyOID, studyeventDefOID etc will be static values which do not exist in the database. 
     * @param metadata
     * @param formVersionOID
     */
    
    //The method underneath tries to reuse the code based on getODMMetadata
    public void getODMMetadataForForm(MetaDataVersionBean metadata,String formVersionOID,String odmVersion){
    	  FormDefBean formDef = new FormDefBean();
    	  String cvIds = new String("");
    	  CRFVersionDAO<String, ArrayList> crfVersionDAO = new CRFVersionDAO<String, ArrayList>(this.ds);
  	 	CRFVersionBean crfVersionBean = crfVersionDAO.findByOid(formVersionOID);
  	 	cvIds =crfVersionBean.getId()+"";
  	 	
  	 	applyStudyEventDef(metadata,formVersionOID);
    	
    	fetchItemGroupMetaData(metadata,cvIds,odmVersion);
    	getOCMetadataForGlobals(crfVersionBean.getId(),metadata,odmVersion);
    	 ArrayList rows  = new ArrayList();
         ArrayList<ItemGroupDefBean> igs = (ArrayList<ItemGroupDefBean>)metadata.getItemGroupDefs();
         HashMap<String,Integer> igPoses = getItemGroupOIDPos(metadata);
         ArrayList<ItemDefBean> its = (ArrayList<ItemDefBean>)metadata.getItemDefs();
      
         
         HashMap<String,Integer> itPoses = getItemOIDPos(metadata);
         HashMap<String,Integer> inPoses = new HashMap<String, Integer>();
         ItemGroupDefBean ig = new ItemGroupDefBean();
    	 Iterator it ;
    	 	
    	 	metadata.setCvIds(cvIds);
 
    	    HashMap<Integer, Integer> maxLengths = new HashMap<Integer, Integer>();
            this.setItemDataMaxLengthTypesExpected();
            rows.clear();
            logger.debug("Begin to execute GetItemDataMaxLengths");
            rows = select(this.getItemDataMaxLengths(cvIds));
            it = rows.iterator();
            while (it.hasNext()) {
                HashMap row = (HashMap) it.next();
                maxLengths.put((Integer) row.get("item_id"), (Integer) row.get("max_length"));
            }
            ItemDefBean itDef = new ItemDefBean();
            formDef =   fetchFormDetails(crfVersionBean,formDef);
            this.setItemGroupAndItemMetaWithUnitTypesExpected();
            rows.clear();
            String prevCvIg = "";
          
            logger.debug("Begin to execute GetItemGroupAndItemMetaWithUnitSql");
            logger.debug("getItemGroupandItemMetaWithUnitsql= " + this.getItemGroupAndItemMetaWithUnitSql(cvIds));   HashMap<Integer,String> sectionLabels = this.getSectionLabels(metadata.getSectionIds());
            HashMap<Integer,String> parentItemOIDs = this.getParentItemOIDs(cvIds);
            this.setItemGroupAndItemMetaOC1_3TypesExpected();
            logger.debug("Begin to execute GetItemGroupAndItemMetaWithUnitSql");
            logger.debug("getItemGroupandItemMetaWithUnitsql= " + this.getItemGroupAndItemMetaOC1_3Sql(cvIds));
             rows = select(this.getItemGroupAndItemMetaOC1_3Sql(cvIds));
            Iterator iter = rows.iterator();
            while (iter.hasNext()) {
                HashMap row = (HashMap) iter.next();
                Integer cvId = (Integer) row.get("crf_version_id");
                Integer igId = (Integer) row.get("item_group_id");
                String cvOID = (String) row.get("crf_version_oid");
                String igOID = (String) row.get("item_group_oid");
                String itOID = (String) row.get("item_oid");

                Integer igRepeatNum = (Integer) row.get("repeat_number");
                Integer igRepeatMax = (Integer) row.get("repeat_max");
                Boolean showGroup = (Boolean) row.get("show_group");
                String itemGroupHeader = (String)row.get("item_group_header");
                
                String itHeader = (String) row.get("item_header");
                String left = (String) row.get("left_item_text");
                String right = (String) row.get("right_item_text");
                String itSubheader = (String) row.get("subheader");
                Integer itSecId = (Integer) row.get("section_id");
                Integer itPId = (Integer) row.get("parent_id");
                Integer itColNum = (Integer) row.get("column_number");
                String itpgNum = (String) row.get("page_number_label");
                String layout = (String) row.get("response_layout");
                Integer rsTypeId = (Integer) row.get("response_type_id");
                String dfValue = (String) row.get("default_value");
                Boolean phi = (Boolean) row.get("phi_status");
                Boolean showItem = (Boolean) row.get("show_item");
                Integer orderInForm = (Integer)row.get("item_order");
                if((cvId+"-"+igId).equals(prevCvIg)) {
                } else {
                    prevCvIg = cvId+"-"+igId;
                    ig = igs.get(igPoses.get(igOID));
                    ItemGroupDetailsBean igDetail = ig.getItemGroupDetails();
                    igDetail.setOid(igOID);
                    
                    PresentInFormBean inForm = new PresentInFormBean();
                    inForm.setFormOid(cvOID);
                    ItemGroupRepeatBean igr = new ItemGroupRepeatBean();
                    
                    igr.setRepeatMax(igRepeatMax);
                    igr.setRepeatNumber(igRepeatNum);
                    inForm.setItemGroupRepeatBean(igr);
                    inForm.setShowGroup(showGroup==true?"Yes":"No");
                    inForm.setItemGroupHeader(itemGroupHeader);
                    igDetail.getPresentInForms().add(inForm);
                    ig.setItemGroupDetails(igDetail);
                    
                }
                itDef = its.get(itPoses.get(itOID));
                ItemDetailsBean itDetail = itDef.getItemDetails();
                itDetail.setOid(itOID);
                ItemPresentInFormBean itInForm = new ItemPresentInFormBean();
                itInForm.setFormOid(cvOID);
                itInForm.setColumnNumber(itColNum);
                itInForm.setDefaultValue(dfValue);
                itInForm.setItemHeader(itHeader);
                itInForm.setLeftItemText(left);
                itInForm.setRightItemText(right);
                itInForm.setItemSubHeader(itSubheader);
                itInForm.setPageNumber(itpgNum);
                itInForm.setParentItemOid(parentItemOIDs.get(itPId));
                itInForm.setSectionLabel(sectionLabels.get(itSecId));
                itInForm.setPhi(phi==false?"No":"Yes");
                itInForm.setOrderInForm(orderInForm);
                itInForm.setShowItem(showItem==true?"Yes":"No");
                ItemResponseBean itemResponse = new ItemResponseBean();
                itemResponse.setResponseLayout(layout);
                itemResponse.setResponseType(ResponseType.get(rsTypeId).getName());
                itInForm.setItemResponse(itemResponse);
                itDetail.getItemPresentInForm().add(itInForm);
                inPoses.put(itOID+"-"+cvOID, itDetail.getItemPresentInForm().size()-1);
            }
            this.getSCDs(cvIds, its, itPoses, inPoses);
            
             metadata.getFormDefs().add(formDef);
    }
    
    
    
   
    
    
    private void applyStudyEventDef(MetaDataVersionBean metadata,String formVersionOID) {

    	StudyEventDefBean studyEventDef = new StudyEventDefBean();
    	studyEventDef.setOid(MetadataUnit.FAKE_STUDY_EVENT_OID);
    	studyEventDef.setName(MetadataUnit.FAKE_SE_NAME);
    	studyEventDef.setRepeating(MetadataUnit.FAKE_SE_REPEATING);
    	
    	ElementRefBean formRefBean = new ElementRefBean();
    	formRefBean.setElementDefOID(formVersionOID);
    	studyEventDef.getFormRefs().add(formRefBean);
    	
    	metadata.getStudyEventDefs().add(studyEventDef);
    	
	}

	public FormDefBean fetchFormDetails(CRFVersionBean crfVBean,FormDefBean formDef){
    
    	CRFDAO<String, ArrayList> crfDao = new CRFDAO(this.ds);
    	CRFBean crfBean   = (CRFBean) crfDao.findByPK(crfVBean.getCrfId());  	
    	formDef.setOid(crfVBean.getOid());
    	formDef.setName(crfBean.getName() + " - " +crfVBean.getName());
    	
    	formDef.setRepeating("No");
    	FormDetailsBean formDetails = new FormDetailsBean();
    	formDetails.setName(crfVBean.getName());
    	formDetails.setOid(crfVBean.getOid());
    	formDetails.setParentFormOid(crfBean.getOid());
    	
    	
    setSectionBean(formDetails,new Integer(crfVBean.getId()));
    	
    formDef.setFormDetails(formDetails);
    	
    	return formDef;
    }
    
    /**
     * metadata for ODM extraction only
     */
    public void getODMMetadata(int parentStudyId, int studyId, MetaDataVersionBean metadata, String odmVersion) {
        String cvIds = "";
        HashMap<Integer, Integer> cvIdPoses = new HashMap<Integer, Integer>();
        this.setStudyEventAndFormMetaTypesExpected();
        logger.debug("Begin to execute GetStudyEventAndFormMetaSql");
        logger.debug("getStudyEventAndFormMetaSQl= " + this.getStudyEventAndFormMetaSql(parentStudyId, studyId, false));
        ArrayList rows = this.select(this.getStudyEventAndFormMetaSql(parentStudyId, studyId, false));
        Iterator it = rows.iterator();
        String sedprev = "";
        MetaDataVersionProtocolBean protocol = metadata.getProtocol();
        HashMap<Integer, String> nullMap = new HashMap<Integer, String>();
        HashMap<String, String> nullValueCVs = new HashMap<String, String>();
        while (it.hasNext()) {
            HashMap row = (HashMap) it.next();
            Integer sedOrder = (Integer) row.get("definition_order");
            Integer cvId = (Integer) row.get("crf_version_id");
            String sedOID = (String) row.get("definition_oid");
            String sedName = (String) row.get("definition_name");
            Boolean sedRepeat = (Boolean) row.get("definition_repeating");
            String sedType = (String) row.get("definition_type");
            String cvOID = (String) row.get("cv_oid");
            String cvName = (String) row.get("cv_name");
            Boolean cvRequired = (Boolean) row.get("cv_required");
            String nullValue = (String) row.get("null_values");
            String crfName = (String) row.get("crf_name");

            StudyEventDefBean sedef = new StudyEventDefBean();
            if (sedprev.equals(sedOID)) {
                int p = metadata.getStudyEventDefs().size() - 1;
                sedef = metadata.getStudyEventDefs().get(p);
            } else {
                sedprev = sedOID;
                ElementRefBean sedref = new ElementRefBean();
                sedref.setElementDefOID(sedOID);
                sedref.setMandatory("Yes");
                sedref.setOrderNumber(sedOrder);
                protocol.getStudyEventRefs().add(sedref);
                sedef.setOid(sedOID);
                sedef.setName(sedName);
                sedef.setRepeating(sedRepeat ? "Yes" : "No");
                String type = sedType.toLowerCase();
                type = type.substring(0, 1).toUpperCase() + type.substring(1);
                sedef.setType(type);
                metadata.getStudyEventDefs().add(sedef);
            }

            ElementRefBean formref = new ElementRefBean();
            formref.setElementDefOID(cvOID);
            formref.setMandatory(cvRequired ? "Yes" : "No");
            sedef.getFormRefs().add(formref);

            if (!cvIdPoses.containsKey(cvId)) {
                FormDefBean formdef = new FormDefBean();
                formdef.setOid(cvOID);
                formdef.setName(crfName + " - " + cvName);
                formdef.setRepeating("No");
                metadata.getFormDefs().add(formdef);
                cvIdPoses.put(cvId, metadata.getFormDefs().size() - 1);
                cvIds += cvId + ",";
                if (nullValue != null && nullValue.length() > 0) {
                    nullMap.put(cvId, nullValue);
                    nullValueCVs.put(sedOID + "-" + cvOID, nullValue);
                }
            }
        }
        cvIds = cvIds.substring(0, cvIds.length() - 1);
        metadata.setCvIds(cvIds);

        HashMap<Integer, Integer> maxLengths = new HashMap<Integer, Integer>();
        this.setItemDataMaxLengthTypesExpected();
        rows.clear();
        logger.debug("Begin to execute GetItemDataMaxLengths");
        rows = select(this.getItemDataMaxLengths(cvIds));
        it = rows.iterator();
        while (it.hasNext()) {
            HashMap row = (HashMap) it.next();
            maxLengths.put((Integer) row.get("item_id"), (Integer) row.get("max_length"));
        }

        this.setItemGroupAndItemMetaWithUnitTypesExpected();
        rows.clear();
        logger.debug("Begin to execute GetItemGroupAndItemMetaWithUnitSql");
        logger.debug("getItemGroupandItemMetaWithUnitsql= " + this.getItemGroupAndItemMetaWithUnitSql(cvIds));
        rows = select(this.getItemGroupAndItemMetaWithUnitSql(cvIds));
        it = rows.iterator();
        ArrayList<ItemGroupDefBean> itemGroupDefs = (ArrayList<ItemGroupDefBean>) metadata.getItemGroupDefs();
        ArrayList<ItemDefBean> itemDefs = (ArrayList<ItemDefBean>) metadata.getItemDefs();
        ArrayList<CodeListBean> codeLists = (ArrayList<CodeListBean>) metadata.getCodeLists();
        ArrayList<MultiSelectListBean> multiSelectLists = (ArrayList<MultiSelectListBean>) metadata.getMultiSelectLists();
        ArrayList<ElementRefBean> itemGroupRefs = new ArrayList<ElementRefBean>();
        Set<String> igset = new HashSet<String>();
        HashMap<String,Integer> igdPos = new HashMap<String,Integer>();
        Set<String> itset = new HashSet<String>();
        Set<Integer> itdset = new HashSet<Integer>();
        Set<Integer> clset = new HashSet<Integer>();
        Set<Integer> mslset = new HashSet<Integer>();
        ItemGroupDefBean igdef = new ItemGroupDefBean();
        HashMap<String, String> igMandatories = new HashMap<String, String>();
        boolean isLatest = false;
        int cvprev = -1;
        String igrprev = "";
        String igdprev = "";
        String itMandatory = "No";
        int itOrder = 0;
        Integer igId = -1;
        String sectionIds = ",";
        while (it.hasNext()) {
            HashMap row = (HashMap) it.next();
            Integer cId = (Integer) row.get("crf_id");
            Integer cvId = (Integer) row.get("crf_version_id");
            igId = (Integer) row.get("item_group_id");
            Integer itId = (Integer) row.get("item_id");
            Integer rsId = (Integer) row.get("response_set_id");
            String cvOID = (String) row.get("crf_version_oid");
            String igOID = (String) row.get("item_group_oid");
            String itOID = (String) row.get("item_oid");
            String igName = (String) row.get("item_group_name");
            String itName = (String) row.get("item_name");
            Integer dataTypeId = (Integer) row.get("item_data_type_id");
            Integer secId = (Integer) row.get("section_id");
            String header = (String) row.get("item_header");
            String left = (String) row.get("left_item_text");
            String right = (String) row.get("right_item_text");
            Boolean itRequired = (Boolean) row.get("item_required");
            String regexp = (String) row.get("regexp");
            String regexpErr = (String) row.get("regexp_error_msg");
            String widthDecimal = (String) row.get("width_decimal");
            Integer rsTypeId = (Integer) row.get("response_type_id");
            String rsText = (String) row.get("options_text");
            String rsValue = (String) row.get("options_values");
            String rsLabel = (String) row.get("response_label");
            String igHeader = (String) row.get("item_group_header");
            Boolean isRepeating = (Boolean)row.get("repeating_group");
            String itDesc = (String) row.get("item_description");
            String itQuesNum = (String) row.get("question_number_label");
            String muOid = (String) row.get("mu_oid");
            if (cvprev != cvId) {
                // at this point, itemGroupRefs is for old cvId
                if (itemGroupRefs != null && itemGroupRefs.size() > 0) {
                    String temp = igMandatories.containsKey(igdprev) ? igMandatories.get(igdprev) : itMandatory;
                    itemGroupRefs.get(itemGroupRefs.size() - 1).setMandatory(temp);
                }
                itMandatory = "No";
                // now update to new cvId
                cvprev = cvId;
                FormDefBean formDef = new FormDefBean();
                if (cvIdPoses.containsKey(cvId)) {
                    int p = cvIdPoses.get(cvId);
                    formDef = metadata.getFormDefs().get(p);
                } else {
                    logger.debug("crf_version_id=" + cvId + " couldn't find from studyEventAndFormMetaSql");
                }
                itemGroupRefs = (ArrayList<ElementRefBean>) formDef.getItemGroupRefs();
            }

            // mandatory is based on the last crf-version
            String igDefKey = igId + "";

            if (!igdprev.equals(igDefKey)) {
                if(igdPos.containsKey(igDefKey)) {
                    igdef = itemGroupDefs.get(igdPos.get(igDefKey));
                    isLatest = false;
                    itOrder = igdef.getItemRefs().size();
                } else {
                    igdef = new ItemGroupDefBean();
                    itOrder = 0;
                    igMandatories.put(igdprev, itMandatory);
                    isLatest = true;
                    igdef.setOid(igOID);
                    igdef.setName("ungrouped".equalsIgnoreCase(igName) ? igOID : igName);
                    //igdef.setName(igName);
                    
                    igdef.setRepeating(isRepeating ? "Yes" : "No");
                    
                    igdef.setComment(igHeader);
                    igdef.setPreSASDatasetName(igName.toUpperCase());
                    itemGroupDefs.add(igdef);
                    igdPos.put(igDefKey, itemGroupDefs.size()-1);
                }
                igdprev = igDefKey;
            }

            String igRefKey = igId + "-" + cvId;
            if (igrprev.equals(igRefKey)) {
                itMandatory = isLatest && itRequired && "No".equals(itMandatory) ? "Yes" : itMandatory;
            } else {
                if (!igset.contains(igRefKey)) {
                    igset.add(igRefKey);
                    ElementRefBean igref = new ElementRefBean();
                    igref.setElementDefOID(igOID);
                    int size = itemGroupRefs.size();
                    if (size > 0) {
                        String prev = igrprev.split("-")[0].trim();
                        String temp = igMandatories.containsKey(prev) ? igMandatories.get(prev) : itMandatory;
                        itemGroupRefs.get(size - 1).setMandatory(temp);
                    }
                    itemGroupRefs.add(igref);
                    itMandatory = "No";
                }
                igrprev = igRefKey;
            }

            String mandatory = itRequired ? "Yes" : "No";
            if (!itset.contains(igDefKey + "-" + itId)) {
                ++itOrder;
                itset.add(igDefKey + "-" + itId);
                ElementRefBean itemRef = new ElementRefBean();
                itemRef.setElementDefOID(itOID);
                if (itemRef.getMandatory() == null || itemRef.getMandatory().length() <= 0) {
                    itemRef.setMandatory(mandatory);
                }
                itemRef.setOrderNumber(itOrder);
                igdef.getItemRefs().add(itemRef);
            }

            boolean hasCode = MetadataUnit.needCodeList(rsTypeId, dataTypeId);
            LinkedHashMap<String, String> codes = new LinkedHashMap<String, String>();
            if (hasCode) {
                /*
                 * //null value will not be added to codelist if (nullMap.containsKey(cvId)) { codes = MetadataUnit.parseCode(rsText, rsValue,
                 * nullMap.get(cvId)); } else { codes = MetadataUnit.parseCode(rsText, rsValue); }
                 */
                codes = MetadataUnit.parseCode(rsText, rsValue);
                // no action has been taken if rsvalue/rstext go wrong,
                // since they have been validated when uploading crf.
            }

            boolean hasMultiSelect = MetadataUnit.needMultiSelectList(rsTypeId);
            LinkedHashMap<String, String> multi = new LinkedHashMap<String, String>();
            if (hasMultiSelect) {
                multi = MetadataUnit.parseCode(rsText, rsValue);
                // no action has been taken if rsvalue/rstext go wrong,
                // since they have been validated when uploading crf.
            }

            String datatype = MetadataUnit.getOdmItemDataType(rsTypeId, dataTypeId, odmVersion);
            if(sectionIds.contains(","+secId+",")) {
            }else {
                sectionIds += secId+",";
            }

            if (!itdset.contains(itId)) {
                itdset.add(itId);
                ItemDefBean idef = new ItemDefBean();
                idef.setOid(itOID);
                idef.setName(itName);
                idef.setComment(itDesc);
                if (muOid != null && muOid.length() > 0) {
                    ElementRefBean measurementUnitRef = new ElementRefBean();
                    measurementUnitRef.setElementDefOID(muOid);
                    idef.setMeasurementUnitRef(measurementUnitRef);
                }
                idef.setPreSASFieldName(itName);
                idef.setCodeListOID(hasCode ? "CL_" + rsId : "");
                if (hasMultiSelect) {
                    ElementRefBean multiSelectListRef = new ElementRefBean();
                    multiSelectListRef.setElementDefOID("MSL_" + rsId);
                    idef.setMultiSelectListRef(multiSelectListRef);
                }
                // if(nullMap.containsKey(cvId)) {
                // }
                idef.getQuestion().setQuestionNumber(itQuesNum);
                idef.getQuestion().getQuestion().setText(MetadataUnit.getItemQuestionText(header, left, right));
                if (regexp != null && regexp.startsWith("func:")) {
                    idef.setRangeCheck(MetadataUnit.getItemRangeCheck(regexp.substring(5).trim(), metadata.getSoftHard(), regexpErr, muOid));
                }
                idef.setDataType(datatype);
                int len = 0;
                int sig = 0;
                if (widthDecimal != null && widthDecimal.length() > 0) {
                    // now there is no validation for width_decimal here.
                    len = parseWidth(widthDecimal);
                    sig = parseDecimal(widthDecimal);
                }
                if (rsTypeId == 3 || rsTypeId == 7) {// 3:checkbox;
                    // //7:multi-select
                   // len = maxLengths.containsKey(itId) ? maxLengths.get(itId) : 0;
                    //len = Math.max(len, Math.max(rsText.length(), rsValue.length()));
                    Iterator<String> iter = multi.keySet().iterator();
                    String temp = "";
                    while (iter.hasNext()) {
                    temp += iter.next();
                    temp += ",";

                    }
                    idef.setLength(temp.length());
                } else if ("text".equalsIgnoreCase(datatype)) {
                    if (len > 0) {
                        idef.setLength(len);
                    } else {
                        idef.setLength(hasCode ? MetadataUnit.getDataTypeLength(codes.keySet()) : maxLengths.containsKey(itId) ? maxLengths.get(itId)
                            : MetaDataCollector.getTextLength());
                    }
                } else if ("integer".equalsIgnoreCase(datatype)) {
                    if (len > 0) {
                        idef.setLength(len);
                    } else {
                        idef.setLength(hasCode ? MetadataUnit.getDataTypeLength(codes.keySet()) : 10);
                    }
                } else if ("float".equalsIgnoreCase(datatype)) {
                    if (len > 0) {
                        idef.setLength(len);
                    } else {
                        // idef.setLength(hasCode ?
                        // MetadataUnit.getDataTypeLength(codes.keySet()) : 32);
                       idef.setLength(hasCode ? MetadataUnit.getDataTypeLength(codes.keySet()) : 25);
                    }
                } else {
                    idef.setLength(0);
                }
                idef.setSignificantDigits(sig > 0 ? sig : MetadataUnit.getSignificantDigits(datatype, codes.keySet(), hasCode));
                itemDefs.add(idef);
            }

            if (hasCode && !clset.contains(rsId)) {
                clset.add(rsId);
                CodeListBean cl = new CodeListBean();
                cl.setOid("CL_" + rsId);
                cl.setName(rsLabel);
                cl.setPreSASFormatName(rsLabel);
                cl.setDataType(datatype);
                Iterator<String> iter = codes.keySet().iterator();
                while (iter.hasNext()) {
                    String de = iter.next();
                    CodeListItemBean cli = new CodeListItemBean();
                    cli.setCodedValue(de);
                    TranslatedTextBean tt = cli.getDecode();
                    // cli.getDecode().setText(codes.get(de));
                    tt.setText(codes.get(de));
                    tt.setXmlLang(CoreResources.getField("translated_text_language"));
                    cli.setDecode(tt);
                    cl.getCodeListItems().add(cli);
                }
                codeLists.add(cl);
            }

            if (odmVersion.startsWith("oc") && hasMultiSelect && !mslset.contains(rsId)) {
                mslset.add(rsId);
                MultiSelectListBean msl = new MultiSelectListBean();
                msl.setOid("MSL_" + rsId);
                msl.setName(rsLabel);
                msl.setDataType(datatype);
                msl.setActualDataType(datatype);
                Iterator<String> iter = multi.keySet().iterator();
                while (iter.hasNext()) {
                    String de = iter.next();
                    MultiSelectListItemBean msli = new MultiSelectListItemBean();
                    msli.setCodedOptionValue(de);
                    TranslatedTextBean tt = new TranslatedTextBean();
                    String t = multi.get(de);
                    tt.setText(t);
                    tt.setXmlLang(CoreResources.getField("translated_text_language"));
                    msli.setDecode(tt);
                    msl.getMultiSelectListItems().add(msli);
                }
                multiSelectLists.add(msl);
            }
        }
        if (itemGroupRefs != null && itemGroupRefs.size() > 0) {
            String last = igMandatories.containsKey(igId + "") ? igMandatories.get(igId + "") : itMandatory;
            itemGroupRefs.get(itemGroupRefs.size() - 1).setMandatory(last);
        }
        sectionIds = sectionIds.length()>0?sectionIds.substring(1, sectionIds.length()-1):"";
        metadata.setSectionIds(sectionIds);
    }

    
    public void getODMMetadata(int parentStudyId,int crfVersionOID,MetaDataVersionBean metadata){
    	
    }
    /**
     * Metadata for ODM_1.2 OpenClinica extension and partial ODM_1.3 OpenClinica extension
     *
     * @param parentStudyId
     * @param studyId
     * @param metadata
     * @param odmVersion
     */
    public void getOCMetadata(int parentStudyId, int studyId, MetaDataVersionBean metadata, String odmVersion) {
        this.getODMMetadata(parentStudyId, studyId, metadata, odmVersion);
        String cvIds = metadata.getCvIds();
        if (odmVersion.startsWith("oc")) {
            // add CRFVersions that itemDef belong to
            HashMap<String, String> itDefCVs = new HashMap<String, String>();
            this.setItemCVOIDsTypesExpected();
            ArrayList al = this.select(this.getItemCVOIDsSql(cvIds));
            Iterator iter = al.iterator();
            while (iter.hasNext()) {
                HashMap row = (HashMap) iter.next();
                Integer cId = (Integer) row.get("crf_id");
                Integer cvId = (Integer) row.get("crf_version_id");
                Integer itId = (Integer) row.get("item_id");
                String cvOID = (String) row.get("cv_oid");
                String itOID = (String) row.get("item_oid");
                if (itDefCVs.containsKey(itOID)) {
                    String cvs = itDefCVs.get(itOID);
                    if (!cvs.contains(cvOID + ",")) {
                        cvs += cvOID + ",";
                    }
                    itDefCVs.put(itOID, cvs);
                } else {
                    itDefCVs.put(itOID, cvOID + ",");
                }
            }
            for (ItemDefBean itdef : metadata.getItemDefs()) {
                String key = itdef.getOid();
                if (itDefCVs.containsKey(key)) {
                    String cvs = itDefCVs.get(key);
                    itdef.setFormOIDs(cvs.substring(0, cvs.length() - 1));
                }
            }

            // add StudyGroupClassList
            this.setStudyGroupClassTypesExpected();
            logger.debug("Begin to execute GetStudyGroupClassSql");
            logger.debug("getStudyGroupClassSql=" + getStudyGroupClassSql(parentStudyId));
            ArrayList rows = select(this.getStudyGroupClassSql(parentStudyId));
            Iterator it = rows.iterator();
            ArrayList<StudyGroupClassListBean> sgcLists = (ArrayList<StudyGroupClassListBean>) metadata.getStudyGroupClassLists();
            String sgcprev = "";
            while (it.hasNext()) {
                HashMap row = (HashMap) it.next();
                Integer sgcid = (Integer) row.get("study_group_class_id");
                String sgcname = (String) row.get("sgc_name");
                String sgctype = (String) row.get("sgc_type");
                Integer statusid = (Integer) row.get("status_id");
                String subassign = (String) row.get("subject_assignment");
                String sgname = (String) row.get("sg_name");
                String des = (String) row.get("description");

                if (sgcprev.equals(sgcid + "")) {
                    StudyGroupItemBean sg = new StudyGroupItemBean();
                    sg.setName(sgname);
                    sg.setDescription(des);
                    StudyGroupClassListBean sgc = sgcLists.get(sgcLists.size() - 1);
                    sgc.getStudyGroupItems().add(sg);
                } else {
                    sgcprev = sgcid + "";
                    StudyGroupClassListBean sgc = new StudyGroupClassListBean();
                    sgc.setID("SGC_" + sgcid);
                    sgc.setName(sgcname);
                    sgc.setType(sgctype);
                    sgc.setStatus(Status.get(statusid).getName());
                    sgc.setSubjectAssignment(subassign);
                    StudyGroupItemBean sg = new StudyGroupItemBean();
                    sg.setName(sgname);
                    sg.setDescription(des);
                    sgc.getStudyGroupItems().add(sg);
                    sgcLists.add(sgc);
                }
            }
        }
        // return nullClSet;
    }

    
    
    
    public void getOCMetadataForGlobals(int crfId, MetaDataVersionBean metadata, String odmVersion) {
    
        String cvIds = crfId+"";
        if (odmVersion.startsWith("oc")) {
            // add CRFVersions that itemDef belong to
            HashMap<String, String> itDefCVs = new HashMap<String, String>();
            this.setItemCVOIDsTypesExpected();
            ArrayList al = this.select(this.getItemCVOIDsSql(cvIds));
            Iterator iter = al.iterator();
            while (iter.hasNext()) {
                HashMap row = (HashMap) iter.next();
                Integer cId = (Integer) row.get("crf_id");
                Integer cvId = (Integer) row.get("crf_version_id");
                Integer itId = (Integer) row.get("item_id");
                String cvOID = (String) row.get("cv_oid");
                String itOID = (String) row.get("item_oid");
                if (itDefCVs.containsKey(itOID)) {
                    String cvs = itDefCVs.get(itOID);
                    if (!cvs.contains(cvOID + ",")) {
                        cvs += cvOID + ",";
                    }
                    itDefCVs.put(itOID, cvs);
                } else {
                    itDefCVs.put(itOID, cvOID + ",");
                }
            }
            for (ItemDefBean itdef : metadata.getItemDefs()) {
                String key = itdef.getOid();
                if (itDefCVs.containsKey(key)) {
                    String cvs = itDefCVs.get(key);
                    itdef.setFormOIDs(cvs.substring(0, cvs.length() - 1));
                }
            }

            // add StudyGroupClassList
/*            this.setStudyGroupClassTypesExpected();
            logger.debug("Begin to execute GetStudyGroupClassSql");
            logger.debug("getStudyGroupClassSql=" + getStudyGroupClassSql(parentStudyId));
            ArrayList rows = select(this.getStudyGroupClassSql(parentStudyId));
            Iterator it = rows.iterator();
            ArrayList<StudyGroupClassListBean> sgcLists = (ArrayList<StudyGroupClassListBean>) metadata.getStudyGroupClassLists();
            String sgcprev = "";
            while (it.hasNext()) {
                HashMap row = (HashMap) it.next();
                Integer sgcid = (Integer) row.get("study_group_class_id");
                String sgcname = (String) row.get("sgc_name");
                String sgctype = (String) row.get("sgc_type");
                Integer statusid = (Integer) row.get("status_id");
                String subassign = (String) row.get("subject_assignment");
                String sgname = (String) row.get("sg_name");
                String des = (String) row.get("description");

                if (sgcprev.equals(sgcid + "")) {
                    StudyGroupItemBean sg = new StudyGroupItemBean();
                    sg.setName(sgname);
                    sg.setDescription(des);
                    StudyGroupClassListBean sgc = sgcLists.get(sgcLists.size() - 1);
                    sgc.getStudyGroupItems().add(sg);
                } else {
                    sgcprev = sgcid + "";
                    StudyGroupClassListBean sgc = new StudyGroupClassListBean();
                    sgc.setID("SGC_" + sgcid);
                    sgc.setName(sgcname);
                    sgc.setType(sgctype);
                    sgc.setStatus(Status.get(statusid).getName());
                    sgc.setSubjectAssignment(subassign);
                    StudyGroupItemBean sg = new StudyGroupItemBean();
                    sg.setName(sgname);
                    sg.setDescription(des);
                    sgc.getStudyGroupItems().add(sg);
                    sgcLists.add(sgc);
                }
            }*/
        }
        // return nullClSet;
    }

  
    
    
    public void getStudyEventAndFormMetaOC1_3(int parentStudyId, int studyId, MetaDataVersionBean metadata, String odmVersion, boolean isIncludedSite) {
        ArrayList<StudyEventDefBean> seds = (ArrayList<StudyEventDefBean>)metadata.getStudyEventDefs();
        ArrayList<FormDefBean> forms = (ArrayList<FormDefBean>)metadata.getFormDefs();
        HashMap<String, EventDefinitionDetailsBean> sedDetails = new HashMap<String, EventDefinitionDetailsBean>();
        HashMap<String, FormDetailsBean> formDetails = new HashMap<String, FormDetailsBean>();
        this.setStudyEventAndFormMetaOC1_3TypesExpected();
        logger.debug("Begin to execute GetStudyEventAndFormMetaOC1_3Sql");
        logger.debug("getStudyEventAndFormMetaOC1_3SQl= " + this.getStudyEventAndFormMetaOC1_3Sql(parentStudyId, studyId, isIncludedSite));
        ArrayList rows = this.select(this.getStudyEventAndFormMetaOC1_3Sql(parentStudyId, studyId, isIncludedSite));
        Iterator iter = rows.iterator();
        String sedOIDs = "";
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            Integer cvId = (Integer) row.get("crf_version_id");
            String sedOID = (String) row.get("definition_oid");
            String cvOID = (String) row.get("cv_oid");
            String sedDesc = (String) row.get("description");
            String category = (String) row.get("category");
            String cvDesc = (String) row.get("version_description");
            String notes = (String) row.get("revision_notes");
            String cOID = (String) row.get("crf_oid");
            String nullValue = (String) row.get("null_values");
            Integer dvId = (Integer) row.get("default_version_id");
            Boolean pwdRequired = (Boolean) row.get("electronic_signature");
            Boolean doubleEntry = (Boolean) row.get("double_entry");
            Boolean hideCrf = (Boolean) row.get("hide_crf");
            Boolean participantForm = (Boolean) row.get("participant_form");
            Boolean allowAnonymousSubmission = (Boolean) row.get("allow_anonymous_submission");
            String submissionUrl = (String) row.get("submission_url");
            Integer sdvId = (Integer) row.get("source_data_verification_code");
            Boolean offline = (Boolean) row.get("active");

            if(sedDetails.containsKey(sedOID)) {
            } else {
                EventDefinitionDetailsBean sedDetail = new EventDefinitionDetailsBean();
                sedDetail.setOid(sedOID);
                sedDetail.setCategory(category);
                sedDetail.setDescription(sedDesc);
                sedDetails.put(sedOID, sedDetail);
            }

            if(formDetails.containsKey(cvOID)) {
          
            	FormDetailsBean formDetail = formDetails.get(cvOID);
            	PresentInEventDefinitionBean p = new PresentInEventDefinitionBean();
                p.setStudyEventOid(sedOID);
                p.setDoubleDataEntry(doubleEntry==false?"No":"Yes");
                p.setHideCrf(hideCrf==false?"No":"Yes");
                p.setParticipantForm(participantForm==false?"No":"Yes");
                p.setIsDefaultVersion(cvId.equals(dvId)?"Yes":"No");
                p.setNullValues(nullValue);
                p.setPasswordRequired(pwdRequired==false?"No":"Yes");
                p.setAllowAnonymousSubmission(allowAnonymousSubmission==false?"No":"Yes");
                p.setOffline((offline==false) ?"No":"Yes");
                p.setSubmissionUrl(submissionUrl);
                p.setSourceDataVerification(SourceDataVerification.getByCode(sdvId > 0 ? sdvId : 3).getDescription());
                formDetail.getPresentInEventDefinitions().add(p);
            }else {
                FormDetailsBean formDetail = new FormDetailsBean();
                formDetail.setOid(cvOID);
                formDetail.setRevisionNotes(notes);
                formDetail.setParentFormOid(cOID);
                formDetail.setVersionDescription(cvDesc);
                //ArrayList sectionBeansRows = this.select(this.getSectionDetails(cvOID),cvId);
               formDetail =  setSectionBean(formDetail,cvId);
               PresentInEventDefinitionBean p = new PresentInEventDefinitionBean();
               p.setStudyEventOid(sedOID);
               p.setDoubleDataEntry(doubleEntry==false?"No":"Yes");
               p.setHideCrf(hideCrf==false?"No":"Yes");
               p.setParticipantForm(participantForm==false?"No":"Yes");
               p.setIsDefaultVersion(cvId.equals(dvId)?"Yes":"No");
               p.setNullValues(nullValue);
               p.setPasswordRequired(pwdRequired==false?"No":"Yes");
               p.setAllowAnonymousSubmission(allowAnonymousSubmission == false?"No":"Yes");
               p.setOffline((offline==false) ?"No":"Yes");
              p.setSubmissionUrl(submissionUrl);

               p.setSourceDataVerification(SourceDataVerification.getByCode(sdvId > 0 ? sdvId : 3).getDescription());
               formDetail.getPresentInEventDefinitions().add(p);
       
                formDetails.put(cvOID, formDetail);
            }
        }
        for(StudyEventDefBean sedef : seds) {
            sedef.setEventDefinitionDetais(sedDetails.get(sedef.getOid()));
        }
        for(FormDefBean formdef : forms) {
            formdef.setFormDetails(formDetails.get(formdef.getOid()));
        }
    }

    
    private FormDetailsBean setSectionBean(FormDetailsBean formDetail,Integer crfVId){
    	
    	HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfVId));
        ArrayList<SectionDetails>sectionBeans = new ArrayList<SectionDetails>();
        
        SectionDAO secdao = new SectionDAO(this.ds);
        ArrayList sections = secdao.findAllByCRFVersionId (crfVId);
    	Iterator iter = sections.iterator();
    	 while(iter.hasNext()){
    		 SectionDetails sectionDetails = new SectionDetails();
    		 SectionBean sectionBean = (SectionBean) iter.next();
    		 sectionDetails.setSectionId(sectionBean.getId());
    		 sectionDetails.setSectionLabel(sectionBean.getLabel());
    		 sectionDetails.setSectionTitle(sectionBean.getTitle());
    		 sectionDetails.setSectionSubtitle(sectionBean.getSubtitle());
    		 sectionDetails.setSectionInstructions(sectionBean.getInstructions());
    		 sectionDetails.setSectionPageNumber(sectionBean.getPageNumberLabel());
    		 
    		 sectionBeans.add(sectionDetails);
    	 }
    	 formDetail.setSectionDetails(sectionBeans);
    	 return formDetail;
    }
    public void getMetadataOC1_3(int parentStudyId, int studyId, MetaDataVersionBean metadata, String odmVersion) {
        this.getOCMetadata(parentStudyId, studyId, metadata, odmVersion);

        //StudyBean study = metadata.getStudy();
        //if(study.getId()>0) {
        //} else {
        //    StudyDAO sdao = new StudyDAO(this.ds);
        //    study = (StudyBean)sdao.findByPK(studyId);
        //}
        //StudyConfigService studyConfig = new StudyConfigService(this.ds);
        //study = studyConfig.setParametersForStudy(study);


        this.getStudyEventAndFormMetaOC1_3(parentStudyId, studyId, metadata, odmVersion, false);

        String cvIds = metadata.getCvIds();
        ArrayList<ItemGroupDefBean> igs = (ArrayList<ItemGroupDefBean>)metadata.getItemGroupDefs();
        HashMap<String,Integer> igPoses = getItemGroupOIDPos(metadata);
        ArrayList<ItemDefBean> its = (ArrayList<ItemDefBean>)metadata.getItemDefs();
        HashMap<String,Integer> itPoses = getItemOIDPos(metadata);
        HashMap<String,Integer> inPoses = new HashMap<String, Integer>();
        ItemGroupDefBean ig = new ItemGroupDefBean();
        ItemDefBean it = new ItemDefBean();
        String prevCvIg = "";
        HashMap<Integer,String> sectionLabels = this.getSectionLabels(metadata.getSectionIds());
        HashMap<Integer,String> parentItemOIDs = this.getParentItemOIDs(cvIds);
        this.setItemGroupAndItemMetaOC1_3TypesExpected();
        logger.debug("Begin to execute GetItemGroupAndItemMetaWithUnitSql");
        logger.debug("getItemGroupandItemMetaWithUnitsql= " + this.getItemGroupAndItemMetaOC1_3Sql(cvIds));
        ArrayList rows = select(this.getItemGroupAndItemMetaOC1_3Sql(cvIds));
        Iterator iter = rows.iterator();
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            //Integer cId = (Integer) row.get("crf_id");
            Integer cvId = (Integer) row.get("crf_version_id");
            Integer igId = (Integer) row.get("item_group_id");
            //Integer itId = (Integer) row.get("item_id");
            //Integer rsId = (Integer) row.get("response_set_id");
            String cvOID = (String) row.get("crf_version_oid");
            String igOID = (String) row.get("item_group_oid");
            String itOID = (String) row.get("item_oid");

            Integer igRepeatNum = (Integer) row.get("repeat_number");
            Integer igRepeatMax = (Integer) row.get("repeat_max");
            Boolean showGroup = (Boolean) row.get("show_group");
            String itemGroupHeader = (String)row.get("item_group_header");
            
            String itHeader = (String) row.get("item_header");
            String left = (String) row.get("left_item_text");
            String right = (String) row.get("right_item_text");
            String itSubheader = (String) row.get("subheader");
            Integer itSecId = (Integer) row.get("section_id");
            Integer itPId = (Integer) row.get("parent_id");
            Integer itColNum = (Integer) row.get("column_number");
            String itpgNum = (String) row.get("page_number_label");
            String layout = (String) row.get("response_layout");
            Integer rsTypeId = (Integer) row.get("response_type_id");
            String dfValue = (String) row.get("default_value");
            Boolean phi = (Boolean) row.get("phi_status");
            Boolean showItem = (Boolean) row.get("show_item");
            Integer orderInForm = (Integer)row.get("item_order");
            if((cvId+"-"+igId).equals(prevCvIg)) {
            } else {
                prevCvIg = cvId+"-"+igId;
                ig = igs.get(igPoses.get(igOID));
                ItemGroupDetailsBean igDetail = ig.getItemGroupDetails();
                igDetail.setOid(igOID);
                PresentInFormBean inForm = new PresentInFormBean();
                inForm.setFormOid(cvOID);
                ItemGroupRepeatBean igr = new ItemGroupRepeatBean();
                igr.setRepeatMax(igRepeatMax);
                igr.setRepeatNumber(igRepeatNum);
                inForm.setItemGroupRepeatBean(igr);
                inForm.setShowGroup(showGroup==true?"Yes":"No");
                inForm.setItemGroupHeader(itemGroupHeader);
                igDetail.getPresentInForms().add(inForm);
            }

            it = its.get(itPoses.get(itOID));
            ItemDetailsBean itDetail = it.getItemDetails();
            itDetail.setOid(itOID);
            ItemPresentInFormBean itInForm = new ItemPresentInFormBean();
            itInForm.setFormOid(cvOID);
            itInForm.setColumnNumber(itColNum);
            itInForm.setDefaultValue(dfValue);
            itInForm.setItemHeader(itHeader);
            itInForm.setLeftItemText(left);
            itInForm.setRightItemText(right);
            itInForm.setItemSubHeader(itSubheader);
            itInForm.setPageNumber(itpgNum);
            itInForm.setParentItemOid(parentItemOIDs.get(itPId));
            itInForm.setSectionLabel(sectionLabels.get(itSecId));
            itInForm.setPhi(phi==false?"No":"Yes");
            itInForm.setOrderInForm(orderInForm);
            itInForm.setShowItem(showItem==true?"Yes":"No");
            ItemResponseBean itemResponse = new ItemResponseBean();
            itemResponse.setResponseLayout(layout);
            itemResponse.setResponseType(ResponseType.get(rsTypeId).getName());
            itInForm.setItemResponse(itemResponse);
            itDetail.getItemPresentInForm().add(itInForm);
            inPoses.put(itOID+"-"+cvOID, itDetail.getItemPresentInForm().size()-1);
        }

        this.getSCDs(cvIds, its, itPoses, inPoses);
    }

    protected void getSCDs(String crfVersionIds, ArrayList<ItemDefBean> its, HashMap<String,Integer> itPoses, HashMap<String,Integer> inFormPoses) {
        logger.debug("Begin to execute getSCDsSql");
        this.setSCDsTypesExpected();
        ArrayList rows = this.select(this.getSCDsSql(crfVersionIds));
        if(rows==null || rows.size()<1) {
            logger.info("OdmExtracDAO.getSCDsSql returns no rows.");
        } else {
            Iterator iter = rows.iterator();
            while(iter.hasNext()) {
                HashMap row = (HashMap) iter.next();
                String cvOID = (String) row.get("crf_version_oid");
                String itOID = (String) row.get("item_oid");
                String controlItemName = (String) row.get("control_item_name");
                String option = (String) row.get("option_value");
                String message = (String) row.get("message");

                if(controlItemName!=null && controlItemName.length()>0 && option!=null && option.length()>0
                        &&message!=null && message.length()>0) {
                    SimpleConditionalDisplayBean scd = new SimpleConditionalDisplayBean();
                    scd.setControlItemName(controlItemName);
                    scd.setOptionValue(option);
                    scd.setMessage(message);
                    if(itPoses.containsKey(itOID) && inFormPoses.containsKey(itOID+"-"+cvOID)) {
                        its.get(itPoses.get(itOID)).getItemDetails().getItemPresentInForm().get(inFormPoses.get(itOID+"-"+cvOID)).setSimpleConditionalDisplay(scd);
                    } else {
                        logger.info("There is no <ItemDef> with item_oid="+itOID+" or has <ItemPresentInForm> with FormOID="+cvOID+".");
                    }
                }else {
                    logger.info("No Simple Conditional Display added for <ItemDef> with crf_version_oid="+cvOID+" and item_oid="+itOID);
                }
            }
        }
    }
private void fetchItemGroupMetaData(MetaDataVersionBean metadata,String cvIds, String odmVersion)

{
	  ArrayList rows = select(this.getItemDataMaxLengths(cvIds));
      Iterator it = rows.iterator();
      HashMap maxLengths = null;
//	while (it.hasNext()) {
//          HashMap row = (HashMap) it.next();
//          maxLengths.put((Integer) row.get("item_id"), (Integer) row.get("max_length"));
//      }

      this.setItemGroupAndItemMetaWithUnitTypesExpected();
      rows.clear();
      logger.debug("Begin to execute GetItemGroupAndItemMetaWithUnitSql");
      logger.debug("getItemGroupandItemMetaWithUnitsql= " + this.getItemGroupAndItemMetaWithUnitSql(cvIds));
      rows = select(this.getItemGroupAndItemMetaWithUnitSql(cvIds));
      it = rows.iterator();
      ArrayList<ItemGroupDefBean> itemGroupDefs = (ArrayList<ItemGroupDefBean>) metadata.getItemGroupDefs();
      ArrayList<ItemDefBean> itemDefs = (ArrayList<ItemDefBean>) metadata.getItemDefs();
      ArrayList<CodeListBean> codeLists = (ArrayList<CodeListBean>) metadata.getCodeLists();
      ArrayList<MultiSelectListBean> multiSelectLists = (ArrayList<MultiSelectListBean>) metadata.getMultiSelectLists();
      ArrayList<ElementRefBean> itemGroupRefs = new ArrayList<ElementRefBean>();
      Set<String> igset = new HashSet<String>();
      HashMap<String,Integer> igdPos = new HashMap<String,Integer>();
      Set<String> itset = new HashSet<String>();
      Set<Integer> itdset = new HashSet<Integer>();
      Set<Integer> clset = new HashSet<Integer>();
      Set<Integer> mslset = new HashSet<Integer>();
      ItemGroupDefBean igdef = new ItemGroupDefBean();
      HashMap<String, String> igMandatories = new HashMap<String, String>();
      boolean isLatest = false;
      int cvprev = -1;
      String igrprev = "";
      String igdprev = "";
      String itMandatory = "No";
      int itOrder = 0;
      Integer igId = -1;
      String sectionIds = ",";
      while (it.hasNext()) {
          HashMap row = (HashMap) it.next();
          Integer cId = (Integer) row.get("crf_id");
          Integer cvId = (Integer) row.get("crf_version_id");
          igId = (Integer) row.get("item_group_id");
          Integer itId = (Integer) row.get("item_id");
          Integer rsId = (Integer) row.get("response_set_id");
          String cvOID = (String) row.get("crf_version_oid");
          String igOID = (String) row.get("item_group_oid");
          String itOID = (String) row.get("item_oid");
          String igName = (String) row.get("item_group_name");
          String itName = (String) row.get("item_name");
          Integer dataTypeId = (Integer) row.get("item_data_type_id");
          Integer secId = (Integer) row.get("section_id");
          String header = (String) row.get("item_header");
          String left = (String) row.get("left_item_text");
          String right = (String) row.get("right_item_text");
          Boolean itRequired = (Boolean) row.get("item_required");
          String regexp = (String) row.get("regexp");
          String regexpErr = (String) row.get("regexp_error_msg");
          String widthDecimal = (String) row.get("width_decimal");
          Integer rsTypeId = (Integer) row.get("response_type_id");
          String rsText = (String) row.get("options_text");
          String rsValue = (String) row.get("options_values");
          String rsLabel = (String) row.get("response_label");
          String igHeader = (String) row.get("item_group_header");
          
          Boolean isRepeating = (Boolean)row.get("repeating_group");
          String itDesc = (String) row.get("item_description");
          String itQuesNum = (String) row.get("question_number_label");
          String muOid = (String) row.get("mu_oid");
          if (cvprev != cvId) {
              // at this point, itemGroupRefs is for old cvId
              if (itemGroupRefs != null && itemGroupRefs.size() > 0) {
                  String temp = igMandatories.containsKey(igdprev) ? igMandatories.get(igdprev) : itMandatory;
                  itemGroupRefs.get(itemGroupRefs.size() - 1).setMandatory(temp);
              }
              itMandatory = "No";
              // now update to new cvId
              cvprev = cvId;
              FormDefBean formDef = new FormDefBean();
              HashMap cvIdPoses = null;
//			if (cvIdPoses.containsKey(cvId)) {
//                  int p = (Integer) cvIdPoses.get(cvId);
//                  formDef = metadata.getFormDefs().get(p);
//              } else {
//                  logger.debug("crf_version_id=" + cvId + " couldn't find from studyEventAndFormMetaSql");
//              }
              itemGroupRefs = (ArrayList<ElementRefBean>) formDef.getItemGroupRefs();
          }

          // mandatory is based on the last crf-version
          String igDefKey = igId + "";

          if (!igdprev.equals(igDefKey)) {
              if(igdPos.containsKey(igDefKey)) {
                  igdef = itemGroupDefs.get(igdPos.get(igDefKey));
                  isLatest = false;
                  itOrder = igdef.getItemRefs().size();
              } else {
                  igdef = new ItemGroupDefBean();
                  itOrder = 0;
                  igMandatories.put(igdprev, itMandatory);
                  isLatest = true;
                  igdef.setOid(igOID);
                  igdef.setName("ungrouped".equalsIgnoreCase(igName) ? igOID : igName);
                  //igdef.setName(igName);
                  igdef.setRepeating(isRepeating ? "Yes" : "No");
                  igdef.setComment(igHeader);
                  igdef.setPreSASDatasetName(igName.toUpperCase());
                  itemGroupDefs.add(igdef);
                  igdPos.put(igDefKey, itemGroupDefs.size()-1);
              }
              igdprev = igDefKey;
          }

          String igRefKey = igId + "-" + cvId;
          if (igrprev.equals(igRefKey)) {
              itMandatory = isLatest && itRequired && "No".equals(itMandatory) ? "Yes" : itMandatory;
          } else {
              if (!igset.contains(igRefKey)) {
                  igset.add(igRefKey);
                  ElementRefBean igref = new ElementRefBean();
                  igref.setElementDefOID(igOID);
                  int size = itemGroupRefs.size();
                  if (size > 0) {
                      String prev = igrprev.split("-")[0].trim();
                      String temp = igMandatories.containsKey(prev) ? igMandatories.get(prev) : itMandatory;
                      itemGroupRefs.get(size - 1).setMandatory(temp);
                  }
                  itemGroupRefs.add(igref);
                  itMandatory = "No";
              }
              igrprev = igRefKey;
          }

          String mandatory = itRequired ? "Yes" : "No";
          if (!itset.contains(igDefKey + "-" + itId)) {
              ++itOrder;
              itset.add(igDefKey + "-" + itId);
              ElementRefBean itemRef = new ElementRefBean();
              itemRef.setElementDefOID(itOID);
              if (itemRef.getMandatory() == null || itemRef.getMandatory().length() <= 0) {
                  itemRef.setMandatory(mandatory);
              }
              itemRef.setOrderNumber(itOrder);
              igdef.getItemRefs().add(itemRef);
          }

          boolean hasCode = MetadataUnit.needCodeList(rsTypeId, dataTypeId);
          LinkedHashMap<String, String> codes = new LinkedHashMap<String, String>();
          if (hasCode) {
              /*
               * //null value will not be added to codelist if (nullMap.containsKey(cvId)) { codes = MetadataUnit.parseCode(rsText, rsValue,
               * nullMap.get(cvId)); } else { codes = MetadataUnit.parseCode(rsText, rsValue); }
               */
              codes = MetadataUnit.parseCode(rsText, rsValue);
              // no action has been taken if rsvalue/rstext go wrong,
              // since they have been validated when uploading crf.
          }

          boolean hasMultiSelect = MetadataUnit.needMultiSelectList(rsTypeId);
          LinkedHashMap<String, String> multi = new LinkedHashMap<String, String>();
          if (hasMultiSelect) {
              multi = MetadataUnit.parseCode(rsText, rsValue);
              // no action has been taken if rsvalue/rstext go wrong,
              // since they have been validated when uploading crf.
          }

          String datatype = MetadataUnit.getOdmItemDataType(rsTypeId, dataTypeId, odmVersion);
          if(sectionIds.contains(","+secId+",")) {
          }else {
              sectionIds += secId+",";
          }

          if (!itdset.contains(itId)) {
              itdset.add(itId);
              ItemDefBean idef = new ItemDefBean();
              idef.setOid(itOID);
              idef.setName(itName);
              idef.setComment(itDesc);
              if (muOid != null && muOid.length() > 0) {
                  ElementRefBean measurementUnitRef = new ElementRefBean();
                  measurementUnitRef.setElementDefOID(muOid);
                  idef.setMeasurementUnitRef(measurementUnitRef);
              }
              idef.setPreSASFieldName(itName);
              idef.setCodeListOID(hasCode ? "CL_" + rsId : "");
              if (hasMultiSelect) {
                  ElementRefBean multiSelectListRef = new ElementRefBean();
                  multiSelectListRef.setElementDefOID("MSL_" + rsId);
                  idef.setMultiSelectListRef(multiSelectListRef);
              }
              // if(nullMap.containsKey(cvId)) {
              // }
              idef.getQuestion().setQuestionNumber(itQuesNum);
              idef.getQuestion().getQuestion().setText(MetadataUnit.getItemQuestionText(header, left, right));
              if (regexp != null && regexp.startsWith("func:")) {
                  idef.setRangeCheck(MetadataUnit.getItemRangeCheck(regexp.substring(5).trim(), metadata.getSoftHard(), regexpErr, muOid));
              }
              idef.setDataType(datatype);
              int len = 0;
              int sig = 0;
              if (widthDecimal != null && widthDecimal.length() > 0) {
                  // now there is no validation for width_decimal here.
                  len = parseWidth(widthDecimal);
                  sig = parseDecimal(widthDecimal);
              }
              if (rsTypeId == 3 || rsTypeId == 7) {// 3:checkbox;
                  // //7:multi-select
                 // len = maxLengths.containsKey(itId) ? maxLengths.get(itId) : 0;
                  //len = Math.max(len, Math.max(rsText.length(), rsValue.length()));
                  Iterator<String> iter = multi.keySet().iterator();
                  String temp = "";
                  while (iter.hasNext()) {
                  temp += iter.next();
                  temp += ",";

                  }
                  idef.setLength(temp.length());
              } else if ("text".equalsIgnoreCase(datatype)) {
                  if (len > 0) {
                      idef.setLength(len);
                  } else {
                     //idef.setLength((Integer) (hasCode ? MetadataUnit.getDataTypeLength(codes.keySet()) : maxLengths.containsKey(itId) ? maxLengths.get(itId)            : MetaDataCollector.getTextLength()));
                     
                	  idef.setLength(0);
                  }
              } else if ("integer".equalsIgnoreCase(datatype)) {
                  if (len > 0) {
                      idef.setLength(len);
                  } else {
                      idef.setLength(hasCode ? MetadataUnit.getDataTypeLength(codes.keySet()) : 10);
                  }
              } else if ("float".equalsIgnoreCase(datatype)) {
                  if (len > 0) {
                      idef.setLength(len);
                  } else {
                      // idef.setLength(hasCode ?
                      // MetadataUnit.getDataTypeLength(codes.keySet()) : 32);
                     idef.setLength(hasCode ? MetadataUnit.getDataTypeLength(codes.keySet()) : 25);
                  }
              } else {
                  idef.setLength(0);
              }
              idef.setSignificantDigits(sig > 0 ? sig : MetadataUnit.getSignificantDigits(datatype, codes.keySet(), hasCode));
              itemDefs.add(idef);
          }

          if (hasCode && !clset.contains(rsId)) {
              clset.add(rsId);
              CodeListBean cl = new CodeListBean();
              cl.setOid("CL_" + rsId);
              cl.setName(rsLabel);
              cl.setPreSASFormatName(rsLabel);
              cl.setDataType(datatype);
              Iterator<String> iter = codes.keySet().iterator();
              while (iter.hasNext()) {
                  String de = iter.next();
                  CodeListItemBean cli = new CodeListItemBean();
                  cli.setCodedValue(de);
                  TranslatedTextBean tt = cli.getDecode();
                  // cli.getDecode().setText(codes.get(de));
                  tt.setText(codes.get(de));
                  tt.setXmlLang(CoreResources.getField("translated_text_language"));
                  cli.setDecode(tt);
                  cl.getCodeListItems().add(cli);
              }
              codeLists.add(cl);
          }

          if (odmVersion.startsWith("oc") && hasMultiSelect && !mslset.contains(rsId)) {
              mslset.add(rsId);
              MultiSelectListBean msl = new MultiSelectListBean();
              msl.setOid("MSL_" + rsId);
              msl.setName(rsLabel);
              msl.setDataType(datatype);
              msl.setActualDataType(datatype);
              Iterator<String> iter = multi.keySet().iterator();
              while (iter.hasNext()) {
                  String de = iter.next();
                  MultiSelectListItemBean msli = new MultiSelectListItemBean();
                  msli.setCodedOptionValue(de);
                  TranslatedTextBean tt = new TranslatedTextBean();
                  String t = multi.get(de);
                  tt.setText(t);
                  tt.setXmlLang(CoreResources.getField("translated_text_language"));
                  msli.setDecode(tt);
                  msl.getMultiSelectListItems().add(msli);
              }
              multiSelectLists.add(msl);
          }
      }
      if (itemGroupRefs != null && itemGroupRefs.size() > 0) {
          String last = igMandatories.containsKey(igId + "") ? igMandatories.get(igId + "") : itMandatory;
          itemGroupRefs.get(itemGroupRefs.size() - 1).setMandatory(last);
      }
      sectionIds = sectionIds.length()>0?sectionIds.substring(1, sectionIds.length()-1):"";
      metadata.setSectionIds(sectionIds);
}
    public int parseWidth(String widthDecimal) {
        String w = "";
        widthDecimal = widthDecimal.trim();
        if (widthDecimal.startsWith("(")) {
        } else if (widthDecimal.contains("(")) {
            w = widthDecimal.split("\\(")[0];
        } else {
            w = widthDecimal;
        }
        if (w.length() > 0) {
            return "w".equalsIgnoreCase(w) ? 0 : Integer.parseInt(w);
        }
        return 0;
    }

    public int parseDecimal(String widthDecimal) {
        String d = "";
        widthDecimal = widthDecimal.trim();
        if (widthDecimal.startsWith("(")) {
            d = widthDecimal.substring(1, widthDecimal.length() - 1);
        } else if (widthDecimal.contains("(")) {
            d = widthDecimal.split("\\(")[1].trim();
            d = d.substring(0, d.length() - 1);

        }
        if (d.length() > 0) {
            return "d".equalsIgnoreCase(d) ? 0 : Integer.parseInt(d);
        }
        return 0;
    }

    public void getAdminData(StudyBean study, DatasetBean dataset, OdmAdminDataBean data, String odmVersion) {
        String dbName = CoreResources.getDBName();
        this.setStudyUsersTypesExpected();
        ArrayList rows = this.select(this.getStudyUsersSql(study.getId() + ""));
        Iterator it = rows.iterator();
        while (it.hasNext()) {
            HashMap row = (HashMap) it.next();
            Integer userId = (Integer) row.get("user_id");
            String firstName = (String) row.get("first_name");
            String lastName = (String) row.get("last_name");
            String organization = (String) row.get("institutional_affiliation");

            UserBean user = new UserBean();
            user.setOid("USR_" + userId);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setOrganization(organization);
            data.getUsers().add(user);
        }
        // LocationBean loc = new LocationBean();
        // loc.setOid("LOC_"+study.getOid());
        // loc.setName(study.getName());
        MetaDataVersionRefBean meta = new MetaDataVersionRefBean();
        meta.setElementDefOID(data.getMetaDataVersionOID());
        meta.setStudyOID(study.getOid());
        meta.setEffectiveDate(study.getCreatedDate());
        // loc.setMetaDataVersionRef(meta);
        // data.getLocations().add(loc);
    }

    protected HashMap<String, String> getNullValueCVs(StudyBean study) {
        HashMap<String, String> nullValueCVs = new HashMap<String, String>();
        int studyId = study.getId();
        this.setNullValueCVsTypesExpected();
        ArrayList viewRows = new ArrayList();
        if (study.getParentStudyId() > 0) {
            viewRows = select(this.getNullValueCVsSql(studyId + ""));
            if (viewRows.size() <= 0) {
                viewRows = select(this.getNullValueCVsSql(study.getParentStudyId() + ""));
            }
        } else {
            viewRows = select(this.getNullValueCVsSql(studyId + ""));
        }
        Iterator iter = viewRows.iterator();
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            String sedOID = (String) row.get("definition_oid");
            String cvOID = (String) row.get("crf_version_oid");
            String nullValues = (String) row.get("null_values");
            nullValueCVs.put(studyId + "-" + sedOID + "-" + cvOID, nullValues);
        }
        return nullValueCVs;
    }

    public void getClinicalData(StudyBean study, DatasetBean dataset, OdmClinicalDataBean data, String odmVersion, String studySubjectIds, String odmType) {
        String dbName = CoreResources.getDBName();
        String subprev = "";
        HashMap<String, Integer> sepos = new HashMap<String, Integer>();
        String seprev = "";
        String formprev = "";
        HashMap<String, Integer> igpos = new HashMap<String, Integer>();
        String igprev = "";
        String oidPos = "";
        HashMap<Integer, String> oidPoses = new HashMap<Integer, String>();
        HashMap<Integer, String> idataOidPoses = new HashMap<Integer, String>();

        // String studyIds = "";
        // if (study.getParentStudyId() > 0) {
        // studyIds += study.getId();
        // } else {
        // ArrayList<Integer> ids = (ArrayList<Integer>) (new
        // StudyDAO(this.ds)).findAllSiteIdsByStudy(study);
        // for (int i = 0; i < ids.size() - 1; ++i) {
        // studyIds += ids.get(i) + ",";
        // }
        // studyIds += ids.get(ids.size() - 1);
        // }

        String studyIds = study.getId() + "";
        int datasetItemStatusId = dataset.getDatasetItemStatus().getId();
        String sql = dataset.getSQLStatement().split("order by")[0].trim();
        sql = sql.split("study_event_definition_id in")[1];
        String[] ss = sql.split("and item_id in");
        String sedIds = ss[0];
        String[] sss = ss[1].split("and");
        String itemIds = sss[0];
        String dateConstraint = "";
        if ("postgres".equalsIgnoreCase(dbName)) {
            dateConstraint = "and " + sss[1] + " and " + sss[2];
            dateConstraint = dateConstraint.replace("date_created", "ss.enrollment_date");
        } else if ("oracle".equalsIgnoreCase(dbName)) {
            String[] os = (sss[1] + sss[2]).split("'");
            dateConstraint = "and trunc(ss.enrollment_date) >= to_date('" + os[1] + "') and trunc(ss.enrollment_date) <= to_date('" + os[3] + "')";
        }
        logger.debug("Begin to GetSubjectEventFormSql");
        if (odmVersion.startsWith("oc")) {
            logger.debug("getOCSubjectEventFormSql="
                + getOCSubjectEventFormSqlSS(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId, studySubjectIds));
            this.setSubjectEventFormDataTypesExpected(odmVersion);
            ArrayList viewRows = select(getOCSubjectEventFormSqlSS(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId, studySubjectIds));
            Iterator iter = viewRows.iterator();
            this.setDataWithOCAttributes(study, dataset, data, odmVersion, iter, oidPoses, odmType);
        } else {
            logger.debug("getSubjectEventFormSql=" + getSubjectEventFormSqlSS(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId, studySubjectIds));
            this.setSubjectEventFormDataTypesExpected();
            ArrayList viewRows = select(getSubjectEventFormSqlSS(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId, studySubjectIds));
            Iterator iter = viewRows.iterator();
            // if any algorithm modification has been done in this block,
            // the method "setDataWithOCAttributes" should be checked about the
            // updated as well because this method came from here.
            while (iter.hasNext()) {
                JobTerminationMonitor.check();
                HashMap row = (HashMap) iter.next();
                String studySubjectLabel = (String) row.get("study_subject_oid");
                String sedOID = (String) row.get("definition_oid");
                Boolean studyEventRepeating = (Boolean) row.get("definition_repeating");
                Integer sampleOrdinal = (Integer) row.get("sample_ordinal");
                String cvOID = (String) row.get("crf_version_oid");
                Integer ecId = (Integer) row.get("event_crf_id");// ecId
                // should
                // be unique;

                String key = studySubjectLabel;
                ExportSubjectDataBean sub = new ExportSubjectDataBean();
                if (subprev.equals(studySubjectLabel)) {
                    int p = data.getExportSubjectData().size() - 1;
                    sub = data.getExportSubjectData().get(p);
                } else {
                    subprev = studySubjectLabel;
                    sub.setSubjectOID(studySubjectLabel);
                    data.getExportSubjectData().add(sub);
                    seprev = "";
                    formprev = "";
                    igprev = "";
                }
                oidPos = data.getExportSubjectData().size() - 1 + "";
                ExportStudyEventDataBean se = new ExportStudyEventDataBean();
                // key += sedOID + sampleOrdinal;
                key += sedOID;
                if (!seprev.equals(key) || !sepos.containsKey(key + sampleOrdinal)) {
                    sepos.put(key + sampleOrdinal, sub.getExportStudyEventData().size());
                    seprev = key;
                    se.setStudyEventOID(sedOID);
                    se.setStudyEventRepeatKey(studyEventRepeating ? sampleOrdinal + "" : "-1");
                    sub.getExportStudyEventData().add(se);
                    formprev = "";
                    igprev = "";
                } else {
                    se = sub.getExportStudyEventData().get(sepos.get(key + sampleOrdinal));
                }
                oidPos += "---" + (sub.getExportStudyEventData().size() - 1);
                ExportFormDataBean form = new ExportFormDataBean();
                key += cvOID;
                if (formprev.equals(key)) {
                    form = se.getExportFormData().get(se.getExportFormData().size() - 1);
                } else {
                    formprev = key;
                    form.setFormOID(cvOID);
                    se.getExportFormData().add(form);
                    igprev = "";
                }
                oidPos += "---" + (se.getExportFormData().size() - 1);
                // ecId should be distinct
                oidPoses.put(ecId, oidPos);
                oidPos = "";
            }
        }

        this.setEventGroupItemDataWithUnitTypesExpected();
        logger.debug("Begin to GetEventGroupItemWithUnitSql");
        ArrayList viewRows = select(getEventGroupItemWithUnitSql(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId, studySubjectIds));
        logger.debug("getEventGroupItemWithUnitSql : "
            + getEventGroupItemWithUnitSql(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId, studySubjectIds));
        String idataIds = "";
        if (viewRows.size() > 0) {
            Iterator iter = viewRows.iterator();
            ExportSubjectDataBean sub = new ExportSubjectDataBean();
            ExportStudyEventDataBean se = new ExportStudyEventDataBean();
            ExportFormDataBean form = new ExportFormDataBean();
            int ecprev = -1;
            igprev = "";
            boolean goon = true;
            String itprev = "";
            // HashMap<String, String> nullValueMap =
            // ClinicalDataUnit.getNullValueMap();

            HashMap<String, String> nullValueCVs = this.getNullValueCVs(study);
            HashSet<Integer> itemDataIds = new HashSet<Integer>();
            String yearMonthFormat = StringUtil.parseDateFormat(ResourceBundleProvider.getFormatBundle(locale).getString("date_format_year_month"));
            String yearFormat = StringUtil.parseDateFormat(ResourceBundleProvider.getFormatBundle(locale).getString("date_format_year"));
            while (iter.hasNext()) {
                JobTerminationMonitor.check();
                HashMap row = (HashMap) iter.next();
                Integer ecId = (Integer) row.get("event_crf_id");
                Integer igId = (Integer) row.get("item_group_id");
                String igOID = (String) row.get("item_group_oid");
                String igName = (String) row.get("item_group_name");
                Integer itId = (Integer) row.get("item_id");
                String itOID = (String) row.get("item_oid");
                Integer itDataOrdinal = (Integer) row.get("item_data_ordinal");
                String itValue = (String) row.get("value");
                Integer datatypeid = (Integer) row.get("item_data_type_id");
                Integer idataId = (Integer) row.get("item_data_id");
                String muOid = (String) row.get("mu_oid");
                String key = "";
                if (ecId != ecprev) {
                    logger.debug("Found ecId=" + ecId + " in subjectEventFormSql is:" + oidPoses.containsKey(ecId));
                    if (oidPoses.containsKey(ecId)) {
                        goon = true;
                        String[] poses = oidPoses.get(ecId).split("---");
                        sub = data.getExportSubjectData().get(Integer.valueOf(poses[0]));
                        se = sub.getExportStudyEventData().get(Integer.valueOf(poses[1]));
                        form = se.getExportFormData().get(Integer.valueOf(poses[2]));
                    } else {
                        goon = false;
                    }
                    ecprev = ecId;
                }
                if (goon) {
                    ImportItemGroupDataBean ig = new ImportItemGroupDataBean();
                    // key = ecId + igOID;
                    key = ecId + "-" + igId;
                    if (!igprev.equals(key) || !igpos.containsKey(key + itDataOrdinal)) {
                        igpos.put(key + itDataOrdinal, form.getItemGroupData().size());
                        igprev = key;
                        ig.setItemGroupOID(igOID + "");
                        ig.setItemGroupRepeatKey("ungrouped".equalsIgnoreCase(igName) ? "-1" : itDataOrdinal + "");
                        form.getItemGroupData().add(ig);
                    } else {
                        ig = form.getItemGroupData().get(igpos.get(key + itDataOrdinal));
                    }
                    String newpos = oidPoses.get(ecId) + "---" + igpos.get(key + itDataOrdinal);
                    // item should be distinct; but duplicated item data have
                    // been reported because "save" have been clicked twice.
                    // those duplicated item data have been arranged together by
                    // "distinct" because of their same
                    // ecId+igOID+itOID+itDataOrdinal (08-2008)
                    key = itId + "_" + itDataOrdinal + key;
                    if (!itprev.equals(key)) {
                        itprev = key;
                        ImportItemDataBean it = new ImportItemDataBean();
                        it.setItemOID(itOID);
                        it.setTransactionType("Insert");
                        String nullKey = study.getId() + "-" + se.getStudyEventOID() + "-" + form.getFormOID();
                        if (ClinicalDataUtil.isNull(itValue, nullKey, nullValueCVs)) {
                            // if
                            // (nullValueMap.containsKey(itValue.trim().toUpperCase()))
                            // {
                            // itValue =
                            // nullValueMap.get(itValue.trim().toUpperCase());

                            it.setIsNull("Yes");
                            String nullvalues = ClinicalDataUtil.presetNullValueStr(nullValueCVs.get(nullKey));
                            boolean hasValueWithNull = ClinicalDataUtil.isValueWithNull(itValue, nullvalues);
                            it.setHasValueWithNull(hasValueWithNull);
                            if(hasValueWithNull) {
                                it.setValue(itValue);
                                it.setReasonForNull(ClinicalDataUtil.getNullsInValue(itValue, nullvalues));
                            }else {
                                it.setReasonForNull(itValue.trim());
                            }
                        } else {
                            if (datatypeid == 9) {
                                try {
                                    itValue = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat(oc_df_string).parse(itValue));
                                } catch (Exception fe) {
                                    logger.debug("Item -" + itOID + " value " + itValue + " might not be ODM date format yyyy-MM-dd.");
                                }
                            }
                            /* not be supported in openclinica-3.0.40.1
                            else if (datatypeid == 10 && odmVersion.contains("1.3")) {
                                if (StringUtil.isFormatDate(itValue, oc_df_string)) {
                                    try {
                                        itValue = new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat(oc_df_string).parse(itValue));
                                    } catch (Exception e) {
                                        logger.info("Item -" + itOID + " value " + itValue + " might not be ODM partialDate format yyyy[-MM[-dd]].");
                                    }
                                } else {
                                    if (StringUtil.isPartialYearMonth(itValue, yearMonthFormat)) {
                                        try {
                                            itValue = new SimpleDateFormat("yyyy-MM").format(new SimpleDateFormat(yearMonthFormat).parse(itValue));
                                        } catch (Exception e) {
                                            logger.info("Item -" + itOID + " value " + itValue + " might not be ODM partialDate format yyyy[-MM[-dd]].");
                                        }
                                    } else {
                                        try {
                                            itValue = new SimpleDateFormat("yyyy").format(new SimpleDateFormat(yearFormat).parse(itValue));
                                        } catch (Exception e) {
                                            logger.info("Item -" + itOID + " value " + itValue + " might not be ODM partialDate format yyyy[-MM[-dd]].");
                                        }
                                    }
                                }
                            }
                            */
                            it.setValue(itValue);
                        }
                        if (muOid != null && muOid.length() > 0) {
                            ElementRefBean measurementUnitRef = new ElementRefBean();
                            measurementUnitRef.setElementDefOID(muOid);
                            it.setMeasurementUnitRef(measurementUnitRef);
                        }
                        ig.getItemData().add(it);
                        newpos += "---" + (ig.getItemData().size() - 1);
                        idataOidPoses.put(idataId, newpos);
                    }
                    idataIds += "'" + idataId + "', ";
                }
            }
        }

        idataIds = idataIds.length() > 0 ? idataIds.substring(0, idataIds.length() - 2) : idataIds;
        if(idataIds.length()>0 && itemIds.length()>0) {
            this.setErasedScoreItemDataValues(data, itemIds, idataIds, idataOidPoses, odmVersion);
        } else {
            logger.info("OdmExtractDAO.setScoreItemDataNullValues was not called because of empty item_data_ids or/and item_ids");
        }

          if (odmType!=null && odmType.equalsIgnoreCase("clinical_data")) {
            logger.debug("Do not create discrepancy notes");
          }
          else if (odmVersion.startsWith("oc")) {
            if(idataIds.length()>0) {
                setOCItemDataAuditLogs(study, data, idataIds, idataOidPoses);
                setOCItemDataDNs(data, idataIds, idataOidPoses);
            } else {
                logger.info("OdmExtractDAO.setOCItemDataAuditLogs & setOCItemDataDNs weren't called because of empty idataIds");
            }
        }
    }

    
    
    public void getClinicalData(StudyBean study,OdmClinicalDataBean data,String odmVersion,String studySubjectIds,String odmType){
    	
    	String dbName = CoreResources.getDBName();
        String subprev = "";
        HashMap<String, Integer> sepos = new HashMap<String, Integer>();
        String seprev = "";
        String formprev = "";
        HashMap<String, Integer> igpos = new HashMap<String, Integer>();
        String igprev = "";
        String oidPos = "";
        HashMap<Integer, String> oidPoses = new HashMap<Integer, String>();
        HashMap<Integer, String> idataOidPoses = new HashMap<Integer, String>();
        String studyIds = study.getId() + "";
        
    }
    protected void setErasedScoreItemDataValues(OdmClinicalDataBean data, String itemIds, String itemDataIds, HashMap<Integer,String> idataOidPoses, String odmVersion) {
        this.setErasedScoreItemDataIdsTypesExpected();
        ArrayList<Integer> rows = this.select(this.getErasedScoreItemDataIdsSql(itemIds, itemDataIds));
        if(rows==null || rows.size()<1) {
            logger.debug("OdmExtractDAO.getErasedScoreItemDataIdsSql return no erased score item_data_id" );
        }else {
            Iterator iter = rows.iterator();
            while(iter.hasNext()) {
                HashMap row = (HashMap) iter.next();
                Integer idataId = (Integer) row.get("item_data_id");
                if(idataOidPoses.containsKey(idataId)) {
                    String[] poses = idataOidPoses.get(idataId).split("---");
                    ImportItemDataBean idata =
                        data.getExportSubjectData().get(Integer.parseInt(poses[0])).getExportStudyEventData().get(Integer.parseInt(poses[1])).getExportFormData()
                                .get(Integer.parseInt(poses[2])).getItemGroupData().get(Integer.parseInt(poses[3])).getItemData().get(Integer.parseInt(poses[4]));
                    idata.setIsNull("Yes");
                    idata.setValue("");
                    idata.setReasonForNull("Erased");
                } else {
                    logger.info("There is no erased score item data with item_data_id =" + idataId + " found in OdmClinicalData" );
                }
            }
        }
    }

    protected void setOCSubjectDataAuditLogs(StudyBean study, OdmClinicalDataBean data, String studySubjectOids, HashMap<String, String> subOidPoses) {
        this.setOCSubjectDataAuditsTypesExpected();
        logger.debug("Begin to execute GetOCSubjectDataAuditsSql");
        logger.debug("getOCSubjectDataAuditsSql= " + this.getOCSubjectDataAuditsSql(studySubjectOids));
        ArrayList rows = select(this.getOCSubjectDataAuditsSql(studySubjectOids));
        Iterator iter = rows.iterator();
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            String studySubjectLabel = (String) row.get("study_subject_oid");
            Integer auditId = (Integer) row.get("audit_id");
            String type = (String) row.get("name");
            Integer userId = (Integer) row.get("user_id");
            Date auditDate = (Date) row.get("audit_date");
            String auditReason = (String) row.get("reason_for_change");
            String oldValue = (String) row.get("old_value");
            String newValue = (String) row.get("new_value");
            Integer typeId = (Integer) row.get("audit_log_event_type_id");

            if (subOidPoses.containsKey(studySubjectLabel)) {

                ExportSubjectDataBean sub = data.getExportSubjectData().get(Integer.parseInt(subOidPoses.get(studySubjectLabel)));
                AuditLogBean auditLog = new AuditLogBean();
                auditLog.setOid("AL_" + auditId);
                auditLog.setUserId("USR_" + userId);
                logger.debug("datatime=" + auditDate + " or " + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(auditDate));
                auditLog.setDatetimeStamp(auditDate);
                auditLog.setType(type);
                auditLog.setReasonForChange(auditReason);
                if (typeId == 3 || typeId == 6) {
                    if ("0".equals(newValue)) {
                        auditLog.setOldValue(Status.INVALID.getName());
                    } else {
                        auditLog.setNewValue(Status.getFromMap(Integer.parseInt(newValue)).getName());
                    }
                    if ("0".equals(oldValue)) {
                        auditLog.setOldValue(Status.INVALID.getName());
                    } else {
                        auditLog.setOldValue(Status.getFromMap(Integer.parseInt(oldValue)).getName());
                    }
                } else {
                    auditLog.setNewValue(newValue);
                    auditLog.setOldValue(oldValue);
                }
                AuditLogsBean logs = sub.getAuditLogs();
                if (logs.getEntityID() == null || logs.getEntityID().length() <= 0) {
                    logs.setEntityID(sub.getSubjectOID());
                }
                logs.getAuditLogs().add(auditLog);
                sub.setAuditLogs(logs);
            }
        }
    }

    protected void setOCEventDataAuditLogs(StudyBean study, OdmClinicalDataBean data, String studySubjectOids, HashMap<String, String> evnOidPoses) {
        this.setOCEventDataAuditsTypesExpected();
        logger.debug("Begin to execute GetOCEventDataAuditsSql");
        logger.debug("getOCEventDataAuditsSql= " + this.getOCEventDataAuditsSql(studySubjectOids));
        ArrayList rows = select(this.getOCEventDataAuditsSql(studySubjectOids));
        Iterator iter = rows.iterator();
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            String studySubjectLabel = (String) row.get("study_subject_oid");
            String sedOid = (String) row.get("definition_oid");
            Integer auditId = (Integer) row.get("audit_id");
            String type = (String) row.get("name");
            Integer userId = (Integer) row.get("user_id");
            Date auditDate = (Date) row.get("audit_date");
            String auditReason = (String) row.get("reason_for_change");
            String oldValue = (String) row.get("old_value");
            String newValue = (String) row.get("new_value");
            Integer typeId = (Integer) row.get("audit_log_event_type_id");

            if (evnOidPoses.containsKey(studySubjectLabel + sedOid)) {
                String[] poses = evnOidPoses.get(studySubjectLabel + sedOid).split("---");
                ExportStudyEventDataBean se =
                    data.getExportSubjectData().get(Integer.parseInt(poses[0])).getExportStudyEventData().get(Integer.parseInt(poses[1]));
                AuditLogBean auditLog = new AuditLogBean();
                auditLog.setOid("AL_" + auditId);
                auditLog.setUserId("USR_" + userId);
                auditLog.setDatetimeStamp(auditDate);
                auditLog.setType(type);
                auditLog.setReasonForChange(auditReason);
                if (typeId == 17 || typeId == 18 || typeId == 19 || typeId == 20 || typeId == 21 || typeId == 22 || typeId == 23 || typeId == 31) {
                    if ("0".equals(newValue)) {
                        auditLog.setOldValue(SubjectEventStatus.INVALID.getName());
                    } else {
                        auditLog.setNewValue(SubjectEventStatus.getFromMap(Integer.parseInt(newValue)).getName());
                    }
                    if ("0".equals(oldValue)) {
                        auditLog.setOldValue(SubjectEventStatus.INVALID.getName());
                    } else {
                        auditLog.setOldValue(SubjectEventStatus.getFromMap(Integer.parseInt(oldValue)).getName());
                    }
                } else {
                    auditLog.setNewValue(newValue);
                    auditLog.setOldValue(oldValue);
                }
                AuditLogsBean logs = se.getAuditLogs();
                if (logs.getEntityID() == null || logs.getEntityID().length() <= 0) {
                    logs.setEntityID(se.getStudyEventOID());
                }
                logs.getAuditLogs().add(auditLog);
                se.setAuditLogs(logs);
            }
        }
    }

    protected void setOCFormDataAuditLogs(StudyBean study, OdmClinicalDataBean data, String studySubjectOids, String ecIds,
            HashMap<Integer, String> formOidPoses) {
        this.setOCFormDataAuditsTypesExpected();
        String dbName = CoreResources.getDBName();
        logger.debug("Begin to execute GetOCFormDataAuditsSql");
        logger.debug("getOCFormDataAuditsSql= " + this.getOCFormDataAuditsSql(studySubjectOids, ecIds));
        ArrayList rows = select(this.getOCFormDataAuditsSql(studySubjectOids, ecIds));
        Iterator iter = rows.iterator();
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            Integer ecId = (Integer) row.get("event_crf_id");
            Integer auditId = (Integer) row.get("audit_id");
            String type = (String) row.get("name");
            Integer userId = (Integer) row.get("user_id");
            Date auditDate = (Date) row.get("audit_date");
            String auditReason = (String) row.get("reason_for_change");
            String oldValue = (String) row.get("old_value");
            String newValue = (String) row.get("new_value");
            Integer typeId = (Integer) row.get("audit_log_event_type_id");
            
            if (formOidPoses.containsKey(ecId)) {
                String[] poses = formOidPoses.get(ecId).split("---");
                ExportFormDataBean form =
                    data.getExportSubjectData().get(Integer.parseInt(poses[0])).getExportStudyEventData().get(Integer.parseInt(poses[1])).getExportFormData()
                            .get(Integer.parseInt(poses[2]));
                AuditLogBean auditLog = new AuditLogBean();
                auditLog.setOid("AL_" + auditId);
                auditLog.setUserId("USR_" + userId);
                auditLog.setDatetimeStamp(auditDate);
                auditLog.setType(type);
                auditLog.setReasonForChange(auditReason);
                if (typeId == 8 || typeId == 10 || typeId == 11 || typeId == 14 || typeId == 15 || typeId == 16) {
                    if ("0".equals(newValue)) {
                        auditLog.setNewValue(Status.INVALID.getName());
                    } else {
                        auditLog.setNewValue(Status.getFromMap(Integer.parseInt(newValue)).getName());
                    }
                    if ("0".equals(oldValue)) {
                        auditLog.setOldValue(Status.INVALID.getName());
                    } else {
                        auditLog.setOldValue(Status.getFromMap(Integer.parseInt(oldValue)).getName());
                    }
                } //Fix for 0011675: SDV'ed subject is dipslayed as not SDV'ed in the 1.3 Full ODM Extract commenting out the following lines as these are treated like booleans while they are strings
                //JN:The Oracle still continues to have 1 and 2 for this audit type 32 so enabling the following code for oracle only, ideally the trigger should be coded same for both postgres and oracle and since the trigger doesnt do same things, the existing data would still be a problem, so doing this patch work
               
                 else if ((typeId == 32) &&
                	  ("oracle".equalsIgnoreCase(dbName))){
                    if ("1".equals(newValue)) {
                        auditLog.setNewValue("TRUE");
                    } else {
                        auditLog.setNewValue("FALSE");
                    }
                    if ("1".equals(oldValue)) {
                        auditLog.setOldValue("TRUE");
                    } else {
                        auditLog.setOldValue("FALSE");
                    }
                	 }
                 else {
                    auditLog.setNewValue(newValue);
                    auditLog.setOldValue(oldValue);
                }
                AuditLogsBean logs = form.getAuditLogs();
                if (logs.getEntityID() == null || logs.getEntityID().length() <= 0) {
                    logs.setEntityID(form.getFormOID());
                }
                logs.getAuditLogs().add(auditLog);
                form.setAuditLogs(logs);
            }
        }
    }

    protected void setOCItemDataAuditLogs(StudyBean study, OdmClinicalDataBean data, String idataIds, HashMap<Integer, String> idataOidPoses) {
        this.setOCItemDataAuditsTypesExpected();
        logger.debug("Begin to execute GetOCItemDataAuditsSql");
        logger.debug("getOCItemDataAuditsSql= " + this.getOCItemDataAuditsSql(idataIds));
        ArrayList rows = select(this.getOCItemDataAuditsSql(idataIds));
        Iterator iter = rows.iterator();
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            Integer idataId = (Integer) row.get("item_data_id");
            Integer auditId = (Integer) row.get("audit_id");
            String type = (String) row.get("name");
            Integer userId = (Integer) row.get("user_id");
            Date auditDate = (Date) row.get("audit_date");
            String auditReason = (String) row.get("reason_for_change");
            String oldValue = (String) row.get("old_value");
            String newValue = (String) row.get("new_value");
            Integer typeId = (Integer) row.get("audit_log_event_type_id");

            if (idataOidPoses.containsKey(idataId)) {
                String[] poses = idataOidPoses.get(idataId).split("---");
                ImportItemDataBean idata =
                    data.getExportSubjectData().get(Integer.parseInt(poses[0])).getExportStudyEventData().get(Integer.parseInt(poses[1])).getExportFormData()
                            .get(Integer.parseInt(poses[2])).getItemGroupData().get(Integer.parseInt(poses[3])).getItemData().get(Integer.parseInt(poses[4]));
                AuditLogBean auditLog = new AuditLogBean();
                auditLog.setOid("AL_" + auditId);
                auditLog.setUserId("USR_" + userId);
                auditLog.setDatetimeStamp(auditDate);
                auditLog.setType(type);
                auditLog.setReasonForChange(auditReason);
                if (typeId == 12) {
                    if ("0".equals(newValue)) {
                        auditLog.setOldValue(Status.INVALID.getName());
                    } else {
                        auditLog.setNewValue(Status.getFromMap(Integer.parseInt(newValue)).getName());
                    }
                    if ("0".equals(oldValue)) {
                        auditLog.setOldValue(Status.INVALID.getName());
                    } else {
                        auditLog.setOldValue(Status.getFromMap(Integer.parseInt(oldValue)).getName());
                    }
                } else {
                    auditLog.setNewValue(newValue);
                    auditLog.setOldValue(oldValue);
                }
                AuditLogsBean logs = idata.getAuditLogs();
                if (logs.getEntityID() == null || logs.getEntityID().length() <= 0) {
                    logs.setEntityID(idata.getItemOID());
                }
                logs.getAuditLogs().add(auditLog);
                idata.setAuditLogs(logs);
            }
        }
    }

    protected void setOCSubjectDataDNs(OdmClinicalDataBean data, String studySubjectOids, HashMap<String, String> subOidPoses) {
        this.setOCSubjectDataDNsTypesExpected();
        HashMap<String, ArrayList<ChildNoteBean>> pDNs = new HashMap<String, ArrayList<ChildNoteBean>>();
        // HashMap<String, ArrayList<DiscrepancyNoteBean>> sDNs = new HashMap<String, ArrayList<DiscrepancyNoteBean>>();
        logger.debug("Begin to execute GetOCSubjectDataDNsSql");
        logger.debug("getOCSubjectDataDNsSql= " + this.getOCSubjectDataDNsSql(studySubjectOids));
        ArrayList rows = select(this.getOCSubjectDataDNsSql(studySubjectOids));
        Iterator iter = rows.iterator();
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            String studySubjectLabel = (String) row.get("study_subject_oid");
            Integer pdnId = (Integer) row.get("parent_dn_id");
            Integer dnId = (Integer) row.get("dn_id");
            String description = (String) row.get("description");
            String detailedNote = (String) row.get("detailed_notes");
            Integer ownerId = (Integer) row.get("owner_id");
            Date dateCreated = (Date) row.get("date_created");
            String status = (String) row.get("status");
            String noteType = (String) row.get("name");

            if (pdnId != null && pdnId > 0) {
                String key = studySubjectLabel + "-" + pdnId;
                ChildNoteBean cn = new ChildNoteBean();
                cn.setDateCreated(dateCreated);
                cn.setDescription(description);
                cn.setDetailedNote(detailedNote);
                cn.setStatus(status);
                cn.setOid("CDN_" + dnId);
                ElementRefBean userRef = new ElementRefBean();
                userRef.setElementDefOID("USR_" + ownerId);
                cn.setUserRef(userRef);
                ArrayList<ChildNoteBean> cns = pDNs.containsKey(key) ? pDNs.get(key) : new ArrayList<ChildNoteBean>();
                cns.add(cn);
                pDNs.put(key, cns);
            } else {
                DiscrepancyNoteBean dn = new DiscrepancyNoteBean();
                String k = studySubjectLabel + "-" + dnId;
                if (pDNs != null && pDNs.containsKey(k)) {
                    dn.setChildNotes(pDNs.get(k));
                    dn.setNumberOfChildNotes(dn.getChildNotes().size());
                }
                dn.setDateUpdated(dateCreated);
                dn.setNoteType(noteType);
                dn.setStatus(status);
                dn.setOid("DN_" + dnId);
                ElementRefBean userRef = new ElementRefBean();
                userRef.setElementDefOID("USR_" + ownerId);
                if (subOidPoses.containsKey(studySubjectLabel)) {
                    int i = Integer.parseInt(subOidPoses.get(studySubjectLabel));
                    String entityID = data.getExportSubjectData().get(i).getSubjectOID();
                    data.getExportSubjectData().get(i).getDiscrepancyNotes().setEntityID(entityID);
                    data.getExportSubjectData().get(i).getDiscrepancyNotes().getDiscrepancyNotes().add(dn);
                }
            }
        }
    }

    protected void setOCEventDataDNs(OdmClinicalDataBean data, String definitionOids, String studySubjectOids, HashMap<String, String> evnOidPoses) {
        this.setOCEventDataDNsTypesExpected();
        HashMap<String, ArrayList<ChildNoteBean>> pDNs = new HashMap<String, ArrayList<ChildNoteBean>>();
        // HashMap<String, ArrayList<DiscrepancyNoteBean>> sDNs = new HashMap<String, ArrayList<DiscrepancyNoteBean>>();
        logger.debug("Begin to execute GetOCEventDataDNsSql");
        logger.debug("getOCEventDataDNsSql= " + this.getOCEventDataDNsSql(definitionOids, studySubjectOids));
        ArrayList rows = select(this.getOCEventDataDNsSql(definitionOids, studySubjectOids));
        Iterator iter = rows.iterator();
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            String studySubjectLabel = (String) row.get("study_subject_oid");
            String defOid = (String) row.get("definition_oid");
            Integer pdnId = (Integer) row.get("parent_dn_id");
            Integer dnId = (Integer) row.get("dn_id");
            String description = (String) row.get("description");
            String detailedNote = (String) row.get("detailed_notes");
            Integer ownerId = (Integer) row.get("owner_id");
            Date dateCreated = (Date) row.get("date_created");
            String status = (String) row.get("status");
            String noteType = (String) row.get("name");

            String oidKey = studySubjectLabel + defOid;
            if (pdnId != null && pdnId > 0) {
                String key = oidKey + pdnId;
                ChildNoteBean cn = new ChildNoteBean();
                cn.setDateCreated(dateCreated);
                cn.setDescription(description);
                cn.setDetailedNote(detailedNote);
                cn.setStatus(status);
                cn.setOid("CDN_" + dnId);
                ElementRefBean userRef = new ElementRefBean();
                userRef.setElementDefOID("USR_" + ownerId);
                cn.setUserRef(userRef);
                ArrayList<ChildNoteBean> cns = pDNs.containsKey(key) ? pDNs.get(key) : new ArrayList<ChildNoteBean>();
                cns.add(cn);
                pDNs.put(key, cns);
            } else {
                DiscrepancyNoteBean dn = new DiscrepancyNoteBean();
                String k = oidKey + dnId;
                if (pDNs != null && pDNs.containsKey(k)) {
                    dn.setChildNotes(pDNs.get(k));
                    dn.setNumberOfChildNotes(dn.getChildNotes().size());
                }
                dn.setDateUpdated(dateCreated);
                dn.setNoteType(noteType);
                dn.setStatus(status);
                dn.setOid("DN_" + dnId);
                ElementRefBean userRef = new ElementRefBean();
                userRef.setElementDefOID("USR_" + ownerId);
                if (evnOidPoses.containsKey(oidKey)) {
                    String[] poses = evnOidPoses.get(oidKey).split("---");
                    int p0 = Integer.parseInt(poses[0]);
                    int p1 = Integer.parseInt(poses[1]);
                    String entityID = data.getExportSubjectData().get(p0).getExportStudyEventData().get(p1).getStudyEventOID();
                    data.getExportSubjectData().get(p0).getExportStudyEventData().get(p1).getDiscrepancyNotes().setEntityID(entityID);
                    data.getExportSubjectData().get(p0).getExportStudyEventData().get(p1).getDiscrepancyNotes().getDiscrepancyNotes().add(dn);
                }
            }
        }
    }

    protected void setOCFormDataDNs(OdmClinicalDataBean data, String ecIds, HashMap<Integer, String> formOidPoses) {
        this.setOCFormDataDNsTypesExpected();
        HashMap<String, ArrayList<ChildNoteBean>> pDNs = new HashMap<String, ArrayList<ChildNoteBean>>();
        // HashMap<String, ArrayList<DiscrepancyNoteBean>> sDNs = new HashMap<String, ArrayList<DiscrepancyNoteBean>>();
        logger.debug("Begin to execute GetOCEventDataDNsSql");
        logger.debug("getOCFormDataDNsSql= " + this.getOCFormDataDNsSql(ecIds));
        ArrayList rows = select(this.getOCFormDataDNsSql(ecIds));
        Iterator iter = rows.iterator();
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            Integer ecId = (Integer) row.get("event_crf_id");
            Integer pdnId = (Integer) row.get("parent_dn_id");
            Integer dnId = (Integer) row.get("dn_id");
            String description = (String) row.get("description");
            String detailedNote = (String) row.get("detailed_notes");
            Integer ownerId = (Integer) row.get("owner_id");
            Date dateCreated = (Date) row.get("date_created");
            String status = (String) row.get("status");
            String noteType = (String) row.get("name");

            if (pdnId != null && pdnId > 0) {
                String key = ecId + "-" + pdnId;
                ChildNoteBean cn = new ChildNoteBean();
                cn.setDateCreated(dateCreated);
                cn.setDescription(description);
                cn.setDetailedNote(detailedNote);
                cn.setStatus(status);
                cn.setOid("CDN_" + dnId);
                ElementRefBean userRef = new ElementRefBean();
                userRef.setElementDefOID("USR_" + ownerId);
                cn.setUserRef(userRef);
                ArrayList<ChildNoteBean> cns = pDNs.containsKey(key) ? pDNs.get(key) : new ArrayList<ChildNoteBean>();
                cns.add(cn);
                pDNs.put(key, cns);
            } else {
                DiscrepancyNoteBean dn = new DiscrepancyNoteBean();
                String k = ecId + "-" + dnId;
                if (pDNs != null && pDNs.containsKey(k)) {
                    dn.setChildNotes(pDNs.get(k));
                    dn.setNumberOfChildNotes(dn.getChildNotes().size());
                }
                dn.setDateUpdated(dateCreated);
                dn.setNoteType(noteType);
                dn.setStatus(status);
                dn.setOid("DN_" + dnId);
                ElementRefBean userRef = new ElementRefBean();
                userRef.setElementDefOID("USR_" + ownerId);
                if (formOidPoses.containsKey(ecId)) {
                    String[] poses = formOidPoses.get(ecId).split("---");
                    int p0 = Integer.parseInt(poses[0]);
                    int p1 = Integer.parseInt(poses[1]);
                    int p2 = Integer.parseInt(poses[2]);
                    String entityID = data.getExportSubjectData().get(p0).getExportStudyEventData().get(p1).getExportFormData().get(p2).getFormOID();
                    data.getExportSubjectData().get(p0).getExportStudyEventData().get(p1).getExportFormData().get(p2).getDiscrepancyNotes()
                            .setEntityID(entityID);
                    data.getExportSubjectData().get(p0).getExportStudyEventData().get(p1).getExportFormData().get(p2).getDiscrepancyNotes()
                            .getDiscrepancyNotes().add(dn);
                }
            }
        }
    }

    protected void setOCItemDataDNs(OdmClinicalDataBean data, String idataIds, HashMap<Integer, String> idataOidPoses) {
        this.setOCItemDataDNsTypesExpected();
        HashMap<String, ArrayList<ChildNoteBean>> pDNs = new HashMap<String, ArrayList<ChildNoteBean>>();
        // HashMap<String, ArrayList<DiscrepancyNoteBean>> sDNs = new HashMap<String, ArrayList<DiscrepancyNoteBean>>();
        logger.debug("Begin to execute GetOCItemDataDNsSql");
        logger.debug("getOCItemDataDNsSql= " + this.getOCItemDataDNsSql(idataIds));
        ArrayList rows = select(this.getOCItemDataDNsSql(idataIds));
        Iterator iter = rows.iterator();
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            Integer idataId = (Integer) row.get("item_data_id");
            Integer pdnId = (Integer) row.get("parent_dn_id");
            Integer dnId = (Integer) row.get("dn_id");
            String description = (String) row.get("description");
            String detailedNote = (String) row.get("detailed_notes");
            Integer ownerId = (Integer) row.get("owner_id");
            Date dateCreated = (Date) row.get("date_created");
            String status = (String) row.get("status");
            String noteType = (String) row.get("name");

            if (pdnId != null && pdnId > 0) {
                String key = idataId + "-" + pdnId;
                ChildNoteBean cn = new ChildNoteBean();
                cn.setDateCreated(dateCreated);
                cn.setDescription(description);
                cn.setDetailedNote(detailedNote);
                cn.setStatus(status);
                cn.setOid("CDN_" + dnId);
                ElementRefBean userRef = new ElementRefBean();
                userRef.setElementDefOID("USR_" + ownerId);
                cn.setUserRef(userRef);
                ArrayList<ChildNoteBean> cns = pDNs.containsKey(key) ? pDNs.get(key) : new ArrayList<ChildNoteBean>();
                cns.add(cn);
                pDNs.put(key, cns);
            } else {
                DiscrepancyNoteBean dn = new DiscrepancyNoteBean();
                String k = idataId + "-" + dnId;
                if (pDNs != null && pDNs.containsKey(k)) {
                    dn.setChildNotes(pDNs.get(k));
                    dn.setNumberOfChildNotes(dn.getChildNotes().size());
                }
                dn.setDateUpdated(dateCreated);
                dn.setNoteType(noteType);
                dn.setStatus(status);
                dn.setOid("DN_" + dnId);
                ElementRefBean userRef = new ElementRefBean();
                userRef.setElementDefOID("USR_" + ownerId);
                if (idataOidPoses.containsKey(idataId)) {
                    String[] poses = idataOidPoses.get(idataId).split("---");
                    int p0 = Integer.parseInt(poses[0]);
                    int p1 = Integer.parseInt(poses[1]);
                    int p2 = Integer.parseInt(poses[2]);
                    int p3 = Integer.parseInt(poses[3]);
                    int p4 = Integer.parseInt(poses[4]);
                    String entityID =
                        data.getExportSubjectData().get(p0).getExportStudyEventData().get(p1).getExportFormData().get(p2).getItemGroupData().get(p3)
                                .getItemData().get(p4).getItemOID();
                    data.getExportSubjectData().get(p0).getExportStudyEventData().get(p1).getExportFormData().get(p2).getItemGroupData().get(p3).getItemData()
                            .get(p4).getDiscrepancyNotes().setEntityID(entityID);
                    data.getExportSubjectData().get(p0).getExportStudyEventData().get(p1).getExportFormData().get(p2).getItemGroupData().get(p3).getItemData()
                            .get(p4).getDiscrepancyNotes().getDiscrepancyNotes().add(dn);
                }
            }
        }
    }

    protected void setDataWithOCAttributes(StudyBean study, DatasetBean dataset, OdmClinicalDataBean data, String odmVersion, Iterator iter,
            HashMap<Integer, String> oidPoses, String odmType) {
        String subprev = "";
        HashMap<String, Integer> sepos = new HashMap<String, Integer>();
        String seprev = "";
        String formprev = "";
        HashMap<String, Integer> igpos = new HashMap<String, Integer>();
        String igprev = "";
        String oidPos = "";
        StudyBean parentStudy = study.getParentStudyId() > 0 ? (StudyBean) new StudyDAO(this.ds).findByPK(study.getParentStudyId()) : study;
        setStudyParemeterConfig(parentStudy);
        HashSet<Integer> sgcIdSet = new HashSet<Integer>();
        HashMap<String, String> subOidPoses = new HashMap<String, String>();
        HashMap<String, String> evnOidPoses = new HashMap<String, String>();
        String studySubjectOids = "";
        String sedOids = "";
        String ecIds = "";
        while (iter.hasNext()) {
            HashMap row = (HashMap) iter.next();
            String studySubjectLabel = (String) row.get("study_subject_oid");
            // String label = (String) row.get("label");
            Integer sgcId = (Integer) row.get("sgc_id");
            String sgcName = (String) row.get("sgc_name");
            String sgName = (String) row.get("sg_name");
            String sedOID = (String) row.get("definition_oid");
            Boolean studyEventRepeating = (Boolean) row.get("definition_repeating");
            Integer sampleOrdinal = (Integer) row.get("sample_ordinal");
            Date startDate = (Date) row.get("date_start");
            String cvOID = (String) row.get("crf_version_oid");
            Integer ecId = (Integer) row.get("event_crf_id");// ecId should
            // be unique;
            Date dob = (Date) row.get("date_of_birth");

            String key = studySubjectLabel;
            ExportSubjectDataBean sub = new ExportSubjectDataBean();
            if (subprev.equals(studySubjectLabel)) {
                int p = data.getExportSubjectData().size() - 1;
                sub = data.getExportSubjectData().get(p);
                // ------ add openclinica subject_group
                if (sgcId > 0) {
                    int presize = sgcIdSet.size();
                    sgcIdSet.add(sgcId);
                    if (sgcIdSet.size() > presize) {
                        sgcIdSet.add(sgcId);
                        SubjectGroupDataBean sgd = new SubjectGroupDataBean();
                        sgd.setStudyGroupClassId("SGC_" + sgcId);
                        sgd.setStudyGroupClassName(sgcName);
                        sgd.setStudyGroupName(sgName);
                        sub.getSubjectGroupData().add(sgd);
                    }
                }
                // ------ finish adding openclinica subject_group
            } else {
                subprev = studySubjectLabel;
                studySubjectOids += "'" + studySubjectLabel + "', ";
                sub.setSubjectOID(studySubjectLabel);
                // ----- add openclinica subject attributes
                sub.setStudySubjectId((String) row.get("label"));
                if (dataset.isShowSubjectUniqueIdentifier()) {
                    sub.setUniqueIdentifier((String) row.get("unique_identifier"));
                }
                if (dataset.isShowSubjectSecondaryId()) {
                    sub.setSecondaryId((String) row.get("secondary_label"));
                }
                if (dob != null) {
                    if (dataset.isShowSubjectDob()) {
                        if (parentStudy.getStudyParameterConfig().getCollectDob().equals("2")) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(dob);
                            int year = cal.get(Calendar.YEAR);
                            if (year > 0) {
                                sub.setYearOfBirth(year);
                            }
                        } else {
                            sub.setDateOfBirth(new SimpleDateFormat("yyyy-MM-dd").format(dob));
                        }
                    }
                }
                if (dataset.isShowSubjectGender()) {
                    sub.setSubjectGender((String) row.get("gender"));
                }
                if (dataset.isShowSubjectStatus()) {
                    sub.setStatus(Status.get((Integer) row.get("status_id")).getName());
                }
                // ------ finish adding openclinica subject attributes

                // ------ add openclinica subject_group
                if (sgcId > 0) {
                    sgcIdSet.clear();
                    sgcIdSet.add(sgcId);
                    SubjectGroupDataBean sgd = new SubjectGroupDataBean();
                    sgd.setStudyGroupClassId("SGC_" + sgcId);
                    sgd.setStudyGroupClassName(sgcName);
                    sgd.setStudyGroupName(sgName);
                    sub.getSubjectGroupData().add(sgd);
                }
                // ------ finish adding openclinica subject_group

                data.getExportSubjectData().add(sub);
                seprev = "";
                formprev = "";
                igprev = "";
            }

            oidPos = data.getExportSubjectData().size() - 1 + "";
            subOidPoses.put(studySubjectLabel, oidPos);
            ExportStudyEventDataBean se = new ExportStudyEventDataBean();
            // key += sedOID + sampleOrdinal;
            key += sedOID;
            if (!seprev.equals(key) || !sepos.containsKey(key + sampleOrdinal)) {
                sepos.put(key + sampleOrdinal, sub.getExportStudyEventData().size());
                seprev = key;
                sedOids += "'" + sedOID + "', ";
                se.setStudyEventOID(sedOID);
                // ----- add openclinica study event attributes
                if (startDate != null && dataset.isShowSubjectAgeAtEvent() && dob != null) {
                    se.setAgeAtEvent(Utils.getAge(dob, startDate));
                }
                if (dataset.isShowEventLocation()) {
                    se.setLocation((String) row.get("se_location"));
                }
                if (dataset.isShowEventStart() && startDate != null) {
                    if ((Boolean) row.get("start_time_flag") == Boolean.TRUE) {
                        se.setStartDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(startDate));
                    } else {
                        se.setStartDate(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
                    }
                }
                Date endDate = (Date) row.get("date_end");
                if (dataset.isShowEventEnd() && endDate != null) {
                    if ((Boolean) row.get("end_time_flag") == Boolean.TRUE) {
                        se.setEndDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(endDate));
                    } else {
                        se.setEndDate(new SimpleDateFormat("yyyy-MM-dd").format(endDate));
                    }
                }
                if (dataset.isShowEventStatus()) {
                    se.setStatus(SubjectEventStatus.get((Integer) row.get("event_status_id")).getName());
                }
                // ----- finish adding study event attributes
                se.setStudyEventRepeatKey(studyEventRepeating ? sampleOrdinal + "" : "-1");
                sub.getExportStudyEventData().add(se);
                formprev = "";
                igprev = "";
            } else {
                se = sub.getExportStudyEventData().get(sepos.get(key + sampleOrdinal));
            }
            oidPos += "---" + (sub.getExportStudyEventData().size() - 1);
            evnOidPoses.put(key, oidPos);
            ExportFormDataBean form = new ExportFormDataBean();
            key += cvOID;
            if (formprev.equals(key)) {
                form = se.getExportFormData().get(se.getExportFormData().size() - 1);
            } else {
                formprev = key;
                ecIds += "'" + ecId + "', ";
                form.setFormOID(cvOID);
                // ----- add openclinica crf attributes
                if (dataset.isShowCRFversion()) {
                    form.setCrfVersion((String) row.get("crf_version"));
                }
                if (dataset.isShowCRFstatus()) {
                    form.setStatus(this.getCrfVersionStatus(se.getStatus(), (Integer) row.get("cv_status_id"), (Integer) row.get("ec_status_id"),
                            (Integer) row.get("validator_id")));
                }
                if (dataset.isShowCRFinterviewerName()) {
                    form.setInterviewerName((String) row.get("interviewer_name"));
                }
                if (dataset.isShowCRFinterviewerDate()) {
                    try {
                        form.setInterviewDate(new SimpleDateFormat("yyyy-MM-dd").format((Date) row.get("date_interviewed")));
                    } catch (NullPointerException npe) {
                        logger.debug("caught NPE for interviewDate");
                        //Comment it out for: 11592. For this exaction function, interviewDate should be kept as the same as in database.
                        //form.setInterviewDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
                    }
                }
                // ----- finish adding crf attributes
                se.getExportFormData().add(form);
                igprev = "";
            }
            oidPos += "---" + (se.getExportFormData().size() - 1);
            // ecId should be distinct
            oidPoses.put(ecId, oidPos);
            oidPos = "";
        }
        studySubjectOids = studySubjectOids.length() > 0 ? studySubjectOids.substring(0, studySubjectOids.length() - 2).trim() : studySubjectOids;
        sedOids = sedOids.length() > 0 ? sedOids.substring(0, sedOids.length() - 2).trim() : sedOids;
        ecIds = ecIds.length() > 0 ? ecIds.substring(0, ecIds.length() - 2).trim() : ecIds;

        if(odmType!=null && odmType.equalsIgnoreCase("clinical_data"))
        {
            logger.debug("No Audit logs or discrepancy Notes");
        }
        else{
            if(studySubjectOids.length()>0) {
                this.setOCSubjectDataAuditLogs(parentStudy, data, studySubjectOids, subOidPoses);
                this.setOCEventDataAuditLogs(parentStudy, data, studySubjectOids, evnOidPoses);
                if(ecIds.length()>0) {
                    this.setOCFormDataAuditLogs(parentStudy, data, studySubjectOids, ecIds, oidPoses);
                } else{
                    logger.debug("OdmExtractDAO.setOCFormDataAuditLogs wasn't called because of empty ecIds");
                }
                this.setOCSubjectDataDNs(data, studySubjectOids, subOidPoses);
                if(sedOids.length()>0) {
                    this.setOCEventDataDNs(data, sedOids, studySubjectOids, evnOidPoses);
                } else {
                    logger.info("OdmExtractDAO.setOCEventDataDNs wasn't called because of empty sedOids");
                }
            } else {
                logger.debug("OdmExtractDAO methods(setOCSubjectDataAuditLogs,setOCEventDataAuditLogs,setOCFormDataAuditLogs,"
                        + "setOCSubjectDataDNs,setOCEventDataDNs) weren't called because of empty studySubjectOids");
            }
            if(ecIds.length()>0) {
                this.setOCFormDataDNs(data, ecIds, oidPoses);
            } else {
                logger.debug("OdmExtractDAO.setOCFormDataDNs wasn't called because of empty ecIds");
            }
        }
    }

    private String getCrfVersionStatus(String seSubjectEventStatus, int cvStatusId, int ecStatusId, int validatorId) {
        DataEntryStage stage = DataEntryStage.INVALID;
        Status status = Status.get(ecStatusId);

        // At this time, EventCRFBean stage is not in database.
        //
        // if (stage != null) {
        // if (!stage.equals(DataEntryStage.INVALID)) {
        // return stage.getName();
        // }
        // }
        //
        // if (!active || !status.isActive()) {
        // stage = DataEntryStage.UNCOMPLETED;
        // }
        if (stage.equals(DataEntryStage.INVALID) || status.equals(Status.INVALID)) {
            stage = DataEntryStage.UNCOMPLETED;
        }

        if (status.equals(Status.AVAILABLE)) {
            stage = DataEntryStage.INITIAL_DATA_ENTRY;
        }
        if (status.equals(Status.PENDING)) {
            if (validatorId != 0) {
                stage = DataEntryStage.DOUBLE_DATA_ENTRY;
            } else {
                stage = DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE;
            }
        }
        if (status.equals(Status.UNAVAILABLE)) {
            stage = DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE;
        }
        if (status.equals(Status.LOCKED)) {
            stage = DataEntryStage.LOCKED;
        }

        try {
            if (seSubjectEventStatus.equals(SubjectEventStatus.LOCKED.getName()) || seSubjectEventStatus.equals(SubjectEventStatus.SKIPPED.getName())
                || seSubjectEventStatus.equals(SubjectEventStatus.STOPPED.getName())) {
                stage = DataEntryStage.LOCKED;
            } else if (seSubjectEventStatus.equals(SubjectEventStatus.INVALID.getName())) {
                stage = DataEntryStage.LOCKED;
            } else if (cvStatusId != 1) {
                stage = DataEntryStage.LOCKED;
            }
        } catch (NullPointerException e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            //System.out.println("caught NPE here");
			logger.debug("caught NPE here");
        }

        logger.debug("returning " + stage.getName());

        return stage.getName();
    }

    protected void setStudyParemeterConfig(StudyBean study) {
        StudyParameterValueBean param = new StudyParameterValueDAO(this.ds).findByHandleAndStudy(study.getId(), "collectDob");
        study.getStudyParameterConfig().setCollectDob(param.getValue());
    }

    protected HashMap<String,Integer> getItemGroupOIDPos(MetaDataVersionBean metadata){
        HashMap<String,Integer> igPoses = new HashMap<String,Integer>();
        ArrayList<ItemGroupDefBean> igs = (ArrayList<ItemGroupDefBean>)metadata.getItemGroupDefs();
        for(int i=0; i<igs.size();++i) {
            igPoses.put(igs.get(i).getOid(), i);
        }
        return igPoses;
    }

    protected HashMap<String,Integer> getItemOIDPos(MetaDataVersionBean metadata){
        HashMap<String,Integer> itPoses = new HashMap<String,Integer>();
        ArrayList<ItemDefBean> its = (ArrayList<ItemDefBean>)metadata.getItemDefs();
        for(int i=0; i<its.size();++i) {
            itPoses.put(its.get(i).getOid(), i);
        }
        return itPoses;
    }

    protected HashMap<Integer,String> getSectionLabels(String sectionIds) {
        HashMap<Integer,String> labels = new HashMap<Integer,String>();
        this.setSectionLabelsTypesExpected();
        ArrayList rows = this.select(this.getSectionLabelsSql(sectionIds));
        Iterator it = rows.iterator();
        while(it.hasNext()) {
            HashMap row = (HashMap) it.next();
            Integer secId = (Integer) row.get("section_id");
            String label = (String) row.get("label");
            labels.put(secId, label);
        }
        return labels;
    }

    protected HashMap<Integer,String> getParentItemOIDs(String crfVersionIds) {
        HashMap<Integer,String> oids = new HashMap<Integer,String>();
        this.setParentItemOIDsTypesExpected();
        ArrayList rows = this.select(this.getParentItemOIDsSql(crfVersionIds));
        Iterator it = rows.iterator();
        while(it.hasNext()) {
            HashMap row = (HashMap) it.next();
            Integer itId = (Integer) row.get("item_id");
            String oid = (String) row.get("oc_oid");
            oids.put(itId, oid);
        }
        return oids;
    }


    protected String getStudyGroupClassSql(int studyId) {
        return "select sgc.study_group_class_id, sgc.name as sgc_name, gct.name as sgc_type, sgc.status_id,"
            + " sgc.subject_assignment, sg.study_group_id, sg.name as sg_name, sg.description from study_group_class sgc,"
            + " study_group sg, group_class_types gct where study_id in (" + studyId + ")" + " and sgc.study_group_class_id = sg.study_group_class_id"
            + " and sgc.group_class_type_id = gct.group_class_type_id order by sgc.study_group_class_id";
    }

    protected String getStudyEventAndFormMetaSql(int parentStudyId, int studyId, boolean isIncludedSite) {
        return "select sed.ordinal as definition_order, edc.ordinal as crf_order, edc.crf_id, cv.crf_version_id,"
            + " sed.oc_oid as definition_oid, sed.name as definition_name, sed.repeating as definition_repeating,"
            + " sed.type as definition_type, cv.oc_oid as cv_oid,"
            + " cv.name as cv_name, edc.required_crf as cv_required, edc.null_values, crf.name as crf_name"
            + " from " + this.studyEventAndFormMetaTables()
            + this.studyEventAndFormMetaCondition(parentStudyId, studyId, isIncludedSite);
    }

    protected String getStudyEventAndFormMetaOC1_3Sql(int parentStudyId, int studyId, boolean isIncludedSite) {
        return "select sed.ordinal as definition_order, edc.ordinal as crf_order, edc.crf_id, cv.crf_version_id,"
            + " sed.oc_oid as definition_oid, cv.oc_oid as cv_oid,"
            + " sed.description, sed.category, cv.description as version_description, cv.revision_notes,"
            + " crf.oc_oid as crf_oid, edc.null_values, edc.default_version_id, edc.electronic_signature,"
            + " edc.double_entry, edc.hide_crf, edc.participant_form,edc.allow_anonymous_submission,edc.submission_url,case when edc_tag.active is null then false else edc_tag.active end, edc.source_data_verification_code"
            + " from " + this.studyEventAndFormMetaTables()
            + this.studyEventAndFormMetaCondition(parentStudyId, studyId, isIncludedSite);
    }

    protected String studyEventAndFormMetaTables() {
   //     return "study_event_definition sed, event_definition_crf edc, crf, crf_version cv ";
        return "event_definition_crf edc Join crf crf on crf.crf_id = edc.crf_id "
              +" Join crf_version cv on cv.crf_id=crf.crf_id Join study_event_definition sed on sed.study_event_definition_id = edc.study_event_definition_id"
              +"  left Join  event_definition_crf_tag edc_tag on edc_tag.path::text = ((sed.oc_oid::text || '.'::text) || crf.oc_oid::text)";
    }

    protected String studyEventAndFormMetaCondition(int parentStudyId, int studyId, boolean isIncludedSite) {
        return " where sed.study_id = "
        + parentStudyId
        + " and sed.status_id not in (5,7) and "
        + this.getEventDefinitionCrfCondition(studyId, parentStudyId, isIncludedSite)
        + " and edc.status_id not in (5,7) and edc.crf_id = crf.crf_id and crf.status_id not in (5,7) and crf.crf_id = cv.crf_id and (cv.status_id not in (5,7))"
        + " and exists (select ifm.crf_version_id from item_form_metadata ifm, item_group_metadata igm"
        + " where cv.crf_version_id = ifm.crf_version_id and cv.crf_version_id = igm.crf_version_id and ifm.item_id = igm.item_id)"
        + " order by sed.ordinal, edc.ordinal, edc.crf_id, cv.crf_version_id desc";
    }

    
    
    
    protected String getItemDataMaxLengths(String crfVersionIds) {
        return "select item_id, max(length(value)) as max_length from item_data where item_id in ("
            + " select distinct ifm.item_id from item_form_metadata ifm where ifm.crf_version_id in (" + crfVersionIds
            + ")) and length(value) > 0 group by item_id";
    }

    protected String getItemGroupAndItemMetaSql(String crfVersionIds) {
        return "select cv.crf_id, cv.crf_version_id,"
            + " ig.item_group_id, item.item_id, rs.response_set_id, cv.oc_oid as crf_version_oid, ig.oc_oid as item_group_oid, item.oc_oid as item_oid,"
            + " ig.name as item_group_name, item.name as item_name, item.item_data_type_id, ifm.item_header, ifm.left_item_text,"
            + " ifm.right_item_text, ifm.required as item_required, ifm.regexp, ifm.regexp_error_msg, ifm.width_decimal,"
            + " rs.response_type_id, rs.options_text, rs.options_values, rs.label as response_label,"
            + " igm.item_group_header, igm.repeating_group,item.description as item_description, ifm.section_id, ifm.question_number_label from crf_version cv,"
            + " (select crf_version_id, item_id, response_set_id, header as item_header, left_item_text, right_item_text, required, regexp,"
            + " regexp_error_msg, width_decimal, section_id, question_number_label from item_form_metadata where crf_version_id in (" + crfVersionIds + "))ifm, item, response_set rs,"
            + " (select crf_version_id, item_group_id, item_id, header as item_group_header,repeating_group from item_group_metadata where crf_version_id in (" + crfVersionIds
            + "))igm," + " item_group ig "
            + this.getItemGroupAndItemMetaCondition(crfVersionIds);
    }

    protected String getItemGroupAndItemMetaOC1_3Sql(String crfVersionIds) {
        return "select cv.crf_id, cv.crf_version_id,"
            + " ig.item_group_id, item.item_id, rs.response_set_id, cv.oc_oid as crf_version_oid, ig.oc_oid as item_group_oid, item.oc_oid as item_oid,"
            + " ifm.item_header, ifm.subheader, ifm.section_id, ifm.left_item_text, ifm.right_item_text,"
            + " ifm.parent_id, ifm.column_number, ifm.page_number_label, ifm.response_layout, ifm.default_value, item.phi_status, ifm.show_item, "
            + " rs.response_type_id, igm.repeat_number, igm.repeat_max, igm.show_group,orderInForm.item_order,igm.item_group_header from crf_version cv, (select crf_version_id, item_id, response_set_id,"
            + " header as item_header, subheader, section_id, left_item_text, right_item_text,"
            + " parent_id, column_number, page_number_label, response_layout,"
            + " default_value, show_item from item_form_metadata where crf_version_id in (" + crfVersionIds + "))ifm, item, response_set rs,"
            + " (select crf_version_id, item_group_id, item_id, header as item_group_header,"
            + " repeat_number, repeat_max, show_group from item_group_metadata where crf_version_id in ("
            + crfVersionIds + "))igm," + " item_group ig, "
            + this.getOrderInForm(crfVersionIds)
            + this.getItemGroupAndItemMetaConditionalOrderItems(crfVersionIds)
            ;
    }

    /**
     * The form display is determined the way items are ordered in CRF and this information is obtained from OID and nothing hence this approach
     * @param crfVersionIds
     * @return
     */
    protected String getOrderInForm(String crfVersionIds){
    	return "(select ordinal  as item_order,crf_version_id, item_id from item_form_metadata ifmd )orderInForm";
    }
    protected String getItemGroupAndItemMetaCondition(String crfVersionIds) {
        return " where cv.crf_version_id in (" + crfVersionIds + ") and cv.crf_version_id = ifm.crf_version_id"
        + " and ifm.item_id = item.item_id and ifm.response_set_id = rs.response_set_id"
        + " and ifm.item_id = igm.item_id and cv.crf_version_id = igm.crf_version_id and igm.item_group_id = ig.item_group_id"
        + " order by cv.crf_id, cv.crf_version_id desc, ig.item_group_id, item.item_id, rs.response_set_id";
    }

    protected String getItemGroupAndItemMetaConditionalOrderItems(String crfVersionIds) {
        return " where cv.crf_version_id in (" + crfVersionIds + ") and cv.crf_version_id = ifm.crf_version_id"
        + " and ifm.item_id = item.item_id and ifm.response_set_id = rs.response_set_id"
        + " and ifm.item_id = igm.item_id and cv.crf_version_id = igm.crf_version_id and igm.item_group_id = ig.item_group_id"  +
        " and orderInForm.crf_version_id = cv.crf_version_id and orderInForm.item_id = ifm.item_id"
      
        + " order by cv.crf_id, cv.crf_version_id desc, ig.item_group_id, item.item_id, rs.response_set_id";
    }
    protected String getSubjectEventFormSql(String studyIds, String sedIds, String itemIds, String dateConstraint, int datasetItemStatusId) {
        return "select ss.oc_oid as study_subject_oid, sed.ordinal as definition_order, sed.oc_oid as definition_oid,"
            + " sed.repeating as definition_repeating, se.sample_ordinal as sample_ordinal, edc.ordinal as crf_order, "
            + " cv.oc_oid as crf_version_oid, ec.event_crf_id from (select study_event_id, study_event_definition_id, study_subject_id,"
            + " sample_ordinal from study_event where study_event_definition_id in " + sedIds
            + " and study_subject_id in (select ss.study_subject_id from study_subject ss where ss.study_id in (" + studyIds + ") " + dateConstraint
            + ")) se, (select ss.oc_oid, ss.study_subject_id from study_subject ss where ss.study_id in (" + studyIds + ") " + dateConstraint + ") ss,"
            + " study_event_definition sed, event_definition_crf edc,"
            + " (select event_crf_id, crf_version_id, study_event_id from event_crf where event_crf_id in ("
            + getEventCrfIdsByItemDataSql(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId) + ")) ec, crf_version cv"
            + " where sed.study_event_definition_id in " + sedIds
            + " and sed.study_event_definition_id = se.study_event_definition_id and se.study_subject_id = ss.study_subject_id"
            + " and sed.study_event_definition_id = edc.study_event_definition_id and se.study_event_id = ec.study_event_id"
            + " and edc.crf_id = cv.crf_id and ec.crf_version_id = cv.crf_version_id order by ss.oc_oid, sed.ordinal, se.sample_ordinal, edc.ordinal";
    }

    protected String getSubjectEventFormSqlSS(String studyIds, String sedIds, String itemIds, String dateConstraint, int datasetItemStatusId,
            String studySubjectIds) {
        return "select ss.oc_oid as study_subject_oid, sed.ordinal as definition_order, sed.oc_oid as definition_oid,"
            + " sed.repeating as definition_repeating, se.sample_ordinal as sample_ordinal, edc.ordinal as crf_order, "
            + " cv.oc_oid as crf_version_oid, ec.event_crf_id from (select study_event_id, study_event_definition_id, study_subject_id,"
            + " sample_ordinal from study_event where study_event_definition_id in " + sedIds + " and study_subject_id in ( " + studySubjectIds
            + ")) se, (select ss.oc_oid, ss.study_subject_id from study_subject ss where ss.study_subject_id in (" + studySubjectIds + ") " + ") ss,"
            + " study_event_definition sed, event_definition_crf edc,"
            + " (select event_crf_id, crf_version_id, study_event_id from event_crf where event_crf_id in ("
            + getEventCrfIdsByItemDataSqlSS(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId, studySubjectIds) + ")) ec, crf_version cv"
            + " where sed.study_event_definition_id in " + sedIds
            + " and sed.study_event_definition_id = se.study_event_definition_id and se.study_subject_id = ss.study_subject_id"
            + " and sed.study_event_definition_id = edc.study_event_definition_id and se.study_event_id = ec.study_event_id"
            + " and edc.crf_id = cv.crf_id and ec.crf_version_id = cv.crf_version_id order by ss.oc_oid, sed.ordinal, se.sample_ordinal, edc.ordinal";
    }

    protected String getOCSubjectEventFormSql(String studyIds, String sedIds, String itemIds, String dateConstraint, int datasetItemStatusId) {
        return "select ss.oc_oid as study_subject_oid, ss.label, ss.unique_identifier, ss.secondary_label, ss.gender, ss.date_of_birth,"
            + " ss.status_id, ss.sgc_id, ss.sgc_name, ss.sg_name, sed.ordinal as definition_order, sed.oc_oid as definition_oid, sed.repeating as definition_repeating,"
            + " se.sample_ordinal as sample_ordinal, se.se_location, se.date_start, se.date_end, se.start_time_flag,"
            + " se.end_time_flag, se.subject_event_status_id as event_status_id, edc.ordinal as crf_order,"
            + " cv.oc_oid as crf_version_oid, cv.name as crf_version, cv.status_id as cv_status_id, ec.status_id as ec_status_id, ec.event_crf_id, ec.date_interviewed,"
            + " ec.interviewer_name, ec.validator_id from (select study_event_id, study_event_definition_id, study_subject_id, location as se_location,"
            + " sample_ordinal, date_start, date_end, subject_event_status_id, start_time_flag, end_time_flag from study_event "
            + " where study_event_definition_id in "
            + sedIds
            + " and study_subject_id in (select ss.study_subject_id from study_subject ss where ss.study_id in ("
            + studyIds
            + ") "
            + dateConstraint
            + ")) se, ( select st_sub.oc_oid, st_sub.study_subject_id, st_sub.label,"
            + " st_sub.secondary_label, st_sub.subject_id, st_sub.unique_identifier, st_sub.gender, st_sub.date_of_birth, st_sub.status_id,"
            + " sb_g.sgc_id, sb_g.sgc_name, sb_g.sg_id, sb_g.sg_name from (select ss.oc_oid, ss.study_subject_id, ss.label, ss.secondary_label, ss.subject_id,"
            + " s.unique_identifier, s.gender, s.date_of_birth, s.status_id from study_subject ss, subject s where ss.study_id in ("
            + studyIds
            + ") "
            + dateConstraint
            + " and ss.subject_id = s.subject_id)st_sub left join (select sgm.study_subject_id, sgc.study_group_class_id as sgc_id, sgc.name as sgc_name,"
            + " sg.study_group_id as sg_id, sg.name as sg_name from subject_group_map sgm, study_group_class sgc, study_group sg where sgc.study_id in ("
            + studyIds
            + ") and sgm.study_group_class_id = sgc.study_group_class_id and sgc.study_group_class_id = sg.study_group_class_id"
            + " and sgm.study_group_id = sg.study_group_id) sb_g on st_sub.study_subject_id = sb_g.study_subject_id) ss, "
            + " study_event_definition sed, event_definition_crf edc,"
            + " (select event_crf_id, crf_version_id, study_event_id, status_id, date_interviewed, interviewer_name, validator_id from event_crf where event_crf_id in ("
            + getEventCrfIdsByItemDataSql(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId)
            + ")) ec, crf_version cv"
            + " where sed.study_event_definition_id in "
            + sedIds
            + " and sed.study_event_definition_id = se.study_event_definition_id and se.study_subject_id = ss.study_subject_id"
            + " and sed.study_event_definition_id = edc.study_event_definition_id and se.study_event_id = ec.study_event_id"
            + " and edc.crf_id = cv.crf_id and ec.crf_version_id = cv.crf_version_id order by ss.oc_oid, sed.ordinal, se.sample_ordinal, edc.ordinal, ss.sgc_id";
    }

    protected String getOCSubjectEventFormSqlSS(String studyIds, String sedIds, String itemIds, String dateConstraint, int datasetItemStatusId,
            String studySubjectIds) {
        return "select ss.oc_oid as study_subject_oid, ss.label, ss.unique_identifier, ss.secondary_label, ss.gender, ss.date_of_birth,"
            + " ss.status_id, ss.sgc_id, ss.sgc_name, ss.sg_name, sed.ordinal as definition_order, sed.oc_oid as definition_oid, sed.repeating as definition_repeating,"
            + " se.sample_ordinal as sample_ordinal, se.se_location, se.date_start, se.date_end, se.start_time_flag,"
            + " se.end_time_flag, se.subject_event_status_id as event_status_id, edc.ordinal as crf_order,"
            + " cv.oc_oid as crf_version_oid, cv.name as crf_version, cv.status_id as cv_status_id, ec.status_id as ec_status_id, ec.event_crf_id, ec.date_interviewed,"
            + " ec.interviewer_name, ec.validator_id from (select study_event_id, study_event_definition_id, study_subject_id, location as se_location,"
            + " sample_ordinal, date_start, date_end, subject_event_status_id, start_time_flag, end_time_flag from study_event "
            + " where study_event_definition_id in "
            + sedIds
            + " and study_subject_id in ("
            + studySubjectIds
            + ")) se, ( select st_sub.oc_oid, st_sub.study_subject_id, st_sub.label,"
            + " st_sub.secondary_label, st_sub.subject_id, st_sub.unique_identifier, st_sub.gender, st_sub.date_of_birth, st_sub.status_id,"
            + " sb_g.sgc_id, sb_g.sgc_name, sb_g.sg_id, sb_g.sg_name from (select ss.oc_oid, ss.study_subject_id, ss.label, ss.secondary_label, ss.subject_id,"
            + " s.unique_identifier, s.gender, s.date_of_birth, s.status_id from study_subject ss, subject s where ss.study_subject_id in ("
            + studySubjectIds
            + ") "
            + " and ss.subject_id = s.subject_id)st_sub left join (select sgm.study_subject_id, sgc.study_group_class_id as sgc_id, sgc.name as sgc_name,"
            + " sg.study_group_id as sg_id, sg.name as sg_name from subject_group_map sgm, study_group_class sgc, study_group sg where sgc.study_id in ("
            + studyIds
            + ") and sgm.study_group_class_id = sgc.study_group_class_id and sgc.study_group_class_id = sg.study_group_class_id"
            + " and sgm.study_group_id = sg.study_group_id) sb_g on st_sub.study_subject_id = sb_g.study_subject_id) ss, "
            + " study_event_definition sed, event_definition_crf edc,"
            + " (select event_crf_id, crf_version_id, study_event_id, status_id, date_interviewed, interviewer_name, validator_id from event_crf where event_crf_id in ("
            + getEventCrfIdsByItemDataSqlSS(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId, studySubjectIds)
            + ")) ec, crf_version cv"
            + " where sed.study_event_definition_id in "
            + sedIds
            + " and sed.study_event_definition_id = se.study_event_definition_id and se.study_subject_id = ss.study_subject_id"
            + " and sed.study_event_definition_id = edc.study_event_definition_id and se.study_event_id = ec.study_event_id"
            + " and edc.crf_id = cv.crf_id and ec.crf_version_id = cv.crf_version_id order by ss.oc_oid, sed.ordinal, se.sample_ordinal, edc.ordinal, ss.sgc_id";
    }

    protected String getEventGroupItemSqlSS(String studyIds, String sedIds, String itemIds, String dateConstraint, int datasetItemStatusId,
            String studySubjectIds) {
        String ecStatusConstraint = this.getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = this.getItemDataStatusConstraint(datasetItemStatusId);
        return "select cvidata.event_crf_id, ig.item_group_id, ig.oc_oid as item_group_oid, ig.name as item_group_name,"
            + " cvidata.item_id, cvidata.item_oid, cvidata.item_data_ordinal, cvidata.value, cvidata.item_data_type_id, cvidata.item_data_id"
            + " from (select ec.event_crf_id, ec.crf_version_id, item.item_id, item.oc_oid as item_oid,"
            + " idata.ordinal as item_data_ordinal, idata.value as value, item.item_data_type_id, idata.item_data_id as item_data_id from item,"
            + " (select event_crf_id, item_id, ordinal, value, item_data_id from item_data where (status_id "
            + itStatusConstraint
            + ")"
            + " and event_crf_id in (select distinct event_crf_id from event_crf where study_subject_id in (select distinct"
            + " ss.study_subject_id from study_subject ss where ss.study_subject_id in ("
            + studySubjectIds
            + ") "
            + dateConstraint
            + ") and study_event_id"
            + " in (select distinct study_event_id from study_event"
            + " where study_event_definition_id in "
            + sedIds
            + " and study_subject_id in ("
            + " select distinct ss.study_subject_id from study_subject ss where ss.study_subject_id in ("
            + studySubjectIds
            + ") "
            + dateConstraint
            + "))))idata,"
            + " (select event_crf_id, crf_version_id from event_crf where status_id "
            + ecStatusConstraint
            + ")ec"
            + " where item.item_id in "
            + itemIds
            + " and length(idata.value) > 0 and item.item_id = idata.item_id and idata.event_crf_id = ec.event_crf_id"
            + " order by ec.event_crf_id, ec.crf_version_id, item.item_id, idata.ordinal) cvidata, item_group_metadata igm,"
            + " item_group ig where cvidata.crf_version_id = igm.crf_version_id and cvidata.item_id = igm.item_id"
            + " and igm.item_group_id = ig.item_group_id order by cvidata.event_crf_id, ig.item_group_id, cvidata.item_id, cvidata.item_data_ordinal";
    }

    protected String getEventGroupItemSql(String studyIds, String sedIds, String itemIds, String dateConstraint, int datasetItemStatusId) {
        String ecStatusConstraint = this.getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = this.getItemDataStatusConstraint(datasetItemStatusId);
        return "select cvidata.event_crf_id, ig.item_group_id, ig.oc_oid as item_group_oid, ig.name as item_group_name,"
            + " cvidata.item_id, cvidata.item_oid, cvidata.item_data_ordinal, cvidata.value, cvidata.item_data_type_id, cvidata.item_data_id"
            + " from (select ec.event_crf_id, ec.crf_version_id, item.item_id, item.oc_oid as item_oid,"
            + " idata.ordinal as item_data_ordinal, idata.value as value, item.item_data_type_id, idata.item_data_id from item,"
            + " (select event_crf_id, item_id, ordinal, item_data_id, value from item_data where (status_id "
            + itStatusConstraint
            + ")"
            + " and event_crf_id in (select distinct event_crf_id from event_crf where study_subject_id in (select distinct"
            + " ss.study_subject_id from study_subject ss where ss.study_id in ("
            + studyIds
            + ") "
            + dateConstraint
            + ") and study_event_id"
            + " in (select distinct study_event_id from study_event"
            + " where study_event_definition_id in "
            + sedIds
            + " and study_subject_id in ("
            + " select distinct ss.study_subject_id from study_subject ss where ss.study_id in ("
            + studyIds
            + ") "
            + dateConstraint
            + "))))idata,"
            + " (select event_crf_id, crf_version_id from event_crf where status_id "
            + ecStatusConstraint
            + ")ec"
            + " where item.item_id in "
            + itemIds
            + " and length(idata.value) > 0 and item.item_id = idata.item_id and idata.event_crf_id = ec.event_crf_id"
            + " order by ec.event_crf_id, ec.crf_version_id, item.item_id, idata.ordinal) cvidata, item_group_metadata igm,"
            + " item_group ig where cvidata.crf_version_id = igm.crf_version_id and cvidata.item_id = igm.item_id"
            + " and igm.item_group_id = ig.item_group_id order by cvidata.event_crf_id, ig.item_group_id, cvidata.item_id, cvidata.item_data_ordinal";
    }

    // should mapped to non-completed
    protected String getEventCrfIdsByItemDataSql(String studyIds, String sedIds, String itemIds, String dateConstraint, int datasetItemStatusId) {
        String ecStatusConstraint = getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = this.getItemDataStatusConstraint(datasetItemStatusId);
        return "select distinct idata.event_crf_id from item_data idata" + " where idata.item_id in " + itemIds + " and (idata.status_id " + itStatusConstraint
            + ")" + " and idata.event_crf_id in (select event_crf_id from event_crf where study_subject_id in"
            + " (select ss.study_subject_id from study_subject ss WHERE ss.study_id in (" + studyIds + ") " + dateConstraint + ")"
            + " and study_event_id in (select study_event_id from study_event where study_event_definition_id in " + sedIds
            + " and study_subject_id in (select ss.study_subject_id from study_subject ss where ss.study_id in (" + studyIds + ") " + dateConstraint + "))"
            + " and (status_id " + ecStatusConstraint + ")) and length(value) > 0";
    }

    protected String getEventCrfIdsByItemDataSqlSS(String studyIds, String sedIds, String itemIds, String dateConstraint, int datasetItemStatusId,
            String studySubjectIds) {
        String ecStatusConstraint = getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = this.getItemDataStatusConstraint(datasetItemStatusId);
        return "select distinct idata.event_crf_id from item_data idata" + " where idata.item_id in " + itemIds + " and (idata.status_id " + itStatusConstraint
            + ")" + " and idata.event_crf_id in (select event_crf_id from event_crf where study_subject_id in"
            + " (select ss.study_subject_id from study_subject ss WHERE ss.study_subject_id in (" + studySubjectIds + ") " + ")"
            + " and study_event_id in (select study_event_id from study_event where study_event_definition_id in " + sedIds
            + " and study_subject_id in (select ss.study_subject_id from study_subject ss where ss.study_subject_id in (" + studySubjectIds + ") " + "))"
            + " and (status_id " + ecStatusConstraint + ")) and length(value) > 0";
    }

    protected String getEventDefinitionCrfCondition(int studyId, int parentStudyId, boolean isIncludedSite) {
        if (isIncludedSite) {
            return "edc.study_id = " + studyId;
        } else {
            if (studyId == parentStudyId) {
                return "edc.study_id = " + studyId;
            } else {
                return "((edc.study_id = " + parentStudyId + " or edc.study_id = " + studyId + ") and edc.event_definition_crf_id not in "
                    + "(select parent_id from event_definition_crf e where e.study_id = " + studyId + "))";
            }
        }
    }

    protected String getStudyMeasurementUnitsSql(int studyId) {
        return "select distinct mu.oc_oid as mu_oid, mu.name from event_definition_crf edc, crf_version cv, versioning_map vm, item, measurement_unit mu"
            + " where edc.study_id =" + studyId + " and edc.crf_id = cv.crf_id" + " and cv.crf_version_id = vm.crf_version_id and vm.item_id = item.item_id "
            + " and item.units = mu.name order by mu.oc_oid";
    }

    protected String getStudyMeasurementUnitsSql(String crfVersionOid) {
        return "select distinct mu.oc_oid as mu_oid, mu.name from  crf_version cv, versioning_map vm, item, measurement_unit mu " +
        		"where cv.oc_OID in (\'"+crfVersionOid +"\')   and cv.crf_version_id = vm.crf_version_id and vm.item_id = item.item_id " +
        		"and item.units = mu.name order by mu.oc_oid";
    }
  
    protected String getEventGroupItemWithUnitSql(String studyIds, String sedIds, String itemIds, String dateConstraint, int datasetItemStatusId,
            String studySubjectIds) {
        return "select cvit.*, mu.oc_oid as mu_oid from ("
                + this.getEventGroupItemSqlSS(studyIds, sedIds, itemIds, dateConstraint, datasetItemStatusId, studySubjectIds)
                + " )cvit left join (select item.item_id, mu.oc_oid from versioning_map vm, item, measurement_unit mu where vm.item_id in " + itemIds
                + " and vm.item_id = item.item_id and item.units = mu.name )mu on cvit.item_id = mu.item_id"
                + " ORDER BY cvit.event_crf_id, cvit.item_group_id, cvit.item_id, cvit.item_data_ordinal";
    }

    protected String getItemGroupAndItemMetaWithUnitSql(String crfVersionIds) {
        return "select cv.*, mu.oc_oid as mu_oid from (" + this.getItemGroupAndItemMetaSql(crfVersionIds) + ")cv left join"
            + " (select item.item_id, mu.oc_oid from item, measurement_unit mu where item.item_id in (select vm.item_id from versioning_map vm"
            + " where vm.crf_version_id in (" + crfVersionIds + "))"
            + " and item.units = mu.name )mu on cv.item_id = mu.item_id ";
    }

    protected String getNullValueCVsSql(String studyId) {
        return "select sed.oc_oid as definition_oid, cv.oc_oid as crf_version_oid, edc.null_values from study_event_definition sed, event_definition_crf edc, crf_version cv"
            + " where edc.study_id = "
            + studyId
            + " and length(edc.null_values) > 0"
            + " and sed.study_event_definition_id = edc.study_event_definition_id"
            + " and edc.crf_id = cv.crf_id";
    }

    protected String getStudyUsersSql(String studyId) {
        return "select distinct ua.user_id, ua.first_name, ua.last_name, ua.institutional_affiliation" + " from user_account ua, study_user_role sur"
            + " where sur.study_id = " + studyId + " and sur.user_name = ua.user_name order by ua.user_id";
    }

    protected String getOCSubjectDataAuditsSql(String studySubjectOids) {
        return "(select ss.oc_oid as study_subject_oid, ale.audit_id, alet.name, ale.user_id,"
            + " ale.audit_date, ale.reason_for_change, ale.old_value, ale.new_value, ale.audit_log_event_type_id"
            + " from audit_log_event ale, study_subject ss, audit_log_event_type alet" + " where audit_table = 'subject' and ss.oc_oid in (" + studySubjectOids
            + ") and ss.subject_id = ale.entity_id" + " and ale.audit_log_event_type_id = alet.audit_log_event_type_id" + " ) union "
            + " (select ss.oc_oid as study_subject_oid, ale.audit_id,  alet.name, ale.user_id,"
            + " ale.audit_date, ale.reason_for_change, ale.old_value, ale.new_value, ale.audit_log_event_type_id"
            + " from audit_log_event ale, study_subject ss, audit_log_event_type alet" + " where audit_table = 'study_subject' and ss.oc_oid in ("
            + studySubjectOids + ") and ss.study_subject_id = ale.entity_id" + " and ale.audit_log_event_type_id = alet.audit_log_event_type_id" + " ) union "
            + " (select ss.oc_oid as study_subject_oid, ale.audit_id, alet.name, ale.user_id,"
            + " ale.audit_date, ale.reason_for_change, ale.old_value, ale.new_value, ale.audit_log_event_type_id"
            + " from audit_log_event ale, study_subject ss, subject_group_map sgm, audit_log_event_type alet"
            + " where audit_table = 'subject_group_map' and ss.oc_oid in (" + studySubjectOids + ") and ss.study_subject_id = sgm.study_subject_id"
            + " and ale.entity_id = sgm.subject_group_map_id and ale.audit_log_event_type_id = alet.audit_log_event_type_id"
            + " ) order by study_subject_oid, audit_id asc";
    }

    protected String getOCEventDataAuditsSql(String studySubjectOids) {
        return "select ss.oc_oid as study_subject_oid, sed.oc_oid as definition_oid, ale.audit_id,"
            + " alet.name, ale.user_id, ale.audit_date, ale.reason_for_change, ale.old_value, ale.new_value," + " ale.audit_log_event_type_id"
            + " from audit_log_event ale, study_subject ss, study_event se, study_event_definition sed, audit_log_event_type alet"
            + " where audit_table = 'study_event' and ss.oc_oid in (" + studySubjectOids + ") and ss.study_subject_id = se.study_subject_id"
            + " and ale.entity_id = se.study_event_id" + " and se.study_event_definition_id = sed.study_event_definition_id"
            + " and ale.audit_log_event_type_id = alet.audit_log_event_type_id" + " order by ss.oc_oid, sed.oc_oid, ale.audit_id asc";
    }

    protected String getOCFormDataAuditsSql(String studySubjectOids, String ecIds) {
        return "select ale.entity_id as event_crf_id, ale.audit_id, alet.name, ale.user_id,"
            + " ale.audit_date, ale.reason_for_change, ale.old_value, ale.new_value, ale.audit_log_event_type_id"
            + " from audit_log_event ale, study_subject ss, event_crf ec, audit_log_event_type alet"
            + " where audit_table = 'event_crf' and ec.event_crf_id in (" + ecIds + ") and ss.oc_oid in (" + studySubjectOids
            + ") and ss.study_subject_id = ec.study_subject_id" + " and ale.entity_id = ec.event_crf_id"
            + " and ale.audit_log_event_type_id = alet.audit_log_event_type_id" + " order by ale.entity_id asc";
    }

    protected String getOCItemDataAuditsSql(String idataIds) {
        return "select ale.entity_id as item_data_id, ale.audit_id, alet.name, ale.user_id,"
            + " ale.audit_date, ale.reason_for_change, ale.old_value, ale.new_value, ale.audit_log_event_type_id"
            + " from audit_log_event ale, audit_log_event_type alet" + " where audit_table = 'item_data' and ale.entity_id in (" + idataIds
            + ") and ale.audit_log_event_type_id = alet.audit_log_event_type_id" + " order by ale.entity_id asc";
    }

    protected String getOCSubjectDataDNsSql(String studySubjectOids) {
        return "(select ss.oc_oid as study_subject_oid, dn.parent_dn_id, dn.discrepancy_note_id as dn_id, dn.description, dn.detailed_notes, "
            + " dn.owner_id, dn.date_created, rs.name as status, dnt.name"
            + " from discrepancy_note dn, dn_subject_map dnsm, study_subject ss, discrepancy_note_type dnt, resolution_status rs"
            + " where dn.entity_type = 'subject'" + " and dn.discrepancy_note_id = dnsm.discrepancy_note_id and ss.oc_oid in (" + studySubjectOids
            + ") and ss.subject_id = dnsm.subject_id and dn.resolution_status_id = rs.resolution_status_id"
            + " and dn.discrepancy_note_type_id = dnt.discrepancy_note_type_id) union"
            + "(select ss.oc_oid as study_subject_oid, dn.parent_dn_id, dn.discrepancy_note_id as dn_id, dn.description, dn.detailed_notes, "
            + " dn.owner_id, dn.date_created, rs.name as status, dnt.name"
            + " from discrepancy_note dn, dn_study_subject_map dnssm, study_subject ss, discrepancy_note_type dnt, resolution_status rs"
            + " where dn.entity_type = 'studySub'" + " and dn.discrepancy_note_id = dnssm.discrepancy_note_id and ss.oc_oid in (" + studySubjectOids
            + ") and ss.study_subject_id = dnssm.study_subject_id and dn.resolution_status_id = rs.resolution_status_id"
            + " and dn.discrepancy_note_type_id = dnt.discrepancy_note_type_id" + ") order by study_subject_oid, parent_dn_id, dn_id";
    }

    protected String getOCEventDataDNsSql(String definitionOids, String studySubjectOids) {
        return "select ss.oc_oid as study_subject_oid, sed.oc_oid as definition_oid, dn.parent_dn_id, dn.discrepancy_note_id as dn_id, dn.description, dn.detailed_notes, "
            + " dn.owner_id, dn.date_created, rs.name as status, dnt.name"
            + " from discrepancy_note dn, dn_study_event_map dnsem, study_event se, study_event_definition sed, study_subject ss, discrepancy_note_type dnt, resolution_status rs"
            + " where dn.entity_type = 'studyEvent'"
            + " and dn.discrepancy_note_id = dnsem.discrepancy_note_id and dnsem.study_event_id = se.study_event_id"
            + " and sed.oc_oid in ("
            + definitionOids
            + ") and se.study_event_definition_id = sed.study_event_definition_id"
            + " and ss.oc_oid in ("
            + studySubjectOids
            + ") and se.study_subject_id = ss.study_subject_id"
            + " and dn.resolution_status_id = rs.resolution_status_id"
            + " and dn.discrepancy_note_type_id = dnt.discrepancy_note_type_id" + " order by ss.oc_oid, sed.oc_oid, dn.parent_dn_id, dn.discrepancy_note_id";
    }

    protected String getOCFormDataDNsSql(String ecIds) {
        return "select ec.event_crf_id, dn.parent_dn_id, dn.discrepancy_note_id as dn_id, dn.description, dn.detailed_notes, dn.owner_id, dn.date_created, rs.name as status, dnt.name"
            + " from discrepancy_note dn, dn_event_crf_map dnecm, event_crf ec, discrepancy_note_type dnt, resolution_status rs"
            + " where dn.entity_type = 'eventCrf'"
            + " and dnecm.event_crf_id in ("
            + ecIds
            + ") and dn.discrepancy_note_id = dnecm.discrepancy_note_id"
            + " and ec.event_crf_id in ("
            + ecIds
            + ") and dnecm.event_crf_id = ec.event_crf_id"
            + " and dn.resolution_status_id = rs.resolution_status_id"
            + " and dn.discrepancy_note_type_id = dnt.discrepancy_note_type_id" + " order by ec.event_crf_id, dn.parent_dn_id, dn.discrepancy_note_id";
    }

    protected String getOCItemDataDNsSql(String idataIds) {
        return "select dnidm.item_data_id, dn.parent_dn_id, dn.discrepancy_note_id as dn_id, dn.description, dn.detailed_notes, dn.owner_id, dn.date_created, rs.name as status, dnt.name"
            + " from discrepancy_note dn, dn_item_data_map dnidm, discrepancy_note_type dnt, resolution_status rs"
            + " where (dn.entity_type = 'itemData' or dn.entity_type = 'itemdata')"
            + " and dnidm.item_data_id in ("
            + idataIds
            + ") and dn.discrepancy_note_id = dnidm.discrepancy_note_id"
            + " and dn.resolution_status_id = rs.resolution_status_id"
            + " and dn.discrepancy_note_type_id = dnt.discrepancy_note_type_id" + " order by dnidm.item_data_id, dn.parent_dn_id, dn.discrepancy_note_id";
    }

    protected String getItemCVOIDsSql(String crfVersionIds) {
        return "select cv.crf_id, cv.crf_version_id, item.item_id, cv.oc_oid as cv_oid, item.oc_oid as item_oid"
            + " from crf_version cv, versioning_map vm, item" + " where vm.crf_version_id in (" + crfVersionIds + ")"
            + " and vm.crf_version_id = cv.crf_version_id and vm.item_id = item.item_id" + " order by cv.crf_id, cv.crf_version_id desc, item.item_id";
    }

    protected String getSectionLabelsSql(String sectionIds) {
        return "select section_id, label from section where section_id in ("+sectionIds+")";
    }

    protected String getParentItemOIDsSql(String crfVersionIds) {
        return "select item_id, oc_oid from item where item_id in (select distinct parent_id from item_form_metadata ifm"
            + " where crf_version_id in (" + crfVersionIds + ") and ifm.parent_id > 0 )";
    }

    protected String getSCDsSql(String crfVersionIds) {
        return "select cv.crf_id, cv.crf_version_id, item.item_id, cv.oc_oid as crf_version_oid, item.oc_oid as item_oid,"
            + " ifm.control_item_name, ifm.option_value, ifm.message from crf_version cv,"
            + " (select im.crf_version_id, im.item_id, scd.control_item_name, scd.option_value, scd.message"
            + " from item_form_metadata im, scd_item_metadata scd where im.crf_version_id in ("
            + crfVersionIds + ") and im.item_form_metadata_id = scd.scd_item_form_metadata_id)ifm, item"
            + " where cv.crf_version_id in ("+ crfVersionIds + ") and cv.crf_version_id = ifm.crf_version_id"
            + " and ifm.item_id = item.item_id order by cv.crf_id, cv.crf_version_id desc, item.item_id";
    }

    protected String getErasedScoreItemDataIdsSql(String itemIds, String idataIds) {
        return "select idata.item_data_id from item_data idata where idata.value='<erased>'"
            + " and idata.item_data_id in (" + idataIds + ") and idata.item_id in ("
            + " select distinct ifm.item_id from item_form_metadata ifm where ifm.response_set_id in ("
            + " select rs.response_set_id from response_set rs where rs.response_type_id in (8,9))"
            + "      and ifm.item_id in " + itemIds + " limit 999)";
    }
	
    
    protected String getSectionDetails(String CrfVersionOID)
    {
    	return "select section_id, label,title,subtitle,instructions,page_number_label from section section where crf_version_id=?";
    }
    
    
    
    
    
    
}
