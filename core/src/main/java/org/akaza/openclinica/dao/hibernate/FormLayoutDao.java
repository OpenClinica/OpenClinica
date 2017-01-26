
package org.akaza.openclinica.dao.hibernate;

import java.util.List;

import org.akaza.openclinica.bean.oid.CrfVersionOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.hibernate.query.Query;

public class FormLayoutDao extends AbstractDomainDao<FormLayout> {
    private static String findByOcIdQuery = "from FormLayout fl  where fl.ocOid = :OCOID";

    @Override
    Class<FormLayout> domainClass() {
        // TODO Auto-generated method stub
        return FormLayout.class;
    }

    public FormLayout findByFormLayoutId(int formLayoutId) {
        String query = "from " + getDomainClassName() + " form_layout  where form_layout.formLayoutId = :formlayoutid ";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("formlayoutid", formLayoutId);
        return (FormLayout) q.uniqueResult();
    }

    public FormLayout findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        Query q = getCurrentSession().createQuery(findByOcIdQuery);
        q.setParameter("OCOID", OCOID);
        return (FormLayout) q.uniqueResult();
    }

    public FormLayout findByNameCrfId(String name, Integer crfId) {
        String query = "select distinct cv.* from form_layout cv,crf c " + "where c.crf_id = " + crfId + " and cv.name = '" + name
                + "' and cv.crf_id = c.crf_id";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(FormLayout.class);
        return ((FormLayout) q.uniqueResult());
    }

    private String getOid(FormLayout formLayout, String crfName, String formLayoutName) {
        OidGenerator oidGenerator = new CrfVersionOidGenerator();
        String oid;
        try {
            oid = formLayout.getOcOid() != null ? formLayout.getOcOid() : oidGenerator.generateOid(crfName, formLayoutName);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(FormLayout formLayout, String crfName, String formLayoutName) {
        OidGenerator oidGenerator = new CrfVersionOidGenerator();
        String oid = getOid(formLayout, crfName, formLayoutName);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;

    }

    public List<FormLayout> findAllByCrfId(int crfId) {
        String query = "from " + getDomainClassName() + " form_layout  where form_layout.crf.crfId = :crfId ";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("crfId", crfId);
        return (List<FormLayout>) q.list();
    }

}
