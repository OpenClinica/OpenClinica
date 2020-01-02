package org.akaza.openclinica.controller.openrosa;

import static org.akaza.openclinica.control.core.SecureController.respage;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.akaza.openclinica.controller.openrosa.processor.QueryServiceHelperBean;
import core.org.akaza.openclinica.core.EmailEngine;
import core.org.akaza.openclinica.core.form.xform.QueriesBean;
import core.org.akaza.openclinica.core.form.xform.QueryBean;
import core.org.akaza.openclinica.core.form.xform.QueryType;
import core.org.akaza.openclinica.dao.hibernate.DiscrepancyNoteDao;
import core.org.akaza.openclinica.dao.hibernate.DnItemDataMapDao;
import core.org.akaza.openclinica.dao.hibernate.EventCrfDao;
import core.org.akaza.openclinica.dao.hibernate.ItemDao;
import core.org.akaza.openclinica.dao.hibernate.ItemDataDao;
import core.org.akaza.openclinica.dao.hibernate.ResolutionStatusDao;
import core.org.akaza.openclinica.dao.hibernate.StudyDao;
import core.org.akaza.openclinica.dao.hibernate.UserAccountDao;
import core.org.akaza.openclinica.domain.Status;
import core.org.akaza.openclinica.domain.datamap.*;
import core.org.akaza.openclinica.domain.user.UserAccount;
import core.org.akaza.openclinica.service.OCUserDTO;
import core.org.akaza.openclinica.service.crfdata.EnketoUrlService;
import core.org.akaza.openclinica.web.SQLInitServlet;
import core.org.akaza.openclinica.web.pform.OpenRosaService;
import core.org.akaza.openclinica.web.pform.StudyAndSiteEnvUuid;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

/**
 * Created by yogi on 11/10/16.
 */
@Service("queryService")
public class QueryServiceImpl implements QueryService {

    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemDataDao itemDataDao;
    @Autowired
    private DiscrepancyNoteDao discrepancyNoteDao;
    @Autowired
    private ResolutionStatusDao resolutionStatusDao;
    @Autowired
    private DnItemDataMapDao dnItemDataMapDao;
    @Autowired
    private UserAccountDao userAccountDao;
    @Autowired
    private EventCrfDao eventCrfDao;
    @Autowired
    private OpenRosaService openRosaService;
    @Autowired
    private BeanFactory beanFactory;
    @Autowired
    private EnketoUrlService enketoUrlService;

    @Autowired
    private ApplicationContext appContext;
    public static final String DASH = "-";

