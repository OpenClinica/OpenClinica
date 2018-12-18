package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.dao.core.CoreResources;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.HibernateTemplate;

import java.math.BigInteger;

/**
 * Created by yogi on 3/9/17.
 */
public class SchemaServiceDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private HibernateTemplate hibernateTemplate;
    public SessionFactory getSessionFactory() {
        return hibernateTemplate.getSessionFactory();
    }

    public Session getCurrentSession() {
        return getSessionFactory().getCurrentSession();
    }

    public HibernateTemplate getHibernateTemplate() {
        return hibernateTemplate;
    }

    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    public void createStudySchema(String schemaName) throws Exception {
        String dbUser = CoreResources.getField("dbUser");
        Query schemaQuery = getCurrentSession().createNativeQuery("CREATE SCHEMA " + schemaName + " AUTHORIZATION  " + dbUser);
        schemaQuery.executeUpdate();
    }
    public void setConnectionSchemaName(String schemaName) throws Exception {
        ((SessionImpl) getCurrentSession()).connection().setSchema(schemaName);
        CoreResources.setRequestSchema(schemaName);
        CoreResources.tenantSchema.set(schemaName);
    }
}
