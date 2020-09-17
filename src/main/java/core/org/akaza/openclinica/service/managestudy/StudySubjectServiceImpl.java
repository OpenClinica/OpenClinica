/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.service.managestudy;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.DataEntryStage;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.core.SubjectEventStatus;
import core.org.akaza.openclinica.bean.login.StudyUserRoleBean;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.*;
import core.org.akaza.openclinica.bean.submit.CRFVersionBean;
import core.org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.core.SessionManager;
import core.org.akaza.openclinica.dao.admin.CRFDAO;
import core.org.akaza.openclinica.dao.hibernate.EventCrfDao;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.StudyEventDao;
import core.org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.CRFVersionDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.dao.submit.FormLayoutDAO;
import core.org.akaza.openclinica.domain.datamap.EventCrf;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.domain.datamap.StudyEvent;
import core.org.akaza.openclinica.domain.datamap.StudySubject;
import core.org.akaza.openclinica.service.StudyEventService;
import core.org.akaza.openclinica.service.StudyEventServiceImpl;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.sql.DataSource;
import java.util.*;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 */
public class StudySubjectServiceImpl implements StudySubjectService {

    private DataSource dataSource;

    private final String COMMON = "common";
    @Autowired
    @Qualifier("studyEventJDBCDao")
    private StudyEventDAO studyEventDAO;
    @Autowired
    @Qualifier("eventCRFJDBCDao")
    private EventCRFDAO eventCrfDAO;
    @Autowired
    private EventCrfDao eventCrfDao;
    @Autowired
    private StudySubjectDAO studySubjectDAO;
    @Autowired
    StudySubjectDao studySubjectDao;
    @Autowired
    private StudyEventDao studyEventDao;
    @Autowired
    StudyEventService studyEventService;


