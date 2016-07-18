package org.akaza.openclinica.dao.submit;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.dao.core.*;
import org.akaza.openclinica.exception.OpenClinicaException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: May 8, 2007
 */
public class ItemGroupDAO<K extends String,V extends ArrayList> extends AuditableEntityDAO {

    public ItemGroupDAO(DataSource ds) {
        super(ds);
        this.getCurrentPKName = "findCurrentPKValue";
        this.getNextPKName = "getNextPK";
    }

    public ItemGroupDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    // This constructor sets up the Locale for JUnit tests; see the locale
    // member variable in EntityDAO, and its initializeI18nStrings() method
    public ItemGroupDAO(DataSource ds, DAODigester digester, Locale locale) {

        this(ds, digester);
        this.getCurrentPKName = "findCurrentPKValue";
        this.getNextPKName = "getNextPK";
        this.locale = locale;
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_ITEM_GROUP;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        /*
         * item_group_id serial NOT NULL, name varchar(255), crf_id numeric NOT
         * NULL, status_id numeric, date_created date, date_updated date,
         * owner_id numeric, update_id numeric,
         */
        this.setTypeExpected(1, TypeNames.INT); // item_group_id
        this.setTypeExpected(2, TypeNames.STRING); // name
        this.setTypeExpected(3, TypeNames.INT);// crf_id
        this.setTypeExpected(4, TypeNames.INT); // status_id
        this.setTypeExpected(5, TypeNames.DATE); // date_created
        this.setTypeExpected(6, TypeNames.DATE); // date_updated
        this.setTypeExpected(7, TypeNames.INT); // owner_id
        this.setTypeExpected(8, TypeNames.INT); // update_id
        this.setTypeExpected(9, TypeNames.STRING); // oc_oid

    }

    public EntityBean update(EntityBean eb) {
        ItemGroupBean formGroupBean = (ItemGroupBean) eb;
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        /*
         * item_group_id serial NOT NULL, name varchar(255), crf_id numeric NOT
         * NULL, status_id numeric, date_created date, date_updated date,
         * owner_id numeric, update_id numeric,
         */
        variables.put(1, formGroupBean.getName());
        variables.put(2, new Integer(formGroupBean.getCrfId()));
        variables.put(3, formGroupBean.getStatus().getId());
        variables.put(4, formGroupBean.getUpdater().getId());
        variables.put(5, formGroupBean.getId());
        this.execute(digester.getQuery("update"), variables);
        return eb;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException {
        return new ArrayList();
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        return new ArrayList();
    }

    private String getOid(ItemGroupBean itemGroupBean, String crfName, String itemGroupLabel) {

        String oid;
        try {
            oid = itemGroupBean.getOid() != null ? itemGroupBean.getOid() : itemGroupBean.getOidGenerator().generateOid(crfName, itemGroupLabel);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(ItemGroupBean itemGroup, String crfName, String itemGroupLabel, ArrayList<String> oidList) {

        String oid = getOid(itemGroup, crfName, itemGroupLabel);
        logger.debug(oid);
        String oidPreRandomization = oid;
        while (findByOid(oid) != null || oidList.contains(oid)) {
            oid = itemGroup.getOidGenerator().randomizeOid(oidPreRandomization);
        }
        return oid;
    }

    /*
     * name varchar(255), crf_id numeric NOT NULL, status_id numeric,
     * date_created date, date_updated date, owner_id numeric, update_id
     * numeric,
     */
    public EntityBean create(EntityBean eb) {
        ItemGroupBean formGroupBean = (ItemGroupBean) eb;
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        int id = getNextPK();
        variables.put(1, id);
        variables.put(2, formGroupBean.getName());
        variables.put(3, formGroupBean.getCrfId());
        variables.put(4, new Integer(formGroupBean.getStatus().getId()));
        variables.put(5, formGroupBean.getOwner().getId());

        this.execute(digester.getQuery("create"), variables);
        if (isQuerySuccessful()) {
            eb.setId(id);
            eb.setActive(true);
        }
        return eb;
    }

    public Collection findAll() {
        this.setTypesExpected();
        List listofMaps = this.select(digester.getQuery("findAll"));
        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (Object map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);
            beanList.add(bean);
        }
        return beanList;
    }

    // YW 10-30-2007, one item_id might have more than one item_groups
    public Collection findGroupsByItemID(int ID) {
        this.setTypesExpected();
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, ID);
        List listofMap = this.select(digester.getQuery("findGroupsByItemID"), variables);

        List<ItemGroupBean> formGroupBs = new ArrayList<ItemGroupBean>();
        for (Object map : listofMap) {
            ItemGroupBean bean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);
            formGroupBs.add(bean);
        }
        return formGroupBs;

    }

