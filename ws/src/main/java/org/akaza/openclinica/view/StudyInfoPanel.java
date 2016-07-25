/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.view;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.extract.FilterBean;
import org.akaza.openclinica.bean.managestudy.*;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * To create a flexible panel of information that will change while the user
 * manages his or her session.
 *
 * @author thickerson
 *
 */
public class StudyInfoPanel {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    ResourceBundle resword;

    private TreeMap data = new TreeMap();

    /**
     * An array of StudyInfoPanelLine objects. This is only used if orderedData
     * is flipped on.
     */
    private ArrayList userOrderedData = new ArrayList();

    String datePattern = "MM/dd/yyyy";

    SimpleDateFormat english_sdf = new SimpleDateFormat(datePattern);
    SimpleDateFormat local_sdf;
    private boolean studyInfoShown = true;

    private boolean orderedData = false;

    private boolean submitDataModule = false; // if it is submit data module,
    // need
    // to show tree panel

    private boolean extractData = false;

    private boolean createDataset = false;

    private boolean iconInfoShown = true;// added for the side icons

    private boolean manageSubject = false;// added to control the group of

    // side icons

    /**
     * @return Returns the manageSubject.
     */
    public boolean isManageSubject() {
        return manageSubject;
    }

    /**
     * @param manageSubject
     *            The manageSubject to set.
     */
    public void setManageSubject(boolean manageSubject) {
        this.manageSubject = manageSubject;
    }

    /**
     * @return Returns the iconInfoShown.
     */
    public boolean isIconInfoShown() {
        return iconInfoShown;
    }

    /**
     * @param iconInfoShown
     *            The iconInfoShown to set.
     */
    public void setIconInfoShown(boolean iconInfoShown) {
        this.iconInfoShown = iconInfoShown;
    }

    /**
     * @return Returns the extractData.
     */
    public boolean isExtractData() {
        return extractData;
    }

    /**
     * @param extractData
     *            The extractData to set.
     */
    public void setExtractData(boolean extractData) {
        this.extractData = extractData;
    }

    /**
     * @return Returns the submitDataModule.
     */
    public boolean isSubmitDataModule() {
        return submitDataModule;
    }

    /**
     * @param submitDataModule
     *            The submitDataModule to set.
     */
    public void setSubmitDataModule(boolean submitDataModule) {
        this.submitDataModule = submitDataModule;
    }

    public StudyInfoPanel() {
        // blank generator
    }

    public void setData(String key, String value) {
        data.put(key, value);
    }

    public void removeData(String key) {
        data.remove(key);
    }

    public void reset() {
        data = new TreeMap();
        userOrderedData = new ArrayList();
    }

    /**
     * @return Returns the data.
     */
    public TreeMap getData() {
        return data;
    }

    /**
     * @param data
     *            The data to set.
     */
    public void setData(TreeMap data) {
        this.data = data;
    }

