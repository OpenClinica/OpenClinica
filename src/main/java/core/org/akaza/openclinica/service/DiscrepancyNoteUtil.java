package core.org.akaza.openclinica.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.core.ResolutionStatus;
import core.org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import core.org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import core.org.akaza.openclinica.bean.submit.EventCRFBean;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import core.org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import core.org.akaza.openclinica.dao.submit.EventCRFDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.control.SpringServletAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * DiscrepancyNoteUtil is a convenience class for managing discrepancy notes,
 * such as getting all notes for a study, or filtering them by subject or
 * resolution status.
 */
@Service
public class DiscrepancyNoteUtil {
    // TODO: initialize these static members from the database.
    public static final Map<String, Integer> TYPES = new HashMap<String, Integer>();
    static {
        TYPES.put("Annotation", 2);
        TYPES.put("Reason for Change", 4);
        TYPES.put("Failed Validation Check", 1);
        TYPES.put("Query", 3);
    }
    public static final Map<String, Integer> RESOLUTION_STATUS = new HashMap<String, Integer>();
    static {
        RESOLUTION_STATUS.put("New", 1);
        RESOLUTION_STATUS.put("Updated", 2);
        RESOLUTION_STATUS.put("Resolution Proposed", 3);
        RESOLUTION_STATUS.put("Closed", 4);
        RESOLUTION_STATUS.put("Not Applicable", 5);
        RESOLUTION_STATUS.put("Closed-Modified", 6);
    }

    @Autowired
    private StudyDao studyDao;
    @Autowired
    @Qualifier("eventCRFJDBCDao")
    private EventCRFDAO eventCrfDAO;

    // These two variables are to arrange the Summary Statistics accordingly
    // Mantis Issue: 7771
    public static final String[] TYPE_NAMES = { "Query", "Failed Validation Check", "Reason for Change", "Annotation" };

    public static String[] getTypeNames() {
        return TYPE_NAMES;
    }

    /**
     * Take a List of DiscrepancyNoteBeans, and filter the List to return only
     * "parent" DiscrepancyNoteBeans. the parameter List<DiscrepancyNoteThread>
     * contains each parent the DiscrepancyNoteBeans are compared to in order to
     * filter the original List.
     *
     * @param dnBeans
     *            A List of DiscrepancyNoteBeans.
     * @param dnThreads
     *            A List of DiscrepancyNoteThreads.
     * @return The filtered List of DiscrepancyNoteBeans.
     */
    public List<DiscrepancyNoteBean> filterDiscNotesForOnlyParents(List<DiscrepancyNoteBean> dnBeans, List<DiscrepancyNoteThread> dnThreads) {

        // Take the parent of each thread, and compare it to each passed
        // in DiscrepancyNoteBean; only return a DiscrepancyNoteBean if it
        // equals a parent bean
        List<DiscrepancyNoteBean> newBeans = new ArrayList<DiscrepancyNoteBean>();
        DiscrepancyNoteBean tempBean;

        outer: for (DiscrepancyNoteBean noteBean : dnBeans) {
            for (DiscrepancyNoteThread dnThread : dnThreads) {
                // the parent...
                tempBean = dnThread.getLinkedNoteList().getFirst();
                if (tempBean != null) {
                    if (tempBean.getId() == noteBean.getId()) {
                        newBeans.add(noteBean);
                        // a noteBean matches a parent, so continue with the
                        // next noteBean
                        continue outer;
                    }
                }
            }

        }
        return newBeans;
    }

