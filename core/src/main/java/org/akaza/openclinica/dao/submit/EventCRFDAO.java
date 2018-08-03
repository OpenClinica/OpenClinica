/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.dao.submit;

import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.FormLayoutBean;
import org.akaza.openclinica.dao.EventCRFSDVFilter;
import org.akaza.openclinica.dao.EventCRFSDVSort;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.apache.commons.lang.StringUtils;

/**
 * <P>
 * EventCRFDAO.java, data access object for an instance of an event being filled out on a subject. Was originally
 * individual_instrument table in OpenClinica v.1.
 * 
 * @author thickerson
 * 
 *         TODO test create and update first thing
 */
public class EventCRFDAO<K extends String, V extends ArrayList> extends AuditableEntityDAO {
    // private DAODigester digester;

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public EventCRFDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public EventCRFDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public EventCRFDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_EVENTCRF;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.DATE);
        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.STRING);// annotations
        this.setTypeExpected(9, TypeNames.TIMESTAMP);// completed
        this.setTypeExpected(10, TypeNames.INT);// validator id
        this.setTypeExpected(11, TypeNames.DATE);// date validate
        this.setTypeExpected(12, TypeNames.TIMESTAMP);// date val. completed
        this.setTypeExpected(13, TypeNames.STRING);
        this.setTypeExpected(14, TypeNames.STRING);
        this.setTypeExpected(15, TypeNames.INT);// owner id
        this.setTypeExpected(16, TypeNames.DATE);
        this.setTypeExpected(17, TypeNames.INT);// subject id
        this.setTypeExpected(18, TypeNames.DATE);// date updated
        this.setTypeExpected(19, TypeNames.INT);// updater
        this.setTypeExpected(20, TypeNames.BOOL);// electronic_signature_status
        this.setTypeExpected(21, TypeNames.BOOL);// sdv_status
        this.setTypeExpected(22, TypeNames.INT);// old_status
        this.setTypeExpected(23, TypeNames.INT); // sdv_update_id
        this.setTypeExpected(24, TypeNames.INT); // form_layout_id
        // if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
        // this.setTypeExpected(24, TypeNames.INT); // r
        // }

    }

    public EntityBean update(EntityBean eb) {
        EventCRFBean ecb = (EventCRFBean) eb;

        ecb.setActive(false);

        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getStudyEventId()));
        variables.put(new Integer(2), new Integer(ecb.getCRFVersionId()));
        if (ecb.getDateInterviewed() == null) {
            nullVars.put(new Integer(3), new Integer(Types.DATE));
            variables.put(new Integer(3), null);
        } else {
            variables.put(new Integer(3), ecb.getDateInterviewed());
        }
        variables.put(new Integer(4), ecb.getInterviewerName());
        variables.put(new Integer(5), new Integer(ecb.getCompletionStatusId()));
        variables.put(new Integer(6), new Integer(ecb.getStatus().getId()));
        variables.put(new Integer(7), ecb.getAnnotations());
        if (ecb.getDateCompleted() == null) {
            nullVars.put(new Integer(8), new Integer(Types.TIMESTAMP));
            variables.put(new Integer(8), null);
        } else {
            variables.put(new Integer(8), new java.sql.Timestamp(ecb.getDateCompleted().getTime()));
        }
        // variables.put(new Integer(8),ecb.getDateCompleted());

        variables.put(new Integer(9), new Integer(ecb.getValidatorId()));

        if (ecb.getDateValidate() == null) {
            nullVars.put(new Integer(10), new Integer(Types.DATE));
            variables.put(new Integer(10), null);
        } else {
            variables.put(new Integer(10), ecb.getDateValidate());
        }
        // variables.put(new Integer(10),ecb.getDateValidate());

        if (ecb.getDateValidateCompleted() == null) {
            nullVars.put(new Integer(11), new Integer(Types.TIMESTAMP));
            variables.put(new Integer(11), null);
        } else {
            variables.put(new Integer(11), new Timestamp(ecb.getDateValidateCompleted().getTime()));
        }
        // variables.put(new Integer(11),ecb.getDateValidateCompleted());
        variables.put(new Integer(12), ecb.getValidatorAnnotations());
        variables.put(new Integer(13), ecb.getValidateString());
        variables.put(new Integer(14), new Integer(ecb.getStudySubjectId()));
        variables.put(new Integer(15), new Integer(ecb.getUpdaterId()));
        variables.put(new Integer(16), new Boolean(ecb.isElectronicSignatureStatus()));

        variables.put(new Integer(17), new Boolean(ecb.isSdvStatus()));
        if (ecb.getOldStatus() != null && ecb.getOldStatus().getId() > 0) {
            variables.put(new Integer(18), new Integer(ecb.getOldStatus().getId()));
        } else {
            variables.put(new Integer(18), new Integer(0));
        }
        // @pgawade 22-May-2011 added the sdv updater id variable
        variables.put(new Integer(19), ecb.getSdvUpdateId());
        // variables.put(new Integer(19), new Integer(ecb.getId()));
        variables.put(new Integer(21), new Integer(ecb.getId()));
        variables.put(new Integer(20), new Integer(ecb.getFormLayoutId()));

        this.execute(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            ecb.setActive(true);
        }

        return ecb;
    }

    public void markComplete(EventCRFBean ecb, boolean ide) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getId()));

        if (ide) {
            execute(digester.getQuery("markCompleteIDE"), variables);
        } else {
            execute(digester.getQuery("markCompleteDDE"), variables);
        }
    }

    public EntityBean create(EntityBean eb) {
        EventCRFBean ecb = (EventCRFBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        variables.put(new Integer(1), new Integer(ecb.getStudyEventId()));
        variables.put(new Integer(2), new Integer(ecb.getCRFVersionId()));

        Date interviewed = ecb.getDateInterviewed();
        if (interviewed != null) {
            variables.put(new Integer(3), ecb.getDateInterviewed());
        } else {
            variables.put(new Integer(3), null);
            nullVars.put(new Integer(3), new Integer(Types.DATE));
        }
        logger.debug("created: ecb.getInterviewerName()" + ecb.getInterviewerName());
        variables.put(new Integer(4), ecb.getInterviewerName());

        variables.put(new Integer(5), new Integer(ecb.getCompletionStatusId()));
        variables.put(new Integer(6), new Integer(ecb.getStatus().getId()));
        variables.put(new Integer(7), ecb.getAnnotations());
        variables.put(new Integer(8), new Integer(ecb.getOwnerId()));
        variables.put(new Integer(9), new Integer(ecb.getStudySubjectId()));
        variables.put(new Integer(10), ecb.getValidateString());
        variables.put(new Integer(11), ecb.getValidatorAnnotations());
        variables.put(new Integer(12), new Integer(ecb.getFormLayoutId()));

        executeWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            ecb.setId(getLatestPK());
        }

        return ecb;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        EventCRFBean eb = new EventCRFBean();
        this.setEntityAuditInformation(eb, hm);

        eb.setId(((Integer) hm.get("event_crf_id")).intValue());
        eb.setStudyEventId(((Integer) hm.get("study_event_id")).intValue());
        eb.setCRFVersionId(((Integer) hm.get("crf_version_id")).intValue());
        eb.setDateInterviewed((Date) hm.get("date_interviewed"));
        eb.setInterviewerName((String) hm.get("interviewer_name"));
        eb.setCompletionStatusId(((Integer) hm.get("completion_status_id")).intValue());
        eb.setAnnotations((String) hm.get("annotations"));
        eb.setDateCompleted((Date) hm.get("date_completed"));
        eb.setValidatorId(((Integer) hm.get("validator_id")).intValue());
        eb.setDateValidate((Date) hm.get("date_validate"));
        eb.setDateValidateCompleted((Date) hm.get("date_validate_completed"));
        eb.setValidatorAnnotations((String) hm.get("validator_annotations"));
        eb.setValidateString((String) hm.get("validate_string"));
        eb.setStudySubjectId(((Integer) hm.get("study_subject_id")).intValue());
        eb.setSdvStatus((Boolean) hm.get("sdv_status"));
        eb.setSdvUpdateId((Integer) hm.get("sdv_update_id"));
        eb.setFormLayoutId(((Integer) hm.get("form_layout_id")).intValue());
        Integer oldStatusId = (Integer) hm.get("old_status_id");
        eb.setOldStatus(Status.get(oldStatusId));

        // eb.setStatus(Status.get((Integer) hm.get("status_id"))
        return eb;
    }

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            EventCRFBean eb = (EventCRFBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public EntityBean findByPK(int ID) {
        EventCRFBean eb = new EventCRFBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (EventCRFBean) this.getEntityFromHashMap((HashMap) it.next());
        }

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

    public ArrayList findAllByStudyEvent(StudyEventBean studyEvent) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));

        return executeFindAllQuery("findAllByStudyEvent", variables);
    }

    public ArrayList findAllByStudyEventAndStatus(StudyEventBean studyEvent, Status status) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), new Integer(status.getId()));
        return executeFindAllQuery("findAllByStudyEventAndStatus", variables);
    }

    public ArrayList<EventCRFBean> findAllByStudySubject(int studySubjectId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studySubjectId));

        return executeFindAllQuery("findAllByStudySubject", variables);
    }

    public List<EventCRFBean> findAllCRFMigrationReportList(FormLayoutBean sourceCrfVersionBean, FormLayoutBean targetCrfVersionBean,
            ArrayList<String> studyEventDefnlist, ArrayList<String> sitelist) {
        HashMap<Integer, Object> variables = new HashMap();
        String eventStr = StringUtils.join(studyEventDefnlist, ",");
        String siteStr = StringUtils.join(sitelist, ",");
        variables.put(new Integer(1), new Integer(sourceCrfVersionBean.getId()));
        variables.put(2, eventStr);
        variables.put(3, siteStr);
        variables.put(4, String.valueOf(sourceCrfVersionBean.getId()));
        variables.put(5, String.valueOf(targetCrfVersionBean.getId()));

        return executeFindAllQuery("findAllCRFMigrationReportList", variables);
    }

    public ArrayList findAllByStudyEventAndFormOrFormLayoutOid(StudyEventBean studyEvent, String crfVersionOrCrfOID) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), crfVersionOrCrfOID);
        variables.put(new Integer(3), crfVersionOrCrfOID);

        return executeFindAllQuery("findAllByStudyEventAndFormOrFormLayoutOid", variables);
    }

    public ArrayList findAllByStudyEventAndCrfOrCrfVersionOid(StudyEventBean studyEvent, String crfVersionOrCrfOID) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), crfVersionOrCrfOID);
        variables.put(new Integer(3), crfVersionOrCrfOID);

        return executeFindAllQuery("findAllByStudyEventAndCrfOrCrfVersionOid", variables);
    }

    public ArrayList<EventCRFBean> findAllByStudyEventInParticipantForm(StudyEventBean studyEvent, int sed_Id, int studyId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), new Integer(sed_Id));
        variables.put(new Integer(3), new Integer(studyId));

        return executeFindAllQuery("findAllByStudyEventInParticipantForm", variables);
    }

    public ArrayList findAllByCRF(int crfId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfId));

        return executeFindAllQuery("findAllByCRF", variables);
    }

    public ArrayList findAllByCRFVersion(int versionId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(versionId));

        return executeFindAllQuery("findAllByCRFVersion", variables);
    }

    public ArrayList findAllStudySubjectByCRFVersion(int versionId) {
        this.setTypesExpected();

        // ss.label, sed.name as sed_name, s.name as study_name
        this.setTypeExpected(24, TypeNames.STRING);
        this.setTypeExpected(25, TypeNames.STRING);
        this.setTypeExpected(26, TypeNames.STRING);
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            this.setTypeExpected(25, TypeNames.STRING); // r
            this.setTypeExpected(26, TypeNames.STRING); // r
            this.setTypeExpected(27, TypeNames.STRING); // r
        }
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(versionId));

        ArrayList alist = this.select(digester.getQuery("findAllStudySubjectByCRFVersion"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            EventCRFBean eb = (EventCRFBean) this.getEntityFromHashMap(hm);
            eb.setStudySubjectName((String) hm.get("label"));
            eb.setEventName((String) hm.get("sed_name"));
            eb.setStudyName((String) hm.get("study_name"));
            al.add(eb);
        }
        return al;

    }

    public ArrayList findUndeletedWithStudySubjectsByCRFVersion(int versionId) {
        this.setTypesExpected();
        // ss.label, sed.name as sed_name, s.name as study_name, ss.sample_ordinal as repeat_number
        // this.setTypeExpected(23, TypeNames.STRING);
        this.setTypeExpected(24, TypeNames.STRING);
        this.setTypeExpected(25, TypeNames.STRING);
        this.setTypeExpected(26, TypeNames.STRING);
        this.setTypeExpected(27, TypeNames.INT);
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(versionId));

        ArrayList alist = this.select(digester.getQuery("findUndeletedWithStudySubjectsByCRFVersion"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            EventCRFBean eb = (EventCRFBean) this.getEntityFromHashMap(hm);
            eb.setStudySubjectName((String) hm.get("label"));
            eb.setEventName((String) hm.get("sed_name"));
            eb.setStudyName((String) hm.get("study_name"));
            eb.setEventOrdinal((Integer) hm.get("repeat_number"));
            al.add(eb);
        }
        return al;
    }

    public ArrayList findByEventSubjectVersion(StudyEventBean studyEvent, StudySubjectBean studySubject, CRFVersionBean crfVersion) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), new Integer(crfVersion.getId()));
        variables.put(new Integer(3), new Integer(studySubject.getId()));

        return executeFindAllQuery("findByEventSubjectVersion", variables);
    }

    public ArrayList findByEventSubjectFormLayout(StudyEventBean studyEvent, StudySubjectBean studySubject, FormLayoutBean formLayout) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), new Integer(formLayout.getId()));
        variables.put(new Integer(3), new Integer(studySubject.getId()));

        return executeFindAllQuery("findByEventSubjectFormLayout", variables);
    }

    // TODO: to get rid of warning refactor executeFindAllQuery method in
    // superclass
    public EventCRFBean findByEventCrfVersion(StudyEventBean studyEvent, CRFVersionBean crfVersion) {
        EventCRFBean eventCrfBean = null;
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), new Integer(crfVersion.getId()));

        ArrayList<EventCRFBean> eventCrfs = executeFindAllQuery("findByEventCrfVersion", variables);
        if (!eventCrfs.isEmpty() && eventCrfs.size() == 1) {
            eventCrfBean = eventCrfs.get(0);
        }
        return eventCrfBean;

    }

    public EventCRFBean findByEventFormLayout(StudyEventBean studyEvent, FormLayoutBean formLayout) {
        EventCRFBean eventCrfBean = null;
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(new Integer(1), new Integer(studyEvent.getId()));
        variables.put(new Integer(2), new Integer(formLayout.getId()));

        ArrayList<EventCRFBean> eventCrfs = executeFindAllQuery("findByEventFormLayout", variables);
        if (!eventCrfs.isEmpty() && eventCrfs.size() == 1) {
            eventCrfBean = eventCrfs.get(0);
        }
        return eventCrfBean;

    }

    public ArrayList<EventCRFBean> findByCrfVersion(CRFVersionBean crfVersion) {
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(new Integer(1), new Integer(crfVersion.getId()));

        ArrayList<EventCRFBean> eventCrfs = executeFindAllQuery("findByCrfVersion", variables);
        return eventCrfs;

    }

    public void delete(int eventCRFId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(eventCRFId));

        this.execute(digester.getQuery("delete"), variables);
        return;

    }

    public void setSDVStatus(boolean sdvStatus, int userId, int eventCRFId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), sdvStatus);
        variables.put(new Integer(2), userId);
        variables.put(new Integer(3), eventCRFId);

        this.execute(digester.getQuery("setSDVStatus"), variables);
    }

    public Integer countEventCRFsByStudy(int studyId, int parentStudyId) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        String sql = digester.getQuery("countEventCRFsByStudy");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Integer countEventCRFsByStudyIdentifier(String identifier) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, identifier);
        String sql = digester.getQuery("countEventCRFsByStudyIdentifier");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Integer countEventCRFsByStudySubject(int studySubjectId, int studyId, int parentStudyId) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, studySubjectId);
        variables.put(2, studyId);
        variables.put(3, parentStudyId);
        String sql = digester.getQuery("countEventCRFsByStudySubject");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Integer countEventCRFsByStudyIdentifier(int studyId, int parentStudyId, String studyIdentifier) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        variables.put(3, studyIdentifier);
        String sql = digester.getQuery("countEventCRFsByStudyIdentifier");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Integer countEventCRFsByByStudySubjectCompleteOrLockedAndNotSDVd(int studySubjectId) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, studySubjectId);
        String sql = digester.getQuery("countEventCRFsByByStudySubjectCompleteOrLockedAndNotSDVd");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public ArrayList getEventCRFsByStudySubjectCompleteOrLocked(int studySubjectId) {

        HashMap variables = new HashMap();
        variables.put(1, studySubjectId);

        return executeFindAllQuery("getEventCRFsByStudySubjectCompleteOrLocked", variables);
    }

    public ArrayList getEventCRFsByStudySubjectLimit(int studySubjectId, int studyId, int parentStudyId, int limit, int offset) {

        HashMap variables = new HashMap();
        variables.put(1, studySubjectId);
        variables.put(2, studyId);
        variables.put(3, parentStudyId);
        variables.put(4, limit);
        variables.put(5, offset);

        return executeFindAllQuery("getEventCRFsByStudySubjectLimit", variables);
    }

    public ArrayList getEventCRFsByStudySubject(int studySubjectId, int studyId, int parentStudyId) {

        HashMap variables = new HashMap();
        variables.put(1, studySubjectId);
        variables.put(2, studyId);
        variables.put(3, parentStudyId);

        return executeFindAllQuery("getEventCRFsByStudySubject", variables);
    }

    public ArrayList getGroupByStudySubject(int studySubjectId, int studyId, int parentStudyId) {

        HashMap variables = new HashMap();
        variables.put(1, studySubjectId);
        variables.put(2, studyId);
        variables.put(3, parentStudyId);

        return executeFindAllQuery("getGroupByStudySubject", variables);
    }

    public ArrayList getEventCRFsByStudyIdentifier(int studyId, int parentStudyId, String studyIdentifier, int limit, int offset) {

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        variables.put(3, studyIdentifier);
        variables.put(4, limit);
        variables.put(5, offset);

        return executeFindAllQuery("getEventCRFsByStudyIdentifier", variables);
    }

    public Integer getCountWithFilter(int studyId, int parentStudyId, EventCRFSDVFilter filter , String permissionTags) {

        setTypesExpected();

        HashMap variables = new HashMap();

        String sql = digester.getQuery("getCountWithFilterPart1");
        if(StringUtils.isEmpty(permissionTags)) {
            sql =sql+" "+ digester.getQuery("getCountWithFilter");
            variables.put(1, studyId);
            variables.put(2, parentStudyId);
        }else{
            sql =sql+" "+ digester.getQuery("getCountWithFilterWithTagId");
            variables.put(1, permissionTags);
            variables.put(2, studyId);
            variables.put(3, parentStudyId);
        }
        sql =sql+" "+ digester.getQuery("getCountWithFilterPart2");

        sql += filter.execute("");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public ArrayList<EventCRFBean> getWithFilterAndSort(int studyId, int parentStudyId, EventCRFSDVFilter filter, EventCRFSDVSort sort, int rowStart,
            int rowEnd, String permissionTags) {
        ArrayList<EventCRFBean> eventCRFs = new ArrayList<EventCRFBean>();
        setTypesExpected();

        HashMap variables = new HashMap();

        String sql = digester.getQuery("getWithFilterAndSortPart1");
        if(StringUtils.isEmpty(permissionTags)) {
            sql =sql+" "+ digester.getQuery("getCountWithFilter");
            variables.put(1, studyId);
            variables.put(2, parentStudyId);
        }else{
            sql =sql+" "+ digester.getQuery("getCountWithFilterWithTagId");
            variables.put(1, permissionTags);
            variables.put(2, studyId);
            variables.put(3, parentStudyId);
        }
        sql =sql+" "+ digester.getQuery("getWithFilterAndSortPart2");

        sql = sql + filter.execute("");
        // sql = sql + sort.execute("");
        sql = sql + " order By  ec.date_created ASC "; // major hack
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            // sql += " ) where rownum <= " + rowEnd + " and rownum >" + rowStart + " ";
            sql += " )x)where r between " + (rowStart + 1) + " and " + rowEnd;
        } else {
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            EventCRFBean eventCRF = (EventCRFBean) this.getEntityFromHashMap((HashMap) it.next());
            eventCRFs.add(eventCRF);
        }
        return eventCRFs;
    }

    public ArrayList getEventCRFsByStudy(int studyId, int parentStudyId, int limit, int offset) {

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        variables.put(3, limit);
        variables.put(4, offset);

        return executeFindAllQuery("getEventCRFsByStudy", variables);
    }

    public ArrayList getEventCRFsByStudySubjectLabelLimit(String label, int studyId, int parentStudyId, int limit, int offset) {

        HashMap variables = new HashMap();
        variables.put(1, '%' + label + '%');
        variables.put(2, studyId);
        variables.put(3, parentStudyId);
        variables.put(4, limit);
        variables.put(5, offset);

        return executeFindAllQuery("getEventCRFsByStudySubjectLabelLimit", variables);
    }

    public ArrayList getEventCRFsByEventNameLimit(String eventName, int limit, int offset) {

        HashMap variables = new HashMap();
        variables.put(1, eventName);
        variables.put(2, limit);
        variables.put(3, offset);

        return executeFindAllQuery("getEventCRFsByEventNameLimit", variables);
    }

    public ArrayList getEventCRFsByEventDateLimit(int studyId, String eventDate, int limit, int offset) {

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, eventDate);
        variables.put(3, limit);
        variables.put(4, offset);

        return executeFindAllQuery("getEventCRFsByEventDateLimit", variables);
    }

    public ArrayList getEventCRFsByStudySDV(int studyId, boolean sdvStatus, int limit, int offset) {

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, sdvStatus);
        variables.put(3, limit);
        variables.put(4, offset);

        return executeFindAllQuery("getEventCRFsByStudySDV", variables);
    }

    public ArrayList getEventCRFsByCRFStatus(int studyId, int subjectEventStatusId, int limit, int offset) {

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, subjectEventStatusId);
        variables.put(3, limit);
        variables.put(4, offset);

        return executeFindAllQuery("getEventCRFsByCRFStatus", variables);
    }

    public ArrayList getEventCRFsBySDVRequirement(int studyId, int parentStudyId, int limit, int offset, Integer... sdvCode) {

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        this.setTypesExpected();

        String sql = digester.getQuery("getEventCRFsBySDVRequirement");
        sql += " AND ( ";
        for (int i = 0; i < sdvCode.length; i++) {
            sql += i != 0 ? " OR " : "";
            sql += " source_data_verification_code = " + sdvCode[i];
        }
        sql += " ) ))  limit " + limit + " offset " + offset;

        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            EventCRFBean eb = (EventCRFBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Integer countEventCRFsByStudySubjectLabel(String label, int studyId, int parentStudyId) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, label);
        variables.put(2, studyId);
        variables.put(3, parentStudyId);

        String sql = digester.getQuery("countEventCRFsByStudySubjectLabel");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Integer countEventCRFsByStudySDV(int studyId, boolean sdvStatus) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, sdvStatus);
        String sql = digester.getQuery("countEventCRFsByStudySDV");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Integer countEventCRFsByCRFStatus(int studyId, int statusId) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, statusId);
        String sql = digester.getQuery("countEventCRFsByCRFStatus");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Integer countEventCRFsByEventName(String eventName) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, eventName);
        String sql = digester.getQuery("countEventCRFsByEventName");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Integer countEventCRFsBySDVRequirement(int studyId, int parentStudyId, Integer... sdvCode) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        String sql = digester.getQuery("countEventCRFsBySDVRequirement");
        sql += " AND ( ";
        for (int i = 0; i < sdvCode.length; i++) {
            sql += i != 0 ? " OR " : "";
            sql += " source_data_verification_code = " + sdvCode[i];
        }
        sql += "))) ";

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Integer countEventCRFsByEventNameSubjectLabel(String eventName, String subjectLabel) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, eventName);
        variables.put(2, subjectLabel);
        String sql = digester.getQuery("countEventCRFsByEventNameSubjectLabel");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Integer countEventCRFsByEventDate(int studyId, String eventDate) {

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, eventDate);
        String sql = digester.getQuery("countEventCRFsByEventDate");
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (Integer) ((HashMap) it.next()).get("count");

        } else {
            return 0;
        }
    }

    public Map<Integer, SortedSet<EventCRFBean>> buildEventCrfListByStudyEvent(Integer studySubjectId) {
        this.setTypesExpected(); // <== Must be called first

        Map<Integer, SortedSet<EventCRFBean>> result = new HashMap<Integer, SortedSet<EventCRFBean>>();

        HashMap<Integer, Object> param = new HashMap<Integer, Object>();
        int i = 1;
        param.put(i++, studySubjectId);

        List selectResult = select(digester.getQuery("buildEventCrfListByStudyEvent"), param);

        Iterator it = selectResult.iterator();

        while (it.hasNext()) {
            EventCRFBean bean = (EventCRFBean) this.getEntityFromHashMap((HashMap) it.next());

            Integer studyEventId = bean.getStudyEventId();
            if (!result.containsKey(studyEventId)) {
                result.put(studyEventId, new TreeSet<EventCRFBean>(new Comparator<EventCRFBean>() {
                    public int compare(EventCRFBean o1, EventCRFBean o2) {
                        Integer id1 = o1.getId();
                        Integer id2 = o2.getId();
                        return id1.compareTo(id2);
                    }
                }));
            }
            result.get(studyEventId).add(bean);
        }

        return result;
    }

    public Set<Integer> buildNonEmptyEventCrfIds(Integer studySubjectId) {
        Set<Integer> result = new HashSet<Integer>();

        HashMap<Integer, Object> param = new HashMap<Integer, Object>();
        int i = 1;
        param.put(i++, studySubjectId);

        List selectResult = select(digester.getQuery("buildNonEmptyEventCrfIds"), param);

        Iterator it = selectResult.iterator();

        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            result.add((Integer) hm.get("event_crf_id"));
        }

        return result;
    }

    public void updateCRFVersionID(int event_crf_id, int crf_version_id, int user_id) {
        Connection con = null;
        updateCRFVersionID(event_crf_id, crf_version_id, user_id, con);
    }

    /* this function allows to run transactional updates for an action */

    public void updateCRFVersionID(int event_crf_id, int crf_version_id, int user_id, Connection con) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.BOOL);
        this.setTypeExpected(3, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, crf_version_id);
        variables.put(2, user_id);
        variables.put(3, user_id);
        variables.put(4, false);
        variables.put(5, event_crf_id);
        String sql = digester.getQuery("updateCRFVersionID");
        // this is the way to make the change transactional
        if (con == null) {
            this.execute(sql, variables);
        } else {
            this.execute(sql, variables, con);
        }
    }

    public void updateFormLayoutID(int event_crf_id, int form_layout_id, int user_id, Connection con) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.BOOL);
        this.setTypeExpected(3, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, form_layout_id);
        variables.put(2, user_id);
        variables.put(3, user_id);
        variables.put(4, false);
        variables.put(5, event_crf_id);
        String sql = digester.getQuery("updateFormLayoutID");
        // this is the way to make the change transactional
        if (con == null) {
            this.execute(sql, variables);
        } else {
            this.execute(sql, variables, con);
        }
    }

}