    /**
     * setData, the external function which creates data for the panel to
     * reflect.
     *
     * @param page
     * @param session
     * @param request
     */
    public void setData(Page page, HttpSession session, HttpServletRequest request) {

        Locale locale = request.getLocale();
        resword = ResourceBundleProvider.getWordsBundle();
        local_sdf = new SimpleDateFormat(ResourceBundleProvider.getFormatBundle(locale).getString("date_format_string"));
        // logger.info("found date format string: " +
        // ResourceBundleProvider.getFormatBundle
        // ().getString("date_format_string"));
        // logger.info("found date format string with locale: " +
        // ResourceBundleProvider
        // .getFormatBundle(locale).getString("date_format_string"));

        try {
            // defaults, can be reset by mistake by running through one page,
            // tbh
            this.setStudyInfoShown(true);
            this.setOrderedData(false);
            // try to avoid errors, tbh
            if (page.equals(Page.CREATE_DATASET_1)) {
                this.reset();
                // this.setData("Number of Steps", "5");
            } else if (page.equals(Page.CREATE_DATASET_2) || page.equals(Page.CREATE_DATASET_EVENT_ATTR) || page.equals(Page.CREATE_DATASET_SUB_ATTR)
                || page.equals(Page.CREATE_DATASET_CRF_ATTR) || page.equals(Page.CREATE_DATASET_GROUP_ATTR) || page.equals(Page.CREATE_DATASET_VIEW_SELECTED)) {
                HashMap eventlist = (HashMap) request.getAttribute("eventlist");
                ArrayList displayData = generateEventTree(eventlist);

                this.reset();
                this.setUserOrderedData(displayData);
                this.setStudyInfoShown(false);
                this.setOrderedData(true);
                this.setCreateDataset(true);
                this.setSubmitDataModule(false);
                this.setExtractData(false);

            } else if (page.equals(Page.CREATE_DATASET_3)) {
                this.reset();
                this.setStudyInfoShown(false);
                this.setOrderedData(false);
                this.setCreateDataset(true);
                this.setSubmitDataModule(false);
                this.setExtractData(false);

                DatasetBean dsb = (DatasetBean) session.getAttribute("newDataset");
                int ev_count = dsb.getItemIds().size();

                this.setData(resword.getString("items_selected"), new Integer(ev_count).toString());

            } else if (page.equals(Page.CREATE_DATASET_4)) {
                this.reset();
                this.setStudyInfoShown(false);
                this.setOrderedData(false);
                this.setCreateDataset(true);
                this.setSubmitDataModule(false);
                this.setExtractData(false);
                this.removeData(resword.getString("beginning_date"));
                this.removeData(resword.getString("ending_date"));
                DatasetBean dsb = (DatasetBean) session.getAttribute("newDataset");
                int ev_count = dsb.getItemIds().size();
                this.setData(resword.getString("items_selected"), new Integer(ev_count).toString());

                if ("01/01/1900".equals(english_sdf.format(dsb.getDateStart()))) {
                    this.setData(resword.getString("beginning_date"), resword.getString("not_specified"));
                } else {
                    this.setData(resword.getString("beginning_date"), local_sdf.format(dsb.getDateStart()));
                }
                if ("12/31/2100".equals(english_sdf.format(dsb.getDateEnd()))) {
                    this.setData(resword.getString("ending_date"), resword.getString("not_specified"));
                } else {
                    this.setData(resword.getString("ending_date"), local_sdf.format(dsb.getDateEnd()));
                }
                FilterBean fb = (FilterBean) session.getAttribute("newFilter");
                if (fb != null) {
                    this.setData("Added Filter", fb.getName());
                }
            } else if (page.equals(Page.APPLY_FILTER)) {
                DatasetBean dsb = (DatasetBean) session.getAttribute("newDataset");
                this.setData(resword.getString("beginning_date"), local_sdf.format(dsb.getDateStart()));
                this.setData(resword.getString("ending_date"), local_sdf.format(dsb.getDateEnd()));

            } else if (page.equals(Page.CONFIRM_DATASET)) {
                this.reset();
                this.setStudyInfoShown(false);
                this.setOrderedData(false);
                this.setCreateDataset(true);
                this.setSubmitDataModule(false);
                this.setExtractData(false);
                DatasetBean dsb = (DatasetBean) session.getAttribute("newDataset");
                this.setData(resword.getString("dataset_name"), dsb.getName());
                this.setData(resword.getString("dataset_description"), dsb.getDescription());
                int ev_count = dsb.getItemIds().size();
                this.setData(resword.getString("items_selected"), new Integer(ev_count).toString());

                if ("01/01/1900".equals(english_sdf.format(dsb.getDateStart()))) {
                    this.setData(resword.getString("beginning_date"), resword.getString("not_specified"));
                } else {
                    this.setData(resword.getString("beginning_date"), local_sdf.format(dsb.getDateStart()));
                }
                if ("12/31/2100".equals(english_sdf.format(dsb.getDateEnd()))) {
                    this.setData(resword.getString("ending_date"), resword.getString("not_specified"));
                } else {
                    this.setData(resword.getString("ending_date"), local_sdf.format(dsb.getDateEnd()));
                }
                FilterBean fb = (FilterBean) session.getAttribute("newFilter");
                if (fb != null) {
                    this.setData(resword.getString("added_filter"), fb.getName());
                }
            } else if (page.equals(Page.CREATE_FILTER_SCREEN_3_1)) {
                CRFVersionBean cvBean = (CRFVersionBean) session.getAttribute("cvBean");
                this.setData(resword.getString("CRF_version_selected"), cvBean.getName());
            } else if (page.equals(Page.CREATE_FILTER_SCREEN_3_2)) {
                SectionBean secBean = (SectionBean) session.getAttribute("secBean");
                this.setData(resword.getString("section_selected"), secBean.getName());
                Collection metadatas = (Collection) request.getAttribute("metadatas");
                this.setData(resword.getString("number_of_questions"), new Integer(metadatas.size()).toString());
            } else if (page.equals(Page.CREATE_FILTER_SCREEN_4)) {

            } else if (page.equals(Page.CREATE_FILTER_SCREEN_5)) {
                // blank here to prevent data reset, tbh
            } else if (page.equals(Page.ADMIN_SYSTEM)) {
                // blank here , info set in servlet itself
            } else if (page.equals(Page.VIEW_STUDY_SUBJECT) || page.equals(Page.LIST_EVENTS_FOR_SUBJECT)) {
                // special case, unlocks study name, subject name, and
                // visits
                // TODO set all this up, tbh
                /*
                 * set up the side info panel to create the following upon entry
                 * from the ViewStudyServlet Study X Subject Y StudyEventDef Z1
                 * StudyEventDef Z2 <status-tag> CRF A1 <status-tag> CRF A2 Z1
                 * should be collapsible/expandible, etc.
                 *
                 * We can pull things from the session and the request:
                 */
                /*
                 * StudyBean study = (StudyBean) request.getAttribute("study");
                 * StudySubjectBean studySubject = (StudySubjectBean)
                 * request.getAttribute("studySub"); EntityBeanTable table =
                 * (EntityBeanTable) request.getAttribute("table"); EventCRFBean
                 * ecb = (EventCRFBean)request.getAttribute("eventCRF");
                 * this.reset(); ArrayList rows = table.getRows(); ArrayList
                 * beans = DisplayStudyEventBean.generateBeansFromRows(rows);
                 *
                 *
                 * addStudyEventTree(study, studySubject, beans, ecb);
                 */
                // this.setIconInfoShown(false);
                // this.setManageSubject(true);
                this.reset();
                this.setStudyInfoShown(true);
                this.setOrderedData(true);
                this.setExtractData(false);
                this.setSubmitDataModule(false);
                this.setCreateDataset(false);
                this.setIconInfoShown(false);
                this.setManageSubject(true);
                request.setAttribute("showDDEIcon", Boolean.TRUE);

            } else if (page.equals(Page.ENTER_DATA_FOR_STUDY_EVENT) || page.equals(Page.ENTER_DATA_FOR_STUDY_EVENT_SERVLET)) {

                StudyBean study = (StudyBean) session.getAttribute("study");
                StudySubjectBean studySubject = (StudySubjectBean) request.getAttribute("studySubject");
                ArrayList beans = (ArrayList) request.getAttribute("beans");
                EventCRFBean ecb = (EventCRFBean) request.getAttribute("eventCRF");
                this.reset();
                addStudyEventTree(study, studySubject, beans, ecb, true);

                this.setStudyInfoShown(false);
                this.setOrderedData(true);
                this.setSubmitDataModule(true);
                this.setExtractData(false);
                this.setCreateDataset(false);
                this.setIconInfoShown(false);

            } else if (page.equals(Page.INTERVIEWER) || page.equals(Page.TABLE_OF_CONTENTS) || page.equals(Page.TABLE_OF_CONTENTS_SERVLET)
                || page.equals(Page.INITIAL_DATA_ENTRY) || page.equals(Page.INITIAL_DATA_ENTRY_SERVLET) || page.equals(Page.DOUBLE_DATA_ENTRY)
                || page.equals(Page.DOUBLE_DATA_ENTRY_SERVLET) || page.equals(Page.ADMIN_EDIT) || page.equals(Page.ADMIN_EDIT_SERVLET)) {
                /*
                 * pages designed to also follow the above format; check to see
                 * if they are in the session already, and does not refresh.
                 * TODO refine and test
                 */
                StudyBean study = (StudyBean) session.getAttribute("study");
                StudySubjectBean studySubject = (StudySubjectBean) request.getAttribute("studySubject");
                ArrayList beans = (ArrayList) request.getAttribute("beans");
                EventCRFBean ecb = (EventCRFBean) request.getAttribute("eventCRF");
                this.reset();
                addStudyEventTree(study, studySubject, beans, ecb, false);

                this.setStudyInfoShown(false);
                this.setOrderedData(true);
                this.setSubmitDataModule(true);
                this.setExtractData(false);
                this.setCreateDataset(false);
                this.setIconInfoShown(true);

            } else if (page.equals(Page.EDIT_DATASET)) {
                this.reset();

                // HashMap eventlist = (HashMap)
                // request.getAttribute("eventlist");
                HashMap eventlist = (LinkedHashMap) session.getAttribute("eventsForCreateDataset");
                ArrayList displayData = generateEventTree(eventlist);

                this.setCreateDataset(true);
                this.setOrderedData(true);
                this.setUserOrderedData(displayData);
                this.setStudyInfoShown(true);
                this.setSubmitDataModule(false);
                this.setExtractData(false);

                DatasetBean dsb = (DatasetBean) request.getAttribute("dataset");
                this.setData(resword.getString("dataset_name"), dsb.getName());
                this.setData(resword.getString("date_created"), local_sdf.format(dsb.getCreatedDate()));
                this.setData(resword.getString("dataset_owner"), dsb.getOwner().getName());
                this.setData(resword.getString("date_last_run"), local_sdf.format(dsb.getDateLastRun()));

            } else if (page.equals(Page.EXPORT_DATASETS)) {

                this.setCreateDataset(false);

            } else if (page.equals(Page.GENERATE_DATASET_HTML)) {
                DatasetBean db = (DatasetBean) request.getAttribute("dataset");
                ExtractBean exbean = (ExtractBean) request.getAttribute("extractBean");
                this.reset();
                ArrayList displayData = new ArrayList();

                displayData = generateDatasetTree(exbean, db);
                this.setUserOrderedData(displayData);
                this.setStudyInfoShown(false);
                this.setOrderedData(true);
                this.setExtractData(true);
                this.setSubmitDataModule(false);
                this.setCreateDataset(false);

            } else if (page.equals(Page.LIST_STUDY_SUBJECT) || page.equals(Page.LIST_STUDY_SUBJECTS) || page.equals(Page.SUBMIT_DATA)
                || page.equals(Page.SUBMIT_DATA_SERVLET)) {
                this.reset();
                this.setStudyInfoShown(true);
                this.setOrderedData(true);
                this.setExtractData(false);
                this.setSubmitDataModule(false);
                this.setCreateDataset(false);
                this.setIconInfoShown(false);
                this.setManageSubject(true);
                // don't want to show DDE icon key for subject matrix page
                request.setAttribute("showDDEIcon", Boolean.FALSE);

            } else if (page.equals(Page.VIEW_SECTION_DATA_ENTRY) || page.equals(Page.VIEW_SECTION_DATA_ENTRY_SERVLET)) {

                this.reset();
                this.setStudyInfoShown(true);
                this.setOrderedData(true);
                this.setExtractData(false);
                this.setSubmitDataModule(false);
                this.setCreateDataset(false);
                this.setIconInfoShown(true);
                this.setManageSubject(false);
            } else if (page.equals(Page.CREATE_SUBJECT_GROUP_CLASS) || page.equals(Page.CREATE_SUBJECT_GROUP_CLASS_CONFIRM)
                || page.equals(Page.UPDATE_SUBJECT_GROUP_CLASS) || page.equals(Page.UPDATE_SUBJECT_GROUP_CLASS_CONFIRM)) {

                this.reset();
                this.setStudyInfoShown(true);
                this.setOrderedData(true);
                this.setExtractData(false);
                this.setSubmitDataModule(false);
                this.setCreateDataset(false);
                this.setIconInfoShown(true);
                this.setManageSubject(false);
            }

            else {
                // automatically reset if we don't know what's happening
                this.reset();
                this.setStudyInfoShown(true);
                this.setOrderedData(true);
                this.setExtractData(false);
                this.setSubmitDataModule(false);
                this.setCreateDataset(false);
                this.setIconInfoShown(true);
                this.setManageSubject(false);
            }
        } catch (Exception e) {
            this.reset();
        }
    }

