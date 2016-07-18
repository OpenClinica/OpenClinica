package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.domain.DomainObject;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.ArrayList;


public abstract class AbstractDomainDao<T extends DomainObject> {

    private HibernateTemplate hibernateTemplate;

    abstract Class<T> domainClass();

    public String getDomainClassName() {
        return domainClass().getName();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public T findById(Integer id) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.id = :id";
        Query q = getCurrentSession().createQuery(query);
        q.setParameter("id", id, IntegerType.INSTANCE);
        return (T) q.uniqueResult();

    }

    @SuppressWarnings("unchecked")
    @Transactional
    public ArrayList<T> findAll() {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do";
        Query q = getCurrentSession().createQuery(query);
        return (ArrayList<T>) q.list();
    }
    
    @SuppressWarnings("unchecked")
	public T findByOcOID(String OCOID){
    	 getSessionFactory().getStatistics().logSummary();
         String query = "from " + getDomainClassName() + " do  where do.oc_oid = :oc_oid";
         Query q = getCurrentSession().createQuery(query);
         q.setParameter("oc_oid", OCOID, StringType.INSTANCE);
         return (T) q.uniqueResult();
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
    Query q = getCurrentSession().createQuery(query);
    q.setParameter(0, id);
    return (T) q.uniqueResult();
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
