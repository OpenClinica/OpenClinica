/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.extract;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.extract.FilterBean;
import org.akaza.openclinica.bean.extract.FilterObjectBean;
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
 * The data access object for filters.
 *
 * @author thickerson
 *
 */
public class FilterDAO extends AuditableEntityDAO {
    private DAODigester digester;

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_FILTER;
    }

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
        getNextPKName = "getNextPK";
        // TODO figure out the error with current primary keys?
    }

    public FilterDAO(DataSource ds) {
        super(ds);
        digester = SQLFactory.getInstance().getDigester(digesterName);
        this.setQueryNames();
    }

    /**
     * creator object to be used during testing, tbh
     *
     * @param ds
     * @param digester
     */
    public FilterDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// filter id
        this.setTypeExpected(2, TypeNames.STRING);// name
        this.setTypeExpected(3, TypeNames.STRING);// description
        this.setTypeExpected(4, TypeNames.STRING);// sql statement?
        this.setTypeExpected(5, TypeNames.INT);// status id
        this.setTypeExpected(6, TypeNames.DATE);// created
        this.setTypeExpected(7, TypeNames.DATE);// updated
        this.setTypeExpected(8, TypeNames.INT);// owner id
        this.setTypeExpected(9, TypeNames.INT);// update id
    }

    public EntityBean update(EntityBean eb) {
        FilterBean fb = (FilterBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        variables.put(new Integer(1), fb.getName());
        variables.put(new Integer(2), fb.getDescription());
        variables.put(new Integer(3), new Integer(fb.getStatus().getId()));
        variables.put(new Integer(4), fb.getSQLStatement());// string, updateid,
        // filterid
        variables.put(new Integer(5), new Integer(fb.getUpdaterId()));
        variables.put(new Integer(6), new Integer(fb.getId()));
        this.execute(digester.getQuery("update"), variables, nullVars);
        return fb;
    }

    public EntityBean create(EntityBean eb) {
        FilterBean fb = (FilterBean) eb;
        logger.info("logged following owner id: " + fb.getOwnerId() + " vs. " + fb.getOwner().getId());
        HashMap variables = new HashMap();
        int id = getNextPK();
        // HashMap nullVars = new HashMap();
        variables.put(new Integer(1), fb.getId());
        variables.put(new Integer(2), fb.getName());
        // name desc sql, status id owner id
        variables.put(new Integer(3), fb.getDescription());
        variables.put(new Integer(4), fb.getSQLStatement());
        variables.put(new Integer(5), new Integer(fb.getStatus().getId()));
        variables.put(new Integer(6), new Integer(fb.getOwner().getId()));
        // changed from get owner id, tbh

        this.execute(digester.getQuery("create"), variables);

        fb.setId(id);
        return fb;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        FilterBean fb = new FilterBean();
        this.setEntityAuditInformation(fb, hm);
        fb.setDescription((String) hm.get("description"));
        fb.setName((String) hm.get("name"));
        fb.setId(((Integer) hm.get("filter_id")).intValue());
        fb.setSQLStatement((String) hm.get("sql_statement"));
        return fb;
    }

    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            FilterBean fb = (FilterBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(fb);
        }
        return al;
    }

    public Collection findAllAdmin() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAllAdmin"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            FilterBean fb = (FilterBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(fb);
        }
        return al;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public EntityBean findByPK(int ID) {
        FilterBean fb = new FilterBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            fb = (FilterBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return fb;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

    /**
     * To be created with the header string and an array list of
     * FilterObjectBeans, which contain the number, type and value of criteria
     * to apply to the existing filter.
     *
     * fundamental change 06-03: adding the new query formation will change the
     * SQL so that
     *
     * @param oldSQLStatement
     * @param connector
     * @param filterObjs
     */
    public String genSQLStatement(String oldSQLStatement, String connector, ArrayList filterObjs) {
        StringBuffer sb = new StringBuffer();
        // sb.append(" and subject_id in "+
        // "(select subject_id from extract_data_table where ");
        if (oldSQLStatement != null) {
            sb.append(oldSQLStatement);
        } else {
            sb.append(" and subject_id in " + "(select subject_id from extract_data_table where ");
        }
        String tailEnd = "";
        Iterator it = filterObjs.iterator();
        int count = 0;
        while (it.hasNext()) {
            FilterObjectBean fob = (FilterObjectBean) it.next();
            tailEnd = "(" + tailEnd;
            if (count != 0) {
                tailEnd = tailEnd + " " + connector + " ";// fob.getOperand();
            }
            count++;
            // TODO add this to create like operators, maybe move this to else
            // where?
            if (fob.getOperand().equals(" like ") || fob.getOperand().equals(" not like ")) {
                fob.setValue("%" + fob.getValue() + "%");
            }
            tailEnd = tailEnd + "(item_id = " + fob.getItemId() + " and value " + fob.getOperand() + " '" + fob.getValue() + "'))";
        }
        if (oldSQLStatement != null) {
            sb.append(" and ");
            // rearrange sql here, and above, so that
            // filter can be changed
        }
        sb.append(tailEnd);
        // sb.append(")");
        // and a parens at the very end!
        return sb.toString();
    }

    /**
     * Will generate an explanation stating that this filter will look for value
     * x at question y AND value like z at question a OR value not like a at
     * question c...
     *
     * @param oldExplanation
     * @param connector
     * @param filterObjs
     */
    public ArrayList genExplanation(ArrayList oldExplanation, String connector, ArrayList filterObjs) {
        ArrayList sb = new ArrayList();
        if (oldExplanation != null) {
            sb.addAll(oldExplanation);
        } else {
            sb.add("This Filter will look for:");
        }
        Iterator it = filterObjs.iterator();
        int count = 0;
        while (it.hasNext()) {
            FilterObjectBean fob = (FilterObjectBean) it.next();
            String answerLine = "A value " + fob.getOperand() + " " + fob.getValue() + " " + "for question " + fob.getItemName();

            sb.add(answerLine);
            count++;
            if (count < filterObjs.size()) {
                sb.add(connector + " ");
            }
        }
        return sb;
    }
}