    /**
     * @return Returns the studyInfoShown.
     */
    public boolean isStudyInfoShown() {
        return studyInfoShown;
    }

    /**
     * @param studyInfoShown
     *            The studyInfoShown to set.
     */
    public void setStudyInfoShown(boolean studyInfoShown) {
        this.studyInfoShown = studyInfoShown;
    }

    /**
     * @return Returns the orderedData.
     */
    public boolean isOrderedData() {
        return orderedData;
    }

    /**
     * @param orderedData
     *            The orderedData to set.
     */
    public void setOrderedData(boolean orderedData) {
        this.orderedData = orderedData;
    }

    /**
     * @return Returns the userOrderedData.
     */
    public ArrayList getUserOrderedData() {
        return userOrderedData;
    }

    /**
     * @param userOrderedData
     *            The userOrderedData to set.
     */
    public void setUserOrderedData(ArrayList userOrderedData) {
        this.userOrderedData = userOrderedData;
    }

    /*
     * note that this has to change if the texts change, so this might be
     * something different in the future.
     */
    public String getStageImageText(DataEntryStage stage) {
        String answer = "";
        if (stage.isInitialDE()) {
            answer = "<img src='images/icon_InitialDE.gif' alt='Initial Data Entry'>";
        } else if (stage.isInitialDE_Complete()) {
            answer = "<img src='images/icon_InitialDEcomplete.gif' alt='Initial Data Entry Complete'>";
        } else if (stage.isDoubleDE()) {
            answer = "<img src='images/icon_DDE.gif' alt='Double Data Entry'>";
        } else if (stage.isDoubleDE_Complete()) {
            answer = "<img src='images/icon_DEcomplete.gif' alt='Data Entry Complete'>";
        } else if (stage.isAdmin_Editing()) {
            answer = "<img src='images/icon_AdminEdit.gif' alt='Administrative Editing'>";
        } else if (stage.isLocked()) {
            answer = "<img src='images/icon_Locked.gif' alt='Locked'>";
        } else {
            answer = "<img src='images/icon_Invalid.gif' alt='Invalid'>";
        }

        return answer;
    }

