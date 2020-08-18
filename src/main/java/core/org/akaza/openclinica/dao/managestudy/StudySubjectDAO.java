/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.dao.managestudy;

import java.sql.Connection;
import java.sql.Types;
import java.util.*;

import javax.sql.DataSource;

import core.org.akaza.openclinica.bean.core.EntityBean;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import core.org.akaza.openclinica.bean.submit.FormLayoutBean;
import core.org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import core.org.akaza.openclinica.dao.StudySubjectSDVFilter;
import core.org.akaza.openclinica.dao.StudySubjectSDVSort;
import core.org.akaza.openclinica.dao.core.AuditableEntityDAO;
import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.core.DAODigester;
import core.org.akaza.openclinica.dao.core.SQLFactory;
import core.org.akaza.openclinica.dao.core.TypeNames;
import core.org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.UserStatus;
import org.apache.commons.lang.StringUtils;

/**
 * @author jxu
 *
 */
public class StudySubjectDAO<K extends String, V extends ArrayList> extends AuditableEntityDAO {

    // private DAODigester digester;

    public void setQueryNames() {
        findAllByStudyName = "findAllByStudy";
        findByPKAndStudyName = "findByPKAndStudy";
        getCurrentPKName = "getCurrentPK";

    }

    public StudySubjectDAO(DataSource ds) {
        super(ds);
        // digester = SQLFactory.getInstance().getDigester(digesterName);
        setQueryNames();
    }