    public void injectParentDiscNotesIntoDisplayStudyEvents(List<DisplayStudyEventBean> displayStudyBeans, Set<Integer> resolutionStatusIds,
                                                            DataSource dataSource, int discNoteType) {

        if (displayStudyBeans == null) {
            return;
        }
        // booleans representing whether this method should only get
        // DiscrepancyNoteBeans with
        // certain resolution status or discrepancyNoteTypeId number.
        boolean hasResolutionStatus = this.checkResolutionStatus(resolutionStatusIds);
        boolean hasDiscNoteType = discNoteType >= 1 && discNoteType <= 4;

        DiscrepancyNoteDAO discrepancyNoteDAO = new DiscrepancyNoteDAO(dataSource);

        StudyEventBean studyEventBean;
        List<EventCRFBean> eventCRFBeans = new ArrayList<EventCRFBean>();
        List<DiscrepancyNoteBean> foundDiscNotes = new ArrayList<DiscrepancyNoteBean>();

        for (DisplayStudyEventBean dStudyEventBean : displayStudyBeans) {
            studyEventBean = dStudyEventBean.getStudyEvent();
            // All EventCRFs for a study event
            eventCRFBeans = eventCrfDAO.findAllByStudyEvent(studyEventBean);

            for (EventCRFBean eventCrfBean : eventCRFBeans) {
                // Find ItemData type notes associated with an event crf
                foundDiscNotes = discrepancyNoteDAO.findParentItemDataDNotesFromEventCRF(eventCrfBean);

                // filter for any specified disc note type
                if (!foundDiscNotes.isEmpty() && hasDiscNoteType) {
                    // only include disc notes that have the specified disc note
                    // type id
                    foundDiscNotes = filterforDiscNoteType(foundDiscNotes, discNoteType);
                }

                if (!foundDiscNotes.isEmpty()) {
                    if (!hasResolutionStatus) {
                        studyEventBean.getDiscBeanList().addAll(foundDiscNotes);
                    } else {
                        // Only include disc notes with a particular resolution
                        // status, specified by
                        // the parameter passed to the servlet or saved in a
                        // session variable
                        for (DiscrepancyNoteBean discBean : foundDiscNotes) {
                            for (int statusId : resolutionStatusIds) {
                                if (discBean.getResolutionStatusId() == statusId) {
                                    studyEventBean.getDiscBeanList().add(discBean);
                                }
                            }
                        }
                    }
                }

            } // end for(EventCRFBean...
        } // end for (DisplayStudyEventBean
    }


    public List<DiscrepancyNoteThread> removeEmptyDNThreads(List<DiscrepancyNoteThread> allDNThreads) {

        if (allDNThreads == null || allDNThreads.isEmpty()) {
            return new ArrayList<DiscrepancyNoteThread>();
        }
        List<DiscrepancyNoteThread> newList = new ArrayList<DiscrepancyNoteThread>();

        for (DiscrepancyNoteThread thread : allDNThreads) {
            if (thread.getLinkedNoteList().size() > 0) {
                newList.add(thread);
            }
        }
        return newList;
    }

    /**
     * Check whether the contents of a list of resolution status ids are valid.
     *
     * @param listOfStatusIds
     *            A HashSet of resolution status ids.
     * @return true or false, depending on whether the ids are valid.
     */
    public boolean checkResolutionStatus(Set<Integer> listOfStatusIds) {
        if (listOfStatusIds == null)
            return false;

        for (int id : listOfStatusIds) {
            if (id >= 1 && id <= 5) {
                return true;
            }

        }
        return false;
    }

    /**
     * Filter a List of DiscrepancyNoteBeans for a particular discrepancy note
     * type id.
     *
     * @param allDiscNotes
     *            A List of DiscrepancyNoteBeans prior to being filtered for a
     *            discrepancy note type id.
     * @param discNoteType
     *            An it representing a discrepancy note type id.
     * @return A List of DiscrepancyNoteBeans that have the specified
     *         discrepancy note type id.
     */
    public List<DiscrepancyNoteBean> filterforDiscNoteType(List<DiscrepancyNoteBean> allDiscNotes, int discNoteType) {

        // Do not filter this List if the discNoteType isn't between 1 and 4
        if (!(discNoteType >= 1 && discNoteType <= 4)) {
            return allDiscNotes;
        }
        List<DiscrepancyNoteBean> newDiscNotes = new ArrayList<DiscrepancyNoteBean>();

        for (DiscrepancyNoteBean dnBean : allDiscNotes) {
            if (dnBean.getDiscrepancyNoteTypeId() == discNoteType) {
                newDiscNotes.add(dnBean);
            }
        }
        return newDiscNotes;
    }

