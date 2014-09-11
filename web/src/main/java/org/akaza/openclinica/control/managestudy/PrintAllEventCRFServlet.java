package org.akaza.openclinica.control.managestudy;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.PrintCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
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
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.i18n.core.LocaleResolver;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.view.display.DisplaySectionBeanHandler;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Shamim
  * Date: Dec 15, 2009
 */
public class PrintAllEventCRFServlet extends DataEntryServlet {
    Locale locale;

    /**
     * Checks whether the user has the correct privilege
     */
    @Override
    public void mayProceed(HttpServletRequest request, HttpServletResponse response) throws InsufficientPermissionException {
        locale = LocaleResolver.getLocale(request);
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
        SessionManager sm = (SessionManager)request.getSession().getAttribute("sm");
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);
        SectionBean sb = (SectionBean)request.getAttribute(SECTION_BEAN);
        ArrayList<SectionBean> allSectionBeans;
        // The PrintDataEntry servlet handles this parameter
        boolean isSubmitted = false;
        StudyEventDefinitionDAO sedao = new StudyEventDefinitionDAO(sm.getDataSource());
        EventDefinitionCRFDAO edao = new EventDefinitionCRFDAO(sm.getDataSource());
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());

        StudyDAO studyDao = new StudyDAO(sm.getDataSource());
        StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");

        ArrayList<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
        seds = sedao.findAllByStudy(currentStudy);

        //        ArrayList eventDefinitionCRFs = (ArrayList) edao.findAllByStudy(site);

        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        CRFDAO cdao = new CRFDAO(sm.getDataSource());

        ArrayList<EventDefinitionCRFBean> edcs = new ArrayList();
        for (StudyEventDefinitionBean sed : seds) {
            int defId = sed.getId();
            edcs.addAll(edcdao.findAllByDefinition(currentStudy, defId));
        }

        Map eventDefinitionDefaultVersions = new LinkedHashMap();
        for (int i = 0; i < edcs.size(); i++) {
            EventDefinitionCRFBean edc = edcs.get(i);
            ArrayList versions = (ArrayList) cvdao.findAllByCRF(edc.getCrfId());
            edc.setVersions(versions);
            CRFBean crf = (CRFBean) cdao.findByPK(edc.getCrfId());
            // edc.setCrfLabel(crf.getLabel());
            edc.setCrfName(crf.getName());
            // to show/hide edit action on jsp page
            if (crf.getStatus().equals(Status.AVAILABLE)) {
                edc.setOwner(crf.getOwner());
            }

            CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edc.getDefaultVersionId());
            StudyEventDefinitionBean studyEventDefinitionBean = (StudyEventDefinitionBean) sedao.findByPK(edc.getStudyEventDefinitionId());
            edc.setDefaultVersionName(defaultVersion.getName());
            if (defaultVersion.getStatus().isAvailable()) {
                List list = (ArrayList) eventDefinitionDefaultVersions.get(studyEventDefinitionBean);
                if (list == null)
                    list = new ArrayList();
                list.add(defaultVersion);
                eventDefinitionDefaultVersions.put(studyEventDefinitionBean, list);
            }
        }

        // Whether IE6 or IE7 is involved
        String isIE = fp.getString("ie");
        if ("y".equalsIgnoreCase(isIE)) {
            request.setAttribute("isInternetExplorer", "true");
        }

        SectionDAO sdao = new SectionDAO(sm.getDataSource());
        CRFVersionDAO crfVersionDAO = new CRFVersionDAO(sm.getDataSource());
        CRFDAO crfDao = new CRFDAO(sm.getDataSource());
        Map sedCrfBeans = null;

        for (Iterator it = eventDefinitionDefaultVersions.keySet().iterator(); it.hasNext();) {
            if (sedCrfBeans == null)
                sedCrfBeans = new LinkedHashMap();
            StudyEventDefinitionBean sedBean = (StudyEventDefinitionBean) it.next();
            List crfVersions = (ArrayList) eventDefinitionDefaultVersions.get(sedBean);
            for (Iterator crfIt = crfVersions.iterator(); crfIt.hasNext();) {
                CRFVersionBean crfVersionBean = (CRFVersionBean) crfIt.next();
                allSectionBeans = new ArrayList<SectionBean>();
                ArrayList sectionBeans = new ArrayList();

                ItemGroupDAO itemGroupDao = new ItemGroupDAO(sm.getDataSource());
                // Find truely grouped tables, not groups with a name of 'Ungrouped'
                List<ItemGroupBean> itemGroupBeans = itemGroupDao.findOnlyGroupsByCRFVersionID(crfVersionBean.getId());
                CRFBean crfBean = crfDao.findByVersionId(crfVersionBean.getId());

                if (itemGroupBeans.size() > 0) {
                    // get a DisplaySectionBean for each section of the CRF, sort
                    // them, then
                    // dispatch the request to a print JSP. The constructor for this
                    // handler takes
                    // a boolean value depending on whether data is involved or not
                    // ('false' in terms of this
                    // servlet; see PrintDataEntryServlet).

                    DisplaySectionBeanHandler handler = new DisplaySectionBeanHandler(false, getDataSource(), getServletContext());
                    handler.setCrfVersionId(crfVersionBean.getId());
                    //handler.setEventCRFId(eventCRFId);
                    List<DisplaySectionBean> displaySectionBeans = handler.getDisplaySectionBeans();

                    request.setAttribute("listOfDisplaySectionBeans", displaySectionBeans);
                    // Make available the CRF names and versions for
                    // the web page's header
                    CRFVersionBean crfverBean = (CRFVersionBean) crfVersionDAO.findByPK(crfVersionBean.getId());
                    request.setAttribute("crfVersionBean", crfverBean);
                    request.setAttribute("crfBean", crfBean);
                    // Set an attribute signaling that data is not involved
                    request.setAttribute("dataInvolved", "false");
                    PrintCRFBean printCrfBean = new PrintCRFBean();
                    printCrfBean.setDisplaySectionBeans(displaySectionBeans);
                    printCrfBean.setCrfVersionBean(crfVersionBean);
                    printCrfBean.setCrfBean(crfBean);
                    printCrfBean.setEventCrfBean(ecb);
                    printCrfBean.setGrouped(true);
                    List list = (ArrayList) sedCrfBeans.get(sedBean);
                    if (list == null)
                        list = new ArrayList();
                    list.add(printCrfBean);
                    sedCrfBeans.put(sedBean, list);

                    continue;
                }
                ecb = new EventCRFBean();
                ecb.setCRFVersionId(crfVersionBean.getId());
                CRFVersionBean version = (CRFVersionBean) crfVersionDAO.findByPK(crfVersionBean.getId());
                ArrayList sects = (ArrayList) sdao.findByVersionId(version.getId());
                for (int i = 0; i < sects.size(); i++) {
                     sb = (SectionBean) sects.get(i);
//                    super.sb = sb;
                    int sectId = sb.getId();
                    if (sectId > 0) {
                        allSectionBeans.add((SectionBean) sdao.findByPK(sectId));
                    }
                }
                request.setAttribute(INPUT_EVENT_CRF,ecb);
                request.setAttribute(SECTION_BEAN,sb);
                request.setAttribute(ALL_SECTION_BEANS, allSectionBeans);
                sectionBeans = super.getAllDisplayBeans(request);

                DisplaySectionBean dsb = super.getDisplayBean(false, false, request, isSubmitted);
                PrintCRFBean printCrfBean = new PrintCRFBean();
                printCrfBean.setAllSections(sectionBeans);
                printCrfBean.setDisplaySectionBean(dsb);
                printCrfBean.setEventCrfBean(ecb);
                printCrfBean.setCrfVersionBean(crfVersionBean);
                printCrfBean.setCrfBean(crfBean);
                printCrfBean.setGrouped(false);
                List list = (ArrayList) sedCrfBeans.get(sedBean);
                if (list == null)
                    list = new ArrayList();
                list.add(printCrfBean);
                sedCrfBeans.put(sedBean, list);
            }
        }
        request.setAttribute("sedCrfBeans", sedCrfBeans);
        request.setAttribute("studyName", currentStudy.getName());
        forwardPage(Page.VIEW_ALL_DEFAULT_CRF_VERSIONS_PRINT, request, response);
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
    protected Status getNonBlankItemStatus(HttpServletRequest request) {
        EventDefinitionCRFBean edcb = (EventDefinitionCRFBean)request.getAttribute(EVENT_DEF_CRF_BEAN);
        return edcb.isDoubleEntry() ? Status.PENDING : Status.UNAVAILABLE;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#getEventCRFAnnotations()
     */
    @Override
    protected String getEventCRFAnnotations(HttpServletRequest request) {
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);

        return ecb.getAnnotations();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.submit.DataEntryServlet#setEventCRFAnnotations(java.lang.String)
     */
    @Override
    protected void setEventCRFAnnotations(String annotations, HttpServletRequest request) {
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);

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
    protected String getServletPage(HttpServletRequest request) {
        return Page.VIEW_SECTION_DATA_ENTRY_SERVLET.getFileName();
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

    @Override
    protected boolean isAdministrativeEditing() {
        return false; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean isAdminForcedReasonForChange(HttpServletRequest request) {
        return false; //To change body of implemented methods use File | Settings | File Templates.
    }

}
