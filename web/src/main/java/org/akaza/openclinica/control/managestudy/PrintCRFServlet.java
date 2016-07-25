/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2006 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.DataEntryServlet;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.view.display.DisplaySectionBeanHandler;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Krikor Krumlian 10/26/2006
 *
 *
 *         View a CRF version section data entry
 */
public class PrintCRFServlet extends DataEntryServlet {

    Locale locale;

    // < ResourceBundlerespage,resexception;

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed(HttpServletRequest request, HttpServletResponse response) throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // <
        // resexception=ResourceBundle.getBundle("org.akaza.openclinica.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("org.akaza.openclinica.i18n.page_messages",locale);
        UserAccountBean ub =(UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
        StudyUserRoleBean  currentRole = (StudyUserRoleBean) request.getSession().getAttribute("userRole");
        if (ub.isSysAdmin()) {
            return;
        }
        if (SubmitDataServlet.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + respage.getString("change_study_contact_sysadmin"), request);
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        FormProcessor fp = new FormProcessor(request);
        // The PrintDataEntry servlet handles this parameter
        int eventCRFId = fp.getInt("ecId");
        //JN:The following were the the global variables, moved as local.
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);
        SectionBean sb = (SectionBean)request.getAttribute(SECTION_BEAN);
        // Whether IE6 or IE7 is involved
        String isIE = fp.getString("ie");
        if ("y".equalsIgnoreCase(isIE)) {
            request.setAttribute("isInternetExplorer", "true");
        }

        int eventDefinitionCRFId = fp.getInt("eventDefinitionCRFId");
        // EventDefinitionCRFDao findByStudyEventIdAndCRFVersionId(int
        // studyEventId, int crfVersionId)
        SectionDAO sdao = new SectionDAO(getDataSource());
        CRFVersionDAO crfVersionDAO = new CRFVersionDAO(getDataSource());
        CRFDAO crfDao = new CRFDAO(getDataSource());

        ArrayList <SectionBean> allSectionBeans = new ArrayList<SectionBean>();
        ArrayList sectionBeans = new ArrayList();
        // The existing application doesn't print null values, even if they are
        // defined in the event definition
        int crfVersionId = fp.getInt("id", true);
        boolean isSubmitted = false;
        if (crfVersionId == 0) {
            addPageMessage(respage.getString("please_choose_a_crf_to_view_details"), request);
            forwardPage(Page.CRF_LIST_SERVLET, request, response);
        } else {
            // BWP 2/7/2008>> Find out if the CRF has grouped tables, and if so,
            // use
            // that dedicated JSP
            ItemGroupDAO itemGroupDao = new ItemGroupDAO(getDataSource());
            // Find truely grouped tables, not groups with a name of 'Ungrouped'
            List<ItemGroupBean> itemGroupBeans = itemGroupDao.findOnlyGroupsByCRFVersionID(crfVersionId);

            if (itemGroupBeans.size() > 0) {
                // get a DisplaySectionBean for each section of the CRF, sort
                // them, then
                // dispatch the request to a print JSP. The constructor for this
                // handler takes
                // a boolean value depending on whether data is involved or not
                // ('false' in terms of this
                // servlet; see PrintDataEntryServlet).
                DisplaySectionBeanHandler handler = new DisplaySectionBeanHandler(false, getDataSource(), getServletContext());
                handler.setCrfVersionId(crfVersionId);
                handler.setEventCRFId(eventCRFId);
                List<DisplaySectionBean> displaySectionBeans = handler.getDisplaySectionBeans();

                request.setAttribute("listOfDisplaySectionBeans", displaySectionBeans);
                // Make available the CRF names and versions for
                // the web page's header
                CRFVersionBean crfverBean = (CRFVersionBean) crfVersionDAO.findByPK(crfVersionId);
                request.setAttribute("crfVersionBean", crfverBean);
                CRFBean crfBean = crfDao.findByVersionId(crfVersionId);
                request.setAttribute("crfBean", crfBean);
                // Set an attribute signaling that data is not involved
                request.setAttribute("dataInvolved", "false");

                // request.setAttribute("displaySection",displaySection);
                forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PRINT_GROUPS, request, response);
                // make sure the rest of the code does not execute and throw an
                // IllegalStateException
                return;
            }
            ecb = new EventCRFBean();//JN:Revisit ME
            ecb.setCRFVersionId(crfVersionId);
            CRFVersionBean version = (CRFVersionBean) crfVersionDAO.findByPK(crfVersionId);
            ArrayList sects = (ArrayList) sdao.findByVersionId(version.getId());
            for (int i = 0; i < sects.size(); i++) {
                 sb = (SectionBean) sects.get(i);
//                super.sb = sb;
                int sectId = sb.getId();
                if (sectId > 0) {
                    allSectionBeans.add((SectionBean) sdao.findByPK(sectId));
                }
            }
            request.setAttribute(ALL_SECTION_BEANS, allSectionBeans);
            request.setAttribute(INPUT_EVENT_CRF,ecb);

