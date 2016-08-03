/*
- * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 20032009 Akaza Research
 */
package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.service.subject.SubjectServiceInterface;
import org.openclinica.ws.crf.v1.CreateCrfResponse;
import org.openclinica.ws.crf.v1.CrfType;
import org.openclinica.ws.crf.v1.ObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @author Krikor Krumlian
 * 
 */
@Endpoint
public class CrfEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/crf/v1";
    private final String SUCCESS_MESSAGE = "success";
    private final String FAIL_MESSAGE = "fail";
    private String dateFormat;
    private Properties dataInfo;

    private final SubjectServiceInterface subjectService;
    private final DataSource dataSource;
    private final ObjectFactory objectFactory;

    /**
     * Constructor
     * 
     * @param subjectService
     * @param cctsService
     */
    public CrfEndpoint(SubjectServiceInterface subjectService, DataSource dataSource) {
        this.subjectService = subjectService;
        this.dataSource = dataSource;
        this.objectFactory = new ObjectFactory();
    }

    @PayloadRoot(localPart = "createCrfRequest", namespace = NAMESPACE_URI_V1)
    public CreateCrfResponse store(JAXBElement<CrfType> requestElement) throws Exception {

        CrfType crf = requestElement.getValue();
        String filePathWithName = getDataInfo().getProperty("filePath") + "crf/original/" + crf.getFileName();

        try {
            FileOutputStream fstream = new FileOutputStream(filePathWithName);
            crf.getFile().writeTo(fstream);
            fstream.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
        CreateCrfResponse crfResponse = new CreateCrfResponse();
        crfResponse.setResult(SUCCESS_MESSAGE);
        crfResponse.setKey("test");
        return crfResponse;
    }

    /**
     * Create Response
     * 
     * @param confirmation
     * @return
     * @throws Exception
     */
    private Element mapConfirmation(String confirmation) throws Exception {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document document = docBuilder.newDocument();

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "commitResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);
        return responseElement;

    }

    /**
     * Helper Method to resolve dates
     * 
     * @param dateAsString
     * @return
     * @throws ParseException
     */
    private Date getDate(String dateAsString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(getDateFormat());
        return sdf.parse(dateAsString);
    }

    /**
     * Helper Method to get the user account
     * 
     * @return UserAccountBean
     */
    private UserAccountBean getUserAccount() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = null;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        UserAccountDAO userAccountDao = new UserAccountDAO(dataSource);
        return (UserAccountBean) userAccountDao.findByUserName(username);
    }

    /**
     * @return
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * @param dateFormat
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Properties getDataInfo() {
        return dataInfo;
    }

    public void setDataInfo(Properties dataInfo) {
        this.dataInfo = dataInfo;
    }
}