    /**
     * Generate a summary of statistics for a collection of discrepancy notes.
     *
     * @return A Map mapping the name of each type of note (e.g., "Annotation")
     *         to another Map containing that type's statistics.
     */
    public Map generateDiscNoteSummaryRefactored(DataSource ds, Study currentStudy, Set<Integer> resolutionStatusIds, int discNoteType) {

        DiscrepancyNoteDAO discrepancyNoteDAO = new DiscrepancyNoteDAO(ds);
        boolean filterDiscNotes = checkResolutionStatus(resolutionStatusIds);
        boolean filterforDiscNoteType = discNoteType >= 1 && discNoteType <= 4;
        // if (allDiscBeans == null || allDiscBeans.isEmpty())
        // return new HashMap();
        /*
         * This container is a Map of Maps. e.g., Failed Validation Check -->
         * Map [Total --> total number of Failed Validation Check type notes,
         * Open --> total number of Failed Validation Check notes that are Open
         * types, etc.]
         */
        Map<String, Map> summaryMap = new HashMap<String, Map>();
        // The internal Map, mapping the name of the status (e.g., Resolved) to
        // the number of
        // Notes that are Resolved for that particular discrepancy note type
        // (e.g., Failed Validation Check).
        Map<String, Integer> tempMap = null;
        int tempType = 0;
        int tempTotal = 0;

        Set<String> p = new HashSet<String>();
        if (filterforDiscNoteType) {
            String[] discNoteTypeNames = { "Failed Validation Check", "Annotation", "Query", "Reason for Change" };
            p.add(discNoteTypeNames[discNoteType - 1]);
        } else {
            p = TYPES.keySet();
        }

        String q = "";
        if (filterDiscNotes) {
            q += " AND ( ";
            int i = 0;
            for (Integer resolutionStatusId : resolutionStatusIds) {
                if (i > 0) {
                    q += " OR ";
                }
                q += " dn.resolution_status_id = " + resolutionStatusId;
                i++;
            }
            q += " ) ";
        }

        // initialize Map
        for (String discNoteTypeName : p) {

            tempMap = new HashMap<String, Integer>();
            // String query = "";
            // Create the summary or outer Map for each type name (e.g.,
            // Incomplete)
            summaryMap.put(discNoteTypeName, tempMap);
            tempType = TYPES.get(discNoteTypeName);
            // tempTotal = getNumberOfDiscNoteType(allDiscBeans, tempType);
            tempTotal = discrepancyNoteDAO.getViewNotesCountWithFilter(q + " AND dn.discrepancy_note_type_id =" + tempType, currentStudy);
            tempMap.put("Total", tempTotal);
            if (tempTotal == 0)
                continue;

            for (String statusName : RESOLUTION_STATUS.keySet()) {
                int number = discrepancyNoteDAO.getViewNotesCountWithFilter(
                        q + " AND dn.discrepancy_note_type_id =" + tempType + " AND dn.resolution_status_id = " + RESOLUTION_STATUS.get(statusName),
                        currentStudy);
                tempMap.put(statusName, number);
            }

        }

        return summaryMap;
    }

    public String countNotes(List<DiscrepancyNoteBean> discList, int statusId, int typeId) {
        Integer count = 0;
        for (int i = 0; i < discList.size(); i++) {
            DiscrepancyNoteBean discBean = discList.get(i);
            if (typeId == 0 && statusId != 0) {
                if (discBean.getResStatus() == ResolutionStatus.get(statusId)) {
                    count++;
                }
            } else if (statusId == 0 && typeId != 0) {
                if (discBean.getDiscrepancyNoteTypeId() == typeId) {
                    count++;
                }
            } else {
                if (discBean.getResStatus() == ResolutionStatus.get(statusId) && discBean.getDiscrepancyNoteTypeId() == typeId) {
                    count++;
                }
            }
        }
        return count.toString();
    }

    /**
     * An overloaded version of the previous method. The method generates a
     * HashMap containing data on the type of discrepancy note and status that
     * the user is currently filtering a JSP view for.
     *
     * @param discNoteType
     *            An id of a type of discrepancy note (e.g., Annotation).
     * @param discNoteStatus
     *            A HashSet of IDs for discrepancy note statuses (e.g., Open,
     *            Closed).
     * @return A HashMap mapping a String such as "status" to any filter on the
     *         status, for example, Open or resolved.
     */
    public Map<String, List<String>> generateFilterSummary(int discNoteType, Set<Integer> discNoteStatus) {
        Map<String, List<String>> filterSummary = new HashMap<String, List<String>>();
        if (discNoteType == 0 && discNoteStatus == null)
            return filterSummary;
        List<String> listOfStatusNames = new ArrayList<String>();
        filterSummary.put("status", listOfStatusNames);
        List<String> listOfTypeNames = new ArrayList<String>();
        filterSummary.put("type", listOfTypeNames);

        // Identify any filter for the resolution status
        int filterNum = 0;
        if (discNoteStatus != null) {
            for (String statusName : RESOLUTION_STATUS.keySet()) {
                filterNum = RESOLUTION_STATUS.get(statusName);
                for (int statusId : discNoteStatus) {
                    if (statusId == filterNum) {
                        filterSummary.get("status").add(statusName);
                    }
                }

            }
        }

        // Identify any filter for the resolution type
        filterNum = 0;
        for (String typeName : TYPES.keySet()) {
            filterNum = TYPES.get(typeName);
            if (discNoteType == filterNum) {
                filterSummary.get("type").add(typeName);
            }

        }

        return filterSummary;
    }

