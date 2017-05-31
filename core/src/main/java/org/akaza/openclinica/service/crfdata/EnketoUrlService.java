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
import java.util.Map;

import javax.servlet.ServletContext;

import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.core.form.xform.LogBean;
import org.akaza.openclinica.core.form.xform.QueriesBean;
import org.akaza.openclinica.core.form.xform.QueryBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import org.akaza.openclinica.dao.hibernate.CrfDao;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.DiscrepancyNoteDao;
import org.akaza.openclinica.dao.hibernate.EventCrfDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutDao;
import org.akaza.openclinica.dao.hibernate.FormLayoutMediaDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupDao;
import org.akaza.openclinica.dao.hibernate.ItemGroupMetadataDao;
import org.akaza.openclinica.dao.hibernate.ResponseTypeDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.CrfBean;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.akaza.openclinica.domain.datamap.EventCrf;
import org.akaza.openclinica.domain.datamap.FormLayout;
import org.akaza.openclinica.domain.datamap.FormLayoutMedia;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.datamap.ItemFormMetadata;
import org.akaza.openclinica.domain.datamap.ItemGroup;
import org.akaza.openclinica.domain.datamap.ItemGroupMetadata;
import org.akaza.openclinica.domain.datamap.Study;
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
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
    public static final String NO_FLAVOR = "";
    public static final String COMMENT = "comment";
    public static final String AUDIT = "audit";
    public static final String ITEMDATA = "item_data";

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

    public static final String FORM_CONTEXT = "ecid";
    ParticipantPortalRegistrar participantPortalRegistrar;

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    UserAccountDAO udao;
    StudyDAO sdao;

    public String getInitialDataEntryUrl(String subjectContextKey, PFormCacheSubjectContextEntry subjectContext, String studyOid, String flavor)
            throws Exception {
        // Call Enketo api to get edit url
        Study study = enketoCredentials.getParentStudy(studyOid);
        studyOid = study.getOc_oid();
        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOid));
        return enketo.getFormURL(subjectContext.getFormLayoutOid() + flavor) + "?ecid=" + subjectContextKey;

    }

    public String getEditUrl(String subjectContextKey, PFormCacheSubjectContextEntry subjectContext, String studyOid, FormLayout formLayout,
            StudyEvent studyEvent, String flavor) throws Exception {
        Study study = enketoCredentials.getParentStudy(studyOid);
        studyOid = study.getOc_oid();

        String editURL = null;
        StudyEventDefinition eventDef;
        StudySubject subject;

        String goTo = null;
        if (subjectContext.getItemName() != null) {
            goTo = (subjectContext.isItemInRepeatingGroup())
                    ? "//" + subjectContext.getItemRepeatGroupName() + "[" + subjectContext.getItemRepeatOrdinal() + "]//" + subjectContext.getItemName()
                    : "//" + subjectContext.getItemName();
        }

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
        String populatedInstance = populateInstance(crfVersion, formLayout, eventCrf, studyOid, flavor);

        // Call Enketo api to get edit url
        EnketoAPI enketo = new EnketoAPI(EnketoCredentials.getInstance(studyOid));

        // Build redirect url
        String redirectUrl = getRedirectUrl(subject.getOcOid(), studyOid);

        boolean markComplete = true;
        if (eventCrf.getStatusId() == Status.UNAVAILABLE.getCode()) {
            markComplete = false;
        }
        // Return Enketo URL
        List<FormLayoutMedia> mediaList = formLayoutMediaDao.findByEventCrfId(eventCrf.getEventCrfId());

        EnketoURLResponse eur = enketo.getEditURL(formLayout, flavor, populatedInstance, subjectContextKey, redirectUrl, markComplete, studyOid, mediaList,
                goTo);
        editURL = eur.getEdit_url();
        int hashIndex = editURL.lastIndexOf("#");
        String part1 = "";
        String part2 = "";
        if (hashIndex != -1) {
            part1 = editURL.substring(0, hashIndex);
            part2 = editURL.substring(hashIndex);
            editURL = part1 + "&ecid=" + subjectContextKey + part2;
        } else {
            editURL = editURL + "&ecid=" + subjectContextKey;
        }

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
        List<LogBean> logBeans = new ArrayList<LogBean>();
        List<DiscrepancyNote> dns = discrepancyNoteDao.findChildQueriesByItemData(itemdata.getItemDataId());

        int i = 0;
        for (DiscrepancyNote dn : dns) {
            i++;
            QueryBean query = new QueryBean();
            query.setId(String.valueOf(i));
            query.setAssigned_to(dn.getUserAccountByOwnerId().getUserName());
            query.setComment(escapedValue(dn.getDetailedNotes()));
            query.setStatus(dn.getResolutionStatus().getName().toLowerCase());
            DateTime dateTime = new DateTime(dn.getDateCreated());
            query.setDate_time(convertDateFormat(dateTime));
            query.setNotify(false);
            query.setUser(dn.getUserAccountByOwnerId().getUserName());
            query.setType(COMMENT);
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
            logBean.setMessage("Value Changed from \"" + escapedValue(oldValue) + "\" to \"" + escapedValue(newValue) + "\"");
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

    private String populateInstance(CrfVersion crfVersion, FormLayout formLayout, EventCrf eventCrf, String studyOid, String flavor) throws Exception {

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
                            if (flavor.equals(QUERY_FLAVOR)) {
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
                    if (flavor.equals(QUERY_FLAVOR)) {
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

                if (flavor.equals(QUERY_FLAVOR) && child.getName().endsWith(INSTANCE_QUERIES_SUFFIX)
                        || flavor.equals(NO_FLAVOR) && child.getName().endsWith(INSTANCE_SUFFIX)) {
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

}
