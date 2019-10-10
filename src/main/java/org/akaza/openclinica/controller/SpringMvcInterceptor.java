package org.akaza.openclinica.controller;

import core.org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static core.org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

/**
 * Created by yogi on 1/31/17.
 */
public class SpringMvcInterceptor extends HandlerInterceptorAdapter {
    private final static Logger logger = LoggerFactory.getLogger(SpringMvcInterceptor.class);
    @Autowired HibernateTemplate hibernateTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler){
        final SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor)hibernateTemplate.getSessionFactory();
        CurrentTenantIdentifierResolverImpl tenantIdentifierResolver = (CurrentTenantIdentifierResolverImpl) sessionFactory
                .getCurrentTenantIdentifierResolver();
        if (request.getParameter("studyOID") != null) {
            HttpSession session = request.getSession();
            if (session != null) {
                session.setAttribute(CURRENT_TENANT_ID, request.getParameter("studyOID"));
            }
        }
        logger.debug("request parameter:{}",request.getParameter("studyOid"));
        logger.debug("request parameter:{}",request.getParameter("studyOID"));
        return true;
    }
}
