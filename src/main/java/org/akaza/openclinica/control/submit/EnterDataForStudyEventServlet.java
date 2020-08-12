/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.submit;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.AuditableEntityBean;
import core.org.akaza.openclinica.bean.core.DataEntryStage;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.login.UserAccountDAO;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.dao.submit.ItemDataDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.i18n.core.LocaleResolver;
import core.org.akaza.openclinica.service.crfdata.HideCRFManager;
import core.org.akaza.openclinica.service.managestudy.StudySubjectService;
import core.org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.akaza.openclinica.view.Page;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author ssachs
 */
public class EnterDataForStudyEventServlet extends SecureController {

    Locale locale;
    // < ResourceBundleresexception,respage;

    public static final String INPUT_EVENT_ID = "eventId";

    public static final String BEAN_STUDY_EVENT = "studyEvent";

    public static final String BEAN_STUDY_SUBJECT = "studySubject";

    public static final String BEAN_UNCOMPLETED_EVENTDEFINITIONCRFS = "uncompletedEventDefinitionCRFs";

    public static final String BEAN_DISPLAY_EVENT_CRFS = "displayEventCRFs";
    // The study event has an existing discrepancy note related to its location
    // property; this
    // value will be saved as a request attribute
    public final static String HAS_LOCATION_NOTE = "hasLocationNote";
    public final static String LOCATION_NOTE = "locationNote";
    // The study event has an existing discrepancy note related to its start
    // date property; this
    // value will be saved as a request attribute
    public final static String HAS_START_DATE_NOTE = "hasStartDateNote";
    public final static String START_DATE_NOTE = "startDateNote";
    // The study event has an existing discrepancy note related to its end date
    // property; this
    // value will be saved as a request attribute
    public final static String HAS_END_DATE_NOTE = "hasEndDateNote";
    public final static String END_DATE_NOTE = "endDateNote";

    @Autowired
    private StudySubjectService studySubjectService;
    @Autowired
    private StudyEventDAO studyEventDAO;
    @Autowired
    private EventCRFDAO eventCRFDAO;


    private StudyEventBean getStudyEvent(int eventId) throws Exception {

        Study studyWithSED = null;
        if (currentStudy.isSite())
            studyWithSED = currentStudy.getStudy();
        else
            studyWithSED = currentStudy;

        AuditableEntityBean aeb = studyEventDAO.findByPKAndStudy(eventId, studyWithSED);

        if (!aeb.isActive()) {
            addPageMessage(respage.getString("study_event_to_enter_data_not_belong_study"));
            throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, resexception.getString("study_event_not_belong_study"), "1");
        }

