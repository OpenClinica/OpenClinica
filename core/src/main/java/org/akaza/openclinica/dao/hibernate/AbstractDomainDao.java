package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.DomainObject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractDomainDao<T extends DomainObject> {

    private SessionFactory sessionFactory;

    abstract Class<T> domainClass();

    public String getDomainClassName() {
        return domainClass().getName();
    }

    @SuppressWarnings("unchecked")
    public T findById(Integer id) {
        String query = "from " + getDomainClassName() + " do  where do.id = :id";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("id", id);
        return (T) q.uniqueResult();
    }

    @Transactional
    public T saveOrUpdate(T domainObject) {
        getCurrentSession().saveOrUpdate(domainObject);
        return domainObject;
    }

    public Long count() {
        return (Long) getCurrentSession().createQuery("select count(*) from " + domainClass().getName()).uniqueResult();
    }

    /**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * @return Session Object
     */
    protected Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

}