    @Override
    public void process(QueryServiceHelperBean helperBean, SubmissionContainer container, Node itemNode, int itemOrdinal) throws Exception {
        String node = itemNode.getTextContent();
        if (StringUtils.isEmpty(node))
            return;
        helperBean.setContainer(container);
        helperBean.setItemOrdinal(itemOrdinal);
        helperBean.setItemNode(itemNode);
        helperBean.setUserAccount(container.getUser());
        ItemData id = getItemData(helperBean);
        if (id == null) {
            helperBean.setItemData(createBlankItemData(helperBean));
        } else {
            helperBean.setItemData(id);
        }
        QueriesBean queries = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            queries = objectMapper.readValue(node, QueriesBean.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }

        List<Integer> idList = new ArrayList();
        List<QueryBean> qBeans = queries.getQueries();
        QueryBean queryBean = null;

        List<DiscrepancyNote> childDns = null;
        if (qBeans.size() > 0) {
            for (QueryBean qBean : qBeans) {
                idList.add(Integer.valueOf(qBean.getId()));
            }
            Collections.reverse(idList);
            childDns = findChildQueries(helperBean.getItemData());

            while (childDns.size() < qBeans.size()) {
                DiscrepancyNote parentDN = null;
                DiscrepancyNote childDN = null;
                queryBean = qBeans.get(0);

            List<DiscrepancyNote> parentDiscrepancyNoteList = discrepancyNoteDao.findParentNotesByItemData(helperBean.getItemData().getItemDataId());
            for (DiscrepancyNote pDiscrepancyNote : parentDiscrepancyNoteList) {
                if (pDiscrepancyNote.getThreadUuid()!=null && pDiscrepancyNote.getThreadUuid().equals(queryBean.getThread_id())) {
                    parentDN = pDiscrepancyNote;
                    break;
                }
            }


            if (parentDN == null) {
                parentDN = createQuery(helperBean, queryBean,true);
                parentDN = discrepancyNoteDao.saveOrUpdate(parentDN);
                helperBean.setDn(parentDN);
                saveQueryItemDatamap(helperBean);
            }


                // Enketo passes JSON "id" attribute for unsubmitted queries only
                // if (StringUtils.isEmpty(queryBean.getId())){

                childDN = createQuery(helperBean, queryBean,false);
                childDN.setParentDiscrepancyNote(parentDN);
                childDN = discrepancyNoteDao.saveOrUpdate(childDN);

                parentDN.setUserAccount(childDN.getUserAccount());
                setResolutionStatus(queryBean, parentDN);
                parentDN.setUserAccountByOwnerId(helperBean.getContainer().getUser());
                parentDN.setDiscrepancyNoteType(childDN.getDiscrepancyNoteType());
                parentDN.setDetailedNotes(childDN.getDetailedNotes());

                // OC-10617 After update, Queries Table displays incorrect date created.
                // (don't update parent detailedNotes and dateCreated)
                // parentDN.setDetailedNotes(childDN.getDetailedNotes());
                // parentDN.setDateCreated(new Date());
                parentDN = discrepancyNoteDao.saveOrUpdate(parentDN);

                helperBean.setDn(childDN);
                helperBean.setParentDn(parentDN);
                saveQueryItemDatamap(helperBean);
                handleEmailNotification(helperBean, queryBean);
                qBeans.remove(0);
            }
        }
    }

    public DiscrepancyNote createQuery(QueryServiceHelperBean helperBean, QueryBean queryBean,boolean parentDn) throws Exception {
        DiscrepancyNote dn = new DiscrepancyNote();
        dn.setStudy(helperBean.getContainer().getStudy());
        dn.setEntityType("itemData");

        dn.setDetailedNotes(queryBean.getComment());
        if (queryBean.getType().equals(QueryType.QUERY.getName())) {
            dn.setDiscrepancyNoteType(new DiscrepancyNoteType(3));
            setResolutionStatus(queryBean, dn);
        }else if
            (queryBean.getType().equals(QueryType.REASON.getName())){
            dn.setDiscrepancyNoteType(new DiscrepancyNoteType(4));
            dn.setResolutionStatus(resolutionStatusDao.findById(5));
        }else if
        (queryBean.getType().equals(QueryType.ANNOTATION.getName())){
            dn.setDiscrepancyNoteType(new DiscrepancyNoteType(2));
            dn.setResolutionStatus(resolutionStatusDao.findById(5));
        }
        String user = queryBean.getUser();
        if (user == null) {
            dn.setUserAccountByOwnerId(helperBean.getContainer().getUser());
        } else {
            UserAccount userAccountByOwnerId = userAccountDao.findByUserName(user);
            dn.setUserAccountByOwnerId(userAccountByOwnerId);
        }

        String assignedTo = "";
        if (queryBean.getType().equals(QueryType.QUERY.getName())) {
            if (queryBean.getComment().startsWith("Automatic query for:")) {
                assignedTo = helperBean.getContainer().getUser().getUserName();
            } else {
                assignedTo = queryBean.getAssigned_to();
            }
        }
        if (!StringUtils.isEmpty(assignedTo)) {
            UserAccount userAccount = userAccountDao.findByUserName(assignedTo);
            if (userAccount == null) {
                userAccount = createUserAccount(assignedTo, helperBean.getContainer().getStudy());
            }
            helperBean.setUserAccount(userAccount);
            dn.setUserAccount(userAccount);
        }
        // create itemData when a query is created without an autosaved itemdata
        if (helperBean.getItemData() == null) {
            helperBean.setItemData(createBlankItemData(helperBean));
        }
        dn.setDateCreated(new Date());
        dn.setThreadUuid(queryBean.getThread_id());
        if(parentDn && queryBean.getType().equals(QueryType.QUERY.getName())){
            int maxThreadNumber= discrepancyNoteDao.getMaxThreadNumber();
            dn.setThreadNumber(maxThreadNumber+1);
        }
        return dn;
    }

