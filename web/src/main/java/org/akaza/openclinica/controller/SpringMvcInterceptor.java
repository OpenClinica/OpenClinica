package org.akaza.openclinica.controller;

import org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.akaza.openclinica.dao.hibernate.multitenant.CurrentTenantIdentifierResolverImpl.CURRENT_TENANT_ID;

/**
 * Created by yogi on 1/31/17.
 */
public class SpringMvcInterceptor extends HandlerInterceptorAdapter {
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
        System.out.println("request parameter:" + request.getParameter("studyOid"));
        System.out.println("request parameter:" + request.getParameter("studyOID"));
        return true;
    }
}