    /**
     * Get the number of DiscrepancyNoteBeans of a particular type, like "Failed
     * Validation Check."
     *
     * @param discrepancyNoteBeans
     *            A List of DiscrepancyNoteBeans.
     * @param discNoteType
     *            An int representing the dsicrepancy note type id.
     * @return Only any DiscrepancyNoteBeans that have this type id.
     */
    public int getNumberOfDiscNoteType(List<DiscrepancyNoteBean> discrepancyNoteBeans, int discNoteType) {
        int typeCount = 0;
        for (DiscrepancyNoteBean dBean : discrepancyNoteBeans) {
            if (dBean.getDiscrepancyNoteTypeId() == discNoteType) {
                typeCount++;
            }
        }

        return typeCount;

    }

    /**
     * Get the number of DiscrepancyNoteBeans of a particular type, like "Failed
     * Validation Check."
     *
     * @param discrepancyNoteBeans
     *            A List of DiscrepancyNoteBeans.
     * @param resStatusId
     *            An int representing the dsicrepancy note type id.
     * @return Only any DiscrepancyNoteBeans that have this type id.
     */
    public int getDiscNoteCountByStatusEventCRFId(List<DiscrepancyNoteBean> discrepancyNoteBeans, int resStatusId, int eventCRFId) {
        int typeCount = 0;
        for (DiscrepancyNoteBean dBean : discrepancyNoteBeans) {
            if (dBean.getResolutionStatusId() == resStatusId && dBean.getEventCRFId() == eventCRFId) {
                typeCount++;
            }
        }

        return typeCount;

    }

    /**
     * Get the number of DiscrepancyNoteBeans of a particular resolution status,
     * like Open or Resolved; and of a certain discrepancy note type.
     *
     * @param discrepancyNoteBeans
     *            A List of DiscrepancyNoteBeans.
     * @param typeId
     *            An int representing the dsicrepancy note type id.
     * @param resolutionStatus
     *            An int representing the resolution status.
     * @return Only any DiscrepancyNoteBeans that have this type id and
     *         resolution status.
     */
    public int getNumberByStatusOfNotes(List<DiscrepancyNoteBean> discrepancyNoteBeans, int typeId, int resolutionStatus) {
        int tempCount = 0;
        for (DiscrepancyNoteBean dBean : discrepancyNoteBeans) {
            if (dBean.getDiscrepancyNoteTypeId() == typeId && dBean.getResolutionStatusId() == resolutionStatus) {
                tempCount++;
            }
        }
        return tempCount;

    }

