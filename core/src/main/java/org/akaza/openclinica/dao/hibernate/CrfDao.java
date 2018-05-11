package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.oid.CrfOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.domain.datamap.CrfBean;

public class CrfDao extends AbstractDomainDao<CrfBean> {

    @Override
    Class<CrfBean> domainClass() {
        // TODO Auto-generated method stub
        return CrfBean.class;
    }

    public CrfBean findByName(String crfName) {
        String query = "from " + getDomainClassName() + " crf  where crf.name = :crfName ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("crfName", crfName);
        return (CrfBean) q.uniqueResult();
    }

    public CrfBean findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("OCOID", OCOID);
        return (CrfBean) q.uniqueResult();
    }

    public CrfBean findByCrfId(Integer crfId) {
        String query = "from " + getDomainClassName() + " crf  where crf.crfId = :crfId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("crfId", crfId);
        return (CrfBean) q.uniqueResult();
    }
    
    private String getOid(CrfBean crf, String crfName) {
        OidGenerator oidGenerator = new CrfOidGenerator();
        String oid;
        try {
            oid = crf.getOcOid() != null ? crf.getOcOid() : oidGenerator.generateOid(crfName);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(CrfBean crfBean, String crfName) {
        OidGenerator oidGenerator = new CrfOidGenerator();
        String oid = getOid(crfBean, crfName);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;
    }

}
