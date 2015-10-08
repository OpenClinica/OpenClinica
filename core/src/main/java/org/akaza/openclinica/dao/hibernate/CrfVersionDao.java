package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.CrfVersion;

public class CrfVersionDao extends AbstractDomainDao<CrfVersion> {

    @Override
    Class<CrfVersion> domainClass() {
        // TODO Auto-generated method stub
        return CrfVersion.class;
    }

    public CrfVersion findByCrfVersionId(int crf_version_id) {
        String query = "from " + getDomainClassName() + " crf_version  where crf_version.crfVersionId = :crfversionid ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("crfversionid", crf_version_id);
        return (CrfVersion) q.uniqueResult();
    }

    public CrfVersion findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.ocOid = :OCOID";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("OCOID", OCOID);
        return (CrfVersion) q.uniqueResult();
    }

    public CrfVersion findByNameCrfId(String name, Integer crfId) {
        String query = "select distinct cv.* from crf_version cv,crf c " + "where c.crf_id = " + crfId + " and cv.name = '" + name
                + "' and cv.crf_id = c.crf_id";
        org.hibernate.Query q = getCurrentSession().createSQLQuery(query).addEntity(CrfVersion.class);
        return ((CrfVersion) q.uniqueResult());
    }

}
