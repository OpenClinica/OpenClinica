package org.akaza.openclinica.service;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.controller.openrosa.processor.QueryServiceHelperBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.OCMultiTenantSpringLiquibase;
import org.akaza.openclinica.core.form.xform.QueriesBean;
import org.akaza.openclinica.core.form.xform.QueryBean;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.web.SQLInitServlet;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.*;

import static org.akaza.openclinica.control.core.SecureController.respage;

/**
 * Created by yogi on 11/10/16.
 */
@Service("protocolBuildService")
@Transactional(propagation= Propagation.REQUIRED,isolation= Isolation.DEFAULT)
public class ProtocolBuildServiceImpl implements ProtocolBuildService {

    @Autowired
    private StudyDao studyDao;
    @Autowired
    private ApplicationContext context;

    public String process(String name, String uniqueId, String ocId, HttpServletRequest request,
            HttpServletResponse response) {
        Session session = studyDao.getSessionFactory().getCurrentSession();
        String schemaName = null;
        try {


            Query query = session.createNativeQuery("select nextval('public.study_schema_id_seq')");
            BigInteger schemaId = (BigInteger) query.getSingleResult();
            Study study = new Study();
            study.setName(name);
            study.setUniqueIdentifier(uniqueId);
            study.setOc_oid(ocId);
            schemaName = "tenant" + schemaId;
            study.setSchemaName(schemaName);
            studyDao.save(study);
        } catch (Exception e) {
            System.out.println("Error while creating a schema error 1");
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
        createSchema(response, session, schemaName);
        return schemaName;
    }

    private boolean createSchema(HttpServletResponse response, Session session, String schemaName) {
        try {
            // create the schema
            Query schemaQuery = session.createNativeQuery("CREATE SCHEMA " + schemaName + " AUTHORIZATION clinica");
            schemaQuery.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while creating a schema error 2");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
        return true;
    }
}