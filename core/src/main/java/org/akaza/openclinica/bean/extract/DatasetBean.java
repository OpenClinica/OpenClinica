/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.extract;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.DatasetItemStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author thickerson
 */
public class DatasetBean extends AuditableEntityBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private int studyId;
    private String description;
    private String SQLStatement;
    private int numRuns = 0;
    private int runTime = 0;
    private java.util.Date dateLastRun;
    private java.util.Date dateStart;
    private java.util.Date dateEnd;
    private int approverId = 0;
    // private int maxItemDataBeanOrdinal = 0;
    // above somewhat of a hack,
    // we need to deliver the maximum ordinal size
    // for repeating items to the extract data bean
    // so that they can generate the full report, tbh 08/2007
    /*
     * private ArrayList versionIds = new ArrayList(); private ArrayList
     * versionNames = new ArrayList();
     * 
     * private ArrayList eventNames = new ArrayList();
     */
    private ArrayList eventIds = new ArrayList();
    private ArrayList itemIds = new ArrayList();
    private ArrayList subjectGroupIds = new ArrayList();
    private HashMap itemMap = new HashMap();

    private boolean showEventLocation = false;
    private boolean showEventStart = false;
    private boolean showEventEnd = false;
    private boolean showSubjectDob = false;
    private boolean showSubjectGender = false;
    //
    // TODO add new attributes to show/hide here, tbh 07/09/2007
    //
    private boolean showSubjectStatus = false;
    private boolean showSubjectUniqueIdentifier = false;
    private boolean showSubjectAgeAtEvent = false;
    private boolean showSubjectSecondaryId = false;

    private boolean showEventStatus = false;
    // tbh
    private boolean showEventStartTime = false;
    private boolean showEventEndTime = false;
    // how is this different than Start/End? not adding the two above for now,
    // tbh

    private boolean showCRFstatus = false;
    private boolean showCRFversion = false;
    private boolean showCRFinterviewerName = false;
    private boolean showCRFinterviewerDate = false;
    private boolean showCRFcompletionDate = false;
    // again, how is it different from Start/End?
    private boolean showSubjectGroupInformation = false;
    // private boolean showGroupInformation = false;
    // private boolean showDiscrepancyInformation = false;
    // removed above after meeting 07/16/2007, tbh
    //
    //

    private ArrayList itemDefCrf = new ArrayList();
    // map items with definition and CRF

    private String VIEW_NAME = "extract_data_table";
    // put up here since we know it's going to be changed, tbh

    private String odmMetaDataVersionName;
    private String odmMetaDataVersionOid;
    private String odmPriorStudyOid;
    private String odmPriorMetaDataVersionOid;
    private DatasetItemStatus datasetItemStatus;

    public DatasetBean() {
    }

    /**
     * @return Returns the dateLastRun.
     */
    public java.util.Date getDateLastRun() {
        return dateLastRun;
    }

    /**
     * @param dateLastRun
     *            The dateLastRun to set.
     */
    public void setDateLastRun(java.util.Date dateLastRun) {
        this.dateLastRun = dateLastRun;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the numRuns.
     */
    public int getNumRuns() {
        return numRuns;
    }

    /**
     * @param numRuns
     *            The numRuns to set.
     */
    public void setNumRuns(int numRuns) {
        this.numRuns = numRuns;
    }

    /**
     * @return Returns the runTime.
     */
    public int getRunTime() {
        return runTime;
    }

    /**
     * @param runTime
     *            The runTime to set.
     */
    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }

    /**
     * @return Returns the sQLStatement.
     */
    public String getSQLStatement() {
        return SQLStatement;
    }

    /**
     * @param statement
     *            The sQLStatement to set.
     */
    public void setSQLStatement(String statement) {
        SQLStatement = statement;
    }

    /**
     * @return Returns the studyId.
     */
    public int getStudyId() {
        return studyId;
    }

    /**
     * @param studyId
     *            The studyId to set.
     */
    public void setStudyId(int studyId) {
        this.studyId = studyId;
    }

    /**
     * @return Returns the approverId.
     */
    public int getApproverId() {
        return approverId;
    }

    /**
     * @param approverId
     *            The approverId to set.
     */
    public void setApproverId(int approverId) {
        this.approverId = approverId;
    }

    /**
     * @return Returns the dateEnd.
     */
    public java.util.Date getDateEnd() {
        return dateEnd;
    }

    /**
     * @param dateEnd
     *            The dateEnd to set.
     */
    public void setDateEnd(java.util.Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    /**
     * @return Returns the dateStart.
     */
    public java.util.Date getDateStart() {
        return dateStart;
    }

    /**
     * @param dateStart
     *            The dateStart to set.
     */
    public void setDateStart(java.util.Date dateStart) {
        this.dateStart = dateStart;
    }

    /**
     * takes the dataset bean information and generates a query; this will
     * changes if the database changes. This will also change when we apply
     * filters.
     * 
     * @return string in SQL, to elicit information.
     */
    public String generateQuery() {
        StringBuffer sb = new StringBuffer();
        sb.append("select distinct * from " + VIEW_NAME + " where ");

        if (!this.getEventIds().isEmpty()) {
            String idList = this.getEventIds().toString();
            sb.append("study_event_definition_id in (" + idList + ") and ");
        }
        if (!this.getItemIds().isEmpty()) {
            String idList = this.getItemIds().toString();
            sb.append("item_id in (" + idList + ") and ");
        }

        String pattern = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        // guard clauses to defend vs NPE, tbh 10-2009
        String beginDate = "1900-01-01";
        if (dateStart != null) {
            beginDate = sdf.format(this.dateStart);
        }
        String stopDate = "2100-01-01";
        if (dateEnd != null) {
            stopDate = sdf.format(this.dateEnd);
        }
        // << tbh 10/2009
        sb.append("(date(date_created) >= date('" + beginDate + "')) and (date(date_created) <= date('" + stopDate + "'))");
        // perform regexp here that pulls out [] square brackets

        String returnMe = sb.toString().replaceAll("\\[|\\]", "");
        // returnMe = returnMe.replaceAll("[^0-9])",")");
        // return sb.toString();
        returnMe = returnMe + " order by date_start asc";
        return returnMe;
    }

    /**
     * generateOracleQuery, generates the Oracle syntax for the query (this may
     * have to be changed to reflect different syntaxes in the future)
     * 
     * @return the Oracle SQL syntax to capture datasets.
     */
    public String generateOracleQuery() {
        StringBuffer sb = new StringBuffer();
        sb.append("select distinct * from " + VIEW_NAME + " where ");
        if (!this.getEventIds().isEmpty()) {
            String idList = this.getEventIds().toString();
            sb.append("study_event_definition_id in (" + idList + ") and ");
        }

        if (!this.getItemIds().isEmpty()) {
            String idList = this.getItemIds().toString();
            sb.append("item_id in (" + idList + ") and ");
        }
        String pattern = "dd-MMM-yyyy";// changed by bads issue 2152
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String beginDate = sdf.format(this.dateStart);
        String stopDate = sdf.format(this.dateEnd);

        sb.append("(date_created >= '" + beginDate + "') and (date_created <= '" + stopDate + "')");
        // perform regexp here that pulls out [] square brackets

        logger.info("-----------------------------");
        logger.info(sb.toString());
        logger.info("-----------------------------");
        String returnMe = sb.toString().replaceAll("\\[|\\]", "");
        // returnMe = returnMe.replaceAll("[^0-9])",")");
        // return sb.toString();
        returnMe = returnMe + " order by date_start";
        return returnMe;
    }

    /**
     * @return Returns the itemIds.
     */
    public ArrayList getItemIds() {
        return itemIds;
    }

    /**
     * @param itemIds
     *            The itemIds to set.
     */
    public void setItemIds(ArrayList itemIds) {
        this.itemIds = itemIds;
    }

    /**
     * @return Returns the itemMap.
     */
    public HashMap getItemMap() {
        return itemMap;
    }

    /**
     * @param itemMap
     *            The itemMap to set.
     */
    public void setItemMap(HashMap itemMap) {
        this.itemMap = itemMap;
    }

    /**
     * @return Returns the eventIds.
     */
    public ArrayList getEventIds() {
        return eventIds;
    }

    /**
     * @param eventIds
     *            The eventIds to set.
     */
    public void setEventIds(ArrayList eventIds) {
        this.eventIds = eventIds;
    }

    /**
     * @return Returns the showEventEnd.
     */
    public boolean isShowEventEnd() {
        return showEventEnd;
    }

    /**
     * @param showEventEnd
     *            The showEventEnd to set.
     */
    public void setShowEventEnd(boolean showEventEnd) {
        this.showEventEnd = showEventEnd;
    }

    /**
     * @return Returns the showEventLocation.
     */
    public boolean isShowEventLocation() {
        return showEventLocation;
    }

    /**
     * @param showEventLocation
     *            The showEventLocation to set.
     */
    public void setShowEventLocation(boolean showEventLocation) {
        this.showEventLocation = showEventLocation;
    }

    /**
     * @return Returns the showEventStart.
     */
    public boolean isShowEventStart() {
        return showEventStart;
    }

    /**
     * @param showEventStart
     *            The showEventStart to set.
     */
    public void setShowEventStart(boolean showEventStart) {
        this.showEventStart = showEventStart;
    }

    /**
     * @return Returns the showSubjectDob.
     */
    public boolean isShowSubjectDob() {
        return showSubjectDob;
    }

    /**
     * @param showSubjectDob
     *            The showSubjectDob to set.
     */
    public void setShowSubjectDob(boolean showSubjectDob) {
        this.showSubjectDob = showSubjectDob;
    }

    /**
     * @return Returns the showSubjectGender.
     */
    public boolean isShowSubjectGender() {
        return showSubjectGender;
    }

    /**
     * @param showSubjectGender
     *            The showSubjectGender to set.
     */
    public void setShowSubjectGender(boolean showSubjectGender) {
        this.showSubjectGender = showSubjectGender;
    }

    /**
     * @return Returns the itemDefCrf.
     */
    public ArrayList getItemDefCrf() {
        return itemDefCrf;
    }

    /**
     * @param itemDefCrf
     *            The itemDefCrf to set.
     */
    public void setItemDefCrf(ArrayList itemDefCrf) {
        this.itemDefCrf = itemDefCrf;
    }

    public boolean isShowCRFcompletionDate() {
        return showCRFcompletionDate;
    }

    public void setShowCRFcompletionDate(boolean showCRFcompletionDate) {
        this.showCRFcompletionDate = showCRFcompletionDate;
    }

    public boolean isShowCRFinterviewerDate() {
        return showCRFinterviewerDate;
    }

    public void setShowCRFinterviewerDate(boolean showCRFinterviewerDate) {
        this.showCRFinterviewerDate = showCRFinterviewerDate;
    }

    public boolean isShowCRFinterviewerName() {
        return showCRFinterviewerName;
    }

    public void setShowCRFinterviewerName(boolean showCRFinteviewerName) {
        this.showCRFinterviewerName = showCRFinteviewerName;
    }

    public boolean isShowCRFstatus() {
        return showCRFstatus;
    }

    public void setShowCRFstatus(boolean showCRFstatus) {
        this.showCRFstatus = showCRFstatus;
    }

    public boolean isShowCRFversion() {
        return showCRFversion;
    }

    public void setShowCRFversion(boolean showCRFversion) {
        this.showCRFversion = showCRFversion;
    }

    /*
     * public boolean isShowDiscrepancyInformation() { return
     * showDiscrepancyInformation; }
     * 
     * public void setShowDiscrepancyInformation(boolean
     * showDiscrepancyInformation) { this.showDiscrepancyInformation =
     * showDiscrepancyInformation; }
     */

    public boolean isShowEventEndTime() {
        return showEventEndTime;
    }

    public void setShowEventEndTime(boolean showEventEndTime) {
        this.showEventEndTime = showEventEndTime;
    }

    public boolean isShowEventStartTime() {
        return showEventStartTime;
    }

    public void setShowEventStartTime(boolean showEventStartTime) {
        this.showEventStartTime = showEventStartTime;
    }

    public boolean isShowEventStatus() {
        return showEventStatus;
    }

    public void setShowEventStatus(boolean showEventStatus) {
        this.showEventStatus = showEventStatus;
    }

    /*
     * public boolean isShowGroupInformation() { return showGroupInformation; }
     * FIXME now change all the places where this is located... public void
     * setShowGroupInformation(boolean showGroupInformation) {
     * this.showGroupInformation = showGroupInformation; }
     */

    public boolean isShowSubjectAgeAtEvent() {
        return showSubjectAgeAtEvent;
    }

    public void setShowSubjectAgeAtEvent(boolean showSubjectAgeAtEvent) {
        this.showSubjectAgeAtEvent = showSubjectAgeAtEvent;
    }

    public boolean isShowSubjectSecondaryId() {
        return showSubjectSecondaryId;
    }

    public void setShowSubjectSecondaryId(boolean showSubjectSecondaryId) {
        this.showSubjectSecondaryId = showSubjectSecondaryId;
    }

    public boolean isShowSubjectStatus() {
        return showSubjectStatus;
    }

    public void setShowSubjectStatus(boolean showSubjectStatus) {
        this.showSubjectStatus = showSubjectStatus;
    }

    public boolean isShowSubjectUniqueIdentifier() {
        return showSubjectUniqueIdentifier;
    }

    public void setShowSubjectUniqueIdentifier(boolean showUniqueIdentifier) {
        this.showSubjectUniqueIdentifier = showUniqueIdentifier;
    }

    public boolean isShowSubjectGroupInformation() {
        return showSubjectGroupInformation;
    }

    public void setShowSubjectGroupInformation(boolean showSubjectGroupInformation) {
        this.showSubjectGroupInformation = showSubjectGroupInformation;
    }

    public ArrayList getSubjectGroupIds() {
        return subjectGroupIds;
    }

    public void setSubjectGroupIds(ArrayList subjectGroupIds) {
        this.subjectGroupIds = subjectGroupIds;
    }

    public void setODMMetaDataVersionName(String odmMetaDataVersionName) {
        this.odmMetaDataVersionName = odmMetaDataVersionName;
    }

    public String getODMMetaDataVersionName() {
        return this.odmMetaDataVersionName;
    }

    public void setODMMetaDataVersionOid(String odmMetaDataVersionOid) {
        this.odmMetaDataVersionOid = odmMetaDataVersionOid;
    }

    public String getODMMetaDataVersionOid() {
        return this.odmMetaDataVersionOid;
    }

    public void setODMPriorStudyOid(String odmPriorStudyOid) {
        this.odmPriorStudyOid = odmPriorStudyOid;
    }

    public String getODMPriorStudyOid() {
        return this.odmPriorStudyOid;
    }

    public void setODMPriorMetaDataVersionOid(String odmPriorMetaDataVersionOid) {
        this.odmPriorMetaDataVersionOid = odmPriorMetaDataVersionOid;
    }

    public String getODMPriorMetaDataVersionOid() {
        return this.odmPriorMetaDataVersionOid;
    }

    public DatasetItemStatus getDatasetItemStatus() {
        return datasetItemStatus;
    }

    public void setDatasetItemStatus(DatasetItemStatus datasetItemStatus) {
        this.datasetItemStatus = datasetItemStatus;
    }

    public String sqlWithUniqeItemIds(String itemIdStr) {
        String sql = "";
        String[] s1 = this.SQLStatement.split("item_id in");
        sql += s1[0] + itemIdStr + s1[1].substring(s1[1].indexOf(")"));
        return sql;
    }

    /*
     * public int getMaxItemDataBeanOrdinal() { return maxItemDataBeanOrdinal; }
     * 
     * public void setMaxItemDataBeanOrdinal(int maxItemDataBeanOrdinal) {
     * this.maxItemDataBeanOrdinal = maxItemDataBeanOrdinal; }
     */
}