    public String getTOCLink(DisplayEventCRFBean dec) {
        String answer = "";
        if (!dec.getEventCRF().getStatus().equals(Status.DELETED) && !dec.getEventCRF().getStatus().equals(Status.AUTO_DELETED)) {
            if (dec.isContinueInitialDataEntryPermitted()) {
                answer = "InitialDataEntry?eventCRFId=" + dec.getEventCRF().getId();
            } else if (dec.isStartDoubleDataEntryPermitted()) {
                answer = "DoubleDataEntry?eventCRFId=" + dec.getEventCRF().getId();
            } else if (dec.isContinueDoubleDataEntryPermitted()) {
                answer = "DoubleDataEntry?eventCRFId=" + dec.getEventCRF().getId();
            } else if (dec.isPerformAdministrativeEditingPermitted()) {
                answer = "AdministrativeEditing?eventCRFId=" + dec.getEventCRF().getId();
            } else if (dec.isLocked()) {
                answer = "ViewSectionDataEntry?eventDefinitionCRFId=" + dec.getEventDefinitionCRF().getId() + "&ecId=" + dec.getEventCRF().getId() + "&tabId=1";
            }
        }
        return answer;
    }

    public void addStudyEventTree(StudyBean study, StudySubjectBean studySubject, ArrayList displayStudyEventBeans, EventCRFBean ecb, boolean withLink) {
        // method behind madness: we want the other pages to show
        // this information, but we don't want to hit the database when we do.
        // so, we gather--and hide--the information here.
        this.setStudyInfoShown(true);
        this.setOrderedData(false);

        ArrayList displayData = new ArrayList();
        // displayData.add(new StudyInfoPanelLine("Study", study.getName(),
        // true, false));
        // displayData.add(new StudyInfoPanelLine("<span class='alert'>Subject",
        // studySubject.getLabel() + "</span>", true, false));
        if (withLink) {
            displayData = generateTreeFromBeans(displayStudyEventBeans, displayData, studySubject, ecb);
        } else {
            displayData = generateTreeFromBeansWithoutLink(displayStudyEventBeans, displayData, studySubject, ecb);

        }
        this.setUserOrderedData(displayData);
    }

