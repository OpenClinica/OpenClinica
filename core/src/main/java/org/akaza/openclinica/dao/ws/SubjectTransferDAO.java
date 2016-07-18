/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.dao.ws;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.rule.RuleSetAuditBean;
import org.akaza.openclinica.bean.rule.RuleSetBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.dao.core.EntityDAO;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.sql.Types;
import java.util.*;

public class SubjectTransferDAO extends EntityDAO {

    UserAccountDAO userAccountDao;

    public SubjectTransferDAO(DataSource ds) {
        super(ds);
        this.getCurrentPKName = "findCurrentPKValue";
        // this.getNextPKName = "getNextPK";
    }

    @Override
    public int getCurrentPK() {
        int answer = 0;

        if (getCurrentPKName == null) {
            return answer;
        }

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        ArrayList al = select(digester.getQuery(getCurrentPKName));

        if (al.size() > 0) {
            HashMap h = (HashMap) al.get(0);
            answer = ((Integer) h.get("key")).intValue();
        }

        return answer;
    }

    private UserAccountDAO getUserAccountDao() {
        return this.userAccountDao != null ? this.userAccountDao : new UserAccountDAO(ds);
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_SUBJECTTRANSFER;
    }

    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.STRING); // personId
        this.setTypeExpected(2, TypeNames.STRING); // studySubject
        this.setTypeExpected(3, TypeNames.DATE); // dateOfBirth
        this.setTypeExpected(4, TypeNames.CHAR); // gender
        this.setTypeExpected(5, TypeNames.STRING); // study_oid
        this.setTypeExpected(6, TypeNames.INT); // owner_id
        this.setTypeExpected(7, TypeNames.DATE); // enrollment_date
        this.setTypeExpected(8, TypeNames.DATE); // date_received

    }

    public Object getEntityFromHashMap(HashMap hm) {
        SubjectTransferBean subjectTransferBean = new SubjectTransferBean();
        subjectTransferBean.setId((Integer) hm.get("subject_transfer_id"));
        subjectTransferBean.setPersonId((String) hm.get("person_id"));
        subjectTransferBean.setStudySubjectId((String) hm.get("study_subject_id"));
        subjectTransferBean.setDateOfBirth((Date) hm.get("date_of_birth"));
        try {
            String gender = (String) hm.get("gender");
            char[] genderarr = gender.toCharArray();
            subjectTransferBean.setGender(genderarr[0]);
        } catch (ClassCastException ce) {
            subjectTransferBean.setGender(' ');
        }
        subjectTransferBean.setEnrollmentDate((Date) hm.get("enrollment_date"));
        subjectTransferBean.setOwner((UserAccountBean) getUserAccountDao().findByPK((Integer) hm.get("owner_id")));

        return subjectTransferBean;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) throws OpenClinicaException {
        return new ArrayList();
    }

    public Collection findAll() throws OpenClinicaException {
        return new ArrayList();
    }

    public EntityBean findByPK(int id) throws OpenClinicaException {
        RuleSetAuditBean ruleSetAudit = null;

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), id);

        String sql = digester.getQuery("findByPK");
        ArrayList<?> alist = this.select(sql, variables);

        Iterator<?> it = alist.iterator();

        if (it.hasNext()) {
            ruleSetAudit = (RuleSetAuditBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
        }
        return ruleSetAudit;
    }

    public ArrayList<RuleSetAuditBean> findAllByRuleSet(RuleSetBean ruleSet) {
        ArrayList<RuleSetAuditBean> ruleSetAuditBeans = new ArrayList<RuleSetAuditBean>();

        this.setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), ruleSet.getId());

        String sql = digester.getQuery("findAllByRuleSet");
        ArrayList<?> alist = this.select(sql, variables);
        Iterator<?> it = alist.iterator();

        while (it.hasNext()) {
            RuleSetAuditBean ruleSetAudit = (RuleSetAuditBean) this.getEntityFromHashMap((HashMap<?, ?>) it.next());
            ruleSetAuditBeans.add(ruleSetAudit);
        }
        return ruleSetAuditBeans;
    }

    public EntityBean create(EntityBean eb, UserAccountBean ub) {
        // INSERT INTO subject_transfer (subject_transfer_id,date_of_birth,gender,person_id,study_subject_id, status_id,updater_id,date_updated) VALUES
        // (?,?,?,?,?)
        SubjectTransferBean subjectTransferBean = (SubjectTransferBean) eb;
        RuleSetAuditBean ruleSetAudit = new RuleSetAuditBean();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap<Integer, Object> nullVars = new HashMap<Integer, Object>();

        if (subjectTransferBean.getDateOfBirth() == null) {
            nullVars.put(new Integer(1), new Integer(Types.DATE));
            variables.put(new Integer(1), null);
        } else {
            variables.put(new Integer(1), subjectTransferBean.getDateOfBirth());
        }

        if (subjectTransferBean.getGender() != 'm' && subjectTransferBean.getGender() != 'f') {
            nullVars.put(new Integer(2), new Integer(Types.CHAR));
            variables.put(new Integer(2), null);
        } else {
            char[] ch = { subjectTransferBean.getGender() };
            variables.put(new Integer(2), new String(ch));
        }

        variables.put(3, subjectTransferBean.getPersonId());
        variables.put(4, subjectTransferBean.getStudySubjectId());
        variables.put(5, subjectTransferBean.getStudyOid());
        variables.put(6, subjectTransferBean.getDateReceived());
        variables.put(7, subjectTransferBean.getEnrollmentDate());
        variables.put(8, subjectTransferBean.getOwner().getId());

        executeWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            subjectTransferBean.setId(getLatestPK());
        }
        return subjectTransferBean;
    }

    public EntityBean create(EntityBean eb) throws OpenClinicaException {
        SubjectTransferBean subjectTransferBean = (SubjectTransferBean) eb;
        return create(eb, subjectTransferBean.getOwner());
    }

    public EntityBean update(EntityBean eb) throws OpenClinicaException {
        return new ItemGroupMetadataBean(); // To change body of implemented

    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException {
        return new ArrayList<RuleSetAuditBean>();
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return new ArrayList<RuleSetAuditBean>();
    }

}