    @Transactional
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<DisplayStudyEventBean> getDisplayStudyEventsForStudySubject(StudySubjectBean studySubject, UserAccountBean userAccount,
                                                                            StudyUserRoleBean currentRole, Study study) {

        StudyEventDefinitionDAO studyEventDefinitionDao = new StudyEventDefinitionDAO(dataSource);
        EventDefinitionCRFDAO eventDefinitionCrfDao = new EventDefinitionCRFDAO(dataSource);
        CRFDAO crfDao = new CRFDAO(dataSource);
        FormLayoutDAO formLayoutDAO = new FormLayoutDAO(dataSource);
        ArrayList events = studyEventDAO.findAllByStudySubject(studySubject);

        Map<Integer, StudyEventDefinitionBean> eventDefinitionByEvent = studyEventDefinitionDao.findByStudySubject(studySubject.getId());

        Map<Integer, SortedSet<EventDefinitionCRFBean>> eventDefinitionCrfByStudyEventDefinition;
        if (!study.isSite()) { // Is a study
            eventDefinitionCrfByStudyEventDefinition = eventDefinitionCrfDao.buildEventDefinitionCRFListByStudyEventDefinitionForStudy(studySubject.getId());
        } else { // Is a site
            eventDefinitionCrfByStudyEventDefinition = eventDefinitionCrfDao.buildEventDefinitionCRFListByStudyEventDefinition(studySubject.getId(),
                    study.getStudyId(), study.checkAndGetParentStudyId());
        }

        Map<Integer, SortedSet<EventCRFBean>> eventCrfListByStudyEvent = eventCrfDAO.buildEventCrfListByStudyEvent(studySubject.getId());

        Map<Integer, Integer> maxOrdinalByStudyEvent = studyEventDefinitionDao.buildMaxOrdinalByStudyEvent(studySubject.getId());

        Set<Integer> nonEmptyEventCrf = eventCrfDAO.buildNonEmptyEventCrfIds(studySubject.getId());

        Map<Integer, FormLayoutBean> formLayoutById = formLayoutDAO.buildFormLayoutById(studySubject.getId());

        Map<Integer, CRFBean> crfById = crfDao.buildCrfById(studySubject.getId());

        ArrayList<DisplayStudyEventBean> displayEvents = new ArrayList<DisplayStudyEventBean>();
        for (int i = 0; i < events.size(); i++) {
            StudyEventBean event = (StudyEventBean) events.get(i);

            StudyEventDefinitionBean sed = eventDefinitionByEvent.get(event.getStudyEventDefinitionId());
            event.setStudyEventDefinition(sed);

            List eventDefinitionCRFs = new ArrayList((eventDefinitionCrfByStudyEventDefinition.containsKey(sed.getId())
                    ? eventDefinitionCrfByStudyEventDefinition.get(sed.getId()) : Collections.EMPTY_LIST));

            List eventCRFs = new ArrayList(
                    (eventCrfListByStudyEvent.containsKey(event.getId())) ? eventCrfListByStudyEvent.get(event.getId()) : Collections.EMPTY_LIST);

            // construct info needed on view study event page
            DisplayStudyEventBean displayStudyEventBean = new DisplayStudyEventBean();
            displayStudyEventBean.setStudyEvent(event);
            displayStudyEventBean.setDisplayEventCRFs((ArrayList<DisplayEventCRFBean>) getDisplayEventCRFs(eventCRFs, userAccount, currentRole, event.getWorkflowStatus(),
                    study, nonEmptyEventCrf, formLayoutById, crfById, event.getStudyEventDefinitionId(), eventDefinitionCRFs));
            ArrayList<DisplayEventDefinitionCRFBean> unstartedEventCrfs = getUnstartedEventCrfs(eventDefinitionCRFs, eventCRFs, event.getWorkflowStatus(), nonEmptyEventCrf,
                    formLayoutById, crfById);
            populateUnstartedCRFsWithCRFAndVersions(unstartedEventCrfs, formLayoutById, crfById);
            displayStudyEventBean.setUncompletedCRFs(unstartedEventCrfs);
            displayStudyEventBean.setMaximumSampleOrdinal(maxOrdinalByStudyEvent.get(event.getStudyEventDefinitionId()));

            displayEvents.add(displayStudyEventBean);
        }

        return displayEvents;
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
            ArrayList eventCRFs = eventCrfDAO.findAllByStudyEvent(event);

            // construct info needed on view study event page
            DisplayStudyEventBean de = new DisplayStudyEventBean();
            de.setStudyEvent(event);
            de.setDisplayEventCRFs(getDisplayEventCRFs(ds, eventCRFs, eventDefinitionCRFs, ub, currentRole, event.getWorkflowStatus(), study));
            ArrayList al = getUncompletedCRFs(ds, eventDefinitionCRFs, eventCRFs, event.getWorkflowStatus(), sed.getId());
            populateUncompletedCRFsWithCRFAndVersions(ds, al);
            de.setUncompletedCRFs(al);

            de.setMaximumSampleOrdinal(studyEventDAO.getMaxSampleOrdinal(sed, studySub));

            if (de.getStudyEvent().isAvailable() || (de.getStudyEvent().isArchived() && !de.getStudyEvent().isRemoved()))
                displayEvents.add(de);
        }