    private UserAccount createUserAccount(String assignedTo, Study study) {
        UserAccount userAccount = null;
        StudyAndSiteEnvUuid studyAndSiteEnvUuid = new StudyAndSiteEnvUuid();
        if (study.getStudy() == null)
            studyAndSiteEnvUuid.setStudyEnvUuid(study.getStudyEnvUuid());
        else {
            studyAndSiteEnvUuid.setStudyEnvUuid(study.getStudy().getStudyEnvUuid());
            studyAndSiteEnvUuid.setSiteEnvUuid(study.getStudyEnvSiteUuid());
        }
        try {
            OCUserDTO ocUserDTO = openRosaService.fetchUserInfoFromUserService(studyAndSiteEnvUuid, assignedTo);
            userAccount = beanFactory.getBean(UserAccount.class, ocUserDTO);

        } catch (Exception e) {
            logger.error("Cannot get user info for user:" + assignedTo, e);
        }
        return userAccount;
    }

    private void handleEmailNotification(QueryServiceHelperBean helperBean, QueryBean queryBean) throws Exception {
        if (queryBean.getNotify() == null)
            return;
        if (queryBean.getNotify() != true) {
            return;
        }
        prepareEmail(helperBean);
    }

    private ItemData getItemData(QueryServiceHelperBean helperBean) {
        int eventCrfId = helperBean.getContainer().getEventCrf().getEventCrfId();
        String itemName = helperBean.getParentElementName();
        int ordinal = helperBean.getItemOrdinal();
        int studyEventId = helperBean.getContainer().getStudyEvent().getStudyEventId();
        String studySubjectOid = helperBean.getContainer().getSubject().getOcOid();

        ItemData itemData = itemDataDao.findByEventCrfItemNameDeletedOrNot(eventCrfId, itemName, ordinal);
        if (itemData == null) {
            List<EventCrf> eventCrfs = (List<EventCrf>) eventCrfDao.findByStudyEventIdStudySubjectIdCrfId(studyEventId,
                    helperBean.getContainer().getSubject().getStudySubjectId(), helperBean.getContainer().getFormLayout().getCrf().getCrfId());
            for (EventCrf eCrf : eventCrfs) {
                itemData = itemDataDao.findByEventCrfItemNameDeletedOrNot(eCrf.getEventCrfId(), itemName, ordinal);
                if (itemData != null)
                    break;
            }
        }

        return itemData;
    }

    private ItemData createBlankItemData(QueryServiceHelperBean helperBean) {
        Item item = itemDao.findByNameCrfId(helperBean.getParentElementName(), helperBean.getContainer().getCrfVersion().getCrf().getCrfId());
        ItemData itemData = new ItemData();
        itemData.setItem(item);
        itemData.setEventCrf(helperBean.getContainer().getEventCrf());
        itemData.setValue("");
        itemData.setDateCreated(new Date());
        itemData.setStatus(Status.AVAILABLE);
        // TODO this value should change once you have an ordinal attribute specified in the query
        itemData.setOrdinal(helperBean.getItemOrdinal());
        itemData.setUserAccount(helperBean.getUserAccount());
        itemData.setDeleted(false);
        itemData.setInstanceId(helperBean.getContainer().getInstanceId());
        itemData = itemDataDao.saveOrUpdate(itemData);
        return itemData;
    }

    private DiscrepancyNote findQueryParent(ItemData itemData, int noteTypeId) {
        DiscrepancyNote parentDiscrepancyNote = discrepancyNoteDao.findParentQueryByItemData(itemData.getItemDataId(), noteTypeId);
        return parentDiscrepancyNote;
    }

