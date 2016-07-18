/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.view;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Maintain the breadcrumbs on the page, remain seamless, for example, it gets
 * set when the Page gets set in the forwardPage() method in the
 * SecureController servlet, Keep track of metadata being sent in the request,
 * so that users can go back down the bread crumb trail.
 *
 * @author thickerson
 *
 */
public class BreadcrumbTrail {
    private ArrayList trail = new ArrayList();

    public BreadcrumbTrail() {

    }

    public BreadcrumbTrail(ArrayList trail) {

        this.trail = trail;

    }

    /**
     * @return Returns the trail.
     */
    public ArrayList getTrail() {
        return trail;
    }

    /**
     * @param trail
     *            The trail to set.
     */
    public void setTrail(ArrayList trail) {
        this.trail = trail;
    }

    /**
     * method to be called right before forwardPage() in the SecureController.
     * Generates an arraylist of breadcrumb beans, which is then set to the
     * request/session. Has the possibility of getting quite long, since we will
     * be setting up all breadcrumb bean configurations here based on the Page
     * submitted to us.
     *
     * @param jspPage
     *            the page which is the new target.
     * @param request
     *            the HTTP request which we will construct the URL with.
     * @return ArrayList of breadcrumb
     */
    public ArrayList generateTrail(Page jspPage, HttpServletRequest request) {

        Locale locale = LocaleResolver.getLocale(request);
        ResourceBundle resworkflow = ResourceBundleProvider.getWorkflowBundle(locale);

        try {
            // ArrayList newTrail = new ArrayList();
            if (jspPage.equals(Page.CREATE_DATASET_1)) {
                // when a user first steps onto the trail,
                // it is created new for them;
                // further on down the trail,
                // we update the statuses and collect URL variables
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("extract_datasets"), "ExtractDatasetsMain", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("create_dataset_instructions"), "CreateDataset" + this.generateURLString(request),
                        Status.PENDING));// 0
                trail.add(new BreadcrumbBean(resworkflow.getString("select_items_event"), "CreateDataset", Status.UNAVAILABLE));// 1
                trail.add(new BreadcrumbBean(resworkflow.getString("define_temporal_scope"), "CreateDataset", Status.UNAVAILABLE));// 2
                // trail.add(
                // new BreadcrumbBean("Select Filter",
                // "ApplyFilter",
                // Status.UNAVAILABLE));//3
                trail.add(new BreadcrumbBean(resworkflow.getString("specify_dataset_properties"), "CreateDataset", Status.UNAVAILABLE));// 3
                trail.add(new BreadcrumbBean(resworkflow.getString("confirm_dataset_properties"), "CreateDataset", Status.UNAVAILABLE));// 4
                trail.add(new BreadcrumbBean(resworkflow.getString("generate_dataset"), "CreateDataset", Status.UNAVAILABLE));// 5, 6
                // items
                // total

            } else if (jspPage.equals(Page.CREATE_DATASET_2)) {
                // BreadcrumbBean bcb = (BreadcrumbBean)trail.remove(0);
                // bcb.setStatus(Status.AVAILABLE);
                // trail.add(0, bcb);
                openBreadcrumbs(2);
                BreadcrumbBean bcb2 = (BreadcrumbBean) trail.remove(2);
                bcb2.setStatus(Status.PENDING);
                bcb2.setUrl("CreateDataset" + this.generateURLString(request));
                trail.add(2, bcb2);
                closeRestOfTrail(2);
            } else if (jspPage.equals(Page.CREATE_DATASET_3)) {
                if (trail.size() == 6) {
                    openBreadcrumbs(1);
                    ((BreadcrumbBean) trail.get(1)).setStatus(Status.UNAVAILABLE);
                    ((BreadcrumbBean) trail.get(2)).setStatus(Status.PENDING);
                    ((BreadcrumbBean) trail.get(2)).setUrl("CreateDataset" + this.generateURLString(request));
                    closeRestOfTrail(2);
                } else {
                    // BreadcrumbBean bcb = (BreadcrumbBean)trail.remove(1);
                    // bcb.setStatus(Status.AVAILABLE);
                    // trail.add(1, bcb);
                    openBreadcrumbs(3);
                    BreadcrumbBean bcb2 = (BreadcrumbBean) trail.remove(3);
                    bcb2.setStatus(Status.PENDING);
                    bcb2.setUrl("CreateDataset" + this.generateURLString(request));
                    trail.add(3, bcb2);
                    closeRestOfTrail(3);
                }
            } else if (jspPage.equals(Page.CREATE_DATASET_APPLY_FILTER) || jspPage.equals(Page.APPLY_FILTER)) {
                // CREATE_DATASET_APPLY_FILTER might be bogus, tbh
                // BreadcrumbBean bcb = (BreadcrumbBean)trail.remove(2);
                // bcb.setStatus(Status.AVAILABLE);
                // trail.add(2, bcb);
                openBreadcrumbs(4);
                BreadcrumbBean bcb2 = (BreadcrumbBean) trail.remove(4);
                bcb2.setStatus(Status.PENDING);
                bcb2.setUrl("ApplyFilter" + this.generateURLString(request));
                trail.add(4, bcb2);
                closeRestOfTrail(4);
            } else if (jspPage.equals(Page.CREATE_DATASET_4)) {
                // BreadcrumbBean bcb = (BreadcrumbBean)trail.remove(3);
                // bcb.setStatus(Status.AVAILABLE);
                // trail.add(3, bcb);
                openBreadcrumbs(4);
                BreadcrumbBean bcb2 = (BreadcrumbBean) trail.remove(4);
                bcb2.setStatus(Status.PENDING);
                bcb2.setUrl("CreateDataset" + this.generateURLString(request));
                trail.add(4, bcb2);
                closeRestOfTrail(4);
            } else if (jspPage.equals(Page.CONFIRM_DATASET)) {
                // BreadcrumbBean bcb = (BreadcrumbBean)trail.remove(4);
                // bcb.setStatus(Status.AVAILABLE);
                // trail.add(4, bcb);
                openBreadcrumbs(5);
                BreadcrumbBean bcb2 = (BreadcrumbBean) trail.remove(5);
                bcb2.setStatus(Status.PENDING);
                bcb2.setUrl("CreateDataset" + this.generateURLString(request));
                trail.add(5, bcb2);
                closeRestOfTrail(5);
            } else if (jspPage.equals(Page.EXPORT_DATASETS) && trail.size() == 7) {
                // i.e. you have the end of the trail here with create dataset
                // BreadcrumbBean bcb = (BreadcrumbBean)trail.remove(5);
                // bcb.setStatus(Status.AVAILABLE);
                // trail.add(5, bcb);
                openBreadcrumbs(6);
                BreadcrumbBean bcb2 = (BreadcrumbBean) trail.remove(6);
                bcb2.setStatus(Status.PENDING);
                bcb2.setUrl("CreateDataset" + this.generateURLString(request));
                trail.add(6, bcb2);
                closeRestOfTrail(6);
            } else if (jspPage.equals(Page.EXPORT_DATASETS) && trail.size() != 7) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("extract_datasets"), "ExtractDatasetsMain", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("download_data"), "ExportDataset" + this.generateURLString(request), Status.PENDING));
            } else if (jspPage.equals(Page.CREATE_FILTER_SCREEN_2)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("extract_datasets"), "ExtractDatasetsMain", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("instructions"), "CreateFiltersOne" + this.generateURLString(request), Status.PENDING));
                trail.add(new BreadcrumbBean(resworkflow.getString("select_CRF"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("select_section"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("select_parameters"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("specify_criteria"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("export"), "ExportDataset", Status.UNAVAILABLE));
            } else if (jspPage.equals(Page.CREATE_FILTER_SCREEN_3)) {
                trail =
                    advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("select_CRF"), "CreateFiltersTwo" + this.generateURLString(request),
                            Status.PENDING), 2);
                closeRestOfTrail(2);
            } else if (jspPage.equals(Page.CREATE_FILTER_SCREEN_3_1)) {
                trail =
                    advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("select_section"), "CreateFiltersTwo" + this.generateURLString(request),
                            Status.PENDING), 3);
                closeRestOfTrail(3);
            } else if (jspPage.equals(Page.CREATE_FILTER_SCREEN_3_2)) {
                trail =
                    advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("select_parameters"), "CreateFiltersTwo" + this.generateURLString(request),
                            Status.PENDING), 4);
                closeRestOfTrail(4);
            } else if (jspPage.equals(Page.CREATE_FILTER_SCREEN_4)) {
                trail =
                    advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("specify_criteria"), "CreateFiltersTwo" + this.generateURLString(request),
                            Status.PENDING), 5);
                closeRestOfTrail(5);
            }
            /*
             * else if (jspPage.equals(Page.CREATE_FILTER_SCREEN_2) ||
             * jspPage.equals(Page.CREATE_FILTER_SCREEN_3) ||
             * jspPage.equals(Page.CREATE_FILTER_SCREEN_3_1) ||
             * jspPage.equals(Page.CREATE_FILTER_SCREEN_3_2)) { trail = new
             * ArrayList(); trail.add(new BreadcrumbBean("Extract Datasets",
             * "ExtractDatasetsMain", Status.AVAILABLE)); trail.add( new
             * BreadcrumbBean("Select Study Events", "CreateFiltersTwo"+
             * this.generateURLString(request), Status.PENDING)); trail.add( new
             * BreadcrumbBean("Specify Dataset Metadata", "CreateFiltersThree",
             * Status.UNAVAILABLE)); trail.add( new BreadcrumbBean("Export",
             * "ExportDataset", Status.UNAVAILABLE)); } else if
             * (jspPage.equals(Page.CREATE_FILTER_SCREEN_4)) {
             *
             * trail = this.advanceTrail(trail, new BreadcrumbBean("Specify
             * Dataset Metadata", "CreateFiltersThree"+
             * this.generateURLString(request), Status.PENDING),2);
             * closeRestOfTrail(2); }
             */
            else if (jspPage.equals(Page.SUBMIT_DATA)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("submit_data"), "ListStudySubjectsSubmit", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_STUDY_SUBJECT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_subjects"), "ListStudySubject", Status.AVAILABLE));
                if (request.getAttribute("id") != null) {
                    trail.add(new BreadcrumbBean(resworkflow.getString("view_study_subject"), "ViewStudySubject?id=" + (String) request.getAttribute("id"),
                            Status.AVAILABLE));
                } else {
                    trail.add(new BreadcrumbBean(resworkflow.getString("view_study_subject"), "ViewStudySubject" + this.generateURLString(request),
                            Status.AVAILABLE));
                }
            } else if (jspPage.equals(Page.UPDATE_STUDY_EVENT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_subjects"), "ListStudySubject", Status.AVAILABLE));
                // trail.add(new
                // BreadcrumbBean(resworkflow.getString("view_study_subject"),
                // "ViewStudySubject" + this.generateURLString(request),
                // Status.AVAILABLE));
                if (request.getAttribute("id") != null) {
                    trail.add(new BreadcrumbBean(resworkflow.getString("view_study_subject"), "ViewStudySubject?module=manage&id="
                        + (String) request.getAttribute("id"), Status.AVAILABLE));
                } else {
                    trail.add(new BreadcrumbBean(resworkflow.getString("view_study_subject"), "ViewStudySubject" + this.generateURLString(request),
                            Status.AVAILABLE));
                }
                trail.add(new BreadcrumbBean(resworkflow.getString("update_study_event"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.INSTRUCTIONS_ENROLL_SUBJECT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("submit_data"), "ListStudySubjectsSubmit", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("enroll_subject_instructions"), "AddNewSubject?instr=1", Status.PENDING));
                trail.add(new BreadcrumbBean(resworkflow.getString("enroll_subject"), "AddNewSubject", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("add_new_study_event"), "CreateNewStudyEvent", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("study_event_overview"), "EnterDataForStudyEvent", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("event_CRF_data_submission"), "TableOfContents", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("data_entry"), "InitialDataEntry", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("mark_event_CRF_complete"), "MarkEventCRFComplete", Status.UNAVAILABLE));
            } else if (jspPage.equals(Page.ADD_NEW_SUBJECT)) {
                trail = advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("enroll_subject"), "AddNewSubject", Status.PENDING), 2);
                closeRestOfTrail(2);
            } else if (jspPage.equals(Page.CREATE_NEW_STUDY_EVENT)) {
                if (!containsServlet("AddNewSubject")) {
                    trail = new ArrayList();
                    trail.add(new BreadcrumbBean(resworkflow.getString("submit_data"), "ListStudySubjectsSubmit", Status.AVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("add_new_study_event"), "CreateNewStudyEvent", Status.PENDING));
                    trail.add(new BreadcrumbBean(resworkflow.getString("study_event_overview"), "EnterDataForStudyEvent", Status.UNAVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("event_CRF_data_submission"), "TableOfContents", Status.UNAVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("data_entry"), "InitialDataEntry", Status.UNAVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("mark_event_CRF_complete"), "MarkEventCRFComplete", Status.UNAVAILABLE));
                } else {
                    trail = advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("add_new_study_event"), "CreateNewStudyEvent", Status.PENDING), 3);
                    closeRestOfTrail(3);
                }
            } else if (jspPage.equals(Page.ENTER_DATA_FOR_STUDY_EVENT)) {
                int ordinal;
                if (containsServlet("AddNewSubject")) {
                    ordinal = 4;
                    trail =
                        advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("study_event_overview"), "EnterDataForStudyEvent"
                            + generateURLString(request), Status.PENDING), ordinal);
                } else if (containsServlet("CreateNewStudyEvent")) {
                    ordinal = 2;
                    trail =
                        advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("study_event_overview"), "EnterDataForStudyEvent"
                            + generateURLString(request), Status.PENDING), ordinal);
                } else {
                    ordinal = 1;
                    trail = new ArrayList();
                    trail.add(new BreadcrumbBean(resworkflow.getString("submit_data"), "ListStudySubjectsSubmit", Status.AVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("study_event_overview"), "EnterDataForStudyEvent" + generateURLString(request),
                            Status.PENDING));
                    trail.add(new BreadcrumbBean(resworkflow.getString("event_CRF_data_submission"), "TableOfContents", Status.UNAVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("data_entry"), "InitialDataEntry", Status.UNAVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("mark_event_CRF_complete"), "MarkEventCRFComplete", Status.UNAVAILABLE));
                }
                closeRestOfTrail(ordinal);
            } else if (jspPage.equals(Page.TABLE_OF_CONTENTS)) {
                int ordinal;
                if (containsServlet("EnterDataForStudyEvent")) {
                    ordinal = trail.size() - 3;
                    trail =
                        advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("event_CRF_data_submission"), "TableOfContents"
                            + this.generateURLString(request), Status.PENDING), ordinal);
                    closeRestOfTrail(ordinal);
                } else {
                    ordinal = 1;
                    trail = new ArrayList();
                    trail.add(new BreadcrumbBean(resworkflow.getString("submit_data"), "ListStudySubjectsSubmit", Status.AVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("event_CRF_data_submission"), "TableOfContents" + generateURLString(request),
                            Status.PENDING));
                    trail.add(new BreadcrumbBean(resworkflow.getString("data_entry"), "InitialDataEntry", Status.UNAVAILABLE));
                    trail.add(new BreadcrumbBean(resworkflow.getString("mark_event_CRF_omplete"), "MarkEventCRFComplete", Status.UNAVAILABLE));
                }
                closeRestOfTrail(ordinal);
            } else if (jspPage.equals(Page.INITIAL_DATA_ENTRY)) {
                int ordinal = trail.size() - 2;
                trail =
                    advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("data_entry"), "InitialDataEntry" + this.generateURLString(request),
                            Status.PENDING), ordinal);
                closeRestOfTrail(ordinal);
            } else if (jspPage.equals(Page.DOUBLE_DATA_ENTRY)) {
                int ordinal = trail.size() - 2;
                trail =
                    advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("data_entry"), "DoubleDataEntry" + this.generateURLString(request),
                            Status.PENDING), ordinal);
                closeRestOfTrail(ordinal);
            } else if (jspPage.equals(Page.MARK_EVENT_CRF_COMPLETE)) {
                int ordinal = trail.size() - 1;
                trail =
                    advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("mark_event_CRF_complete"), "MarkEventCRFComplete"
                        + this.generateURLString(request), Status.PENDING), ordinal);
                closeRestOfTrail(ordinal);
            }

            else if (jspPage.equals(Page.CREATE_STUDY1)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("study_description"), "#", Status.PENDING));
                trail.add(new BreadcrumbBean(resworkflow.getString("study_status"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("study_design"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("conditions_and_eligibility"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("facility_information"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("related_information"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("confirm_and_submit"), "#", Status.UNAVAILABLE));
            }

            else if (jspPage.equals(Page.CREATE_STUDY2)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("study_status"), "#", Status.PENDING), 1);
                closeRestOfTrail(1);
            } else if (jspPage.equals(Page.CREATE_STUDY3) || jspPage.equals(Page.CREATE_STUDY4)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("study_design"), "#", Status.PENDING), 2);
                closeRestOfTrail(2);
                // closeBreadcrumb(1);
            } else if (jspPage.equals(Page.CREATE_STUDY5)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("conditions_and_eligibility"), "#", Status.PENDING), 3);
                closeRestOfTrail(3);
                // closeBreadcrumb(2);
            } else if (jspPage.equals(Page.CREATE_STUDY6)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("facility_information"), "#", Status.PENDING), 4);
                closeRestOfTrail(4);
                // closeBreadcrumb(3);

            } else if (jspPage.equals(Page.CREATE_STUDY7)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("related_information"), "#", Status.PENDING), 5);
                closeRestOfTrail(5);
                // closeBreadcrumb(4);
            } else if (jspPage.equals(Page.STUDY_CREATE_CONFIRM)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("confirm_and_submit"), "#", Status.PENDING), 6);
                closeRestOfTrail(6);
                // closeBreadcrumb(5);
            } else if (jspPage.equals(Page.UPDATE_STUDY1)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("study_description"), "#", Status.PENDING));
                trail.add(new BreadcrumbBean(resworkflow.getString("study_status"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("study_design"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("conditions_and_eligibility"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("facility_information"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("related_information"), "#", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("confirm_and_submit"), "#", Status.UNAVAILABLE));
            }

            else if (jspPage.equals(Page.UPDATE_STUDY2)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("study_status"), "#", Status.PENDING), 1);
                closeRestOfTrail(1);
            } else if (jspPage.equals(Page.UPDATE_STUDY3) || jspPage.equals(Page.CREATE_STUDY4)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("study_design"), "#", Status.PENDING), 2);
                closeRestOfTrail(2);
            } else if (jspPage.equals(Page.UPDATE_STUDY5)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("conditions_and_eligibility"), "#", Status.PENDING), 3);
                closeRestOfTrail(3);

            } else if (jspPage.equals(Page.UPDATE_STUDY6)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("facility_information"), "#", Status.PENDING), 4);
                closeRestOfTrail(4);

            } else if (jspPage.equals(Page.UPDATE_STUDY7)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("related_information"), "#", Status.PENDING), 5);
                closeRestOfTrail(5);
            } else if (jspPage.equals(Page.STUDY_UPDATE_CONFIRM)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("confirm_and_submit"), "#", Status.PENDING), 6);
                closeRestOfTrail(6);
            } else if (jspPage.equals(Page.ADMIN_SYSTEM) || jspPage.equals(Page.TECH_ADMIN_SYSTEM)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.ENTERPRISE)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("home"), "MainMenu", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("openclinica_enterprise"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.MANAGE_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.MANAGE_STUDY_BODY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.LIST_USER_IN_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_users"), "ListStudyUser", Status.PENDING));

            } else if (jspPage.equals(Page.LIST_STUDY_SUBJECT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_subjects"), "ListStudySubject", Status.PENDING));
            } else if (jspPage.equals(Page.SITE_LIST)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_sites"), "ListSite", Status.PENDING));
            } else if (jspPage.equals(Page.STUDY_EVENT_DEFINITION_LIST)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_event_definitions"), "ListEventDefinition", Status.PENDING));
            }

            else if (jspPage.equals(Page.SUBJECT_GROUP_CLASS_LIST)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_groups"), "ListSubjectGroupClass", Status.PENDING));
            } else if (jspPage.equals(Page.CREATE_SUBJECT_GROUP_CLASS)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_groups"), "ListSubjectGroupClass", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("create_subject_group_class"), "CreateSubjectGroupClass", Status.PENDING));
            } else if (jspPage.equals(Page.CRF_LIST)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.PENDING));
            } else if (jspPage.equals(Page.SUBJECT_LIST)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_subjects"), "ListSubject", Status.PENDING));
            } else if (jspPage.equals(Page.LIST_USER_ACCOUNTS)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_users"), "ListUserAccounts", Status.PENDING));
            } else if (jspPage.equals(Page.STUDY_LIST)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_studies"), "ListStudy", Status.PENDING));
            } else if (jspPage.equals(Page.CREATE_CRF)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("create_a_new_CRF"), "CreateCRF", Status.PENDING));
            }

            else if (jspPage.equals(Page.CREATE_CRF_VERSION)) {
                trail = new ArrayList();

                // trail.add(new
                // BreadcrumbBean(resworkflow.getString("enter_version_name"),
                // "#", Status.PENDING));
                // trail.add(new
                // BreadcrumbBean(resworkflow.getString("check_version"), "#",
                // Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("upload_spreadsheet"), "#", Status.PENDING));
                trail.add(new BreadcrumbBean(resworkflow.getString("validate_spreadsheet"), "#", Status.UNAVAILABLE));
                // trail.add(new
                // BreadcrumbBean(resworkflow.getString("review_new_items"),
                // "#", Status.UNAVAILABLE));
                // trail.add(new
                // BreadcrumbBean(resworkflow.getString("review_SQL_generated"),
                // "#", Status.UNAVAILABLE));

                /*
                 * MERGED from the following, may need to update i18n'ed text
                 * above:
                 *
                 * trail.add(new BreadcrumbBean("Upload Spreadsheet", "#",
                 * Status.PENDING)); // trail.add(new BreadcrumbBean("Check
                 * Version", "#", Status.UNAVAILABLE)); //trail.add(new
                 * BreadcrumbBean("Upload Spreadsheet", "#",
                 * Status.UNAVAILABLE)); trail.add(new BreadcrumbBean("Review
                 * Spreadsheet", "#", Status.UNAVAILABLE)); //trail.add(new
                 * BreadcrumbBean("Review New Items", "#", Status.UNAVAILABLE));
                 * //trail.add(new BreadcrumbBean("Review SQL Generated", "#",
                 * Status.UNAVAILABLE)); >>>>>>> .r9766
                 */

            } else if (jspPage.equals(Page.CREATE_CRF_VERSION_NODELETE) || jspPage.equals(Page.REMOVE_CRF_VERSION_DEF)
                || jspPage.equals(Page.REMOVE_CRF_VERSION_CONFIRM)) {

                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("upload_spreadsheet"), "#", Status.PENDING), 0);
                closeRestOfTrail(0);

            } else if (jspPage.equals(Page.UPLOAD_CRF_VERSION)) {

                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("upload_spreadsheet"), "#", Status.PENDING), 1);

                BreadcrumbBean b = (BreadcrumbBean) trail.get(0);
                b.setStatus(Status.AVAILABLE);
                closeRestOfTrail(2);
            } else if (jspPage.equals(Page.CREATE_CRF_VERSION_CONFIRM)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("validate_spreadsheet"), "#", Status.PENDING), 3);
                closeRestOfTrail(3);
            } else if (jspPage.equals(Page.CREATE_CRF_VERSION_CONFIRMSQL)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("review_new_items"), "#", Status.PENDING), 4);
                closeRestOfTrail(4);
            } else if (jspPage.equals(Page.CREATE_CRF_VERSION_DONE) || jspPage.equals(Page.CREATE_CRF_VERSION_ERROR)) {
                advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("review_SQL_generated"), "#", Status.PENDING), 5);
                closeRestOfTrail(5);
            } else if (jspPage.equals(Page.VIEW_CRF_VERSION)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_CRF_version"), "ViewCRFVersion", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_CRF)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_CRF_version"), "ViewCRF", Status.PENDING));
            } else if (jspPage.equals(Page.REMOVE_CRF)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("remove_CRF"), "RemoveCRF", Status.PENDING));
            } else if (jspPage.equals(Page.RESTORE_CRF)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("restore_CRF"), "RestoreCRF", Status.PENDING));
            } else if (jspPage.equals(Page.REMOVE_CRF_VERSION)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("remove_CRF"), "RemoveCRFVersion", Status.PENDING));
            } else if (jspPage.equals(Page.RESTORE_CRF_VERSION)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("restore_CRF_version"), "RestoreCRFVersion", Status.PENDING));
            } else if (jspPage.equals(Page.UPDATE_CRF)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("update_CRF"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_SUBJECT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_subjects"), "ListSubject", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_subject"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.UPDATE_SUBJECT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_subjects"), "ListSubject", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("update_subject"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.REMOVE_SUBJECT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_subjects"), "ListSubject", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("remove_subject"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.RESTORE_SUBJECT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_subjects"), "ListSubject", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("restore_subject"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_USER_ACCOUNT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_users"), "ListUserAccounts", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_user_account"), "ViewUserAccount" + generateURLString(request), Status.PENDING));
            }

            else if (jspPage.equals(Page.EDIT_ACCOUNT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_system"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_users"), "ListUserAccounts", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("edit_user_account"), "EditUserAccount" + generateURLString(request), Status.PENDING));
                trail.add(new BreadcrumbBean(resworkflow.getString("confirm_user_account_details"), "EditUserAccount", Status.UNAVAILABLE));
            }

            else if (jspPage.equals(Page.EDIT_ACCOUNT_CONFIRM)) {
                trail =
                    advanceTrail(trail, new BreadcrumbBean(resworkflow.getString("confirm_user_account_details"), "EditUserAccount"
                        + generateURLString(request), Status.PENDING), 3);
            }

            else if (jspPage.equals(Page.EDIT_STUDY_USER_ROLE)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_users"), "ListUserAccounts", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("edit_user_role"), "EditStudyUserRole", Status.PENDING));
            }

            else if (jspPage.equals(Page.CREATE_ACCOUNT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_users"), "ListUserAccounts", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("create_user_account"), "CreateUserAccount", Status.PENDING));
            } else if (jspPage.equals(Page.REASSIGN_STUDY_SUBJECT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_subjects"), "ListStudySubject", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("reassign_study_subject"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.DEFINE_STUDY_EVENT1)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_event_definitions"), "ListEventDefinition", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("create_study_event_definition"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.UPDATE_EVENT_DEFINITION1)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_event_definitions"), "ListEventDefinition", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("update_study_event_definition"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.VIEW_EVENT_DEFINITION)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_event_definitions"), "ListEventDefinition", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_event_definition"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.CHANGE_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("home"), "MainMenu", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("change_current_study"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.UPDATE_PROFILE)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("home"), "MainMenu", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("update_user_profile"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.CREATE_SUB_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_sites"), "ListSite", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("create_new_site"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.VIEW_SITE)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_sites"), "ListSite", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_site"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.SET_USER_ROLE_IN_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_users"), "ListStudyUser", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("set_user_role"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.STUDY_USER_LIST)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_users"), "ListStudyUser", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("assign_new_users_to_study"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.LOCK_DEFINITION)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_event_definitions"), "ListEventDefinition", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("lock_event_definition"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.UNLOCK_DEFINITION)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_event_definitions"), "ListEventDefinition", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("unlock_event_definition"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.VIEW_USER_IN_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_users"), "ListStudyUser", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_user_details"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.REMOVE_USER_ROLE_IN_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_users"), "ListStudyUser", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("remove_user_role"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.REMOVE_DEFINITION)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_event_definitions"), "ListEventDefinition", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("remove_event_definition"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.RESTORE_DEFINITION)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_event_definitions"), "ListEventDefinition", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("restore_event_definition"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_studies"), "ListStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_study_details"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.SET_USER_ROLE)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_users"), "ListUserAccounts", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("set_user_role"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.REMOVE_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_studies"), "ListStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("remove_a_study"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.RESTORE_STUDY)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("business_admin"), "AdminSystem", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("administer_studies"), "ListStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("restore_a_study"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.REMOVE_SITE)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_sites"), "ListSite", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("remove_a_site"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.RESTORE_SITE)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_sites"), "ListSite", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("restore_a_site"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.MENU)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("home"), "MainMenu", Status.PENDING));

            } else if (jspPage.equals(Page.VIEW_TABLE_OF_CONTENT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_CRF_version_data_entry"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_SECTION_DATA_ENTRY) || jspPage.equals(Page.VIEW_SECTION_DATA_ENTRY_SERVLET)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_CRF_version_section_data"), "#", Status.PENDING));

            } else if (jspPage.equals(Page.VIEW_EVENT_CRF_CONTENT)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_subjects"), "ListStudySubject", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_study_subject"), "ViewStudySubject" + this.generateURLString(request),
                        Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_event_CRF_data"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_STUDY_EVENTS)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_events"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.DELETE_CRF_VERSION)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_CRFs"), "ListCRF", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("delete_CRF_version"), "#", Status.PENDING));
            }

            // TODO fill in your page here if it does not require a
            // breadcrumb trail:

            else if (jspPage.equals(Page.MENU)) {
                trail = new ArrayList();
            }// below are new breadcrumbs added to provide links, tbh
            else if (jspPage.equals(Page.EDIT_DATASET)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("extract_datasets"), "ExtractDatasetsMain", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("edit_dataset_items_attributes"), "#", Status.PENDING));
                trail.add(new BreadcrumbBean(resworkflow.getString("define_temporal_scope"), "CreateDataset", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("specify_dataset_properties"), "CreateDataset", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("confirm_dataset_properties"), "CreateDataset", Status.UNAVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("generate_dataset"), "CreateDataset", Status.UNAVAILABLE));
            } else if (jspPage.equals(Page.EDIT_FILTER)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("extract_datasets"), "ExtractDatasetsMain", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("edit_filter"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_DATASET_DETAILS)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("extract_datasets"), "ExtractDatasetsMain", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_dataset_details"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.EXTRACT_DATASETS_MAIN)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("home"), "MainMenu", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("extract_datasets"), "ExtractDatasetsMain", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_DATASETS)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("extract_datasets"), "ExtractDatasetsMain", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_dataset"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.CREATE_FILTER_SCREEN_1)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("extract_datasets"), "ExtractDatasetsMain", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("view_filters"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_RULES)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("rule_manage_assignment"), "ViewRuleAssignment", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("rule_manage"), "#", Status.PENDING));
            } else if (jspPage.equals(Page.VIEW_RULE_SETS)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("manage_study"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("rule_manage_assignment"), "ViewRuleAssignment", Status.AVAILABLE));
            } else if (jspPage.equals(Page.IMPORT_RULES)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("import_rules_1"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("import_rules_2"), "ViewRuleAssignment", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("import_rules_3"), "#", Status.AVAILABLE));
            } else if (jspPage.equals(Page.TEST_RULES)) {
                trail = new ArrayList();
                trail.add(new BreadcrumbBean(resworkflow.getString("test_rules_validate"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("test_rules_test"), "ManageStudy", Status.AVAILABLE));
                trail.add(new BreadcrumbBean(resworkflow.getString("test_rules_get_results"), "TestRule", Status.AVAILABLE));
            }
            // else {
            // trail = new ArrayList();
            // }
        } catch (IndexOutOfBoundsException ioobe) {
            // TODO Auto-generated catch block, created to disallow errors
            ioobe.printStackTrace();

            trail = new ArrayList();
        }

        return trail;
    }

    public String generateURLString(HttpServletRequest request) {
        String newURL = "?";
        FormProcessor fp = new FormProcessor(request);
        Enumeration en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String title = (String) en.nextElement();
            String value = fp.getString(title);
            newURL += title + "=" + value + "&";
        }
        return newURL;
    }

    public ArrayList advanceTrail(ArrayList trail, BreadcrumbBean newBean, int ordinal) {

        int previous = ordinal - 1;

        BreadcrumbBean bcb;

        if (previous >= 0 && previous < trail.size()) {
            bcb = (BreadcrumbBean) trail.remove(previous);
            bcb.setStatus(Status.AVAILABLE);
            trail.add(previous, bcb);
        }

        if (ordinal >= 0 && ordinal < trail.size()) {
            bcb = (BreadcrumbBean) trail.remove(ordinal);
            trail.add(ordinal, newBean);
        }

        return trail;
    }

    /**
     * Determines if the trail contains a particular servlet.
     *
     * @param servlet
     *            The name of the servlet.
     * @return <code>true</code> if one of the elements refers to the
     *         specified servlet, <code>false</code> otherwise.
     */
    public boolean containsServlet(String servlet) {
        servlet = servlet.toLowerCase();
        for (int i = 0; i < trail.size(); i++) {
            BreadcrumbBean b = (BreadcrumbBean) trail.get(i);
            if (b.getUrl().toLowerCase().indexOf(servlet) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Make everything in the trail after the specified ordinal unavailable.
     *
     * It is recommended that this method be called after advanceTrail. Using
     * this method ensures that if the user got to the current page by "going
     * back" through the trail, all the "future" pages will be marked
     * unavailable.
     *
     * @param ordinal
     *            The index after which everything will be unavailable.
     */
    private void closeRestOfTrail(int ordinal) {
        if (ordinal < 0) {
            return;
        }

        for (int i = ordinal + 1; i < trail.size(); i++) {
            BreadcrumbBean b = (BreadcrumbBean) trail.get(i);
            b.setStatus(Status.UNAVAILABLE);
            trail.set(i, b);
        }

        return;
    }

    /**
     * Make the breadcrumb at position ordinal unavailable.
     *
     * @param ordinal
     *            The index of the breadcrumb.
     */
    private void closeBreadcrumb(int ordinal) {
        if (ordinal < 0 || ordinal >= trail.size()) {
            return;
        }

        BreadcrumbBean b = (BreadcrumbBean) trail.get(ordinal);
        b.setStatus(Status.UNAVAILABLE);
        trail.set(ordinal, b);

        return;
    }

    /**
     * Makes all breadcrumbs previous to this one open. Good for when you have
     * to skip a few steps ahead.
     *
     * @author thickerson
     * @param ordinal
     *            the index of the current breadcrumb.
     *
     */
    private void openBreadcrumbs(int ordinal) {
        if (ordinal < 0 || ordinal > trail.size()) {
            return;
        }

        for (int i = 0; i < ordinal; i++) {
            BreadcrumbBean b = (BreadcrumbBean) trail.get(i);
            b.setStatus(Status.AVAILABLE);
            trail.set(i, b);
        }
        return;
    }
}
