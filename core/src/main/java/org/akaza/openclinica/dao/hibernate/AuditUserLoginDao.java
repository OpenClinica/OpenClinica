package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;

import java.util.ArrayList;

public class AuditUserLoginDao extends AbstractDomainDao<AuditUserLoginBean> {

    @Override
    public Class<AuditUserLoginBean> domainClass() {
        return AuditUserLoginBean.class;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<AuditUserLoginBean> findAll() {
        String query = "from " + getDomainClassName() + " aul order by aul.loginAttemptDate desc ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return (ArrayList<AuditUserLoginBean>) q.list();
    }

    public int getCountWithFilter(final AuditUserLoginFilter filter) {
        Criteria criteria = getCurrentSession().createCriteria(domainClass());
        criteria = filter.execute(criteria);
        criteria.setProjection(Projections.rowCount()).uniqueResult();
        return ((Long) criteria.uniqueResult()).intValue();
    }

    @SuppressWarnings("unchecked")
    public ArrayList<AuditUserLoginBean> getWithFilterAndSort(final AuditUserLoginFilter filter, final AuditUserLoginSort sort, final int rowStart,
            final int rowEnd) {
        Criteria criteria = getCurrentSession().createCriteria(domainClass());
        criteria = filter.execute(criteria);
        criteria = sort.execute(criteria);
        criteria.setFirstResult(rowStart);
        criteria.setMaxResults(rowEnd - rowStart);
        return (ArrayList<AuditUserLoginBean>) criteria.list();
    }

}
