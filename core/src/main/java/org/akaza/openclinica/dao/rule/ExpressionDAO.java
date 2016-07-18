/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.dao.rule;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.rule.expression.Context;
import org.akaza.openclinica.bean.rule.expression.ExpressionBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>
 * Manage Rules
 * </p>
 * 
 * 
 * @author Krikor Krumlian
 * 
 */
public class ExpressionDAO extends AuditableEntityDAO {

    private void setQueryNames() {
        this.findByPKAndStudyName = "findByPKAndStudy";
        this.getCurrentPKName = "getCurrentPK";
    }

    public ExpressionDAO(DataSource ds) {
        super(ds);
        setQueryNames();
    }

    public ExpressionDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        setQueryNames();
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_EXPRESSION;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT); // rule_expression_id
        this.setTypeExpected(2, TypeNames.STRING); // value
        this.setTypeExpected(3, TypeNames.INT); // context

        // Standard set of fields
        this.setTypeExpected(4, TypeNames.INT);// owner_id
        this.setTypeExpected(5, TypeNames.DATE); // date_created
        this.setTypeExpected(6, TypeNames.DATE);// date_updated
        this.setTypeExpected(7, TypeNames.INT);// updater_id
        this.setTypeExpected(8, TypeNames.INT);// status_id
    }

    public EntityBean update(EntityBean eb) {
        ExpressionBean expressionBean = (ExpressionBean) eb;

        expressionBean.setActive(false);

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap nullVars = new HashMap();
        variables.put(new Integer(1), expressionBean.getContext().getCode());
        variables.put(new Integer(2), expressionBean.getValue());
        variables.put(new Integer(3), expressionBean.getUpdaterId());
        variables.put(new Integer(4), expressionBean.getId());

        this.execute(digester.getQuery("update"), variables, nullVars);

        if (isQuerySuccessful()) {
            expressionBean.setActive(true);
        }

        return expressionBean;
    }

    public EntityBean create(EntityBean eb) {
        ExpressionBean expressionBean = (ExpressionBean) eb;
        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        HashMap<Integer, Object> nullVars = new HashMap<Integer, Object>();
        variables.put(new Integer(1), expressionBean.getContext().getCode());
        variables.put(new Integer(2), expressionBean.getValue());

        variables.put(new Integer(3), new Integer(expressionBean.getOwnerId()));
        variables.put(new Integer(4), new Integer(Status.AVAILABLE.getId()));

        executeWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            expressionBean.setId(getLatestPK());
        }

        return expressionBean;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        ExpressionBean expressionBean = new ExpressionBean();
        this.setEntityAuditInformation(expressionBean, hm);

        expressionBean.setId(((Integer) hm.get("rule_expression_id")).intValue());
        expressionBean.setContext(Context.getByCode(((Integer) hm.get("context"))));
        expressionBean.setValue(((String) hm.get("value")));

        return expressionBean;
    }

    /*
     * Do not return All
     * @see org.akaza.openclinica.dao.core.DAOInterface#findAll()
     */
    public Collection findAll() {
        return null;
    }

    public EntityBean findByPK(int ID) {
        ExpressionBean expressionBean = new ExpressionBean();
        this.setTypesExpected();

        HashMap<Integer, Object> variables = new HashMap<Integer, Object>();
        variables.put(new Integer(1), new Integer(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            expressionBean = (ExpressionBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return expressionBean;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    /*
     * Why should we even have these in here if they are not needed? TODO: refactor super class to remove dependency.
     */
    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }

}