    private List<DiscrepancyNote> findChildQueries(ItemData itemData) {
        List<DiscrepancyNote> childDiscrepancyNotes = discrepancyNoteDao.findChildQueriesByItemData(itemData.getItemDataId());
        return childDiscrepancyNotes;
    }

    public void saveQueryItemDatamap(QueryServiceHelperBean helperBean) {
        // Create Mapping for new Discrepancy Note
        DnItemDataMapId dnItemDataMapId = new DnItemDataMapId();
        dnItemDataMapId.setDiscrepancyNoteId(helperBean.getDn().getDiscrepancyNoteId());
        dnItemDataMapId.setItemDataId(helperBean.getItemData().getItemDataId());
        dnItemDataMapId.setStudySubjectId(helperBean.getContainer().getSubject().getStudySubjectId());
        dnItemDataMapId.setColumnName("value");

        DnItemDataMap mapping = new DnItemDataMap();
        mapping.setDnItemDataMapId(dnItemDataMapId);
        mapping.setItemData(helperBean.getItemData());
        mapping.setStudySubject(helperBean.getContainer().getSubject());
        mapping.setActivated(false);
        mapping.setDiscrepancyNote(helperBean.getDn());
        dnItemDataMapDao.saveOrUpdate(mapping);
        // updateParentQuery(helperBean);
    }

    private void updateParentQuery(QueryServiceHelperBean helperBean) {
        if (helperBean.getDn().getParentDiscrepancyNote() == null)
            return;

        DiscrepancyNote itemParentNote = discrepancyNoteDao.findByDiscrepancyNoteId(helperBean.getDn().getParentDiscrepancyNote().getDiscrepancyNoteId());
        itemParentNote.setResolutionStatus(helperBean.getResStatus());
        discrepancyNoteDao.saveOrUpdate(itemParentNote);
    }

    public String getQueryAttribute(QueryServiceHelperBean helperBean, Node itemNode) {
        helperBean.setItemNode(itemNode);
        helperBean.setParentElementName(QueryService.super.getQueryAttribute(helperBean, itemNode));
        return helperBean.getParentElementName();
    }

