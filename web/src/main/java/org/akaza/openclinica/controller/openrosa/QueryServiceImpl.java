package org.akaza.openclinica.controller.openrosa;

import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.form.xform.QueriesBean;
import org.akaza.openclinica.core.form.xform.QueryBean;
import org.akaza.openclinica.dao.hibernate.*;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.web.SQLInitServlet;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import static org.akaza.openclinica.control.core.SecureController.respage;

/**
 * Created by yogi on 11/10/16.
 */
@Service("queryService")
public class QueryServiceImpl implements QueryService {
    private Node itemNode;
    private String parentElementName;
    private DiscrepancyNote dn;
    private SubmissionContainer container;
    private CrfVersion crfVersion;
    private EventCrf eventCrf;
    private ItemData itemData;

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
    private StudyDao studyDao;

    @Autowired
    private ApplicationContext appContext;

    private UserAccount userAccount;

    @Override
    public void process(SubmissionContainer container, CrfVersion crfVersion, EventCrf eventCrf)  throws Exception {
        this.container = container;
        this.crfVersion = crfVersion;
        this.eventCrf = eventCrf;
        itemData = getItemData();
        createQuery();
        saveQueryItemDatamap();
    }

    private void createQuery() throws Exception {
        dn = new DiscrepancyNote();
        ResolutionStatus resStatus = resolutionStatusDao.findByResolutionStatusId(1);
        dn.setStudy(container.getStudy());
        dn.setEntityType("itemData");
        dn.setDescription("description");
        QueriesBean queries = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            queries = objectMapper.readValue(itemNode.getTextContent(), QueriesBean.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }
        QueryBean queryBean = queries.getQueries().get(0);

        dn.setDetailedNotes(queryBean.getComment());
        dn.setDiscrepancyNoteType(new DiscrepancyNoteType(3));
        dn.setResolutionStatus(resStatus);
        String assignedTo = queryBean.getAssigned_to();
        int endIndex = assignedTo.indexOf(")");
        int begIndex = assignedTo.indexOf("(");
        String userName = assignedTo.substring(begIndex + 1, endIndex);
        userAccount = userAccountDao.findByUserName(userName);
        dn.setUserAccount(userAccount);
        dn.setUserAccountByOwnerId(container.getUser());
        // create itemData when a query is created without an autosaved itemdata
        if (itemData == null) {
            createBlankItemData(userAccount);
        }
        dn.setParentDiscrepancyNote(findQueryParent());
        dn.setDateCreated(new Date());
        dn = discrepancyNoteDao.saveOrUpdate(dn);
        handleEmailNotification(queryBean, userAccount);
    }

    private void handleEmailNotification(QueryBean queryBean, UserAccount userAccount) throws Exception {
        if (queryBean.getNotify() != true) {
            return;
        }
        prepareEmail();
    }
    private ItemData getItemData() {
        ItemData id = itemDataDao.findByEventCrfItemName(eventCrf.getEventCrfId(), parentElementName);
        return id;
    }

    protected ItemData createBlankItemData(UserAccount user) {
        Item item = itemDao.findByNameCrfId(parentElementName, crfVersion.getCrf().getCrfId());
        itemData = new ItemData();
        itemData.setItem(item);
        itemData.setEventCrf(eventCrf);
        itemData.setValue("");
        itemData.setDateCreated(new Date());
        itemData.setStatus(Status.AVAILABLE);
        // TODO this value should change once you have an ordinal attribute specified in the query
        itemData.setOrdinal(1);
        itemData.setUserAccount(user);
        itemData.setDeleted(false);
        itemDataDao.saveOrUpdate(itemData);
        return itemData;
    }

    private DiscrepancyNote findQueryParent() {
        DiscrepancyNote parentDiscrepancyNote = discrepancyNoteDao.findParentQueryByItemData(itemData.getItemDataId());
        return parentDiscrepancyNote;
    }
    private void saveQueryItemDatamap() {
        // Create Mapping for new Discrepancy Note
        DnItemDataMapId dnItemDataMapId = new DnItemDataMapId();
        dnItemDataMapId.setDiscrepancyNoteId(dn.getDiscrepancyNoteId());
        dnItemDataMapId.setItemDataId(itemData.getItemDataId());
        dnItemDataMapId.setStudySubjectId(container.getSubject().getStudySubjectId());
        dnItemDataMapId.setColumnName("value");

        DnItemDataMap mapping = new DnItemDataMap();
        mapping.setDnItemDataMapId(dnItemDataMapId);
        mapping.setItemData(itemData);
        mapping.setStudySubject(container.getSubject());
        mapping.setActivated(false);
        mapping.setDiscrepancyNote(dn);
        dnItemDataMapDao.saveOrUpdate(mapping);
/*
        DiscrepancyNote itemParentNote = discrepancyNoteDao.findByDiscrepancyNoteId(dn.getParentDiscrepancyNote().getDiscrepancyNoteId());
        itemParentNote.setResolutionStatus(resStatus);
        itemParentNote.setUserAccount(container.getUser());
        discrepancyNoteDao.saveOrUpdate(itemParentNote);*/
    }
    public String getQueryAttribute(Node itemNode) {
        this.itemNode = itemNode;
        this.parentElementName = QueryService.super.getQueryAttribute(itemNode);
        return parentElementName;
    }

