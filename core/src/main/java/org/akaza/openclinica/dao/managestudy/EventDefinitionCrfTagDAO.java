/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.managestudy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCrfTagBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.exception.OpenClinicaException;

/**
 * @author jxu
 * 
 */
public class EventDefinitionCrfTagDAO extends AuditableEntityDAO {
    // private DAODigester digester;

    private void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
        getNextPKName = "getNextPK";
        findAllByStudyName = "findAllByStudy";
    }

    public EventDefinitionCrfTagDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public EventDefinitionCrfTagDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_EVENTDEFINITIONCRFTAG;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.INT);
        this.setTypeExpected(4, TypeNames.BOOL);
        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.INT);
        this.setTypeExpected(7, TypeNames.DATE);
        this.setTypeExpected(8, TypeNames.DATE);

    }

    /**
     * <p>
     * getEntityFromHashMap, the method that gets the object from the database
     * query.
     */
    @Override
    public Object getEntityFromHashMap(HashMap hm) {
        EventDefinitionCrfTagBean eb = new EventDefinitionCrfTagBean();

        eb.setId(((Integer) hm.get("id")).intValue());
        eb.setPath(((String) hm.get("path")));
        eb.setTagId(((Integer) hm.get("tag_id")).intValue());
        eb.setActive(((Boolean) hm.get("active")).booleanValue());
        eb.setCreatedDate((Date) hm.get("date_created"));
        eb.setUpdatedDate((Date) hm.get("date_updated"));
        eb.setOwnerId((Integer) hm.get("owner_id"));
        eb.setUpdaterId((Integer) hm.get("update_id"));

        return eb;
    }

    @Override
    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            EventDefinitionCRFBean eb = (EventDefinitionCRFBean) this.getEntityFromHashMap((HashMap) it.next());
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
        EventDefinitionCRFBean eb = new EventDefinitionCRFBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = (EventDefinitionCRFBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    public EventDefinitionCrfTagBean findByCrfPath(int tagId, String path, boolean active) {
        EventDefinitionCrfTagBean eb = null;
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(tagId));
        variables.put(new Integer(2), path);
        variables.put(new Integer(3), active);

        String sql = digester.getQuery("findByCrfPath");

        ArrayList alist = this.select(sql, variables);

        Iterator it = alist.iterator();

        if (it.hasNext()) {
            eb = new EventDefinitionCrfTagBean();
            eb = (EventDefinitionCrfTagBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return eb;
    }

    @Override
    public EntityBean create(EntityBean eb) throws OpenClinicaException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EntityBean update(EntityBean eb) throws OpenClinicaException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase)
            throws OpenClinicaException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) throws OpenClinicaException {
        // TODO Auto-generated method stub
        return null;
    }

}