/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.submit;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.sql.DataSource;

/**
 * @author jxu
 */
public class SubjectDAO extends AuditableEntityDAO {
    // private DataSource ds;
    // private DAODigester digester;
    // protected String

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPrimaryKey";
    }

    public SubjectDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public SubjectDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public SubjectDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_SUBJECT;
    }

    @Override
    public void setTypesExpected() {
        // SERIAL, NUMERIC, NUMERIC, NUMERIC,
        // DATE, CHAR(1), VARCHAR(255),DATE,
        // NUMERIC, DATE, NUMERIC
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.INT);
        this.setTypeExpected(5, TypeNames.DATE);
        this.setTypeExpected(6, TypeNames.CHAR);
        this.setTypeExpected(7, TypeNames.STRING);
        this.setTypeExpected(8, TypeNames.DATE);
        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.DATE);
        this.setTypeExpected(11, TypeNames.INT);
        this.setTypeExpected(12, TypeNames.BOOL);
        this.setTypeExpected(13, TypeNames.STRING);

    }

    /**
     * findAllSubjectsAndStudies()
     *
     * For every subject find all studies that subject belongs to.
     *
     * smw
     *
     */
    public ArrayList findAllSubjectsAndStudies() {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();
        this.setTypeExpected(13, TypeNames.CHAR); // label from study_subject table
        this.setTypeExpected(14, TypeNames.CHAR); // unique_identifier from study table

        String sql = digester.getQuery("findAllSubjectsAndStudies");

        ArrayList alist = this.select(sql);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            SubjectBean sb = (SubjectBean) this.getEntityFromHashMap(hm);
            sb.setLabel((String) hm.get("label"));
            sb.setStudyIdentifier((String) hm.get("study_unique_identifier"));

            answer.add(sb);
        }

        return answer;
    }

    /**
     * @param gender
     *            Use 'm' for males, 'f' for females.
     * @return All subjects who are male, if <code>gender == 'm'</code>, or all
     *         subjects who are female, if <code>gender == 'f'</code>, or a
     *         blank list, otherwise.
     */
    public ArrayList findAllByGender(char gender) {
        if (gender == 'm') {
            return findAllMales();
        } else if (gender == 'f') {
            return findAllFemales();
        }
        return new ArrayList();
    }

    public ArrayList findAllFemales() {

        return executeFindAllQuery("findAllFemales");
    }

    public ArrayList findAllMales() {

        return executeFindAllQuery("findAllMales");
    }

    /**
     * @param gender
     *            Use 'm' for males, 'f' for females, not include himself.
     * @return All subjects who are male, if <code>gender == 'm'</code>, or all
     *         subjects who are female, if <code>gender == 'f'</code>, or a
     *         blank list, otherwise.
     */
    public ArrayList findAllByGenderNotSelf(char gender, int id) {
        if (gender == 'm') {
            return findAllMalesNotSelf(id);
        } else if (gender == 'f') {
            return findAllFemalesNotSelf(id);
        }
        return new ArrayList();
    }

    public ArrayList findAllFemalesNotSelf(int id) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(id));
        return executeFindAllQuery("findAllFemalesNotSelf", variables);
    }

    public ArrayList findAllMalesNotSelf(int id) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(id));
        return executeFindAllQuery("findAllMalesNotSelf", variables);
    }

    public ArrayList<SubjectBean> getWithFilterAndSort(StudyBean currentStudy, ListSubjectFilter filter, ListSubjectSort sort, int rowStart, int rowEnd) {
        ArrayList<SubjectBean> subjects = new ArrayList<SubjectBean>();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        String sql = digester.getQuery("getWithFilterAndSort");
        sql = sql + filter.execute("");

        if (CoreResources.getDBName().equals("oracle")) {
            sql += " )x)where r between " + (rowStart + 1) + " and " + rowEnd;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        //        System.out.println("SQL: "+sql);
        ArrayList rows = this.select(sql);
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            SubjectBean subjectBean = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            subjects.add(subjectBean);
        }
        return subjects;
    }

    public Integer getCountWithFilter(ListSubjectFilter filter, StudyBean currentStudy) {
        StudySubjectBean studySubjectBean = new StudySubjectBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        String sql = digester.getQuery("getCountWithFilter");
        sql += filter.execute("");

        ArrayList rows = this.select(sql);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    public Object getEntityFromHashMap(HashMap hm) {
        SubjectBean eb = new SubjectBean();
        super.setEntityAuditInformation(eb, hm);
        eb.setId(((Integer) hm.get("subject_id")).intValue());
        Date birthday = (Date) hm.get("date_of_birth");
        eb.setDateOfBirth(birthday);
        try {
            String gender = (String) hm.get("gender");
            char[] genderarr = gender.toCharArray();
            eb.setGender(genderarr[0]);
        } catch (ClassCastException ce) {
            eb.setGender(' ');
        }
        eb.setUniqueIdentifier((String) hm.get("unique_identifier"));
        eb.setDobCollected(((Boolean) hm.get("dob_collected")).booleanValue());

        return eb;
    }

    public Collection findAll() {

        return findAllByLimit(false);
    }

    public Collection findAllByLimit(boolean hasLimit) {
        this.setTypesExpected();
        ArrayList alist = null;
        if (hasLimit) {
            alist = this.select(digester.getQuery("findAllByLimit"));
        } else {
            alist = this.select(digester.getQuery("findAll"));
        }
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            SubjectBean eb = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public EntityBean findAnotherByIdentifier(String name, int subjectId) {
        SubjectBean eb = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), name);
        variables.put(Integer.valueOf(2), Integer.valueOf(subjectId));

        String sql = digester.getQuery("findAnotherByIdentifier");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;
    }

    public Collection findAllChildrenByPK(int subjectId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(subjectId));
        variables.put(Integer.valueOf(2), Integer.valueOf(subjectId));
        ArrayList alist = this.select(digester.getQuery("findAllChildrenByPK"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            SubjectBean eb = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    /**
     * Finds the subject which has the given identifier and is inside given
     * study
     *
     * @param uniqueIdentifier
     * @param studyId
     * @return
     */

    public SubjectBean findByUniqueIdentifierAndAnyStudy(String uniqueIdentifier, int studyId) {
        SubjectBean answer = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), uniqueIdentifier);
        variables.put(Integer.valueOf(2), Integer.valueOf(studyId));
        variables.put(Integer.valueOf(3), Integer.valueOf(studyId));

        String sql = digester.getQuery("findByUniqueIdentifierAndAnyStudy");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;

    }

    
    public SubjectBean findByUniqueIdentifierAndStudy(String uniqueIdentifier, int studyId) {
        SubjectBean answer = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), uniqueIdentifier);
        variables.put(Integer.valueOf(2), Integer.valueOf(studyId));

        String sql = digester.getQuery("findByUniqueIdentifierAndStudy");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;

    }

    /**
     * Finds the subject which has the given identifier and is inside given
     * study
     *
     * @param uniqueIdentifier
     * @param studyId
     * @return
     */
    public SubjectBean findByUniqueIdentifierAndParentStudy(String uniqueIdentifier, int studyId) {
        SubjectBean answer = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), uniqueIdentifier);
        variables.put(Integer.valueOf(2), Integer.valueOf(studyId));

        String sql = digester.getQuery("findByUniqueIdentifierAndParentStudy");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;

    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public EntityBean findByPK(int ID) {
        SubjectBean eb = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    /**
     * @deprecated Creates a new subject
     */
    @Deprecated
    public EntityBean create(EntityBean eb) {
        SubjectBean sb = (SubjectBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        // FATHER_ID,MOTHER_ID, STATUS_ID,
        // DATE_OF_BIRTH,GENDER,UNIQUE_IDENTIFIER,DATE_CREATED,
        // OWNER_ID
        variables.put(Integer.valueOf(1), Integer.valueOf(sb.getStatus().getId()));
        if (sb.getDateOfBirth() == null) {
            nullVars.put(Integer.valueOf(4), Integer.valueOf(Types.DATE));
            variables.put(Integer.valueOf(4), null);
        } else {
            variables.put(Integer.valueOf(4), sb.getDateOfBirth());
        }
        if (sb.getGender() != 'm' && sb.getGender() != 'f') {
            nullVars.put(Integer.valueOf(5), Integer.valueOf(Types.CHAR));
            variables.put(Integer.valueOf(5), null);
        } else {
            char[] ch = { sb.getGender() };
            variables.put(Integer.valueOf(5), new String(ch));
        }

        variables.put(Integer.valueOf(6), sb.getUniqueIdentifier());
        // DATE_CREATED is now()
        variables.put(Integer.valueOf(7), Integer.valueOf(sb.getOwner().getId()));

        execute(digester.getQuery("create"), variables, nullVars);

        if (isQuerySuccessful()) {
            sb.setId(getCurrentPK());
        }

        return sb;
    }

    /**
     * Create a subject.
     *
     * @param sb
     *            The subject to create. <code>true</code> if the father and
     *            mother id have been properly set; primarily for use with
     *            genetic studies. <code>false</code> if the father and mother
     *            id have not been properly set; primarily for use with
     *            non-genetic studies.
     * @return
     */
    public SubjectBean create(SubjectBean sb) {
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        logger.debug("Logged in subject DAO.create");
        int ind = 1;

        

        variables.put(Integer.valueOf(ind), Integer.valueOf(sb.getStatus().getId()));
        ind++;

        if (sb.getDateOfBirth() == null) {
            nullVars.put(Integer.valueOf(ind), Integer.valueOf(Types.DATE));
            variables.put(Integer.valueOf(ind), null);
        } else {
            variables.put(Integer.valueOf(ind), sb.getDateOfBirth());
        }
        ind++;

        if (sb.getGender() != 'm' && sb.getGender() != 'f') {
            nullVars.put(Integer.valueOf(ind), Integer.valueOf(Types.CHAR));
            variables.put(Integer.valueOf(ind), null);
        } else {
            char[] ch = { sb.getGender() };
            variables.put(Integer.valueOf(ind), new String(ch));
        }
        ind++;
        variables.put(Integer.valueOf(ind), sb.getUniqueIdentifier());
        ind++;
        variables.put(Integer.valueOf(ind), Integer.valueOf(sb.getOwnerId()));
        ind++;
        variables.put(Integer.valueOf(ind), Boolean.valueOf(sb.isDobCollected()));
        ind++;

        executeWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            sb.setId(getLatestPK());
        }

        return sb;
    }

    /**
     * Create a subject whose father and mother id have been properly set. This
     * is primarily for use when creating subjects in genetic studies.
     *
     * @param sb
     *            The subject to create.
     * @return The created subject, with id set according to the insert id if
     *         the operation was successful, or id set to 0 otherwise.
     * @deprecated
     */
    @Deprecated
    public SubjectBean createWithParents(SubjectBean sb) {
        return create(sb);
    }

    /**
     * Create a subject whose father and mother id have not been properly set.
     * This is primarily for use when creating subjects in non-genetic studies.
     *
     * @param sb
     *            The subject to create.
     * @return The created subject, with id set according to the insert id if
     *         the operation was successful, or id set to 0 otherwise.
     * @deprecated
     */
    @Deprecated
    public SubjectBean createWithoutParents(SubjectBean sb) {
        return create(sb);
    }

    public SubjectBean findByUniqueIdentifier(String uniqueIdentifier) {
        SubjectBean answer = new SubjectBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), uniqueIdentifier);

        String sql = digester.getQuery("findByUniqueIdentifier");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            answer = (SubjectBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;
    }

    /**
     * <b>update </b>, the method that returns an updated subject bean after it
     * updates the database.
     *
     * @return sb, an updated study bean.
     */
    public EntityBean update(EntityBean eb) {
        SubjectBean sb = (SubjectBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();

        // UPDATE subject SET FATHER_ID=?,MOTHER_ID=?, STATUS_ID=?,
        // DATE_OF_BIRTH=?,GENDER=?,UNIQUE_IDENTIFIER=?, DATE_UPDATED=?,
        // UPDATE_ID=? DOB_COLLECTED=? WHERE SUBJECT_ID=?
        // YW <<
        int ind = 1;
        variables.put(Integer.valueOf(ind++), Integer.valueOf(sb.getStatus().getId()));
        if (sb.getDateOfBirth() != null) {
            variables.put(Integer.valueOf(ind), sb.getDateOfBirth());
        } else {
            nullVars.put(Integer.valueOf(ind), Integer.valueOf(Types.DATE));
            variables.put(Integer.valueOf(ind), null);
        }
        ind++;
        if (sb.getGender() != 'm' && sb.getGender() != 'f') {
            nullVars.put(Integer.valueOf(ind), Integer.valueOf(Types.CHAR));
            variables.put(Integer.valueOf(ind), null);
        } else {
            char[] ch = { sb.getGender() };
            variables.put(Integer.valueOf(ind), new String(ch));
        }
        ind++;
        variables.put(Integer.valueOf(ind++), new String(sb.getUniqueIdentifier()));
        // date_updated is set to now()
        //    variables.put(Integer.valueOf(ind++), new java.util.Date());
        variables.put(Integer.valueOf(ind++), Integer.valueOf(sb.getUpdater().getId()));
        variables.put(Integer.valueOf(ind++), Boolean.valueOf(sb.isDobCollected()));
        // YW >>

        variables.put(Integer.valueOf(ind++), Integer.valueOf(sb.getId()));

        String sql = digester.getQuery("update");
        this.execute(sql, variables, nullVars);

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

    public void deleteTestSubject(String uniqueIdentifier) {
        HashMap variables = new HashMap();
        variables.put(1, uniqueIdentifier);
        this.execute(digester.getQuery("deleteTestSubject"), variables);
    }
}
