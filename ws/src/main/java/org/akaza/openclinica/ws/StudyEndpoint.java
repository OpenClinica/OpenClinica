package org.akaza.openclinica.ws;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

@Endpoint
public class StudyEndpoint {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String NAMESPACE_URI_V1 = "http://openclinica.org/ws/study/v1";
    private final String SUCCESS_MESSAGE = "success";
    private final String FAIL_MESSAGE = "fail";

    private final DataSource dataSource;
    StudyDAO studyDao;
    UserAccountDAO userAccountDao;
    private final MessageSource messages;
    private final Locale locale;

    public StudyEndpoint(DataSource dataSource, MessageSource messages) {
        this.dataSource = dataSource;
        this.messages = messages;
        this.locale = new Locale("en_US");
    }

    /**
     * if NAMESPACE_URI_V1:getStudyListRequest execute this method
     * 
     * @return
     * @throws Exception
     */
    @PayloadRoot(localPart = "listAllRequest", namespace = NAMESPACE_URI_V1)
    public Source getStudyList() throws Exception {
        ResourceBundleProvider.updateLocale(new Locale("en_US"));
        return new DOMSource(mapConfirmation(messages.getMessage("studyEndpoint.success", null, "Success", locale)));
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
        return (UserAccountBean) getUserAccountDao().findByUserName(username);
    }

    private HashMap<Integer, ArrayList<StudyBean>> getStudies() {

        ArrayList<StudyUserRoleBean> studyUserRoleBeans = getUserAccountDao().findStudyByUser(getUserAccount().getName(), (ArrayList) getStudyDao().findAll());

        HashMap<Integer, ArrayList<StudyBean>> validStudySiteMap = new HashMap<Integer, ArrayList<StudyBean>>();
        for (int i = 0; i < studyUserRoleBeans.size(); i++) {
            StudyUserRoleBean sr = studyUserRoleBeans.get(i);
            StudyBean study = (StudyBean) studyDao.findByPK(sr.getStudyId());
            if (study != null && study.getStatus().equals(Status.PENDING)) {
                sr.setStatus(study.getStatus());
            }
            if (study.isSite(study.getParentStudyId()) && !sr.isInvalid()) {
                if (validStudySiteMap.get(study.getParentStudyId()) == null) {
                    ArrayList<StudyBean> sites = new ArrayList<StudyBean>();
                    sites.add(study);
                    validStudySiteMap.put(study.getParentStudyId(), sites);
                } else {
                    validStudySiteMap.get(study.getParentStudyId()).add(study);
                }
            } else if (!study.isSite(study.getParentStudyId())) {
                if (validStudySiteMap.get(study.getId()) == null) {
                    ArrayList<StudyBean> sites = new ArrayList<StudyBean>();
                    validStudySiteMap.put(study.getId(), sites);
                }
            }
        }
        return validStudySiteMap;
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

        Element responseElement = document.createElementNS(NAMESPACE_URI_V1, "listAllResponse");
        Element resultElement = document.createElementNS(NAMESPACE_URI_V1, "result");
        resultElement.setTextContent(confirmation);
        responseElement.appendChild(resultElement);

        Element studyListElement = document.createElementNS(NAMESPACE_URI_V1, "studies");
        responseElement.appendChild(studyListElement);

        for (Map.Entry<Integer, ArrayList<StudyBean>> entry : getStudies().entrySet()) {
            StudyBean study = (StudyBean) getStudyDao().findByPK(entry.getKey());
            studyListElement.appendChild(createStudyWithSiteElement(document, study, entry.getValue()));
        }

        return responseElement;
    }

    private Element createStudyWithSiteElement(Document document, StudyBean study, ArrayList<StudyBean> sites) {

        Element studyElement = createStudyElement(document, "study", study);
        if (sites.size() > 0) {
            Element siteListElement = document.createElementNS(NAMESPACE_URI_V1, "sites");
            studyElement.appendChild(siteListElement);
            for (StudyBean siteBean : sites) {
                Element siteElement = createStudyElement(document, "site", siteBean);
                siteListElement.appendChild(siteElement);
            }
        }
        return studyElement;
    }

    private Element createStudyElement(Document document, String studyOrSite, StudyBean study) {

        Element studyElement = document.createElementNS(NAMESPACE_URI_V1, studyOrSite);

        Element element = document.createElementNS(NAMESPACE_URI_V1, "identifier");
        element.setTextContent(study.getIdentifier() + "");
        studyElement.appendChild(element);

        element = document.createElementNS(NAMESPACE_URI_V1, "oid");
        element.setTextContent(study.getOid());
        studyElement.appendChild(element);

        element = document.createElementNS(NAMESPACE_URI_V1, "name");
        element.setTextContent(study.getName());
        studyElement.appendChild(element);

        return studyElement;

    }

    public StudyDAO getStudyDao() {
        studyDao = studyDao != null ? studyDao : new StudyDAO(dataSource);
        return studyDao;
    }

    public UserAccountDAO getUserAccountDao() {
        userAccountDao = userAccountDao != null ? userAccountDao : new UserAccountDAO(dataSource);
        return userAccountDao;
    }

}
