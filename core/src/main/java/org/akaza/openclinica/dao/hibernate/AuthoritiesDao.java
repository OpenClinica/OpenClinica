package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.user.AuthoritiesBean;


public class AuthoritiesDao extends AbstractDomainDao<AuthoritiesBean> {

    @Override
    public Class<AuthoritiesBean> domainClass() {
        return AuthoritiesBean.class;
    }

    public AuthoritiesBean findByUsername(String username) {
        String query = "from " + getDomainClassName() + " authorities  where authorities.username = :username ";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("username", username);
        return (AuthoritiesBean) q.uniqueResult();
    }
}
