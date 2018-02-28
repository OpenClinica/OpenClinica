/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 * Created on Jul 7, 2005
 */
package org.akaza.openclinica.bean.extract;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.ApplicationConstants;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    public static final int SAS_FORMAT = 1;

    public static final int SPSS_FORMAT = 2;

    public static final int CSV_FORMAT = 3;

    public static final int PDF_FORMAT = 4;

    public static final int XLS_FORMAT = 5;

    public static final int TXT_FORMAT = 6;

    public static final String UNGROUPED = "Ungrouped";

    // java.text.SimpleDateFormat sdf = new
    // SimpleDateFormat(ResourceBundleProvider
    // .getFormatBundle().getString("date_format_string_ds"));
    java.text.SimpleDateFormat sdf = new SimpleDateFormat(ApplicationConstants.getDateFormatInExtract());

    java.text.SimpleDateFormat long_sdf = new SimpleDateFormat(ResourceBundleProvider.getFormatBundle().getString("date_time_format_string"));

    private int format = 1;

    private String showUniqueId = "1";

    private StudyBean parentStudy;

    private StudyBean study;

    private DatasetBean dataset;

    private final DataSource ds;

    private Date dateCreated;

    // an array of StudyEventDefinitionBean objects
    private ArrayList studyEvents;

    private HashMap eventData;

    /**
     * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION - separate the subjects
     *      from study subjects - leave the subjects array name - add
     *      subjectsnostudy for original subjects The two array could be
     *      different because they are not synchronized in the code
     */
    // an array of subjects and study_subject
    private final ArrayList subjects;
    private ArrayList subjectsnostudy;
    private ArrayList hBASE_EVENTSIDE;
    private ArrayList hBASE_ITEMGROUPSIDE;
    private ArrayList aBASE_ITEMDATAID;

    // an array of discrepancy notes, followed by a hash map of notes
    // private ArrayList discrepancies;

    // private HashMap discrepancyNotes;

    private HashMap groupNames;

    // a hashmap of group names to use for generating the keys and column names
    /**
     * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION - remove subjectsAdded -
     *      it is not used - add InKeysHelper - a HashMap that will speed up the
     *      data extract display
     */
    private HashMap hmInKeys;

    // a hashmap indicating which subjects have already been added
    // key is subjectId as Integer, value is Boolean.TRUE
    // private HashMap subjectsAdded;

    // keys are studySubjectId-studyEventDefinitionId-sampleOrdinal-crfId-ItemID
    // strings
    // values are the corresponding values in the item_data table
    private HashMap data;

    // keys are studyEventDefinitionId Integer
    // values are the maximum sample ordinal for that sed
    private final HashMap maxOrdinals;

    // keys are itemId Integer
    // values are Boolean.TRUE
    // if an item has its id in the keySet for this HashMap,
    // that means the user has chosen to display this item in the report
    private final HashMap selectedItems;

    private final HashMap selectedSEDs;

    private final HashMap selectedSEDCRFs;

    private HashMap<String, String> eventDescriptions;// for spss only

    private ArrayList<String> eventHeaders; // for displaying dataset in HTML
    // view,event

    // header

    private ArrayList<Object> itemNames;// for displaying dataset in HTML
    // view,item header

    private ArrayList rowValues; // for displaying dataset in html view

    private HashMap studyGroupMap;

    private HashMap studyGroupMaps;

    // to contain all the studysubject ids and link them to another hashmap, the
    // study group map above, tbh
    private ArrayList studyGroupClasses; // for displaying groups for
    // subjects in the exported dataset,
    // tbh

    private StudySubjectBean currentSubject;

    private int subjIndex = -1;

    private CRFBean currentCRF;

    private int crfIndex = -1;

    private int maxItemDataBeanOrdinal = 0;

    private StudyEventDefinitionBean currentDef;

    private int sedIndex = -1;

    private ItemBean currentItem;

    private int itemIndex = -1;

    private final boolean defChanged = false;

    // Added By Hamid
    // EventCRFBean eventCRF = new EventCRFBean();

    public ExtractBean(DataSource ds) {
        this.ds = ds;
        study = new StudyBean();
        parentStudy = new StudyBean();
        studyEvents = new ArrayList();

        data = new HashMap();
        maxOrdinals = new HashMap();
        subjects = new ArrayList();
        // discrepancies = new ArrayList();
        // discrepancyNotes = new HashMap();
        // above added 7/07, tbh
        // subjectsAdded = new HashMap();
        selectedItems = new HashMap();
        selectedSEDs = new HashMap();
        groupNames = new HashMap();
        selectedSEDCRFs = new HashMap();
        itemNames = new ArrayList<Object>();
        rowValues = new ArrayList();
        eventHeaders = new ArrayList<String>();
        eventDescriptions = new HashMap<String, String>();
        // groups = new HashMap();
        // above added 7/07, tbh

        hmInKeys = new HashMap();
        hBASE_EVENTSIDE = new ArrayList();
        hBASE_ITEMGROUPSIDE = new ArrayList();
        aBASE_ITEMDATAID = new ArrayList();
    }

    public ExtractBean(DataSource ds, SimpleDateFormat sdf, SimpleDateFormat long_sdf) {
        this.sdf = sdf;
        this.long_sdf = long_sdf;
        // TODO need to refactor the below
        this.ds = ds;
        study = new StudyBean();
        parentStudy = new StudyBean();
        studyEvents = new ArrayList();

        data = new HashMap();
        maxOrdinals = new HashMap();
        subjects = new ArrayList();
        // subjectsAdded = new HashMap();
        selectedItems = new HashMap();
        selectedSEDs = new HashMap();
        groupNames = new HashMap();
        selectedSEDCRFs = new HashMap();
        itemNames = new ArrayList<Object>();
        rowValues = new ArrayList();
        eventHeaders = new ArrayList<String>();
        eventDescriptions = new HashMap<String, String>();

        hmInKeys = new HashMap();
        hBASE_EVENTSIDE = new ArrayList();
        hBASE_ITEMGROUPSIDE = new ArrayList();
        aBASE_ITEMDATAID = new ArrayList();

    }

    /**
     * @return Returns the eventDescriptions.
     */
    public HashMap getEventDescriptions() {
        return eventDescriptions;
    }

    /**
     * @param eventDescriptions
     *            The eventDescriptions to set.
     */
    public void setEventDescriptions(HashMap<String, String> eventDescriptions) {
        this.eventDescriptions = eventDescriptions;
    }

    //
    // TODO place to add additional metadata, tbh
    //
    public void computeReportMetadata(ReportBean answer) {
        // ///////////////////
        // //
        // HEADER //
        // //
        // ///////////////////
        answer.nextCell("Database Export Header Metadata");
        answer.nextRow();

        answer.nextCell("Dataset Name");
        answer.nextCell(dataset.getName());
        answer.nextRow();

        answer.nextCell("Date");

        answer.nextCell(sdf.format(new Date(System.currentTimeMillis())));
        answer.nextRow();

        answer.nextCell("Protocol ID");
        answer.nextCell(getParentProtocolId());
        answer.nextRow();

        answer.nextCell("Study Name");
        answer.nextCell(getParentStudyName());
        answer.nextRow();

        String siteName = getSiteName();
        if (!siteName.equals("")) {
            answer.nextCell("Site Name");
            answer.nextCell(siteName);
            answer.nextRow();
        }

        answer.nextCell("Subjects");
        answer.nextCell(Integer.toString(getNumSubjects()));
        answer.nextRow();

        int numSEDs = getNumSEDs();
        answer.nextCell("Study Event Definitions");
        answer.nextCell(String.valueOf(numSEDs));
        answer.nextRow();

        for (int i = 1; i <= numSEDs; i++) {
            String repeating = getSEDIsRepeating(i) ? " (Repeating) " : "";
            answer.nextCell("Study Event Definition " + i + repeating);
            answer.nextCell(getSEDName(i));
            answer.nextRow();

            int numSEDCRFs = getSEDNumCRFs(i);
            for (int j = 1; j <= numSEDCRFs; j++) {
                answer.nextCell("CRF ");
                answer.nextCell(getSEDCRFName(i, j));
                answer.nextCell(getSEDCRFCode(i, j));
                answer.nextRow();
            }
        }
    }

    public void computeReportData(ReportBean answer) {
        answer.nextCell("Subject Event Item Values (Item-CRF-Ordinal)");
        answer.nextRow();

        // ///////////////////
        // //
        // COLUMNS //
        // //
        // ///////////////////
        answer.nextCell("SubjID");
        answer.nextCell("ProtocolID");

        // subject column labels
        // general order: subject info first, then group info, then event info,
        // then CRF info
        if (dataset.isShowSubjectDob()) {
            if (study.getStudyParameterConfig().getCollectDob().equals("2")) {
                answer.nextCell("YOB");
            } else if (study.getStudyParameterConfig().getCollectDob().equals("1")) {
                answer.nextCell("DOB");
            }
        }
        if (dataset.isShowSubjectGender()) {
            answer.nextCell("Gender");
        }
        // TODO add additional labels here
        if (dataset.isShowSubjectStatus()) {
            answer.nextCell("SubjectStatus");
            eventDescriptions.put("SubjectStatus", "Subject Status");
        }

        // TODO set datainfo-settable code here, tbh
        if (dataset.isShowSubjectUniqueIdentifier() && "1".equals(showUniqueId)) {
            answer.nextCell("UniqueID");
            eventDescriptions.put("UniqueID", "Unique ID");
        }

        if (dataset.isShowSubjectSecondaryId()) {
            answer.nextCell("SecondaryID");
            eventDescriptions.put("SecondaryID", "SecondaryID");
        }

        // ///////////////////////////////
        // subject group info here, tbh
        if (dataset.isShowSubjectGroupInformation()) {
            // logger.info("got this far in subject group columns...");
            for (int y = 0; y < studyGroupClasses.size(); y++) {
                // logger.info("found a study group class here: "+y);
                StudyGroupClassBean studyGroupClassBean = (StudyGroupClassBean) studyGroupClasses.get(y);
                answer.nextCell(studyGroupClassBean.getName());
                // logger.info("found the name:
                // "+studyGroupClassBean.getName());
                // YW 10-31-2007, for SPSS, eventHeader probably doesn't match
                // the final header which will be validated
                eventDescriptions.put(studyGroupClassBean.getName(), studyGroupClassBean.getName());
            }
        }

        // ////////////////////
        // sed column labels
        int numSEDs = getNumSEDs();
        for (int i = 1; i <= numSEDs; i++) {
            int numSamples = getSEDNumSamples(i);

            for (int j = 1; j <= numSamples; j++) {
                // ///////////////////////////////
                // ADD ALL EVENT HEADERS HERE, tbh

                if (dataset.isShowEventLocation()) {
                    String location = getColumnLabel(i, j, "Location", numSamples);
                    String description = getColumnDescription(i, j, "Location For ", currentDef.getName(), numSamples);
                    answer.nextCell(location);
                    eventHeaders.add(location);
                    eventDescriptions.put(location, description);
                }
                if (dataset.isShowEventStart()) {
                    String start = getColumnLabel(i, j, "StartDate", numSamples);
                    String description = getColumnDescription(i, j, "Start Date For ", currentDef.getName(), numSamples);
                    answer.nextCell(start);
                    eventHeaders.add(start);
                    eventDescriptions.put(start, description);

                }
                if (dataset.isShowEventEnd()) {
                    String end = getColumnLabel(i, j, "EndDate", numSamples);
                    String description = getColumnDescription(i, j, "End Date For ", currentDef.getName(), numSamples);
                    answer.nextCell(end);
                    eventHeaders.add(end);
                    eventDescriptions.put(end, description);
                }
                if (dataset.isShowEventStatus()) {
                    String eventStatus = getColumnLabel(i, j, "SubjectEventStatus", numSamples);
                    String description = getColumnDescription(i, j, "Event Status For ", currentDef.getName(), numSamples);
                    answer.nextCell(eventStatus);
                    eventHeaders.add(eventStatus);
                    eventDescriptions.put(eventStatus, description);
                }
                if (dataset.isShowSubjectAgeAtEvent()
                        && ("1".equals(study.getStudyParameterConfig().getCollectDob()) || "2".equals(study.getStudyParameterConfig().getCollectDob()))) {
                    String subjectAgeAtEvent = getColumnLabel(i, j, "AgeAtEvent", numSamples);
                    String description = getColumnDescription(i, j, "Age At Event for ", currentDef.getName(), numSamples);
                    answer.nextCell(subjectAgeAtEvent);
                    eventHeaders.add(subjectAgeAtEvent);
                    eventDescriptions.put(subjectAgeAtEvent, description);
                }
            }
        }

        // item-crf-ordinal column labels
        for (int i = 1; i <= numSEDs; i++) {
            int numSamples = getSEDNumSamples(i);
            // no need for this, as we only need to track number of crfs, tbh
            // 03/08
            // int numSEDCRFs = getSEDNumCRFs(i);
            for (int j = 1; j <= numSamples; j++) {
                int numSEDCRFs = getSEDNumCRFs(i);
                if (dataset.isShowCRFcompletionDate()) {
                    // logger.info();
                    String crfCompletionDate = getColumnLabel(i, j, "CompletionDate", numSamples);
                    String description = getColumnDescription(i, j, "Completion Date for ", currentDef.getName(), numSamples);// FIXME
                    // is
                    // this
                    // correct?
                    answer.nextCell(crfCompletionDate);
                    eventHeaders.add(crfCompletionDate);
                    eventDescriptions.put(crfCompletionDate, description);
                }

                if (dataset.isShowCRFinterviewerDate()) {
                    String interviewerDate = getColumnLabel(i, j, "InterviewDate", numSamples);
                    String description = getColumnDescription(i, j, "Interviewed Date for ", currentDef.getName(), numSamples);// FIXME
                    // is
                    // this
                    // correct?
                    answer.nextCell(interviewerDate);
                    eventHeaders.add(interviewerDate);
                    eventDescriptions.put(interviewerDate, description);
                }

                if (dataset.isShowCRFinterviewerName()) {
                    String interviewerName = getColumnLabel(i, j, "InterviewerName", numSamples);
                    String description = getColumnDescription(i, j, "Interviewer Name for ", currentDef.getName(), numSamples);// FIXME
                    // is
                    // this
                    // correct?
                    answer.nextCell(interviewerName);
                    eventHeaders.add(interviewerName);
                    eventDescriptions.put(interviewerName, description);
                }

                if (dataset.isShowCRFstatus()) {
                    String crfStatus = getColumnLabel(i, j, "CRFVersionStatus", numSamples);// numSamples
                    // ?
                    // ?
                    // ?
                    String description = getColumnDescription(i, j, "Event CRF Status for ", currentDef.getName(), numSamples);// FIXME
                    // is
                    // this
                    // correct?
                    answer.nextCell(crfStatus);
                    eventHeaders.add(crfStatus);
                    eventDescriptions.put(crfStatus, description);
                }

                if (dataset.isShowCRFversion()) {
                    String crfCompletionDate = getColumnLabel(i, j, "VersionName", numSamples);
                    String description = getColumnDescription(i, j, "CRF Version Name for ", currentDef.getName(), numSamples);// FIXME
                    // is
                    // this
                    // correct?
                    answer.nextCell(crfCompletionDate);
                    eventHeaders.add(crfCompletionDate);
                    eventDescriptions.put(crfCompletionDate, description);
                }
            }
        }

        for (int i = 1; i <= numSEDs; i++) {
            int numSamples = getSEDNumSamples(i);

            for (int j = 1; j <= numSamples; j++) {
                int numSEDCRFs = getSEDNumCRFs(i);
                for (int k = 1; k <= numSEDCRFs; k++) {
                    // add CRF level descriptions here, tbh

                    int numItems = getNumItems(i, k);
                    for (int l = 1; l <= numItems; l++) {
                        // for (int m = 0; m <= maxItemDataBeanOrdinal; m++) {
                        for (Iterator iter = groupNames.entrySet().iterator(); iter.hasNext();) {
                            java.util.Map.Entry groupEntry = (java.util.Map.Entry) iter.next();
                            String groupName = (String) groupEntry.getKey();

                            logger.info("*** Found a row in groupNames: key " + groupName);
                            // + ", value "+
                            // groupCount);
                            // readded tbh 12-4-2007

                            // int m = groupCount.intValue();
                            // String data = getDataByIndex(i, j, k, l,
                            // numSamples, m, groupName);
                            // if (!groupName.equals(UNGROUPED) ||
                            // !data.equals("")) {
                            // YW 10-30-2007, if an item_id doesn't match a
                            // groupName, it will not be added to item title
                            if (inKeys(i, j, k, l, groupName)) {
                                Integer groupCount = (Integer) groupEntry.getValue();
                                for (int m = 1; m <= groupCount.intValue(); m++) {
                                    answer.nextCell(getColumnItemLabel(i, j, k, l, numSamples, m, groupName));
                                    DisplayItemHeaderBean dih = new DisplayItemHeaderBean();
                                    dih.setItemHeaderName(getColumnItemLabel(i, j, k, l, numSamples, m, groupName));
                                    // ItemBean item = new ItemBean();
                                    dih.setItem(currentItem);
                                    itemNames.add(dih);
                                }
                            }
                        }
                    }
                }
            }
        }

        answer.nextRow();

        // ////////////////
        // //
        // DATA //
        // //
        // ////////////////
        for (int h = 1; h <= getNumSubjects(); h++) {
            DisplayItemDataBean didb = new DisplayItemDataBean();
            String label = getSubjectStudyLabel(h);
            answer.nextCell(label);
            didb.setSubjectName(label);

            String protocolId = getParentProtocolId();
            answer.nextCell(protocolId);
            didb.setStudyLabel(protocolId);

            // ////////////////////////
            // subject column data
            if (dataset.isShowSubjectDob()) {
                if (study.getStudyParameterConfig().getCollectDob().equals("2")) {
                    String yob = getSubjectYearOfBirth(h);
                    answer.nextCell(yob);
                    didb.setSubjectDob(yob);
                } else if (study.getStudyParameterConfig().getCollectDob().equals("1")) {
                    String dob = getSubjectDateOfBirth(h);
                    answer.nextCell(dob);
                    didb.setSubjectDob(dob);
                }
            }
            if (dataset.isShowSubjectGender()) {
                String gender = getSubjectGender(h);
                answer.nextCell(gender);
                didb.setSubjectGender(gender);
            }

            // TODO column headers above, column values here, tbh
            if (dataset.isShowSubjectStatus()) {
                String status = getSubjectStatusName(h);
                answer.nextCell(status);
                didb.setSubjectStatus(status);
            }
            if (dataset.isShowSubjectUniqueIdentifier() && "1".equals(showUniqueId)) {
                String uniqueName = getSubjectUniqueIdentifier(h);
                answer.nextCell(uniqueName);
                didb.setSubjectUniqueId(uniqueName);
            }

            if (dataset.isShowSubjectSecondaryId()) {
                String secondaryId = getSubjectSecondaryId(h);
                answer.nextCell(secondaryId);
                didb.setSubjectSecondaryId(secondaryId);
            }
            if (dataset.isShowSubjectGroupInformation()) {
                ArrayList studyGroupList = new ArrayList();
                studyGroupList = getStudyGroupMap(h);// studyGroupMap =
                // getStudyGroupMap(h);
                // logger.info("+++ picture of study group classes:
                // "+studyGroupClasses.toString());
                // logger.info("+++ picture of study group list:
                // "+studyGroupList);
                // logger.info("+++ picture of study group map:
                // "+studyGroupMap.toString());
                for (int y = 0; y < studyGroupClasses.size(); y++) {
                    StudyGroupClassBean sgcBean = (StudyGroupClassBean) studyGroupClasses.get(y);
                    // if the subject is in the group...
                    // logger.info("iterating through keys:
                    // "+sgcBean.getId());
                    Iterator iter = studyGroupList.iterator();
                    /*
                     * case 0 - no groups assigned - should just have a blank
                     * here
                     */
                    if (!iter.hasNext()) {
                        answer.nextCell("");

                        didb.setGroupName(Integer.valueOf(sgcBean.getId()), "");
                    }
                    /*
                     * case 1 - one or more groups assigned - runs through the
                     * maps and assigns them in rows
                     */
                    while (iter.hasNext()) {
                        studyGroupMap = (HashMap) iter.next();

                        // logger.info("+++ picture of study group map:
                        // "+studyGroupMap.toString());

                        if (studyGroupMap.containsKey(Integer.valueOf(sgcBean.getId()))) {
                            StudyGroupBean groupBean = (StudyGroupBean) studyGroupMap.get(Integer.valueOf(sgcBean.getId()));
                            // logger.info("found a group name in a group
                            // class: "+groupBean.getName());

                            answer.nextCell(groupBean.getName());

                            didb.setGroupName(Integer.valueOf(sgcBean.getId()), groupBean.getName());

                            break;
                            // didb.setGroupName(groupBean.getName());
                            // otherwise we don't enter anything...
                        } else {
                            answer.nextCell("");

                            didb.setGroupName(Integer.valueOf(sgcBean.getId()), "");
                        } // end if
                    } // end while
                } // end for
            } // end if

            // sed column values
            for (int i = 1; i <= numSEDs; i++) {
                int numSamples = getSEDNumSamples(i);

                // add event-specific attributes here, tbh
                for (int j = 1; j <= numSamples; j++) {
                    if (dataset.isShowEventLocation()) {
                        String location = getEventLocation(h, i, j);
                        answer.nextCell(location);
                        didb.getEventValues().add(location);

                    }
                    if (dataset.isShowEventStart()) {
                        String start = getEventStart(h, i, j);
                        answer.nextCell(start);
                        didb.getEventValues().add(start);
                    }
                    if (dataset.isShowEventEnd()) {
                        String end = getEventEnd(h, i, j);
                        answer.nextCell(end);
                        didb.getEventValues().add(end);
                    }
                    if (dataset.isShowEventStatus()) {
                        String status = getEventStatus(h, i, j);
                        answer.nextCell(status);
                        didb.getEventValues().add(status);
                    }
                    if (dataset.isShowSubjectAgeAtEvent()
                            && ("1".equals(study.getStudyParameterConfig().getCollectDob()) || "2".equals(study.getStudyParameterConfig().getCollectDob()))) {
                        String ageAtEvent = currentSubject.getDateOfBirth() != null ? getAgeAtEvent(h, i, j) : "";
                        answer.nextCell(ageAtEvent);
                        didb.getEventValues().add(ageAtEvent);
                    }
                }
            }

            // item-crf-ordinal column labels
            for (int i = 1; i <= numSEDs; i++) {

                int numSamples = getSEDNumSamples(i);

                for (int j = 1; j <= numSamples; j++) {

                    if (dataset.isShowCRFcompletionDate()) {
                        String completionDate = getCRFCompletionDate(h, i, j);
                        answer.nextCell(completionDate);
                        didb.getEventValues().add(completionDate);
                    }

                    if (dataset.isShowCRFinterviewerDate()) {
                        String interviewerDate = getCRFInterviewerDate(h, i, j);
                        answer.nextCell(interviewerDate);
                        didb.getEventValues().add(interviewerDate);
                    }

                    if (dataset.isShowCRFinterviewerName()) {
                        String interviewerName = getCRFInterviewerName(h, i, j);
                        answer.nextCell(interviewerName);
                        didb.getEventValues().add(interviewerName);

                    }

                    if (dataset.isShowCRFstatus()) {

                        String crfStatus = getSEDCRFStatus(h, i, j);
                        answer.nextCell(crfStatus);
                        didb.getEventValues().add(crfStatus);
                    }

                    if (dataset.isShowCRFversion()) {
                        String crfVersion = getSEDCRFVersionName(h, i, j);
                        answer.nextCell(crfVersion);
                        didb.getEventValues().add(crfVersion);

                    }
                } // end j
            }

            for (int i = 1; i <= numSEDs; i++) {
                int numSamples = getSEDNumSamples(i);
                // restart j so that text reports match tbh 03/08
                for (int j = 1; j <= numSamples; j++) {
                    int numSEDCRFs = getSEDNumCRFs(i);
                    for (int k = 1; k <= numSEDCRFs; k++) {
                        // add CRF-level column data here, tbh

                        int numItems = getNumItems(i, k);
                        for (int l = 1; l <= numItems; l++) {
                            // adding the extra loop here for repeating items in
                            // extract data, tbh
                            // for (int m = 0; m <= maxItemDataBeanOrdinal; m++)
                            // {
                            for (java.util.Iterator iter = groupNames.entrySet().iterator(); iter.hasNext();) {
                                java.util.Map.Entry groupEntry = (java.util.Map.Entry) iter.next();
                                String groupName = (String) groupEntry.getKey();
                                if (inKeys(i, j, k, l, groupName)) {
                                    // logger.info("found group name at line
                                    // 703: " + groupName);
                                    Integer groupCount = (Integer) groupEntry.getValue();
                                    // logger.info("found groupCount of " +
                                    // groupCount);
                                    for (int m = 1; m <= groupCount.intValue(); m++) {
                                        String data = getDataByIndex(h, i, j, k, l, m, groupName);
                                        // a guard clause here to take care of
                                        // empties...
                                        // if (!data.equals("") ||
                                        // !groupName.equals(UNGROUPED)) {
                                        answer.nextCell(data);
                                        didb.getItemValues().add(data);
                                    }
                                }
                                // }
                                // removing guard clause for now, tbh
                                // and the column code above should look about
                                // the same, tbh
                            }

                        }
                    }
                }
            }
            rowValues.add(didb);
            answer.nextRow();
        }
    }

    public void computeReport(ReportBean answer) {
        computeReportMetadata(answer);
        answer.closeMetadata();
        computeReportData(answer);
    }

    private HashMap displayed = new HashMap();

    // keys are Strings returned by getColumnKeys, values are ArrayLists of
    // ItemBean objects in order of their display in the SED/CRF
    private HashMap sedCrfColumns = new HashMap();

    private HashMap sedCrfItemFormMetadataBeans = new HashMap();

    /**
     * Implements the Column algorithm in "Dataset Export Algorithms" Must be
     * called after DatasetDAO.getDatasetData();
     */
    public void getMetadata() {
        StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(ds);
        CRFDAO cdao = new CRFDAO(ds);
        CRFVersionDAO cvdao = new CRFVersionDAO(ds);
        ItemDAO idao = new ItemDAO(ds);
        ItemFormMetadataDAO ifmDAO = new ItemFormMetadataDAO(this.ds);
        StudyGroupDAO studygroupDAO = new StudyGroupDAO(ds);
        StudyGroupClassDAO studygroupclassDAO = new StudyGroupClassDAO(ds);
        // SubjectGroupMapDAO subjectGroupMapDAO = new SubjectGroupMapDAO(ds);
        studyGroupClasses = new ArrayList();
        studyGroupMap = new HashMap();
        studyGroupMaps = new HashMap<Integer, ArrayList>();
        sedCrfColumns = new HashMap();
        displayed = new HashMap();
        sedCrfItemFormMetadataBeans = new HashMap();

        studyEvents = seddao.findAllByStudy(study);
        ArrayList finalStudyEvents = new ArrayList();
        // set up group classes first, tbh
        // this bit of code throws an error b/c we try to access
        // currentSubject...

        if (dataset.isShowSubjectGroupInformation()) {
            // logger.info("found study id for maps: "+study.getId());
            studyGroupMaps = studygroupDAO.findSubjectGroupMaps(study.getId());
            // below is for a given subject; we need a data structure for
            // all subjects
            // studyGroupMap = studygroupDAO.findByStudySubject(currentSubject);
            // problem: can't use currentSubject here, since it's not 'set up'
            // properly
            // how to get the current subject?

            logger.info("found subject group ids: " + dataset.getSubjectGroupIds().toString());
            // studyGroupClasses = dataset.getSubjectGroupIds();
            for (int h = 0; h < dataset.getSubjectGroupIds().size(); h++) {
                Integer groupId = (Integer) dataset.getSubjectGroupIds().get(h);

                StudyGroupClassBean sgclass = (StudyGroupClassBean) studygroupclassDAO.findByPK(groupId.intValue());
                // logger.info();
                // hmm how to link groups to subjects though? only through
                // subject group map
                logger.info("found a studygroupclass bean: " + sgclass.getName());
                studyGroupClasses.add(sgclass);
            }
        }
        for (int i = 0; i < studyEvents.size(); i++) {
            StudyEventDefinitionBean sed = (StudyEventDefinitionBean) studyEvents.get(i);

            if (!selectedSED(sed)) {
                continue;
            }
            ArrayList CRFs = (ArrayList) cdao.findAllActiveByDefinition(sed);
            ArrayList CRFsDisplayedInThisSED = new ArrayList();

            for (int j = 0; j < CRFs.size(); j++) {
                CRFBean cb = (CRFBean) CRFs.get(j);

                if (!selectedSEDCRF(sed, cb)) {
                    continue;
                } else {

                    CRFsDisplayedInThisSED.add(cb);

                    ArrayList CRFVersions = cvdao.findAllByCRFId(cb.getId());
                    for (int k = 0; k < CRFVersions.size(); k++) {
                        CRFVersionBean cvb = (CRFVersionBean) CRFVersions.get(k);

                        ArrayList Items = idao.findAllItemsByVersionId(cvb.getId());
                        // sort by ordinal/name
                        Collections.sort(Items);
                        for (int l = 0; l < Items.size(); l++) {
                            ItemBean ib = (ItemBean) Items.get(l);
                            if (selected(ib) && !getDisplayed(sed, cb, ib)) {
                                // logger.info("found at
                                // itemformmetadatadao: "+ib.getId()+",
                                // "+cvb.getId());
                                ItemFormMetadataBean ifmb = ifmDAO.findByItemIdAndCRFVersionId(ib.getId(), cvb.getId());
                                addColumn(sed, cb, ib);
                                addItemFormMetadataBeans(sed, cb, ifmb);
                                markDisplayed(sed, cb, ib);
                            }
                        }
                    }
                } // else
            } // for

            sed.setCrfs(CRFsDisplayedInThisSED);
            finalStudyEvents.add(sed); // make the setCrfs call "stick"
        }
        this.studyEvents = finalStudyEvents;
    }

    protected boolean selected(ItemBean ib) {
        return selectedItems.containsKey(Integer.valueOf(ib.getId()));
    }

    protected boolean selectedSEDCRF(StudyEventDefinitionBean sed, CRFBean cb) {
        return selectedSEDCRFs.containsKey(sed.getId() + "_" + cb.getId());
    }

    protected boolean selectedSED(StudyEventDefinitionBean sed) {
        return selectedSEDs.containsKey(Integer.valueOf(sed.getId()));
    }

    private void markDisplayed(StudyEventDefinitionBean sed, CRFBean cb, ItemBean ib) {
        displayed.put(getDisplayedKey(sed, cb, ib), Boolean.TRUE);
    }

    private boolean getDisplayed(StudyEventDefinitionBean sed, CRFBean cb, ItemBean ib) {
        return displayed.containsKey(getDisplayedKey(sed, cb, ib));
    }

    private void addColumn(StudyEventDefinitionBean sed, CRFBean cb, ItemBean ib) {
        String key = getColumnsKey(sed, cb);
        ArrayList columns = (ArrayList) sedCrfColumns.get(key);

        if (columns == null) {
            columns = new ArrayList();
        }

        columns.add(ib);
        sedCrfColumns.put(key, columns);
    }

    public ArrayList getColumns(StudyEventDefinitionBean sed, CRFBean cb) {
        String key = getColumnsKey(sed, cb);
        ArrayList columns = (ArrayList) sedCrfColumns.get(key);

        if (columns == null) {
            columns = new ArrayList();
        }

        return columns;
    }

    private void addItemFormMetadataBeans(StudyEventDefinitionBean sed, CRFBean cb, ItemFormMetadataBean ifmb) {
        String key = sed.getId() + "_" + cb.getId();
        ArrayList columns = (ArrayList) sedCrfItemFormMetadataBeans.get(key);

        if (columns == null) {
            columns = new ArrayList();
        }

        columns.add(ifmb);
        sedCrfItemFormMetadataBeans.put(key, columns);
    }

    public ArrayList getItemFormMetadataBeans(StudyEventDefinitionBean sed, CRFBean cb) {
        String key = sed.getId() + "_" + cb.getId();
        ArrayList columns = (ArrayList) sedCrfItemFormMetadataBeans.get(key);

        if (columns == null) {
            columns = new ArrayList();
        }

        return columns;
    }

    /**
     * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION replaced the old one with
     *      a new function
     */
    public void addStudySubjectData(ArrayList objs) {
        for (int i = 0; i < objs.size(); i++) {
            StudySubjectBean sub = new StudySubjectBean();
            sub = (StudySubjectBean) objs.get(i);
            subjects.add(sub);
        } // for
    }// addStudySubjectData

    /*
     * public void addStudySubjectData(Integer studySubjectId, String
     * studySubjectLabel, Date dateOfBirth, String gender, Integer
     * subjectStatusId, Boolean dobCollected, String uniqueIdentifier, String
     * subjectSecondaryLabel) { if (!subjectsAdded.containsKey(studySubjectId))
     * { StudySubjectBean sub = new StudySubjectBean();
     * sub.setId(studySubjectId.intValue()); sub.setLabel(studySubjectLabel);
     *
     * sub.setDateOfBirth(dateOfBirth); if (gender != null && gender.length() >
     * 0) { sub.setGender(gender.charAt(0)); } else { sub.setGender(' '); }
     * sub.setStatus(Status.get(subjectStatusId.intValue())); //
     * sub.setSecondaryLabel(secondaryID);//????
     * sub.setUniqueIdentifier(uniqueIdentifier);
     * sub.setSecondaryLabel(subjectSecondaryLabel); //
     * sub.setEnrollmentDate(enrollmentDate); // TODO need to find enrollment
     * date, later, tbh subjects.add(sub); subjectsAdded.put(studySubjectId,
     * Boolean.TRUE); } }
     */

    /**
     * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION Combines the two HashMaps
     *      into eventData entries - the data is already filtered for null
     *      values
     */
    public void addStudyEventData() {

        /**
         * The two ArrayList are synchronized because they are extracted with
         * "ORDER BY aitem_data_id"
         */
        boolean isfnd = false;

        // initialize
        eventData = new HashMap();

        for (int ik = 0; ik < aBASE_ITEMDATAID.size(); ik++) {
            // get the item_group side
            extractDataset_ITEMGROUPSIDE objgrp = (extractDataset_ITEMGROUPSIDE) hBASE_ITEMGROUPSIDE.get(ik);
            extractDataset_EVENTSIDE objev = (extractDataset_EVENTSIDE) hBASE_EVENTSIDE.get(ik);

            // sanity check - assume both are not null
            Integer itemdataid = (Integer) aBASE_ITEMDATAID.get(ik);
            Integer itemdataid_objgrp = objgrp.itemDataId;
            Integer itemdataid_objev = objev.itemDataId;
            if (itemdataid_objgrp.intValue() == itemdataid.intValue() && itemdataid_objev.intValue() == itemdataid.intValue()) {
                // OK - add entries to the dataEvent

                // initialize
                StudyEventBean event = new StudyEventBean();
                EventCRFBean eventCRF = new EventCRFBean();

                //
                event.setName(objev.studyEventDefinitionName); // studyEventDefinitionName
                event.setDateStarted(objev.studyEventDateStart); // studyEventStart
                event.setDateEnded(objev.studyEventDateEnd); // studyEventEnd
                event.setLocation(objev.studyEventLoacation); // studyEventLocation
                event.setSampleOrdinal(objev.sampleOrdinal.intValue()); // sampleOrdinal
                // .
                // intValue
                // ()
                event.setStudyEventDefinitionId(objev.studyEvenetDefinitionId.intValue()); // studyEventDefinitionId
                // .
                // intValue
                // (
                // )
                event.setStudySubjectId(objev.studySubjectId.intValue()); // studySubjectId
                // .
                // intValue
                // ()
                event.setStartTimeFlag(objev.studyEventStartTimeFlag.booleanValue()); // se
                // .
                // getStartTimeFlag
                // (
                // )
                event.setEndTimeFlag(objev.studyEventEndTimeFlag.booleanValue()); // se
                // .
                // getEndTimeFlag
                // (
                // )
                // below needs to be added, tbh
                // TODO - @vbc - validate this
                event.setStatus(Status.get(objev.studyEventStatusId.intValue())); // se
                // .
                // getStatus
                // (
                // )
                event.setSubjectEventStatus(SubjectEventStatus.get(objev.studyEventSubjectEventStatusId.intValue())); // se
                // .
                // getSubjectEventStatus
                // (
                // )

                // @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION
                // the stage is set on setStatus - skip it
                // event.setStage(se.getStage());

                event.setId(objev.studyEventId.intValue()); // se.getId()
                // end tbh, 03/08
                eventCRF.setCompletionStatusId(objgrp.eventCrfCompletionStatusId);// completionStatusId
                // .
                // intValue
                // (
                // )
                eventCRF.setInterviewerName(objgrp.interviewerName); // interviewerName
                eventCRF.setDateCompleted(objgrp.eventCrfDateCompleted); // dateCompleted
                eventCRF.setDateValidateCompleted(objgrp.eventCrfDateValidateCompleted); // dateValidateCompleted
                eventCRF.setStatus(Status.get(objgrp.eventCrfStatusId));
                // eventCRF.setCreatedDate();//same as interviewed date? NO
                eventCRF.setDateInterviewed(objgrp.dateInterviewed); // dateInterviewedv
                // eventCRF.setStatus(status); //this is the one that we want,
                // tbh

                CRFVersionBean crfVersion = new CRFVersionBean();
                crfVersion.setName(objgrp.crfVersionName); // crfVersionName
                crfVersion.setStatus(Status.get(objgrp.crfVersionStatusId.intValue())); // crfVersionStatusId
                crfVersion.setStatusId(objgrp.crfVersionStatusId.intValue()); // crfVersionStatusId
                // .
                // intValue
                // (
                // )

                eventCRF.setCrfVersion(crfVersion);

                ArrayList events = new ArrayList();
                events.add(eventCRF);
                // logger.info("///adding an event CRF..."
                // + eventCRF.getInterviewerName());
                event.setEventCRFs(events);// hmm, one to one relationship?
                // guard clause to see if it's in there already?
                // not rly, the above is only used in auditlogging
                // could fit in crf and crf version ids here, though
                // FIXME def not one to one relationship, tbh, 03.08
                String key = getStudyEventDataKey(/* studySubjectId.intValue() */objev.studySubjectId.intValue(),
                        /* studyEventDefinitionId.intValue() */objev.studyEvenetDefinitionId.intValue(),
                        /* sampleOrdinal.intValue()) */objev.sampleOrdinal.intValue());

                if (eventData == null) {
                    eventData = new HashMap();
                }
                StudyEventBean checkEvent = (StudyEventBean) eventData.get(key);
                // debug(event);

                if (checkEvent == null) {
                    eventData.put(key, event);
                    // logger.info("###just CREATED key: " + key + " event: " +
                    // event.getName() + " int.name: " +
                    // eventCRF.getInterviewerName());
                } else {
                    // OK - already saved
                }

            } else {
                isfnd = true;
            } //
        } // for

        // before return quick count
        logger.debug("Number of entries in the eventData is:" + eventData.size());

    }// addStudyEventData

    /*
     * addStudyEventData, a function which puts information about a
     * study-subject taking an event-crf into the ExtractBean's interface.
     */

    public void addStudyEventDataOld(Integer studySubjectId, String studyEventDefinitionName, Integer studyEventDefinitionId, Integer sampleOrdinal,
            String studyEventLocation, Date studyEventStart, Date studyEventEnd, String crfVersionName, Integer crfVersionStatusId, Date dateInterviewed,
            String interviewerName, Date dateCompleted, Date dateValidateCompleted, Integer completionStatusId) {
        // Integer statusId) {

        if (studySubjectId == null || studyEventDefinitionId == null || sampleOrdinal == null || studyEventLocation == null || studyEventStart == null) {
            return;
        }

        if (studyEventDefinitionId.intValue() <= 0 || studySubjectId.intValue() <= 0 || sampleOrdinal.intValue() <= 0) {
            return;
        }
        // YW 08-21-2007 << fetch start_time_flag and end_time_flag
        StudyEventDAO sedao = new StudyEventDAO(ds);
        StudyEventBean se = (StudyEventBean) sedao.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectId, studyEventDefinitionId, sampleOrdinal);
        // YW >>
        if (se == null) {
            se.setStatus(Status.INVALID);
        }
        StudyEventBean event = new StudyEventBean();
        EventCRFBean eventCRF = new EventCRFBean();

        event.setName(studyEventDefinitionName);
        event.setDateStarted(studyEventStart);
        event.setDateEnded(studyEventEnd);
        event.setLocation(studyEventLocation);
        event.setSampleOrdinal(sampleOrdinal.intValue());
        event.setStudyEventDefinitionId(studyEventDefinitionId.intValue());
        event.setStudySubjectId(studySubjectId.intValue());
        event.setStartTimeFlag(se.getStartTimeFlag());
        event.setEndTimeFlag(se.getEndTimeFlag());
        // below needs to be added, tbh
        event.setStatus(se.getStatus());
        event.setSubjectEventStatus(se.getSubjectEventStatus());

        event.setStage(se.getStage());
        logger.info("found stage: " + se.getStage().getName());
        event.setId(se.getId());
        // end tbh, 03/08
        eventCRF.setCompletionStatusId(completionStatusId.intValue());//
        eventCRF.setInterviewerName(interviewerName);
        eventCRF.setDateCompleted(dateCompleted);
        eventCRF.setDateValidateCompleted(dateValidateCompleted);
        // eventCRF.setCreatedDate();//same as interviewed date? NO
        eventCRF.setDateInterviewed(dateInterviewed);
        // eventCRF.setStatus(status); //this is the one that we want, tbh

        // EventCRFDAO ecrfdao = new EventCRFDAO(ds);
        // ArrayList events = ecrfdao.findAllByStudyEvent(se);

        CRFVersionBean crfVersion = new CRFVersionBean();
        crfVersion.setName(crfVersionName);
        crfVersion.setStatus(Status.get(crfVersionStatusId.intValue()));
        crfVersion.setStatusId(crfVersionStatusId.intValue());

        eventCRF.setCrfVersion(crfVersion);
        // logger.info();
        ArrayList events = new ArrayList();
        events.add(eventCRF);
        // logger.info("///adding an event CRF..."
        // + eventCRF.getInterviewerName());
        event.setEventCRFs(events);// hmm, one to one relationship?
        // guard clause to see if it's in there already?
        // not rly, the above is only used in auditlogging
        // could fit in crf and crf version ids here, though
        // FIXME def not one to one relationship, tbh, 03.08
        String key = getStudyEventDataKey(studySubjectId.intValue(), studyEventDefinitionId.intValue(), sampleOrdinal.intValue());
        if (eventData == null) {
            eventData = new HashMap();
        }
        StudyEventBean checkEvent = (StudyEventBean) eventData.get(key);
        // debug(event);

        if (checkEvent == null) {
            eventData.put(key, event);
            logger.info("###just CREATED key: " + key + " event: " + event.getName() + " int.name: " + eventCRF.getInterviewerName());
        } else {
            // ArrayList eventCRFs = checkEvent.getEventCRFs();
            // eventCRFs.add(eventCRF);
            // checkEvent.setEventCRFs(eventCRFs);
            // eventData.put(key, checkEvent);
            // logger.info("count of eventcrfs "+eventCRFs.size());
            // logger.info("other things about the event crf: int.name
            // "+eventCRF.getInterviewerName()+" comp status id
            // "+eventCRF.getCompletionStatusId()+" version name
            // "+eventCRF.getCrfVersion().getName());
            // logger.info("###just UPDATED key: "+key+" event:
            // "+event.getName()+" int.name: "+eventCRF.getInterviewerName());
        }
        // the problem: we want to order by start date
        // but hashmaps are by their very nature hard to order
        // and there is no contigous start date that we can sort on, i.e. we
        // only
        // look at one at a time.
    }

    /*
     * addStudyGroupData: each subject can have more than one Group, so we need
     * to create a list for each subject and check if the groupclasses are
     * there, and which group it is. tbh
     */
    // public void addSubjectGroupData(Integer subjectGroupId,
    // Integer subjectGroupClassId) {
    // // DO NOT USE -- using another method, tbh
    // }
    /**
     * debug: takes in a event crf bean and spits out all its data. tbh
     */
    public void debug(StudyEventBean seb) {
        java.lang.StringBuffer buf = new java.lang.StringBuffer();
        buf.append("***** ***** *****\n");

        buf.append("event crf count: " + seb.getEventCRFs().size() + " ");
        buf.append("study event bean location: " + seb.getLocation() + " ");
        buf.append("study event def id: " + seb.getStudyEventDefinitionId() + " ");
        buf.append("study Event Start Date: " + seb.getDateStarted() != null ? seb.getDateStarted() : "" + " ");
        buf.append("study event date ended: " + seb.getDateEnded() + " ");
        buf.append("study event status: " + seb.getStatus().getName() + " ");
        buf.append("***** ***** *****\n");
        logger.info(buf.toString());
        for (int i = 0; i < seb.getEventCRFs().size(); i++) {
            EventCRFBean check = (EventCRFBean) seb.getEventCRFs().get(i);

            debug(check);
        }
    }

    public void debug(EventCRFBean checkEvent) {
        java.lang.StringBuffer buf = new java.lang.StringBuffer();
        buf.append("****************\n");
        buf.append("debug of event crf bean: id " + checkEvent.getId() + " ");
        buf.append("crf int name: " + checkEvent.getInterviewerName() + " ");
        buf.append("crf version id: " + checkEvent.getCrfVersion().getId() + " ");
        buf.append("crf version name: " + checkEvent.getCrfVersion().getName() + " ");
        buf.append("interview date: " + checkEvent.getCreatedDate() + " ");
        buf.append("status: " + checkEvent.getStatus().getName() + " ");
        buf.append("crf version status: " + checkEvent.getCrfVersion().getStatus().getName() + " ");
        buf.append("completion status id: " + checkEvent.getCompletionStatusId() + " ");
        buf.append("data entry stage: " + checkEvent.getStage().getName() + " ");
        logger.info(buf.toString());
    }

    /*
     * addGroupName -- check to see if this group name is in the system, if it
     * is not, add it together with its ordinal If it is already in the system,
     * look at the ordinals and find out which is bigger, then add the bigger of
     * the two back into the data structure, tbh
     */
    public void addGroupName(String name, Integer ordinal) {
        if (name == null) {
            return;
        }

        if (!groupNames.containsKey(name)) {
            groupNames.put(name, ordinal);
        } else {
            Integer numTimes = (Integer) groupNames.get(name);

            if (numTimes > ordinal) {
                groupNames.put(name, numTimes);
            } else {
                groupNames.put(name, ordinal);
            }
        }
    }

    /**
     *
     */
    public void addItemData() {
        /**
         * The two ArrayList are synchronized because they are extracted with
         * "ORDER BY aitem_data_id"
         */
        boolean isfnd = false;

        // initialize
        data = new HashMap();

        for (int ik = 0; ik < aBASE_ITEMDATAID.size(); ik++) {
            // get the item_group side
            extractDataset_ITEMGROUPSIDE objgrp = (extractDataset_ITEMGROUPSIDE) hBASE_ITEMGROUPSIDE.get(ik);
            extractDataset_EVENTSIDE objev = (extractDataset_EVENTSIDE) hBASE_EVENTSIDE.get(ik);

            // sanity check - assume both are not null
            Integer itemdataid = (Integer) aBASE_ITEMDATAID.get(ik);
            Integer itemdataid_objgrp = objgrp.itemDataId;
            Integer itemdataid_objev = objev.itemDataId;
            if (itemdataid_objgrp.intValue() == itemdataid.intValue() && itemdataid_objev.intValue() == itemdataid.intValue()) {
                // OK - add entries to the dataEvent

                if (!"".equals(/* itemGroupName */objgrp.itemGroupName)) {
                    // logger.info("got inside the loop!");
                    // "i do not think that word means what you think it
                    // means..."
                    // that is to say, i think we are confusing sampleOrdinal
                    // and itemDataOrdinal when we count the number of CRFs in a
                    // given
                    // study event definition
                    // logging here to test this out, tbh 03/08

                    // logger.info("sample ordinal: " +
                    // sampleOrdinal.toString());
                    // logger.info("item data ordinal: " +
                    // itemDataOrdinal.toString());

                    // the mistake here is that we are taking the number of CRFs
                    // from
                    // this maxOrdinals map downstream, and that is wrong. We
                    // need to get
                    // this number
                    // from another source. tbh 03/08

                    /**
                     * @vbc NOTE: the "item_data_ordinal" is the id.ordinal AS
                     *      item_data_ordinal which is the item_data table. Here
                     *      we retrieve the item_group_metadata.repeat_number
                     *
                     *      TODO - validate the logic
                     */
                    String key = getDataKey(/* studySubjectId.intValue() */objev.studySubjectId.intValue(),
                            /* studyEventDefinitionId.intValue() */objev.studyEvenetDefinitionId.intValue(),
                            /* sampleOrdinal.intValue() */objev.sampleOrdinal.intValue(), /* crfId.intValue() */objgrp.crfid.intValue(),
                            /* itemId.intValue() */objgrp.itemId.intValue(), /* itemDataOrdinal.intValue() */objgrp.itemGroupRepeatNumber.intValue(),
                            /* groupName */objgrp.itemGroupName);

                    data.put(key, objgrp.itemValue/* itemValue */);
                    // logger.info("*** just put in data for " + key + " and
                    // value " + objgrp.itemValue/*itemValue*/);
                    // groups.put(key, itemGroupName);
                    int maxOrdinal = getMaxOrdinal(/*
                                                    * studyEventDefinitionId.intValue
                                                    * ()
                                                    */objev.studyEvenetDefinitionId.intValue());
                    if (maxOrdinal < objev.sampleOrdinal.intValue()) { // /*
                        // sampleOrdinal
                        // .
                        // intValue
                        // ()*/
                        setMaxOrdinal(objev.studyEvenetDefinitionId.intValue(), objev.sampleOrdinal.intValue());
                    } // if

                    selectedItems.put(objgrp.itemId /* itemId */, Boolean.TRUE);
                    selectedSEDCRFs.put(objev.studyEvenetDefinitionId.intValue() + "_" + objgrp.crfid.intValue()
                    /*
                     * studyEventDefinitionId.intValue() + "_" +
                     * crfId.intValue()
                     */, Boolean.TRUE);
                    selectedSEDs.put(objev.studyEvenetDefinitionId /* studyEventDefinitionId */, Boolean.TRUE);

                    // TODO - see comment above
                    if ( /* itemDataOrdinal.intValue() */objgrp.itemGroupRepeatNumber.intValue() > getMaxItemDataBeanOrdinal()) {
                        setMaxItemDataBeanOrdinal(objgrp.itemGroupRepeatNumber.intValue()/*
                                                                                          * itemDataOrdinal.
                                                                                          * intValue
                                                                                          * (
                                                                                          * )
                                                                                          */);
                        // logger.info("### just updated max ordinal for
                        // itemdatabean: "+itemDataOrdinal.intValue());
                    }
                }
                addGroupName(/* itemGroupName_temp, itemDataOrdinal */objgrp.itemGroupName, objgrp.itemGroupRepeatNumber);
            } else {
                // ERROR - not match
            } // if
        } // for

    }

    /*
     * addItemData -- we create a key out of a combination of variables, and
     * then put the data in a hashmap with the key.
     */
    public void addItemDataOld(Integer studySubjectId, Integer studyEventDefinitionId, Integer sampleOrdinal, // study
            // event
            // sample
            // ordinal
            Integer crfId, Integer itemId, String itemValue, Integer itemDataOrdinal, // item
            // data
            // ordinal,
            // having
            // to
            // do
            // specifically with repeating items
            String groupName) {// item group name, having to do with the group
        // name in the database
        // String itemGroupName) {
        if (studyEventDefinitionId == null || studySubjectId == null || crfId == null || itemId == null || sampleOrdinal == null || itemValue == null) {
            return;
        }

        if (studyEventDefinitionId.intValue() <= 0 || studySubjectId.intValue() <= 0 || crfId.intValue() <= 0 || itemId.intValue() <= 0
                || sampleOrdinal.intValue() <= 0) {
            return;
        }

        // "i do not think that word means what you think it means..."
        // that is to say, i think we are confusing sampleOrdinal
        // and itemDataOrdinal when we count the number of CRFs in a given
        // study event definition
        // logging here to test this out, tbh 03/08

        logger.info("sample ordinal: " + sampleOrdinal.toString());
        logger.info("item data ordinal: " + itemDataOrdinal.toString());

        // the mistake here is that we are taking the number of CRFs from
        // this maxOrdinals map downstream, and that is wrong. We need to get
        // this number
        // from another source. tbh 03/08

        String key = getDataKey(studySubjectId.intValue(), studyEventDefinitionId.intValue(), sampleOrdinal.intValue(), crfId.intValue(), itemId.intValue(),
                itemDataOrdinal.intValue(), groupName);

        data.put(key, itemValue);
        logger.info("*** just put in data for " + key + " and value " + itemValue);
        // groups.put(key, itemGroupName);
        int maxOrdinal = getMaxOrdinal(studyEventDefinitionId.intValue());
        if (maxOrdinal < sampleOrdinal.intValue()) {
            setMaxOrdinal(studyEventDefinitionId.intValue(), sampleOrdinal.intValue());
        }
        selectedItems.put(itemId, Boolean.TRUE);
        selectedSEDCRFs.put(studyEventDefinitionId.intValue() + "_" + crfId.intValue(), Boolean.TRUE);
        selectedSEDs.put(studyEventDefinitionId, Boolean.TRUE);

        return;
    }

    protected String getDataByIndex(int subjectInd, int sedInd, int sampleOrdinal, int crfInd, int itemInd, int itemOrdinal, String groupName) {
        syncSubjectIndex(subjectInd);
        syncItemIndex(sedInd, crfInd, itemInd);
        String key = getDataKey(currentSubject.getId(), currentDef.getId(), sampleOrdinal, currentCRF.getId(), currentItem.getId(), itemOrdinal, groupName);
        String itemValue = (String) data.get(key);

        if (itemValue == null) {
            itemValue = "";
        }

        return itemValue;
    }

    // //////////////////////////
    // Max Ordinals Section //
    // //////////////////////////

    private Integer getMaxOrdinalsKey(int studySubjectId) {
        return Integer.valueOf(studySubjectId);
    }

    private int getMaxOrdinal(int studyEventDefinitionId) {
        // logger.info("*** max ordinals: " + maxOrdinals.toString());
        Integer key = getMaxOrdinalsKey(studyEventDefinitionId);
        try {
            if (maxOrdinals.containsKey(key)) {
                Integer maxOrdinal = (Integer) maxOrdinals.get(key);
                if (maxOrdinal != null) {
                    return maxOrdinal.intValue();
                }
            }
        } catch (Exception e) {
        }

        return 0;
    }

    private void setMaxOrdinal(int studyEventDefinitionId, int sampleOrdinal) {
        Integer key = getMaxOrdinalsKey(studyEventDefinitionId);
        // disable logg - slow performance
        // logger.info("*** set max ordinal: " + sampleOrdinal + " for key " +
        // key.toString());
        maxOrdinals.put(key, Integer.valueOf(sampleOrdinal));
    }

    // /////////////////////////////////////////////
    // //
    // RETRIEVE METADATA AND DATA SECTION //
    // //
    // /////////////////////////////////////////////

    public String getParentProtocolId() {
        // updated 11-2007 to support protocol id - site id terminology, tbh
        if (!parentStudy.isActive()) {
            return study.getIdentifier();
        } else {
            return parentStudy.getIdentifier() + "_" + study.getIdentifier();
        }
    }

    public String getParentStudyName() {
        if (!parentStudy.isActive()) {
            return study.getName();
        } else {
            return parentStudy.getName();
        }
    }

    public String getParentStudySummary() {
        if (!parentStudy.isActive()) {
            return study.getSummary();
        } else {
            return parentStudy.getSummary();
        }
    }

    private String getSiteName() {
        if (parentStudy.isActive()) {
            return study.getName();
        } else {
            return "";
        }
    }

    public int getNumSubjects() {
        if (subjects != null) {
            return subjects.size();
        } else {
            return 0;
        }
    }

    protected int getNumSEDs() {
        return studyEvents.size();
    }

    public int getMaxItemDataBeanOrdinal() {
        return maxItemDataBeanOrdinal;
    }

    public void setMaxItemDataBeanOrdinal(int maxItemDataBeanOrdinal) {
        this.maxItemDataBeanOrdinal = maxItemDataBeanOrdinal;
    }

    private void syncSubjectIndex(int ind) {
        if (subjIndex != ind) {
            currentSubject = (StudySubjectBean) subjects.get(ind - 1);
            subjIndex = ind;
        }
    }

    private String getSubjectStudyLabel(int h) {
        syncSubjectIndex(h);

        return currentSubject.getLabel();
    }

    private String getSubjectDateOfBirth(int h) {
        syncSubjectIndex(h);
        Date dob = currentSubject.getDateOfBirth();
        return dob == null ? "" : sdf.format(dob);
    }

    private String getSubjectStatusName(int h) {
        syncSubjectIndex(h);
        Status status = currentSubject.getStatus();
        return status.getName();
    }

    private String getSubjectUniqueIdentifier(int h) {
        syncSubjectIndex(h);
        String uni = currentSubject.getSecondaryLabel();
        uni = currentSubject.getUniqueIdentifier();
        logger.info("+++ comparing " + uni + " vs. secondary label " + currentSubject.getSecondaryLabel());
        return uni;
    }

    private String getSubjectSecondaryId(int h) {
        syncSubjectIndex(h);
        return currentSubject.getSecondaryLabel();
    }

    private ArrayList getStudyGroupMap(int h) {
        syncSubjectIndex(h);
        Integer key = Integer.valueOf(currentSubject.getId());
        ArrayList value = (ArrayList) studyGroupMaps.get(key);
        return value != null ? value : new ArrayList();
    }

    private String getSubjectYearOfBirth(int h) {
        syncSubjectIndex(h);
        Date dob = currentSubject.getDateOfBirth();

        if (dob == null) {
            return "";
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(dob);
        int year = cal.get(Calendar.YEAR);

        return year + "";
    }

    private String getSubjectGender(int h) {
        syncSubjectIndex(h);
        return String.valueOf(currentSubject.getGender());
    }

    private void syncSEDIndex(int ind) {
        if (sedIndex != ind) {
            currentDef = (StudyEventDefinitionBean) studyEvents.get(ind - 1);
            sedIndex = ind;
        }
    }

    private boolean getSEDIsRepeating(int ind) {
        syncSEDIndex(ind);
        return currentDef.isRepeating();
    }

    private String getSEDName(int ind) {
        syncSEDIndex(ind);
        return currentDef.getName();
    }

    protected int getSEDNumCRFs(int ind) {
        syncSEDIndex(ind);
        return currentDef.getCrfs().size();
    }

    // below created to support CRF metadata, tbh
    protected String getCRFStatus(int h, int i, int j) {

        // return crfbean.getStatus().getName();
        // return currentCRF.getStatus().getName();
        StudyEventBean seb = getEvent(h, i, j);

        ArrayList crfbeans = currentDef.getCrfs();
        EventCRFBean eventCRF = null;
        if (seb.getEventCRFs().size() > 0) {
            eventCRF = (EventCRFBean) seb.getEventCRFs().get(0);
        }

        return eventCRF != null ? eventCRF.getStatus().getName() : "";
    }

    protected String getCRFVersionName(int h, int i, int j) {
        // syncCRFIndex(ind, crfv);
        // how to cross ref this with what's being entered???
        // return currentCRF.getVersions()
        StudyEventBean seb = getEvent(h, i, j);
        EventCRFBean eventCRF = null;
        if (seb.getEventCRFs().size() == 1) {
            eventCRF = (EventCRFBean) seb.getEventCRFs().get(0);
        } else {
            eventCRF = (EventCRFBean) seb.getEventCRFs().get(j - 1);
        }

        return eventCRF != null ? eventCRF.getCrfVersion().getName() : "";

    }

    protected String getCRFInterviewerDate(int h, int i, int j) {
        StudyEventBean seb = getEvent(h, i, j);

        EventCRFBean eventCRF = null;
        if (seb.getEventCRFs().size() > 0) {
            eventCRF = (EventCRFBean) seb.getEventCRFs().get(0);
        }

        // Calendar c = Calendar.getInstance();
        // c.setTime(eventCRF.getDateInterviewed());
        return eventCRF != null && eventCRF.getDateInterviewed() != null ? sdf.format(eventCRF.getDateInterviewed()) : "";

    }

    protected String getCRFInterviewerName(int h, int i, int j) {
        StudyEventBean seb = getEvent(h, i, j);

        EventCRFBean eventCRF = null;
        if (seb.getEventCRFs().size() > 0) {
            eventCRF = (EventCRFBean) seb.getEventCRFs().get(0);
        }

        return eventCRF != null ? eventCRF.getInterviewerName() : "";

    }

    protected String getCRFCompletionDate(int h, int i, int j) {
        StudyEventBean seb = getEvent(h, i, j);

        EventCRFBean eventCRF = null;
        if (seb.getEventCRFs().size() > 0) {
            eventCRF = (EventCRFBean) seb.getEventCRFs().get(0);
        }

        return eventCRF.getDateValidateCompleted() == null ? sdf.format(eventCRF.getDateCompleted()) : sdf.format(eventCRF.getDateValidateCompleted());// need
        // to
        // be
        // fixed?

    }

    private String getSEDCRFName(int sedInd, int crfInd) {
        syncCRFIndex(sedInd, crfInd);
        return currentCRF.getName();
    }

    private String getSEDCRFVersionName(int h, int sedInd, int crfInd) {
        // syncCRFIndex(sedInd, crfInd);

        StudyEventBean seb = getEvent(h, sedInd, crfInd);

        EventCRFBean eventCRF = null;
        if (seb.getEventCRFs().size() > 0) {
            eventCRF = (EventCRFBean) seb.getEventCRFs().get(0);
        }

        String returnMe = "";
        // ArrayList versions = currentCRF.getVersions();//returns zero results
        // EventCRFBean eventCRF = null;
        if (seb.getEventCRFs().size() > 0) {
            logger.info("found getEventCRFs.size " + seb.getEventCRFs().size());
            for (int t = 0; t < seb.getEventCRFs().size(); t++) {
                eventCRF = (EventCRFBean) seb.getEventCRFs().get(t);
                returnMe = eventCRF.getCrfVersion().getName();
                // logger.info("found versions.size "+versions.size());
                // for (int s = 0; s < versions.size(); s++) {
                // CRFVersionBean version = (CRFVersionBean)versions.get(s);
                // if (version.getId()==eventCRF.getCRFVersionId()) {
                // returnMe = version.getName();
                // break;
                // }
                // }
            }

        }
        logger.info("returning the following for crf version name: " + returnMe);
        return returnMe;
    }

    /*
     * change this to pull out the correct data entry stage for a given crf
     * version within a study event
     */
    // private String getSEDCRFStatus(int sedInd, int crfInd) {
    // syncCRFIndex(sedInd, crfInd);
    //
    // return currentCRF.getStatus().getName();
    // }
    private void syncCRFIndex(int sedInd, int crfInd) {
        syncSEDIndex(sedInd);
        // debug(currentDef);
        try {
            currentCRF = (CRFBean) currentDef.getCrfs().get(crfInd - 1);
        } catch (IndexOutOfBoundsException e) {
            logger.info("found exception");
            currentCRF = (CRFBean) currentDef.getCrfs().get(0);
        }
        crfIndex = crfInd;

    }

    // private String getSEDCRFName(int sedInd, int crfInd) {
    // syncCRFIndex(sedInd, crfInd);
    // return currentCRF.getName();
    // }

    // private String getSEDCRFVersionName(int h, int sedInd, int crfInd) {
    // syncCRFIndex(sedInd, crfInd);
    //
    // StudyEventBean seb = getEvent(h, sedInd, crfInd);
    //
    // String returnMe = "";
    // //ArrayList versions = currentCRF.getVersions();//returns zero results
    // EventCRFBean eventCRF = null;
    // if (seb.getEventCRFs().size() > 0) {
    // logger.info("found getEventCRFs.size " + seb.getEventCRFs().size());
    // for (int t = 0; t < seb.getEventCRFs().size(); t++) {
    // eventCRF = (EventCRFBean) seb.getEventCRFs().get(t);
    // returnMe = eventCRF.getCrfVersion().getName();
    // //logger.info("found versions.size "+versions.size());
    // //for (int s = 0; s < versions.size(); s++) {
    // // CRFVersionBean version = (CRFVersionBean)versions.get(s);
    // // if (version.getId()==eventCRF.getCRFVersionId()) {
    // // returnMe = version.getName();
    // //break;
    // // }
    // //}
    // }
    //
    // }
    // logger.info("returning the following for crf version name: " + returnMe);
    // return returnMe;
    // }

    private String getSEDCRFStatus(int h, int sedInd, int crfInd) {// BADS Flag
        syncCRFIndex(sedInd, crfInd);

        StudyEventBean seb = getEvent(h, sedInd, crfInd);
        Status ecStatus = Status.AVAILABLE;
        EventCRFBean eventCRF = null;
        if (seb.getEventCRFs().size() > 0) {
            eventCRF = (EventCRFBean) seb.getEventCRFs().get(0);
        }
        String crfVersionStatus = "";
        SubjectEventStatus status = SubjectEventStatus.NOT_SCHEDULED;
        CRFVersionBean crfv = new CRFVersionBean();
        crfv.setStatus(Status.AVAILABLE);
        // modified stage so that crfVersionStatus could be the same as what it
        // shows in subject matrix - as required.
        DataEntryStage stage = DataEntryStage.INVALID;
        try {
            stage = eventCRF.getStage();
            ecStatus = eventCRF.getStatus();
            status = seb.getSubjectEventStatus();// SubjectEventStatus.get(
            // eventCRF
            // .getCompletionStatusId());
            crfv = eventCRF.getCrfVersion();
        } catch (NullPointerException e) {
            logger.info("exception hit, status set to not scheduled");
        }
        // currentCRF.getStatus().getName();
        //
        logger.info("event crf stage: " + stage.getName() + ", event crf status: " + ecStatus.getName() + ", STATUS: " + status.getName() + " crf version: "
                + crfv.getStatus().getName() + " data entry stage: " + stage.getName());

        if (stage.equals(DataEntryStage.INVALID) || ecStatus.equals(Status.INVALID)) {
            stage = DataEntryStage.UNCOMPLETED;
        }
        crfVersionStatus = stage.getName();
        if (status.equals(SubjectEventStatus.LOCKED) || status.equals(SubjectEventStatus.SKIPPED) || status.equals(SubjectEventStatus.STOPPED)) {
            crfVersionStatus = DataEntryStage.LOCKED.getName();
        } else if (status.equals(SubjectEventStatus.INVALID)) {
            crfVersionStatus = DataEntryStage.LOCKED.getName();
        } else if (!currentCRF.getStatus().equals(Status.AVAILABLE)) {
            crfVersionStatus = DataEntryStage.LOCKED.getName();
        } else if (!crfv.getStatus().equals(Status.AVAILABLE)) {
            crfVersionStatus = DataEntryStage.LOCKED.getName();
        }

        logger.info("returning: " + crfVersionStatus);
        return crfVersionStatus;
    }

    protected int getNumItems(int sedInd, int crfInd) {
        syncCRFIndex(sedInd, crfInd);
        ArrayList items = getColumns(currentDef, currentCRF);
        return items.size();
    }

    private void syncItemIndex(int sedInd, int crfInd, int itemInd) {
        syncCRFIndex(sedInd, crfInd);

        ArrayList items = getColumns(currentDef, currentCRF);
        currentItem = (ItemBean) items.get(itemInd - 1);
        itemIndex = itemInd;

    }

    private String getItemName(int sedInd, int crfInd, int itemInd) {
        syncItemIndex(sedInd, crfInd, itemInd);
        return currentItem.getName();
    }

    // //////////////////
    // //
    // HASHMAP KEYS //
    // //
    // //////////////////

    private String getDataKey(int studySubjectId, int studyEventDefinitionId, int sampleOrdinal, int crfId, int itemId, int itemOrdinal, String groupName) {
        String groupString = "";
        if (!groupName.equals(UNGROUPED)) {
            // need to remember that this is hard coded, need to place it
            // outside the code somehow, tbh
            groupString = "_" + groupName + "_" + itemOrdinal;
        }
        return studySubjectId + "_" + studyEventDefinitionId + "_" + sampleOrdinal + "_" + crfId + "_" + itemId + groupString;
    }

    private String getDisplayedKey(StudyEventDefinitionBean sed, CRFBean cb, ItemBean ib) {
        return sed.getId() + "_" + cb.getId() + "_" + ib.getId();
    }

    private String getColumnsKey(StudyEventDefinitionBean sed, CRFBean cb) {
        return sed.getId() + "_" + cb.getId();
    }

    private String getStudyEventDataKey(int studySubjectId, int studyEventDefinitionId, int sampleOrdinal) {
        String key = studySubjectId + "_" + studyEventDefinitionId + "_" + sampleOrdinal;
        // logger.info("found key "+key);
        return key;
    }

    // /////////////////////////////
    // //
    // CODES AND COLUMN LABELS //
    // //
    // /////////////////////////////

    public static String getSEDCode(int sedInd) {
        sedInd--;
        if (sedInd > 26) {
            int digit1 = sedInd / 26;
            int digit2 = sedInd % 26;

            char letter1 = (char) ('A' + digit1);
            char letter2 = (char) ('A' + digit2);

            return "" + letter1 + letter2;
        } else {
            char letter = (char) ('A' + sedInd);

            return "" + letter;
        }
    }

    public static String getSEDCRFCode(int sedInd, int crfInd) {
        return getSEDCode(sedInd) + crfInd;
    }

    private String getSampleCode(int ordinal, int numSamples) {
        return numSamples > 1 ? "_" + ordinal : "";
    }

    private String getColumnLabel(int sedInd, int ordinal, String labelType, int numSamples) {
        return labelType + "_" + getSEDCode(sedInd) + getSampleCode(ordinal, numSamples);
    }

    private String getColumnDescription(int sedInd, int ordinal, String labelType, String defName, int numSamples) {
        return labelType + defName + "(" + getSEDCode(sedInd) + getSampleCode(ordinal, numSamples) + ")";
    }

    private String getColumnItemLabel(int sedInd, int ordinal, int crfInd, int itemInd, int numSamples, int itemDataOrdinal, String groupName) {
        String groupEnd = "";
        if (!groupName.equals(UNGROUPED)) {
            groupEnd = "_" + groupName + "_" + itemDataOrdinal;
        }
        return getItemName(sedInd, crfInd, itemInd) + "_" + getSEDCRFCode(sedInd, crfInd) + getSampleCode(ordinal, numSamples) + groupEnd;// "_"
        // +
        // itemDataOrdinal;
    }

    // YW 10-30-2007
    protected boolean inKeys(int sedInd, int sampleOrdinal, int crfInd, int itemInd, String groupName) {
        syncSEDIndex(sedInd);
        syncCRFIndex(sedInd, crfInd);
        syncItemIndex(sedInd, crfInd, itemInd);

        /**
         * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION change it into a
         *      simple HashMap
         *
         *      TODO - verify if the itemOrdinalItem is required - in the
         *      previous code is set to 1 !?!!?
         */
        String key = currentDef.getId() + "_" + sampleOrdinal + "_" + currentCRF.getId() + "_" + currentItem.getId() + "_" + groupName;
        Boolean issavedkey = (Boolean) getHmInKeys().get(key);
        if (issavedkey == null) {
            // the "True" is not found for key - set to false
            return false;
        } else {
            return true;
        } // if

        /*
         * for (Iterator iter = data.entrySet().iterator(); iter.hasNext();) {
         * String key = (String) ((java.util.Map.Entry) iter.next()).getKey() +
         * "_Ungrouped"; String testKey = getDataKey(0, currentDef.getId(),
         * sampleOrdinal, currentCRF.getId(), currentItem.getId(), 1,
         * groupName).substring(2); testKey = groupName.equals("Ungrouped") ?
         * testKey + "_Ungrouped" : testKey; if (key.contains(testKey)) { return
         * true; } }
         */

    }

    // ////////////////////////////
    // //
    // GETTERS AND SETTERS //
    // //
    // ////////////////////////////

    /**
     * @return Returns the study.
     */
    public StudyBean getStudy() {
        return study;
    }

    /**
     * @param study
     *            The study to set.
     */
    public void setStudy(StudyBean study) {
        this.study = study;
    }

    /**
     * @return Returns the parentStudy.
     */
    public StudyBean getParentStudy() {
        return parentStudy;
    }

    /**
     * @param parentStudy
     *            The parentStudy to set.
     */
    public void setParentStudy(StudyBean parentStudy) {
        this.parentStudy = parentStudy;
    }

    /**
     * @return Returns the format.
     */
    public int getFormat() {
        return format;
    }

    /**
     * @param format
     *            The format to set.
     */
    public void setFormat(int format) {
        this.format = format;
    }

    /**
     * @return Returns the dataset.
     */
    public DatasetBean getDataset() {
        return dataset;
    }

    /**
     * @param dataset
     *            The dataset to set.
     */
    public void setDataset(DatasetBean dataset) {
        this.dataset = dataset;
    }

    /**
     * @return Returns the studyEvents.
     */
    public ArrayList getStudyEvents() {
        return studyEvents;
    }

    /**
     * The maximum over all ordinals over all study events for the provided SED.
     *
     * @param i
     *            An index into the studyEvents list for the SED whose max
     *            ordinal we want.
     * @return The maximum number of samples for the i-th SED.
     */
    public int getSEDNumSamples(int i) {
        syncSEDIndex(i);
        int sedId = currentDef.getId();
        return getMaxOrdinal(sedId);
    }

    /**
     * Get the event correspodning to the provided study subject, SED and sample
     * ordinal.
     *
     * @param h
     *            An index into the array of subjects.
     * @param i
     *            An index into the array of SEDs.
     * @param j
     *            The sample ordinal.
     * @return The event correspodning to the provided study subject, SED and
     *         sample ordinal.
     */
    private StudyEventBean getEvent(int h, int i, int j) {
        syncSubjectIndex(h);
        syncSEDIndex(i);

        String key = getStudyEventDataKey(currentSubject.getId(), currentDef.getId(), j);
        StudyEventBean seb = (StudyEventBean) eventData.get(key);

        if (seb == null) {
            // logger.info("did not find seb, h" + h + " i" + i + " j" + j + "
            // key "+key);
            // logger.info("dump of event data: "+eventData.toString());
            return new StudyEventBean();
        } else {
            // debug(seb);
            // logger.info("FOUND seb, h" + h + " i" + i + " j" + j + " key
            // "+key);
            if (seb.getEventCRFs().size() > 0) {
                EventCRFBean ecbean = (EventCRFBean) seb.getEventCRFs().get(0);
            }
            // logger.info("int.name "+ecbean.getInterviewerName());
            return seb;
        }
    }

    private String getEventLocation(int h, int i, int j) {
        return getEvent(h, i, j).getLocation();
    }

    private String getEventStart(int h, int i, int j) {
        StudyEventBean seb = getEvent(h, i, j);
        Date start = seb.getDateStarted();
        // YW 08-20-2007 for displaying time if appliable
        if (seb.getStartTimeFlag()) {
            // return (start != null ? new SimpleDateFormat(
            // "MM/dd/yyyy hh:mm:ss a").format(start) : "");
            // need to pull date_time_format_string from format.properties here
            // instead, tbh
            return start != null ? long_sdf.format(start) : "";
        } else {
            return start != null ? sdf.format(start) : "";
        }
    }

    private String getEventEnd(int h, int i, int j) {
        StudyEventBean seb = getEvent(h, i, j);
        Date end = seb.getDateEnded();
        // YW 08-20-2007 for displaying time if appliable
        if (seb.getEndTimeFlag()) {
            // return (end != null ? new SimpleDateFormat("MM/dd/yyyy hh:mm:ss
            // a")
            // .format(end) : "");
            return end != null ? long_sdf.format(end) : "";
        } else {
            return end != null ? sdf.format(end) : "";
        }
    }

    private String getEventStatus(int h, int i, int j) {
        StudyEventBean seb = getEvent(h, i, j);

        // logger.info("+++ found study event "+ seb.getName()+ " id "+
        // seb.getId()+ " status "+ seb.getStatus().getName());

        // ArrayList eventCRFs = seb.getEventCRFs();
        // int completionStatusId = 0;
        // for (int x = 0; x < seb.getEventCRFs().size(); x++) {
        // EventCRFBean eventCRF = (EventCRFBean) seb.getEventCRFs().get(x);
        // completionStatusId = eventCRF.getCompletionStatusId();
        // }
        // return (completionStatusId > 0 ?
        // SubjectEventStatus.get(completionStatusId).getName() : "");
        return seb.getSubjectEventStatus().getName();
    }

    private String getAgeAtEvent(int h, int i, int j) {
        // syncSubjectIndex(h);
        StudyEventBean seb = getEvent(h, i, j);
        Date startDate = seb.getDateStarted();
        // to try and avoid NPEs, tbh 092007
        startDate = seb.getDateStarted() != null ? seb.getDateStarted() : new Date();
        Date age = currentSubject.getDateOfBirth();
        String answer = "";
        if (age.before(startDate)) {
            Calendar dateOfBirth = Calendar.getInstance();
            dateOfBirth.setTime(age);// new GregorianCalendar(age);
            Calendar theStartDate = Calendar.getInstance();// new
            // GregorianCalendar(startDate);
            theStartDate.setTime(startDate);
            int theAge = theStartDate.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
            Calendar today = Calendar.getInstance();
            // add the age to the year to see if it's happened yet
            dateOfBirth.add(Calendar.YEAR, theAge);
            // subtract one from the age if the birthday hasn't happened yet
            if (today.before(dateOfBirth)) {
                theAge--;
            }
            answer = "" + theAge;
        } else {
            // ideally should not get here, but we have an 'error' code if it
            // does, tbh
            answer = "-1";
            // logger.info("reached error state for age at event");
        }
        return answer;
    }

    protected ArrayList getSubjects() {
        return this.subjects;
    }

    // public String getItemValue(int h, int i, int j, int k, int l) {
    // StudySubjectBean sub = (StudySubjectBean) subjects.get(h);
    // int studyEventDefinitionId = ((StudyEventDefinitionBean)
    // studyEvents.get(i)).getId();
    // ArrayList events = (ArrayList) eventData.get(new
    // Integer(studyEventDefinitionId));
    // int sampleOrdinal = ((StudyEventBean) events.get(j)).getSampleOrdinal();
    // ArrayList items = getColumns(currentDef, currentCRF);
    //
    // String key = getDataKey(sub.getId(), studyEventDefinitionId,
    // sampleOrdinal,
    // currentCRF.getId(),
    // ((ItemBean) items.get(l)).getId());
    // return (String) data.get(key);
    //
    // }

    /**
     * @return Returns the dateCreated.
     */
    public Date getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated
     *            The dateCreated to set.
     */
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * @return Returns the itemNames.
     */
    public ArrayList getItemNames() {
        return itemNames;
    }

    /**
     * @param itemNames
     *            The itemNames to set.
     */
    public void setItemNames(ArrayList itemNames) {
        this.itemNames = itemNames;
    }

    /**
     * @return Returns the rowValues.
     */
    public ArrayList getRowValues() {
        return rowValues;
    }

    /**
     * @param rowValues
     *            The rowValues to set.
     */
    public void setRowValues(ArrayList rowValues) {
        this.rowValues = rowValues;
    }

    /**
     * @return Returns the eventHeaders.
     */
    public ArrayList getEventHeaders() {
        return eventHeaders;
    }

    /**
     * @param eventHeaders
     *            The eventHeaders to set.
     */
    public void setEventHeaders(ArrayList eventHeaders) {
        this.eventHeaders = eventHeaders;
    }

    public ArrayList getStudyGroupClasses() {
        return studyGroupClasses;
    }

    public void setStudyGroupClasses(ArrayList studyGroupClasses) {
        this.studyGroupClasses = studyGroupClasses;
    }

    public HashMap getGroupNames() {
        return groupNames;
    }

    public void setGroupNames(HashMap groupNames) {
        this.groupNames = groupNames;
    }

    public String getShowUniqueId() {
        return showUniqueId;
    }

    public void setShowUniqueId(String showUniqueId) {
        this.showUniqueId = showUniqueId;
    }

    /**
     * @return the hmInKeys
     */
    public HashMap getHmInKeys() {
        return hmInKeys;
    }

    /**
     * @param hmInKeys
     *            the hmInKeys to set
     */
    public void setHmInKeys(HashMap hmInKeys) {
        this.hmInKeys = hmInKeys;
    }

    /**
     * *************************************************************************
     * *******
     * ******************************************************************
     * **************
     *
     * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION - a new section that uses
     *      a different way to access the data - this is to improve performance
     *      and fix some bugs with the data extraction
     *      **************************
     *      *****************************************************
     */

    /**
     * This sets the values from the two querries
     */
    public void resetEntryBASE_EVENTSIDE() {
        hBASE_EVENTSIDE = new ArrayList();

    }

    public void resetEntryBASE_ITEMGROUPSIDE() {
        hBASE_ITEMGROUPSIDE = new ArrayList();
    }

    public void resetArrayListEntryBASE_ITEMGROUPSIDE() {
        aBASE_ITEMDATAID = new ArrayList();
    }

    /**
     * Add n entry
     *
     * @param itemdataid
     */
    public void addItemDataIdEntry(Integer itemdataid) {
        aBASE_ITEMDATAID.add(itemdataid);
    }//

    /**
     * Add an entry
     *
     * @param pitemDataId
     * @param pitemGroupId
     * @param pitemGroupName
     * @param pitemDescription
     * @param pitemName
     * @param pitemValue
     * @param pitemUnits
     * @param pcrfVersionName
     * @param pcrfVersionStatusId
     * @param pdateInterviewed
     * @param pinterviewerName
     * @param peventCrfDateCompleted
     * @param peventCrfDateValidateCompleted
     * @param peventCrfCompletionStatusId
     * @param pstudySubjectId
     * @param peventCrfId
     * @param pitemId
     * @param pcrfVersionId
     */
    public void addEntryBASE_ITEMGROUPSIDE(Integer pitemDataId, Integer pitemdataordinal, Integer pitemGroupId, String pitemGroupName, Integer pitemDatatypeId,
            String pitemDescription, String pitemName, String pitemValue, String pitemUnits, String pcrfVersionName, Integer pcrfVersionStatusId,
            Date pdateInterviewed, String pinterviewerName, Timestamp peventCrfDateCompleted, Timestamp peventCrfDateValidateCompleted,
            Integer peventCrfCompletionStatusId, Integer pitemGroupRepeatNumber, Integer pcrfId, Integer pstudySubjectId, Integer peventCrfId, Integer pitemId,
            Integer pcrfVersionId, Integer eventcrfStatusId) {
        extractDataset_ITEMGROUPSIDE obj = new extractDataset_ITEMGROUPSIDE();

        obj.setSQLDatasetBASE_ITEMGROUPSIDE(pitemDataId, pitemdataordinal, pitemGroupId, pitemGroupName, pitemDatatypeId, pitemDescription, pitemName,
                pitemValue, pitemUnits, pcrfVersionName, pcrfVersionStatusId, pdateInterviewed, pinterviewerName, peventCrfDateCompleted,
                peventCrfDateValidateCompleted, peventCrfCompletionStatusId, pitemGroupRepeatNumber, pcrfId, pstudySubjectId, peventCrfId, pitemId,
                pcrfVersionId, eventcrfStatusId);

        hBASE_ITEMGROUPSIDE.add(obj);
        // TODO - verify that the order is the same

    }// addEntryBASE_ITEMGROUPSIDE

    /**
     * Add an entry in the HashMap
     *
     * @param pitemDataId
     * @param pstudySubjectId
     * @param psampleOrdinal
     * @param pstudyEvenetDefinitionId
     * @param pstudyEventDefinitionName
     * @param pstudyEventLoacation
     * @param pstudyEventDateStart
     * @param pstudyEventDateEnd
     * @param pstudyEventStartTimeFlag
     * @param pstudyEventEndTimeFlag
     * @param pstudyEventStatusId
     * @param pstudyEventSubjectEventStatusId
     * @param pitemId
     * @param pcrfVersionId
     * @param peventCrfId
     * @param pstudyEventId
     */
    public void addEntryBASE_EVENTSIDE(Integer pitemDataId, Integer pstudySubjectId, Integer psampleOrdinal, Integer pstudyEvenetDefinitionId,
            String pstudyEventDefinitionName, String pstudyEventLoacation, Timestamp pstudyEventDateStart, Timestamp pstudyEventDateEnd,
            Boolean pstudyEventStartTimeFlag, Boolean pstudyEventEndTimeFlag, Integer pstudyEventStatusId, Integer pstudyEventSubjectEventStatusId,
            Integer pitemId, Integer pcrfVersionId, Integer peventCrfId, Integer pstudyEventId)

    {
        extractDataset_EVENTSIDE obj = new extractDataset_EVENTSIDE();

        obj.setSQLDatasetBASE_EVENTSIDE(pitemDataId, pstudySubjectId, psampleOrdinal, pstudyEvenetDefinitionId, pstudyEventDefinitionName, pstudyEventLoacation,
                pstudyEventDateStart, pstudyEventDateEnd, pstudyEventStartTimeFlag, pstudyEventEndTimeFlag, pstudyEventStatusId,
                pstudyEventSubjectEventStatusId, pitemId, pcrfVersionId, peventCrfId, pstudyEventId);

        hBASE_EVENTSIDE.add(obj);
        // TODO - verify that the order is the same

    }// addEntryBASE_EVENTSIDE

    /**
     * This class captures the data from getSQLDatasetBASE_EVENTSIDE SELECT
     *
     * itemdataid, studysubjectid, study_event.sample_ordinal,
     * study_event.study_event_definition_id, study_event_definition.name,
     * study_event.location, study_event.date_start, study_event.date_end,
     *
     * study_event.start_time_flag study_event.end_time_flag
     * study_event.status_id study_event.subject_event_status_id
     *
     * //ids itemid, crfversionid, eventcrfid, studyeventid
     *
     * This is used to merge the two BASE querries and build the eventData for
     * ExtractBean.java
     *
     */
    public class extractDataset_EVENTSIDE {
        // TODO - could be made private and then get/set

        // "primary key"
        public Integer itemDataId;

        // data
        public Integer studySubjectId;
        public Integer sampleOrdinal;
        public Integer studyEvenetDefinitionId;
        public String studyEventDefinitionName;
        public String studyEventLoacation;
        public Timestamp studyEventDateStart;
        public Timestamp studyEventDateEnd;
        public Boolean studyEventStartTimeFlag;
        public Boolean studyEventEndTimeFlag;
        public Integer studyEventStatusId;
        public Integer studyEventSubjectEventStatusId;
        public Integer itemId;
        public Integer crfVersionId;
        public Integer eventCrfId;
        public Integer studyEventId;

        // ctor
        void extractDataset_EVENTSIDE() {
            // ctor
        }

        public void setSQLDatasetBASE_EVENTSIDE(Integer pitemDataId, Integer pstudySubjectId, Integer psampleOrdinal, Integer pstudyEvenetDefinitionId,
                String pstudyEventDefinitionName, String pstudyEventLoacation, Timestamp pstudyEventDateStart, Timestamp pstudyEventDateEnd,
                Boolean pstudyEventStartTimeFlag, Boolean pstudyEventEndTimeFlag, Integer pstudyEventStatusId, Integer pstudyEventSubjectEventStatusId,
                Integer pitemId, Integer pcrfVersionId, Integer peventCrfId, Integer pstudyEventId) {
            // assigns

            // "primary key"
            itemDataId = pitemDataId;

            // data
            studySubjectId = pstudySubjectId;
            sampleOrdinal = psampleOrdinal;
            studyEvenetDefinitionId = pstudyEvenetDefinitionId;
            studyEventDefinitionName = pstudyEventDefinitionName;
            studyEventLoacation = pstudyEventLoacation;
            studyEventDateStart = pstudyEventDateStart;
            studyEventDateEnd = pstudyEventDateEnd;
            studyEventStartTimeFlag = pstudyEventStartTimeFlag;
            studyEventEndTimeFlag = pstudyEventEndTimeFlag;
            studyEventStatusId = pstudyEventStatusId;
            studyEventSubjectEventStatusId = pstudyEventSubjectEventStatusId;

            itemId = pitemId;
            crfVersionId = pcrfVersionId;
            eventCrfId = peventCrfId;
            studyEventId = pstudyEventId;

        }// set

        /**
         * for debug
         */
        @Override
        public String toString() {
            String ret = new String("");

            // "primary key"
            if (itemDataId == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemDataId.toString();
            }
            ret = ret + "_";

            // data

            if (studySubjectId == null) {
                ret = ret + "null";
            } else {
                ret = ret + studySubjectId.toString();
            }
            ret = ret + "_";

            if (sampleOrdinal == null) {
                ret = ret + "null";
            } else {
                ret = ret + sampleOrdinal.toString();
            }
            ret = ret + "_";

            if (studyEvenetDefinitionId == null) {
                ret = ret + "null";
            } else {
                ret = ret + studyEvenetDefinitionId.toString();
            }
            ret = ret + "_";

            if (studyEventDefinitionName == null) {
                ret = ret + "null";
            } else {
                ret = ret + studyEventDefinitionName.toString();
            }
            ret = ret + "_";

            if (studyEventLoacation == null) {
                ret = ret + "null";
            } else {
                ret = ret + studyEventLoacation.toString();
            }
            ret = ret + "_";

            if (studyEventDateStart == null) {
                ret = ret + "null";
            } else {
                ret = ret + studyEventDateStart.toString();
            }
            ret = ret + "_";

            if (studyEventDateEnd == null) {
                ret = ret + "null";
            } else {
                ret = ret + studyEventDateEnd.toString();
            }
            ret = ret + "_";

            if (studyEventStartTimeFlag == null) {
                ret = ret + "null";
            } else {
                ret = ret + studyEventStartTimeFlag.toString();
            }
            ret = ret + "_";

            if (studyEventEndTimeFlag == null) {
                ret = ret + "null";
            } else {
                ret = ret + studyEventEndTimeFlag.toString();
            }
            ret = ret + "_";

            if (studyEventStatusId == null) {
                ret = ret + "null";
            } else {
                ret = ret + studyEventStatusId.toString();
            }
            ret = ret + "_";

            if (studyEventSubjectEventStatusId == null) {
                ret = ret + "null";
            } else {
                ret = ret + studyEventSubjectEventStatusId.toString();
            }
            ret = ret + "_";

            if (itemId == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemId.toString();
            }
            ret = ret + "_";

            if (crfVersionId == null) {
                ret = ret + "null";
            } else {
                ret = ret + crfVersionId.toString();
            }
            ret = ret + "_";

            if (eventCrfId == null) {
                ret = ret + "null";
            } else {
                ret = ret + eventCrfId.toString();
            }
            ret = ret + "_";

            if (studyEventId == null) {
                ret = ret + "null";
            } else {
                ret = ret + studyEventId.toString();
            }
            ret = ret + "_";

            return ret;
        }

    }// class

    /**
     * The second part of the merge for eventData in ExtractBean
     *
     * SELECT itemdataid, item_group_metadata.item_group_id , item_group.name,
     * itemdesc, itemname, itemvalue, itemunits, crfversioname,
     * crfversionstatusid, dateinterviewed, interviewername,
     * eventcrfdatecompleted, eventcrfdatevalidatecompleted,
     * eventcrfcompletionstatusid, repeat_number, crfid,
     *
     * //and ids studysubjectid, eventcrfid, itemid, crfversionid
     *
     *
     *
     * @author vbcoman
     *
     */
    public class extractDataset_ITEMGROUPSIDE {

        // TODO - could be made private and then get/set

        // this is the key
        public Integer itemDataId;

        // data
        public Integer itemGroupId;
        public String itemGroupName;
        public String itemDescription;
        public String itemName;
        public String itemValue;
        public String itemUnits;
        public String crfVersionName;
        public Integer crfVersionStatusId;
        public Date dateInterviewed;
        public String interviewerName;
        public Timestamp eventCrfDateCompleted;
        public Timestamp eventCrfDateValidateCompleted;
        public Integer eventCrfCompletionStatusId;
        public Integer itemGroupRepeatNumber;
        public Integer crfid;
        public Integer eventCrfStatusId;

        // keys
        public Integer studySubjectId;
        public Integer eventCrfId;
        public Integer itemId;
        public Integer crfVersionId;

        // ctor

        void extractDataset_ITEMGROUPSIDE() {
            // empty ctor
        }

        public void setSQLDatasetBASE_ITEMGROUPSIDE(Integer pitemDataId, Integer pitemdataordinal, Integer pitemGroupId, String pitemGroupName,
                Integer pitemDatatypeId, String pitemDescription, String pitemName, String pitemValue, String pitemUnits, String pcrfVersionName,
                Integer pcrfVersionStatusId, Date pdateInterviewed, String pinterviewerName, Timestamp peventCrfDateCompleted,
                Timestamp peventCrfDateValidateCompleted, Integer peventCrfCompletionStatusId, Integer pitemGroupMetatdatrepeatNumber, Integer pcrfId,
                Integer pstudySubjectId, Integer peventCrfId, Integer pitemId, Integer pcrfVersionId, Integer eventcrfStatusId) {
            // assign
            itemDataId = pitemDataId;
            itemGroupId = pitemGroupId;
            itemGroupName = pitemGroupName;
            itemDescription = pitemDescription;
            itemName = pitemName;

            if (pitemDatatypeId == 9) {
                SimpleDateFormat sdf = new SimpleDateFormat(ApplicationConstants.getDateFormatInItemData());
                sdf.setLenient(false);
                try {
                    java.util.Date date = sdf.parse(pitemValue);
                    itemValue = new SimpleDateFormat("yyyy-MM-dd").format(date);
                } catch (ParseException fe) {
                    itemValue = pitemValue;
                    logger.info("Failed date format for: item-data-id=" + pitemDataId + " with data-type-id=" + pitemDatatypeId + " and item-data-value="
                            + pitemValue);
                }
            } else {
                itemValue = pitemValue;
            }

            itemUnits = pitemUnits;
            crfVersionName = pcrfVersionName;
            crfVersionStatusId = pcrfVersionStatusId;
            dateInterviewed = pdateInterviewed;
            interviewerName = pinterviewerName;
            eventCrfDateCompleted = peventCrfDateCompleted;
            eventCrfDateValidateCompleted = peventCrfDateValidateCompleted;
            eventCrfCompletionStatusId = peventCrfCompletionStatusId;
            eventCrfStatusId = eventcrfStatusId;

            /**
             * @vbc - Sept 2 , 2008 - changed the group ordinal from
             *      repeat_number to ordinal in item_data
             */
            // itemGroupRepeatNumber = pitemGroupMetatdatrepeatNumber;
            itemGroupRepeatNumber = pitemdataordinal;

            crfid = pcrfId;

            studySubjectId = pstudySubjectId;
            eventCrfId = peventCrfId;
            itemId = pitemId;
            crfVersionId = pcrfVersionId;
        }

        /**
         * for debug
         */
        @Override
        public String toString() {
            String ret = new String("");

            if (itemDataId == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemDataId.toString();
            }
            ret = ret + "_";

            // data

            if (itemGroupId == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemGroupId.toString();
            }
            ret = ret + "_";

            if (itemGroupName == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemGroupName.toString();
            }
            ret = ret + "_";

            if (itemDescription == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemDescription.toString();
            }
            ret = ret + "_";

            if (itemName == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemName.toString();
            }
            ret = ret + "_";

            if (itemValue == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemValue.toString();
            }
            ret = ret + "_";

            if (itemUnits == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemUnits.toString();
            }
            ret = ret + "_";

            if (crfVersionName == null) {
                ret = ret + "null";
            } else {
                ret = ret + crfVersionName.toString();
            }
            ret = ret + "_";

            if (crfVersionStatusId == null) {
                ret = ret + "null";
            } else {
                ret = ret + crfVersionStatusId.toString();
            }
            ret = ret + "_";

            if (dateInterviewed == null) {
                ret = ret + "null";
            } else {
                ret = ret + dateInterviewed.toString();
            }
            ret = ret + "_";

            if (interviewerName == null) {
                ret = ret + "null";
            } else {
                ret = ret + interviewerName.toString();
            }
            ret = ret + "_";

            if (eventCrfDateCompleted == null) {
                ret = ret + "null";
            } else {
                ret = ret + eventCrfDateCompleted.toString();
            }
            ret = ret + "_";

            if (eventCrfDateValidateCompleted == null) {
                ret = ret + "null";
            } else {
                ret = ret + eventCrfDateValidateCompleted.toString();
            }
            ret = ret + "_";

            if (eventCrfCompletionStatusId == null) {
                ret = ret + "null";
            } else {
                ret = ret + eventCrfCompletionStatusId.toString();
            }
            ret = ret + "_";

            if (itemGroupRepeatNumber == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemGroupRepeatNumber.toString();
            }
            ret = ret + "_";

            if (crfid == null) {
                ret = ret + "null";
            } else {
                ret = ret + crfid.toString();
            }
            ret = ret + "_";

            // keys
            if (studySubjectId == null) {
                ret = ret + "null";
            } else {
                ret = ret + studySubjectId.toString();
            }
            ret = ret + "_";

            if (eventCrfId == null) {
                ret = ret + "null";
            } else {
                ret = ret + eventCrfId.toString();
            }
            ret = ret + "_";

            if (itemId == null) {
                ret = ret + "null";
            } else {
                ret = ret + itemId.toString();
            }
            ret = ret + "_";

            if (crfVersionId == null) {
                ret = ret + "null";
            } else {
                ret = ret + crfVersionId.toString();
            }
            ret = ret + "_";

            return ret;
        }

    }// class

    /**
     * @return the hBASE_EVENTSIDE
     */
    public ArrayList getHBASE_EVENTSIDE() {
        return hBASE_EVENTSIDE;
    }

    /**
     * @param hbase_eventside
     *            the hBASE_EVENTSIDE to set
     */
    public void setHBASE_EVENTSIDE(ArrayList hbase_eventside) {
        hBASE_EVENTSIDE = hbase_eventside;
    }

    /**
     * @return the hBASE_ITEMGROUPSIDE
     */
    public ArrayList getHBASE_ITEMGROUPSIDE() {
        return hBASE_ITEMGROUPSIDE;
    }

    /**
     * @param hbase_itemgroupside
     *            the hBASE_ITEMGROUPSIDE to set
     */
    public void setHBASE_ITEMGROUPSIDE(ArrayList hbase_itemgroupside) {
        hBASE_ITEMGROUPSIDE = hbase_itemgroupside;
    }

    /**
     * @return the aBASE_ITEMDATAID
     */
    public ArrayList getABASE_ITEMDATAID() {
        return aBASE_ITEMDATAID;
    }

    /**
     * @param abase_itemdataid
     *            the aBASE_ITEMDATAID to set
     */
    public void setABASE_ITEMDATAID(ArrayList abase_itemdataid) {
        aBASE_ITEMDATAID = abase_itemdataid;
    }

}