    /**
     * Examiine a List of StudyEventBeans to determine if any of them have any
     * discrepancy notes. The method is passed either a List of
     * DisplayStudyEventBeans or DisplayStudyEventBeans, in the manner of
     * studyEventsHaveDiscNotes(studyEventBeans, null) or
     * studyEventsHaveDiscNotes(null,displayStudyEventBeans)
     *
     * @param studyEvents
     *            A List of StudyEventBeans.
     * @param displayStudyEvents
     *            A List of DisplayStudyEventBeans.
     * @return boolean true if any of the study events have a List that contains
     *         some discrepancy notes.
     */
    public boolean studyEventsHaveDiscNotes(List<StudyEventBean> studyEvents, List<DisplayStudyEventBean> displayStudyEvents) {

        if (studyEvents != null) {
            for (StudyEventBean studyEBean : studyEvents) {
                if (studyEBean.getDiscBeanList().size() > 0) {
                    return true;
                }
            }
        }
        StudyEventBean studyEventBean = null;

        if (displayStudyEvents != null) {
            for (DisplayStudyEventBean displayStudyEventBean : displayStudyEvents) {
                studyEventBean = displayStudyEventBean.getStudyEvent();
                if (studyEventBean.getDiscBeanList().size() > 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public String getResolutionStatusName(int resId) {

        for (String resName : RESOLUTION_STATUS.keySet()) {
            if (resId == RESOLUTION_STATUS.get(resName)) {
                return resName;
            }
        }
        return "";
    }

    public String getResolutionStatusTypeName(int resTypeId) {

        for (String resName : TYPES.keySet()) {
            if (resTypeId == TYPES.get(resName)) {
                return resName;
            }
        }
        return "";
    }

    public Map<Integer, Map<String, Integer>> createDiscNoteMapByEventCRF(List<DisplayStudyEventBean> displayStudyEvents) {

        Map<Integer, Map<String, Integer>> discNoteMap = new HashMap<Integer, Map<String, Integer>>();
        if (displayStudyEvents == null || displayStudyEvents.isEmpty()) {
            return discNoteMap;
        }
        Map<String, Integer> innerMap = new HashMap<String, Integer>();

        SortedSet<Integer> allEventCRFIds = getEventCRFIdsFromDisplayEvents(displayStudyEvents);

        for (Integer eventCRFId : allEventCRFIds) {
            innerMap = getDiscNoteCountFromDisplayEvents(displayStudyEvents, eventCRFId);
            discNoteMap.put(eventCRFId, innerMap);
        }

        return discNoteMap;
    }

    private Map<String, Integer> getDiscNoteCountFromDisplayEvents(List<DisplayStudyEventBean> displayStudyEvents, int eventCRFId) {

        Map<String, Integer> innerMap = new HashMap<String, Integer>();
        if (eventCRFId == 0 || displayStudyEvents == null) {
            return innerMap;
        }

        List<DiscrepancyNoteBean> dnBeans;
        for (DisplayStudyEventBean displayStudyEvent : displayStudyEvents) {
            for (DisplayEventCRFBean decBean : displayStudyEvent.getDisplayEventCRFs()) {
                if (decBean.getEventCRF().getId() == eventCRFId) {
                    dnBeans = displayStudyEvent.getStudyEvent().getDiscBeanList();
                    for (String statusName : RESOLUTION_STATUS.keySet()) {
                        innerMap.put(statusName, getDiscNoteCountByStatusEventCRFId(dnBeans, RESOLUTION_STATUS.get(statusName), eventCRFId));
                    }
                    break;
                }
            }
        }

        return innerMap;
    }

    private SortedSet<Integer> getEventCRFIdsFromDisplayEvents(List<DisplayStudyEventBean> displayEvents) {
        SortedSet<Integer> treeSet = new TreeSet<Integer>();
        if (displayEvents == null || displayEvents.isEmpty()) {
            return treeSet;
        }

        StudyEventBean studyEventBean = null;
        List<DisplayEventCRFBean> displayEventCRFBeans = null;
        List<EventCRFBean> eventCRFBeans = null;

        for (DisplayStudyEventBean displayStudyEventBean : displayEvents) {
            displayEventCRFBeans = displayStudyEventBean.getDisplayEventCRFs();
            if (displayEventCRFBeans.isEmpty()) {
                eventCRFBeans = displayStudyEventBean.getStudyEvent().getEventCRFs();
                for (EventCRFBean evBean : eventCRFBeans) {
                    treeSet.add(evBean.getId());
                }
                continue; // move on to the next DisplayStudyEventBean
            }
            for (DisplayEventCRFBean disBean : displayEventCRFBeans) {
                treeSet.add(disBean.getEventCRF().getId());
            }
        }

        return treeSet;
    }

    public List<DiscrepancyNoteBean> filterDiscNotesBySubjectOrStudySubject(List<DiscrepancyNoteBean> allDiscNotes, int subjectId, int studySubjectId) {
        // This collection will hold the beans that are associated with either
        // the subject id or
        // study subject id
        List<DiscrepancyNoteBean> newNotes = new ArrayList<DiscrepancyNoteBean>();
        if (allDiscNotes == null || allDiscNotes.isEmpty()) {
            return newNotes;
        }
        for (DiscrepancyNoteBean dnBean : allDiscNotes) {
            if ("subject".equalsIgnoreCase(dnBean.getEntityType())) {
                if (dnBean.getEntityId() == subjectId) {
                    newNotes.add(dnBean);
                }
            } else if ("studySub".equalsIgnoreCase(dnBean.getEntityType())) {
                if (dnBean.getEntityId() == studySubjectId) {
                    newNotes.add(dnBean);
                }
            }
        }

        return newNotes;
    }

    public int getStudySubjectIdForDiscNote(DiscrepancyNoteBean discrepancyNoteBean, DataSource dataSource, int studyId) {
        if (discrepancyNoteBean == null)
            return 0;

        StudySubjectDAO studySubjectDAO = new StudySubjectDAO(dataSource);
        List<StudySubjectBean> studySubjectBeans = new ArrayList<StudySubjectBean>();

        if ("subject".equalsIgnoreCase(discrepancyNoteBean.getEntityType())) {
            studySubjectBeans = studySubjectDAO.findAllBySubjectId(discrepancyNoteBean.getEntityId());
            for (StudySubjectBean bean : studySubjectBeans) {
                if (bean.getStudyId() == studyId) {
                    return bean.getId();
                } else {
                    if (((Study) studyDao.findByPK(bean.getStudyId())).checkAndGetParentStudyId() == studyId) {
                        return bean.getId();
                    }
                }
            }
        } else if ("studySub".equalsIgnoreCase(discrepancyNoteBean.getEntityType())) {
            return discrepancyNoteBean.getEntityId();
        }
        return 0;
    }

    public List<DiscrepancyNoteThread> createThreads(List<DiscrepancyNoteBean> allDiscNotes, DataSource dataSource, Study currentStudy) {

        List<DiscrepancyNoteThread> dnThreads = new ArrayList<DiscrepancyNoteThread>();
        if (allDiscNotes == null || allDiscNotes.isEmpty()) {
            return dnThreads;
        }
        if (currentStudy == null) {
            currentStudy = new Study();
        }

        List<DiscrepancyNoteBean> childDiscBeans = new ArrayList<DiscrepancyNoteBean>();
        List<DiscrepancyNoteBean> eventCRFChildDiscBeans = new ArrayList<DiscrepancyNoteBean>();

        DiscrepancyNoteDAO discrepancyNoteDAO = new DiscrepancyNoteDAO(dataSource);
        DiscrepancyNoteThread tempDNThread = null;
        int resolutionStatusId = 0;

        for (DiscrepancyNoteBean discBean : allDiscNotes) {
            tempDNThread = new DiscrepancyNoteThread();
            tempDNThread.setStudyId(currentStudy.getStudyId());

            tempDNThread.getLinkedNoteList().addFirst(discBean);
            // childDiscBeans should be empty here
            if (!childDiscBeans.isEmpty()) {
                childDiscBeans.clear();
            }
            childDiscBeans = discBean.getChildren();
            Collections.sort(childDiscBeans);

            resolutionStatusId = discBean.getResolutionStatusId();
            // the thread's status id is the parent's in this case, when there
            // are no children
            tempDNThread.setLatestResolutionStatus(this.getResolutionStatusName(resolutionStatusId));

            if (!childDiscBeans.isEmpty()) {

                for (DiscrepancyNoteBean childBean : childDiscBeans) {
                    /*
                     * if (childBean.getResolutionStatusId() != resolutionStatusId) {
                     * // BWP issue 3468 WHO 5/2009: this local variable needs
                     * // to be updated>>
                     * resolutionStatusId = childBean.getResolutionStatusId();
                     * // <<
                     * tempDNThread.setLatestResolutionStatus(this.getResolutionStatusName(childBean.
                     * getResolutionStatusId()));
                     * }
                     */
                    childBean.setEventCrfWorkflowStatus(discBean.getEventCrfWorkflowStatus());
                    tempDNThread.getLinkedNoteList().offer(childBean);
                }
            }
            dnThreads.add(tempDNThread);

        }
        /*
         * // Do the filtering here; remove any DN threads that do not have any
         * // notes
         * LinkedList<DiscrepancyNoteBean> linkedList = null;
         * 
         * if (resolutionStatusIds != null && !resolutionStatusIds.isEmpty()) {
         * 
         * for (DiscrepancyNoteThread dnThread : dnThreads) {
         * linkedList = new LinkedList<DiscrepancyNoteBean>();
         * for (DiscrepancyNoteBean discBean : dnThread.getLinkedNoteList()) {
         * for (int statusId : resolutionStatusIds) {
         * if (discBean.getResolutionStatusId() == statusId) {
         * linkedList.offer(discBean);
         * }
         * }
         * }
         * dnThread.setLinkedNoteList(linkedList);
         * }
         * dnThreads = removeEmptyDNThreads(dnThreads);
         * }
         * if (discNoteType >= 1 && discNoteType <= 5) {
         * 
         * for (DiscrepancyNoteThread dnThread : dnThreads) {
         * linkedList = new LinkedList<DiscrepancyNoteBean>();
         * for (DiscrepancyNoteBean discBean : dnThread.getLinkedNoteList()) {
         * if (discBean.getDiscrepancyNoteTypeId() == discNoteType) {
         * linkedList.offer(discBean);
         * }
         * }
         * dnThread.setLinkedNoteList(linkedList);
         * }
         * dnThreads = removeEmptyDNThreads(dnThreads);
         * }
         */
        return dnThreads;
    }
}
