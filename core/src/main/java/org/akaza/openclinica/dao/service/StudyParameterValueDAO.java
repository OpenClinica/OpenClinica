/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.service;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameter;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.service.StudyParamsConfig;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.sql.DataSource;

public class StudyParameterValueDAO extends AuditableEntityDAO {

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_STUDY_PARAMETER;

    }

    public StudyParameterValueDAO(DataSource ds) {
        super(ds);
    }

    public StudyParameterValueDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    public Collection findAll() {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public EntityBean create(EntityBean eb) {
        StudyParameterValueBean spvb = (StudyParameterValueBean) eb;
        HashMap variables = new HashMap();

        variables.put(new Integer(1), new Integer(spvb.getStudyId()));
        variables.put(new Integer(2), spvb.getValue());
        variables.put(new Integer(3), spvb.getParameter());

        this.execute(digester.getQuery("create"), variables);
        return spvb;

    }

    public EntityBean update(EntityBean eb) {
        StudyParameterValueBean spvb = (StudyParameterValueBean) eb;
        HashMap variables = new HashMap();

        variables.put(new Integer(1), spvb.getValue());
        variables.put(new Integer(2), new Integer(spvb.getStudyId()));
        variables.put(new Integer(3), spvb.getParameter());

        this.execute(digester.getQuery("update"), variables);
        return spvb;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        // study_id numeric,
        // value varchar(50),
        // study_parameter_id int4,
        StudyParameterValueBean spvb = new StudyParameterValueBean();
        // super.setEntityAuditInformation(spvb, hm);
        spvb.setValue((String) hm.get("value"));
        spvb.setStudyId(((Integer) hm.get("study_id")).intValue());
        spvb.setId(((Integer) hm.get("study_parameter_value_id")).intValue());
        // YW 10-15-2007 <<
        spvb.setParameter((String) hm.get("parameter"));
        // YW >>

        return spvb;
    }

    public Object getParameterEntityFromHashMap(HashMap hm) {
        // study_parameter_id serial NOT NULL,
        // handle varchar(50),
        // name varchar(50),
        // description varchar(255),
        // default_value varchar(50),
        // inheritable bool DEFAULT true,
        // overridable bool,
        StudyParameter sp = new StudyParameter();
        // super.setEntityAuditInformation(spvb, hm);
        sp.setId(((Integer) hm.get("study_parameter_id")).intValue());
        sp.setHandle((String) hm.get("handle"));
        sp.setName((String) hm.get("name"));
        sp.setDescription((String) hm.get("description"));
        sp.setDefaultValue((String) hm.get("default_value"));
        sp.setInheritable(((Boolean) hm.get("inheritable")).booleanValue());
        sp.setOverridable(((Boolean) hm.get("overridable")).booleanValue());
        return sp;
    }

    @Override
    public void setTypesExpected() {
        // study_parameter_value_id serial NOT NULL,
        // study_id int4,
        // value varchar(50),
        // parameter varchar(50),

        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);

    }

    public void setTypesExpectedForParameter() {
        // study_parameter_id serial NOT NULL,
        // handle varchar(50),
        // name varchar(50),
        // description varchar(255),
        // default_value varchar(50),
        // inheritable bool DEFAULT true,
        // overridable bool,
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.STRING);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.STRING);
        this.setTypeExpected(6, TypeNames.BOOL);
        this.setTypeExpected(7, TypeNames.BOOL);

    }

    public StudyParameterValueBean findByHandleAndStudy(int studyId, String handle) {
        StudyParameterValueBean spvb = new StudyParameterValueBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(studyId));
        variables.put(new Integer(2), handle);

        String sql = digester.getQuery("findByStudyAndHandle");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            spvb = (StudyParameterValueBean) this.getEntityFromHashMap((HashMap) it.next());
        }
        return spvb;
    }

    public StudyParameter findParameterByHandle(String handle) {
        StudyParameter sp = new StudyParameter();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(new Integer(1), handle);

        String sql = digester.getQuery("findParameterByHandle");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            sp = (StudyParameter) this.getEntityFromHashMap((HashMap) it.next());
        }
        return sp;

    }

    public boolean setParameterValue(int studyId, String parameterHandle, String value) {

        return false;

    }

    public ArrayList findAllParameters() {
        this.setTypesExpectedForParameter();
        ArrayList alist = this.select(digester.getQuery("findAllParameters"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyParameter eb = (StudyParameter) this.getParameterEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public ArrayList findAllParameterValuesByStudy(StudyBean study) {
        this.setTypesExpected();
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(study.getId()));

        ArrayList alist = this.select(digester.getQuery("findAllParameterValuesByStudy"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            StudyParameterValueBean eb = (StudyParameterValueBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public ArrayList findParamConfigByStudy(StudyBean study) {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);
        this.setTypeExpected(2, TypeNames.INT);
        this.setTypeExpected(3, TypeNames.STRING);
        this.setTypeExpected(4, TypeNames.STRING);
        this.setTypeExpected(5, TypeNames.INT);
        this.setTypeExpected(6, TypeNames.STRING);
        this.setTypeExpected(7, TypeNames.STRING);
        this.setTypeExpected(8, TypeNames.STRING);
        this.setTypeExpected(9, TypeNames.STRING);
        this.setTypeExpected(10, TypeNames.BOOL);
        this.setTypeExpected(11, TypeNames.BOOL);
        HashMap variables = new HashMap();
        variables.put(new Integer(1), new Integer(study.getId()));

        ArrayList alist = this.select(digester.getQuery("findParamConfigByStudy"), variables);
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            HashMap hm = (HashMap) it.next();
            StudyParameterValueBean spvb = new StudyParameterValueBean();
            spvb.setValue((String) hm.get("value"));
            spvb.setStudyId(((Integer) hm.get("study_id")).intValue());
            spvb.setId(((Integer) hm.get("study_parameter_value_id")).intValue());

            StudyParameter sp = new StudyParameter();
            sp.setId(((Integer) hm.get("study_parameter_id")).intValue());
            sp.setHandle((String) hm.get("handle"));
            sp.setName((String) hm.get("name"));
            sp.setDescription((String) hm.get("description"));
            sp.setDefaultValue((String) hm.get("default_value"));
            sp.setInheritable(((Boolean) hm.get("inheritable")).booleanValue());
            sp.setOverridable(((Boolean) hm.get("overridable")).booleanValue());

            StudyParamsConfig config = new StudyParamsConfig();
            config.setParameter(sp);
            config.setValue(spvb);
            al.add(config);
        }
        return al;

    }

    public EntityBean findByPK(int ID) {
        EntityBean eb = new StudyParameterValueBean();
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
