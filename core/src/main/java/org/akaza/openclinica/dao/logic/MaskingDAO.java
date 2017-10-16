/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.logic;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.extract.QueryBean;
import org.akaza.openclinica.bean.masking.MaskingBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.DataSource;

/**
 * @author thickerson
 *
 *
 */
public class MaskingDAO extends AuditableEntityDAO {
    private DAODigester digester;

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_MASKING;
        // TODO work on new instance
    }

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
        // TODO figure out the error with current primary keys?
    }

    public MaskingDAO(DataSource ds) {
        super(ds);
        digester = SQLFactory.getInstance().getDigester(digesterName);
        this.setQueryNames();
    }

    public MaskingDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// mask id
        this.setTypeExpected(2, TypeNames.STRING);// name
        this.setTypeExpected(3, TypeNames.STRING);// desc
        this.setTypeExpected(4, TypeNames.INT);// status id
        this.setTypeExpected(5, TypeNames.INT);// owner id
        this.setTypeExpected(6, TypeNames.TIMESTAMP);// created
        this.setTypeExpected(7, TypeNames.TIMESTAMP);// updated
        this.setTypeExpected(8, TypeNames.INT);// update id
        this.setTypeExpected(9, TypeNames.INT);// study id
        this.setTypeExpected(10, TypeNames.INT);// role id
        this.setTypeExpected(11, TypeNames.STRING);// entity name
        this.setTypeExpected(12, TypeNames.INT);// entity id

    }

    public Object getEntityFromHashMap(HashMap hm) {
        MaskingBean mb = new MaskingBean();
        this.setEntityAuditInformation(mb, hm);
        mb.setName((String) hm.get("name"));
        mb.setId(((Integer) hm.get("mask_id")).intValue());
        // TODO set other variables here, tbh
        return mb;
    }

    public java.util.Collection findAll() {
        this.setTypesExpected();
        ArrayList aList = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = aList.iterator();
        while (it.hasNext()) {
            MaskingBean mb = (MaskingBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(mb);
        }
        return al;
    }

    public EntityBean update(EntityBean eb) {
        return eb;
    }

    public EntityBean create(EntityBean eb) {
        return eb;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public EntityBean findByPK(int ID) {
        QueryBean eb = new QueryBean();

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

}
