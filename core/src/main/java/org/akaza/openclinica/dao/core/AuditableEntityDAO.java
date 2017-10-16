/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.core;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.StudyBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.DataSource;

/**
 * <P>
 * AuditableEntityDAO.java, an extension of EntityDAO.java.
 * <P>
 * A DAO Class meant to represent an object in the database which is auditable;
 * that is, carry extra information about that object in the object and the
 * database.
 *
 * @author thickerson
 *
 *
 */
public abstract class AuditableEntityDAO<K extends String,V extends ArrayList> extends EntityDAO {
    /**
     * Should the name of a query which refers to a SQL command of the following
     * form:
     *
     * <code>
     * 	SELECT t.*
     * 	FROM tableName t, study s
     * 	WHERE t.study_id=s.study_id
     * 	AND (s.study_id=? or s.parent_study_id=?)
     * </code>
     */
    protected String findAllByStudyName;

    /**
     * status =1
     */
    protected String findAllActiveByStudyName;

    /**
     * Should the name of a query which refers to a SQL command of the following
     * form:
     *
     * <code>
     * 	SELECT t.*
     * 	FROM tableName t, study s
     * 	WHERE t.id=?
     * 		AND t.study_id=s.study_id
     * 		AND (s.study_id=? or s.parent_study_id=?)
     * </code>
     */
    protected String findByPKAndStudyName;

    public AuditableEntityDAO(DataSource ds) {
        super(ds);
        setDigesterName();
        // logger.info("digester name set to " + digesterName);
        digester = SQLFactory.getInstance().getDigester(digesterName);
        // logger.info("digester null? " + (digester == null));
    }

    /*
     * public AuditableEntityBean getEntityAuditInformation(HashMap hm) {
     * AuditableEntityBean aeb = new AuditableEntityBean(); //grab the required
     * information from the table //so that we don't have to repeat this in
     * every single dao Date dateCreated = (Date) hm.get("date_created"); Date
     * dateUpdated = (Date) hm.get("date_updated"); Integer statusId = (Integer)
     * hm.get("status_id"); Integer ownerId = (Integer) hm.get("owner_id");
     * Integer updateId = (Integer) hm.get("update_id");
     *
     * aeb.setCreatedDate(dateCreated); aeb.setUpdatedDate(dateUpdated);
     * aeb.setStatus(Status.get(statusId.intValue()));
     * aeb.setOwnerId(ownerId.intValue());
     * aeb.setUpdaterId(updateId.intValue()); return aeb; }
     */

    public abstract void setTypesExpected();

    /**
     * Note: The subclass must define findAllByStudyName before calling this
     * method. Otherwise an empty array will be returned.
     *
     * @param study
     *            The study to which the entities belong.
     * @return An array containing all the entities which belong to
     *         <code>study</code>.
     */
    public ArrayList findAllByStudy(StudyBean study) {
        ArrayList answer = new ArrayList();

        if (findAllByStudyName == null) {
            return answer;
        }

        setTypesExpected();

        HashMap variables = new HashMap();

        // study.study_id=?
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));

        // or study.parent_study_id=?
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));

        String sql = digester.getQuery(findAllByStudyName);

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            AuditableEntityBean aeb = (AuditableEntityBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(aeb);
        }

        return answer;
    }

    public ArrayList findAllActiveByStudy(StudyBean study) {
        ArrayList answer = new ArrayList();

        if (findAllActiveByStudyName == null) {
            return answer;
        }

        setTypesExpected();

        HashMap variables = new HashMap();

        // study.study_id=?
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));

        // or study.parent_study_id=?
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));

        String sql = digester.getQuery(findAllActiveByStudyName);

        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            AuditableEntityBean aeb = (AuditableEntityBean) this.getEntityFromHashMap((HashMap) it.next());
            answer.add(aeb);
        }

        return answer;
    }

    /**
     * Note: The subclass must define findByPKAndStudyName before calling this
     * method. Otherwise an inactive AuditableEntityBean will be returned.
     *
     * @param id
     *            The primary key of the AuditableEntity which is sought.
     * @param study
     *            The study to which the entity belongs.
     * @return The entity which belong to <code>study</code> and has primary
     *         key <code>id</code>.
     */
    public AuditableEntityBean findByPKAndStudy(int id, StudyBean study) {
        AuditableEntityBean answer = new AuditableEntityBean();

        if (findByPKAndStudyName == null) {
            return answer;
        }

        setTypesExpected();

        HashMap variables = new HashMap();
        // id=?
        variables.put(Integer.valueOf(1), Integer.valueOf(id));

        // study.study_id = ?
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));

        // study.parent_study_id = ?
        variables.put(Integer.valueOf(3), Integer.valueOf(study.getId()));

        String sql = digester.getQuery(findByPKAndStudyName);

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            answer = (AuditableEntityBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return answer;

    }

    public void setEntityAuditInformation(AuditableEntityBean aeb, HashMap hm) {
        // grab the required information from the table
        // so that we don't have to repeat this in every single dao
        Date dateCreated = (Date) hm.get("date_created");
        Date dateUpdated = (Date) hm.get("date_updated");
        Integer statusId = (Integer) hm.get("status_id");
        Integer ownerId = (Integer) hm.get("owner_id");
        Integer updateId = (Integer) hm.get("update_id");

        if (aeb != null) {
            aeb.setCreatedDate(dateCreated);
            aeb.setUpdatedDate(dateUpdated);
            //This was throwing a ClassCastException : BWP altered in 4/2009
           // aeb.setStatus(Status.get(statusId.intValue()));
            aeb.setStatus(Status.getFromMap(statusId));
            aeb.setOwnerId(ownerId.intValue());
            aeb.setUpdaterId(updateId.intValue());
        }
    }

    /**
     * This method executes a "findAll-style" query. Such a query has two
     * characteristics:
     * <ol>
     * <li> The columns SELECTed by the SQL are all of the columns in the table
     * relevant to the DAO, and only those columns. (e.g., in StudyDAO, the
     * columns SELECTed are all of the columns in the study table, and only
     * those columns.)
     * <li> It returns multiple AuditableEntityBeans.
     * </ol>
     *
     * Note that queries which join two tables may be included in the definition
     * of "findAll-style" query, as long as the first criterion is met.
     *
     * @param queryName
     *            The name of the query which should be executed.
     * @param variables
     *            The set of variables used to populate the PreparedStatement;
     *            should be empty if none are needed.
     * @return An ArrayList of AuditableEntityBeans selected by the query.
     */
    public ArrayList executeFindAllQuery(String queryName, HashMap variables) {
        ArrayList answer = new ArrayList();

        this.setTypesExpected();

        ArrayList rows;
        String sql = digester.getQuery(queryName);

        if (variables == null || variables.isEmpty()) {
            rows = this.select(sql);
        } else {
            rows = this.select(sql, variables);
        }
        Iterator it = rows.iterator();

        while (it.hasNext()) {
            answer.add(this.getEntityFromHashMap((HashMap) it.next()));
        }

        return answer;
    }

    /**
     * This method executes a "findAll-style" query which does not accept any
     * variables.
     *
     * @param queryName
     *            The name of the query which selects the AuditableEntityBeans.
     * @return An ArrayList of AuditableEntityBeans selected by the query.
     */
    public ArrayList executeFindAllQuery(String queryName) {
        return executeFindAllQuery(queryName, new HashMap());
    }
}