            sectionBeans = super.getAllDisplayBeans(request);
        }
        request.setAttribute(INPUT_EVENT_CRF,ecb);
        request.setAttribute(SECTION_BEAN,sb);

        DisplaySectionBean dsb = super.getDisplayBean(false, false, request, isSubmitted);
        request.setAttribute("allSections", sectionBeans);
        request.setAttribute("displayAllCRF", "1");
        request.setAttribute(BEAN_DISPLAY, dsb);
        request.setAttribute(BEAN_ANNOTATIONS, ecb.getAnnotations());
        request.setAttribute("sec", sb);
        request.setAttribute("EventCRFBean", ecb);
        forwardPage(Page.VIEW_SECTION_DATA_ENTRY_PRINT, request, response);
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getBlankItemStatus()
     */
    @Override
    protected Status getBlankItemStatus() {
        return Status.AVAILABLE;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getNonBlankItemStatus()
     */
    @Override
    protected Status getNonBlankItemStatus(HttpServletRequest request) {
        EventDefinitionCRFBean edcb = (EventDefinitionCRFBean)request.getAttribute(EVENT_DEF_CRF_BEAN);
        return edcb.isDoubleEntry() ? Status.PENDING : Status.UNAVAILABLE;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getEventCRFAnnotations()
     */
    @Override
    protected String getEventCRFAnnotations(HttpServletRequest request) {
        //JN:The following were the the global variables, moved as local.
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);
        return ecb.getAnnotations();
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#setEventCRFAnnotations(java.lang.String)
     */
    @Override
    protected void setEventCRFAnnotations(String annotations, HttpServletRequest request) {
        //JN:The following were the the global variables, moved as local.
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);
        ecb.setAnnotations(annotations);
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getJSPPage()
     */
    @Override
    protected Page getJSPPage() {
        return Page.VIEW_SECTION_DATA_ENTRY;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getServletPage()
     */
    @Override
    protected String getServletPage(HttpServletRequest request) {
        return Page.VIEW_SECTION_DATA_ENTRY_SERVLET.getFileName();
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#validateInputOnFirstRound()
     */
    @Override
    protected boolean validateInputOnFirstRound() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#validateDisplayItemBean(org.akaza.openclinica.core.form.Validator,
     * org.akaza.openclinica.bean.submit.DisplayItemBean)
     */
    @Override
    protected DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib, String inputName, HttpServletRequest request) {
        ItemBean ib = dib.getItem();
        org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();

        // note that this step sets us up both for
        // displaying the data on the form again, in the event of an error
        // and sending the data to the database, in the event of no error
        dib = loadFormValue(dib, request);

        // types TEL and ED are not supported yet
        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)) {
            dib = validateDisplayItemBeanText(v, dib, inputName, request);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
            dib = validateDisplayItemBeanSingleCV(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
            dib = validateDisplayItemBeanMultipleCV(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CALCULATION)
            || rt.equals(org.akaza.openclinica.bean.core.ResponseType.GROUP_CALCULATION)) {
            // for now, treat calculation like any other text input --
            // eventually this might need to be customized
            dib = validateDisplayItemBeanText(v, dib, inputName, request);
        }

        return dib;
    }

    @Override
    protected List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v, DisplayItemGroupBean digb, List<DisplayItemGroupBean> digbs,
            List<DisplayItemGroupBean> formGroups, HttpServletRequest request, HttpServletResponse response) {

        return formGroups;

    }

    /*
     * (non-Javadoc)
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
    @Override
    protected boolean isAdministrativeEditing() {
    	return false;
    }

    @Override
    protected boolean isAdminForcedReasonForChange(HttpServletRequest request) {
    	return false;
    }
}
