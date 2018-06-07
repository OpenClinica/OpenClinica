package org.akaza.openclinica.dao.hibernate;

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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;

public abstract class AbstractDomainDao<T extends DomainObject> {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private HibernateTemplate hibernateTemplate;

    abstract Class<T> domainClass();

    public String getDomainClassName() {
        return domainClass().getName();
    }

    @SuppressWarnings("unchecked") @Transactional public T findById(Integer id) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.id = :id";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setInteger("id", id);
        return (T) q.uniqueResult();
    }

    @SuppressWarnings("unchecked") @Transactional public ArrayList<T> findAll() {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        return (ArrayList<T>) q.list();
    }

    @SuppressWarnings("unchecked") public T findByOcOID(String OCOID) {
        getSessionFactory().getStatistics().logSummary();
        String query = "from " + getDomainClassName() + " do  where do.oc_oid = :oc_oid";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setString("oc_oid", OCOID);
        return (T) q.uniqueResult();
    }

    @Transactional public T saveOrUpdate(T domainObject) {
        getSessionFactory().getStatistics().logSummary();
        getCurrentSession().saveOrUpdate(domainObject);
        return domainObject;
    }

    @Transactional public Serializable save(T domainObject) {
        getSessionFactory().getStatistics().logSummary();
        Serializable id = getCurrentSession().save(domainObject);
        return id;
    }

    @Transactional public T findByColumnName(Object id, String key) {
        String query = "from " + getDomainClassName() + " do where do." + key + "= ?";
        org.hibernate.Query q = getCurrentSession().createQuery(query);
        q.setParameter(0, id);
        return (T) q.uniqueResult();
    }

    public Long count() {
        return (Long) getCurrentSession().createQuery("select count(*) from " + domainClass().getName()).uniqueResult();
    }

    public SessionFactory getSessionFactory() {
        return hibernateTemplate.getSessionFactory();
    }

    public Session getCurrentSession() {
        Session session = null;
        String tenant = null;
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null && requestAttributes.getRequest() != null) {
            HttpServletRequest request = requestAttributes.getRequest();
            tenant = (String) request.getAttribute("requestSchema");
        } else {
            tenant = CoreResources.tenantSchema.get();
        }
        session = getSessionFactory().getCurrentSession();
        SessionImpl sessionImpl = (SessionImpl) session;

        if (StringUtils.isNotEmpty(tenant)) {
            try {
                String currentSchema = sessionImpl.connection().getSchema();
                if (!tenant.equals(currentSchema)) {
                    sessionImpl.connection().setSchema(tenant);
                }
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            String schema = CoreResources.tenantSchema.get();
            if (StringUtils.isNotEmpty(schema)) {
                try {
                    sessionImpl.connection().setSchema(schema);
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            }

        }

        return session;
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
                logger.error(e.getMessage(), e);            }
        }
        return session;
    }

    public HibernateTemplate getHibernateTemplate() {
        return hibernateTemplate;
    }

    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

}
