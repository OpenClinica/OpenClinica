/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.managestudy;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.patterns.ocobserver.Listener;
import org.akaza.openclinica.patterns.ocobserver.Observer;
import org.akaza.openclinica.patterns.ocobserver.StudyEventBeanContainer;
import org.akaza.openclinica.patterns.ocobserver.StudyEventChangeDetails;
import org.akaza.openclinica.service.rule.StudyEventBeanListener;

/**
 * @author jxu
 *
 *         Modified by ywang.
 *
 */
public class StudyEventDAO extends AuditableEntityDAO implements Listener {
    
	
	private Observer observer;
	// private DAODigester digester;

	    private void setQueryNames() {
        findByPKAndStudyName = "findByPKAndStudy";
        getCurrentPKName = "getCurrentPrimaryKey";
    }

    public StudyEventDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public StudyEventDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public StudyEventDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDYEVENT;
    }

    @Override
    public void setTypesExpected() {
        // SERIAL NUMERIC NUMERIC VARCHAR(2000)
        // NUMERIC DATE DATE NUMERIC
        // NUMERIC DATE DATE NUMERIC
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.STRING);

        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.TIMESTAMP); // YW 08-17-2007,
        // date_start
        this.setTypeExpected(7, TypeNames.TIMESTAMP); // YW 08-17-2007,
        // date_end
        this.setTypeExpected(8, TypeNames.INT);

        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.DATE);
        this.setTypeExpected(11, TypeNames.DATE);
        this.setTypeExpected(12, TypeNames.INT);
        this.setTypeExpected(13, TypeNames.INT);
        // YW 08-17-2007 <<
        this.setTypeExpected(14, TypeNames.BOOL); // start_time_flag
        this.setTypeExpected(15, TypeNames.BOOL); // end_time_flag
        // YW >>

    }

    public void setTypesExpected(boolean withSubject) {
        // SERIAL NUMERIC NUMERIC VARCHAR(2000)
        // NUMERIC DATE DATE NUMERIC
        // NUMERIC DATE DATE NUMERIC
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.STRING);

        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.TIMESTAMP); // YW 08-17-2007,
        // date_start
        this.setTypeExpected(7, TypeNames.TIMESTAMP); // YW 08-17-2007,
        // date_end
        this.setTypeExpected(8, TypeNames.INT);

        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.DATE);
        this.setTypeExpected(11, TypeNames.DATE);
        this.setTypeExpected(12, TypeNames.INT);
        this.setTypeExpected(13, TypeNames.INT);
        // YW 08-17-2007 <<
        this.setTypeExpected(14, TypeNames.BOOL); // start_time_flag
        this.setTypeExpected(15, TypeNames.BOOL); // end_time_flag
        if (withSubject) {
            this.setTypeExpected(16, TypeNames.STRING);
        }
        // YW >>
    }

    public void setCRFTypesExpected() {
        /*
         * <sql>SELECT C.CRF_ID, C.STATUS_ID, C.NAME, C.DESCRIPTION,
         * V.CRF_VERSION_ID, V.NAME, V.REVISION_NOTES FROM CRF C, CRF_VERSION V,
         * EVENT_DEFINITION_CRF EDC WHERE C.CRF_ID = V.CRF_ID AND EDC.CRF_ID =
         * C.CRF_ID AND EDC.STUDY_EVENT_DEFINITION_ID =? </sql>
         */
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.STRING);
        this.setTypeExpected(7, TypeNames.STRING);
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public Object getEntityFromHashMap(HashMap hm) {
        StudyEventBean eb = new StudyEventBean();
        super.setEntityAuditInformation(eb, hm);
        // STUDY_EVENT_ID STUDY_EVENT_DEFINITION_ID SUBJECT_ID LOCATION
        // SAMPLE_ORDINAL DATE_START DATE_END OWNER_ID
        // STATUS_ID DATE_CREATED DATE_UPDATED UPDATE_ID
        eb.setId(((Integer) hm.get("study_event_id")).intValue());
        eb.setStudyEventDefinitionId(((Integer) hm.get("study_event_definition_id")).intValue());
        eb.setStudySubjectId(((Integer) hm.get("study_subject_id")).intValue());
        eb.setLocation((String) hm.get("location"));
        eb.setSampleOrdinal(((Integer) hm.get("sample_ordinal")).intValue());
        eb.setDateStarted((Date) hm.get("date_start"));
        eb.setDateEnded((Date) hm.get("date_end"));
        // eb.setStatus(eb.getStatus());
        int subjectEventStatuId = ((Integer) hm.get("subject_event_status_id")).intValue();
        eb.setSubjectEventStatus(SubjectEventStatus.get(subjectEventStatuId));
        // YW 08-17-2007
        eb.setStartTimeFlag((Boolean) hm.get("start_time_flag"));
        eb.setEndTimeFlag((Boolean) hm.get("end_time_flag"));

        return eb;
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public Object getEntityFromHashMap(HashMap hm, boolean withSubject) {
        StudyEventBean eb = new StudyEventBean();
        super.setEntityAuditInformation(eb, hm);
        // STUDY_EVENT_ID STUDY_EVENT_DEFINITION_ID SUBJECT_ID LOCATION
        // SAMPLE_ORDINAL DATE_START DATE_END OWNER_ID
        // STATUS_ID DATE_CREATED DATE_UPDATED UPDATE_ID
        eb.setId(((Integer) hm.get("study_event_id")).intValue());
        eb.setStudyEventDefinitionId(((Integer) hm.get("study_event_definition_id")).intValue());
        eb.setStudySubjectId(((Integer) hm.get("study_subject_id")).intValue());
        eb.setLocation((String) hm.get("location"));
        eb.setSampleOrdinal(((Integer) hm.get("sample_ordinal")).intValue());
        eb.setDateStarted((Date) hm.get("date_start"));
        eb.setDateEnded((Date) hm.get("date_end"));
        // eb.setStatus(eb.getStatus());
        int subjectEventStatuId = ((Integer) hm.get("subject_event_status_id")).intValue();
        eb.setSubjectEventStatus(SubjectEventStatus.get(subjectEventStatuId));
        // YW 08-17-2007
        eb.setStartTimeFlag((Boolean) hm.get("start_time_flag"));
        eb.setEndTimeFlag((Boolean) hm.get("end_time_flag"));
        if (withSubject) {
            eb.setStudySubjectLabel((String) hm.get("label"));
        }

        return eb;
    }

    // public HashMap getListOfStudyEvents()

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean eb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAllByDefinition(int definitionId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(definitionId));

        String sql = digester.getQuery("findAllByDefinition");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean eb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllByStudyEventDefinitionAndCrfOids(String studyEventDefinitionOid, String crfOrCrfVersionOid) {
        this.setTypesExpected(true);
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), studyEventDefinitionOid);
        variables.put(Integer.valueOf(2), crfOrCrfVersionOid);
        variables.put(Integer.valueOf(3), crfOrCrfVersionOid);

        String sql = digester.getQuery("findAllByStudyEventDefinitionAndCrfOids");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean eb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next(), true);
            al.add(eb);
        }
        return al;
    }

    public Integer getCountofEventsBasedOnEventStatus(StudyBean currentStudy, SubjectEventStatus subjectEventStatus) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), subjectEventStatus.getId());
        String sql = digester.getQuery("getCountofEventsBasedOnEventStatus");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getCountofEvents(StudyBean currentStudy) {
        // StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        String sql = digester.getQuery("getCountofEvents");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public StudyEventBean findAllByStudyEventDefinitionAndCrfOidsAndOrdinal(String studyEventDefinitionOid, String crfOrCrfVersionOid, String ordinal,
            String studySubjectId) {
        this.setTypesExpected(true);
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(Integer.valueOf(1), studyEventDefinitionOid);
        variables.put(Integer.valueOf(2), Integer.valueOf(studySubjectId));
        variables.put(Integer.valueOf(3), Integer.valueOf(ordinal));
        variables.put(Integer.valueOf(4), crfOrCrfVersionOid);
        variables.put(Integer.valueOf(5), crfOrCrfVersionOid);

        String sql = digester.getQuery("findAllByStudyEventDefinitionAndCrfOidsAndOrdinal");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean eb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next(), true);
            al.add(eb);
        }
        if (al.isEmpty()) {
            return null;
        }
        if (al.size() == 1) {
            return (StudyEventBean) al.get(0);
        } else {
            logger.warn("The query in findAllByStudyEventDefinitionAndCrfOidsAndOrdinal return a list of size {}. Business logic assumes only one", al.size());
            return (StudyEventBean) al.get(0);
        }
    }

    public ArrayList findAllWithSubjectLabelByDefinition(int definitionId) {
        this.setTypesExpected(true);
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(definitionId));

        String sql = digester.getQuery("findAllWithSubjectLabelByDefinition");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean eb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next(), true);
            al.add(eb);
        }
        return al;
    }

    // YW <<
    public ArrayList findAllWithSubjectLabelByStudySubjectAndDefinition(StudySubjectBean studySubject, int definitionId) {
        this.setTypesExpected(true);
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(studySubject.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(definitionId));

        String sql = digester.getQuery("findAllWithSubjectLabelByStudySubjectAndDefinition");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean eb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next(), true);
            eb.setStudySubject(studySubject);
            al.add(eb);
        }
        return al;
    }

    // YW 08-21-2007
    public EntityBean findByStudySubjectIdAndDefinitionIdAndOrdinal(int ssbid, int sedid, int ord) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(ssbid));
        variables.put(Integer.valueOf(2), Integer.valueOf(sedid));
        variables.put(Integer.valueOf(3), Integer.valueOf(ord));

        String sql = digester.getQuery("findByStudySubjectIdAndDefinitionIdAndOrdinal");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();
        StudyEventBean eb = new StudyEventBean();
        if (it.hasNext()) {
            eb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;
    }

    // YW >>

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public ArrayList findAllByDefinitionAndSubject(StudyEventDefinitionBean definition, StudySubjectBean subject) {
        ArrayList answer = new ArrayList();

        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(definition.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(subject.getId()));

        String sql = digester.getQuery("findAllByDefinitionAndSubject");

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            StudyEventBean studyEvent = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(studyEvent);
        }

        return answer;
    }

    public ArrayList findAllByDefinitionAndSubjectOrderByOrdinal(StudyEventDefinitionBean definition, StudySubjectBean subject) {
        ArrayList answer = new ArrayList();

        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(definition.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(subject.getId()));

        String sql = digester.getQuery("findAllByDefinitionAndSubjectOrderByOrdinal");

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            StudyEventBean studyEvent = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(studyEvent);
        }

        return answer;
    }

    public EntityBean findByPK(int ID) {
        StudyEventBean eb = new StudyEventBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }
    public EntityBean findByPKCached(int ID) {
        StudyEventBean eb = new StudyEventBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.selectByCache(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    /**
     * Creates a new studysubject
     */
    public EntityBean create(EntityBean eb) {
        return create(eb,false);
    }
    /**
     * Creates a new studysubject
     */
    public EntityBean create(EntityBean eb, boolean isTransaction) {
        StudyEventBean sb = (StudyEventBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        // INSERT INTO STUDY_EVENT
        // (STUDY_EVENT_DEFINITION_ID,SUBJECT_ID,LOCATION,SAMPLE_ORDINAL,
        // DATE_START,DATE_END,OWNER_ID,STATUS_ID,DATE_CREATED,subject_event_status_id
        // start_time_flag, end_time_flag)
        // VALUES (?,?,?,?,?,?,?,?,NOW())
        variables.put(Integer.valueOf(1), Integer.valueOf(sb.getStudyEventDefinitionId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(sb.getStudySubjectId()));
        variables.put(Integer.valueOf(3), sb.getLocation());
        variables.put(Integer.valueOf(4), Integer.valueOf(sb.getSampleOrdinal()));
        nullVars.put(3, TypeNames.STRING);
        if (sb.getDateStarted() == null) {
            // YW 08-16-2007 << data type changed from DATE to TIMESTAMP
            nullVars.put(Integer.valueOf(5), Integer.valueOf(TypeNames.TIMESTAMP));
            variables.put(Integer.valueOf(5), null);
        } else {
            // YW 08-16-2007 << data type changed from DATE to TIMESTAMP
            variables.put(Integer.valueOf(5), new Timestamp(sb.getDateStarted().getTime()));
        }
        if (sb.getDateEnded() == null) {
            // YW 08-16-2007 << data type changed from DATE to TIMESTAMP
            nullVars.put(Integer.valueOf(6), Integer.valueOf(TypeNames.TIMESTAMP));
            variables.put(Integer.valueOf(6), null);
        } else {
            // YW 08-16-2007 << data type changed from DATE to TIMESTAMP
            variables.put(Integer.valueOf(6), new Timestamp(sb.getDateEnded().getTime()));
        }
        variables.put(Integer.valueOf(7), Integer.valueOf(sb.getOwner().getId()));
        variables.put(Integer.valueOf(8), Integer.valueOf(sb.getStatus().getId()));
        variables.put(Integer.valueOf(9), Integer.valueOf(sb.getSubjectEventStatus().getId()));
        variables.put(Integer.valueOf(10), sb.getStartTimeFlag());
        variables.put(Integer.valueOf(11), sb.getEndTimeFlag());

        this.executeWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            sb.setId(getLatestPK());
        }
        
        StudyEventChangeDetails changeDetails = new StudyEventChangeDetails(true,true);
        changeDetails.setRunningInTransaction(isTransaction);
        StudyEventBeanContainer container = new StudyEventBeanContainer(sb,changeDetails);
        notifyObservers(container);
        return sb;
    }

    /**
     * Updates a Study event
     */
    public EntityBean update(EntityBean eb) {
        return update(eb,false);
    }
    
    /**
     * Updates a Study event
     */
    public EntityBean update(EntityBean eb, boolean isTransaction) {
    	 Connection con = null;
    	 return update( eb, con, isTransaction);
    }
    
    public EntityBean update(EntityBean eb, Connection con) {
        return update(eb,con,false);
    }
    /* this function allows to run transactional updates for an action*/
    
    public EntityBean update(EntityBean eb, Connection con, boolean isTransaction) {
        StudyEventBean sb = (StudyEventBean) eb;
        StudyEventBean oldStudyEventBean = (StudyEventBean)findByPK(sb.getId());
        HashMap nullVars = new HashMap();
        HashMap variables = new HashMap();
        // UPDATE study_event SET
        // STUDY_EVENT_DEFINITION_ID=?,SUBJECT_ID=?,LOCATION=?,
        // SAMPLE_ORDINAL=?, DATE_START=?,DATE_END=?,STATUS_ID=?,DATE_UPDATED=?,
        // UPDATE_ID=?, subject_event_status_id=?, end_time_flag=? WHERE
        // STUDY_EVENT_ID=?

        sb.setActive(false);

        variables.put(Integer.valueOf(1), Integer.valueOf(sb.getStudyEventDefinitionId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(sb.getStudySubjectId()));
        variables.put(Integer.valueOf(3), sb.getLocation());
        variables.put(Integer.valueOf(4), Integer.valueOf(sb.getSampleOrdinal()));
        // YW 08-17-2007, data type changed from DATE to TIMESTAMP
        variables.put(Integer.valueOf(5), new Timestamp(sb.getDateStarted().getTime()));
        if (sb.getDateEnded() == null) {
            nullVars.put(Integer.valueOf(6), Integer.valueOf(TypeNames.TIMESTAMP));
            variables.put(Integer.valueOf(6), null);
        } else {
            variables.put(Integer.valueOf(6), new Timestamp(sb.getDateEnded().getTime()));
        }
        variables.put(Integer.valueOf(7), Integer.valueOf(sb.getStatus().getId()));
        // changing date_updated from java.util.Date() into postgres now() statement
       // variables.put(Integer.valueOf(8), new java.util.Date());// DATE_Updated
        variables.put(Integer.valueOf(8), Integer.valueOf(sb.getUpdater().getId()));
        variables.put(Integer.valueOf(9), Integer.valueOf(sb.getSubjectEventStatus().getId()));
        variables.put(Integer.valueOf(10), sb.getStartTimeFlag()); // YW
        // 08-17-2007,
        // start_time_flag
        variables.put(Integer.valueOf(11), sb.getEndTimeFlag()); // YW
        // 08-17-2007,
        // end_time_flag
        variables.put(Integer.valueOf(12), Integer.valueOf(sb.getId()));

        String sql = digester.getQuery("update");
        if ( con == null){
        	this.execute(sql, variables, nullVars);
        }else{
        	this.execute(sql, variables, nullVars, con);
        }
        
        if (isQuerySuccessful()) {
            sb.setActive(true);
        }

        StudyEventChangeDetails changeDetails = new StudyEventChangeDetails();
        if (oldStudyEventBean.getDateStarted().compareTo(sb.getDateStarted()) != 0)
        	changeDetails.setStartDateChanged(true);
        if (oldStudyEventBean.getSubjectEventStatus().getId() != sb.getSubjectEventStatus().getId()) 
        	changeDetails.setStatusChanged(true);
        changeDetails.setRunningInTransaction(isTransaction);
        StudyEventBeanContainer container = new StudyEventBeanContainer(sb,changeDetails);
       notifyObservers(container);
        
        return sb;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

    @Override
    public int getCurrentPK() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        int pk = 0;
        ArrayList al = select(digester.getQuery("getCurrentPrimaryKey"));

        if (al.size() > 0) {
            HashMap h = (HashMap) al.get(0);
            pk = ((Integer) h.get("key")).intValue();
        }

        return pk;
    }

    public ArrayList findAllByStudyAndStudySubjectId(StudyBean study, int studySubjectId) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(studySubjectId));

        ArrayList alist = this.select(digester.getQuery("findAllByStudyAndStudySubjectId"), variables);

        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean seb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(seb);
        }

        return answer;
    }

    public ArrayList findAllByStudyAndEventDefinitionId(StudyBean study, int eventDefinitionId) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(eventDefinitionId));

        ArrayList alist = this.select(digester.getQuery("findAllByStudyAndEventDefinitionId"), variables);

        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean seb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(seb);
        }

        return answer;
    }

    /**
     * Get the maximum sample ordinal over all study events for the provided
     * StudyEventDefinition / StudySubject combination. Note that the maximum
     * may be zero but must be non-negative.
     *
     * @param sedb
     *            The study event definition whose ordinal we're looking for.
     * @param studySubject
     *            The study subject whose ordinal we're looking for.
     * @return The maximum sample ordinal over all study events for the provided
     *         combination, or 0 if no such combination exists.
     */
    public int getMaxSampleOrdinal(StudyEventDefinitionBean sedb, StudySubjectBean studySubject) {
        ArrayList answer = new ArrayList();

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(sedb.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(studySubject.getId()));

        ArrayList alist = this.select(digester.getQuery("getMaxSampleOrdinal"), variables);
        Iterator it = alist.iterator();
        if (it.hasNext()) {
            try {
                HashMap hm = (HashMap) it.next();
                Integer max = (Integer) hm.get("max_ord");
                return max.intValue();
            } catch (Exception e) {
            }
        }

        return 0;
    }

    /**
     * Get the maximum sample ordinal over all study events for the provided
     * StudyEventDefinition / StudySubject combination. Note that the maximum
     * may be zero but must be non-negative.
     *
     * @param sedbId
     *            The study event definition id whose ordinal we're looking for.
     * @param studySubjectId
     *            The study subject whose ordinal we're looking for.
     * @return The maximum sample ordinal over all study events for the provided
     *         combination, or 0 if no such combination exists.
     */
    public int getMaxSampleOrdinal(
	int sedbId,
	int studySubjectId) {
        ArrayList answer = new ArrayList();

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(sedbId));
        variables.put(Integer.valueOf(2), Integer.valueOf(studySubjectId));

        ArrayList alist = this.select(digester.getQuery("getMaxSampleOrdinal"), variables);
        Iterator it = alist.iterator();
        if (it.hasNext()) {
            try {
                HashMap hm = (HashMap) it.next();
                Integer max = (Integer) hm.get("max_ord");
                return max.intValue();
            } catch (Exception e) {
            }
        }

        return 0;
    }

    @Override
    public ArrayList findAllByStudy(StudyBean study) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));

        ArrayList alist = this.select(digester.getQuery("findAllByStudy"), variables);

        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean seb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(seb);
        }

        return answer;
    }

    /**
     * @deprecated
     * @param subjectId
     * @param studyId
     */
    @Deprecated
    public ArrayList findAllBySubjectAndStudy(int subjectId, int studyId) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(subjectId));
        variables.put(Integer.valueOf(2), Integer.valueOf(studyId));
        variables.put(Integer.valueOf(3), Integer.valueOf(studyId));

        ArrayList alist = this.select(digester.getQuery("findAllBySubjectAndStudy"), variables);

        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean seb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(seb);
        }

        return answer;

    }

    public ArrayList findAllBySubjectId(int subjectId) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(subjectId));

        ArrayList alist = this.select(digester.getQuery("findAllBySubjectId"), variables);

        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean seb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(seb);
        }

        return answer;

    }

    public ArrayList findAllBySubjectIdOrdered(int subjectId) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(subjectId));

        ArrayList alist = this.select(digester.getQuery("findAllBySubjectIdOrdered"), variables);

        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventBean seb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(seb);
        }

        return answer;

    }

    public void setNewCRFTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.STRING);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.STRING);

    }

    /**
     * Using the HashMaps returned from a <code>select</code> call in
     * findCRFsByStudy, prepare a HashMap whose keys are study event definitions
     * and whose values are ArrayLists of CRF versions included in those
     * definitions.
     *
     * @param rows
     *            The HashMaps retured by the <code>select</code> call in
     *            findCRFsByStudy.
     * @return a HashMap whose keys are study event definitions and whose values
     *         are ArrayLists of CRF versions included in those definitions.
     *         Both the keys of the HashMap and the elements of the ArrayLists
     *         are actually EntitBeans.
     */
    public HashMap getEventsAndMultipleCRFVersionInformation(ArrayList rows) {
        HashMap returnMe = new HashMap();
        Iterator it = rows.iterator();
        EntityBean event = new EntityBean();
        EntityBean crf = new EntityBean();
        EntityBean version = new EntityBean();
        while (it.hasNext()) {
            HashMap answers = (HashMap) it.next();

            // removed setActive since the setId calls automatically result in
            // setActive calls
            event = new EntityBean();
            event.setName((String) answers.get("sed_name"));
            event.setId(((Integer) answers.get("study_event_definition_id")).intValue());

            crf = new EntityBean();
            crf.setName((String) answers.get("crf_name") + " " + (String) answers.get("ver_name"));
            crf.setId(((Integer) answers.get("crf_version_id")).intValue());

            ArrayList crfs = new ArrayList();
            if (this.findDouble(returnMe, event)) {// (returnMe.containsKey(event))
                // {
                // TODO create custom checker, this does not work
                // logger.warn("putting a crf into an OLD event: " +
                // crf.getName() + " into "
                // + event.getName());
                // logger.warn("just entered the if statement");
                crfs = this.returnDouble(returnMe, event);// (ArrayList)
                // returnMe.get(event);
                // logger.warn("just got the array list from the hashmap");
                crfs.add(crf);
                // logger.warn("just added the crf to the array list");
                returnMe = this.removeDouble(returnMe, event);
                // .remove(event);
                // not sure the above will work, tbh
                returnMe.put(event, crfs);
            } else {
                crfs = new ArrayList();
                logger.warn("put a crf into a NEW event: " + crf.getName() + " into " + event.getName());
                crfs.add(crf);
                returnMe.put(event, crfs);// maybe combine the two crf +
                // version?
            }
        }// end of cycling through answers

        return returnMe;
    }

    // TODO: decide whether to use getEventsAndMultipleCRFVersionInformation
    // instead of this method
    public HashMap getEventAndCRFVersionInformation(ArrayList al) {
        HashMap returnMe = new HashMap();
        Iterator it = al.iterator();
        EntityBean event = new EntityBean();
        EntityBean crf = new EntityBean();
        EntityBean version = new EntityBean();
        while (it.hasNext()) {
            HashMap answers = (HashMap) it.next();
            logger.warn("***Study Event Def ID: " + answers.get("study_event_definition_id"));
            logger.warn("***CRF ID: " + answers.get("crf_id"));
            logger.warn("***CRFVersion ID: " + answers.get("crf_version_id"));
            event = new EntityBean();
            event.setActive(true);
            event.setName((String) answers.get("sed_name"));
            event.setId(((Integer) answers.get("study_event_definition_id")).intValue());
            crf = new EntityBean();
            crf.setActive(true);
            crf.setName((String) answers.get("crf_name") + " " + (String) answers.get("ver_name"));
            crf.setId(((Integer) answers.get("crf_version_id")).intValue());
            returnMe.put(event, crf);// maybe combine the two crf + version?
        }// end of cycling through answers

        return returnMe;
    }

    // TODO: decide whether to use SQL below in place of other sql. they're
    // pretty
    // similar
    /*
     * ssachs - this is meant for use with
     * getEventsAndMultipleCRFVersionInformation; in particular the column names
     * "study_event_name", "crf_name" and "crf_version_name" should be
     * maintained if the SQL changes SELECT SED.name AS study_event_name ,
     * SED.study_event_definition_id , C.name AS crf_name , CV.name AS
     * crf_version_name , CV.crf_version_id FROM study_event_definition SED ,
     * event_definition_crf EDC , crf C , crf_version CV WHERE SED.study_id = ?
     * AND SED.study_event_definition_id = EDC.study_event_definition_id AND
     * C.crf_id = EDC.crf_id AND C.crf_id = CV.crf_id
     */

    public HashMap findCRFsByStudy(StudyBean sb) {
        // SELECT DISTINCT
        // C.CRF_ID
        // , C.NAME AS CRF_NAME
        // , C.DESCRIPTION
        // , V.CRF_VERSION_ID
        // , V.NAME AS VER_NAME
        // , V.REVISION_NOTES
        // , SED.STUDY_EVENT_DEFINITION_ID
        // , SED.NAME AS SED_NAME
        // FROM
        // CRF C
        // , CRF_VERSION V
        // , EVENT_DEFINITION_CRF EDC
        // , STUDY_EVENT_DEFINITION SED
        // WHERE
        // C.CRF_ID = V.CRF_ID
        // AND EDC.CRF_ID = C.CRF_ID
        // AND EDC.STUDY_EVENT_DEFINITION_ID = SED.STUDY_EVENT_DEFINITION_ID
        // AND SED.STATUS_ID = 1
        // AND SED.STUDY_ID = ?
        // ORDER BY C.CRF_ID, V.CRF_VERSION_ID

        HashMap crfs = new HashMap();
        this.setNewCRFTypesExpected();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(sb.getId()));
        ArrayList alist = this.select(digester.getQuery("findCRFsByStudy"), variables);
        // TODO make sure this other statement for eliciting crfs works, tbh
        // switched from getEventAndCRFVersionInformation
        // to getEventsAndMultipleCRFVersionInformation
        // crfs = this.getEventAndCRFVersionInformation(alist);
        crfs = this.getEventsAndMultipleCRFVersionInformation(alist);
        return crfs;
    }

    public HashMap findCRFsByStudyEvent(StudyEventBean seb) {
        // Soon-to-be-depreciated, replaced by find crfs by study, tbh 11-26
        // returns a hashmap of crfs + arraylist of crfversions,
        // for creating a checkbox list of crf versions all collected by
        // the study event primary key, tbh
        HashMap crfs = new HashMap();
        this.setCRFTypesExpected();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(seb.getStudyEventDefinitionId()));
        ArrayList alist = this.select(digester.getQuery("findCRFsByStudyEvent"), variables);
        Iterator it = alist.iterator();
        CRFDAO cdao = new CRFDAO(this.ds);
        CRFVersionDAO cvdao = new CRFVersionDAO(this.ds);
        while (it.hasNext()) {
            HashMap answers = (HashMap) it.next();
            logger.warn("***First CRF ID: " + answers.get("crf_id"));
            logger.warn("***Next CRFVersion ID: " + answers.get("crf_version_id"));
            // here's the logic:
            // grab a crf,
            // iterate through crfs in hashmap,
            // if one matches, grab it;
            // take a look at its arraylist of versions;
            // if there is no version correlating, add it;
            // else, add the crf with a fresh arraylist + one version.
            // how long could this take to run???
            CRFBean cbean = (CRFBean) cdao.findByPK(((Integer) answers.get("crf_id")).intValue());
            CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(((Integer) answers.get("crf_version_id")).intValue());
            Set se = crfs.entrySet();
            boolean found = false;
            boolean versionFound = false;
            for (Iterator itse = se.iterator(); itse.hasNext();) {
                Map.Entry me = (Map.Entry) itse.next();
                CRFBean checkCrf = (CRFBean) me.getKey();
                if (checkCrf.getId() == cbean.getId()) {
                    found = true;
                    ArrayList oldList = (ArrayList) me.getValue();
                    Iterator itself = oldList.iterator();
                    while (itself.hasNext()) {
                        CRFVersionBean cvbCheck = (CRFVersionBean) itself.next();
                        if (cvbCheck.getId() == cvb.getId()) {
                            versionFound = true;
                        }
                    }// end of iteration through versions
                    if (!versionFound) {
                        oldList.add(cvb);
                        crfs.put(cbean, oldList);
                    }// end of adding new version to old crf
                }// end of check to see if current crf is in list
            }// end of iterating
            if (!found) {
                // add new crf here with version
                // CRFVersionBean cvb = (CRFVersionBean)cvdao.findByPK(
                // ((Integer)answers.get("crf_version_id")).intValue());
                ArrayList newList = new ArrayList();
                newList.add(cvb);
                crfs.put(cbean, newList);
            }
        }// end of cycling through answers

        return crfs;
    }

    // TODO make sure we are returning the correct boolean, tbh
    public boolean findDouble(HashMap hm, EntityBean event) {
        boolean returnMe = false;
        Set s = hm.entrySet();
        for (Iterator it = s.iterator(); it.hasNext();) {
            Map.Entry me = (Map.Entry) it.next();
            EntityBean eb = (EntityBean) me.getKey();
            if (eb.getId() == event.getId() && eb.getName().equals(event.getName())) {
                logger.warn("found OLD bean, return true");
                returnMe = true;
            }
        }
        return returnMe;
    }

    // so as not to get null pointer returns, tbh
    public ArrayList returnDouble(HashMap hm, EntityBean event) {
        ArrayList al = new ArrayList();
        Set s = hm.entrySet();
        for (Iterator it = s.iterator(); it.hasNext();) {
            Map.Entry me = (Map.Entry) it.next();
            EntityBean eb = (EntityBean) me.getKey();
            if (eb.getId() == event.getId() && eb.getName().equals(event.getName())) {
                // logger.warn("found OLD bean, return true");
                al = (ArrayList) me.getValue();
            }
        }
        return al;
    }

    // so as to remove the object correctly, tbh
    public HashMap removeDouble(HashMap hm, EntityBean event) {
        ArrayList al = new ArrayList();
        Set s = hm.entrySet();
        EntityBean removeMe = new EntityBean();
        for (Iterator it = s.iterator(); it.hasNext();) {
            Map.Entry me = (Map.Entry) it.next();
            EntityBean eb = (EntityBean) me.getKey();
            if (eb.getId() == event.getId() && eb.getName().equals(event.getName())) {
                logger.warn("found OLD bean, remove it");
                removeMe = eb;
            }
        }
        hm.remove(removeMe);
        return hm;
    }

    public int getDefinitionIdFromStudyEventId(int studyEventId) {
        int answer = 0;

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(studyEventId));

        ArrayList rows = select(digester.getQuery("getDefinitionIdFromStudyEventId"), variables);

        if (rows.size() > 0) {
            HashMap row = (HashMap) rows.get(0);
            answer = ((Integer) row.get("study_event_definition_id")).intValue();
        }

        return answer;
    }

    public EntityBean getNextScheduledEvent(String studySubjectOID) {
        StudyEventBean eb = new StudyEventBean();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), studySubjectOID);
        variables.put(Integer.valueOf(2), studySubjectOID);

        String sql = digester.getQuery("getNextScheduledEvent");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (StudyEventBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    public ArrayList findAllByStudySubject(StudySubjectBean ssb) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(ssb.getId()));

        return executeFindAllQuery("findAllByStudySubject", variables);
    }

    public ArrayList findAllByStudySubjectAndDefinition(StudySubjectBean ssb, StudyEventDefinitionBean sed) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(ssb.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(sed.getId()));

        return executeFindAllQuery("findAllByStudySubjectAndDefinition", variables);
    }

    private HashMap subjDefs;

    public void updateSampleOrdinals_v092() {
        subjDefs = new HashMap();
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);

        ArrayList rows =
            select("SELECT study_event_id, study_event_definition_id, study_subject_id, sample_ordinal FROM study_event ORDER BY study_subject_id ASC, study_event_definition_id ASC, sample_ordinal ASC");

        for (int i = 0; i < rows.size(); i++) {
            HashMap row = (HashMap) rows.get(i);

            Integer studyEventId = (Integer) row.get("study_event_id");
            Integer studyEventDefinitionId = (Integer) row.get("study_event_definition_id");
            Integer studySubjectId = (Integer) row.get("study_subject_id");

            addEvent(studySubjectId, studyEventDefinitionId, studyEventId);
        }

        Iterator keysIt = subjDefs.keySet().iterator();
        while (keysIt.hasNext()) {
            String key = (String) keysIt.next();
            ArrayList events = (ArrayList) subjDefs.get(key);

            for (int i = 0; i < events.size(); i++) {
                Integer id = (Integer) events.get(i);
                if (id != null) {
                    int ordinal = i + 1;
                    logger.debug("UPDATE study_event SET sample_ordinal = " + ordinal + " WHERE study_event_id = " + id);
                    // execute("UPDATE study_event SET sample_ordinal = " +
                    // ordinal + " WHERE study_event_id = " + id);
                }
            }
        }
    }

    private void addEvent(Integer studySubjectId, Integer studyEventDefinitionId, Integer studyEventId) {
        if (studySubjectId == null || studyEventDefinitionId == null || studyEventId == null) {
            return;
        }

        String key = studySubjectId + "-" + studyEventDefinitionId;

        ArrayList events;
        if (subjDefs.containsKey(key)) {
            events = (ArrayList) subjDefs.get(key);
        } else {
            events = new ArrayList();
        }
        events.add(studyEventId);
        logger.debug("putting in key: " + key + " seid: " + studyEventId);
        subjDefs.put(key, events);
    }

    public Integer countNotRemovedEvents(Integer studyEventDefinitionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), studyEventDefinitionId);
        String sql = digester.getQuery("countNotRemovedEvents");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return 0;
        }
    }
    
    public HashMap getStudySubjectCRFData(StudyBean sb, int studySubjectId, int eventDefId, String crfVersionOID, int eventOrdinal) {
        HashMap studySubjectCRFDataDetails = new HashMap();
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
       
        HashMap variables = new HashMap();
        variables.put(1, Integer.valueOf(sb.getId()));
        variables.put(2, Integer.valueOf(eventOrdinal));
        variables.put(3, crfVersionOID);
        variables.put(4, Integer.valueOf(studySubjectId));
        variables.put(5, Integer.valueOf(eventDefId));
        
        ArrayList alist = this.select(digester.getQuery("getStudySubjectCRFDataDetails"), variables);
        // TODO make sure this other statement for eliciting crfs works, tbh
        // switched from getEventAndCRFVersionInformation
        // to getEventsAndMultipleCRFVersionInformation
        // crfs = this.getEventAndCRFVersionInformation(alist);
        studySubjectCRFDataDetails = this.getStudySubjectCRFDataDetails(alist);
        return studySubjectCRFDataDetails;
    }

    private HashMap getStudySubjectCRFDataDetails(ArrayList rows) {
        HashMap returnMe = new HashMap();
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            HashMap answers = (HashMap) it.next();

            returnMe.put("event_crf_id", answers.get("event_crf_id"));
            returnMe.put("event_definition_crf_id", answers.get("event_definition_crf_id"));
            returnMe.put("study_event_id", answers.get("study_event_id"));

        }// end of cycling through answers

        return returnMe;
    }

    private void notifyObservers(StudyEventBeanContainer sbc){
    if(getObserver()!=null)
    	getObserver().update(sbc);
    }
    
    @Override
	public Observer getObserver() {
		return new StudyEventBeanListener(this);
	}

	@Override
    public void setObserver(Observer observer) {
		this.observer = observer;
	}

}
