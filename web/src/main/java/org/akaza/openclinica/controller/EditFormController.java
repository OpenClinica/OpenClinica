package org.akaza.openclinica.controller;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.akaza.openclinica.web.pform.EnketoAPI;
import org.akaza.openclinica.web.pform.EnketoCredentials;
import org.akaza.openclinica.web.pform.PFormCache;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
    private SectionDao sectionDao;

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
    private ItemGroupDao itemGroupDao;

    @Autowired
    private ItemGroupMetadataDao itemGroupMetadataDao;

    @Autowired
    private ItemFormMetadataDao itemFormMetadataDao;

    @Autowired
    private ResponseTypeDao responseTypeDao;

    @Autowired
    private ItemDataDao itemDataDao;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO udao;
    StudyDAO sdao;

    /**
     * @api {get} /pages/api/v1/editform/:studyOid/url Get Form Edit URL
     * @apiName getEditUrl
     * @apiPermission admin
     * @apiVersion 3.8.0
     * @apiParam {String} studyOid Study Oid.
     * @apiParam {String} ecid Key that will be used by enketo to cache form information.
     * @apiGroup Form
     * @apiDescription This API is used to retrieve a URL for a form with data pre-loaded into it
     * @apiParamExample {json} Request-Example:
     *                  {
     *                  "studyOid": "S_BL101",
     *                  "ecid":"a9f8f3aadea4b67e1f214140ccfdf70bad0b9e9b622a9776a3c85bbf6bb532cd"
     *                  }
     * @apiSuccessExample Success-Response:
     *                    HTTP/1.1 200 OK
     *                    {
     *                    http://ocform.oc.com:8005/edit/::YYYM?instance_id=
     *                    d16bba9200177fad34594e75d8b9565ff92b0bce4297e3b6c27275e531044a59
     *                    &returnUrl=http%3A%2F%2Fstudy1.mystudy.me%3A8080%2F%23%2Fevent%2FSS_SUB001%2Fdashboard&ecid=
     *                    d16bba9200177fad34594e75d8b9565ff92b0bce4297e3b6c27275e531044a59
     *                    }
     */

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
        String populatedInstance = getPopulatedInstance(crfVersion, eventCrf);

        // Call Enketo api to get edit url
        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOID));

        // Build redirect url
        String redirectUrl = getRedirectUrl(subject.getOcOid(), studyOID);

        // Return Enketo URL
        editURL = enketo.getEditURL(crfVersion.getOcOid(), populatedInstance, formContext, redirectUrl).getEdit_url() + "&ecid=" + formContext;
        logger.debug("Generating Enketo edit url for form: " + editURL);

        return new ResponseEntity<String>(editURL, org.springframework.http.HttpStatus.ACCEPTED);

    }

    private String getRedirectUrl(String studySubjectOid, String studyOid) {
        String portalURL = CoreResources.getField("portalURL");
        String url = "";
        if (portalURL != null && !portalURL.equals("")) {
            ParticipantPortalRegistrar registrar = new ParticipantPortalRegistrar();
            Authorization pManageAuthorization = registrar.getAuthorization(studyOid);
            try {
                URL pManageUrl = new URL(portalURL);

                if (pManageAuthorization != null && pManageAuthorization.getStudy() != null && pManageAuthorization.getStudy().getHost() != null
                        && !pManageAuthorization.getStudy().getHost().equals("")) {
                    url = pManageUrl.getProtocol() + "://" + pManageAuthorization.getStudy().getHost() + "." + pManageUrl.getHost()
                            + ((pManageUrl.getPort() > 0) ? ":" + String.valueOf(pManageUrl.getPort()) : "");
                }
            } catch (MalformedURLException e) {
                logger.error("Error building redirect URL: " + e.getMessage());
                logger.error(ExceptionUtils.getStackTrace(e));
                return "";
            }
        }
        if (!url.equals(""))
            url = url + "/#/event/" + studySubjectOid + "/dashboard";
        return url;
    }

    private String getPopulatedInstance(CrfVersion crfVersion, EventCrf eventCrf) throws Exception {
        boolean isXform = false;
        if (crfVersion.getXform() != null && !crfVersion.getXform().equals(""))
            isXform = true;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder build = docFactory.newDocumentBuilder();
        Document doc = build.newDocument();

        Element crfElement = null;
        if (isXform)
            crfElement = doc.createElement(crfVersion.getXformName());
        else
            crfElement = doc.createElement(crfVersion.getOcOid());
        doc.appendChild(crfElement);

        ArrayList<ItemGroup> itemGroups = itemGroupDao.findByCrfVersionId(crfVersion.getCrfVersionId());
        for (ItemGroup itemGroup : itemGroups) {
            ItemGroupMetadata itemGroupMetadata = itemGroupMetadataDao.findByItemGroupCrfVersion(itemGroup.getItemGroupId(), crfVersion.getCrfVersionId()).get(
                    0);
            ArrayList<Item> items = (ArrayList<Item>) itemDao.findByItemGroupCrfVersionOrdered(itemGroup.getItemGroupId(), crfVersion.getCrfVersionId());

            // Get max repeat in item data
            int maxGroupRepeat = itemDataDao.getMaxGroupRepeat(eventCrf.getEventCrfId(), items.get(0).getItemId());
            // loop thru each repeat creating items in instance
            String repeatGroupMin = itemGroupMetadata.getRepeatNumber().toString();
            Boolean isrepeating = itemGroupMetadata.isRepeatingGroup();

            // TODO: Test empty group here (no items). make sure doesn't get nullpointer exception
            for (int i = 0; i < maxGroupRepeat; i++) {
                Element groupElement = null;

                if (isXform)
                    groupElement = doc.createElement(itemGroup.getName());
                else
                    groupElement = doc.createElement(itemGroup.getOcOid());
                Element repeatOrdinal = null;
                if (isrepeating) {
                	repeatOrdinal = doc.createElement("OC.REPEAT_ORDINAL");
                	repeatOrdinal.setTextContent(String.valueOf(i+1));
                	groupElement.appendChild(repeatOrdinal);
                }
                boolean hasItemData = false;
                for (Item item : items) {
                    ItemFormMetadata itemMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                    ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), i + 1);

                    Element question = null;
                    if (crfVersion.getXform() != null && !crfVersion.getXform().equals(""))
                        question = doc.createElement(item.getName());
                    else
                        question = doc.createElement(item.getOcOid());

                    if (itemData != null && itemData.getValue() != null && !itemData.getValue().equals("")) {
                        ResponseType responseType = responseTypeDao.findByItemFormMetaDataId(itemMetadata.getItemFormMetadataId());
                        String itemValue = itemData.getValue();
                        if (responseType.getResponseTypeId() == 3 || responseType.getResponseTypeId() == 7) {
                            itemValue = itemValue.replaceAll(",", " ");
                        }

                        question.setTextContent(itemValue);
                    }
                    if (itemData==null || !itemData.isDeleted()) { 
                    	hasItemData = true; 
                    	groupElement.appendChild(question);
                    }
                } // end of item
                if (hasItemData) {
                	crfElement.appendChild(groupElement);
                }
            }

        } // end of group

        TransformerFactory transformFactory = TransformerFactory.newInstance();
        Transformer transformer = transformFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        transformer.transform(source, result);
        String instance = writer.toString();
        System.out.println("Editable instance = " + instance);
        return instance;
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