        StudyEventBean seb = (StudyEventBean) aeb;

        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
        StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(seb.getStudyEventDefinitionId());
        seb.setStudyEventDefinition(sedb);
        // A. Hamid mantis issue 5048
        if (!(currentRole.isDirector() || currentRole.isCoordinator()) &&  seb.isLocked()) {
            seb.setEditable(false);
        }
        return seb;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#processRequest()
     */
    @Override
    protected void processRequest() throws Exception {
        getEventCrfLocker().unlockAllForUser(ub.getId());
        FormProcessor fp = new FormProcessor(request);

        int eventId = fp.getInt(INPUT_EVENT_ID, true);
        request.setAttribute("eventId", eventId + "");
        request.setAttribute("originatingPage", URLEncoder.encode("EnterDataForStudyEvent?eventId=" + eventId, "UTF-8"));

        // so we can display the event for which we're entering data
        StudyEventBean seb = getStudyEvent(eventId);

        // so we can display the subject's label
        StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
        StudySubjectBean studySubjectBean = (StudySubjectBean) ssdao.findByPK(seb.getStudySubjectId());
        int studyId = studySubjectBean.getStudyId();

        Study study = (Study) getStudyDao().findByPK(studyId);

        // Get any disc notes for this study event
        DiscrepancyNoteDAO discrepancyNoteDAO = new DiscrepancyNoteDAO(sm.getDataSource());
        ArrayList<DiscrepancyNoteBean> allNotesforSubjectAndEvent = new ArrayList<DiscrepancyNoteBean>();

        allNotesforSubjectAndEvent = discrepancyNoteDAO.findExistingNoteForStudyEvent(seb);

        if (!allNotesforSubjectAndEvent.isEmpty()) {
            setRequestAttributesForNotes(allNotesforSubjectAndEvent);
        }

        // prepare to figure out what the display should look like
        ArrayList<EventCRFBean> eventCRFs = eventCRFDAO.findAllByStudyEvent(seb);
        ArrayList<Boolean> doRuleSetsExist = new ArrayList<Boolean>();

        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
        ArrayList eventDefinitionCRFs = (ArrayList) edcdao.findAllActiveByEventDefinitionId(study, seb.getStudyEventDefinitionId());

        // get the event definition CRFs for which no event CRF exists
        // the event definition CRFs must be populated with versions so we can
        // let the user choose which version he will enter data for
        // However, this method seems to be returning DisplayEventDefinitionCRFs
        // that contain valid eventCRFs??
        ArrayList uncompletedEventDefinitionCRFs = studySubjectService.getUncompletedCRFs(sm, eventDefinitionCRFs, eventCRFs, seb.getId());
        populateUncompletedCRFsWithCRFAndVersions(uncompletedEventDefinitionCRFs);

        // BWP 2816 << Attempt to provide the DisplayEventDefinitionCRF with a
        // valid owner
        // only if its container eventCRf has a valid id
        populateUncompletedCRFsWithAnOwner(uncompletedEventDefinitionCRFs);
        // >>BWP

        // for the event definition CRFs for which event CRFs exist, get
        // DisplayEventCRFBeans, which the JSP will use to determine what
        // the user will see for each event CRF

        // removing the below row in exchange for the ViewStudySubjectServlet
        // version, for two
        // reasons:
        // 1. concentrate all business logic in one place
        // 2. VSSS seems to handle the javascript creation correctly
        // ArrayList displayEventCRFs = getDisplayEventCRFs(eventCRFs,
        // eventDefinitionCRFs, seb.getSubjectEventStatus());


        ArrayList displayEventCRFs = studySubjectService.getDisplayEventCRFs(sm.getDataSource(), eventCRFs, eventDefinitionCRFs, ub, currentRole,
                seb.getWorkflowStatus(), study);

        // Issue 3212 BWP << hide certain CRFs at the site level
        if (currentStudy.isSite()) {
            HideCRFManager hideCRFManager = HideCRFManager.createHideCRFManager();

            uncompletedEventDefinitionCRFs = hideCRFManager.removeHiddenEventDefinitionCRFBeans(uncompletedEventDefinitionCRFs);

            displayEventCRFs = hideCRFManager.removeHiddenEventCRFBeans(displayEventCRFs);
        }
        // >>

        request.setAttribute(BEAN_STUDY_EVENT, seb);
        request.setAttribute("doRuleSetsExist", doRuleSetsExist);
        request.setAttribute(BEAN_STUDY_SUBJECT, studySubjectBean);
        request.setAttribute(BEAN_UNCOMPLETED_EVENTDEFINITIONCRFS, uncompletedEventDefinitionCRFs);
        request.setAttribute(BEAN_DISPLAY_EVENT_CRFS, displayEventCRFs);

        // @pgawade 31-Aug-2012 fix for issue #15315: Reverting to set the request variable "beans" back
        // this is for generating side info panel
        ArrayList beans = studySubjectService.getDisplayStudyEventsForStudySubject(studySubjectBean, sm.getDataSource(), ub, currentRole, getStudyDao());
        request.setAttribute("beans", beans);
        EventCRFBean ecb = new EventCRFBean();
        ecb.setStudyEventId(eventId);
        request.setAttribute("eventCRF", ecb);
        // Make available the study
        request.setAttribute("study", currentStudy);
        Study subjectStudy = getStudyDao().findByPK(studySubjectBean.getStudyId());
        request.setAttribute("subjectStudy", subjectStudy);
        forwardPage(Page.ENTER_DATA_FOR_STUDY_EVENT);
    }

