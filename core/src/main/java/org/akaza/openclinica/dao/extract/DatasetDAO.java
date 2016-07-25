/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.extract;

import org.akaza.openclinica.bean.core.DatasetItemStatus;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.submit.ItemDAO;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.sql.DataSource;

/**
 * The data access object for datasets; also generates datasets based on their
 * query and criteria set; also generates the extract bean, which holds dataset
 * information.
 *
 * @author thickerson
 *
 *
 */
public class DatasetDAO extends AuditableEntityDAO {

    // private DataSource ds;
    // private DAODigester digester;

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_DATASET;
    }

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
    }

    /**
     * Creates a DatasetDAO object, for use in the application only.
     *
     * @param ds
     */
    public DatasetDAO(DataSource ds) {
        super(ds);
        this.setQueryNames();
    }

    /**
     * Creates a DatasetDAO object suitable for testing purposes only.
     *
     * @param ds
     * @param digester
     */
    public DatasetDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        this.setQueryNames();
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.STRING);// name
        this.setTypeExpected(5, TypeNames.STRING);// desc
        this.setTypeExpected(6, TypeNames.STRING);// sql
        this.setTypeExpected(7, TypeNames.INT);// num runs
        this.setTypeExpected(8, TypeNames.TIMESTAMP);// date start. YW,
        // 08-21-2007, datatype
        // changed to Timestamp
        this.setTypeExpected(9, TypeNames.TIMESTAMP);// date end
        this.setTypeExpected(10, TypeNames.DATE);// created
        this.setTypeExpected(11, TypeNames.DATE);// updated
        this.setTypeExpected(12, TypeNames.DATE);// last run
        this.setTypeExpected(13, TypeNames.INT);// owner id
        this.setTypeExpected(14, TypeNames.INT);// approver id
        this.setTypeExpected(15, TypeNames.INT);// update id
        this.setTypeExpected(16, TypeNames.BOOL);// show_event_location
        this.setTypeExpected(17, TypeNames.BOOL);// show_event_start
        this.setTypeExpected(18, TypeNames.BOOL);// show_event_end
        this.setTypeExpected(19, TypeNames.BOOL);// show_subject_dob
        this.setTypeExpected(20, TypeNames.BOOL);// show_subject_gender
        this.setTypeExpected(21, TypeNames.BOOL);// show_event_status
        this.setTypeExpected(22, TypeNames.BOOL);// show_subject_status
        this.setTypeExpected(23, TypeNames.BOOL);// show_subject_unique_id
        this.setTypeExpected(24, TypeNames.BOOL);// show_subject_age_at_event
        this.setTypeExpected(25, TypeNames.BOOL);// show_crf_status
        this.setTypeExpected(26, TypeNames.BOOL);// show_crf_version
        this.setTypeExpected(27, TypeNames.BOOL);// show_crf_int_name
        this.setTypeExpected(28, TypeNames.BOOL);// show_crf_int_date
        this.setTypeExpected(29, TypeNames.BOOL);// show_group_info
        this.setTypeExpected(30, TypeNames.BOOL);// show_disc_info
        this.setTypeExpected(31, TypeNames.STRING);// odm_metadataversion_name
        this.setTypeExpected(32, TypeNames.STRING);// odm_metadataversion_oid
        this.setTypeExpected(33, TypeNames.STRING);// odm_prior_study_oid
        this.setTypeExpected(34, TypeNames.STRING);// odm_prior_metadataversion_oid
        this.setTypeExpected(35, TypeNames.BOOL);// show_secondary_id
        this.setTypeExpected(36, TypeNames.INT);// dataset_item_status_id
    }

    public void setExtractTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// subj id
        this.setTypeExpected(2, TypeNames.STRING);// subj identifier
        this.setTypeExpected(3, TypeNames.INT);// study id
        this.setTypeExpected(4, TypeNames.STRING);// study ident
        this.setTypeExpected(5, TypeNames.INT);// event def crf id
        this.setTypeExpected(6, TypeNames.INT);// crf id
        this.setTypeExpected(7, TypeNames.STRING);// crf label
        this.setTypeExpected(8, TypeNames.STRING);// crf name
        this.setTypeExpected(9, TypeNames.INT);// version id
        this.setTypeExpected(10, TypeNames.STRING);// version label
        this.setTypeExpected(11, TypeNames.STRING);// version name
        this.setTypeExpected(12, TypeNames.INT);// study event id
        this.setTypeExpected(13, TypeNames.INT);// event crf id
        this.setTypeExpected(14, TypeNames.INT);// item data id
        this.setTypeExpected(15, TypeNames.STRING);// value
        // oops added three more here
        this.setTypeExpected(16, TypeNames.STRING);// sed.name
        this.setTypeExpected(17, TypeNames.BOOL);// repeating
        this.setTypeExpected(18, TypeNames.INT);// sample ordinal
        this.setTypeExpected(19, TypeNames.INT);// item id
        this.setTypeExpected(20, TypeNames.STRING);// item name
        this.setTypeExpected(21, TypeNames.STRING);// item desc
        this.setTypeExpected(22, TypeNames.STRING);// item units
        this.setTypeExpected(23, TypeNames.DATE);// date created for item
        // data
        this.setTypeExpected(24, TypeNames.INT);// study event definition id
        this.setTypeExpected(25, TypeNames.STRING);// option stings
        this.setTypeExpected(26, TypeNames.STRING);// option values
        this.setTypeExpected(27, TypeNames.INT);// response type id
        this.setTypeExpected(28, TypeNames.STRING);// gender
        this.setTypeExpected(29, TypeNames.DATE);// dob
        // added more columns below this line
        this.setTypeExpected(30, TypeNames.INT);// s.status_id AS
        // subject_status_id,
        this.setTypeExpected(31, TypeNames.STRING);// s.unique_identifier,
        this.setTypeExpected(32, TypeNames.BOOL);// s.dob_collected,
        this.setTypeExpected(33, TypeNames.INT);// ec.completion_status_id,
        this.setTypeExpected(34, TypeNames.DATE);// ec.date_created AS
        // event_crf_start_time,
        this.setTypeExpected(35, TypeNames.INT);// crfv.status_id AS
        // crf_version_status_id,
        this.setTypeExpected(36, TypeNames.STRING);// ec.interviewer_name,
        this.setTypeExpected(37, TypeNames.DATE);// ec.date_interviewed,
        this.setTypeExpected(38, TypeNames.DATE);// ec.date_completed AS
        // event_crf_date_completed,
        this.setTypeExpected(39, TypeNames.DATE);// ec.date_validate_completed
        // AS
        // event_crf_date_validate_completed,
        this.setTypeExpected(40, TypeNames.INT);// sgmap.study_group_id,
        this.setTypeExpected(41, TypeNames.INT);// sgmap.study_group_class_id
        // this.setTypeExpected(39, TypeNames.STRING);//ig.name AS
        // item_group_name,
        // this.setTypeExpected(40, TypeNames.STRING);//dn.description AS
        // discrepancy_note_description,
        // this.setTypeExpected(41, TypeNames.INT);//dn.resolution_status_id AS
        // discrepancy_resolution_status_id,
        // this.setTypeExpected(42, TypeNames.STRING);//dn.detailed_notes,
        // this.setTypeExpected(43, TypeNames.INT);//type id for dn
        // added more columns above this line
        this.setTypeExpected(42, TypeNames.STRING);// location
        this.setTypeExpected(43, TypeNames.TIMESTAMP);// date start. YW,
        // 08-21-2007, datatype
        // changed to Timestamp
        this.setTypeExpected(44, TypeNames.TIMESTAMP);// date end
        this.setTypeExpected(45, TypeNames.INT);// item data ordinal, added tbh
        this.setTypeExpected(46, TypeNames.STRING);// item group name, added
        this.setTypeExpected(47, TypeNames.STRING);// secondary label
        // tbh
        this.setTypeExpected(48, TypeNames.INT);// item_data_type_id
        this.setTypeExpected(49, TypeNames.STRING);// study_event_definition_oid
        this.setTypeExpected(50, TypeNames.STRING);// crf_version_oid
        this.setTypeExpected(51, TypeNames.STRING);// item_group_oid
        this.setTypeExpected(52, TypeNames.STRING);// item_oid
        this.setTypeExpected(53, TypeNames.STRING);// study_subject_oid
        this.setTypeExpected(54, TypeNames.INT);// sed_order
        this.setTypeExpected(55, TypeNames.INT);// crf_order
        this.setTypeExpected(56, TypeNames.INT);// item_order
    }

    public void setDefinitionCrfItemTypesExpected() {
        this.unsetTypeExpected();
        // copy from itemdao.setTypesExpected()
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.BOOL);// phi status
        this.setTypeExpected(6, TypeNames.INT);// data type id
        this.setTypeExpected(7, TypeNames.INT);// reference type id
        this.setTypeExpected(8, TypeNames.INT);// status id
        this.setTypeExpected(9, TypeNames.INT);// owner id
        this.setTypeExpected(10, TypeNames.DATE);// created
        this.setTypeExpected(11, TypeNames.DATE);// updated
        this.setTypeExpected(12, TypeNames.INT);// update id
        this.setTypeExpected(13, TypeNames.STRING);// oc_oid

        this.setTypeExpected(14, TypeNames.INT);// sed_id
        this.setTypeExpected(15, TypeNames.STRING);// sed_name
        this.setTypeExpected(16, TypeNames.INT);// crf_id
        this.setTypeExpected(17, TypeNames.STRING);// crf_name
    }

    public EntityBean update(EntityBean eb) {
        DatasetBean db = (DatasetBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(db.getStudyId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(db.getStatus().getId()));
        variables.put(Integer.valueOf(3), db.getName());
        variables.put(Integer.valueOf(4), db.getDescription());
        variables.put(Integer.valueOf(5), db.getSQLStatement());
        variables.put(Integer.valueOf(6), db.getDateLastRun());
        variables.put(Integer.valueOf(7), Integer.valueOf(db.getNumRuns()));

        variables.put(Integer.valueOf(8), Integer.valueOf(db.getUpdaterId()));
        if (db.getApproverId() <= 0) {
            // nullVars.put(Integer.valueOf(9), null);
            // ABOVE IS WRONG; follow the example below:
            nullVars.put(Integer.valueOf(9), Integer.valueOf(Types.NUMERIC));
            variables.put(Integer.valueOf(9), null);
        } else {
            variables.put(Integer.valueOf(9), Integer.valueOf(db.getApproverId()));
        }

        variables.put(Integer.valueOf(10), db.getDateStart());
        variables.put(Integer.valueOf(11), db.getDateEnd());
        variables.put(Integer.valueOf(12), Integer.valueOf(db.getId()));
        this.execute(digester.getQuery("update"), variables, nullVars);
        return eb;
    }

    public EntityBean create(EntityBean eb) {
        /*
         * INSERT INTO DATASET (STUDY_ID, STATUS_ID, NAME, DESCRIPTION,
         * SQL_STATEMENT, OWNER_ID, DATE_CREATED, DATE_LAST_RUN, NUM_RUNS,
         * DATE_START, DATE_END,
         * SHOW_EVENT_LOCATION,SHOW_EVENT_START,SHOW_EVENT_END,
         * SHOW_SUBJECT_DOB,SHOW_SUBJECT_GENDER) VALUES
         * (?,?,?,?,?,?,NOW(),NOW(),?,NOW(),'2005-11-15', ?,?,?,?,?) ADDED THE
         * COLUMNS 7-2007, TBH ALTER TABLE dataset ADD COLUMN show_event_status
         * bool DEFAULT false; ALTER TABLE dataset ADD COLUMN
         * show_subject_status bool DEFAULT false; ALTER TABLE dataset ADD
         * COLUMN show_subject_unique_id bool DEFAULT false; ALTER TABLE dataset
         * ADD COLUMN show_subject_age_at_event bool DEFAULT false; ALTER TABLE
         * dataset ADD COLUMN show_crf_status bool DEFAULT false; ALTER TABLE
         * dataset ADD COLUMN show_crf_version bool DEFAULT false; ALTER TABLE
         * dataset ADD COLUMN show_crf_int_name bool DEFAULT false; ALTER TABLE
         * dataset ADD COLUMN show_crf_int_date bool DEFAULT false; ALTER TABLE
         * dataset ADD COLUMN show_group_info bool DEFAULT false; ALTER TABLE
         * dataset ADD COLUMN show_disc_info bool DEFAULT false; added table
         * mapping dataset id to study group classes id, tbh
         *
         */
        DatasetBean db = (DatasetBean) eb;
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap nullVars = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(db.getStudyId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(db.getStatus().getId()));
        variables.put(Integer.valueOf(3), db.getName());
        variables.put(Integer.valueOf(4), db.getDescription());
        variables.put(Integer.valueOf(5), db.getSQLStatement());
        variables.put(Integer.valueOf(6), Integer.valueOf(db.getOwnerId()));
        variables.put(Integer.valueOf(7), Integer.valueOf(db.getNumRuns()));
        variables.put(Integer.valueOf(8), new Boolean(db.isShowEventLocation()));
        variables.put(Integer.valueOf(9), new Boolean(db.isShowEventStart()));
        variables.put(Integer.valueOf(10), new Boolean(db.isShowEventEnd()));
        variables.put(Integer.valueOf(11), new Boolean(db.isShowSubjectDob()));
        variables.put(Integer.valueOf(12), new Boolean(db.isShowSubjectGender()));
        variables.put(Integer.valueOf(13), new Boolean(db.isShowEventStatus()));
        variables.put(Integer.valueOf(14), new Boolean(db.isShowSubjectStatus()));
        variables.put(Integer.valueOf(15), new Boolean(db.isShowSubjectUniqueIdentifier()));
        variables.put(Integer.valueOf(16), new Boolean(db.isShowSubjectAgeAtEvent()));
        variables.put(Integer.valueOf(17), new Boolean(db.isShowCRFstatus()));
        variables.put(Integer.valueOf(18), new Boolean(db.isShowCRFversion()));
        variables.put(Integer.valueOf(19), new Boolean(db.isShowCRFinterviewerName()));
        variables.put(Integer.valueOf(20), new Boolean(db.isShowCRFinterviewerDate()));
        variables.put(Integer.valueOf(21), new Boolean(db.isShowSubjectGroupInformation()));
        // variables.put(Integer.valueOf(22), new
        // Boolean(db.isShowDiscrepancyInformation()));
        variables.put(Integer.valueOf(22), new Boolean(false));
        // currently not changing structure to allow for disc notes to be added
        // in the future
        variables.put(Integer.valueOf(23), db.getODMMetaDataVersionName());
        variables.put(Integer.valueOf(24), db.getODMMetaDataVersionOid());
        variables.put(Integer.valueOf(25), db.getODMPriorStudyOid());
        variables.put(Integer.valueOf(26), db.getODMPriorMetaDataVersionOid());
        variables.put(Integer.valueOf(27), db.isShowSubjectSecondaryId());
        variables.put(Integer.valueOf(28), db.getDatasetItemStatus().getId());

        this.executeWithPK(digester.getQuery("create"), variables, nullVars);

        // logger.warn("**************************************************");
        // logger.warn("just created dataset bean: "+
        // "show event status "+db.isShowEventStatus()+
        // "show subject age at event "+db.isShowSubjectAgeAtEvent()+
        // "show group info "+db.isShowGroupInformation()+
        // "show disc info "+db.isShowDiscrepancyInformation());
        // logger.warn("**************************************************");

        if (isQuerySuccessful()) {
            eb.setId(getLatestPK());
            if (db.isShowSubjectGroupInformation()) {
                // add additional information here
                for (int i = 0; i < db.getSubjectGroupIds().size(); i++) {
                    createGroupMap(eb.getId(), ((Integer) db.getSubjectGroupIds().get(i)).intValue(), nullVars);
                }
            }
        }
        return eb;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        DatasetBean eb = new DatasetBean();
        this.setEntityAuditInformation(eb, hm);
        eb.setDescription((String) hm.get("description"));
        eb.setStudyId(((Integer) hm.get("study_id")).intValue());
        eb.setName((String) hm.get("name"));
        eb.setId(((Integer) hm.get("dataset_id")).intValue());
        eb.setSQLStatement((String) hm.get("sql_statement"));
        eb.setNumRuns(((Integer) hm.get("num_runs")).intValue());
        eb.setDateStart((Date) hm.get("date_start"));
        eb.setDateEnd((Date) hm.get("date_end"));
        eb.setApproverId(((Integer) hm.get("approver_id")).intValue());
        eb.setDateLastRun((Date) hm.get("date_last_run"));
        eb.setShowEventEnd(((Boolean) hm.get("show_event_end")).booleanValue());
        eb.setShowEventStart(((Boolean) hm.get("show_event_start")).booleanValue());
        eb.setShowEventLocation(((Boolean) hm.get("show_event_location")).booleanValue());
        eb.setShowSubjectDob(((Boolean) hm.get("show_subject_dob")).booleanValue());
        eb.setShowSubjectGender(((Boolean) hm.get("show_subject_gender")).booleanValue());
        eb.setShowEventStatus(((Boolean) hm.get("show_event_status")).booleanValue());
        eb.setShowSubjectStatus(((Boolean) hm.get("show_subject_status")).booleanValue());
        eb.setShowSubjectUniqueIdentifier(((Boolean) hm.get("show_subject_unique_id")).booleanValue());
        eb.setShowSubjectAgeAtEvent(((Boolean) hm.get("show_subject_age_at_event")).booleanValue());
        eb.setShowCRFstatus(((Boolean) hm.get("show_crf_status")).booleanValue());
        eb.setShowCRFversion(((Boolean) hm.get("show_crf_version")).booleanValue());
        eb.setShowCRFinterviewerName(((Boolean) hm.get("show_crf_int_name")).booleanValue());
        eb.setShowCRFinterviewerDate(((Boolean) hm.get("show_crf_int_date")).booleanValue());
        eb.setShowSubjectGroupInformation(((Boolean) hm.get("show_group_info")).booleanValue());
        // eb.setShowDiscrepancyInformation(((Boolean)
        // hm.get("show_disc_info")).booleanValue());
        // do we want to find group info here? looks like the best place for
        // non-repeats...
        // if (eb.isShowSubjectGroupInformation()) {
        eb.setSubjectGroupIds(getGroupIds(eb.getId()));
        // }
        eb.setODMMetaDataVersionName((String) hm.get("odm_metadataversion_name"));
        eb.setODMMetaDataVersionOid((String) hm.get("odm_metadataversion_oid"));
        eb.setODMPriorStudyOid((String) hm.get("odm_prior_study_oid"));
        eb.setODMPriorMetaDataVersionOid((String) hm.get("odm_prior_metadataversion_oid"));
        eb.setShowSubjectSecondaryId((Boolean) hm.get("show_secondary_id"));
        int isId = ((Integer) hm.get("dataset_item_status_id")).intValue();
        isId = isId > 0 ? isId : 1;
        DatasetItemStatus dis = DatasetItemStatus.get(isId);
        eb.setDatasetItemStatus(dis);
        return eb;
    }

    private ArrayList getGroupIds(int datasetId) {
        ArrayList<Integer> groupIds = new ArrayList<Integer>();
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// dataset id
        this.setTypeExpected(2, TypeNames.INT);// subject group id
        HashMap<Integer, Integer> variablesNew = new HashMap<Integer, Integer>();
        variablesNew.put(Integer.valueOf(1), Integer.valueOf(datasetId));
        ArrayList alist = this.select(digester.getQuery("findAllGroups"), variablesNew);
        // convert them to ids for the array list, tbh
        // the above is an array list of hashmaps, each hash map being a row in
        // the DB
        for (Iterator iter = alist.iterator(); iter.hasNext();) {
            HashMap row = (HashMap) iter.next();
            Integer id = (Integer) row.get("study_group_class_id");
            groupIds.add(id);
        }
        return groupIds;
    }

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            DatasetBean eb = (DatasetBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAllOrderByStudyIdAndName() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAllOrderByStudyIdAndName"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            DatasetBean eb = (DatasetBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findTopFive(StudyBean currentStudy) {
        int studyId = currentStudy.getId();
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(studyId));
        variables.put(Integer.valueOf(2), Integer.valueOf(studyId));
        ArrayList alist = this.select(digester.getQuery("findTopFive"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            DatasetBean eb = (DatasetBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    /**
     * find by owner id, reports a list of datasets by user account id.
     *
     * @param ownerId
     *            studyId
     */

    public Collection findByOwnerId(int ownerId, int studyId) {
        // TODO add an findbyadminownerid?
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(studyId));
        variables.put(Integer.valueOf(2), Integer.valueOf(studyId));
        variables.put(Integer.valueOf(3), Integer.valueOf(ownerId));

        ArrayList alist = this.select(digester.getQuery("findByOwnerId"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            DatasetBean eb = (DatasetBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public EntityBean findByPK(int ID) {
        DatasetBean eb = new DatasetBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (DatasetBean) this.getEntityFromHashMap((HashMap) it.next());
        } else {
            logger.warn("found no object: " + sql + " " + ID);
        }
        return eb;
    }

    /**
     *
     * @param name
     * @return
     */
    public EntityBean findByNameAndStudy(String name, StudyBean study) {
        DatasetBean eb = new DatasetBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), name);
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        String sql = digester.getQuery("findByNameAndStudy");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (DatasetBean) this.getEntityFromHashMap((HashMap) it.next());
        } else {
            logger.warn("found no object: " + sql + " " + name);
        }
        return eb;
    }

    /**
     * Implements the Data Algorithm described in Dataset Export Algorithms,
     * stores output in the returned ExtractBean.
     *
     * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION - add Study
     *
     * @param eb
     *            The ExtractBean containing the dataset and study for which
     *            data is being retrieved.
     * @return An ExtractBean containing structured data stored by subject,
     *         study event definition, ordinal, CRF and item, as well as the
     *         maximum ordinal per study event definition.
     *
     *
     */
    public ExtractBean getDatasetData(ExtractBean eb, int currentstudyid, int parentstudyid) {

        /**
         * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION replace with a new
         *      algorithm
         */

        String sql = eb.getDataset().getSQLStatement();

        /**
         * I. First step to get the event_definition_id and item_id
         */

        // String parseSQLDataset(String sql, boolean issed, boolean
        // hasfilterzero)
        // get the study_event_definition_id IN (1,2,3)
        String st_sed_in = parseSQLDataset(sql, true, true);
        String st_itemid_in = parseSQLDataset(sql, false, true);

        /**
         * The next step get the string (study_subj_1, study_subj_2,...) for SQL
         * IN NOTE: This is not the final list of stusy_subject_id that will be
         * displayed!! It is only to help extract data faster with IN (...)
         * instead of SELECT subquerry
         */
        //
        // String st_studysubjectid_in =
        // getINStringInitStudySubjectIDs(currentstudyid, parentstudyid,
        // st_sed_in);
        /**
         * Get the final string of event_crf_id that will be used in other SQL
         * as IN (value1, value2,...)
         */
        // String st_eventcrfid_in = getINStringEventCRFIDs(currentstudyid,
        // parentstudyid, st_sed_in, st_studysubjectid_in);
        /**
         * get the study subjects; to each study subject it associates the data
         * from the subjects themselves
         */
        int datasetItemStatusId = eb.getDataset().getDatasetItemStatus().getId();
        String ecStatusConstraint = this.getECStatusConstraint(datasetItemStatusId);
        String itStatusConstraint = this.getItemDataStatusConstraint(datasetItemStatusId);
        ArrayList newRows =
            selectStudySubjects(currentstudyid, parentstudyid, st_sed_in, st_itemid_in, this.genDatabaseDateConstraint(eb), ecStatusConstraint,
                    itStatusConstraint);
        /**
         * Add it to ths subjects
         */
        eb.addStudySubjectData(newRows);

        /**
         * II. Add the study_event records
         */

        /**
         * Add InKeysHelper HashMap This is to speed up the function InKeys in
         * theh ExtractBean protected boolean inKeys(int sedInd, int
         * sampleOrdinal, int crfInd, int itemInd, String groupName) {
         *
         * This loop inside InKeys that has a major performance problem is
         * replaced:
         *
         * for (Iterator iter = data.entrySet().iterator(); iter.hasNext();) {
         * String key = (String) ((java.util.Map.Entry) iter.next()).getKey() +
         * "_Ungrouped"; String testKey = getDataKey(0, currentDef.getId(),
         * sampleOrdinal, currentCRF.getId(), currentItem.getId(), 1,
         * groupName).substring(2); testKey = groupName.equals("Ungrouped") ?
         * testKey + "_Ungrouped" : testKey; if (key.contains(testKey)) { return
         * true; } }
         *
         *
         */

        // setHashMapInKeysHelper(String sedin, String itin, int studyid, int
        // parentid)
        HashMap nhInHelpKeys =
            setHashMapInKeysHelper(currentstudyid, parentstudyid, st_sed_in, st_itemid_in, this.genDatabaseDateConstraint(eb), ecStatusConstraint,
                    itStatusConstraint);
        eb.setHmInKeys(nhInHelpKeys);

        /**
         * Get the arrays of ArrayList for SQL BASE There are split in two
         * querries for perfomance
         */
        eb.resetArrayListEntryBASE_ITEMGROUPSIDE();
        loadBASE_EVENTINSIDEHashMap(currentstudyid, parentstudyid, st_sed_in, st_itemid_in, eb);
        loadBASE_ITEMGROUPSIDEHashMap(currentstudyid, parentstudyid, st_sed_in, st_itemid_in, eb);

        /**
         * add study_event data
         */
        eb.addStudyEventData();

        /**
         * add item_data
         */

        eb.addItemData();

        return eb;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

    public ArrayList findAllByStudyId(int studyId) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(studyId));
        variables.put(Integer.valueOf(2), Integer.valueOf(studyId));

        return executeFindAllQuery("findAllByStudyId", variables);
    }

    public ArrayList findAllByStudyIdAdmin(int studyId) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(studyId));
        variables.put(Integer.valueOf(2), Integer.valueOf(studyId));

        return executeFindAllQuery("findAllByStudyIdAdmin", variables);
    }

    /**
     * Initialize itemMap, itemIds, itemDefCrf and groupIds for a DatasetBean
     *
     * @param db
     * @return
     * @author ywang (Feb., 2008)
     */
    public DatasetBean initialDatasetData(int datasetId) {
        ItemDAO idao = new ItemDAO(ds);
        DatasetBean db = (DatasetBean) findByPK(datasetId);
        String sql = db.getSQLStatement();
        sql = sql.split("study_event_definition_id in")[1];
        String[] ss = sql.split("and item_id in");
        String sedIds = ss[0];
        String[] sss = ss[1].split("and");
        String itemIds = sss[0];

        this.setDefinitionCrfItemTypesExpected();
        logger.debug("begin to execute GetDefinitionCrfItemSql");
        ArrayList alist = select(getDefinitionCrfItemSql(sedIds, itemIds));
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap row = (HashMap) it.next();
            ItemBean ib = (ItemBean) idao.getEntityFromHashMap(row);
            Integer defId = (Integer) row.get("sed_id");
            String defName = (String) row.get("sed_name");
            String crfName = (String) row.get("crf_name");
            Integer itemId = ib.getId();
            String key = defId + "_" + itemId;
            if (!db.getItemMap().containsKey(key)) {
                ib.setSelected(true);
                ib.setDefName(defName);
                ib.setCrfName(crfName);
                ib.setDatasetItemMapKey(key);
                // YW 2-22-2008, CreateDatasetServlet.java shows that eventIds
                // contains study_event_definition_ids
                if (!db.getEventIds().contains(defId)) {
                    db.getEventIds().add(defId);
                }
                db.getItemIds().add(itemId);
                db.getItemDefCrf().add(ib);
                db.getItemMap().put(key, ib);
            }
        }
        db.setSubjectGroupIds(getGroupIds(db.getId()));
        return db;
    }

    protected String getDefinitionCrfItemSql(String sedIds, String itemIds) {
        return "select item.*, sed.study_event_definition_id as sed_id, sed.name as sed_name, crf.crf_id, crf.name as crf_name"
            + " from study_event_definition sed, event_definition_crf edc, crf, crf_version cv,item_form_metadata ifm, item"
            + " where sed.study_event_definition_id in " + sedIds + " and item.item_id in " + itemIds
            + " and sed.study_event_definition_id = edc.study_event_definition_id and edc.crf_id = crf.crf_id"
            + " and crf.crf_id = cv.crf_id and cv.crf_version_id = ifm.crf_version_id and ifm.item_id = item.item_id";
    }

    /**
     * Update all columns of the dataset table except owner_id
     *
     * @param eb
     * @return
     *
     * @author ywang (Feb., 2008)
     */
    public EntityBean updateAll(EntityBean eb) {
        eb.setActive(false);
        DatasetBean db = (DatasetBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(db.getStudyId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(db.getStatus().getId()));
        variables.put(Integer.valueOf(3), db.getName());
        variables.put(Integer.valueOf(4), db.getDescription());
        variables.put(Integer.valueOf(5), db.getSQLStatement());
        variables.put(Integer.valueOf(6), db.getDateLastRun());
        variables.put(Integer.valueOf(7), Integer.valueOf(db.getNumRuns()));

        variables.put(Integer.valueOf(8), Integer.valueOf(db.getUpdaterId()));
        if (db.getApproverId() <= 0) {
            // nullVars.put(Integer.valueOf(9), null);
            // ABOVE IS WRONG; follow the example below:
            nullVars.put(Integer.valueOf(9), Integer.valueOf(Types.NUMERIC));
            variables.put(Integer.valueOf(9), null);
        } else {
            variables.put(Integer.valueOf(9), Integer.valueOf(db.getApproverId()));
        }

        variables.put(Integer.valueOf(10), db.getDateStart());
        variables.put(Integer.valueOf(11), db.getDateEnd());
        variables.put(Integer.valueOf(12), new Boolean(db.isShowEventLocation()));
        variables.put(Integer.valueOf(13), new Boolean(db.isShowEventStart()));
        variables.put(Integer.valueOf(14), new Boolean(db.isShowEventEnd()));
        variables.put(Integer.valueOf(15), new Boolean(db.isShowSubjectDob()));
        variables.put(Integer.valueOf(16), new Boolean(db.isShowSubjectGender()));
        variables.put(Integer.valueOf(17), new Boolean(db.isShowEventStatus()));
        variables.put(Integer.valueOf(18), new Boolean(db.isShowSubjectStatus()));
        variables.put(Integer.valueOf(19), new Boolean(db.isShowSubjectUniqueIdentifier()));
        variables.put(Integer.valueOf(20), new Boolean(db.isShowSubjectAgeAtEvent()));
        variables.put(Integer.valueOf(21), new Boolean(db.isShowCRFstatus()));
        variables.put(Integer.valueOf(22), new Boolean(db.isShowCRFversion()));
        variables.put(Integer.valueOf(23), new Boolean(db.isShowCRFinterviewerName()));
        variables.put(Integer.valueOf(24), new Boolean(db.isShowCRFinterviewerDate()));
        variables.put(Integer.valueOf(25), new Boolean(db.isShowSubjectGroupInformation()));
        variables.put(Integer.valueOf(26), new Boolean(false));
        variables.put(Integer.valueOf(27), db.getODMMetaDataVersionName());
        variables.put(Integer.valueOf(28), db.getODMMetaDataVersionOid());
        variables.put(Integer.valueOf(29), db.getODMPriorStudyOid());
        variables.put(Integer.valueOf(30), db.getODMPriorMetaDataVersionOid());
        variables.put(Integer.valueOf(31), new Boolean(db.isShowSubjectSecondaryId()));
        variables.put(Integer.valueOf(32), Integer.valueOf(db.getDatasetItemStatus().getId()));
        variables.put(Integer.valueOf(33), Integer.valueOf(db.getId()));

        this.execute(digester.getQuery("updateAll"), variables, nullVars);
        if (isQuerySuccessful()) {
            eb.setActive(true);
        }
        return eb;
    }

    public EntityBean updateGroupMap(DatasetBean db) {
        HashMap nullVars = new HashMap();
        db.setActive(false);
        boolean success = true;

        ArrayList<Integer> sgcIds = this.getGroupIds(db.getId());
        if (sgcIds == null)
            sgcIds = new ArrayList<Integer>();
        ArrayList<Integer> dbSgcIds = (ArrayList<Integer>) db.getSubjectGroupIds().clone();
        if (dbSgcIds == null)
            dbSgcIds = new ArrayList<Integer>();
        if (sgcIds.size() > 0) {
            for (Integer id : sgcIds) {
                if (!dbSgcIds.contains(id)) {
                    removeGroupMap(db.getId(), id, nullVars);
                    if (!isQuerySuccessful())
                        success = false;
                } else {
                    dbSgcIds.remove(id);
                }
            }
        }
        if (success) {
            if (dbSgcIds.size() > 0) {
                for (Integer id : dbSgcIds) {
                    createGroupMap(db.getId(), id, nullVars);
                    if (!isQuerySuccessful())
                        success = false;
                }
            }
        }
        if (success) {
            db.setActive(true);
        }
        return db;
    }

    protected void createGroupMap(int datasetId, int studyGroupClassId, HashMap nullVars) {
        HashMap<Integer, Integer> variablesNew = new HashMap<Integer, Integer>();
        variablesNew.put(Integer.valueOf(1), Integer.valueOf(datasetId));
        Integer groupId = Integer.valueOf(studyGroupClassId);
        variablesNew.put(Integer.valueOf(2), groupId);
        this.execute(digester.getQuery("createGroupMap"), variablesNew, nullVars);
    }

    protected void removeGroupMap(int datasetId, int studyGroupClassId, HashMap nullVars) {
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(Integer.valueOf(1), Integer.valueOf(datasetId));
        Integer groupId = Integer.valueOf(studyGroupClassId);
        variables.put(Integer.valueOf(2), groupId);
        this.execute(digester.getQuery("removeGroupMap"), variables, nullVars);
    }

    /**
     *
     * @vbc 08/06/2008 NEW EXTRACT DATA IMPLEMENTATION parses the sql dataset
     *      and extract the two IN set of values - remove the value 0 from the
     *      study_event_definition_id - if ssed is true return the
     *      study_event_definition_id else return the item_id set
     */

    public String parseSQLDataset(String sql, boolean issed, boolean hasfilterzero)

    {
        // for instance:
        // select distinct * from extract_data_table
        // where study_event_definition_id in (3, 4)
        // and item_id in (53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 53, 54,
        // 55, 56, 57, 58, 59, 60, 61, 62, 63)
        // and (date(date_created) >= date('1900-01-01')) and
        // (date(date_created) <= date('2100-12-31'))

        int sedid_one = 0;
        int sedid_two = 0;
        int itid_one = 0;
        int itid_two = 0;
        String sed_st = "";
        String sed_stno = "";
        String it_st = "";
        String it_stno = "";
        Vector sedvec_tmp = new Vector();
        Vector sedvec = new Vector();
        Vector itvec = new Vector();

        // get the first
        sedid_one = sql.indexOf("(");
        sedid_two = sql.indexOf(")");
        if (sedid_one != -1 && sedid_two != -1) {
            // found - get the substring
            sed_st = sql.substring(sedid_one + 1, sedid_two);
            // parse it for values
            boolean hasmore = true;
            int no;
            do {
                // get to the first comma
                int ic = sed_st.indexOf(",");
                if (ic != -1) {
                    // found
                    sed_stno = sed_st.substring(0, ic);
                    // get into int
                    try {
                        no = Integer.parseInt(sed_stno.trim());
                        sedvec_tmp.add(Integer.valueOf(no));

                        // set the new string
                        sed_st = sed_st.substring(ic + 1, sed_st.length());

                    } catch (NumberFormatException nfe) {
                        // info("Exception when converted to Integer
                        // for:"+number);
                        // fall through
                    }// try

                } else {
                    // only one
                    try {
                        no = Integer.parseInt(sed_st.trim());

                        sedvec_tmp.add(Integer.valueOf(no));
                    } catch (NumberFormatException nfe) {
                        // info("Exception when converted to Integer
                        // for:"+number);
                        // fall through
                    }// try

                    hasmore = false;
                }

            } while (hasmore);

        } else {
            // ERROR
        }// if

        // get the second
        sql = sql.substring(sedid_two + 1, sql.length());
        itid_one = sql.indexOf("(");
        itid_two = sql.indexOf(")");
        if (itid_one != -1 && sedid_two != -1) {
            // found - get the substring
            it_st = sql.substring(itid_one + 1, itid_two);
            // parse it for values
            boolean hasmore = true;
            int no;
            do {
                // get to the first comma
                int ic = it_st.indexOf(",");
                if (ic != -1) {
                    // found
                    it_stno = it_st.substring(0, ic);
                    // get into int
                    try {
                        no = Integer.parseInt(it_stno.trim());
                        itvec.add(Integer.valueOf(no));

                        // set the new string
                        it_st = it_st.substring(ic + 1, it_st.length());

                    } catch (NumberFormatException nfe) {
                        // info("Exception when converted to Integer
                        // for:"+number);
                        // fall through
                    }// try

                } else {
                    // only one
                    try {
                        no = Integer.parseInt(it_st.trim());

                        itvec.add(Integer.valueOf(no));
                    } catch (NumberFormatException nfe) {
                        // info("Exception when converted to Integer
                        // for:"+number);
                        // fall through
                    }// try

                    hasmore = false;
                }

            } while (hasmore);

        } else {
            // ERROR
        }// if

        // Eliminate 0 from SED but only if
        if (hasfilterzero) {
            for (int i = 0; i < sedvec_tmp.size(); i++) {
                Integer itmp = (Integer) sedvec_tmp.get(i);
                if (itmp.intValue() != 0) {
                    sedvec.add(itmp);
                }
            }// for
        }// if
        int a = 0;
        a = 9;

        String stsed_in = "";
        for (int ij = 0; ij < sedvec.size(); ij++) {
            stsed_in = stsed_in + ((Integer) sedvec.get(ij)).toString();
            if (ij == sedvec.size() - 1) {
                // last
            } else {
                stsed_in = stsed_in + ",";
            }// if

        }// for

        String stit_in = "";
        for (int ij = 0; ij < itvec.size(); ij++) {
            stit_in = stit_in + ((Integer) itvec.get(ij)).toString();
            if (ij == itvec.size() - 1) {
                // last
            } else {
                stit_in = stit_in + ",";
            }// if

        }// for

        stsed_in = "(" + stsed_in + ")";
        stit_in = "(" + stit_in + ")";

        if (issed) {
            return stsed_in;
        } else {
            return stit_in;
        }//

    }

}
