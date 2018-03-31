
package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.datamap.PageLayout;
import org.hibernate.query.Query;

public class PageLayoutDao extends AbstractDomainDao<PageLayout> {

    @Override
    Class<PageLayout> domainClass() {
        // TODO Auto-generated method stub
        return PageLayout.class;
    }

    public PageLayout findByPageLayoutName(String pageName) {
        String query = "from " + getDomainClassName() + " page_layout  where page_layout.name = :pageName ";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("pageName", pageName);
        return (PageLayout) q.uniqueResult();
    }

}
