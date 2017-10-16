/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.managestudy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * @author thickerson
 * @author jsampson
 */
public class StudyEventDefinitionDAO<K extends String,V extends ArrayList> extends AuditableEntityDAO {

    private void setQueryNames() {
        findAllByStudyName = "findAllByStudy";
        findAllActiveByStudyName = "findAllActiveByStudy";
        findByPKAndStudyName = "findByPKAndStudy";
    }

    public StudyEventDefinitionDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public StudyEventDefinitionDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public StudyEventDefinitionDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDYEVENTDEFNITION;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.BOOL);
        this.setTypeExpected(6, TypeNames.STRING);
        this.setTypeExpected(7, TypeNames.STRING);
        // int int date date int
        this.setTypeExpected(8, TypeNames.INT);
        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.DATE);
        this.setTypeExpected(11, TypeNames.DATE);
        this.setTypeExpected(12, TypeNames.INT);
        this.setTypeExpected(13, TypeNames.INT);
        this.setTypeExpected(14, TypeNames.STRING);
    }

    /**
     * <P>
     * findNextKey, a method to return a simple int from the database.
     *
     * @return int, which is the next primary key for creating a study event
     *         definition.
     */
    public int findNextKey() {
        this.unsetTypeExpected();
        Integer keyInt = new Integer(0);
        this.setTypeExpected(1, TypeNames.INT);
        ArrayList alist = this.select(digester.getQuery("findNextKey"));
        Iterator it = alist.iterator();
        if (it.hasNext()) {
            HashMap key = (HashMap) it.next();
            keyInt = (Integer) key.get("key");
        }
        return keyInt.intValue();
    }

    private String getOid(StudyEventDefinitionBean sedb) {

        String oid;
        try {
            oid = sedb.getOid() != null ? sedb.getOid() : sedb.getOidGenerator().generateOid(sedb.getName());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    private String getValidOid(StudyEventDefinitionBean sedb) {

        String oid = getOid(sedb);
        logger.debug(oid);
        String oidPreRandomization = oid;
        while (findByOid(oid) != null) {
            oid = sedb.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;

    }

    public EntityBean create(EntityBean eb) {
        // study_event_definition_id ,
        // STUDY_ID, NAME,DESCRIPTION, REPEATING, TYPE, CATEGORY, OWNER_ID,
        // STATUS_ID, DATE_CREATED,ordinal,oid
        StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) eb;
        sedb.setId(this.findNextKey());
        logger.debug("***id:" + sedb.getId());
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sedb.getId()));
        variables.put(new Integer(2), new Integer(sedb.getStudyId()));
        variables.put(new Integer(3), sedb.getName());
        variables.put(new Integer(4), sedb.getDescription());
        variables.put(new Integer(5), new Boolean(sedb.isRepeating()));
        variables.put(new Integer(6), sedb.getType());
        variables.put(new Integer(7), sedb.getCategory());
        variables.put(new Integer(8), new Integer(sedb.getOwnerId()));
        variables.put(new Integer(9), new Integer(sedb.getStatus().getId()));
        variables.put(new Integer(10), new Integer(sedb.getOrdinal()));
        variables.put(new Integer(11), getValidOid(sedb));
        this.execute(digester.getQuery("create"), variables);

        return sedb;
    }

    public EntityBean update(EntityBean eb) {
        StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) eb;
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sedb.getStudyId()));
        variables.put(new Integer(2), sedb.getName());
        variables.put(new Integer(3), sedb.getDescription());
        variables.put(new Integer(4), new Boolean(sedb.isRepeating()));
        variables.put(new Integer(5), sedb.getType());
        variables.put(new Integer(6), sedb.getCategory());
        variables.put(new Integer(7), new Integer(sedb.getStatus().getId()));
        variables.put(new Integer(8), new Integer(sedb.getUpdaterId()));
        variables.put(new Integer(9), new Integer(sedb.getOrdinal()));
        variables.put(new Integer(10), new Integer(sedb.getId()));
        this.execute(digester.getQuery("update"), variables);
        return eb;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        StudyEventDefinitionBean eb = new StudyEventDefinitionBean();

        this.setEntityAuditInformation(eb, hm);
        // set dates and ints first, then strings
        // create a sub-function in auditable entity dao that can do this?
        Integer sedId = (Integer) hm.get("study_event_definition_id");
        eb.setId(sedId.intValue());

        Integer studyId = (Integer) hm.get("study_id");
        eb.setStudyId(studyId.intValue());
        Integer ordinal = (Integer) hm.get("ordinal");
        eb.setOrdinal(ordinal.intValue());
        Boolean repeating = (Boolean) hm.get("repeating");
        eb.setRepeating(repeating.booleanValue());

        // below functions changed by get entity audit information functions

        /*
         * Integer ownerId = (Integer)hm.get("owner_id");
         * eb.setOwnerId(ownerId.intValue()); Integer updaterId =
         * (Integer)hm.get("update_id"); eb.setUpdaterId(updaterId.intValue());
         * Integer statusId = (Integer)hm.get("status_id");
         * eb.setStatus(Status.get(statusId.intValue()));
         *
         * Date dateCreated = (Date)hm.get("date_created"); Date dateUpdated =
         * (Date)hm.get("date_updated"); eb.setCreatedDate(dateCreated);
         * eb.setUpdatedDate(dateUpdated);
         */
        eb.setName((String) hm.get("name"));
        eb.setDescription((String) hm.get("description"));
        eb.setType((String) hm.get("type"));
        eb.setCategory((String) hm.get("category"));
        eb.setOid((String) hm.get("oc_oid"));
        return eb;
    }

    public StudyEventDefinitionBean findByOid(String oid) {
        StudyEventDefinitionBean studyEventDefinitionBean = new StudyEventDefinitionBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);
        String sql = digester.getQuery("findByOid");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            studyEventDefinitionBean = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
            return studyEventDefinitionBean;
        } else {
            return null;
        }
    }

    /*
     * find by oid and study id - sometimes we have relationships which can't
     * break past the parent study relationship. This 'covering' allows us to
     * query on both the study and the parent study id. added tbh 10/2008 for
     * 2.5.2
     */
    public StudyEventDefinitionBean findByOidAndStudy(String oid, int studyId, int parentStudyId) {
        StudyEventDefinitionBean studyEventDefinitionBean = this.findByOidAndStudy(oid, studyId);
        if (studyEventDefinitionBean == null) {
            studyEventDefinitionBean = this.findByOidAndStudy(oid, parentStudyId);
        }
        return studyEventDefinitionBean;
    }

    private StudyEventDefinitionBean findByOidAndStudy(String oid, int studyId) {
        StudyEventDefinitionBean studyEventDefinitionBean = new StudyEventDefinitionBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);
        variables.put(new Integer(2), new Integer(studyId));
        variables.put(new Integer(3), new Integer(studyId));
        String sql = digester.getQuery("findByOidAndStudy");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            studyEventDefinitionBean = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
            return studyEventDefinitionBean;
        } else {
            logger.info("WARNING: cannot find sed bean by oid " + oid + " and study id " + studyId);
            // throw new
            // RuntimeException("cannot find sed bean by oid and study id");
            return null;
        }
    }

    @Override
    public ArrayList findAllByStudy(StudyBean study) {

        StudyDAO studyDao = new StudyDAO(this.getDs());

        if (study.getParentStudyId() > 0) {
            // If the study has a parent than it is a site, in this case we
            // should get the event definitions of the parent
            StudyBean parentStudy = new StudyBean();
            parentStudy = (StudyBean) studyDao.findByPK(study.getParentStudyId());
            return super.findAllByStudy(parentStudy);
        } else {
            return super.findAllByStudy(study);
        }
    }

    public ArrayList findAllWithStudyEvent(StudyBean currentStudy) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(currentStudy.getId()));
        variables.put(new Integer(2), new Integer(currentStudy.getId()));

        ArrayList alist = this.select(digester.getQuery("findAllWithStudyEvent"), variables);

        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventDefinitionBean seb = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(seb);
        }

        return answer;
    }

    public ArrayList<StudyEventDefinitionBean> findAllByCrf(CRFBean crf) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crf.getId()));

        ArrayList alist = this.select(digester.getQuery("findAllByCrf"), variables);

        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventDefinitionBean seb = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(seb);
        }

        return answer;
    }

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventDefinitionBean eb = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public EntityBean findByPK(int ID) {

        StudyEventDefinitionBean eb = new StudyEventDefinitionBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;
    }

    /*
     * added tbh, 02/2008 (non-Javadoc)
     *
     * @see org.akaza.openclinica.dao.core.DAOInterface#findByPK(int)
     */
    public EntityBean findByName(String name) {

        StudyEventDefinitionBean eb = new StudyEventDefinitionBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), name);

        String sql = digester.getQuery("findByName");

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
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

    /**
     * @param eventDefinitionCRFId
     *            The id of an event definition crf.
     * @return the study event definition bean for the specified event
     *         definition crf.
     */
    public StudyEventDefinitionBean findByEventDefinitionCRFId(int eventDefinitionCRFId) {
        StudyEventDefinitionBean answer = new StudyEventDefinitionBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(eventDefinitionCRFId));

        String sql = digester.getQuery("findByEventDefinitionCRFId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;
    }

    public Collection findAllByStudyAndLimit(int studyId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyId));
        variables.put(new Integer(2), new Integer(studyId));
        ArrayList alist = this.select(digester.getQuery("findAllByStudyAndLimit"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventDefinitionBean eb = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;

    }

    public ArrayList<StudyEventDefinitionBean> findAllActiveByParentStudyId(int parentStudyId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(parentStudyId));
        ArrayList alist = this.select(digester.getQuery("findAllActiveByParentStudyId"), variables);
        ArrayList<StudyEventDefinitionBean> al = new ArrayList<StudyEventDefinitionBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyEventDefinitionBean eb = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    /**
     *
     * @param studySubjectId
     * @return
     */
    public Map<Integer, StudyEventDefinitionBean> findByStudySubject(int studySubjectId) {
        this.setTypesExpected(); // <== Must be called first
        HashMap<Integer, Object> param = new HashMap<Integer, Object>();
        param.put(1, studySubjectId);

        List selectResult = select(digester.getQuery("findByStudySubject"), param);

        Map<Integer,StudyEventDefinitionBean> result = new HashMap<Integer, StudyEventDefinitionBean>();

        Iterator it = selectResult.iterator();
        while (it.hasNext()) {
            StudyEventDefinitionBean bean = (StudyEventDefinitionBean) this.getEntityFromHashMap((HashMap) it.next());
            result.put(bean.getId(), bean);
        }
        return result;
    }

    /**
     *
     * @param studySubjectId
     * @return
     */
    public Map<Integer, Integer> buildMaxOrdinalByStudyEvent(int studySubjectId) {
        HashMap<Integer, Object> param = new HashMap<Integer, Object>();
        param.put(1, studySubjectId);

        List selectResult = select(digester.getQuery("buildMaxOrdinalByStudyEvent"), param);

        Map<Integer,Integer> result = new HashMap<Integer, Integer>();

        Iterator it = selectResult.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            result.put((Integer) hm.get("study_event_definition_id"), (Integer) hm.get("max_ord"));
        }
        return result;
    }

}