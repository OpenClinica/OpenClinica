/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2006 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.DataEntryServlet;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.view.display.DisplaySectionBeanHandler;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Krikor Krumlian 10/26/2006
 *
 *
 * View a CRF version section data entry
 */
public class PrintDataEntryServlet extends DataEntryServlet {

    Locale locale;

    // < ResourceBundleresword, resworkflow, respage,resexception;

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed() throws InsufficientPermissionException {

        locale = request.getLocale();
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);

        if (ub.isSysAdmin()) {
            return;
        }
        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"));
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest() throws Exception {
        FormProcessor fp = new FormProcessor(request);
        int eventCRFId = fp.getInt("ecId");
        SectionDAO sdao = new SectionDAO(sm.getDataSource());
        allSectionBeans = new ArrayList();
        ArrayList sectionBeans = new ArrayList();
        String age = "";

        // Whether IE6 or IE7 is involved
        String isIE = fp.getString("ie");
        if ("y".equalsIgnoreCase(isIE)) {
            request.setAttribute("isInternetExplorer", "true");
        }

        if (eventCRFId == 0) {
            super.ecb = new EventCRFBean();
            // super.ecb.setCRFVersionId(sb.getCRFVersionId());
        } else {
            EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
            super.ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);

            // Get all the SectionBeans attached to this ECB
            ArrayList sects = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());
            for (int i = 0; i < sects.size(); i++) {
                SectionBean sb = (SectionBean) sects.get(i);
                super.sb = sb;
                int sectId = sb.getId();
                if (sectId > 0) {
                    allSectionBeans.add((SectionBean) sdao.findByPK(sectId));
                }
            }
            // This is the StudySubjectBean
            StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
            StudySubjectBean sub = (StudySubjectBean) ssdao.findByPK(super.ecb.getStudySubjectId());
            // This is the SubjectBean
            SubjectDAO subjectDao = new SubjectDAO(sm.getDataSource());
            int subjectId = sub.getSubjectId();
            int studyId = sub.getStudyId();
            SubjectBean subject = (SubjectBean) subjectDao.findByPK(subjectId);
            StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
            StudyEventBean se = (StudyEventBean) sedao.findByPK(super.ecb.getStudyEventId());
            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
            se.setStudyEventDefinition(sed);
            // Let us process the age
            if (currentStudy.getStudyParameterConfig().getCollectDob().equals("1")) {
                // YW 11-16-2007 enrollment-date is used for computing age
                age = Utils.getInstacne().processAge(sub.getEnrollmentDate(), subject.getDateOfBirth());
            }
            // Get the study then the parent study
            StudyDAO studydao = new StudyDAO(sm.getDataSource());
            StudyBean study = (StudyBean) studydao.findByPK(studyId);

            if (study.getParentStudyId() > 0) {
                // this is a site,find parent
                StudyBean parentStudy = (StudyBean) studydao.findByPK(study.getParentStudyId());
                request.setAttribute("studyTitle", parentStudy.getName() + " - " + study.getName());
            } else {
                request.setAttribute("studyTitle", study.getName());
            }

