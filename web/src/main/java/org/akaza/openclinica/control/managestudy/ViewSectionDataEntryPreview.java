/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.managestudy;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.DisplayTableOfContentsBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.DataEntryServlet;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bruce W. Perry
 *
 * Preview a CRF version section data entry. This class is based almost entirely
 * on ViewSectionDataEntryServlet; Except that it's designed to provide a
 * preview of a crf before the crfversion is inserted into the database.
 */
public class ViewSectionDataEntryPreview extends DataEntryServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ViewSectionDataEntryPreview.class);

    public static String SECTION_TITLE = "section_title";
    public static String SECTION_LABEL = "section_label";
    public static String SECTION_SUBTITLE = "subtitle";
    public static String INSTRUCTIONS = "instructions";
    public static String BORDERS = "borders";

    /**
     * Checks whether the user has the correct privilege. This is from
     * ViewSectionDataEntryServlet.
     */
    @Override
    public void mayProceed(HttpServletRequest request, HttpServletResponse response) throws InsufficientPermissionException {
        UserAccountBean ub =(UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
        StudyUserRoleBean  currentRole = (StudyUserRoleBean) request.getSession().getAttribute("userRole");
        if (ub.isSysAdmin()) {
            return;
        }
        if (currentRole.getRole().equals(Role.STUDYDIRECTOR) || currentRole.getRole().equals(Role.COORDINATOR)
                || currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.RESEARCHASSISTANT) || currentRole.getRole().equals(Role.RESEARCHASSISTANT2)) {
            return;
        }

        addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " " + respage.getString("change_active_study_or_contact"), request);
        throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_director"), "1");

    }

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        FormProcessor fp = new FormProcessor(request);
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);
        SectionBean sb = (SectionBean)request.getAttribute(SECTION_BEAN);
        // These numbers will be zero if the
        // params are not present in the URL
        int crfid = fp.getInt("crfId");
        int tabNum = fp.getInt("tabId");
        HttpSession session = request.getSession();
        request.setAttribute("crfId", crfid);
        String crfName = "";
        String verNumber = "";
        // All the data on the uploaded Excel file
        // see org.akaza.openclinica.control.admin.SpreadsheetPreview
        // createCrfMetaObject() method
        Map<String, Map> crfMap = (Map) session.getAttribute("preview_crf");
        if (crfMap == null) {
            // addPageMessage
            String msg = respage.getString("preview_data_has_timed_out");
            this.addPageMessage(msg, request);
            LOGGER.debug("The session attribute \"preview_crf\" has expired or gone out of scope in: " + this.getClass().getName());
            this.forwardPage(Page.CRF_LIST_SERVLET, request, response);
        }

        Map<String, String> crfIdnameInfo = null;
        if (crfMap != null) {
            crfIdnameInfo = crfMap.get("crf_info");
        }
        // Get the CRF name and version String
        if (crfIdnameInfo != null) {
            Map.Entry mapEnt = null;
            for (Object element : crfIdnameInfo.entrySet()) {
                mapEnt = (Map.Entry) element;
                if (((String) mapEnt.getKey()).equalsIgnoreCase("crf_name")) {
                    crfName = (String) mapEnt.getValue();
                }
                if (((String) mapEnt.getKey()).equalsIgnoreCase("version")) {
                    verNumber = (String) mapEnt.getValue();
                }
            }
        }

        // Set up the beans that DisplaySectionBean and the preview
        // depend on
        EventCRFBean ebean = new EventCRFBean();
        CRFVersionBean crfverBean = new CRFVersionBean();
        crfverBean.setName(verNumber);
        CRFBean crfbean = new CRFBean();
        crfbean.setId(crfid);
        crfbean.setName(crfName);
        ebean.setCrf(crfbean);

        // This happens in ViewSectionDataEntry
        // It's an assumption that it has to happen here as well
        ecb = ebean;

        // All the groups data, if it's present in the CRF
        Map<Integer, Map<String, String>> groupsMap = null;
        if (crfMap != null)
            groupsMap = crfMap.get("groups");
        // Find out whether this CRF involves groups
        // At least one group is involved if the groups Map is not null or
        // empty, and the first group entry (there may be only one) has a
        // valid group label
        boolean hasGroups = false;
        /*
         * if(groupsMap != null && (! groupsMap.isEmpty()) &&
         * groupsMap.get(1).get("group_label").length() > 0) hasGroups = true;
         */

        // A SortedMap containing the row number as the key, and the
        // section headers/values (contained in a Map) as the value
        Map<Integer, Map<String, String>> sectionsMap = null;
        if (crfMap != null)
            sectionsMap = crfMap.get("sections");
        // The itemsMap contains the spreadsheet table items row number as a
        // key,
        // followed by a map of the column names/values; it contains values for
        // display
        // such as 'left item text'
        Map<Integer, Map<String, String>> itemsMap = null;
        if (crfMap != null)
            itemsMap = crfMap.get("items");

        // Create a list of FormGroupBeans from Maps of groups,
        // items, and sections
        BeanFactory beanFactory = new BeanFactory();
        // FormBeanUtil formUtil = new FormBeanUtil();

        // Set up sections for the preview
        Map.Entry me = null;
        SectionBean secbean = null;
        ArrayList<SectionBean> allSectionBeans = new ArrayList<SectionBean>();
        String name_str = "";
        String pageNum = "";
        Map secMap = null;
        // SpreadsheetPreviewNw returns doubles (via the
        // HSSFCell API, which parses Excel files)
        // as Strings (such as "1.0") for "1" in a spreadsheet cell,
        // so make sure only "1" is displayed using
        // this NumberFormat object
        NumberFormat numFormatter = NumberFormat.getInstance();
        numFormatter.setMaximumFractionDigits(0);
        if (sectionsMap != null) {
            for (Object element : sectionsMap.entrySet()) {
                secbean = new SectionBean();
                me = (Map.Entry) element;
                secMap = (Map) me.getValue();
                name_str = (String) secMap.get("section_label");
                secbean.setName(name_str);
                secbean.setTitle((String) secMap.get("section_title"));
                secbean.setInstructions((String) secMap.get("instructions"));
                secbean.setSubtitle((String) secMap.get("subtitle"));
                pageNum = (String) secMap.get("page_number");
                // ensure pageNum is an actual number; the user is not required
                // to
                // type a number in that Spreadsheet cell
                try {
                    pageNum = numFormatter.format(Double.parseDouble(pageNum));
                } catch (NumberFormatException nfe) {
                    pageNum = "";
                }

                secbean.setPageNumberLabel(pageNum);
                // Sift through the items to see if their section label matches
                // the section's section_label column
                secbean.setNumItems(this.getNumberOfItems(itemsMap, secbean.getName()));
                allSectionBeans.add(secbean);
            }
        }
        DisplayTableOfContentsBean dtocBean = new DisplayTableOfContentsBean();
        // Methods should just take Lists, the interface, not
        // ArrayList only!
        dtocBean.setSections(allSectionBeans);

        request.setAttribute("toc", dtocBean);
        request.setAttribute("sectionNum", allSectionBeans.size() + "");

        // Assuming that the super class' SectionBean sb variable must be
        // initialized,
        // since it happens in ViewSectionDataEntryServlet. TODO: verify this
        sb = allSectionBeans.get(0);
        // This is the StudySubjectBean
        // Not sure if this is needed for a Preview, but leaving
        // it in for safety/consisitency reasons
        request.setAttribute(INPUT_EVENT_CRF, ecb);
        request.setAttribute(SECTION_BEAN,sb);
        setupStudyBean(request);
        // Create a DisplaySectionBean for the SectionBean specified by the
        // tab number.
        tabNum = tabNum == 0 ? 1 : tabNum;
        String sectionTitle = getSectionColumnBySecNum(sectionsMap, tabNum, SECTION_TITLE);
        String secLabel = getSectionColumnBySecNum(sectionsMap, tabNum, SECTION_LABEL);
        String secSubtitle = getSectionColumnBySecNum(sectionsMap, tabNum, SECTION_SUBTITLE);
        String instructions = getSectionColumnBySecNum(sectionsMap, tabNum, INSTRUCTIONS);
        int secBorders = getSectionBordersBySecNum(sectionsMap, tabNum, BORDERS);

        DisplaySectionBean displaySection =
                beanFactory.createDisplaySectionBean(itemsMap, sectionTitle, secLabel, secSubtitle, instructions, crfName, secBorders);

        //
        // the variable hasGroups should only be true if the group appears in
        // this section
        List<DisplayItemBean> disBeans = displaySection.getItems();
        ItemFormMetadataBean metaBean;
        String groupLabel;
        hasGroups = false;
        for (DisplayItemBean diBean : disBeans) {
            metaBean = diBean.getMetadata();
            groupLabel = metaBean.getGroupLabel();
            if (groupLabel != null && groupLabel.length() > 0) {
                hasGroups = true;
                break;
            }

        }
        // Create groups associated with this section
        List<DisplayItemGroupBean> disFormGroupBeans = null;

        if (hasGroups) {
            disFormGroupBeans = beanFactory.createGroupBeans(itemsMap, groupsMap, secLabel, crfName);
            displaySection.setDisplayFormGroups(disFormGroupBeans);
        }

        /*
         * DisplaySectionBean displaySection =
         * beanFactory.createDisplaySectionBean (itemsMap, sectionTitle,
         * secLabel, secSubtitle, instructions, crfName);
         */
        displaySection.setCrfVersion(crfverBean);
        displaySection.setCrf(crfbean);
        displaySection.setEventCRF(ebean);
        // Not sure if this is needed? The JSPs pull it out
        // as a request attribute
        SectionBean aSecBean = new SectionBean();

        request.setAttribute(BEAN_DISPLAY, displaySection);
        // TODO: verify these attributes, from the original servlet, are
        // necessary
        request.setAttribute("sec", aSecBean);
        request.setAttribute("EventCRFBean", ebean);
        try {
            request.setAttribute("tabId", Integer.toString(tabNum));
        } catch (NumberFormatException nfe) {
            request.setAttribute("tabId", new Integer("1"));
        }
        if (hasGroups) {
            LOGGER.debug("has group, new_table is true");
            request.setAttribute("new_table", true);
        }
        // YW 07-23-2007 << for issue 0000937
        forwardPage(Page.CREATE_CRF_VERSION_CONFIRM, request, response);
        // YW >>

    }

    // Get a Section's title by its key number in the sectionsMap; i.e., what is
    // the title
    // of the first section in the CRF?
    private String getSectionColumnBySecNum(Map sectionsMap, int sectionNum, String sectionColumn) {
        if (sectionsMap == null || sectionColumn == null || sectionColumn.length() < 1) {
            return "";
        }
        Map innerMap = (Map) sectionsMap.get(sectionNum);
        return (String) innerMap.get(sectionColumn);
    }

    private int getSectionBordersBySecNum(Map sectionsMap, int sectionNum, String sectionColumn) {
        if (sectionsMap == null || sectionColumn == null || sectionColumn.length() < 1) {
            return 0;
        }
        Map innerMap = (Map) sectionsMap.get(sectionNum);
        String tempBorder = (String) innerMap.get(sectionColumn);
        // if the section borders property in the CRF template
        // is blank, return 0
        if (tempBorder != null && tempBorder.length() < 1) {
            return 0;
        }
        // if the borders property is null, return 0; otherwise return the value stored
        // in the HashMap
        if (tempBorder != null) {
            return new Integer(tempBorder);

        } else {
            return 0;
        }
    }

    /*
     * private String getSectionTitleBySecNum(Map sectionsMap, int sectionNum){
     * if(sectionsMap==null) return ""; Map innerMap = (Map)sectionsMap.get(new
     * Integer(sectionNum)); return (String)innerMap.get("section_title"); }
     * private String getSectionLabelBySecNum(Map sectionsMap, int sectionNum){
     * if(sectionsMap==null) return ""; Map innerMap = (Map)sectionsMap.get(new
     * Integer(sectionNum)); return (String)innerMap.get("section_label"); }
     * private String getSectionSubtitleBySecNum(Map sectionsMap, int
     * sectionNum){ if(sectionsMap==null) return ""; Map innerMap =
     * (Map)sectionsMap.get(new Integer(sectionNum)); return
     * (String)innerMap.get("subtitle"); } private String
     * getInstructionsBySecNum(Map sectionsMap, int sectionNum){
     * if(sectionsMap==null) return ""; Map innerMap = (Map)sectionsMap.get(new
     * Integer(sectionNum)); return (String)innerMap.get("instructions"); }
     */
    // Determine the number of items associated with this section
    // by checking the page number value of each item, and comparing it
    // with the page number of the section
    private int getNumberOfItems(Map itemsMap, String sectionLabel) {
        if (itemsMap == null)
            return 0;
        int itemCount = 0;
        Map itemVals = null;
        Map.Entry me = null;
        Map.Entry me2 = null;
        String columnName = "";
        String val = "";
        for (Iterator iter = itemsMap.entrySet().iterator(); iter.hasNext();) {
            me = (Map.Entry) iter.next();
            itemVals = (Map) me.getValue();
            // each Map member is a key/value pair representing an
            // item column/value
            for (Iterator iter2 = itemVals.entrySet().iterator(); iter2.hasNext();) {
                me2 = (Map.Entry) iter2.next();
                columnName = (String) me2.getKey();
                val = (String) me2.getValue();
                if (columnName.equalsIgnoreCase("section_label")) {
                    if (val.equalsIgnoreCase(sectionLabel))
                        itemCount++;
                }
            }
        }
        return itemCount;
    }

    private void setupStudyBean(HttpServletRequest request) {
        String age = "";
        EventCRFBean ecb = (EventCRFBean)request.getAttribute(INPUT_EVENT_CRF);

        StudySubjectDAO ssdao = new StudySubjectDAO(getDataSource());
        StudySubjectBean sub = (StudySubjectBean) ssdao.findByPK(ecb.getStudySubjectId());
        // This is the SubjectBean
        SubjectDAO subjectDao = new SubjectDAO(getDataSource());
        int subjectId = sub.getSubjectId();
        int studyId = sub.getStudyId();
        SubjectBean subject = (SubjectBean) subjectDao.findByPK(subjectId);
        StudyBean currentStudy =    (StudyBean)  request.getSession().getAttribute("study");
        // Let us process the age
        if (currentStudy.getStudyParameterConfig().getCollectDob().equals("1")) {
            StudyEventDAO sedao = new StudyEventDAO(getDataSource());
            StudyEventBean se = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
            StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(getDataSource());
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(se.getStudyEventDefinitionId());
            se.setStudyEventDefinition(sed);
            request.setAttribute("studyEvent", se);
            // YW 11-16-2007 enrollment-date is used for calculating age
            age = Utils.getInstacne().processAge(sub.getEnrollmentDate(), subject.getDateOfBirth());
        }
        // Get the study then the parent study
        StudyDAO studydao = new StudyDAO(getDataSource());
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
        request.setAttribute("age", age);
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
        if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)
                || rt.equals(org.akaza.openclinica.bean.core.ResponseType.CALCULATION) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.GROUP_CALCULATION)) {
            dib = validateDisplayItemBeanText(v, dib, inputName, request);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
            dib = validateDisplayItemBeanSingleCV(v, dib, inputName);
        } else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX) || rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
            dib = validateDisplayItemBeanMultipleCV(v, dib, inputName);
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
        return false;
    }

    @Override
    protected boolean isAdminForcedReasonForChange(HttpServletRequest request) {
        return false;
    }
}