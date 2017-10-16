/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.extract;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.extract.QueryBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.sql.DataSource;

/**
 * @author thickerson
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class QueryDAO extends AuditableEntityDAO {
    // private DataSource ds;

    public QueryDAO(DataSource ds) {
        super(ds);
    }

    @Override
    protected void setDigesterName() {
        // digesterName = SQLFactory.getInstance().DAO_QUERY;
    }

    @Override
    public void setTypesExpected() {

    }

    public EntityBean update(EntityBean eb) {
        return eb;
    }

    public EntityBean create(EntityBean eb) {
        return eb;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        QueryBean eb = new QueryBean();

        return eb;
    }

    public Collection findAll() {
        ArrayList al = new ArrayList();

        return al;
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
