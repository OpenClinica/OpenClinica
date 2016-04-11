/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2016-2018 Akaza Research
 */
package org.akaza.openclinica.dao.login;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import javax.sql.DataSource;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.exception.DAOInsertFailureException;
import org.akaza.openclinica.exception.DAOUpdateFailureException;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.springframework.transaction.annotation.Transactional;

/**
 * StudyUserRoleDAO, the data access object for the study_user_role table in OpenClinica 2.0 database.
 *
 * @author fahri
 */
public class StudyUserRoleDAO extends AuditableEntityDAO {

    public StudyUserRoleDAO(DataSource dataSource) {
        super(dataSource);
    }

    public StudyUserRoleDAO(DataSource dataSource, DAODigester digester) {
        super(dataSource);
        this.digester = digester;
    }

    public void deleteAllByStudyId(Integer studyId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), studyId);
        execute(digester.getQuery("deleteAllByNonParticipantUserAndStudyId"));
    }

    @Override
    public Collection findAll() {
        ArrayList al = new ArrayList();
        setTypesExpected();
        ArrayList rows = select(digester.getQuery("findAllByNonParticipantUser"));
        Iterator it = rows.iterator();
        while (it.hasNext()) {
            StudyUserRoleBean surb = getEntityFromHashMap((HashMap) it.next());
            al.add(surb);
        }
        return al;
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();
        return al;
    }

    @Override
    public Collection findAllByPermission(Object currentUser, int actionType) {
        ArrayList al = new ArrayList();
        return al;
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();
        return al;
    }

    public ArrayList<StudyUserRoleBean> findAllByStudyId(Integer studyId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), studyId);

        setTypesExpected();
        ArrayList rows = select(digester.getQuery("findAllByNonParticipantUserAndStudyId"));

        ArrayList<StudyUserRoleBean> al = new ArrayList();
        Iterator it = rows.iterator();
        while (it.hasNext()) {
            al.add(getEntityFromHashMap((HashMap) it.next()));
        }
        return al;
    }

    @Override
    public EntityBean findByPK(int id) {
        return null;
    }

    public StudyUserRoleBean findByStudyIdAndUserName(Integer studyId, String userName) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), studyId);
        variables.put(new Integer(2), userName);

        setTypesExpected();
        ArrayList rows = select(digester.getQuery("findByStudyIdAndUserName"), variables);
        Iterator it = rows.iterator();
        if (it.hasNext()) {
            return getEntityFromHashMap((HashMap) it.next());
        } else {
            return null;
        }
    }

    public StudyUserRoleBean getEntityFromHashMap(HashMap map) {
        // pull out objects from hashmap
        String roleName = (String) map.get("role_name");
        Integer studyId = (Integer) map.get("study_id");
        Integer statusId = (Integer) map.get("status_id");
        Integer ownerId = (Integer) map.get("owner_id");
        Date dateCreated = (Date) map.get("date_created");
        Date dateUpdated = (Date) map.get("date_updated");
        Integer updaterId = (Integer) map.get("update_id");
        String userName = (String) map.get("user_name");

        // related entities
        UserAccountDAO uadao = new UserAccountDAO(ds);
        UserAccountBean updater = (UserAccountBean) uadao.findByPK(updaterId);
        UserAccountBean owner = (UserAccountBean) uadao.findByPK(ownerId);

        StudyUserRoleBean entity = new StudyUserRoleBean();
        entity.setId(0);
        entity.setName(userName);
        entity.setRoleName(roleName);
        entity.setStudyId(studyId);
        entity.setStatus(Status.get(statusId));
        entity.setOwner(owner);
        entity.setCreatedDate(dateCreated);
        entity.setUpdatedDate(dateUpdated);
        entity.setUpdater(updater);
        entity.setUserName(userName);

        // related entities
        UserAccountBean user = (UserAccountBean) uadao.findByUserName(userName);
        StudyDAO sdao = new StudyDAO(ds);
        StudyBean study = (StudyBean) sdao.findByPK(studyId);

        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setParentStudyId(study.getParentStudyId());
        entity.setStudyName(study.getName());
        return entity;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDYUSERROLE;
    }

    @Override
    public void setTypesExpected() {
        unsetTypeExpected();
        // assuming select star query on study_user_role
        setTypeExpected(1, TypeNames.STRING); // role_name
        setTypeExpected(2, TypeNames.INT);    // study_id
        setTypeExpected(3, TypeNames.INT);    // status_id
        setTypeExpected(4, TypeNames.INT);    // owner_id
        setTypeExpected(5, TypeNames.DATE);   // date_created
        setTypeExpected(6, TypeNames.DATE);   // date_updated
        setTypeExpected(7, TypeNames.INT);    // update_id
        setTypeExpected(8, TypeNames.STRING); // user_name
    }

    @Override
    public EntityBean create(EntityBean eb) throws OpenClinicaException {
        StudyUserRoleBean entity = (StudyUserRoleBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();

        // insert into study_user_role (role_name, status_id, owner_id, date_created,
        //     date_updated, update_id, study_id, user_name) values (?, ?, ?, ?, ?, ?,
        //     ?, ?)
        variables.put(new Integer(1), entity.getRoleName());
        if (entity.getStatus().getId() == 0) {
            nullVars.put(new Integer(2), new Integer(TypeNames.INT));
            variables.put(new Integer(2), null);
        } else {
            variables.put(new Integer(2), entity.getStatus().getId());
        }
        variables.put(new Integer(3), entity.getOwnerId());
        Date currentDate = new Date();
        if (entity.getCreatedDate() == null) {
            entity.setCreatedDate(currentDate);
        }
        variables.put(new Integer(4), entity.getCreatedDate());
        if (entity.getUpdatedDate() == null) {
            entity.setUpdatedDate(currentDate);
        }
        variables.put(new Integer(5), entity.getUpdatedDate());
        variables.put(new Integer(6), entity.getUpdaterId());
        variables.put(new Integer(7), entity.getStudyId());
        variables.put(new Integer(8), entity.getUserName());

        String sql = digester.getQuery("create");
        execute(sql, variables, nullVars);

        if (!isQuerySuccessful()) {
            logger.warn("query failed: " + sql);
            throw new DAOInsertFailureException(this.getFailureDetails().getMessage(),
                    "StudyUserRoleBean", "create", "StudyUserRoleDAO");
        }
        return entity;
    }

    @Override
    public EntityBean update(EntityBean eb) throws OpenClinicaException {
        StudyUserRoleBean entity = (StudyUserRoleBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();

        // update study_user_role set role_name=?, status_id=?, owner_id=?, date_created=?,
        //     date_updated=?, update_id=? WHERE study_id=? AND user_name=?
        variables.put(new Integer(1), entity.getRoleName());
        if (entity.getStatus().getId() == 0) {
            nullVars.put(new Integer(2), new Integer(TypeNames.INT));
            variables.put(new Integer(2), null);
        } else {
            variables.put(new Integer(2), entity.getStatus().getId());
        }
        variables.put(new Integer(3), entity.getOwnerId());
        if (entity.getCreatedDate() == null) {
            nullVars.put(new Integer(4), new Integer(TypeNames.DATE));
            variables.put(new Integer(4), null);
        } else {
            variables.put(new Integer(4), entity.getCreatedDate());
        }
        if (entity.getUpdatedDate() == null) {
            entity.setUpdatedDate(new Date());
        }
        variables.put(new Integer(5), entity.getUpdatedDate());
        variables.put(new Integer(6), entity.getUpdaterId());
        variables.put(new Integer(7), entity.getStudyId());
        variables.put(new Integer(8), entity.getUserName());

        String sql = digester.getQuery("update");
        execute(sql, variables, nullVars);

        if (!isQuerySuccessful()) {
            logger.warn("query failed: " + sql);
            throw new DAOUpdateFailureException(this.getFailureDetails().getMessage(),
                    "StudyUserRoleBean", "update", "StudyUserRoleDAO");
        }
        return entity;
    }

    /**
     * This is a poor attempt at UPSERT, please review at a later time.
     *
     * @param studyId
     * @param newRoles
     * @return
     * @throws OpenClinicaException 
     */
    @Transactional
    public ArrayList<StudyUserRoleBean> bulkUpsert(Integer studyId,
            ArrayList<StudyUserRoleBean> newRoles) throws OpenClinicaException {
        for (StudyUserRoleBean role : newRoles) {
            if (role.getUserName().contains(".SS_")) {
                throw new DAOUpdateFailureException("API does not support updating participant user roles.",
                        "StudyUserRoleBean", "bulkUpsert", "StudyUserRoleDAO");
            }
        }
        StudyUserRoleBean sur;
        // exclusive lock the table
        execute(digester.getQuery("lockTableExclusively"));
        // delete (set state to deleted) all related rows
        deleteAllByStudyId(studyId);
        // begin upsert (this approach is considered naive)
        Date currentDate = new Date();
        HashMap variables;
        HashMap nullVars;
        for (StudyUserRoleBean entity : newRoles) {
            variables = new HashMap();
            nullVars = new HashMap();
            // UPDATE study_user_role
            // SET role_name=?, status_id=?, owner_id=?, date_created=?, date_updated=?, update_id=?
            // WHERE study_id=? AND user_name=?;
            // INSERT INTO study_user_role (role_name, status_id, owner_id, date_created,
            //     date_updated, update_id, study_id, user_name)
            // SELECT v.*
            //     FROM (VALUES (?, ?, ?, ?, ?, ?, ?, ?)) AS v(role_name, status_id, owner_id,
            //         date_created, date_updated, update_id, study_id, user_name)
            //     LEFT JOIN study_user_role t ON t.study_id = v.study_id AND
            //         t.user_name = v.user_name
            //     WHERE t.user_name IS NULL;
            variables.put(new Integer(1), entity.getRoleName());
            if (entity.getStatus().getId() == 0) {
                nullVars.put(new Integer(2), new Integer(TypeNames.INT));
                variables.put(new Integer(2), null);
            } else {
                variables.put(new Integer(2), entity.getStatus().getId());
            }
            variables.put(new Integer(3), entity.getOwnerId());
            if (entity.getCreatedDate() == null) {
                nullVars.put(new Integer(4), new Integer(TypeNames.DATE));
                variables.put(new Integer(4), null);
            } else {
                variables.put(new Integer(4), entity.getCreatedDate());
            }
            if (entity.getUpdatedDate() == null) {
                entity.setUpdatedDate(currentDate);
            }
            variables.put(new Integer(5), entity.getUpdatedDate());
            variables.put(new Integer(6), entity.getUpdaterId());
            variables.put(new Integer(7), entity.getStudyId());
            variables.put(new Integer(8), entity.getUserName());
            variables.put(new Integer(9), entity.getRoleName());
            if (entity.getStatus().getId() == 0) {
                nullVars.put(new Integer(10), new Integer(TypeNames.INT));
                variables.put(new Integer(10), null);
            } else {
                variables.put(new Integer(10), entity.getStatus().getId());
            }
            variables.put(new Integer(11), entity.getOwnerId());
            if (entity.getCreatedDate() == null) {
                nullVars.put(new Integer(12), new Integer(TypeNames.DATE));
                variables.put(new Integer(12), null);
            } else {
                variables.put(new Integer(12), entity.getCreatedDate());
            }
            variables.put(new Integer(13), entity.getUpdatedDate());
            variables.put(new Integer(14), entity.getUpdaterId());

            String sql = digester.getQuery("upsert");
            execute(sql, variables, nullVars);
        }
        return findAllByStudyId(studyId);
    }
}
