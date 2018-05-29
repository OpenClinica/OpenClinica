/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.service.managestudy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DisplayEventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.FormLayoutDAO;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 * 
 */
public class StudySubjectServiceImpl implements StudySubjectService {

    private DataSource dataSource;

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<DisplayStudyEventBean> getDisplayStudyEventsForStudySubject(StudySubjectBean studySubject, UserAccountBean userAccount,
            StudyUserRoleBean currentRole) {

        StudyEventDAO studyEventDao = new StudyEventDAO(dataSource);
        StudyEventDefinitionDAO studyEventDefinitionDao = new StudyEventDefinitionDAO(dataSource);
        StudyDAO studyDao = new StudyDAO(dataSource);
        EventDefinitionCRFDAO eventDefinitionCrfDao = new EventDefinitionCRFDAO(dataSource);
        EventCRFDAO eventCrfDao = new EventCRFDAO(dataSource);
        CRFDAO crfDao = new CRFDAO(dataSource);
        FormLayoutDAO formLayoutDAO = new FormLayoutDAO(dataSource);

        ArrayList events = studyEventDao.findAllByStudySubject(studySubject);

        Map<Integer, StudyEventDefinitionBean> eventDefinitionByEvent = studyEventDefinitionDao.findByStudySubject(studySubject.getId());

        StudyBean study = (StudyBean) studyDao.findByPK(studySubject.getStudyId());

        Map<Integer, SortedSet<EventDefinitionCRFBean>> eventDefinitionCrfByStudyEventDefinition;
        if (study.getParentStudyId() < 1) { // Is a study
            eventDefinitionCrfByStudyEventDefinition = eventDefinitionCrfDao.buildEventDefinitionCRFListByStudyEventDefinitionForStudy(studySubject.getId());
        } else { // Is a site
            eventDefinitionCrfByStudyEventDefinition = eventDefinitionCrfDao.buildEventDefinitionCRFListByStudyEventDefinition(studySubject.getId(),
                    study.getId(), study.getParentStudyId());
        }

        Map<Integer, SortedSet<EventCRFBean>> eventCrfListByStudyEvent = eventCrfDao.buildEventCrfListByStudyEvent(studySubject.getId());

        Map<Integer, Integer> maxOrdinalByStudyEvent = studyEventDefinitionDao.buildMaxOrdinalByStudyEvent(studySubject.getId());

        Set<Integer> nonEmptyEventCrf = eventCrfDao.buildNonEmptyEventCrfIds(studySubject.getId());

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
            DisplayStudyEventBean de = new DisplayStudyEventBean();
            de.setStudyEvent(event);
            de.setDisplayEventCRFs((ArrayList<DisplayEventCRFBean>) getDisplayEventCRFs(eventCRFs, userAccount, currentRole, event.getSubjectEventStatus(),
                    study, nonEmptyEventCrf, formLayoutById, crfById, event.getStudyEventDefinitionId(), eventDefinitionCRFs));
            ArrayList<DisplayEventDefinitionCRFBean> al = getUncompletedCRFs(eventDefinitionCRFs, eventCRFs, event.getSubjectEventStatus(), nonEmptyEventCrf,
                    formLayoutById, crfById);
            populateUncompletedCRFsWithCRFAndVersions(al, formLayoutById, crfById);
            de.setUncompletedCRFs(al);

            // de.setMaximumSampleOrdinal(studyEventDao.getMaxSampleOrdinal(sed,
            // studySubject));
            de.setMaximumSampleOrdinal(maxOrdinalByStudyEvent.get(event.getStudyEventDefinitionId()));

            displayEvents.add(de);
            // event.setEventCRFs(createAllEventCRFs(eventCRFs,
            // eventDefinitionCRFs));

        }

        return displayEvents;
    }

    private List<DisplayEventCRFBean> getDisplayEventCRFs(List eventCRFs, UserAccountBean ub, StudyUserRoleBean currentRole, SubjectEventStatus status,
            StudyBean study, Set<Integer> nonEmptyEventCrf, Map<Integer, FormLayoutBean> formLayoutById, Map<Integer, CRFBean> crfById,
            Integer studyEventDefinitionId, List eventDefinitionCRFs) {
        ArrayList<DisplayEventCRFBean> answer = new ArrayList<DisplayEventCRFBean>();

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
            if (status.equals(SubjectEventStatus.LOCKED) || status.equals(SubjectEventStatus.SKIPPED)) {
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

                if (dec.isLocked()) {
                    // System.out.println("*** found a locked DEC:
                    // "+edc.getCrfName());
                }
                if (nonEmptyEventCrf.contains(ecb.getId())) {
                    // consider an event crf started only if item data get
                    // created
                    answer.add(dec);
                }
            }
        }

        return answer;
    }

    private ArrayList<DisplayEventDefinitionCRFBean> getUncompletedCRFs(List eventDefinitionCRFs, List eventCRFs, SubjectEventStatus status,
            Set<Integer> nonEmptyEventCrf, Map<Integer, FormLayoutBean> formLayoutById, Map<Integer, CRFBean> crfById) {
        int i;
        HashMap<Integer, Boolean> completed = new HashMap<Integer, Boolean>();
        HashMap<Integer, EventCRFBean> startedButIncompleted = new HashMap<Integer, EventCRFBean>();
        ArrayList<DisplayEventDefinitionCRFBean> answer = new ArrayList<DisplayEventDefinitionCRFBean>();

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

            if (nonEmptyEventCrf.contains(ecrf.getId())) {// this crf has data
                                                          // already
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void populateUncompletedCRFsWithCRFAndVersions(ArrayList<DisplayEventDefinitionCRFBean> uncompletedEventDefinitionCRFs,
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

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
