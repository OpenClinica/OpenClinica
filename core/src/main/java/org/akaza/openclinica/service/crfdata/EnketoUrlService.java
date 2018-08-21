package org.akaza.openclinica.service.crfdata;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.core.form.xform.LogBean;
import org.akaza.openclinica.core.form.xform.QueriesBean;
import org.akaza.openclinica.core.form.xform.QueryBean;
import org.akaza.openclinica.core.form.xform.QueryType;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.DiscrepancyNoteDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutMediaDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.RepeatCountDao;
import org.akaza.openclinica.dao.hibernate.ResponseTypeDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.AuditLogEventType;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.EventDefinitionCrf;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.RepeatCount;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.domain.xform.XformParserHelper;
import org.akaza.openclinica.service.crfdata.xform.*;
import org.akaza.openclinica.service.pmanage.ParticipantPortalRegistrar;
import org.apache.commons.dbcp2.BasicDataSource;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
    public static final String INSTANCE_QUERIES_SUFFIX = "instance-queries.tpl";
    public static final String INSTANCE_SUFFIX = "instance.tpl";
    public static final String FORM_SUFFIX = "form.xml";
    public static final String QUERY_FLAVOR = "-query";
    public static final String SINGLE_ITEM_FLAVOR = "-single_item";
    public static final String NO_FLAVOR = "";
    public static final String QUERY = "comment";
    public static final String REASON = "reason";
    public static final String AUDIT = "audit";
    public static final String ITEMDATA = "item_data";
    public static final String STUDYEVENT = "study_event";
    public static final String SURVEY_CACHE = "/api/v2/survey/cache";

    public static final String DASH = "-";

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
    private StudyDao studyDao;

    @Autowired
    private EventCrfDao eventCrfDao;

    @Autowired
    private CrfDao crfDao;

    @Autowired
    private UserAccountDao userAccountDao;

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
    private AuditLogEventDao auditLogEventDao;

    @Autowired
    private XformParserHelper xformParserHelper;

    @Autowired
    private EnketoCredentials enketoCredentials;

    @Autowired
    private FormLayoutMediaDao formLayoutMediaDao;

    @Autowired
    private EventDefinitionCrfDao eventDefinitionCrfDao;

    @Autowired
    private RepeatCountDao repeatCountDao;

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO udao;
    StudyDAO sdao;

    public FormUrlObject getInitialDataEntryUrl(String subjectContextKey, PFormCacheSubjectContextEntry subjectContext, String studyOid,
                                                String flavor, Role role,
                                                String mode, String hash, String loadWarning, boolean isFormLocked) throws Exception {
        Study parentStudy = enketoCredentials.getParentStudy(studyOid);
        studyOid = parentStudy.getOc_oid();
        EnketoCredentials credentials = EnketoCredentials.getInstance(studyOid);
        EnketoAPI enketo = new EnketoAPI(credentials);
        StudyEvent studyEvent = null;
        if (subjectContext.getStudyEventId() != null) {
            studyEvent = studyEventDao.findById(Integer.valueOf(subjectContext.getStudyEventId()));
        }
        String crfOID = subjectContext.getFormLayoutOid() + DASH + hash + flavor;
        FormUrlObject formUrlObject = enketo.getFormURL(subjectContextKey, crfOID, studyOid, role,
                parentStudy, studyEvent, mode, loadWarning, isFormLocked);
        return formUrlObject;

    }

    public FormUrlObject getActionUrl(String subjectContextKey, PFormCacheSubjectContextEntry subjectContext, String studyOid, FormLayout formLayout, String flavor,
                               ItemDataBean idb, Role role, String mode, String loadWarning, boolean formLocked) throws Exception {
        Study study = enketoCredentials.getParentStudy(studyOid);
        Study site = enketoCredentials.getSiteStudy(studyOid);
        studyOid = study.getOc_oid();
        int filePath = study.getFilePath();

        String editURL = null;
        StudyEventDefinition eventDef = null;
        StudySubject subject = null;

        String goTo = null;
        if (subjectContext.getItemName() != null) {
            goTo = (subjectContext.isItemInRepeatingGroup()) ? "//" + subjectContext.getItemRepeatGroupName() + "["
                    + subjectContext.getItemRepeatOrdinalAdjusted() + "]//" + subjectContext.getItemName() : "//" + subjectContext.getItemName();
        }
        if (flavor.equals(SINGLE_ITEM_FLAVOR))
            goTo = "//" + subjectContext.getItemName();

        // Lookup relevant data

        eventDef = studyEventDefinitionDao.findByStudyEventDefinitionId(Integer.valueOf(subjectContext.getStudyEventDefinitionId()));
        StudyEvent studyEvent = studyEventDao.findById(Integer.valueOf(subjectContext.getStudyEventId()));
        subject = studyEvent.getStudySubject();

        if (formLayout == null) {
            formLayout = formLayoutDao.findByOcOID(subjectContext.getFormLayoutOid());
        }
        EventCrf eventCrf = eventCrfDao.findByStudyEventIdStudySubjectIdFormLayoutId(studyEvent.getStudyEventId(), subject.getStudySubjectId(),
                formLayout.getFormLayoutId());

        CrfVersion crfVersion = eventCrf.getCrfVersion();
        boolean markComplete = true;
        if (eventCrf.getStatusId() == Status.UNAVAILABLE.getCode()) {
            markComplete = false;
        }

        // Load populated instance
        String populatedInstance = "";
        String crfFlavor = "";
        String crfOid = "";
        if (flavor.equals(QUERY_FLAVOR)) {
            populatedInstance = populateInstance(crfVersion, formLayout, eventCrf, studyOid, filePath, flavor);
            crfFlavor = flavor;
        } else if (flavor.equals(SINGLE_ITEM_FLAVOR)) {
            populatedInstance = populateInstanceSingleItem(subjectContext, eventCrf, studyEvent, subject, crfVersion);
            crfFlavor = flavor + "[" + idb.getId() + "]";
            markComplete = false;
        }
        crfOid = formLayout.getOcOid() + DASH + formLayout.getXform() + crfFlavor;

        // Call Enketo api to get edit url
        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOid));

        // Build redirect url
        String redirectUrl = CoreResources.getField("sysURL");

        EventDefinitionCrf edc = eventDefinitionCrfDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(eventDef.getStudyEventDefinitionId(),
                formLayout.getCrf().getCrfId(), eventDef.getStudy().getStudyId());

        // Return Enketo URL
        List<FormLayoutMedia> mediaList = formLayoutMediaDao.findByEventCrfId(eventCrf.getEventCrfId());
        ActionUrlObject actionUrlObject = new ActionUrlObject(formLayout, crfOid, populatedInstance, subjectContextKey, redirectUrl, markComplete, studyOid,
                mediaList, goTo, flavor, role, study, site, studyEvent, mode, edc, eventCrf, loadWarning, formLocked);

        // EnketoCredentials credentials = EnketoCredentials.getInstance(studyOid);
        // URL eURL = new URL(credentials.getServerUrl() + SURVEY_CACHE);
        // enketo.registerAndDeleteCache(eURL, crfOid);

        EnketoFormResponse eur = enketo.registerAndGetActionURL(actionUrlObject);

        if (eur.getEnketoUrlResponse().getUrl() != null) {
            editURL = eur.getEnketoUrlResponse().getUrl();
        }

        logger.debug("Generating Enketo edit url for form: " + editURL);

        return new FormUrlObject(editURL, eur.isLockOn());

    }

    public QueriesBean buildQueryElement(ItemData itemdata) {
        QueriesBean queryElement = new QueriesBean();
        List<QueryBean> queryBeans = new ArrayList<>();
        List<LogBean> logBeans = new ArrayList<LogBean>();
        List<DiscrepancyNote> dns = discrepancyNoteDao.findChildQueriesByItemData(itemdata.getItemDataId());

        int i = 0;
        for (DiscrepancyNote dn : dns) {
            i++;
            QueryBean query = new QueryBean();
            query.setId(String.valueOf(i));
            query.setAssigned_to(dn.getUserAccountByOwnerId().getUserName());
            query.setComment(escapedValue(dn.getDetailedNotes()));
            if (dn.getResolutionStatus().getResolutionStatusId() != 5) {
                query.setStatus(dn.getResolutionStatus().getName().toLowerCase());
            }
            DateTime dateTime = new DateTime(dn.getDateCreated());
            query.setDate_time(convertDateFormat(dateTime));
            query.setNotify(false);
            query.setUser(dn.getUserAccountByOwnerId().getUserName());
            if (dn.getDiscrepancyNoteType().getDiscrepancyNoteTypeId() == QueryType.QUERY.getValue()) {
                query.setType(QUERY);
            } else if (dn.getDiscrepancyNoteType().getDiscrepancyNoteTypeId() == QueryType.REASON.getValue()) {
                query.setType(REASON);
            }
            queryBeans.add(query);
        }

        AuditLogEvent auditLog = new AuditLogEvent();
        auditLog.setEntityId(new Integer(itemdata.getItemDataId()));
        auditLog.setAuditTable(ITEMDATA);
        ArrayList<AuditLogEvent> auditLogEvents = auditLogEventDao.findByParam(auditLog, null);

        for (AuditLogEvent audit : auditLogEvents) {
            LogBean logBean = new LogBean();
            String oldValue = audit.getOldValue() != null ? audit.getOldValue() : "";
            String newValue = audit.getNewValue() != null ? audit.getNewValue() : "";
            logBean.setMessage("Value changed from \"" + escapedValue(oldValue) + "\" to \"" + escapedValue(newValue) + "\"");
            DateTime dateTime = new DateTime(audit.getAuditDate());
            logBean.setDate_time(convertDateFormat(dateTime));
            UserAccount uAccount = userAccountDao.findById(audit.getUserAccount().getUserId());
            logBean.setUser(uAccount.getUserName());
            logBean.setAssigned_to(uAccount.getUserName());
            logBean.setType(AUDIT);
            logBeans.add(logBean);
        }

        queryElement.setQueries(queryBeans);
        queryElement.setLogs(logBeans);
        if (queryElement.getQueries().size() != 0 || queryElement.getLogs().size() != 0)
            return queryElement;
        else
            return null;

    }

    private String convertDateFormat(DateTime dateTime) {
        String dt = dateTime.toString();
        dt = dt.replaceAll("T", " ");
        dt = dt.substring(0, 23) + " " + dt.substring(23);
        return dt;
    }

    private String populateInstance(CrfVersion crfVersion, FormLayout formLayout, EventCrf eventCrf, String studyOid, int filePath, String flavor)
            throws Exception {

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
                    if (flavor.equals(QUERY_FLAVOR))
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
                            String itemValue = getItemValue(itemData, crfVersion);
                            hashMap.put(igm.getItem().getName(), itemData != null ? itemValue : "");

                            ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(igm.getItem().getItemId(),
                                    crfVersion.getCrfVersionId());
                            Integer responseTypeId = itemFormMetadata.getResponseSet().getResponseType().getResponseTypeId();

                            if (flavor.equals(QUERY_FLAVOR) && responseTypeId != 8) {
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
                    String itemValue = getItemValue(itemData, crfVersion);
                    data.put(igm.getItem().getName(), itemData != null ? itemValue : "");
                    ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(igm.getItem().getItemId(), crfVersion.getCrfVersionId());
                    Integer responseTypeId = itemFormMetadata.getResponseSet().getResponseType().getResponseTypeId();

                    if (flavor.equals(QUERY_FLAVOR) && responseTypeId != 8) {
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
        Study study = studyDao.findByOcOID(studyOid);
        String templateStr = "";
        int studyFilePath = study.getFilePath();
        CrfBean crf = crfDao.findById(formLayout.getCrf().getCrfId());

        do {
            templateStr = getTemplate(studyOid, studyFilePath, crf.getOcOid(), formLayout.getOcOid(), flavor);
            studyFilePath--;
        } while (templateStr.equals("") && studyFilePath > 0);

        data.put("instanceID", "uuid:1234");
        List<RepeatCount> repeatCounts = repeatCountDao.findAllByEventCrfId(eventCrf.getEventCrfId());
        for (RepeatCount repeatCount : repeatCounts) {
            data.put(repeatCount.getGroupName(), repeatCount.getGroupCount());
        }

        Template template = new Template("template name", new StringReader(templateStr), new Configuration());

        StringWriter wtr = new StringWriter();
        template.process(data, wtr);

        String instance = wtr.toString();
        StudyEvent studyEvent = studyEventDao.findByStudyEventId(eventCrf.getStudyEvent().getStudyEventId());
        if (studyEvent.getSubjectEventStatusId().equals(SubjectEventStatus.SIGNED.getId())) {
            AuditLogEvent auditLogEvent = new AuditLogEvent();
            auditLogEvent.setAuditTable(STUDYEVENT);
            auditLogEvent.setEntityId(studyEvent.getStudyEventId());
            auditLogEvent.setEntityName("Status");
            auditLogEvent.setAuditLogEventType(new AuditLogEventType(31));
            auditLogEvent.setNewValue(String.valueOf(SubjectEventStatus.SIGNED.getId()));

            List<AuditLogEvent> ales = auditLogEventDao.findByParam(auditLogEvent);
            for (AuditLogEvent audit : ales) {
                String signature = audit.getDetails();
                instance = instance.substring(0, instance.indexOf("</meta>")) + "<oc:signature>" + signature + "</oc:signature>"
                        + instance.substring(instance.indexOf("</meta>"));
                break;
            }
        }
        System.out.println(instance);
        return instance;
    }

    private String getItemValue(ItemData itemData, CrfVersion crfVersion) {
        String itemValue = null;
        if (itemData != null) {
            itemValue = itemData.getValue();
            ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(itemData.getItem().getItemId(), crfVersion.getCrfVersionId());

            // Convert space separated Enketo multiselect values to comma separated OC
            // multiselect
            // values
            Integer responseTypeId = itemFormMetadata.getResponseSet().getResponseType().getResponseTypeId();
            if (responseTypeId == 3 || responseTypeId == 7) {
                itemValue = itemValue.replaceAll(",", " ");
            }
        }
        return escapedValue(itemValue);
    }

    private String escapedValue(String value) {
        if (value != null) {
            value = value.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        }
        return value;
    }

    private String populateInstanceSingleItem(PFormCacheSubjectContextEntry subjectContext, EventCrf eventCrf, StudyEvent studyEvent, StudySubject studySubject,
            CrfVersion crfVersion) throws JsonProcessingException {
        String itemName = subjectContext.getItemName();
        if (itemName.endsWith(QUERY_SUFFIX))
            itemName = itemName.substring(0, itemName.length() - QUERY_SUFFIX.length());

        int ordinal = subjectContext.getItemRepeatOrdinalOriginal();

        ItemData itemData = itemDataDao.findByEventCrfItemNameDeletedOrNot(eventCrf.getEventCrfId(), itemName, ordinal);
        if (itemData == null) {
            List<EventCrf> eventCrfs = eventCrfDao.findByStudyEventIdStudySubjectId(studyEvent.getStudyEventId(), studySubject.getOcOid());
            for (EventCrf eCrf : eventCrfs) {
                itemData = itemDataDao.findByEventCrfItemNameDeletedOrNot(eCrf.getEventCrfId(), itemName, ordinal);
                if (itemData != null)
                    break;
            }
        }

        String itemValue = getItemValue(itemData, crfVersion);

        String queries = "";
        if (itemData != null) {
            ObjectMapper mapper = new ObjectMapper();
            QueriesBean queriesBean = buildQueryElement(itemData);
            queries = queriesBean != null ? mapper.writeValueAsString(queriesBean) : "";
        }
        int repeatOrdinal = itemData.getOrdinal();

        StringBuffer sb = new StringBuffer();
        sb.append("<form xmlns:enk=\"http://enketo.org/xforms\" id=\"single_item\" >");
        sb.append("<group_layout enk:ordinal=\"" + repeatOrdinal + "\">");

        sb.append("<" + itemName + ">");
        sb.append(itemValue);
        sb.append("</" + itemName + ">");

        sb.append("<" + itemName + QUERY_SUFFIX + ">");
        sb.append(queries);
        sb.append("</" + itemName + QUERY_SUFFIX + ">");

        sb.append("</group_layout>");
        sb.append("<meta><instanceID>uuid:" + itemName + "</instanceID></meta>");
        sb.append("</form>");

        return sb.toString();
    }

    private String getTemplate(String studyOID, int studyFilePath, String crfOID, String formLayoutOID, String flavor) throws IOException {
        String templateStr = "";
        String directoryPath = Utils.getFilePath() + Utils.getCrfMediaPath(studyOID, studyFilePath, crfOID, formLayoutOID);
        File dir = new File(directoryPath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if ((flavor.equals(QUERY_FLAVOR) && child.getName().endsWith(INSTANCE_QUERIES_SUFFIX))
                        || (flavor.equals(NO_FLAVOR) && child.getName().endsWith(INSTANCE_SUFFIX))) {
                    templateStr = new String(Files.readAllBytes(Paths.get(child.getPath())));
                    break;
                }
            }
        }
        return templateStr;
    }
}
