package org.akaza.openclinica.service.crfdata;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.core.form.xform.LogBean;
import org.akaza.openclinica.core.form.xform.QueriesBean;
import org.akaza.openclinica.core.form.xform.QueryBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.DiscrepancyNoteDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.ResponseTypeDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.akaza.openclinica.domain.datamap.DnItemDataMap;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.ResponseType;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.xform.XformParserHelper;
import org.akaza.openclinica.service.crfdata.xform.EnketoAPI;
import org.akaza.openclinica.service.crfdata.xform.EnketoCredentials;
import org.akaza.openclinica.service.crfdata.xform.EnketoURLResponse;
import org.akaza.openclinica.service.crfdata.xform.PFormCacheSubjectContextEntry;
import org.akaza.openclinica.service.pmanage.Authorization;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class EnketoUrlService {

    public static final String ENKETO_ORDINAL = "enk:ordinal";
    public static final String ENKETO_LAST_USED_ORDINAL = "enk:last-used-ordinal";
    public static final String FS_QUERY_ATTRIBUTE = "oc:queryParent";
    public static final String OC_QUERY_SUFFIX = "_OC_COMMENT";
    public static final String QUERY_SUFFIX = "_comment";
    public static final String INSTANCE_SUFFIX = "instance-queries.tpl";
    public static final String FORM_SUFFIX = "form.xml";

    @Autowired
    @Qualifier("dataSource")
    private BasicDataSource dataSource;

    @Autowired
    ServletContext context;

    @Autowired
    private CrfVersionDao crfVersionDao;

    @Autowired
    private FormLayoutDao formLayoutDao;

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
    private CrfDao crfDao;

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

    @Autowired
    private DiscrepancyNoteDao discrepancyNoteDao;

    @Autowired
    private XformParserHelper xformParserHelper;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO udao;
    StudyDAO sdao;

    public String getInitialDataEntryUrl(String subjectContextKey, PFormCacheSubjectContextEntry subjectContext, String studyOid, String queryFlavor)
            throws Exception {
        // Call Enketo api to get edit url
        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOid));
        return enketo.getFormURL(subjectContext.getFormLayoutOid() + queryFlavor) + "?ecid=" + subjectContextKey;

    }

    public String getEditUrl(String subjectContextKey, PFormCacheSubjectContextEntry subjectContext, String studyOid, FormLayout formLayout,
            StudyEvent studyEvent, String queryFlavor) throws Exception {

        String editURL = null;
        StudyEventDefinition eventDef;
        StudySubject subject;

        if (studyEvent == null) {
            // Lookup relevant data
            eventDef = studyEventDefinitionDao.findByStudyEventDefinitionId(subjectContext.getStudyEventDefinitionId());
            subject = studySubjectDao.findByOcOID(subjectContext.getStudySubjectOid());
            studyEvent = studyEventDao.fetchByStudyEventDefOIDAndOrdinal(eventDef.getOc_oid(), Integer.valueOf(subjectContext.getOrdinal()),
                    subject.getStudySubjectId());

        } else {
            eventDef = studyEvent.getStudyEventDefinition();
            subject = studyEvent.getStudySubject();
        }
        if (formLayout == null) {
            formLayout = formLayoutDao.findByOcOID(subjectContext.getFormLayoutOid());
        }
        EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEvent.getStudyEventId(), subject.getStudySubjectId(),
                formLayout.getFormLayoutId());

        CrfVersion crfVersion = eventCrf.getCrfVersion();
        // Load populated instance
        String populatedInstance = populateInstance(crfVersion, formLayout, eventCrf, studyOid);

        // Call Enketo api to get edit url
        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOid));

        // Build redirect url
        String redirectUrl = getRedirectUrl(subject.getOcOid(), studyOid);

        boolean markComplete = true;
        if (eventCrf.getStatusId() == Status.UNAVAILABLE.getCode()) {
            markComplete = false;
        }
        // Return Enketo URL
        EnketoURLResponse eur = enketo.getEditURL(formLayout.getOcOid() + queryFlavor, populatedInstance, subjectContextKey, redirectUrl, markComplete);
        editURL = eur.getEdit_url() + "&ecid=" + subjectContextKey;
        logger.debug("Generating Enketo edit url for form: " + editURL);

        return editURL;

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

    public QueriesBean buildQueryElement(ItemData itemdata) {
        QueriesBean queryElement = new QueriesBean();
        List<QueryBean> queryBeans = new ArrayList<>();
        List<LogBean> logs = new ArrayList<LogBean>();
        // LogBean logBean = new LogBean();
        List<DiscrepancyNote> dns = discrepancyNoteDao.findChildQueriesByItemData(itemdata.getItemDataId());
        int i = 0;
        for (DiscrepancyNote dn : dns) {
            i++;
            QueryBean query = new QueryBean();
            query.setId(String.valueOf(i));
            if (dn.getUserAccount() != null) {
                query.setAssigned_to(dn.getUserAccount().getUserName());
            }
            query.setComment(dn.getDetailedNotes());
            query.setStatus(dn.getResolutionStatus().getName().toLowerCase());
            query.setDate_time(dn.getDateCreated().toString());
            query.setNotify(false);
            queryBeans.add(query);
        }
        // logs.add(logBean);
        queryElement.setQueries(queryBeans);
        queryElement.setLogs(logs);
        if (dns.size() != 0)
            return queryElement;
        else
            return null;
    }

    private String populateInstance(CrfVersion crfVersion, FormLayout formLayout, EventCrf eventCrf, String studyOid) throws Exception {
        boolean flavor = true;

        Map<String, Object> data = new HashMap<String, Object>();

        List<ItemGroup> igs = itemGroupDao.findByCrfVersionId(crfVersion.getCrfVersionId());

        for (ItemGroup ig : igs) {
            List<HashMap<String, Object>> hashMapList = new ArrayList<HashMap<String, Object>>();
            List<ItemGroupMetadata> igms = itemGroupMetadataDao.findByItemGroupCrfVersion(ig.getItemGroupId(), crfVersion.getCrfVersionId());
            int maxRowCount = itemDataDao.getMaxCountByEventCrfGroup(eventCrf.getEventCrfId(), ig.getItemGroupId());
            HashMap<String, Object> hashMap = null;

            if (igms.get(0).isRepeatingGroup() && maxRowCount == 0) {
                hashMap = new HashMap<>();
                hashMap.put("index", 1);
                hashMap.put("lastUsedOrdinal", 1);
                for (ItemGroupMetadata igm : igms) {
                    hashMap.put(igm.getItem().getName(), "");
                    if (flavor)
                        hashMap.put(igm.getItem().getName() + QUERY_SUFFIX, "");
                }
                hashMapList.add(hashMap);
                data.put(ig.getName(), hashMapList);
            }
            boolean rowDeleted = false;
            if (igms.get(0).isRepeatingGroup()) {
                for (int i = 0; i < maxRowCount; i++) {
                    rowDeleted = false;
                    for (ItemGroupMetadata igm : igms) {
                        ItemData itemData = itemDataDao.findByItemEventCrfOrdinalDeleted(igm.getItem().getItemId(), eventCrf.getEventCrfId(), i + 1);
                        if (itemData != null) {
                            rowDeleted = true;
                            break;
                        }
                    }

                    if (!rowDeleted) {
                        hashMap = new HashMap<>();
                        hashMap.put("index", i + 1);
                        if (i == 0) {
                            hashMap.put("lastUsedOrdinal", maxRowCount);
                        }
                        for (ItemGroupMetadata igm : igms) {
                            ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(igm.getItem().getItemId(), eventCrf.getEventCrfId(), i + 1);
                            hashMap.put(igm.getItem().getName(), itemData != null ? itemData.getValue() : "");
                            if (flavor) {
                                if (itemData != null) {
                                    ObjectMapper mapper = new ObjectMapper();
                                    QueriesBean queriesBean = buildQueryElement(itemData);
                                    hashMap.put(igm.getItem().getName() + QUERY_SUFFIX, queriesBean != null ? mapper.writeValueAsString(queriesBean) : "");
                                } else {
                                    hashMap.put(igm.getItem().getName() + QUERY_SUFFIX, "");
                                }
                            }
                        }
                        hashMapList.add(hashMap);
                    }
                }
            }

            if (igms.get(0).isRepeatingGroup() && maxRowCount != 0) {
                data.put(ig.getName(), hashMapList);
            }

            if (!igms.get(0).isRepeatingGroup()) {
                for (ItemGroupMetadata igm : igms) {
                    ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(igm.getItem().getItemId(), eventCrf.getEventCrfId(), 1);
                    data.put(igm.getItem().getName(), itemData != null ? itemData.getValue() : "");
                    if (flavor) {
                        if (itemData != null) {
                            ObjectMapper mapper = new ObjectMapper();
                            QueriesBean queriesBean = buildQueryElement(itemData);
                            data.put(igm.getItem().getName() + QUERY_SUFFIX, queriesBean != null ? mapper.writeValueAsString(queriesBean) : "");
                        } else {
                            data.put(igm.getItem().getName() + QUERY_SUFFIX, "");
                        }
                    }
                }
            }
        }
        String templateStr = null;
        CrfBean crfBean = crfDao.findById(formLayout.getCrf().getCrfId());
        String directoryPath = Utils.getCrfMediaFilePath(crfBean.getOcOid(), formLayout.getOcOid());
        File dir = new File(directoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().endsWith(INSTANCE_SUFFIX)) {
                    templateStr = new String(Files.readAllBytes(Paths.get(child.getPath())));
                    break;
                }
            }
        }

        Template template = new Template("template name", new StringReader(templateStr), new Configuration());

        StringWriter wtr = new StringWriter();
        template.process(data, wtr);

        String instance = wtr.toString();
        System.out.println(instance);
        return instance;
    }

    private String getPopulatedInstance(CrfVersion crfVersion, EventCrf eventCrf) throws Exception {
        boolean isXform = false;
        if (StringUtils.isNotEmpty(crfVersion.getXform()))
            isXform = true;

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        DocumentBuilder build = docFactory.newDocumentBuilder();
        Document doc = build.newDocument();

        Element crfElement = null;
        if (isXform) {
            crfElement = doc.createElement(crfVersion.getXformName());
            crfElement.setAttribute("xmlns:enk", "http://enketo.org/xforms");
        } else {
            crfElement = doc.createElement(crfVersion.getOcOid());
        }
        doc.appendChild(crfElement);

        ArrayList<ItemGroup> itemGroups = itemGroupDao.findByCrfVersionId(crfVersion.getCrfVersionId());
        for (ItemGroup itemGroup : itemGroups) {
            ItemGroupMetadata itemGroupMetadata = itemGroupMetadataDao.findByItemGroupCrfVersion(itemGroup.getItemGroupId(), crfVersion.getCrfVersionId())
                    .get(0);
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
                    repeatOrdinal.setTextContent(String.valueOf(i + 1));
                    groupElement.appendChild(repeatOrdinal);
                    // add enketo related attributes
                    groupElement.setAttribute(ENKETO_ORDINAL, String.valueOf(i + 1));
                    groupElement.setAttribute(ENKETO_LAST_USED_ORDINAL, String.valueOf(maxGroupRepeat));

                    repeatOrdinal = doc.createElement("OC.REPEAT_ORDINAL");
                    repeatOrdinal.setTextContent(String.valueOf(i + 1));
                    groupElement.appendChild(repeatOrdinal);
                }
                boolean hasItemData = false;
                for (Item item : items) {
                    // ItemFormMetadata itemMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(),
                    // crfVersion.getCrfVersionId());
                    ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), i + 1);
                    ItemFormMetadata itemMetadata = item.getItemFormMetadatas().iterator().next();// itemFormMetadataDao.findByItemCrfVersion(item.getItemId(),
                                                                                                  // crfVersion.getCrfVersionId());
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
                    if (itemData == null || !itemData.isDeleted()) {
                        hasItemData = true;
                        groupElement.appendChild(question);
                        // add the corresponding query element
                        if (isXform) {
                            processQueryElement(doc, item.getName(), itemData, groupElement);
                        }
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
        System.out.println(instance);
        return instance;
    }

    private void processQueryElement(Document doc, String itemName, ItemData itemData, Element groupElement) throws JsonProcessingException {
        if (itemData == null)
            return;

        List<DnItemDataMap> dnItemDataMaps = itemData.getDnItemDataMaps();
        if (CollectionUtils.isEmpty(dnItemDataMaps)) {
            return;
        }
        dnItemDataMaps.sort((d1, d2) -> d2.getDiscrepancyNote().getDateCreated().compareTo(d1.getDiscrepancyNote().getDateCreated()));

        QueriesBean queriesBean = new QueriesBean();
        // create a json query list
        ListIterator<DnItemDataMap> queryIterator = dnItemDataMaps.listIterator();
        while (queryIterator.hasNext()) {
            DnItemDataMap dnItemDataMap = queryIterator.next();
            DiscrepancyNote discrepancyNote = dnItemDataMap.getDiscrepancyNote();
            if (StringUtils.isNotEmpty(discrepancyNote.getDetailedNotes())) {
                QueryBean queryBean = new QueryBean();
                queryBean.setComment(discrepancyNote.getDetailedNotes());
                UserAccount owner = discrepancyNote.getUserAccountByOwnerId();
                String queryCreator = owner.getFirstName() + " " + owner.getLastName() + " (" + owner.getUserName() + ")";
                queryBean.setUser(queryCreator);
                UserAccount userAccount = discrepancyNote.getUserAccount();
                String assignedTo = userAccount.getFirstName() + " " + userAccount.getLastName() + " (" + userAccount.getUserName() + ")";
                queryBean.setAssigned_to(assignedTo);
                queryBean.setDate_time(discrepancyNote.getDateCreated().toString());
                queriesBean.getQueries().add(queryBean);
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        String jsonQueries = mapper.writeValueAsString(queriesBean);
        Element queryElement = doc.createElement(itemName + OC_QUERY_SUFFIX);
        queryElement.setTextContent(jsonQueries);
        groupElement.appendChild(queryElement);
    }

    private List<Node> appendRepeatGroupElements(List<Node> target, List<ItemData> itemDatas, Node groupNode, CrfVersion crfVersion, EventCrf eventCrf,
            Item item, Document doc) throws JsonProcessingException {

        // Get max repeat in item data
        int maxGroupRepeat = itemDataDao.getMaxGroupRepeat(eventCrf.getEventCrfId(), item.getItemId());
        boolean hasItemData = false;
        int i = 0;
        for (ItemData itemData : itemDatas) {
            // for (int i = 0; i < maxGroupRepeat; i++) {
            Element groupElement = null;

            groupElement = doc.createElement(groupNode.getNodeName());
            // Element repeatOrdinal = null;
            // repeatOrdinal = doc.createElement("OC.REPEAT_ORDINAL");
            // repeatOrdinal.setTextContent(String.valueOf(i + 1));
            // groupElement.appendChild(repeatOrdinal);
            // add enketo related attributes
            groupElement.setAttribute(ENKETO_ORDINAL, String.valueOf(i + 1));
            groupElement.setAttribute(ENKETO_LAST_USED_ORDINAL, String.valueOf(maxGroupRepeat));
            // groupElement.appendChild(repeatOrdinal);

            if (itemDatas.size() == 0) {
                hasItemData = false;
            }
            Element question = null;
            question = doc.createElement(item.getName());
            if (itemData != null && itemData.getValue() != null && !itemData.getValue().equals("")) {
                ItemFormMetadata itemMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersion.getCrfVersionId());
                ResponseType responseType = responseTypeDao.findByItemFormMetaDataId(itemMetadata.getItemFormMetadataId());
                String itemValue = itemData.getValue();
                if (responseType.getResponseTypeId() == 3 || responseType.getResponseTypeId() == 7) {
                    itemValue = itemValue.replaceAll(",", " ");
                }

                question.setTextContent(itemValue);
            }
            if (itemData == null || !itemData.isDeleted()) {
                hasItemData = true;
                groupElement.appendChild(question);
                // add the corresponding query element
                // processQueryElement(doc, item.getName(), itemData, groupElement);
            }

            // ItemData itemData = itemDataDao.findByItemEventCrfOrdinal(item.getItemId(), eventCrf.getEventCrfId(), i +
            // 1);
            // ItemFormMetadata itemMetadata = item.getItemFormMetadatas().iterator().next();
            i++;

            if (hasItemData) {
                target.add(groupElement);
            }
        }
        // maxGroupRepeat
        return target;
    }

}