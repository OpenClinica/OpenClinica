package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.bean.oid.CrfVersionOidGenerator;
import org.akaza.openclinica.bean.oid.OidGenerator;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.hibernate.query.Query;

public class CrfVersionDao extends AbstractDomainDao<CrfVersion> {
    private static String findByOcIdQuery = "from CrfVersion cv  where cv.ocOid = :OCOID";

    @Override
    Class<CrfVersion> domainClass() {
        // TODO Auto-generated method stub
        return CrfVersion.class;
    }

    public CrfVersion findByCrfVersionId(int crf_version_id) {
        String query = "from " + getDomainClassName() + " crf_version  where crf_version.crfVersionId = :crfversionid ";
        Query q = getCurrentSession().createQuery(query).setCacheable(true);
        q.setParameter("crfversionid", crf_version_id);
        return (CrfVersion) q.uniqueResult();
    }

    public void resetCache() {
        getCurrentSession().getSessionFactory().getCache().evictAllRegions();
    }
    public CrfVersion findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        Query q = getCurrentSession().createQuery(findByOcIdQuery).setCacheable(true);
        q.setParameter("OCOID", OCOID);
        return (CrfVersion) q.uniqueResult();
    }

    public CrfVersion findByNameCrfId(String name, Integer crfId) {
        String query = "select distinct cv.* from crf_version cv,crf c " + "where c.crf_id = " + crfId + " and cv.name = '" + name
                + "' and cv.crf_id = c.crf_id";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(CrfVersion.class);
        return ((CrfVersion) q.uniqueResult());
    }
    
    private String getOid(CrfVersion crfVersion, String crfName, String crfVersionName) {
        OidGenerator oidGenerator = new CrfVersionOidGenerator();
        String oid;
        try {
            oid = crfVersion.getOcOid() != null ? crfVersion.getOcOid() : oidGenerator.generateOid(crfName, crfVersionName);
            return oid;
        } catch (Exception e) {
            throw new RuntimeException("CANNOT GENERATE OID");
        }
    }

    public String getValidOid(CrfVersion crfVersion, String crfName, String crfVersionName) {
        OidGenerator oidGenerator = new CrfVersionOidGenerator();
        String oid = getOid(crfVersion, crfName, crfVersionName);
        String oidPreRandomization = oid;
        while (findByOcOID(oid) != null) {
            oid = oidGenerator.randomizeOid(oidPreRandomization);
        }
        return oid;

    }


}