    public ArrayList<DisplayStudyEventBean> getDisplayStudyEventsForStudySubject(StudySubjectBean studySub, DataSource ds, UserAccountBean ub,
                                                                                 StudyUserRoleBean currentRole, StudyDao studyDao) {
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);
        StudySubjectDAO ssdao = new StudySubjectDAO(ds);

        ArrayList events = studyEventDAO.findAllByStudySubject(studySub);
        studySub = (StudySubjectBean) ssdao.findByPK(studySub.getSubjectId());

        ArrayList displayEvents = new ArrayList();
        for (int i = 0; i < events.size(); i++) {
            StudyEventBean event = (StudyEventBean) events.get(i);

            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());

            event.setStudyEventDefinition(sed);

            // find all active crfs in the definition
            Study study = (Study) studyDao.findByPK(studySub.getStudyId());
            ArrayList eventDefinitionCRFs = (ArrayList) edcdao.findAllActiveByEventDefinitionId(study, sed.getId());
            ArrayList eventCRFs = eventCRFDAO.findAllByStudyEvent(event);

            // construct info needed on view study event page
            DisplayStudyEventBean de = new DisplayStudyEventBean();
            de.setStudyEvent(event);
            de.setDisplayEventCRFs(getDisplayEventCRFs(ds, eventCRFs, eventDefinitionCRFs, ub, currentRole, event.getWorkflowStatus(), study));
            ArrayList al = studySubjectService.getUncompletedCRFs(ds, eventDefinitionCRFs, eventCRFs, event.getWorkflowStatus(), sed.getId());
            studySubjectService.populateUncompletedCRFsWithCRFAndVersions(ds, al);
            de.setUncompletedCRFs(al);

            de.setMaximumSampleOrdinal(studyEventDAO.getMaxSampleOrdinal(sed, studySub));

