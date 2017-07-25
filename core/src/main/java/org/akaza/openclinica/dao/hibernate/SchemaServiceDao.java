package org.akaza.openclinica.dao.hibernate;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.sql.SQLException;

/**
 * Created by yogi on 3/9/17.
 */
public class SchemaServiceDao {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private HibernateTemplate hibernateTemplate;
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

    public int getProtocolSchemaSeq () {
        Query query = getCurrentSession().createNativeQuery("select nextval('public.study_schema_id_seq')");
        BigInteger schemaId = (BigInteger) query.getSingleResult();
        return schemaId.intValue();
    }

    public void createProtocolSchema(String schemaName) throws Exception {
        // create the protocol schema
        Query schemaQuery = getCurrentSession().createNativeQuery("CREATE SCHEMA " + schemaName + " AUTHORIZATION clinica");
        schemaQuery.executeUpdate();
    }
    public void setConnectionSchemaName(String schemaName) throws Exception {
        ((SessionImpl) getCurrentSession()).connection().setSchema(schemaName);
    }
}