    private void prepareEmail() throws Exception {
        StringBuffer message = new StringBuffer();


        message.append(MessageFormat.format(respage.getString("mailDNHeader"), userAccount.getFirstName(),userAccount.getLastName()));
        message.append("<A HREF='" + SQLInitServlet.getField("sysURL.base")
                + "ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.user=" + userAccount.getUserName()
                + "&listNotes_f_entityName=" + parentElementName
                + "'>" + SQLInitServlet.getField("sysURL.base") + "</A><BR/>");
        message.append(respage.getString("you_received_this_from"));

        message.append(respage.getString("email_body_separator"));
        message.append(respage.getString("disc_note_info"));
        message.append(respage.getString("email_body_separator"));
        message.append(MessageFormat.format(respage.getString("mailDNParameters1"), dn.getDescription(), dn.getDetailedNotes(), userAccount.getUserName()));
        message.append(respage.getString("email_body_separator"));
        message.append(respage.getString("entity_information"));
        message.append(respage.getString("email_body_separator"));
        message.append(MessageFormat.format(respage.getString("mailDNParameters2"), dn.getStudy().getName(), dn.getDescription()));

        if (!("studySub".equalsIgnoreCase(dn.getEntityType())
                || "subject".equalsIgnoreCase(dn.getEntityType()))) {
            message.append(MessageFormat.format(respage.getString("mailDNParameters3"), container.getStudyEvent().getStudyEventDefinition().getName()));
            if (!"studyEvent".equalsIgnoreCase(dn.getEntityType())) {
                message.append(MessageFormat.format(respage.getString("mailDNParameters4"), container.getCrfVersion().getCrf().getName()));
                if (!"eventCrf".equalsIgnoreCase(dn.getEntityType())) {
                    message.append(MessageFormat.format(respage.getString("mailDNParameters6"), parentElementName));
                }
            }
        }

        message.append(respage.getString("email_body_separator"));
        message.append(MessageFormat.format(respage.getString("mailDNThanks"), dn.getStudy().getName()));
        message.append(respage.getString("email_body_separator"));
        message.append(respage.getString("disclaimer"));
        message.append(respage.getString("email_body_separator"));
        message.append(respage.getString("email_footer"));

        String emailBodyString = message.toString();
        try {
            sendEmail(userAccount.getEmail().trim(), EmailEngine.getAdminEmail(), MessageFormat.format(respage.getString("mailDNSubject"),
                    dn.getStudy().getName(), parentElementName), emailBodyString, true, null,
                    null, true);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }

    }
    public Boolean sendEmail(String to, String from, String subject, String body, Boolean htmlEmail, String successMessage, String failMessage,
            Boolean sendMessage) throws Exception {
        Boolean messageSent = true;
        try {
            JavaMailSenderImpl mailSender = (JavaMailSenderImpl) appContext.getBean("mailSender");
            //@pgawade 09-Feb-2012 #issue 13201 - setting the "mail.smtp.localhost" property to localhost when java API is not able to
            //retrieve the host name
            Properties javaMailProperties = mailSender.getJavaMailProperties();
            if(null != javaMailProperties){
                if (javaMailProperties.get("mail.smtp.localhost") == null || ((String)javaMailProperties.get("mail.smtp.localhost")).equalsIgnoreCase("") ){
                    javaMailProperties.put("mail.smtp.localhost", "localhost");
                }
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, htmlEmail);
            helper.setFrom(from);
            helper.setTo(processMultipleImailAddresses(to.trim()));
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(mimeMessage);

            logger.debug("Email sent successfully on {}", new Date());
        } catch (MailException me) {
            me.printStackTrace();
            logger.debug("Email could not be sent on {} due to: {}", new Date(), me.toString());
            messageSent = false;
        }
        return messageSent;
    }

    private InternetAddress[] processMultipleImailAddresses(String to) throws MessagingException {
        ArrayList<String> recipientsArray = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(to, ",");
        while (st.hasMoreTokens()) {
            recipientsArray.add(st.nextToken());
        }

        int sizeTo = recipientsArray.size();
        InternetAddress[] addressTo = new InternetAddress[sizeTo];
        for (int i = 0; i < sizeTo; i++) {
            addressTo[i] = new InternetAddress(recipientsArray.get(i).toString());
        }
        return addressTo;

    }
}