    public List<ItemGroupBean> findGroupByCRFVersionIDMap(int Id) {
        this.setTypesExpected();
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, Id);
        List listofMaps = this.select(digester.getQuery("findGroupByCRFVersionIDMap"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (Object map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);
            beanList.add(bean);
        }
        return beanList;

    }

    public EntityBean findByPK(int ID) {
        ItemGroupBean formGroupB = new ItemGroupBean();
        this.setTypesExpected();

        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, ID);

        String sql = digester.getQuery("findByPK");
        ArrayList listofMap = this.select(sql, variables);
        for (Object map : listofMap) {
            formGroupB = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);

        }
        return formGroupB;
    }

    public EntityBean findByName(String name) {
        ItemGroupBean formGroupBean = new ItemGroupBean();
        this.setTypesExpected();

        HashMap<Integer, String> variables = new HashMap<Integer, String>();
        variables.put(1, name);

        String sql = digester.getQuery("findByName");
        ArrayList listofMap = this.select(sql, variables);
        for (Object map : listofMap) {
            formGroupBean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);

        }
        return formGroupBean;
    }

    public List<ItemGroupBean> findAllByOid(String oid) {
        // ItemGroupBean itemGroup = new ItemGroupBean();
        this.unsetTypeExpected();
        setTypesExpected();

        HashMap<Integer, String> variables = new HashMap<Integer, String>();
        variables.put(new Integer(1), oid);
        String sql = digester.getQuery("findGroupByOid");

        ArrayList rows = this.select(sql, variables);
        // return rows;
        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (Object map : rows) {
            bean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);
            beanList.add(bean);
        }
        return beanList;
    }

    public ItemGroupBean findByOid(String oid) {
        ItemGroupBean itemGroup = new ItemGroupBean();
        this.unsetTypeExpected();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);
        String sql = digester.getQuery("findGroupByOid");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            itemGroup = (ItemGroupBean) this.getEntityFromHashMap((HashMap) it.next());
            return itemGroup;
        } else {
            return null;
        }
    }

    public ItemGroupBean findByOidAndCrf(String oid, int crfId) {
        ItemGroupBean itemGroup = new ItemGroupBean();
        this.unsetTypeExpected();
        setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), oid);
        variables.put(new Integer(2), new Integer(crfId));
        String sql = digester.getQuery("findGroupByOidAndCrfId");

        ArrayList rows = this.select(sql, variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            itemGroup = (ItemGroupBean) this.getEntityFromHashMap((HashMap) it.next());
            return itemGroup;
        } else {
            return null;
        }
    }

    public List<ItemGroupBean> findGroupByCRFVersionID(int Id) {
        ItemGroupBean itemGroup = new ItemGroupBean();
        this.unsetTypeExpected();
        setTypesExpected();

        
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, Id);
        List listofMaps = this.select(digester.getQuery("findGroupByCRFVersionID"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (Object map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);
            beanList.add(bean);
        }
        return beanList;
    }

    public ItemGroupBean findGroupByGroupNameAndCrfVersionId(String groupName, int crfVersionId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfVersionId));
        variables.put(new Integer(2), groupName);

        ArrayList rows = this.select(digester.getQuery("findGroupByGroupNameCRFVersionID"), variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (ItemGroupBean) this.getEntityFromHashMap((HashMap) it.next());
        } else {
            return null;
        }
    }

    public ItemGroupBean findGroupByItemIdCrfVersionId(int itemId, int crfVersionId) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(crfVersionId));
        variables.put(new Integer(2), new Integer(itemId));

        ArrayList rows = this.select(digester.getQuery("findGroupByItemIdCRFVersionID"), variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            return (ItemGroupBean) this.getEntityFromHashMap((HashMap) it.next());
        } else {
            return null;
        }
    }

    public List<ItemGroupBean> findOnlyGroupsByCRFVersionID(int Id) {
        this.setTypesExpected();
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, Id);
        List listofMaps = this.select(digester.getQuery("findOnlyGroupsByCRFVersionID"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (Object map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);
            beanList.add(bean);
        }
        return beanList;
    }

    public List<ItemGroupBean> findGroupBySectionId(int sectionId) {
        this.setTypesExpected();
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, sectionId);
        List listofMaps = this.select(digester.getQuery("findGroupBySectionId"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (Object map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);
            beanList.add(bean);
        }
        return beanList;
    }

    public List<ItemGroupBean> findLegitGroupBySectionId(int sectionId) {
        this.setTypesExpected();
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, sectionId);
        List listofMaps = this.select(digester.getQuery("findLegitGroupBySectionId"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (Object map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);
            beanList.add(bean);
        }
        return beanList;
    }
    public List<ItemGroupBean> findLegitGroupAllBySectionId(int sectionId) {
        this.setTypesExpected();
        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, sectionId);
        List listofMaps = this.select(digester.getQuery("findLegitGroupAllBySectionId"), variables);

        List<ItemGroupBean> beanList = new ArrayList<ItemGroupBean>();
        ItemGroupBean bean;
        for (Object map : listofMaps) {
            bean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);
            beanList.add(bean);
        }
        return beanList;
    }
    public Object getEntityFromHashMap(HashMap hm) {
        ItemGroupBean formGroupBean = new ItemGroupBean();
        super.setEntityAuditInformation(formGroupBean, hm);
        formGroupBean.setId((Integer) hm.get("item_group_id"));
        formGroupBean.setName((String) hm.get("name"));
        formGroupBean.setCrfId((Integer) hm.get("crf_id"));
        formGroupBean.setOid((String) hm.get("oc_oid"));

        return formGroupBean;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        return new ArrayList();
    }

    public void deleteTestGroup(String name) {
        HashMap variables = new HashMap();
        variables.put(new Integer(1), name);
        this.execute(digester.getQuery("deleteTestGroup"), variables);
    }
    
    public Boolean isItemGroupRepeatingBasedOnAllCrfVersions(String groupOid) {
    	Boolean result = false;
        setTypesExpected();
        HashMap<Integer, String> variables = new HashMap<Integer, String>();
        variables.put(1, groupOid);

        String sql = digester.getQuery("isItemGroupRepeatingBasedOnAllCrfVersions");

        ArrayList rows = this.select(sql,variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            result = count > 0 ? true : false;
        } 
        return result;
    }
    
    public Boolean isItemGroupRepeatingBasedOnCrfVersion(String groupOid,Integer crfVersion) {
    	Boolean result = false;
        setTypesExpected();
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(1, groupOid);
        variables.put(2, crfVersion);

        String sql = digester.getQuery("isItemGroupRepeatingBasedOnCrfVersion");

        ArrayList rows = this.select(sql,variables);
        Iterator it = rows.iterator();

        if (it.hasNext()) {
            Integer count = (Integer) ((HashMap) it.next()).get("count");
            result = count > 0 ? true : false;
        } 
        return result;
    }
    

    public ItemGroupBean findTopOneGroupBySectionId(int sectionId) {
        ItemGroupBean formGroupBean = new ItemGroupBean();
        this.setTypesExpected();

        HashMap<Integer, Integer> variables = new HashMap<Integer, Integer>();
        variables.put(1, sectionId);

        String sql = digester.getQuery("findTopOneGroupBySectionId");
        ArrayList listofMap = this.select(sql, variables);
        for (Object map : listofMap) {
            formGroupBean = (ItemGroupBean) this.getEntityFromHashMap((HashMap) map);

        }
        return formGroupBean;
    }
    @Override
    public ArrayList<V> select(String query, HashMap variables) {
        clearSignals();

        ArrayList results = new ArrayList();
        V  value;
        K key;
        ResultSet rs = null;
        Connection con = null;
        PreparedStatementFactory psf = new PreparedStatementFactory(variables);
        PreparedStatement ps = null;
        
        try {
            con = ds.getConnection();
            CoreResources.setSchema(con);

            if (con.isClosed()) {
                if (logger.isWarnEnabled())
                    logger.warn("Connection is closed: GenericDAO.select!");
                throw new SQLException();
            }

           ps = con.prepareStatement(query);
           
       
            ps = psf.generate(ps);// enter variables here!
            key = (K) ps.toString();
            if((results=(V) cache.get(key))==null)
            {
            rs = ps.executeQuery();
            results = this.processResultRows(rs);
            if(results!=null){
                cache.put(key,results);
            }
            }
            
           // if (logger.isInfoEnabled()) {
                logger.debug("Executing dynamic query, EntityDAO.select:query " + query);
          //  }
            signalSuccess();
              

        } catch (SQLException sqle) {
            signalFailure(sqle);
            if (logger.isWarnEnabled()) {
                logger.warn("Exception while executing dynamic query, GenericDAO.select: " + query + ":message: " + sqle.getMessage());
                sqle.printStackTrace();
            }
        } finally {
            this.closeIfNecessary(con, rs, ps);
        }
        return results;

    }
}
