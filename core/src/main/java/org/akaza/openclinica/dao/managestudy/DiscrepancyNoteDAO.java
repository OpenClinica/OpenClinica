/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.managestudy;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;

/**
 * @author jxu
 *
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class DiscrepancyNoteDAO extends AuditableEntityDAO {
    // if true, we fetch the mapping along with the bean
    // only applies to functions which return a single bean
    private boolean fetchMapping = false;

    /**
     * @return Returns the fetchMapping.
     */
    public boolean isFetchMapping() {
        return fetchMapping;
    }

    /**
     * @param fetchMapping
     *            The fetchMapping to set.
     */
    public void setFetchMapping(boolean fetchMapping) {
        this.fetchMapping = fetchMapping;
    }

    private void setQueryNames() {
        findByPKAndStudyName = "findByPKAndStudy";
        getCurrentPKName = "getCurrentPrimaryKey";
    }

    public DiscrepancyNoteDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public DiscrepancyNoteDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_DISCREPANCY_NOTE;
    }

    @Override
    public void setTypesExpected() {
        // discrepancy_note_id serial NOT NULL,
        // description varchar(255),
        // discrepancy_note_type_id numeric,
        // resolution_status_id numeric,

        // detailed_notes varchar(1000),
        // date_created date,
        // owner_id numeric,
        // parent_dn_id numeric,
        // adding study id
        // adding assigned user id, tbh 02/2009
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.INT);

        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.DATE);
        this.setTypeExpected(7, TypeNames.INT);
        this.setTypeExpected(8, TypeNames.INT);
        this.setTypeExpected(9, TypeNames.STRING);
        this.setTypeExpected(10, TypeNames.INT);
        this.setTypeExpected(11, TypeNames.INT);
    }

    public void setMapTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database query.
     */
    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        DiscrepancyNoteBean eb = new DiscrepancyNoteBean();
        Date dateCreated = (Date) hm.get("date_created");
        Integer ownerId = (Integer) hm.get("owner_id");
        eb.setCreatedDate(dateCreated);
        eb.setOwnerId(ownerId.intValue());

        // discrepancy_note_id serial NOT NULL,
        // description varchar(255),
        // discrepancy_note_type_id numeric,
        // resolution_status_id numeric,

        // detailed_notes varchar(1000),
        // date_created date,
        // owner_id numeric,
        // parent_dn_id numeric,
        eb.setId(selectInt(hm, "discrepancy_note_id"));
        eb.setDescription((String) hm.get("description"));
        eb.setDiscrepancyNoteTypeId(((Integer) hm.get("discrepancy_note_type_id")).intValue());
        eb.setResolutionStatusId(((Integer) hm.get("resolution_status_id")).intValue());
        eb.setParentDnId(((Integer) hm.get("parent_dn_id")).intValue());
        eb.setDetailedNotes((String) hm.get("detailed_notes"));
        eb.setEntityType((String) hm.get("entity_type"));
        eb.setDisType(DiscrepancyNoteType.get(eb.getDiscrepancyNoteTypeId()));
        eb.setResStatus(ResolutionStatus.get(eb.getResolutionStatusId()));
        eb.setStudyId(selectInt(hm, "study_id"));
        eb.setAssignedUserId(selectInt(hm, "assigned_user_id"));
        if (eb.getAssignedUserId() > 0) {
            UserAccountDAO userAccountDAO = new UserAccountDAO(ds);
            UserAccountBean assignedUser = (UserAccountBean) userAccountDAO.findByPK(eb.getAssignedUserId());
            eb.setAssignedUser(assignedUser);
        }
        eb.setAge(selectInt(hm, "age"));
        eb.setDays(selectInt(hm, "days"));
        return eb;
    }

    @Override
    public Collection findAll() {
        return this.executeFindAllQuery("findAll");
    }

    public ArrayList findAllParentsByStudy(StudyBean study) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        ArrayList notes = executeFindAllQuery("findAllParentsByStudy", variables);

        if (fetchMapping) {
            for (int i = 0; i < notes.size(); i++) {
                DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) notes.get(i);
                dnb = findSingleMapping(dnb);
                notes.set(i, dnb);
            }
        }

        return notes;
    }

    public ArrayList findAllByStudyAndParent(StudyBean study, int parentId) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(parentId));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(study.getId()));
        return this.executeFindAllQuery("findAllByStudyAndParent", variables);
    }

    public ArrayList<DiscrepancyNoteBean> findAllItemNotesByEventCRF(int eventCRFId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFId));
        alist = this.select(digester.getQuery("findAllItemNotesByEventCRF"), variables);
        ArrayList<DiscrepancyNoteBean> al = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllParentItemNotesByEventCRF(int eventCRFId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFId));
        alist = this.select(digester.getQuery("findAllParentItemNotesByEventCRF"), variables);
        ArrayList<DiscrepancyNoteBean> al = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllParentItemNotesByEventCRFWithConstraints(int eventCRFId, StringBuffer constraints) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFId));
        String sql = digester.getQuery("findAllParentItemNotesByEventCRF");
        String[] s = sql.split("order by");
        sql = s[0] + " " + constraints.toString() + " order by " + s[1];
        alist = this.select(sql, variables);
        ArrayList<DiscrepancyNoteBean> al = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;
    }

    public Integer getSubjectDNCountWithFilter(ListNotesFilter filter, Integer currentStudyId) {
        DiscrepancyNoteBean discrepancyNoteBean = new DiscrepancyNoteBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudyId);
        variables.put(Integer.valueOf(2), currentStudyId);
        String sql = digester.getQuery("getSubjectDNCountWithFilter");
        sql += filter.execute("", variables);

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getStudySubjectDNCountWithFilter(ListNotesFilter filter, Integer currentStudyId) {
        DiscrepancyNoteBean discrepancyNoteBean = new DiscrepancyNoteBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudyId);
        variables.put(Integer.valueOf(2), currentStudyId);
        String sql = digester.getQuery("getStudySubjectDNCountWithFilter");
        sql += filter.execute("", variables);

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getStudyEventDNCountWithFilter(ListNotesFilter filter, Integer currentStudyId) {
        DiscrepancyNoteBean discrepancyNoteBean = new DiscrepancyNoteBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudyId);
        variables.put(Integer.valueOf(2), currentStudyId);
        String sql = digester.getQuery("getStudyEventDNCountWithFilter");
        sql += filter.execute("", variables);

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getEventCrfDNCountWithFilter(ListNotesFilter filter, Integer currentStudyId) {
        DiscrepancyNoteBean discrepancyNoteBean = new DiscrepancyNoteBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudyId);
        variables.put(Integer.valueOf(2), currentStudyId);
        String sql = digester.getQuery("getEventCrfDNCountWithFilter");
        sql += filter.execute("", variables);

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getItemDataDNCountWithFilter(ListNotesFilter filter, Integer currentStudyId) {
        DiscrepancyNoteBean discrepancyNoteBean = new DiscrepancyNoteBean();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudyId);
        variables.put(Integer.valueOf(2), currentStudyId);
        String sql = digester.getQuery("getItemDataDNCountWithFilter");
        sql += filter.execute("", variables);

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public ArrayList<DiscrepancyNoteBean> getWithFilterAndSort(StudyBean currentStudy, ListNotesFilter filter, ListNotesSort sort, int rowStart, int rowEnd) {
        ArrayList<DiscrepancyNoteBean> discNotes = new ArrayList<DiscrepancyNoteBean>();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        String sql = digester.getQuery("getWithFilterAndSort");
        sql += filter.execute("", variables);

        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += " AND rownum <= " + rowEnd + " and rownum >" + rowStart;
            sql = sql + sort.execute("");
        } else {
            sql = sql + sort.execute("");
            sql = sql + " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
        }

        ArrayList rows = select(sql, variables);

        Iterator it = rows.iterator();
        while (it.hasNext()) {
            DiscrepancyNoteBean discBean = (DiscrepancyNoteBean) this.getEntityFromHashMap((HashMap) it.next());
            discBean = findSingleMapping(discBean);
            discNotes.add(discBean);
        }
        return discNotes;

    }

    public Integer getViewNotesCountWithFilter(ListNotesFilter filter, StudyBean currentStudy) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), currentStudy.getId());
        variables.put(Integer.valueOf(4), currentStudy.getId());
        variables.put(Integer.valueOf(5), currentStudy.getId());
        variables.put(Integer.valueOf(6), currentStudy.getId());
        variables.put(Integer.valueOf(7), currentStudy.getId());
        variables.put(Integer.valueOf(8), currentStudy.getId());
        variables.put(Integer.valueOf(9), currentStudy.getId());
        variables.put(Integer.valueOf(10), currentStudy.getId());
        String sql = "select count(all_dn.discrepancy_note_id) as COUNT from (";
        sql += digester.getQuery("findAllSubjectDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllStudySubjectDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllStudyEventDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllEventCrfDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllItemDataDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter.execute("", variables);
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += " ) all_dn";
        } else {
            sql += " ) as all_dn";
        }

        ArrayList rows = select(sql, variables);
        Iterator it = rows.iterator();
        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public Integer getViewNotesCountWithFilter(String filter, StudyBean currentStudy) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), currentStudy.getId());
        variables.put(Integer.valueOf(4), currentStudy.getId());
        variables.put(Integer.valueOf(5), currentStudy.getId());
        variables.put(Integer.valueOf(6), currentStudy.getId());
        variables.put(Integer.valueOf(7), currentStudy.getId());
        variables.put(Integer.valueOf(8), currentStudy.getId());
        variables.put(Integer.valueOf(9), currentStudy.getId());
        variables.put(Integer.valueOf(10), currentStudy.getId());
        String sql = "select count(all_dn.discrepancy_note_id) as COUNT from (";
        sql += digester.getQuery("findAllSubjectDNByStudy");
        sql += filter;
        sql += " UNION ";
        sql += digester.getQuery("findAllStudySubjectDNByStudy");
        sql += filter;
        sql += " UNION ";
        sql += digester.getQuery("findAllStudyEventDNByStudy");
        sql += filter;
        sql += " UNION ";
        sql += digester.getQuery("findAllEventCrfDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter;
        sql += " UNION ";
        sql += digester.getQuery("findAllItemDataDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter;
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql += " ) all_dn";
        } else {
            sql += " ) as all_dn";
        }

        ArrayList rows = select(sql, variables);
        Iterator it = rows.iterator();
        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    /*
     * public ArrayList<DiscrepancyNoteBean> getViewNotesWithFilterAndSort(StudyBean currentStudy, ListNotesFilter
     * filter, ListNotesSort sort, int rowStart,
     * int rowEnd) {
     * ArrayList<DiscrepancyNoteBean> discNotes = new ArrayList<DiscrepancyNoteBean>();
     * setTypesExpected();
     * this.setTypeExpected(12, TypeNames.STRING);
     * this.setTypeExpected(13, TypeNames.INT);
     * this.setTypeExpected(14, TypeNames.INT);
     * this.setTypeExpected(15, TypeNames.INT);
     * 
     * HashMap variables = new HashMap();
     * variables.put(Integer.valueOf(1), currentStudy.getId());
     * variables.put(Integer.valueOf(2), currentStudy.getId());
     * variables.put(Integer.valueOf(3), currentStudy.getId());
     * variables.put(Integer.valueOf(4), currentStudy.getId());
     * variables.put(Integer.valueOf(5), currentStudy.getId());
     * variables.put(Integer.valueOf(6), currentStudy.getId());
     * variables.put(Integer.valueOf(7), currentStudy.getId());
     * variables.put(Integer.valueOf(8), currentStudy.getId());
     * variables.put(Integer.valueOf(9), currentStudy.getId());
     * variables.put(Integer.valueOf(10), currentStudy.getId());
     * 
     * String sql = "";
     * if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
     * sql = sql + "SELECT * FROM ( SELECT x.*, ROWNUM as rnum FROM (";
     * }
     * sql = sql + digester.getQuery("findAllSubjectDNByStudy");
     * sql = sql + filter.execute("");
     * sql += " UNION ";
     * sql += digester.getQuery("findAllStudySubjectDNByStudy");
     * sql += filter.execute("");
     * sql += " UNION ";
     * sql += digester.getQuery("findAllStudyEventDNByStudy");
     * sql += filter.execute("");
     * sql += " UNION ";
     * sql += digester.getQuery("findAllEventCrfDNByStudy");
     * if (currentStudy.isSite(currentStudy.getParentStudyId())) {
     * sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
     * }
     * sql += filter.execute("");
     * sql += " UNION ";
     * sql += digester.getQuery("findAllItemDataDNByStudy");
     * if (currentStudy.isSite(currentStudy.getParentStudyId())) {
     * sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
     * }
     * sql += filter.execute("");
     * 
     * if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
     * sql += ") x )  WHERE rnum BETWEEN " + (rowStart + 1) + " and " + rowEnd;
     * sql += sort.execute("");
     * } else {
     * sql += sort.execute("");
     * sql += " LIMIT " + (rowEnd - rowStart) + " OFFSET " + rowStart;
     * }
     * // System.out.println(sql);
     * ArrayList rows = select(sql, variables);
     * 
     * Iterator it = rows.iterator();
     * while (it.hasNext()) {
     * DiscrepancyNoteBean discBean = (DiscrepancyNoteBean) this.getEntityFromHashMap((HashMap) it.next());
     * discBean = findSingleMapping(discBean);
     * discNotes.add(discBean);
     * }
     * return discNotes;
     * }
     */

    public ArrayList<DiscrepancyNoteBean> getViewNotesWithFilterAndSort(StudyBean currentStudy, ListNotesFilter filter, ListNotesSort sort) {
        ArrayList<DiscrepancyNoteBean> discNotes = new ArrayList<DiscrepancyNoteBean>();
        setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);
        this.setTypeExpected(13, TypeNames.INT);
        this.setTypeExpected(14, TypeNames.INT);
        this.setTypeExpected(15, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), currentStudy.getId());
        variables.put(Integer.valueOf(4), currentStudy.getId());
        variables.put(Integer.valueOf(5), currentStudy.getId());
        variables.put(Integer.valueOf(6), currentStudy.getId());
        variables.put(Integer.valueOf(7), currentStudy.getId());
        variables.put(Integer.valueOf(8), currentStudy.getId());
        variables.put(Integer.valueOf(9), currentStudy.getId());
        variables.put(Integer.valueOf(10), currentStudy.getId());

        String sql = "";
        // if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
        // sql = sql + "SELECT * FROM ( SELECT x.*, ROWNUM as rnum FROM (";
        // }
        sql = sql + digester.getQuery("findAllSubjectDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllStudySubjectDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllStudyEventDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllEventCrfDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllItemDataDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter.execute("", variables);
        sql += sort.execute("");

        ArrayList rows = select(sql, variables);

        Iterator it = rows.iterator();
        while (it.hasNext()) {
            DiscrepancyNoteBean discBean = (DiscrepancyNoteBean) this.getEntityFromHashMap((HashMap) it.next());
            discBean = findSingleMapping(discBean);
            discNotes.add(discBean);
        }
        return discNotes;
    }

    public ArrayList<DiscrepancyNoteBean> findAllDiscrepancyNotesDataByStudy(StudyBean currentStudy) {
        ArrayList<DiscrepancyNoteBean> discNotes = new ArrayList<DiscrepancyNoteBean>();
        setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);
        this.setTypeExpected(13, TypeNames.INT);
        this.setTypeExpected(14, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), currentStudy.getId());
        variables.put(Integer.valueOf(4), currentStudy.getId());
        variables.put(Integer.valueOf(5), currentStudy.getId());
        variables.put(Integer.valueOf(6), currentStudy.getId());
        variables.put(Integer.valueOf(7), currentStudy.getId());
        variables.put(Integer.valueOf(8), currentStudy.getId());
        variables.put(Integer.valueOf(9), currentStudy.getId());
        variables.put(Integer.valueOf(10), currentStudy.getId());
        String sql = digester.getQuery("findAllSubjectDNByStudy");
        sql += " UNION ";
        sql += digester.getQuery("findAllStudySubjectDNByStudy");
        sql += " UNION ";
        sql += digester.getQuery("findAllStudyEventDNByStudy");
        sql += " UNION ";
        sql += digester.getQuery("findAllEventCrfDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += " UNION ";
        sql += digester.getQuery("findAllItemDataDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }

        ArrayList rows = select(sql, variables);

        Iterator it = rows.iterator();
        while (it.hasNext()) {
            DiscrepancyNoteBean discBean = (DiscrepancyNoteBean) this.getEntityFromHashMap((HashMap) it.next());
            discBean = findSingleMapping(discBean);
            discNotes.add(discBean);
        }
        return discNotes;
    }

    public ArrayList<DiscrepancyNoteBean> getNotesWithFilterAndSort(StudyBean currentStudy, ListNotesFilter filter, ListNotesSort sort) {
        ArrayList<DiscrepancyNoteBean> discNotes = new ArrayList<DiscrepancyNoteBean>();
        setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);
        this.setTypeExpected(13, TypeNames.INT);
        this.setTypeExpected(14, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), currentStudy.getId());
        variables.put(Integer.valueOf(2), currentStudy.getId());
        variables.put(Integer.valueOf(3), currentStudy.getId());
        variables.put(Integer.valueOf(4), currentStudy.getId());
        variables.put(Integer.valueOf(5), currentStudy.getId());
        variables.put(Integer.valueOf(6), currentStudy.getId());
        variables.put(Integer.valueOf(7), currentStudy.getId());
        variables.put(Integer.valueOf(8), currentStudy.getId());
        variables.put(Integer.valueOf(9), currentStudy.getId());
        variables.put(Integer.valueOf(10), currentStudy.getId());
        String sql = digester.getQuery("findAllSubjectDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllStudySubjectDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllStudyEventDNByStudy");
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllEventCrfDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter.execute("", variables);
        sql += " UNION ";
        sql += digester.getQuery("findAllItemDataDNByStudy");
        if (currentStudy.isSite(currentStudy.getParentStudyId())) {
            sql += " and ec.event_crf_id not in ( " + this.findSiteHiddenEventCrfIdsString(currentStudy) + " ) ";
        }
        sql += filter.execute("", variables);
        sql += " order by label";

        ArrayList rows = select(sql, variables);

        Iterator it = rows.iterator();
        while (it.hasNext()) {
            DiscrepancyNoteBean discBean = (DiscrepancyNoteBean) this.getEntityFromHashMap((HashMap) it.next());
            discBean = findSingleMapping(discBean);
            discNotes.add(discBean);
        }
        return discNotes;
    }

    public Collection findAllByEntityAndColumn(String entityName, int entityId, String column) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        ArrayList alist = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(entityId));
        variables.put(Integer.valueOf(2), column);
        if ("subject".equalsIgnoreCase(entityName)) {
            alist = this.select(digester.getQuery("findAllBySubjectAndColumn"), variables);
        } else if ("studySub".equalsIgnoreCase(entityName)) {
            alist = this.select(digester.getQuery("findAllByStudySubjectAndColumn"), variables);
        } else if ("eventCrf".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.DATE);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            this.setTypeExpected(15, TypeNames.STRING);// crf_name
            alist = this.select(digester.getQuery("findAllByEventCRFAndColumn"), variables);
        } else if ("studyEvent".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.DATE);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            alist = this.select(digester.getQuery("findAllByStudyEventAndColumn"), variables);
        } else if ("itemData".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.DATE);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            this.setTypeExpected(15, TypeNames.STRING);// crf_name
            this.setTypeExpected(16, TypeNames.STRING);// item_name
            alist = this.select(digester.getQuery("findAllByItemDataAndColumn"), variables);
        }

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            if ("eventCrf".equalsIgnoreCase(entityName) || "itemData".equalsIgnoreCase(entityName)) {
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
                eb.setCrfName((String) hm.get("crf_name"));
                eb.setEntityName((String) hm.get("item_name"));

            } else if ("studyEvent".equalsIgnoreCase(entityName)) {
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
            }
            if (fetchMapping) {
                eb = findSingleMapping(eb);
            }

            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllEntityByPK(String entityName, int noteId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(noteId));
        variables.put(Integer.valueOf(2), Integer.valueOf(noteId));
        if ("subject".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.STRING);// column_name
            alist = this.select(digester.getQuery("findAllSubjectByPK"), variables);
        } else if ("studySub".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.STRING);// column_name
            alist = this.select(digester.getQuery("findAllStudySubjectByPK"), variables);
        } else if ("eventCrf".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.DATE);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            this.setTypeExpected(15, TypeNames.STRING);// crf_name
            this.setTypeExpected(16, TypeNames.STRING);// column_name
            alist = this.select(digester.getQuery("findAllEventCRFByPK"), variables);
        } else if ("studyEvent".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.DATE);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            this.setTypeExpected(15, TypeNames.STRING);// column_name
            alist = this.select(digester.getQuery("findAllStudyEventByPK"), variables);
        } else if ("itemData".equalsIgnoreCase(entityName)) {
            this.setTypeExpected(13, TypeNames.DATE);// date_start
            this.setTypeExpected(14, TypeNames.STRING);// sed_name
            this.setTypeExpected(15, TypeNames.STRING);// crf_name
            this.setTypeExpected(16, TypeNames.STRING);// item_name
            this.setTypeExpected(17, TypeNames.STRING);// value
            // YW <<
            this.setTypeExpected(18, TypeNames.INT);// item_data_id
            this.setTypeExpected(19, TypeNames.INT);// item_id
            // YW >>
            alist = this.select(digester.getQuery("findAllItemDataByPK"), variables);
        }

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            if ("subject".equalsIgnoreCase(entityName) || "studySub".equalsIgnoreCase(entityName)) {
                eb.setSubjectName((String) hm.get("label"));
                eb.setColumn((String) hm.get("column_name"));
            } else if ("eventCrf".equalsIgnoreCase(entityName)) {
                eb.setSubjectName((String) hm.get("label"));
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
                eb.setCrfName((String) hm.get("crf_name"));
                eb.setColumn((String) hm.get("column_name"));
            } else if ("itemData".equalsIgnoreCase(entityName)) {
                eb.setSubjectName((String) hm.get("label"));
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
                eb.setCrfName((String) hm.get("crf_name"));
                eb.setEntityName((String) hm.get("item_name"));
                eb.setEntityValue((String) hm.get("value"));
                // YW <<
                eb.setEntityId(((Integer) hm.get("item_data_id")).intValue());
                eb.setItemId(((Integer) hm.get("item_id")).intValue());
                // YW >>

            } else if ("studyEvent".equalsIgnoreCase(entityName)) {
                eb.setSubjectName((String) hm.get("label"));
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
                eb.setColumn((String) hm.get("column_name"));
            }
            if (fetchMapping) {
                eb = findSingleMapping(eb);
            }
            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllSubjectByStudy(StudyBean study) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// subject_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(study.getId()));

        alist = this.select(digester.getQuery("findAllSubjectByStudy"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllSubjectByStudyAndId(StudyBean study, int subjectId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// subject_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(subjectId));

        alist = this.select(digester.getQuery("findAllSubjectByStudyAndId"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllStudySubjectByStudy(StudyBean study) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// study_subject_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));

        alist = this.select(digester.getQuery("findAllStudySubjectByStudy"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllStudySubjectByStudyAndId(StudyBean study, int studySubjectId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// study_subject_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(studySubjectId));

        alist = this.select(digester.getQuery("findAllStudySubjectByStudyAndId"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllStudySubjectByStudiesAndStudySubjectId(StudyBean currentStudy, StudyBean subjectStudy, int studySubjectId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// study_subject_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(currentStudy.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(studySubjectId));

        alist = this.select(digester.getQuery("findAllStudySubjectByStudiesAndStudySubjectId"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllSubjectByStudiesAndSubjectId(StudyBean currentStudy, StudyBean subjectStudy, int studySubjectId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.STRING);// column_name
        this.setTypeExpected(14, TypeNames.INT);// subject_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(currentStudy.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(currentStudy.getId()));
        variables.put(Integer.valueOf(5), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(6), Integer.valueOf(studySubjectId));

        alist = this.select(digester.getQuery("findAllSubjectByStudiesAndSubjectId"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("subject_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllStudyEventByStudy(StudyBean study) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.DATE);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// column_name
        this.setTypeExpected(16, TypeNames.INT);// study_event_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        alist = this.select(digester.getQuery("findAllStudyEventByStudy"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_event_id")).intValue());

            al.add(eb);
        }
        return al;
    }

    /**
     * Find all DiscrepancyNoteBeans associated with a certain Study Subject and Study.
     *
     * @param study
     *            A StudyBean, whose id property is checked.
     * @param studySubjectId
     *            The id of a Study Subject.
     * @return An ArrayList of DiscrepancyNoteBeans.
     */
    public ArrayList findAllStudyEventByStudyAndId(StudyBean study, int studySubjectId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.DATE);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// column_name
        this.setTypeExpected(16, TypeNames.INT);// study_event_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(studySubjectId));
        alist = this.select(digester.getQuery("findAllStudyEventByStudyAndId"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_event_id")).intValue());

            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllStudyEventByStudiesAndSubjectId(StudyBean currentStudy, StudyBean subjectStudy, int studySubjectId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.DATE);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// column_name
        this.setTypeExpected(16, TypeNames.INT);// study_event_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(currentStudy.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(subjectStudy.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(currentStudy.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(studySubjectId));
        alist = this.select(digester.getQuery("findAllStudyEventByStudiesAndSubjectId"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setSubjectName((String) hm.get("label"));
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("study_event_id")).intValue());

            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllEventCRFByStudy(StudyBean study) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.DATE);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// crf_name
        this.setTypeExpected(16, TypeNames.STRING);// column_name
        this.setTypeExpected(17, TypeNames.INT);// event_crf_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        alist = this.select(digester.getQuery("findAllEventCRFByStudy"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setCrfName((String) hm.get("crf_name"));
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("event_crf_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllEventCRFByStudyAndParent(StudyBean study, DiscrepancyNoteBean parent) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.DATE);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// crf_name
        this.setTypeExpected(16, TypeNames.STRING);// column_name
        this.setTypeExpected(17, TypeNames.INT);// event_crf_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(parent.getId()));

        alist = this.select(digester.getQuery("findAllEventCRFByStudyAndParent"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setCrfName((String) hm.get("crf_name"));
            eb.setSubjectName((String) hm.get("label"));
            eb.setColumn((String) hm.get("column_name"));
            eb.setEntityId(((Integer) hm.get("event_crf_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findItemDataDNotesFromEventCRF(EventCRFBean eventCRFBean) {

        this.setTypesExpected();
        ArrayList dNotelist = new ArrayList();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFBean.getId()));
        dNotelist = this.select(digester.getQuery("findItemDataDNotesFromEventCRF"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = dNotelist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;

    }

    public ArrayList<DiscrepancyNoteBean> findParentItemDataDNotesFromEventCRF(EventCRFBean eventCRFBean) {

        this.setTypesExpected();
        ArrayList dNotelist = new ArrayList();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFBean.getId()));
        dNotelist = this.select(digester.getQuery("findParentItemDataDNotesFromEventCRF"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = dNotelist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;

    }

    public ArrayList<DiscrepancyNoteBean> findEventCRFDNotesFromEventCRF(EventCRFBean eventCRFBean) {

        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);
        ArrayList dNotelist = new ArrayList();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFBean.getId()));
        dNotelist = this.select(digester.getQuery("findEventCRFDNotesFromEventCRF"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = dNotelist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setColumn((String) hm.get("column_name"));
            eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;

    }

    public ArrayList<DiscrepancyNoteBean> findEventCRFDNotesToolTips(EventCRFBean eventCRFBean) {

        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);
        ArrayList dNotelist = new ArrayList();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(4), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(5), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(6), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(7), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(8), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(9), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(10), Integer.valueOf(eventCRFBean.getId()));

        dNotelist = this.select(digester.getQuery("findEventCRFDNotesForToolTips"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = dNotelist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setColumn((String) hm.get("column_name"));
            eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;

    }

    public ArrayList<DiscrepancyNoteBean> findAllDNotesByItemNameAndEventCRF(EventCRFBean eventCRFBean, String itemName) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFBean.getId()));
        variables.put(Integer.valueOf(2), itemName);
        ArrayList dNotelist = new ArrayList();

        dNotelist = this.select(digester.getQuery("findAllDNotesByItemNameAndEventCRF"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = dNotelist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            // eb.setColumn((String) hm.get("column_name"));
            // eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;

    }

    public ArrayList findAllItemDataByStudy(StudyBean study) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.DATE);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// crf_name
        this.setTypeExpected(16, TypeNames.STRING);// item_name
        this.setTypeExpected(17, TypeNames.STRING);// value
        this.setTypeExpected(18, TypeNames.INT);// item_data_id
        this.setTypeExpected(19, TypeNames.INT);// item_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        alist = this.select(digester.getQuery("findAllItemDataByStudy"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setCrfName((String) hm.get("crf_name"));
            eb.setSubjectName((String) hm.get("label"));
            eb.setEntityName((String) hm.get("item_name"));
            eb.setEntityValue((String) hm.get("value"));
            // YW << change EntityId from item_id to item_data_id.
            eb.setEntityId(((Integer) hm.get("item_data_id")).intValue());
            eb.setItemId(((Integer) hm.get("item_id")).intValue());
            // YW >>
            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllItemDataByStudy(StudyBean study, Set<String> hiddenCrfNames) {
        this.setTypesExpected();
        ArrayList al = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.DATE);// date_start
        this.setTypeExpected(14, TypeNames.INT);// sed_id
        this.setTypeExpected(15, TypeNames.STRING);// sed_name
        this.setTypeExpected(16, TypeNames.STRING);// crf_name
        this.setTypeExpected(17, TypeNames.STRING);// item_name
        this.setTypeExpected(18, TypeNames.STRING);// value
        this.setTypeExpected(19, TypeNames.INT);// item_data_id
        this.setTypeExpected(20, TypeNames.INT);// item_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        ArrayList alist = this.select(digester.getQuery("findAllItemDataByStudy"), variables);
        Iterator it = alist.iterator();

        if (hiddenCrfNames.size() > 0) {
            while (it.hasNext()) {
                HashMap hm = (HashMap) it.next();
                Integer sedId = (Integer) hm.get("sed_id");
                String crfName = (String) hm.get("crf_name");
                if (!hiddenCrfNames.contains(sedId + "_" + crfName)) {
                    DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
                    eb.setEventName((String) hm.get("sed_name"));
                    eb.setEventStart((Date) hm.get("date_start"));
                    eb.setCrfName(crfName);
                    eb.setSubjectName((String) hm.get("label"));
                    eb.setEntityName((String) hm.get("item_name"));
                    eb.setEntityValue((String) hm.get("value"));
                    eb.setEntityId((Integer) hm.get("item_data_id"));
                    eb.setItemId((Integer) hm.get("item_id"));
                    al.add(eb);
                }
            }
        } else {
            while (it.hasNext()) {
                HashMap hm = (HashMap) it.next();
                DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
                eb.setEventName((String) hm.get("sed_name"));
                eb.setEventStart((Date) hm.get("date_start"));
                eb.setCrfName((String) hm.get("crf_name"));
                eb.setSubjectName((String) hm.get("label"));
                eb.setEntityName((String) hm.get("item_name"));
                eb.setEntityValue((String) hm.get("value"));
                eb.setEntityId((Integer) hm.get("item_data_id"));
                eb.setItemId((Integer) hm.get("item_id"));
                al.add(eb);
            }
        }
        return al;
    }

    public Integer countAllItemDataByStudyAndUser(StudyBean study, UserAccountBean user) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.DATE);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// crf_name
        this.setTypeExpected(16, TypeNames.STRING);// item_name
        this.setTypeExpected(17, TypeNames.STRING);// value
        this.setTypeExpected(18, TypeNames.INT);// item_data_id
        this.setTypeExpected(19, TypeNames.INT);// item_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(user.getId()));

        ArrayList rows = this.select(digester.getQuery("countAllItemDataByStudyAndUser"), variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }

    public ArrayList findAllItemDataByStudyAndParent(StudyBean study, DiscrepancyNoteBean parent) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        this.setTypeExpected(12, TypeNames.STRING);// ss.label
        this.setTypeExpected(13, TypeNames.DATE);// date_start
        this.setTypeExpected(14, TypeNames.STRING);// sed_name
        this.setTypeExpected(15, TypeNames.STRING);// crf_name
        this.setTypeExpected(16, TypeNames.STRING);// item_name
        this.setTypeExpected(17, TypeNames.STRING);// value
        this.setTypeExpected(18, TypeNames.INT);// item_data_id
        this.setTypeExpected(19, TypeNames.INT);// item_id

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(study.getId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(parent.getId()));
        alist = this.select(digester.getQuery("findAllItemDataByStudyAndParent"), variables);

        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setEventName((String) hm.get("sed_name"));
            eb.setEventStart((Date) hm.get("date_start"));
            eb.setCrfName((String) hm.get("crf_name"));
            eb.setSubjectName((String) hm.get("label"));
            eb.setEntityName((String) hm.get("item_name"));
            eb.setEntityValue((String) hm.get("value"));
            // YW << change EntityId from item_id to item_data_id.
            eb.setEntityId(((Integer) hm.get("item_data_id")).intValue());
            eb.setItemId(((Integer) hm.get("item_id")).intValue());
            // YW >>
            al.add(eb);
        }
        return al;
    }

    @Override
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    @Override
    public EntityBean findByPK(int ID) {
        DiscrepancyNoteBean eb = new DiscrepancyNoteBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (DiscrepancyNoteBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        if (fetchMapping) {
            eb = findSingleMapping(eb);
        }

        return eb;
    }

    /**
     * Creates a new discrepancy note
     */
    @Override
    public EntityBean create(EntityBean eb) {
        DiscrepancyNoteBean sb = (DiscrepancyNoteBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        // INSERT INTO discrepancy_note
        // (description, discrepancy_note_type_id ,
        // resolution_status_id , detailed_notes , date_created,
        // owner_id, parent_dn_id)
        // VALUES (?,?,?,?,now(),?,?)
        variables.put(Integer.valueOf(1), sb.getDescription());
        variables.put(Integer.valueOf(2), Integer.valueOf(sb.getDiscrepancyNoteTypeId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(sb.getResolutionStatusId()));
        variables.put(Integer.valueOf(4), sb.getDetailedNotes());

        variables.put(Integer.valueOf(5), Integer.valueOf(sb.getOwner().getId()));
        if (sb.getParentDnId() == 0) {
            nullVars.put(Integer.valueOf(6), Integer.valueOf(Types.INTEGER));
            variables.put(Integer.valueOf(6), null);
        } else {
            variables.put(Integer.valueOf(6), Integer.valueOf(sb.getParentDnId()));
        }
        variables.put(Integer.valueOf(7), sb.getEntityType());
        variables.put(Integer.valueOf(8), Integer.valueOf(sb.getStudyId()));
        if (sb.getAssignedUserId() == 0) {
            nullVars.put(Integer.valueOf(9), Integer.valueOf(Types.INTEGER));
            variables.put(Integer.valueOf(9), null);
        } else {
            variables.put(Integer.valueOf(9), Integer.valueOf(sb.getAssignedUserId()));
        }
        // variables.put(Integer.valueOf(9), Integer.valueOf(sb.getAssignedUserId()));

        this.executeWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            sb.setId(getLatestPK());
        }

        return sb;
    }

    /**
     * Creates a new discrepancy note map
     */
    public void createMapping(DiscrepancyNoteBean eb) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eb.getEntityId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(eb.getId()));
        variables.put(Integer.valueOf(3), eb.getColumn());
        String entityType = eb.getEntityType();

        if ("subject".equalsIgnoreCase(entityType)) {
            this.execute(digester.getQuery("createSubjectMap"), variables);
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            this.execute(digester.getQuery("createStudySubjectMap"), variables);
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            this.execute(digester.getQuery("createEventCRFMap"), variables);
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            this.execute(digester.getQuery("createStudyEventMap"), variables);
        } else if ("itemData".equalsIgnoreCase(entityType)) {
            variables.put(Integer.valueOf(4), eb.isActivated());
            this.execute(digester.getQuery("createItemDataMap"), variables);
        }

    }

    /**
     * Updates a Study event
     */
    @Override
    public EntityBean update(EntityBean eb) {
        // update discrepancy_note set
        // description =?,
        // discrepancy_note_type_id =? ,
        // resolution_status_id =? ,
        // detailed_notes =?
        // where discrepancy_note_id=?
        DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) eb;
        dnb.setActive(false);

        HashMap variables = new HashMap();

        variables.put(Integer.valueOf(1), dnb.getDescription());
        variables.put(Integer.valueOf(2), Integer.valueOf(dnb.getDiscrepancyNoteTypeId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(dnb.getResolutionStatusId()));
        variables.put(Integer.valueOf(4), dnb.getDetailedNotes());
        variables.put(Integer.valueOf(5), Integer.valueOf(dnb.getId()));
        this.execute(digester.getQuery("update"), variables);

        if (isQuerySuccessful()) {
            dnb.setActive(true);
        }

        return dnb;
    }

    public EntityBean updateAssignedUser(EntityBean eb) {
        // update discrepancy_note set
        // assigned_user_id = ?
        // where discrepancy_note_id=?
        DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) eb;
        dnb.setActive(false);

        HashMap variables = new HashMap();

        variables.put(Integer.valueOf(1), dnb.getAssignedUserId());
        variables.put(Integer.valueOf(2), Integer.valueOf(dnb.getId()));
        this.execute(digester.getQuery("updateAssignedUser"), variables);

        if (isQuerySuccessful()) {
            dnb.setActive(true);
        }

        return dnb;
    }

    public EntityBean updateDnMapActivation(EntityBean eb) {
        // update discrepancy_note set
        // assigned_user_id = ?
        // where discrepancy_note_id=?
        DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) eb;
        dnb.setActive(false);

        HashMap variables = new HashMap();

        variables.put(Integer.valueOf(1), dnb.isActivated());
        variables.put(Integer.valueOf(2), dnb.getEntityId());
        this.execute(digester.getQuery("updateDnMapActivation"), variables);

        if (isQuerySuccessful()) {
            dnb.setActive(true);
        }

        return dnb;
    }

    public EntityBean updateAssignedUserToNull(EntityBean eb) {
        // update discrepancy_note set
        // assigned_user_id = null
        // where discrepancy_note_id=?
        DiscrepancyNoteBean dnb = (DiscrepancyNoteBean) eb;
        dnb.setActive(false);

        HashMap variables = new HashMap();

        variables.put(Integer.valueOf(1), Integer.valueOf(dnb.getId()));
        this.execute(digester.getQuery("updateAssignedUserToNull"), variables);

        if (isQuerySuccessful()) {
            dnb.setActive(true);
        }

        return dnb;
    }

    public void deleteNotes(int id) {
        HashMap<Integer, Comparable> variables = new HashMap<Integer, Comparable>();
        variables.put(Integer.valueOf(1), Integer.valueOf(id));
        this.execute(digester.getQuery("deleteNotes"), variables);
        return;

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

    public ArrayList findAllByParent(DiscrepancyNoteBean parent) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(parent.getId()));

        return this.executeFindAllQuery("findAllByParent", variables);
    }

    public ArrayList findAllByStudyEvent(StudyEventBean studyEvent) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(studyEvent.getId()));

        return this.executeFindAllQuery("findByStudyEvent", variables);
    }

    public ArrayList findAllByStudyEventWithConstraints(StudyEventBean studyEvent, StringBuffer constraints) {
        this.setTypesExpected();
        ArrayList answer = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(studyEvent.getId()));
        String sql = digester.getQuery("findByStudyEvent");
        sql += constraints.toString();
        Iterator it = this.select(sql, variables).iterator();
        while (it.hasNext()) {
            answer.add(this.getEntityFromHashMap((HashMap) it.next()));
        }
        return answer;
    }

    public HashMap<ResolutionStatus, Integer> findAllByStudyEventWithConstraints(StudyEventBean studyEvent, StringBuffer constraints, boolean isSite) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        // ArrayList answer = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(studyEvent.getId()));
        String sql = digester.getQuery("findByStudyEvent");
        sql += constraints.toString();
        if (isSite) {
            if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
                sql += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                        + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                        + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 1" + " AND edc.event_definition_crf_id not in ("
                        + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";
            } else {
                sql += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                        + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                        + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 'true'"
                        + " AND edc.event_definition_crf_id not in ("
                        + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";

            }
        }
        sql += " group By  dn.resolution_status_id ";
        Iterator it = this.select(sql, variables).iterator();
        HashMap<ResolutionStatus, Integer> discCounts = new HashMap<ResolutionStatus, Integer>();
        while (it.hasNext()) {
            HashMap h = (HashMap) it.next();
            Integer resolutionStatusId = (Integer) h.get("resolution_status_id");
            Integer count = (Integer) h.get("count");
            discCounts.put(ResolutionStatus.get(resolutionStatusId), count);
        }
        return discCounts;
    }

    public HashMap<ResolutionStatus, Integer> countByEntityTypeAndStudyEventWithConstraints(String entityType, StudyEventBean studyEvent,
            StringBuffer constraints, boolean isSite) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        // ArrayList answer = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(studyEvent.getId()));
        String sql = "";
        String temp = "";
        if ("itemData".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findByStudyEvent");
            temp = " and (dn.entity_type='itemData' or dn.entity_type='ItemData') ";
            if (isSite) {
                if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
                    temp += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                            + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                            + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 1"
                            + " AND edc.event_definition_crf_id not in ("
                            + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";
                } else {
                    temp += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                            + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                            + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 'true'"
                            + " AND edc.event_definition_crf_id not in ("
                            + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";

                }
            }
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("countByEventCrfTypeAndStudyEvent");
            temp = " and dn.entity_type='eventCrf' ";
            if (isSite) {
                if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
                    temp += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                            + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                            + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 1"
                            + " AND edc.event_definition_crf_id not in ("
                            + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";
                } else {
                    temp += " AND ec.crf_version_id not in (select cv.crf_version_id from crf_version cv where cv.crf_id in ("
                            + "select edc.crf_id from event_definition_crf edc, study_event se where se.study_event_id = " + studyEvent.getId()
                            + " AND edc.study_event_definition_id = se.study_event_definition_id AND edc.hide_crf = 'true'"
                            + " AND edc.event_definition_crf_id not in ("
                            + "select parent_id from event_definition_crf where study_event_definition_id = se.study_event_definition_id and parent_id > 0)) )";

                }
            }
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("countByStudyEventTypeAndStudyEvent");
            temp = " and dn.entity_type='studyEvent' ";
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("countByStudySubjectTypeAndStudyEvent");
            temp = " and dn.entity_type='studySub' ";
        } else if ("subject".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("countBySubjectTypeAndStudyEvent");
            temp = " and dn.entity_type='subject' ";
        }
        sql += temp;
        sql += constraints.toString();
        sql += " group By  dn.resolution_status_id ";
        Iterator it = this.select(sql, variables).iterator();
        HashMap<ResolutionStatus, Integer> discCounts = new HashMap<ResolutionStatus, Integer>();
        while (it.hasNext()) {
            HashMap h = (HashMap) it.next();
            Integer resolutionStatusId = (Integer) h.get("resolution_status_id");
            Integer count = (Integer) h.get("count");
            discCounts.put(ResolutionStatus.get(resolutionStatusId), count);
        }
        return discCounts;
    }

    private DiscrepancyNoteBean findSingleMapping(DiscrepancyNoteBean note) {
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(note.getId()));

        setMapTypesExpected();
        String entityType = note.getEntityType();
        String sql = "";
        if ("subject".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findSubjectMapByDNId");
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findStudySubjectMapByDNId");
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findEventCRFMapByDNId");
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findStudyEventMapByDNId");
        } else if ("itemData".equalsIgnoreCase(entityType)) {
            sql = digester.getQuery("findItemDataMapByDNId");
            this.unsetTypeExpected();
            this.setTypeExpected(1, TypeNames.INT);
            this.setTypeExpected(2, TypeNames.INT);
            this.setTypeExpected(3, TypeNames.STRING);
            this.setTypeExpected(4, TypeNames.INT);
        }

        ArrayList hms = select(sql, variables);

        if (hms.size() > 0) {
            HashMap hm = (HashMap) hms.get(0);
            note = getMappingFromHashMap(hm, note);
        }

        return note;
    }

    private DiscrepancyNoteBean getMappingFromHashMap(HashMap hm, DiscrepancyNoteBean note) {
        String entityType = note.getEntityType();
        String entityIDColumn = getEntityIDColumn(entityType);

        if (!entityIDColumn.equals("")) {
            note.setEntityId(selectInt(hm, entityIDColumn));
        }
        note.setColumn(selectString(hm, "column_name"));
        return note;
    }

    public static String getEntityIDColumn(String entityType) {
        String entityIDColumn = "";
        if ("subject".equalsIgnoreCase(entityType)) {
            entityIDColumn = "subject_id";
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            entityIDColumn = "study_subject_id";
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            entityIDColumn = "event_crf_id";
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            entityIDColumn = "study_event_id";
        } else if ("itemData".equalsIgnoreCase(entityType)) {
            entityIDColumn = "item_data_id";
        }
        return entityIDColumn;
    }

    public AuditableEntityBean findEntity(DiscrepancyNoteBean note) {
        AuditableEntityDAO aedao = getAEDAO(note, ds);

        try {
            if (aedao != null) {
                AuditableEntityBean aeb = (AuditableEntityBean) aedao.findByPK(note.getEntityId());
                return aeb;
            }
        } catch (Exception e) {
        }

        return null;
    }

    public static AuditableEntityDAO getAEDAO(DiscrepancyNoteBean note, DataSource ds) {
        String entityType = note.getEntityType();
        if ("subject".equalsIgnoreCase(entityType)) {
            return new SubjectDAO(ds);
        } else if ("studySub".equalsIgnoreCase(entityType)) {
            return new StudySubjectDAO(ds);
        } else if ("eventCrf".equalsIgnoreCase(entityType)) {
            return new EventCRFDAO(ds);
        } else if ("studyEvent".equalsIgnoreCase(entityType)) {
            return new StudyEventDAO(ds);
        } else if ("itemData".equalsIgnoreCase(entityType)) {
            return new ItemDataDAO(ds);
        }

        return null;
    }

    public int findNumExistingNotesForItem(int itemDataId) {
        unsetTypeExpected();
        setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(itemDataId));
        String sql = digester.getQuery("findNumExistingNotesForItem");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            try {
                Integer i = (Integer) hm.get("num");
                return i.intValue();
            } catch (Exception e) {
            }
        }

        return 0;
    }

    public int findNumOfActiveExistingNotesForItemData(int itemDataId) {
        unsetTypeExpected();
        setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(itemDataId));
        String sql = digester.getQuery("findNumOfActiveExistingNotesForItemData");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            try {
                Integer i = (Integer) hm.get("num");
                return i.intValue();
            } catch (Exception e) {
            }
        }

        return 0;
    }

    public ArrayList findExistingNotesForItemData(int itemDataId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(itemDataId));
        alist = this.select(digester.getQuery("findExistingNotesForItemData"), variables);
        ArrayList<DiscrepancyNoteBean> al = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;

    }

    public ArrayList findExistingNotesForToolTip(int itemDataId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(itemDataId));
        variables.put(Integer.valueOf(2), Integer.valueOf(itemDataId));
        variables.put(Integer.valueOf(3), Integer.valueOf(itemDataId));
        variables.put(Integer.valueOf(4), Integer.valueOf(itemDataId));
        alist = this.select(digester.getQuery("findExistingNotesForToolTip"), variables);
        ArrayList<DiscrepancyNoteBean> al = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            al.add(eb);
        }

        // alist = this.select(digester.getQuery("findParentNotesForToolTip"), variables);
        // it = alist.iterator();
        // while (it.hasNext()) {
        // HashMap hm = (HashMap) it.next();
        // DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
        // al.add(eb);
        // }
        return al;

    }

    public ArrayList findParentNotesForToolTip(int itemDataId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(itemDataId));
        variables.put(Integer.valueOf(2), Integer.valueOf(itemDataId));

        alist = this.select(digester.getQuery("findParentNotesForToolTip"), variables);
        ArrayList<DiscrepancyNoteBean> al = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;

    }

    public ArrayList<DiscrepancyNoteBean> findParentNotesOnlyByItemData(int itemDataId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(itemDataId));
        alist = this.select(digester.getQuery("findParentNotesOnlyByItemData"), variables);
        ArrayList<DiscrepancyNoteBean> al = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findAllTopNotesByEventCRF(int eventCRFId) {
        this.setTypesExpected();
        ArrayList alist = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFId));
        alist = this.select(digester.getQuery("findAllTopNotesByEventCRF"), variables);
        ArrayList<DiscrepancyNoteBean> al = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            al.add(eb);
        }
        return al;
    }

    public ArrayList<DiscrepancyNoteBean> findOnlyParentEventCRFDNotesFromEventCRF(EventCRFBean eventCRFBean) {
        this.setTypesExpected();
        this.setTypeExpected(12, TypeNames.STRING);
        ArrayList dNotelist = new ArrayList();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(eventCRFBean.getId()));
        dNotelist = this.select(digester.getQuery("findOnlyParentEventCRFDNotesFromEventCRF"), variables);

        ArrayList<DiscrepancyNoteBean> returnedNotelist = new ArrayList<DiscrepancyNoteBean>();
        Iterator it = dNotelist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            DiscrepancyNoteBean eb = (DiscrepancyNoteBean) this.getEntityFromHashMap(hm);
            eb.setColumn((String) hm.get("column_name"));
            eb.setEventCRFId(eventCRFBean.getId());
            returnedNotelist.add(eb);
        }
        return returnedNotelist;
    }

    public String findSiteHiddenEventCrfIdsString(StudyBean site) {
        String sql = "";
        if ("oracle".equalsIgnoreCase(CoreResources.getDBName())) {
            sql = "select ec.event_crf_id from event_crf ec, study_event se, crf_version cv, " + "(select edc.study_event_definition_id, edc.crf_id, crf.name "
                    + "from event_definition_crf edc, crf, study s " + "where s.study_id=" + site.getId()
                    + " and (edc.study_id = s.study_id or edc.study_id = s.parent_study_id)" + "    and edc.event_definition_crf_id not in ( "
                    + "        select parent_id from event_definition_crf where study_id=s.study_id) "
                    + "            and edc.status_id=1 and edc.hide_crf = 1 and edc.crf_id = crf.crf_id) sedc " + "where ec.study_event_id = se.study_event_id "
                    + "and se.study_event_definition_id = sedc.study_event_definition_id "
                    + "and ec.crf_version_id = cv.crf_version_id and cv.crf_id = sedc.crf_id";
        } else {
            sql = "select ec.event_crf_id from event_crf ec, study_event se, crf_version cv, " + "(select edc.study_event_definition_id, edc.crf_id, crf.name "
                    + "from event_definition_crf edc, crf, study s " + "where s.study_id=" + site.getId()
                    + " and (edc.study_id = s.study_id or edc.study_id = s.parent_study_id)" + "    and edc.event_definition_crf_id not in ( "
                    + "        select parent_id from event_definition_crf where study_id=s.study_id) "
                    + "            and edc.status_id=1 and edc.hide_crf = 'true' and edc.crf_id = crf.crf_id) as sedc "
                    + "where ec.study_event_id = se.study_event_id " + "and se.study_event_definition_id = sedc.study_event_definition_id "
                    + "and ec.crf_version_id = cv.crf_version_id and cv.crf_id = sedc.crf_id";
        }
        return sql;
    }

    public EntityBean findLatestChildByParent(int parentId) {
        DiscrepancyNoteBean eb = new DiscrepancyNoteBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(parentId));
        variables.put(Integer.valueOf(2), Integer.valueOf(parentId));

        String sql = digester.getQuery("findLatestChildByParent");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (DiscrepancyNoteBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;
    }

    public int getResolutionStatusIdForSubjectDNFlag(int subjectId, String column) {
        int id = 0;
        unsetTypeExpected();
        setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(subjectId));
        variables.put(Integer.valueOf(2), new String(column));

        String sql = digester.getQuery("getResolutionStatusIdForSubjectDNFlag");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();
        if (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            try {
                id = ((Integer) hm.get("resolution_status_id")).intValue();
            } catch (Exception e) {
            }
        }
        return id;
    }

    // Yufang code, addded by Jamuna
    public Integer getViewNotesCountWithFilter(Integer assignedUserId, Integer studyId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), assignedUserId);
        variables.put(Integer.valueOf(2), studyId);
        variables.put(Integer.valueOf(3), studyId);
        String sql = digester.getQuery("countViewNotesForAssignedUserInStudy");

        ArrayList rows = select(sql, variables);
        Iterator it = rows.iterator();
        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            return count;
        } else {
            return null;
        }
    }
}