        return displayEvents;
    }

    public DisplayStudyEventBean getDisplayStudyEventsForStudySubject(StudySubjectBean studySub, StudyEventBean event, DataSource ds,
                                                                      UserAccountBean ub, StudyUserRoleBean currentRole, Study study) {
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);
        EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(ds);

        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(event.getStudyEventDefinitionId());
        event.setStudyEventDefinition(sed);

        // find all active crfs in the definition
        ArrayList eventDefinitionCRFs = edcdao.findAllActiveByEventDefinitionId(sed.getId());

        ArrayList eventCRFs = eventCrfDAO.findAllByStudyEvent(event);

        // construct info needed on view study event page
        DisplayStudyEventBean de = new DisplayStudyEventBean();
        de.setStudyEvent(event);
        de.setDisplayEventCRFs(getDisplayEventCRFs(ds, eventCRFs, eventDefinitionCRFs, ub, currentRole, event.getWorkflowStatus(),
                study));
        ArrayList al = getUncompletedCRFs(ds, eventDefinitionCRFs, eventCRFs, event.getWorkflowStatus(), event.getId());
        // ViewStudySubjectServlet.populateUncompletedCRFsWithCRFAndVersions(ds,
        // al);
        de.setUncompletedCRFs(al);

        return de;
    }

    public List<DisplayEventCRFBean> getDisplayEventCRFs(List eventCRFs, UserAccountBean ub, StudyUserRoleBean currentRole, StudyEventWorkflowStatusEnum status,
                                                         Study study, Set<Integer> nonEmptyEventCrf, Map<Integer, FormLayoutBean> formLayoutById, Map<Integer, CRFBean> crfById,
                                                         Integer studyEventDefinitionId, List eventDefinitionCRFs) {
        ArrayList<DisplayEventCRFBean> answer = new ArrayList<>();

        FormLayoutDAO formLayoutDao = new FormLayoutDAO(dataSource);
        Iterator edcs = eventDefinitionCRFs.iterator();
        while(edcs.hasNext()) {
            EventDefinitionCRFBean edcBean = (EventDefinitionCRFBean) edcs.next();
            ArrayList<FormLayoutBean> versions = (ArrayList<FormLayoutBean>) formLayoutDao.findAllActiveByCRF(edcBean.getCrfId());
            edcBean.setVersions(versions);
        }

        for (int i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecb = (EventCRFBean) eventCRFs.get(i);

            // populate the event CRF with its crf bean
            int formLayoutId = ecb.getFormLayoutId();

            FormLayoutBean flb = formLayoutById.get(formLayoutId);
            ecb.setFormLayout(flb);

            CRFBean cb = crfById.get(flb.getCrfId());
            ecb.setCrf(cb);

            EventDefinitionCRFBean edc = null;
            Iterator it = eventDefinitionCRFs.iterator();
            while (it.hasNext()) {
                EventDefinitionCRFBean edcBean = (EventDefinitionCRFBean) it.next();
                if (edcBean.getCrfId() == cb.getId()) {
                    edc = edcBean;
                    break;
                }
            }
            // below added 092007 tbh
            // rules updated 112007 tbh
            if (status.equals(SubjectEventStatus.LOCKED) || status.equals(SubjectEventStatus.SKIPPED) || status.equals(SubjectEventStatus.STOPPED)) {
                ecb.setStage(DataEntryStage.LOCKED);

                // we need to set a SED-wide flag here, because other edcs
                // in this event can be filled in and change the status, tbh
            } else if (status.equals(SubjectEventStatus.INVALID)) {
                ecb.setStage(DataEntryStage.LOCKED);
            } else if (!cb.getStatus().equals(Status.AVAILABLE)) {
                ecb.setStage(DataEntryStage.LOCKED);
            } else if (!flb.getStatus().equals(Status.AVAILABLE)) {
                ecb.setStage(DataEntryStage.LOCKED);
            }

            // above added 092007-102007 tbh
            // TODO need to refactor since this is similar to other code, tbh
            if (edc != null) {
                // System.out.println("edc is not null, need to set flags");
                DisplayEventCRFBean dec = new DisplayEventCRFBean();
                dec.setEventDefinitionCRF(edc);
                // System.out.println("edc.isDoubleEntry()" +
                // edc.isDoubleEntry() + ecb.getId());
                dec.setFlags(ecb, ub, currentRole, edc.isDoubleEntry());

                // OC-11019 Form status does not update to complete when a form contains ONLY the participant contact info
                // this crf has data already or determined completed by StudyEventWorkflowStatusEnum(COMPLETED)
                if (nonEmptyEventCrf.contains(ecb.getId()) || status.equals(StudyEventWorkflowStatusEnum.COMPLETED)
                        || dec.getEventCRF().getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)) {
                    // consider an event crf started only if item data get
                    // created
                    answer.add(dec);
                }
            }
        }

        return answer;
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
                answer.add(dec);
            }
        }
        return answer;
    }

    private ArrayList<DisplayEventDefinitionCRFBean> getUnstartedEventCrfs(List eventDefinitionCRFs, List eventCRFs, StudyEventWorkflowStatusEnum status,
                                                                           Set<Integer> nonEmptyEventCrf, Map<Integer, FormLayoutBean> formLayoutById, Map<Integer, CRFBean> crfById) {
        int i;
        HashMap<Integer, Boolean> completed = new HashMap<>();
        HashMap<Integer, EventCRFBean> startedButIncompleted = new HashMap<>();
        ArrayList<DisplayEventDefinitionCRFBean> answer = new ArrayList<>();

        /**
         * A somewhat non-standard algorithm is used here: let answer = empty;
         * foreach event definition ED, set isCompleted(ED) = false foreach
         * event crf EC, set isCompleted(EC.getEventDefinition()) = true foreach
         * event definition ED, if (!isCompleted(ED)) { answer += ED; } return
         * answer; This algorithm is guaranteed to find all the event
         * definitions for which no event CRF exists.
         *
         * The motivation for using this algorithm is reducing the number of
         * database hits.
         *
         * -jun-we have to add more CRFs here: the event CRF which dones't have
         * item data yet
         */

        for (i = 0; i < eventDefinitionCRFs.size(); i++) {
            EventDefinitionCRFBean edcrf = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);
            completed.put(new Integer(edcrf.getCrfId()), Boolean.FALSE);
            startedButIncompleted.put(new Integer(edcrf.getCrfId()), new EventCRFBean());
        }

        for (i = 0; i < eventCRFs.size(); i++) {
            EventCRFBean ecrf = (EventCRFBean) eventCRFs.get(i);
            int crfId = formLayoutById.get(ecrf.getFormLayoutId()).getCrfId();
            // OC-11019 Form status does not update to complete when a form contains ONLY the participant contact info
            // this crf has data already or determined completed by StudyEventWorkflowStatusEnum(COMPLETED)
            if (nonEmptyEventCrf.contains(ecrf.getId()) || status.equals(StudyEventWorkflowStatusEnum.COMPLETED)
                    || ecrf.getWorkflowStatus().equals(EventCrfWorkflowStatusEnum.COMPLETED)) {
                completed.put(new Integer(crfId), Boolean.TRUE);
            } else {// event crf got created, but no data entered
                startedButIncompleted.put(new Integer(crfId), ecrf);
            }
        }

        // TODO possible relation to 1689 here, tbh
        for (i = 0; i < eventDefinitionCRFs.size(); i++) {
            DisplayEventDefinitionCRFBean dedc = new DisplayEventDefinitionCRFBean();
            EventDefinitionCRFBean edcrf = (EventDefinitionCRFBean) eventDefinitionCRFs.get(i);

            // Display event definition without event CRF data only if it is
            // available (i.e., not removed)
            if (edcrf.getStatus().equals(Status.AVAILABLE)) {
                dedc.setEdc(edcrf);
                // below added tbh, 112007 to fix bug 1943
                if (status.equals(SubjectEventStatus.LOCKED)) {
                    dedc.setStatus(Status.LOCKED);
                }
                Boolean b = completed.get(new Integer(edcrf.getCrfId()));
                EventCRFBean ev = startedButIncompleted.get(new Integer(edcrf.getCrfId()));
                if (b == null || !b.booleanValue()) {
                    dedc.setEventCRF(ev);
                    answer.add(dedc);
                }
            }

        }
        return answer;
    }

    public ArrayList getUncompletedCRFs(DataSource ds, ArrayList eventDefinitionCRFs, ArrayList eventCRFs, StudyEventWorkflowStatusEnum workflowStatus, int studyEventId) {

        HashMap<Integer, EventDefinitionCRFBean> eventDefinitionsHashMap = new HashMap();

        for (Object eventDefinitionObj : eventDefinitionCRFs) {
            EventDefinitionCRFBean eventDefinitionBean = (EventDefinitionCRFBean) eventDefinitionObj;
            eventDefinitionsHashMap.put(eventDefinitionBean.getCrfId(), eventDefinitionBean);
        }

        ArrayList answer = new ArrayList();

        StudyEventBean studyEventBean = new StudyEventBean();
        studyEventBean.setId(studyEventId);
        ArrayList<EventCRFBean> listOfActiveEventCRFs = eventCrfDAO.findAllByStudyEvent(studyEventBean);

        ArrayList<Integer> listOfCrfVersionsInUse = new ArrayList();
        CRFVersionDAO cvdao = new CRFVersionDAO(ds);
        for (EventCRFBean eventCRFBean : listOfActiveEventCRFs) {
            listOfCrfVersionsInUse.add(cvdao.findByPK(eventCRFBean.getCRFVersionId()).getId());
        }

        CRFDAO cdao = new CRFDAO(ds);
        for (Integer crfVersionId : listOfCrfVersionsInUse) {
            eventDefinitionsHashMap.remove(cdao.findByVersionId(crfVersionId).getId());
        }

        for (EventDefinitionCRFBean eventDefinitionCrfBean : eventDefinitionsHashMap.values()) {
            DisplayEventDefinitionCRFBean dedc = new DisplayEventDefinitionCRFBean();
            dedc.setEdc(eventDefinitionCrfBean);
            dedc.setEventCRF(new EventCRFBean());
            answer.add(dedc);
        }

        return answer;
    }

    /**
     * Finds all the event definitions for which no event CRF exists - which is
     * the list of event definitions with uncompleted event CRFs.
     * @param eventDefinitionCRFs All of the event definition CRFs for this study event.
     * @param eventCRFs All of the event CRFs for this study event.
     * @return The list of event definitions for which no event CRF exists.
     */
    public ArrayList getUncompletedCRFs(SessionManager sm, ArrayList eventDefinitionCRFs, ArrayList eventCRFs, int studyEventId) {

        HashMap<Integer, EventDefinitionCRFBean> eventDefinitionsHashMap = new HashMap();

        for (Object eventDefinitionObj : eventDefinitionCRFs) {
            EventDefinitionCRFBean eventDefinitionBean = (EventDefinitionCRFBean) eventDefinitionObj;
            eventDefinitionsHashMap.put(eventDefinitionBean.getCrfId(), eventDefinitionBean);
        }

        ArrayList answer = new ArrayList();

        StudyEventBean studyEventBean = new StudyEventBean();
        studyEventBean.setId(studyEventId);
        ArrayList<EventCRFBean> listOfActiveEventCRFs = eventCrfDAO.findAllByStudyEvent(studyEventBean);

        ArrayList<Integer> listOfCrfVersionsInUse = new ArrayList();
        CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
        for (EventCRFBean eventCRFBean : listOfActiveEventCRFs) {
            listOfCrfVersionsInUse.add(cvdao.findByPK(eventCRFBean.getCRFVersionId()).getId());
        }

        CRFDAO cdao = new CRFDAO(sm.getDataSource());
        for (Integer crfVersionId : listOfCrfVersionsInUse) {
            eventDefinitionsHashMap.remove(cdao.findByVersionId(crfVersionId).getId());
        }

        for (EventDefinitionCRFBean eventDefinitionCrfBean : eventDefinitionsHashMap.values()) {
            DisplayEventDefinitionCRFBean dedc = new DisplayEventDefinitionCRFBean();
            dedc.setEdc(eventDefinitionCrfBean);
            dedc.setEventCRF(new EventCRFBean());
            answer.add(dedc);
        }

        return answer;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void populateUnstartedCRFsWithCRFAndVersions(ArrayList<DisplayEventDefinitionCRFBean> uncompletedEventDefinitionCRFs,
                                                        Map<Integer, FormLayoutBean> formLayoutById, Map<Integer, CRFBean> crfById) {

        FormLayoutDAO formLayoutDAo = new FormLayoutDAO(dataSource);

        int size = uncompletedEventDefinitionCRFs.size();
        for (int i = 0; i < size; i++) {
            DisplayEventDefinitionCRFBean dedcrf = uncompletedEventDefinitionCRFs.get(i);
            CRFBean cb = crfById.get(dedcrf.getEdc().getCrfId());
            dedcrf.getEdc().setCrf(cb);

            ArrayList<FormLayoutBean> theVersions = (ArrayList<FormLayoutBean>) formLayoutDAo.findAllActiveByCRF(dedcrf.getEdc().getCrfId());
            ArrayList<FormLayoutBean> versions = new ArrayList<FormLayoutBean>();
            HashMap<String, FormLayoutBean> formLayoutIds = new HashMap<String, FormLayoutBean>();

            for (int j = 0; j < theVersions.size(); j++) {
                FormLayoutBean formLayout = theVersions.get(j);
                formLayoutIds.put(String.valueOf(formLayout.getId()), formLayout);
            }

            if (!dedcrf.getEdc().getSelectedVersionIds().equals("")) {
                String[] kk = dedcrf.getEdc().getSelectedVersionIds().split(",");
                for (String string : kk) {
                    if (formLayoutIds.get(string) != null) {
                        versions.add(formLayoutIds.get(string));
                    }
                }
            } else {
                versions = theVersions;
            }
            dedcrf.getEdc().setVersions(versions);
            uncompletedEventDefinitionCRFs.set(i, dedcrf);
        }
    }

    public void populateUncompletedCRFsWithCRFAndVersions(DataSource ds, ArrayList uncompletedEventDefinitionCRFs) {
        CRFDAO cdao = new CRFDAO(ds);
        CRFVersionDAO cvdao = new CRFVersionDAO(ds);

        int size = uncompletedEventDefinitionCRFs.size();
        for (int i = 0; i < size; i++) {
            DisplayEventDefinitionCRFBean dedcrf = (DisplayEventDefinitionCRFBean) uncompletedEventDefinitionCRFs.get(i);
            CRFBean cb = (CRFBean) cdao.findByPK(dedcrf.getEdc().getCrfId());
            dedcrf.getEdc().setCrf(cb);

            ArrayList theVersions = (ArrayList) cvdao.findAllActiveByCRF(dedcrf.getEdc().getCrfId());
            ArrayList versions = new ArrayList();
            HashMap<String, CRFVersionBean> crfVersionIds = new HashMap<String, CRFVersionBean>();

            for (int j = 0; j < theVersions.size(); j++) {
                CRFVersionBean crfVersion = (CRFVersionBean) theVersions.get(j);
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
            uncompletedEventDefinitionCRFs.set(i, dedcrf);
        }
    }



    public Boolean isSignable(int studySubjectId) {

        boolean archivedCommonEvent=false;
        StudySubject studySubject = studySubjectDao.findById(studySubjectId);
        List<StudyEvent> studyEvents = studySubject.getStudyEvents();

        if (studySubject.getStatus().isSigned() || studyEvents.size() == 0){
            return false;}

        for (StudyEvent studyEvent: studyEvents) {
            if(studyEvent.getStudyEventDefinition().getType().equals(COMMON)){
                List <EventCrf> eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectId(studyEvent.getStudyEventId(), studySubject.getOcOid());
                if(eventCrfs.size()!=0 && eventCrfs.get(0).isCurrentlyArchived()){
                    archivedCommonEvent= true;
                }
            }

            if (!studyEvent.isCurrentlyRemoved() && !studyEvent.isCurrentlyArchived() && !archivedCommonEvent) {
                if (!studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.NOT_SCHEDULED)
                        && !studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.SKIPPED)
                        && !studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.STOPPED)
                        && !studyEvent.getWorkflowStatus().equals(StudyEventWorkflowStatusEnum.COMPLETED)) {
                    return false;
                } else {
                    if (!studyEventService.isEventSignable(studyEvent)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
