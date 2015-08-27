package org.akaza.openclinica.dao.hibernate;

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

    public CrfBean findByCrfId(Integer crfId) {
        String query = "from " + getDomainClassName() + " crf  where crf.crfId = :crfId ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("crfId", crfId);
        return (CrfBean) q.uniqueResult();
    }

}