            request.setAttribute("studySubject", sub);
            request.setAttribute("subject", subject);
            request.setAttribute("studyEvent", se);
            request.setAttribute("age", age);
            // Get the section beans from super
            sectionBeans = super.getAllDisplayBeans();

        }

        // Find out whether the sections involve groups
        ItemGroupDAO itemGroupDao = new ItemGroupDAO(sm.getDataSource());
        // Find truely grouped tables, not groups with a name of 'Ungrouped'
        // CRF VERSION ID WILL BE 0 IF "ecId" IS NOT IN THE QUERYSTRING
        int crfVersionId = super.ecb.getCRFVersionId();
        List<ItemGroupBean> itemGroupBeans = itemGroupDao.findOnlyGroupsByCRFVersionID(crfVersionId);
        boolean sectionsHaveGroups = false;

        if (itemGroupBeans.size() > 0) {
            sectionsHaveGroups = true;
            // get a DisplaySectionBean for each section of the CRF, sort them,
            // then
            // dispatch the request to a print JSP. the constructor for this
            // handler takes
            // a boolean value depending on whether an event or data is involved
            // or not
            DisplaySectionBeanHandler handler = new DisplaySectionBeanHandler(true, sm.getDataSource());

            handler.setCrfVersionId(crfVersionId);
            handler.setEventCRFId(eventCRFId);
            List<DisplaySectionBean> displaySectionBeans = handler.getDisplaySectionBeans();

            CRFVersionDAO crfVersionDAO = new CRFVersionDAO(sm.getDataSource());
            CRFDAO crfDao = new CRFDAO(sm.getDataSource());

            request.setAttribute("listOfDisplaySectionBeans", displaySectionBeans);
            // Make available the CRF names and versions for
            // the web page's header
            CRFVersionBean crfverBean = (CRFVersionBean) crfVersionDAO.findByPK(crfVersionId);
            request.setAttribute("crfVersionBean", crfverBean);
            CRFBean crfBean = crfDao.findByVersionId(crfVersionId);
            request.setAttribute("crfBean", crfBean);
            // Set an attribute signaling that an event and/or data is involved
            request.setAttribute("dataInvolved", "true");
        }
        request.setAttribute(BEAN_ANNOTATIONS, ecb.getAnnotations());
        request.setAttribute("EventCRFBean", super.ecb);
        // We do not need most of these attributes if groups are involved
        if (!sectionsHaveGroups) {
            DisplaySectionBean dsb = super.getDisplayBean(false, false);
            request.setAttribute("allSections", sectionBeans);
            request.setAttribute("displayAll", "1");
            request.setAttribute(BEAN_DISPLAY, dsb);

            request.setAttribute("sec", sb);

            forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PRINT);
        } else { // end if(! sectionsHaveGroups)
            forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PRINT_GROUPS);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getBlankItemStatus()
     */
    @Override
    protected Status getBlankItemStatus() {
        return Status.AVAILABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getNonBlankItemStatus()
     */
    @Override
    protected Status getNonBlankItemStatus() {
        return edcb.isDoubleEntry() ? Status.PENDING : Status.UNAVAILABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getEventCRFAnnotations()
     */
    @Override
    protected String getEventCRFAnnotations() {
        return ecb.getAnnotations();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#setEventCRFAnnotations(java.lang.String)
     */
    @Override
    protected void setEventCRFAnnotations(String annotations) {
        ecb.setAnnotations(annotations);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getJSPPage()
     */
    @Override
    protected Page getJSPPage() {
        return Page.VIEW_SECTION_DATA_ENTRY;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getServletPage()
     */
    @Override
    protected Page getServletPage() {
        return Page.VIEW_SECTION_DATA_ENTRY_SERVLET;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#validateInputOnFirstRound()
     */
    @Override
    protected boolean validateInputOnFirstRound() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#validateDisplayItemBean(org.akaza.openclinica.core.form.Validator,
     *      org.akaza.openclinica.bean.submit.DisplayItemBean)
     */
    @Override
    protected DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib, String inputName) {
        ItemBean ib = dib.getItem();
        org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();

        // note that this step sets us up both for
        // displaying the data on the form again, in the event of an error
        // and sending the data to the database, in the event of no error
        dib = loadFormValue(dib);

        // types TEL and ED are not supported yet
        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)) {
            dib = validateDisplayItemBeanText(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
            dib = validateDisplayItemBeanSingleCV(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
            dib = validateDisplayItemBeanMultipleCV(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CALCULATION)
            || rt.equals(org.akaza.openclinica.bean.core.ResponseType.GROUP_CALCULATION)) {
            // for now, treat calculation like any other text input --
            // eventually this might need to be customized
            dib = validateDisplayItemBeanText(v, dib, inputName);
        }

        return dib;
    }

    @Override
    protected List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v, DisplayItemGroupBean digb, List<DisplayItemGroupBean> digbs,
            List<DisplayItemGroupBean> formGroups) {

        return formGroups;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#loadDBValues()
     */
    @Override
    protected boolean shouldLoadDBValues(DisplayItemBean dib) {
        return true;
    }

    @Override
    protected boolean shouldRunRules() {
        return false;
    }
    
    protected boolean isAdministrativeEditing() {
    	return false;
    }
    
    protected boolean isAdminForcedReasonForChange() {
    	return false;
    }
}
