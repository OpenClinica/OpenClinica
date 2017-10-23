package org.akaza.openclinica.dao.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.akaza.openclinica.domain.CompositeIdDomainObject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;


public abstract class CompositeIdAbstractDomainDao<T extends CompositeIdDomainObject> {

    private HibernateTemplate hibernateTemplate;

    abstract Class<T> domainClass();

    public String getDomainClassName() {
        return domainClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public ArrayList<T> findAll() {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return (ArrayList<T>) q.list();
    }
    
    @Transactional
    public T saveOrUpdate(T domainObject) {
        getSessionFactory().getStatistics().logSummary();
        getCurrentSession().saveOrUpdate(domainObject);
        return domainObject;
    }

    @Transactional
    public Serializable save(T domainObject) {
        getSessionFactory().getStatistics().logSummary();
        Serializable id = getCurrentSession().save(domainObject);
        return id;
    }

    

    @Transactional
    public T findByColumnName(Object id,String key) {
    String query = "from " + getDomainClassName() + " do where do."+key +"= ?";
    org.hibernate.Query q = getCurrentSession().createQuery(query);
    q.setParameter(0, id);
    return (T) q.uniqueResult();
    }

    @Transactional
    public Collection<T> findAllByColumnName(Object id,String key) {
        String query = "from " + getDomainClassName() + " do where do."+key +"= ?";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter(0, id);
        return (Collection<T>) q.list();
    }


    public Long count() {
        return (Long) getCurrentSession().createQuery("select count(*) from " + domainClass().getName()).uniqueResult();
    }

    public SessionFactory getSessionFactory() {
        return hibernateTemplate.getSessionFactory();
    }

    /**
     * @return Session Object
     */
    public Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

    public HibernateTemplate getHibernateTemplate() {
        return hibernateTemplate;
    }

    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }



}