    private void prepareEmail(QueryServiceHelperBean helperBean) throws Exception {
        StringBuffer message = new StringBuffer();
        Integer threadNumber = helperBean.getDn().getThreadNumber();
        if (null == threadNumber)
            threadNumber = helperBean.getDn().getParentDiscrepancyNote().getThreadNumber();

        message.append(MessageFormat.format(respage.getString("mailDNHeader"), helperBean.getUserAccount().getFirstName(), helperBean.getUserAccount().getLastName()));
        message.append(
            "<A HREF='" +
                SQLInitServlet.getField("sysURL.base") + 
                "ViewNotes?module=submit&maxRows=50&showMoreLink=true&listNotes_tr_=true&listNotes_p_=1&listNotes_mr_=50&listNotes_f_discrepancyNoteBean.disType=Query&listNotes_f_discrepancyNoteBean.threadNumber=" + 
                threadNumber.toString() + 
            "'>[Click Here]</A><BR/>"
        );
        message.append(respage.getString("you_received_this_from"));
        message.append(respage.getString("email_body_separator"));
        message.append(respage.getString("disc_note_info"));
        message.append(respage.getString("email_body_separator"));
        message.append(
             MessageFormat.format(respage.getString("mailDNParameters1"), String.valueOf(helperBean.getParentDn().getThreadNumber()),helperBean.getDn().getDetailedNotes(), helperBean.getContainer().getUser().getUserName()));
        message.append(respage.getString("email_body_separator"));
        message.append(respage.getString("entity_information"));
        message.append(respage.getString("email_body_separator"));
        message.append(MessageFormat.format(respage.getString("mailDNParameters2"), helperBean.getDn().getStudy().getName(),
                helperBean.getContainer().getSubject().getLabel()));

        if (!("studySub".equalsIgnoreCase(helperBean.getDn().getEntityType()) || "subject".equalsIgnoreCase(helperBean.getDn().getEntityType()))) {
            message.append(MessageFormat.format(respage.getString("mailDNParameters3"),
                    helperBean.getContainer().getStudyEvent().getStudyEventDefinition().getName()));
            if (!"studyEvent".equalsIgnoreCase(helperBean.getDn().getEntityType())) {
                message.append(MessageFormat.format(respage.getString("mailDNParameters4"), helperBean.getContainer().getCrfVersion().getCrf().getName()));
                if (!"eventCrf".equalsIgnoreCase(helperBean.getDn().getEntityType())) {
                    message.append(MessageFormat.format(respage.getString("mailDNParameters6"), helperBean.getParentElementName()));
                    message.append(MessageFormat.format(respage.getString("mailDNParameters7"), helperBean.getItemData().getItem().getBriefDescription()));
                }
            }
            else {
                String description = helperBean.getDn().getDnStudyEventMaps().get(0).getDnStudyEventMapId().getColumnName();
                description = description.equals("start_date") ? "Event Start Date" : "Event End Date";
                message.append(MessageFormat.format(respage.getString("mailDNParameters7"), description));
            }
        }

        message.append(respage.getString("email_body_separator"));
        message.append(MessageFormat.format(respage.getString("mailDNThanks"), helperBean.getDn().getStudy().getName()));
        message.append(respage.getString("email_body_separator"));
        message.append(respage.getString("disclaimer"));
        message.append(respage.getString("email_body_separator"));
        message.append(respage.getString("email_footer"));
        String subject = MessageFormat.format(respage.getString("mailDNSubject"), helperBean.getDn().getStudy().getName(), helperBean.getParentElementName());

        String emailBodyString = message.toString();
        try {
            sendEmail(helperBean.getUserAccount().getEmail().trim(), subject, emailBodyString, true);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

    }

    private void sendEmail(String to, String subject, String body, Boolean htmlEmail) throws Exception {
        try {
            JavaMailSenderImpl mailSender = (JavaMailSenderImpl) appContext.getBean("mailSender");
            Properties javaMailProperties = mailSender.getJavaMailProperties();
            if (null != javaMailProperties) {
                if (javaMailProperties.get("mail.smtp.localhost") == null || ((String) javaMailProperties.get("mail.smtp.localhost")).equalsIgnoreCase("")) {
                    javaMailProperties.put("mail.smtp.localhost", "localhost");
                }
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, htmlEmail);
            helper.setFrom(EmailEngine.getAdminEmail());
            helper.setTo(processMultipleImailAddresses(to.trim()));
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(mimeMessage);

            logger.debug("Email sent successfully on {}", new Date());
        } catch (MailException me) {
            logger.error("Email could not be sent on {} due to: {}", new Date(), me.getMessage());
        }
    }

    private InternetAddress[] processMultipleImailAddresses(String to) throws MessagingException {
        List<String> recipientsArray = Arrays.asList(to.split(","));

        InternetAddress[] addressTo = recipientsArray.stream().map((java.util.function.Function<String, InternetAddress>) (address) -> {
            try {
                return new InternetAddress(address);
            } catch (AddressException e) {
                logger.error("Error while creating the InternetAddress: ",e);
            }
            return null;
        }).toArray(InternetAddress[]::new);

        return addressTo;

    }

    private void setResolutionStatus(QueryBean queryBean, DiscrepancyNote dn) {
        if (queryBean.getStatus() == null) {
            dn.setResolutionStatus(resolutionStatusDao.findById(5));
        } else if (queryBean.getStatus().equals("new")) {
            dn.setResolutionStatus(resolutionStatusDao.findById(1));
        } else if (queryBean.getStatus().equals("updated")) {
            dn.setResolutionStatus(resolutionStatusDao.findById(2));
        } else if (queryBean.getStatus().equals("closed")) {
            dn.setResolutionStatus(resolutionStatusDao.findById(4));
        } else if (queryBean.getStatus().equals("closed-modified")) {
            dn.setResolutionStatus(resolutionStatusDao.findById(6));
        } else {
        }
    }
}
