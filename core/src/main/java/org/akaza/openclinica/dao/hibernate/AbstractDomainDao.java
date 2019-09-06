package org.akaza.openclinica.dao.hibernate;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.domain.DomainObject;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.transaction.annotation.Transactional;


public abstract class AbstractDomainDao<T extends DomainObject> {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
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
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("id", id);
        return (T) q.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public ArrayList<T> findAll() {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        return (ArrayList<T>) q.list();
    }
    
    @SuppressWarnings("unchecked")
	public T findByOcOID(String OCOID){
    	 getSessionFactory().getStatistics().logSummary();
         String query = "from " + getDomainClassName() + " do  where do.oc_oid = :oc_oid";
         org.hibernate.query.Query q = getCurrentSession().createQuery(query);
         q.setParameter("oc_oid", OCOID);
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
        return getCurrentSession().save(domainObject);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public T findByColumnName(Object id, String key) {
        String query = "from " + getDomainClassName() + " do where do." + key + " = :key_value";
        org.hibernate.query.Query q = getCurrentSession().createQuery(query);
        q.setParameter("key_value", id);
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

    public Session getCurrentSession(String schema) {
        Session session = getSessionFactory().getCurrentSession();

        if (StringUtils.isNotEmpty(schema)) {
            SessionImpl sessionImpl = (SessionImpl) session;
            try {
                String currentSchema = sessionImpl.connection().getSchema();
                if (!schema.equals(currentSchema)) {
                    sessionImpl.connection().setSchema(schema);
                    CoreResources.tenantSchema.set(schema);
                    //CoreResources.setSchema(sessionImpl.connection());
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return session;
    }

    @SuppressWarnings("unused")
    public HibernateTemplate getHibernateTemplate() {
        return hibernateTemplate;
    }

    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

}