            Status status = de.getStudyEvent().getStatus();
            if (status == Status.AVAILABLE || status == Status.AUTO_DELETED)
                displayEvents.add(de);
            // event.setEventCRFs(createAllEventCRFs(eventCRFs,
            // eventDefinitionCRFs));

        }

        return displayEvents;
    }

    public ArrayList getDisplayEventCRFs(DataSource ds, ArrayList eventCRFs, ArrayList eventDefinitionCRFs, UserAccountBean ub,
                                         StudyUserRoleBean currentRole, StudyEventWorkflowStatusEnum workflowStatus, Study study) {
        ArrayList answer = new ArrayList();

        // HashMap definitionsById = new HashMap();
        int i;
        /*
         * for (i = 0; i < eventDefinitionCRFs.size(); i++) {
         * EventDefinitionCRFBean edc = (EventDefinitionCRFBean)
         * eventDefinitionCRFs.get(i); definitionsById.put(new
         * Integer(edc.getStudyEventDefinitionId()), edc); }
         */

        CRFDAO cdao = new CRFDAO(ds);
        CRFVersionDAO cvdao = new CRFVersionDAO(ds);
        FormLayoutDAO fldao = new FormLayoutDAO(ds);
        ItemDataDAO iddao = new ItemDataDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);

        for (i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecb = (EventCRFBean) eventCRFs.get(i);

            // populate the event CRF with its crf bean
            int crfVersionId = ecb.getCRFVersionId();
            int formLayoutId = ecb.getFormLayoutId();
            CRFBean cb = cdao.findByLayoutId(formLayoutId);
            ecb.setCrf(cb);

            CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(crfVersionId);
            ecb.setCrfVersion(cvb);
            FormLayoutBean flb = (FormLayoutBean) fldao.findByPK(formLayoutId);
            ecb.setFormLayout(flb);

            // then get the definition so we can call
            // DisplayEventCRFBean.setFlags
            int studyEventId = ecb.getStudyEventId();
            int studyEventDefinitionId = studyEventDAO.getDefinitionIdFromStudyEventId(studyEventId);

            // EventDefinitionCRFBean edc = (EventDefinitionCRFBean)
            // definitionsById.get(new Integer(
            // studyEventDefinitionId));
            // fix problem of the above code(commented out), find the correct
            // edc, note that on definitionId can be related to multiple
            // eventdefinitioncrfBeans
            EventDefinitionCRFBean edc = edcdao.findByStudyEventDefinitionIdAndCRFId(study, studyEventDefinitionId, cb.getId());
            // below added 092007 tbh

            // above added 092007-102007 tbh
            // TODO need to refactor since this is similar to other code, tbh
            if (edc != null) {
                ArrayList<FormLayoutBean> versions = (ArrayList<FormLayoutBean>) fldao.findAllActiveByCRF(edc.getCrfId());
                edc.setVersions(versions);

                // System.out.println("edc is not null, need to set flags");
                DisplayEventCRFBean dec = new DisplayEventCRFBean();
                dec.setEventDefinitionCRF(edc);
                // System.out.println("edc.isDoubleEntry()" +
                // edc.isDoubleEntry() + ecb.getId());
                dec.setFlags(ecb, ub, currentRole, edc.isDoubleEntry());

                if (dec.isLocked()) {
                    // System.out.println("*** found a locked DEC:
                    // "+edc.getCrfName());
                }
                ArrayList idata = iddao.findAllByEventCRFId(ecb.getId());
                if (!idata.isEmpty()) {
                    // consider an event crf started only if item data get
                    // created
                    answer.add(dec);
                }
            }
        }
        return answer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.control.core.SecureController#mayProceed()
     */
    @Override
    protected void mayProceed() throws InsufficientPermissionException {

        locale = LocaleResolver.getLocale(request);
        // <
        // resexception=ResourceBundle.getBundle(
        // "core.org.akaza.openclinica.i18n.exceptions",locale);
        // < respage =
        // ResourceBundle.getBundle("core.org.akaza.openclinica.i18n.page_messages",
        // locale);

        String exceptionName = resexception.getString("no_permission_to_submit_data");
        String noAccessMessage = respage.getString("may_not_enter_data_for_this_study");

        if (SubmitDataUtil.mayViewData(ub, currentRole)) {
            return;
        }

        addPageMessage(noAccessMessage);
        throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, exceptionName, "1");
    }

    private void populateUncompletedCRFsWithAnOwner(List<DisplayEventDefinitionCRFBean> displayEventDefinitionCRFBeans) {
        if (displayEventDefinitionCRFBeans == null || displayEventDefinitionCRFBeans.isEmpty()) {
            return;
        }
        UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());
        UserAccountBean userAccountBean;
        EventCRFBean eventCRFBean;

        for (DisplayEventDefinitionCRFBean dedcBean : displayEventDefinitionCRFBeans) {

            eventCRFBean = dedcBean.getEventCRF();
            if (eventCRFBean != null && eventCRFBean.getOwner() == null && eventCRFBean.getOwnerId() > 0) {
                userAccountBean = (UserAccountBean) userAccountDAO.findByPK(eventCRFBean.getOwnerId());

                eventCRFBean.setOwner(userAccountBean);
            }

            // Failing the above, obtain the owner from the
            // EventDefinitionCRFBean
            if (eventCRFBean != null && eventCRFBean.getOwner() == null) {
                int ownerId = dedcBean.getEdc().getOwnerId();
                if (ownerId > 0) {
                    userAccountBean = (UserAccountBean) userAccountDAO.findByPK(ownerId);

                    eventCRFBean.setOwner(userAccountBean);
                }
            }

        }

    }

    private void populateUncompletedCRFsWithCRFAndVersions(ArrayList uncompletedEventDefinitionCRFs) {
        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        FormLayoutDAO fldao = new FormLayoutDAO(sm.getDataSource());

        int size = uncompletedEventDefinitionCRFs.size();
        for (int i = 0; i < size; i++) {
            DisplayEventDefinitionCRFBean dedcrf = (DisplayEventDefinitionCRFBean) uncompletedEventDefinitionCRFs.get(i);
            CRFBean cb = (CRFBean) cdao.findByPK(dedcrf.getEdc().getCrfId());
            // note that we do not check status in the above query, so let's
            // check it here, tbh 102007
            if (cb.getStatus().equals(Status.AVAILABLE)) {
                // the above does not allow us to show the CRF as a thing with
                // status of 'invalid' so we have to
                // go to the JSP for this one, I think
                dedcrf.getEdc().setCrf(cb);

                ArrayList theVersions = (ArrayList) fldao.findAllActiveByCRF(dedcrf.getEdc().getCrfId());
                ArrayList versions = new ArrayList();
                HashMap<String, FormLayoutBean> crfVersionIds = new HashMap<String, FormLayoutBean>();

                for (int j = 0; j < theVersions.size(); j++) {
                    FormLayoutBean crfVersion = (FormLayoutBean) theVersions.get(j);
                    crfVersionIds.put(String.valueOf(crfVersion.getId()), crfVersion);
                }

                if (!dedcrf.getEdc().getSelectedVersionIds().equals("")) {
                    String[] kk = dedcrf.getEdc().getSelectedVersionIds().split(",");
                    for (String string : kk) {
                        if (crfVersionIds.get(string) != null) {
                            versions.add(crfVersionIds.get(string));
                        }
                    }
                } else {
                    versions = theVersions;
                }

                dedcrf.getEdc().setVersions(versions);
                // added tbh 092007, fix for 1461
                if (versions != null && versions.size() != 0) {
                    boolean isLocked = false;
                    for (int ii = 0; ii < versions.size(); ii++) {
                        FormLayoutBean crfvb = (FormLayoutBean) versions.get(ii);
                        logger.debug("...checking versions..." + crfvb.getName());
                        if (!crfvb.getStatus().equals(Status.AVAILABLE)) {
                            logger.debug("found a non active crf version");
                            isLocked = true;
                        }
                    }
                    logger.debug("re-set event def, line 240: " + isLocked);
                    if (isLocked) {
                        dedcrf.setStatus(Status.LOCKED);
                        dedcrf.getEventCRF().setStage(DataEntryStage.LOCKED);
                    }
                    uncompletedEventDefinitionCRFs.set(i, dedcrf);
                } else {// above added 092007, tbh
                    dedcrf.setStatus(Status.LOCKED);
                    dedcrf.getEventCRF().setStage(DataEntryStage.LOCKED);
                    uncompletedEventDefinitionCRFs.set(i, dedcrf);
                } // added 102007, tbh
            } else {
                dedcrf.getEdc().setCrf(cb);
                logger.debug("_found a non active crf _");
                dedcrf.setStatus(Status.LOCKED);
                dedcrf.getEventCRF().setStage(DataEntryStage.LOCKED);
                dedcrf.getEdc().getCrf().setStatus(Status.LOCKED);
                uncompletedEventDefinitionCRFs.set(i, dedcrf);
            } // enclosing if statement added 102007, tbh
        }
    }

    /**
     * Generate a list of DisplayEventCRFBean objects for a study event. Some of
     * the DisplayEventCRFBeans will represent uncompleted Event CRFs; others
     * will represent Event CRFs which are in initial data entry, have completed
     * initial data entry, are in double data entry, or have completed double
     * data entry. The list is sorted using the DisplayEventCRFBean's compareTo method (that
     * is, using the event definition crf bean's ordinal value.) Also, the
     * setFlags method of each DisplayEventCRFBean object will have been called
     * once.
     * @param studyEvent The study event for which we want the Event CRFs.
     * @param ecdao An EventCRFDAO from which to grab the study event's Event CRFs.
     * @param edcdao An EventDefinitionCRFDAO from which to grab the Event CRF. Definitions which apply to the study event.
     * @return A list of DisplayEventCRFBean objects releated to the study
     * event, ordered by the EventDefinitionCRF ordinal property, and
     * with flags already set.
     */
    public static ArrayList getDisplayEventCRFs(StudyEventBean studyEvent, EventCRFDAO ecdao, EventDefinitionCRFDAO edcdao, FormLayoutDAO fldao,
                                                UserAccountBean user, StudyUserRoleBean surb) {
        ArrayList answer = new ArrayList();
        HashMap indexByCRFId = new HashMap();

        ArrayList eventCRFs = ecdao.findAllByStudyEvent(studyEvent);
        ArrayList eventDefinitionCRFs = edcdao.findAllByEventDefinitionId(studyEvent.getStudyEventDefinitionId());

        // TODO: map this out to another function
        ArrayList crfVersions = (ArrayList) fldao.findAll();
        HashMap crfIdByCRFVersionId = new HashMap();
        for (int i = 0; i < crfVersions.size(); i++) {
            FormLayoutBean cvb = (FormLayoutBean) crfVersions.get(i);
            crfIdByCRFVersionId.put(new Integer(cvb.getId()), new Integer(cvb.getCrfId()));
        }

        // put the event definition crfs inside DisplayEventCRFs
        for (int i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
            // set number of crf versions
            ArrayList<FormLayoutBean> versions = (ArrayList<FormLayoutBean>) fldao.findAllActiveByCRF(edcb.getCrfId());
            edcb.setVersions(versions);
            DisplayEventCRFBean decb = new DisplayEventCRFBean();
            decb.setEventDefinitionCRF(edcb);

            answer.add(decb);
            indexByCRFId.put(new Integer(edcb.getCrfId()), new Integer(answer.size() - 1));
        }

        // attach EventCRFs to the DisplayEventCRFs
        for (int i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecb = (EventCRFBean) eventCRFs.get(i);

            Integer crfVersionId = new Integer(ecb.getFormLayoutId());
            if (crfIdByCRFVersionId.containsKey(crfVersionId)) {
                Integer crfId = (Integer) crfIdByCRFVersionId.get(crfVersionId);

                if (crfId != null && indexByCRFId.containsKey(crfId)) {
                    Integer indexObj = (Integer) indexByCRFId.get(crfId);

                    if (indexObj != null) {
                        int index = indexObj.intValue();
                        if (index > 0 && index < answer.size()) {
                            DisplayEventCRFBean decb = (DisplayEventCRFBean) answer.get(index);
                            decb.setEventCRF(ecb);
                            answer.set(index, decb);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < answer.size(); i++) {
            DisplayEventCRFBean decb = (DisplayEventCRFBean) answer.get(i);
            decb.setFlags(decb.getEventCRF(), user, surb, decb.getEventDefinitionCRF().isDoubleEntry());
            answer.set(i, decb);
        }

        return answer;
    }

    /**
     * If DiscrepancyNoteBeans have a certain column value, then set flags that
     * a JSP will check in the request attribute. This is a convenience method
     * called by the processRequest() method.
     * @param discBeans A List of DiscrepancyNoteBeans.
     */
    private void setRequestAttributesForNotes(List<DiscrepancyNoteBean> discBeans) {
        for (DiscrepancyNoteBean discrepancyNoteBean : discBeans) {
            if ("location".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(HAS_LOCATION_NOTE, "yes");
                request.setAttribute(LOCATION_NOTE, discrepancyNoteBean);
            } else if ("start_date".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(HAS_START_DATE_NOTE, "yes");
                request.setAttribute(START_DATE_NOTE, discrepancyNoteBean);

            } else if ("end_date".equalsIgnoreCase(discrepancyNoteBean.getColumn())) {
                request.setAttribute(HAS_END_DATE_NOTE, "yes");
                request.setAttribute(END_DATE_NOTE, discrepancyNoteBean);
            }

        }

    }
}