    /**
     * Generates a tree view in sdie info panel for submitting data page
     *
     * @param rows
     * @param displayData
     * @param studySubject
     * @param ecb
     * @return
     */
    public ArrayList generateTreeFromBeans(ArrayList rows, ArrayList displayData, StudySubjectBean studySubject, EventCRFBean ecb) {
        Iterator itRows = rows.iterator();

        displayData.add(new StudyInfoPanelLine(resword.getString("study_events"), "(" + rows.size() + ")", true, false, false));

        while (itRows.hasNext()) {
            DisplayStudyEventBean dseBean = (DisplayStudyEventBean) itRows.next();
            StudyEventBean seBean = dseBean.getStudyEvent();
            // checks whether the event is the current one
            if (ecb != null && ecb.getStudyEventId() == seBean.getId()) {
                displayData.add(new StudyInfoPanelLine("Study Event", seBean.getStudyEventDefinition().getName(), true, false, true));

            } else {
                displayData.add(new StudyInfoPanelLine("Study Event", seBean.getStudyEventDefinition().getName(), true, false, false));
            }

            displayData.add(new StudyInfoPanelLine("<b>Status: </b>", "<a href='EnterDataForStudyEvent?eventId=" + seBean.getId()
                + "'>" + seBean.getSubjectEventStatus().getName() + "</a>", false, false, false));
            ArrayList displayCRFs = dseBean.getDisplayEventCRFs();
            int count = 0;
            Iterator displayIt = displayCRFs.iterator();
            while (displayIt.hasNext()) {
                DisplayEventCRFBean dec = (DisplayEventCRFBean) displayIt.next();
                if (count == displayCRFs.size() - 1 && dseBean.getUncompletedCRFs().size() == 0) {
                    // last event CRF for this event
                    // it's the current crf
                    if (ecb != null && ecb.getId() == dec.getEventCRF().getId()) {// was
                        // getName(),
                        // tbh

                        displayData.add(new StudyInfoPanelLine("" + getStageImageText(dec.getStage()), "<span class='alert'>"
                            + dec.getEventCRF().getCrf().getName() + " " + dec.getEventCRF().getCrfVersion().getName() + "</span>", false, true, true));
                    } else {
                        displayData.add(new StudyInfoPanelLine("" + getStageImageText(dec.getStage()), "<a href='" + getTOCLink(dec) + "'>"
                            + dec.getEventCRF().getCrf().getName() + " " + dec.getEventCRF().getCrfVersion().getName() + "</a>", false, true, false));
                    }

                } else {
                    if (ecb != null && ecb.getId() == dec.getEventCRF().getId()) {
                        displayData.add(new StudyInfoPanelLine("" + getStageImageText(dec.getStage()), "<span class='alert'>"
                            + dec.getEventCRF().getCrf().getName() + " " + dec.getEventCRF().getCrfVersion().getName() + "</span>", false, false, true));
                    } else {
                        displayData.add(new StudyInfoPanelLine("" + getStageImageText(dec.getStage()), "<a href='" + getTOCLink(dec) + "'>"
                            + dec.getEventCRF().getCrf().getName() + " " + dec.getEventCRF().getCrfVersion().getName() + "</a>", false, false, false));
                    }
                }
                count++;
            }
            count = 0;
            ArrayList uncompleted = dseBean.getUncompletedCRFs();
            Iterator uncompIt = uncompleted.iterator();
            while (uncompIt.hasNext()) {
                DisplayEventDefinitionCRFBean dedc = (DisplayEventDefinitionCRFBean) uncompIt.next();
                if (count == uncompleted.size() - 1) {
                    if (ecb != null && ecb.getId() == dedc.getEventCRF().getId() && ecb.getCrf().getId() == dedc.getEventCRF().getCrf().getId()) {
                        // logger.info("ecb id*******" + ecb.getId() +
                        // dedc.getEventCRF().getId());
                        displayData.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>", "<span class='alert'>"
                            + dedc.getEdc().getCrf().getName() + "</span>", false, true, true));
                    } else {
                        displayData.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>",
                                "<a href='InitialDataEntry?eventDefinitionCRFId=" + dedc.getEdc().getId() + "&studyEventId=" + seBean.getId() + "&subjectId="
                                    + studySubject.getSubjectId() + "&eventCRFId=" + dedc.getEventCRF().getId() + "&crfVersionId="
                                    + dedc.getEdc().getDefaultVersionId() + "'>" + dedc.getEdc().getCrf().getName() + "</a>", false, true, false));

                    }
                } else {
                    if (ecb != null && ecb.getId() == dedc.getEventCRF().getId()) {
                        // logger.info("ecb id*******" + ecb.getId() +
                        // dedc.getEventCRF().getId());
                        displayData.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>", "<span class='alert'>"
                            + dedc.getEdc().getCrf().getName() + "</span>", false, false, true));
                    } else {
                        displayData.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>",
                                "<a href='InitialDataEntry?eventDefinitionCRFId=" + dedc.getEdc().getId() + "&studyEventId=" + seBean.getId() + "&subjectId="
                                    + studySubject.getSubjectId() + "&eventCRFId=" + dedc.getEventCRF().getId() + "&crfVersionId="
                                    + dedc.getEdc().getDefaultVersionId() + "'>" + dedc.getEdc().getCrf().getName() + "</a>", false, false, false));

                    }
                }
                count++;
            }
        }

        return displayData;
    }

    /**
     * Generates a tree view in sdie info panel for submitting data page
     *
     * @param rows
     * @param displayData
     * @param studySubject
     * @param ecb
     * @return
     */
    public ArrayList generateTreeFromBeansWithoutLink(ArrayList rows, ArrayList displayData, StudySubjectBean studySubject, EventCRFBean ecb) {
        Iterator itRows = rows.iterator();

        displayData.add(new StudyInfoPanelLine("Study Events", "(" + rows.size() + ")", true, false, false));

        while (itRows.hasNext()) {
            DisplayStudyEventBean dseBean = (DisplayStudyEventBean) itRows.next();
            StudyEventBean seBean = dseBean.getStudyEvent();
            // checks whether the event is the current one
            if (ecb != null && ecb.getStudyEventId() == seBean.getId()) {
                displayData.add(new StudyInfoPanelLine("Study Event", seBean.getStudyEventDefinition().getName(), true, false, true));

            } else {
                displayData.add(new StudyInfoPanelLine("Study Event", seBean.getStudyEventDefinition().getName(), true, false, false));
            }

            displayData.add(new StudyInfoPanelLine("<b>Status: </b>", seBean.getSubjectEventStatus().getName(), false, false, false));
            ArrayList displayCRFs = dseBean.getDisplayEventCRFs();
            int count = 0;
            Iterator displayIt = displayCRFs.iterator();
            while (displayIt.hasNext()) {
                DisplayEventCRFBean dec = (DisplayEventCRFBean) displayIt.next();
                if (count == displayCRFs.size() - 1 && dseBean.getUncompletedCRFs().size() == 0) {
                    // last event CRF for this event
                    // it's the current crf
                    if (ecb != null && ecb.getId() == dec.getEventCRF().getId()) {
                        displayData.add(new StudyInfoPanelLine("" + getStageImageText(dec.getStage()), "<span class='alert'>"
                            + dec.getEventCRF().getCrf().getName() + " " + dec.getEventCRF().getCrfVersion().getName() + "</span>", false, true, true));
                    } else {
                        displayData.add(new StudyInfoPanelLine("" + getStageImageText(dec.getStage()), dec.getEventCRF().getCrf().getName() + " "
                            + dec.getEventCRF().getCrfVersion().getName(), false, true, false));
                    }

                } else {
                    if (ecb != null && ecb.getId() == dec.getEventCRF().getId()) {
                        displayData.add(new StudyInfoPanelLine("" + getStageImageText(dec.getStage()), "<span class='alert'>"
                            + dec.getEventCRF().getCrf().getName() + " " + dec.getEventCRF().getCrfVersion().getName() + "</span>", false, false, true));
                    } else {
                        displayData.add(new StudyInfoPanelLine("" + getStageImageText(dec.getStage()), dec.getEventCRF().getCrf().getName() + " "
                            + dec.getEventCRF().getCrfVersion().getName(), false, false, false));
                    }
                }
                count++;
            }
            count = 0;
            ArrayList uncompleted = dseBean.getUncompletedCRFs();
            Iterator uncompIt = uncompleted.iterator();
            while (uncompIt.hasNext()) {
                DisplayEventDefinitionCRFBean dedc = (DisplayEventDefinitionCRFBean) uncompIt.next();
                if (count == uncompleted.size() - 1) {
                    if (ecb != null && ecb.getId() == dedc.getEventCRF().getId() && ecb.getCrf().getId() == dedc.getEventCRF().getCrf().getId()) {
                        // logger.info("ecb id*******" + ecb.getId() +
                        // dedc.getEventCRF().getId());
                        displayData.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>", "<span class='alert'>"
                            + dedc.getEdc().getCrf().getName() + "</span>", false, true, true));
                    } else {
                        displayData.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>", dedc.getEdc().getCrf().getName(),
                                false, true, false));

                    }
                } else {
                    if (ecb != null && ecb.getId() == dedc.getEventCRF().getId()) {
                        // logger.info("ecb id*******" + ecb.getId() +
                        // dedc.getEventCRF().getId());
                        displayData.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>", "<span class='alert'>"
                            + dedc.getEdc().getCrf().getName() + "</span>", false, false, true));
                    } else {
                        displayData.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>", dedc.getEdc().getCrf().getName(),
                                false, false, false));

                    }
                }
                count++;
            }
        }

        return displayData;
    }

    private ArrayList generateDatasetTree(ExtractBean eb, DatasetBean db) {
        ArrayList displayData = new ArrayList();

        ArrayList seds = eb.getStudyEvents();

        for (int i = 0; i < seds.size(); i++) {
            // second, iterate through seds
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seds.get(i);
            String repeating = "";
            if (sed.isRepeating()) {
                repeating = " (Repeating) ";
            }
            // if repeating:
            // change string to (Repeating)
            displayData.add(new StudyInfoPanelLine("Study Event Definition", sed.getName() + repeating, true, false));

            ArrayList crfs = sed.getCrfs();
            for (int j = 0; j < crfs.size(); j++) {
                CRFBean cb = (CRFBean) crfs.get(j);

                if (j < crfs.size() - 1 && crfs.size() > 1) {
                    displayData.add(new StudyInfoPanelLine("CRF", cb.getName() + " <b>" + ExtractBean.getSEDCRFCode(i + 1, j + 1) + "</b>", false, false));

                } else {// last crf
                    displayData.add(new StudyInfoPanelLine("CRF", cb.getName() + " <b>" + ExtractBean.getSEDCRFCode(i + 1, j + 1) + "</b>", false, true));
                }

                // third, iterate through crf versions
            }
        }
        return displayData;
    }

    private ArrayList generateEventTree(HashMap eventlist) {
        ArrayList displayData = new ArrayList();
        // Iterator keyIt = eventlist.keySet().iterator();
        // logger.info("how many events =" + eventlist.size());

        int count = 0;
        for (Iterator keyIt = eventlist.keySet().iterator(); keyIt.hasNext();) {
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) keyIt.next();
            displayData.add(new StudyInfoPanelLine("Definition", sed.getName(), true, false));
            ArrayList crfs = (ArrayList) eventlist.get(sed);
            int ordinal_crf = 1;
            for (int i = 0; i < crfs.size(); i++) {
                CRFBean crf = (CRFBean) crfs.get(i);
                if (ordinal_crf < crfs.size()) {
                    displayData.add(new StudyInfoPanelLine("CRF", "<a href='SelectItems?crfId=" + crf.getId() + "&defId=" + sed.getId() + "'>" + crf.getName()
                        + "</a>", false, false));
                } else {
                    displayData.add(new StudyInfoPanelLine("CRF", "<a href='SelectItems?crfId=" + crf.getId() + "&defId=" + sed.getId() + "'>" + crf.getName()
                        + "</a>", false, true));
                }
                ordinal_crf++;
            }
            count++;
        }
        // logger.info("how many definitions =" + count);
        return displayData;

    }

    /**
     * @return Returns the createDataset.
     */
    public boolean isCreateDataset() {
        return createDataset;
    }

    /**
     * @param createDataset
     *            The createDataset to set.
     */
    public void setCreateDataset(boolean createDataset) {
        this.createDataset = createDataset;
    }
}
