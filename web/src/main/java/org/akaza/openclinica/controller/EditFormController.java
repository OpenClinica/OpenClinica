package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.EnketoAPI;
import org.akaza.openclinica.web.pform.EnketoCredentials;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

@Controller
@RequestMapping(value = "/api/v1/editform")
public class EditFormController {

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    private CrfVersionDao crfVersionDao;

    @Autowired
    private StudyEventDao studyEventDao;

    @Autowired
    private StudyEventDefinitionDao studyEventDefinitionDao;

    @Autowired
    private StudySubjectDao studySubjectDao;

    @Autowired
    private EventCrfDao eventCrfDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemDataDao itemDataDao;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO udao;
    StudyDAO sdao;

    @RequestMapping(value = "/{studyOid}/url", method = RequestMethod.GET)
    public ResponseEntity<String> getEditUrl(@RequestParam(FORM_CONTEXT) String formContext, @PathVariable("studyOid") String studyOID) throws Exception {

        String editURL = null;
        if (!mayProceed(studyOID))
            return new ResponseEntity<String>(editURL, org.springframework.http.HttpStatus.NOT_ACCEPTABLE);

        // Load context
        PFormCache cache = PFormCache.getInstance(context);
        HashMap<String, String> userContext = cache.getSubjectContext(formContext);

        // Lookup relevant data
        StudyEventDefinition eventDef = studyEventDefinitionDao.findById(Integer.valueOf(userContext.get("studyEventDefinitionID")));
        CrfVersion crfVersion = crfVersionDao.findByOcOID(userContext.get("crfVersionOID"));
        StudySubject subject = studySubjectDao.findByOcOID(userContext.get("studySubjectOID"));
        StudyEvent event = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(eventDef.getOc_oid(), Integer.valueOf(userContext.get("studyEventOrdinal")),
                subject.getStudySubjectId());
        EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdCrfVersionId(event.getStudyEventId(), subject.getStudySubjectId(),
                crfVersion.getCrfVersionId());

        // Load populated instance
        String populatedInstance = null;
        if (crfVersion.getXform() != null && !crfVersion.getXform().equals(""))
            populatedInstance = getPopulatedXformInstance(crfVersion, eventCrf);
        else
            populatedInstance = getPopulatedLegacyInstance();
        // String instance =
        // "<question_types_1><intro/><text_questions><my_string>AswcdewfcW</my_string></text_questions><number_questions><my_int>1</my_int><my_decimal>1.1</my_decimal></number_questions><select_questions><my_select>a</my_select><my_select1>1</my_select1></select_questions></question_types_1>";

        // Call Enketo api to get edit url
        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOID));

        // Return Enketo URL
        editURL = enketo.getEditURL(crfVersion.getOcOid(), populatedInstance).getEdit_url();
        System.out.println(editURL);

        return new ResponseEntity<String>(editURL, org.springframework.http.HttpStatus.ACCEPTED);

    }

    private String getPopulatedXformInstance(CrfVersion crfVersion, EventCrf eventCrf) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(crfVersion.getXform()));
        Document doc = db.parse(is);
        String itemName;

        NodeList instanceNodeList = doc.getElementsByTagName("instance");
        Node populatedCrfNode = null;
        // Instance loop
        for (int i = 0; i < instanceNodeList.getLength(); i = i + 1) {
            Node instanceNode = instanceNodeList.item(i);
            if (instanceNode instanceof Element) {
                NodeList crfNodeList = instanceNode.getChildNodes();
                // Form loop
                for (int j = 0; j < crfNodeList.getLength(); j = j + 1) {
                    Node crfNode = crfNodeList.item(j);
                    if (crfNode instanceof Element) {
                        populatedCrfNode = crfNode;
                        NodeList groupNodeList = crfNode.getChildNodes();
                        // Group loop
                        for (int k = 0; k < groupNodeList.getLength(); k = k + 1) {
                            Node groupNode = groupNodeList.item(k);

                            if (groupNode instanceof Element && !groupNode.getNodeName().startsWith("SECTION_")) {
                                NodeList itemNodeList = groupNode.getChildNodes();
                                // Item loop
                                for (int m = 0; m < itemNodeList.getLength(); m = m + 1) {
                                    Node itemNode = itemNodeList.item(m);
                                    if (itemNode instanceof Element && !itemNode.getNodeName().endsWith(".HEADER")
                                            && !itemNode.getNodeName().endsWith(".SUBHEADER")) {

                                        itemName = itemNode.getNodeName().trim();
                                        Item item = itemDao.findByNameCrfId(itemName, crfVersion.getCrf().getCrfId());
                                        ItemData itemData = null;
                                        if (item != null)
                                            itemData = itemDataDao.findByItemEventCrf(item.getItemId(), eventCrf.getEventCrfId());

                                        // Set Value here
                                        if (itemData != null && itemData.getValue() != null && !itemData.getValue().equals(""))
                                            itemNode.setTextContent(itemData.getValue());

                                    }
                                }
                            }
                        }
                    }

                }
            }
        }// End of instance loop
        return convertNodeToXMLString(populatedCrfNode);

    }

    private String convertNodeToXMLString(Node item) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(item);
        transformer.transform(source, result);

        String xmlInstance = result.getWriter().toString();
        System.out.println(xmlInstance);
        return xmlInstance;
    }

    private String getPopulatedLegacyInstance() {
        // TODO Auto-generated method stub
        return "<question_types_1><intro/><text_questions><my_string>AswcdewfcW</my_string></text_questions><number_questions><my_int>1</my_int><my_decimal>1.1</my_decimal></number_questions><select_questions><my_select>a</my_select><my_select1>1</my_select1></select_questions></question_types_1>";
    }

    private StudyBean getParentStudy(Integer studyId) {
        StudyBean study = getStudy(studyId);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }

    }

    private StudyBean getStudy(Integer id) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByPK(id);
        return studyBean;
    }

    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private String fetchEditUrl(String studyOID, CRFVersionBean crfVersion, int studyEventDefinitionId) throws Exception {
        StudyBean parentStudyBean = getParentStudy(studyOID);
        PFormCache cache = PFormCache.getInstance(context);
        String enketoURL = cache.getPFormURL(parentStudyBean.getOid(), crfVersion.getOid());
        String contextHash = cache.putAnonymousFormContext(studyOID, crfVersion.getOid(), studyEventDefinitionId);

        String url = enketoURL + "&" + FORM_CONTEXT + "=" + contextHash;
        logger.debug("Enketo URL for " + crfVersion.getName() + "= " + url);
        return url;

    }

    private boolean mayProceed(String studyOid) throws Exception {
        boolean accessPermission = false;
        StudyBean siteStudy = getStudy(studyOid);
        StudyBean study = getParentStudy(studyOid);
        StudyParameterValueDAO spvdao = new StudyParameterValueDAO(dataSource);
        StudyParameterValueBean pStatus = spvdao.findByHandleAndStudy(study.getId(), "participantPortal");
        participantPortalRegistrar = new ParticipantPortalRegistrar();
        String pManageStatus = participantPortalRegistrar.getRegistrationStatus(study.getOid()).toString(); // ACTIVE ,
                                                                                                            // PENDING ,
                                                                                                            // INACTIVE
        String participateStatus = pStatus.getValue().toString(); // enabled , disabled
        String studyStatus = study.getStatus().getName().toString(); // available , pending , frozen , locked
        String siteStatus = siteStudy.getStatus().getName().toString(); // available , pending , frozen , locked
        System.out.println("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus
                + "   siteStatus: " + siteStatus);
        logger.info("pManageStatus: " + pManageStatus + "  participantStatus: " + participateStatus + "   studyStatus: " + studyStatus + "   siteStatus: "
                + siteStatus);
        if (participateStatus.equalsIgnoreCase("enabled") && studyStatus.equalsIgnoreCase("available") && siteStatus.equalsIgnoreCase("available")
                && pManageStatus.equalsIgnoreCase("ACTIVE")) {
            accessPermission = true;
        }

        return accessPermission;
    }

}