    public StudySubjectDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDYSUBJECT;
    }

    @Override
    public void setTypesExpected() {
        // study_subject_id | integer | not null default
        // nextval('public.study_subject_study_subject_id_seq'::text)
        // label | character varying(30) |
        // secondary_label | character varying(30) |
        // subject_id | numeric |
        // study_id | numeric |
        // status_id | numeric |
        // enrollment_date | date |
        // date_created | date |
        // date_updated | date |
        // owner_id | numeric |
        // update_id | numeric |

        this.unsetTypeExpected();
        int ind = 1;

        ind = setStudySubjectTypeExpected(ind);
    }

    public void setTypesExpectedFilter() {

        this.unsetTypeExpected();
        int ind = 1;
        ind = setStudySubjectTypeExpected(ind);
    }

    public void setDNTypesExpected() {

        this.unsetTypeExpected();
        int ind = 1;

        ind = setStudySubjectTypeExpected(ind);
    }

    private int setStudySubjectTypeExpected(int ind){
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // study_subject_id
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // label
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // secondary_label
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // subject_id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // study_id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // status_id

        this.setTypeExpected(ind, TypeNames.DATE);
        ind++; // enrollment_date
        this.setTypeExpected(ind, TypeNames.DATE);
        ind++; // date_created
        this.setTypeExpected(ind, TypeNames.DATE);
        ind++; // date_updated
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // owner_id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // update_id
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // oc_oid
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // time_zone
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // user_id
        this.setTypeExpected(ind, TypeNames.INT);
        ind++; // user_status_id

        return ind;
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        StudySubjectBean eb = new StudySubjectBean();
        super.setEntityAuditInformation(eb, hm);
        // STUDY_SUBJECT_ID, LABEL, SUBJECT_ID, STUDY_ID
        // STATUS_ID, DATE_CREATED, OWNER_ID, STUDY_GROUP_ID
        // DATE_UPDATED, UPDATE_ID
        Integer ssid = (Integer) hm.get("study_subject_id");
        eb.setId(ssid.intValue());

        eb.setLabel((String) hm.get("label"));
        eb.setSubjectId(((Integer) hm.get("subject_id")).intValue());
        eb.setStudyId(((Integer) hm.get("study_id")).intValue());
        // eb.setStudyGroupId(((Integer) hm.get("study_group_id")).intValue());
        eb.setEnrollmentDate((Date) hm.get("enrollment_date"));
        eb.setSecondaryLabel((String) hm.get("secondary_label"));
        eb.setOid((String) hm.get("oc_oid"));
        eb.setStudyName((String) hm.get("unique_identifier"));
        // eb.setEventStartDate((Date) hm.get("date_start"));
        // eb.setActive(true);
        eb.setTime_zone((String) hm.get("time_zone"));
        eb.setUserId((Integer) hm.get("user_id"));

        for(UserStatus us:UserStatus.values()){
            if(us.getCode()==(Integer) hm.get("user_status_id")){
                eb.setUserStatus(us);
                break;
            }
        }
        return eb;
    }

    public ArrayList getGroupByStudySubject(int studySubjectId, int studyId, int parentStudyId) {

        HashMap variables = new HashMap();
        variables.put(1, studySubjectId);
        variables.put(2, studyId);
        variables.put(3, parentStudyId);

        return executeFindAllQuery("getGroupByStudySubject", variables);
    }

    @Override
    public Collection findAll() {
        this.setTypesExpected();
        String sql = digester.getQuery("findAll");
        ArrayList alist = this.select(sql);
        ArrayList answer = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudySubjectBean eb = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(eb);
        }
        return answer;
    }

    public ArrayList findAllByStudySDV(int studyId, int parentStudyId, StudySubjectSDVFilter filter, StudySubjectSDVSort sort, int rowStart, int rowEnd) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<StudySubjectBean>();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);
        String sql = digester.getQuery("findAllByStudySDV");
        sql = sql + filter.execute("");

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += ")x) where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }
        // System.out.println(sql);
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            StudySubjectBean studySubjectBean = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public int countAllByStudySDV(int studyId, int parentStudyId, StudySubjectSDVFilter filter) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(1, studyId);
        variables.put(2, parentStudyId);

        String sql = digester.getQuery("countAllByStudySDV");
        sql += filter.execute("");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return 0;
        }
    }

    public int findTheGreatestLabel() {
        this.setTypesExpected();
        String sql = digester.getQuery("findAll");
        ArrayList alist = this.select(sql);
        ArrayList answer = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudySubjectBean eb = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(eb);
        }

        int greatestLabel = 0;
        for (int i = 0; i < answer.size(); i++) {
            StudySubjectBean sb = (StudySubjectBean) answer.get(i);
            int labelInt = 0;
            try {
                labelInt = Integer.parseInt(sb.getLabel());
            } catch (NumberFormatException ne) {
                labelInt = 0;
            }
            if (labelInt > greatestLabel) {
                greatestLabel = labelInt;
            }
        }
        return greatestLabel;
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public ArrayList findAllByStudyOrderByLabel(Study sb) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sb.getStudyId()));
        variables.put(new Integer(2), new Integer(sb.getStudyId()));

        return executeFindAllQuery("findAllByStudyOrderByLabel", variables);
    }

    public ArrayList findAllActiveByStudyOrderByLabel(Study sb) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(sb.getStudyId()));
        variables.put(new Integer(2), new Integer(sb.getStudyId()));

        return executeFindAllQuery("findAllActiveByStudyOrderByLabel", variables);
    }

    public ArrayList findAllWithStudyEvent(Study currentStudy) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(currentStudy.getStudyId()));
        variables.put(new Integer(2), new Integer(currentStudy.getStudyId()));

        String sql = digester.getQuery("findAllWithStudyEvent");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            StudySubjectBean ssb = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ssb);
        }

        return answer;
    }

    public ArrayList findAllBySubjectId(int subjectId) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(subjectId));

        String sql = digester.getQuery("findAllBySubjectId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            StudySubjectBean ssb = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ssb);
        }

        return answer;
    }

    public ArrayList findAllBySiteId(int siteId) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(siteId));

        String sql = digester.getQuery("findAllBySitetId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            StudySubjectBean ssb = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(ssb);
        }

        return answer;
    }

    public EntityBean findAnotherBySameLabel(String label, int studyId, int studySubjectId) {
        StudySubjectBean eb = new StudySubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), label);
        variables.put(new Integer(2), new Integer(studyId));
        variables.put(new Integer(3), new Integer(studySubjectId));

        String sql = digester.getQuery("findAnotherBySameLabel");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    public EntityBean findAnotherBySameLabelInSites(String label, int studyId, int studySubjectId) {
        StudySubjectBean eb = new StudySubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), label);
        variables.put(new Integer(2), new Integer(studyId));
        variables.put(new Integer(3), new Integer(studySubjectId));

        String sql = digester.getQuery("findAnotherBySameLabelInSites");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    @Override
    public EntityBean findByPK(int ID) {
        StudySubjectBean eb = new StudySubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    public StudySubjectBean findByLabel(String label) {
        StudySubjectBean answer = new StudySubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), label);

        String sql = digester.getQuery("findByLabel");

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;
    }
    /**
     * Finds a study subject which has the same label (without case-sensitive) provided in the same study
     *This method is called only during creation of participants
     * @param label
     * @param siteOrStudyId
     * @return
     */
    public StudySubjectBean findByLabelAndStudyForCreatingParticipant(String label, int siteOrStudyId) {
        StudySubjectBean answer = new StudySubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), label);
        variables.put(new Integer(2), new Integer(siteOrStudyId));
        variables.put(new Integer(3), new Integer(siteOrStudyId));

        String sql = digester.getQuery("findByLabelAndStudyWithoutCaseSensitiveLabel");

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;
    }

    public List<StudySubjectBean> findAllSubjectsByLabelAndStudy(String label, Study study) {
        List<StudySubjectBean> answerList = new ArrayList<>();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), label);
        variables.put(new Integer(2), new Integer(study.getStudyId()));
        variables.put(new Integer(3), new Integer(study.getStudyId()));

        answerList=executeFindAllQuery("findByLabelAndStudyWithoutCaseSensitiveLabel",variables);

        return answerList;
    }
    public StudySubjectBean findByLabelAndStudy(String label, Study study) {
        StudySubjectBean answer = new StudySubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), label);
        variables.put(new Integer(2), new Integer(study.getStudyId()));
        variables.put(new Integer(3), new Integer(study.getStudyId()));

        String sql=null;
        sql = digester.getQuery("findByLabelAndStudy");

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;
    }
    public StudySubjectBean findByLabelAndOnlyByStudy(String label, Study study) {
        StudySubjectBean answer = new StudySubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), label);
        variables.put(new Integer(2), new Integer(study.getStudyId()));

        String sql = digester.getQuery("findByLabelAndOnlyByStudy");

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;
    }

    /**
     * @deprecated Creates a new studysubject
     */
    @Override
    @Deprecated
    public EntityBean create(EntityBean eb) {
        StudySubjectBean sb = (StudySubjectBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();

        // INSERT INTO study_subject
        // (LABEL, SUBJECT_ID, STUDY_ID, STATUS_ID,
        // DATE_CREATED, OWNER_ID, ENROLLMENT_DATE,SECONDARY_LABEL)
        // VALUES (?,?,?,?,NOW(),?,?,?)

        int ind = 1;
        variables.put(new Integer(ind), sb.getLabel());
        ind++;
        variables.put(new Integer(ind), new Integer(sb.getSubjectId()));
        ind++;
        variables.put(new Integer(ind), new Integer(sb.getStudyId()));
        ind++;
        variables.put(new Integer(ind), new Integer(sb.getStatus().getId()));
        ind++;
        // Date_created is now()
        variables.put(new Integer(ind), new Integer(sb.getOwnerId()));
        ind++;
        // variables.put(new Integer(ind), new Integer(sb.getStudyGroupId()));
        // ind++;
        if (sb.getEnrollmentDate() == null) {
            nullVars.put(new Integer(ind), new Integer(Types.DATE));
            variables.put(new Integer(ind), null);
            ind++;
        } else {
            variables.put(new Integer(ind), sb.getEnrollmentDate());
            ind++;
        }
        variables.put(new Integer(ind), sb.getSecondaryLabel());
        ind++;

        this.execute(digester.getQuery("create"), variables, nullVars);

        if (isQuerySuccessful()) {
            sb.setId(getCurrentPK());
        }

        return sb;
    }

    /**
     * Create a study subject (that is, enroll a subject in a study).
     *
     * @param sb
     *            The study subject to create.
     * @param withGroup
     *            <code>true</code> if the group id has been set (primarily
     *            for use with genetic studies); <code>false</false> otherwise.
     * @return The study subject with id set to the insert id if the operation
     *         was successful, or 0 otherwise.
     */
    public StudySubjectBean create(StudySubjectBean sb, boolean withGroup) {
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();

        int ind = 1;
        variables.put(new Integer(ind), sb.getLabel());
        ind++;
        variables.put(new Integer(ind), new Integer(sb.getSubjectId()));
        ind++;
        variables.put(new Integer(ind), new Integer(sb.getStudyId()));
        ind++;
        variables.put(new Integer(ind), new Integer(sb.getStatus().getId()));
        ind++;
        // Date_created is now()
        variables.put(new Integer(ind), new Integer(sb.getOwner().getId()));
        ind++;

        // if (withGroup) {
        // variables.put(new Integer(ind), new Integer(sb.getStudyGroupId()));
        // ind++;
        // } else {
        // nullVars.put(new Integer(ind), new Integer(TypeNames.INT));
        // variables.put(new Integer(ind), null);
        // ind++;
        // }

        Date enrollmentDate = sb.getEnrollmentDate();
        if (enrollmentDate == null) {
            nullVars.put(new Integer(ind), new Integer(Types.DATE));
            variables.put(new Integer(ind), null);
            ind++;
        } else {
            variables.put(new Integer(ind), enrollmentDate);
            ind++;
        }

        String secondLabel = sb.getSecondaryLabel();
        if(secondLabel == null) {
        	nullVars.put(new Integer(ind), new Integer(Types.VARCHAR));
            variables.put(new Integer(ind), null);
            ind++;
        }else {
        	 variables.put(new Integer(ind), sb.getSecondaryLabel());
             ind++;
        }

        variables.put(new Integer(ind), getValidOid(sb));
        ind++;

        this.executeWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            sb.setId(getLatestPK());
        }

        SubjectGroupMapDAO sgmdao = new SubjectGroupMapDAO(ds);
        ArrayList groupMaps = sb.getStudyGroupMaps();
        for (int i = 0; i < groupMaps.size(); i++) {
            SubjectGroupMapBean sgmb = (SubjectGroupMapBean) groupMaps.get(i);
            sgmb = (SubjectGroupMapBean) sgmdao.create(sgmb);
            if (sgmdao.isQuerySuccessful()) {
                sgmb.setId(sgmdao.getCurrentPK());
            }
        }

        return sb;
    }

    public StudySubjectBean createWithGroup(StudySubjectBean sb) {
        return create(sb, true);
    }

    public StudySubjectBean createWithoutGroup(StudySubjectBean sb) {
        return create(sb, false);
    }

    /**
     * Creates a valid OID for the StudySubject
     */
    private String getOid(StudySubjectBean ssb) {

        String oid;
        try {
            oid = ssb.getOid() != null ? ssb.getOid() : ssb.getOidGenerator().generateOid(ssb.getLabel());
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    private String getValidOid(StudySubjectBean ssb) {

        String oid = getOid(ssb);
        logger.debug(oid);
        String oidPreRandomization = oid;
        while (findByOid(oid) != null) {
            oid = ssb.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;

    }

    public StudySubjectBean findByOidAndStudy(String oid, int studyId) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);
        variables.put(new Integer(2), new Integer(studyId));
        variables.put(new Integer(3), new Integer(studyId));
        String sql = digester.getQuery("findByOidAndStudy");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            studySubjectBean = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            return studySubjectBean;
        } else {
        	logger.info(sql+"WARNING: cannot find StudySubjectBean by oid " + oid + " and study id " + studyId);
            return null;
        }
    }

    public StudySubjectBean findByOid(String oid) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);
        String sql = digester.getQuery("findByOid");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            studySubjectBean = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            return studySubjectBean;
        } else {
            return null;
        }
    }


    public ArrayList<StudySubjectBean> getWithFilterAndSort(Study currentStudy, FindSubjectsFilter filter, FindSubjectsSort sort, int rowStart,
                                                            int rowEnd) {
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<StudySubjectBean>();
        unsetTypeExpected();
        int variableIndex=0;
        int ind=1;
        ind= setStudySubjectTypeExpected(ind);

        String partialSql;
        HashMap variables = new HashMap();

        String sql;
        sql = digester.getQuery("getWithFilterAndSort");
        variables.put(new Integer(++variableIndex), currentStudy.getStudyId());
        variables.put(new Integer(++variableIndex), currentStudy.getStudyId());

        sql = sql + filter.execute("");

        if(sort!=null) {
            partialSql = sort.execute("");

            if (!StringUtils.isEmpty(partialSql)) {
                int filterCountInPartialSql = StringUtils.countMatches(partialSql, "FILTERED_SQL");
                filterCountInPartialSql = filterCountInPartialSql * 2 - 2;

                if (filterCountInPartialSql > 0)
                    this.setTypeExpected(ind + 1, TypeNames.STRING);

                for (int i = 0; i < filterCountInPartialSql; i++) {
                    variables.put(new Integer(++variableIndex), currentStudy.getStudyId());
                }
                sql = partialSql.replaceAll("FILTERED_SQL", sql);
            } else {
                sql = sql + " order by label asc ";
            }
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            StudySubjectBean studySubjectBean = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public Integer getCountofStudySubjectsAtStudyOrSite(Study currentStudy) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        String sql = digester.getQuery("getCountofStudySubjectsAtStudyOrSite");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getTotalCountStudySubjectForCrfMigration(FormLayoutBean sourceCrfVersionBean, FormLayoutBean targetCrfVersionBean,
            ArrayList<String> studyEventDefnlist, ArrayList<String> sitelist) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        String eventStr = StringUtils.join(studyEventDefnlist, ",");
        String siteStr = StringUtils.join(sitelist, ",");
        variables.put(new Integer(1), new Integer(sourceCrfVersionBean.getId()));
        variables.put(2, eventStr);
        variables.put(3, siteStr);
        variables.put(4, String.valueOf(sourceCrfVersionBean.getId()));
        variables.put(5, String.valueOf(targetCrfVersionBean.getId()));

        String sql = digester.getQuery("getTotalCountStudySubjectForCrfMigration");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getTotalEventCrfCountForCrfMigration(FormLayoutBean sourceCrfVersionBean, FormLayoutBean targetCrfVersionBean,
            ArrayList<String> studyEventDefnlist, ArrayList<String> sitelist) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        String eventStr = StringUtils.join(studyEventDefnlist, ",");
        String siteStr = StringUtils.join(sitelist, ",");
        variables.put(new Integer(1), new Integer(sourceCrfVersionBean.getId()));
        variables.put(2, eventStr);
        variables.put(3, siteStr);
        variables.put(4, String.valueOf(sourceCrfVersionBean.getId()));
        variables.put(5, String.valueOf(targetCrfVersionBean.getId()));

        String sql = digester.getQuery("getTotalEventCrfCountForCrfMigration");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getCountofStudySubjectsAtStudy(Study currentStudy) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        String sql = digester.getQuery("getCountofStudySubjectsAtStudy");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getCountofStudySubjects(Study currentStudy) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        String sql = digester.getQuery("getCountofStudySubjects");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getCountofActiveStudySubjects() {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        String sql = digester.getQuery("getCountOfActiveStudySubjects");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getCountofStudySubjectsBasedOnStatus(Study currentStudy, Status status) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        variables.put(new Integer(3), status.getId());
        String sql = digester.getQuery("getCountofStudySubjectsBasedOnStatus");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getCountWithFilter(ListDiscNotesSubjectFilter filter, Study currentStudy) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        String sql = digester.getQuery("getCountWithFilterListDiscNotes");
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

    public ArrayList<StudySubjectBean> getWithFilterAndSort(Study currentStudy, ListDiscNotesSubjectFilter filter, ListDiscNotesSubjectSort sort,
            int rowStart, int rowEnd) {
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<StudySubjectBean>();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        String sql = digester.getQuery("getWithFilterAndSortListDiscNotes");
        sql = sql + filter.execute("");

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += " )x)  where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            StudySubjectBean studySubjectBean = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public Integer getCountWithFilter(ListDiscNotesForCRFFilter filter, Study currentStudy) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        String sql = digester.getQuery("getCountWithFilterListDiscNotes");
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

    public ArrayList<StudySubjectBean> getWithFilterAndSort(Study currentStudy, ListDiscNotesForCRFFilter filter, ListDiscNotesForCRFSort sort,
            int rowStart, int rowEnd) {
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<StudySubjectBean>();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        String sql = digester.getQuery("getWithFilterAndSortListDiscNotes");
        sql = sql + filter.execute("");

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += " )x)  where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            StudySubjectBean studySubjectBean = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public Integer getCountWithFilter(FindSubjectsFilter filter, Study currentStudy , UserStatus participateStatusSetFilter) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        String sql = "";
        boolean filterIsEmpty = false;
       
        if (filter.getFilters().isEmpty()){
            sql = digester.getQuery("getStudySubjects");
            filterIsEmpty = true;
        }
        else {
             sql = digester.getQuery("getWithFilterAndSort");
        }

        if(participateStatusSetFilter ==null) {
            variables.put(new Integer(1), currentStudy.getStudyId());
            variables.put(new Integer(2), currentStudy.getStudyId());
        }else{
        	if(filterIsEmpty) {
        		sql=sql + " WHERE subss.user_status_id=?";
        	}else {
        		sql=sql + " AND ss.user_status_id=?";
        	}
          
            variables.put(new Integer(1), currentStudy.getStudyId());
            variables.put(new Integer(2), currentStudy.getStudyId());
            variables.put(new Integer(3), participateStatusSetFilter.getCode());
        }

        sql += filter.execute("");

        ArrayList rows = this.select(sql, variables);
        if(rows!=null)
            return rows.size();

        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public ArrayList<StudySubjectBean> getWithFilterAndSort(Study currentStudy, StudyAuditLogFilter filter, StudyAuditLogSort sort, int rowStart,
            int rowEnd) {
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<StudySubjectBean>();
        setTypesExpectedFilter();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        String sql = digester.getQuery("getWithFilterAndSortAuditLog");
        sql = sql + filter.execute("");

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += " )x) where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        // System.out.println("SQL: " + sql);
        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            StudySubjectBean studySubjectBean = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public Integer getCountWithFilter(StudyAuditLogFilter filter, Study currentStudy) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        String sql = digester.getQuery("getCountWithFilterAuditLog");
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

    public ArrayList<StudySubjectBean> getWithFilterAndSort(Study currentStudy, ListEventsForSubjectFilter filter, ListEventsForSubjectSort sort,
            int rowStart, int rowEnd) {
        ArrayList<StudySubjectBean> studySubjects = new ArrayList<StudySubjectBean>();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        String sql = digester.getQuery("getWithFilterAndSort");

        sql = sql + filter.execute("");

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += ")x) where r between " + (rowStart + 1) + " and " + rowEnd + " ";
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            StudySubjectBean studySubjectBean = (StudySubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            studySubjects.add(studySubjectBean);
        }
        return studySubjects;
    }

    public Integer getCountWithFilter(ListEventsForSubjectFilter filter, Study currentStudy) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), currentStudy.getStudyId());
        variables.put(new Integer(2), currentStudy.getStudyId());
        String sql = digester.getQuery("getCountWithFilter");
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

    /**
     * Updates a StudySubject
     */
    @Override
    public EntityBean update(EntityBean eb) {
        Connection con = null;
        return update(eb, con);
    }

    /* this function allows to run transactional updates for an action */
    public EntityBean update(EntityBean eb, Connection con) {
        StudySubjectBean sb = (StudySubjectBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();

        // UPDATE study_subject SET LABEL=?, SUBJECT_ID=?, STUDY_ID=?,
        // STATUS_ID=?, ENROLLMENT_DATE=?, DATE_UPDATED=?,
        // UPDATE_ID=?, SECONDARY_LABEL=? WHERE STUDY_SUBJECT_ID=?
        int ind = 1;
        variables.put(new Integer(ind), sb.getLabel());
        ind++;
        variables.put(new Integer(ind), new Integer(sb.getSubjectId()));
        ind++;
        variables.put(new Integer(ind), new Integer(sb.getStudyId()));
        ind++;
        variables.put(new Integer(ind), new Integer(sb.getStatus().getId()));
        ind++;
        Date enrollmentDate = sb.getEnrollmentDate();
        if (enrollmentDate == null) {
            nullVars.put(new Integer(ind), new Integer(Types.DATE));
            variables.put(new Integer(ind), null);
            ind++;
        } else {
            variables.put(new Integer(ind), enrollmentDate);
            ind++;
        }
        // date_updated is set to now()
        // variables.put(new Integer(ind), new java.util.Date());
        // ind++;
        variables.put(new Integer(ind), new Integer(sb.getUpdater().getId()));
        ind++;
        variables.put(new Integer(ind), sb.getSecondaryLabel());
        ind++;
        if (sb.getTime_zone() == null || sb.getTime_zone().equals("")) {
            nullVars.put(new Integer(ind), new Integer(TypeNames.STRING));
            variables.put(new Integer(ind), "");
        } else {
            variables.put(new Integer(ind), sb.getTime_zone());
        }
        ind++;
        if (sb.getUserId() == 0 ) {
            nullVars.put(new Integer(ind), new Integer(TypeNames.STRING));
            variables.put(new Integer(ind), null);
        } else {
            variables.put(new Integer(ind), sb.getUserId());
        }
        ind++;

        if(sb.getUserStatus() == null) {
            nullVars.put(new Integer(ind), new Integer(Types.VARCHAR));
            variables.put(new Integer(ind), null);
            ind++;
        }else{
            variables.put(new Integer(ind), sb.getUserStatus().getCode());
            ind++;
        }


        variables.put(new Integer(ind), new Integer(sb.getId()));
        ind++;

        String sql = digester.getQuery("update");
        if (con == null) {
            this.execute(sql, variables, nullVars);
        } else {
            this.execute(sql, variables, nullVars, con);
        }
        return sb;
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

    public StudySubjectBean findBySubjectIdAndStudy(int subjectId, Study study) {
        StudySubjectBean answer = new StudySubjectBean();

        this.unsetTypeExpected();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(subjectId));
        variables.put(new Integer(2), new Integer(study.getStudyId()));
        variables.put(new Integer(3), new Integer(study.getStudyId()));

        String sql = digester.getQuery("findBySubjectIdAndStudy");

        ArrayList results = select(sql, variables);
        if (results.size() > 0) {
            // logger.info("result size is >0");
            HashMap row = (HashMap) results.get(0);
            answer = (StudySubjectBean) getEntityFromHashMap(row);
        }

        return answer;
    }

    public ArrayList findAllByStudyId(int studyId) {
        return findAllByStudyIdAndLimit(studyId, false);
    }

    public ArrayList findAllByStudyIdAndLimit(int studyId, boolean isLimited) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();
        int ind = 1;
        this.setTypeExpected(ind, TypeNames.STRING);
        ind++; // unique_identifier
        this.setTypeExpected(ind, TypeNames.CHAR);
        ind++; // gender

        ind = setStudySubjectTypeExpected(ind);

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyId));
        variables.put(new Integer(2), new Integer(studyId));

        String sql = null;
        if (isLimited) {
            sql = digester.getQuery("findAllByStudyIdAndLimit");
        } else {
            sql = digester.getQuery("findAllByStudyId");
        }
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            StudySubjectBean ssb = (StudySubjectBean) this.getEntityFromHashMap(hm);
            ssb.setUniqueIdentifier((String) hm.get("unique_identifier"));
            ssb.setStudyName((String) hm.get("name"));
            // logger.info("gender here:" + hm.get("gender").getClass());
            try {
                if (hm.get("gender") == null || ((String) hm.get("gender")).equals(" ")) {
                    logger.debug("here");
                    ssb.setGender(' ');

                } else {
                    String gender = (String) hm.get("gender");
                    char[] genderarr = gender.toCharArray();
                    ssb.setGender(genderarr[0]);
                }
            } catch (ClassCastException ce) {
                // object type is Character
                ssb.setGender(' ');
            }

            answer.add(ssb);
        }

        return answer;
    }

    public String findStudySubjectIdsByStudyIds(String studyIds) {
        String studySubjectIds = "";
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.STRING);
        ArrayList alist = this.select("select study_subject_id from study_subject where study_id in (" + studyIds + ")");
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            studySubjectIds += (String) hm.get("study_subject_id") + ",";
        }
        studySubjectIds = studySubjectIds.endsWith(",") ? studySubjectIds.substring(0, studySubjectIds.length() - 1) : studySubjectIds;
        return studySubjectIds;
    }
    // Jn: Commenting out the studySubjectDao's caching since its used only in one place in dataentry and is causing
    // issues when trying to add new subject to a study event via createNewStudyEvent
    /*
     * @Override
     * public ArrayList<V> select(String query, HashMap variables) {
     * clearSignals();
     * 
     * ArrayList results = new ArrayList();
     * V value;
     * K key;
     * ResultSet rs = null;
     * Connection con = null;
     * PreparedStatementFactory psf = new PreparedStatementFactory(variables);
     * PreparedStatement ps = null;
     * 
     * try {
     * con = ds.getConnection();
     * if (con.isClosed()) {
     * if (logger.isWarnEnabled())
     * logger.warn("Connection is closed: GenericDAO.select!");
     * throw new SQLException();
     * }
     * 
     * ps = con.prepareStatement(query);
     * 
     * 
     * ps = psf.generate(ps);// enter variables here!
     * key = (K) ps.toString();
     * if((results=(V) cache.get(key))==null)
     * {
     * rs = ps.executeQuery();
     * results = this.processResultRows(rs);
     * if(results!=null){
     * cache.put(key,results);
     * }
     * }
     * 
     * if (logger.isInfoEnabled()) {
     * logger.info("Executing dynamic query, EntityDAO.select:query " + query);
     * }
     * signalSuccess();
     * 
     * 
     * } catch (SQLException sqle) {
     * signalFailure(sqle);
     * if (logger.isWarnEnabled()) {
     * logger.warn("Exception while executing dynamic query, GenericDAO.select: " + query + ":message: " +
     * sqle.getMessage());
     * sqle.printStackTrace();
     * }
     * } finally {
     * this.closeIfNecessary(con, rs, ps);
     * }
     * return results;
     * 
     * }
     */
}
