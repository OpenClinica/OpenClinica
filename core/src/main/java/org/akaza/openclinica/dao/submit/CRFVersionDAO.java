/*
 * Copyright 2003-2008 Akaza Research
 *
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 */
package org.akaza.openclinica.dao.submit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

/**
 * <p>
 * CRFVersionDAO.java, the data access object for versions of instruments in the database. Each of these are related to
 * Sections, a versioning map that links them with Items, and an Event, which then links to a Study.
 * 
 * @author thickerson
 * 
 * 
 */
public class CRFVersionDAO<K extends String, V extends ArrayList> extends AuditableEntityDAO {

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_CRFVERSION;
    }

    public CRFVersionDAO(DataSource ds) {
        super(ds);
    }

    public CRFVersionDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public CRFVersionDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.locale = locale;
    }

    public EntityBean update(EntityBean eb) {
        // UPDATE CRF_VERSION SET CRF_ID=?,STATUS_ID=?,NAME=?,
        // DESCRIPTION=?,DATE_UPDATED=NOW(),UPDATE_ID=?,REVISION_NOTES =? WHERE
        // CRF_VERSION_ID=?
        CRFVersionBean ib = (CRFVersionBean) eb;
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ib.getCrfId()));
        variables.put(new Integer(2), new Integer(ib.getStatus().getId()));
        variables.put(new Integer(3), ib.getName());
        variables.put(new Integer(4), ib.getDescription());
        variables.put(new Integer(5), new Integer(ib.getUpdater().getId()));
        variables.put(new Integer(6), ib.getRevisionNotes());
        variables.put(new Integer(7), new Integer(ib.getId()));
        this.execute(digester.getQuery("update"), variables);
        return eb;
    }

    public EntityBean create(EntityBean eb) {
        // "INSERT INTO CRF_VERSION (NAME, DESCRIPTION, CRF_ID, STATUS_ID,DATE_CREATED," +
        // "OWNER_ID,REVISION_NOTES,OC_OID) "
        // + "VALUES ('" + stripQuotes(version) + "','" + stripQuotes(versionDesc) + "'," +
        // "(SELECT CRF_ID FROM CRF WHERE NAME='"
        // + crfName + "'),1,NOW()," + ub.getId() + ",'" + stripQuotes(revisionNotes) + "','" + oid + "')";

        // <sql>INSERT INTO CRF_VERSION (CRF_ID, STATUS_ID, NAME,
        // DESCRIPTION, OWNER_ID,
        // DATE_CREATED, REVISION_NOTES)
        // VALUES (?,?,?,?,?,NOW(),?)</sql>

        CRFVersionBean cvb = (CRFVersionBean) eb;
        HashMap variables = new HashMap();
        // variables.put(Integer.valueOf(2), cb.getLabel());
        variables.put(Integer.valueOf(1), Integer.valueOf(cvb.getCrfId()));
        variables.put(Integer.valueOf(2), Integer.valueOf(cvb.getStatus().getId()));
        variables.put(Integer.valueOf(3), cvb.getName());
        variables.put(Integer.valueOf(4), cvb.getDescription());
        variables.put(Integer.valueOf(5), Integer.valueOf(cvb.getOwner().getId()));
        variables.put(Integer.valueOf(6), cvb.getRevisionNotes());
        variables.put(Integer.valueOf(7), getValidOid(cvb, cvb.getName(), cvb.getOid()));
        variables.put(Integer.valueOf(8), cvb.getXform());
        variables.put(Integer.valueOf(9), cvb.getXformName());

        // am i the only one who runs their daos' unit tests after I change
        // things, tbh?
        this.execute(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
            cvb.setActive(true);
        }
        return cvb;

    }

    @Override
    public void setTypesExpected() {
        // crf_version_id serial NOT NULL,
        // crf_id numeric NOT NULL,
        // name varchar(255),
        // description varchar(4000),

        // revision_notes varchar(255),
        // status_id numeric,
        // date_created date,

        // date_updated date,
        // owner_id numeric,
        // update_id numeric,
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);

        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.DATE);

        this.setTypeExpected(8, TypeNames.DATE);
        this.setTypeExpected(9, TypeNames.INT);
        this.setTypeExpected(10, TypeNames.INT);
        this.setTypeExpected(11, TypeNames.STRING);
        this.setTypeExpected(12, TypeNames.STRING);
        this.setTypeExpected(13, TypeNames.STRING);

    }

    public Object getEntityFromHashMap(HashMap hm) {
        // CRF_VERSION_ID NAME DESCRIPTION
        // CRF_ID STATUS_ID DATE_CREATED DATE_UPDATED
        // OWNER_ID REVISION_NUMBER UPDATE_ID
        CRFVersionBean eb = new CRFVersionBean();
        super.setEntityAuditInformation(eb, hm);
        eb.setId(((Integer) hm.get("crf_version_id")).intValue());

        eb.setName((String) hm.get("name"));
        eb.setDescription((String) hm.get("description"));
        eb.setCrfId(((Integer) hm.get("crf_id")).intValue());
        eb.setRevisionNotes((String) hm.get("revision_notes"));
        eb.setOid((String) hm.get("oc_oid"));
        eb.setXform((String) hm.get("xform"));
        eb.setXformName((String) hm.get("xform_name"));
        return eb;
    }

    public Collection findAll() {
        this.setTypesExpected();

        ArrayList al = new ArrayList();
        ArrayList alist = this.select(digester.getQuery("findAll"));

        Iterator it = alist.iterator();
        while (it.hasNext()) {
            CRFVersionBean eb = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findAllByCRF(int crfId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfId));
        String sql = digester.getQuery("findAllByCRF");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            CRFVersionBean eb = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findAllActiveByCRF(int crfId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfId));
        String sql = digester.getQuery("findAllActiveByCRF");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            CRFVersionBean eb = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Collection findItemFromMap(int versionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.INT);
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(versionId));
        String sql = digester.getQuery("findItemFromMap");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            ItemBean eb = new ItemBean();
            HashMap hm = (HashMap) it.next();
            eb.setId(((Integer) hm.get("item_id")).intValue());
            eb.setName((String) hm.get("name"));
            Integer ownerId = (Integer) hm.get("owner_id");
            eb.setOwnerId(ownerId.intValue());

            al.add(eb);
        }
        return al;
    }

    public Collection findItemUsedByOtherVersion(int versionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.INT);
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(versionId));
        String sql = digester.getQuery("findItemUsedByOtherVersion");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            ItemBean eb = new ItemBean();
            HashMap hm = (HashMap) it.next();
            eb.setId(((Integer) hm.get("item_id")).intValue());
            eb.setName((String) hm.get("name"));
            eb.setOwnerId(((Integer) hm.get("owner_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList findNotSharedItemsByVersion(int versionId) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.INT);
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(versionId));
        variables.put(new Integer(2), new Integer(versionId));
        String sql = digester.getQuery("findNotSharedItemsByVersion");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            ItemBean eb = new ItemBean();
            HashMap hm = (HashMap) it.next();
            eb.setId(((Integer) hm.get("item_id")).intValue());
            eb.setName((String) hm.get("name"));
            eb.setOwnerId(((Integer) hm.get("owner_id")).intValue());
            al.add(eb);
        }
        return al;
    }

    public ArrayList findDefCRFVersionsByStudyEvent(int studyEventDefinitionId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyEventDefinitionId));
        String sql = digester.getQuery("findDefCRFVersionsByStudyEvent");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            CRFVersionBean eb = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public boolean isItemUsedByOtherVersion(int versionId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(versionId));
        String sql = digester.getQuery("isItemUsedByOtherVersion");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            return true;
        }
        return false;
    }

    public boolean hasItemData(int itemId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(itemId));
        String sql = digester.getQuery("hasItemData");
        ArrayList alist = this.select(sql, variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            return true;
        }
        return false;
    }

    public EntityBean findByPK(int ID) {
        CRFVersionBean eb = new CRFVersionBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;

    }

    public EntityBean findByFullName(String version, String crfName) {
        CRFVersionBean eb = new CRFVersionBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), version);
        variables.put(new Integer(2), crfName);

        String sql = digester.getQuery("findByFullName");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return eb;

    }

    /**
     * Deletes a CRF version
     */
    public void delete(int id) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(id));

        String sql = digester.getQuery("delete");
        this.execute(sql, variables);
    }

    /**
     * Generates all the delete queries for deleting a version
     * 
     * @param versionId
     * @param items
     */
    public ArrayList generateDeleteQueries(int versionId, ArrayList items) {
        ArrayList sqls = new ArrayList();
        String sql = digester.getQuery("deleteScdItemMetadataByVersion") + versionId + ")";
        sqls.add(sql);
        sql = digester.getQuery("deleteItemMetaDataByVersion") + versionId;
        sqls.add(sql);
        sql = digester.getQuery("deleteSectionsByVersion") + versionId;
        sqls.add(sql);
        sql = digester.getQuery("deleteItemMapByVersion") + versionId;
        sqls.add(sql);
        sql = digester.getQuery("deleteItemGroupMetaByVersion") + versionId;
        sqls.add(sql);

        for (int i = 0; i < items.size(); i++) {
            ItemBean item = (ItemBean) items.get(i);
            sql = digester.getQuery("deleteItemsByVersion") + item.getId();
            sqls.add(sql);
        }

        sql = digester.getQuery("deleteResponseSetByVersion") + versionId;
        sqls.add(sql);
        sql = digester.getQuery("deleteCrfVersionMediaByVersion") + versionId;
        sqls.add(sql);
        sql = digester.getQuery("delete") + versionId;
        sqls.add(sql);
        return sqls;

    }

    private String getOid(CRFVersionBean crfVersion, String crfName, String crfVersionName) {

        String oid;
        try {
            oid = crfVersion.getOid() != null ? crfVersion.getOid() : crfVersion.getOidGenerator().generateOid(crfName, crfVersionName);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(CRFVersionBean crfVersion, String crfName, String crfVersionName) {

        String oid = getOid(crfVersion, crfName, crfVersionName);
        logger.debug(oid);
        String oidPreRandomization = oid;
        while (findAllByOid(oid).size() > 0) {
            oid = crfVersion.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;

    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

    public ArrayList findAllByOid(String oid) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);

        return executeFindAllQuery("findAllByOid", variables);
    }

    public int getCRFIdFromCRFVersionId(int CRFVersionId) {
        int answer = 0;

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(CRFVersionId));

        String sql = digester.getQuery("getCRFIdFromCRFVersionId");
        ArrayList rows = select(sql, variables);

        if (rows.size() > 0) {
            HashMap h = (HashMap) rows.get(0);
            answer = ((Integer) h.get("crf_id")).intValue();
        }
        return answer;
    }

    public ArrayList findAllByCRFId(int CRFId) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(CRFId));

        return executeFindAllQuery("findAllByCRFId", variables);
    }

    public Integer findCRFVersionId(int crfId, String versionName) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfId));
        variables.put(new Integer(2), versionName);
        ArrayList result = this.select(digester.getQuery("findCRFVersionId"), variables);
        HashMap map;
        Integer crfVersionId = null;
        if (result.iterator().hasNext()) {
            map = (HashMap) result.iterator().next();
            crfVersionId = (Integer) map.get("crf_version_id");
        }
        return crfVersionId;
    }

    public CRFVersionBean findByOid(String oid) {
        CRFVersionBean crfVersionBean = new CRFVersionBean();
        this.unsetTypeExpected();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);
        String sql = digester.getQuery("findByOID");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            crfVersionBean = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
            return crfVersionBean;
        } else {
            return null;
        }
    }

    /**
     * 
     * @param studySubjectId
     * @return
     */
    public Map<Integer, CRFVersionBean> buildCrfVersionById(Integer studySubjectId) {
        this.setTypesExpected(); // <== Must be called first
        Map<Integer, CRFVersionBean> result = new HashMap<Integer, CRFVersionBean>();

        HashMap<Integer, Object> param = new HashMap<Integer, Object>();
        int i = 1;
        param.put(i++, studySubjectId);

        List selectResult = select(digester.getQuery("buildCrfVersionById"), param);

        Iterator it = selectResult.iterator();

        while (it.hasNext()) {
            CRFVersionBean bean = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
            result.put(bean.getId(), bean);
        }

        return result;
    }

}