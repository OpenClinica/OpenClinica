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
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.form.xform.QueriesBean;
import org.akaza.openclinica.core.form.xform.QueryBean;
import org.akaza.openclinica.dao.hibernate.DiscrepancyNoteDao;
import org.akaza.openclinica.dao.hibernate.DnItemDataMapDao;
import org.akaza.openclinica.dao.hibernate.ItemDao;
import org.akaza.openclinica.dao.hibernate.ItemDataDao;
import org.akaza.openclinica.dao.hibernate.ResolutionStatusDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.DiscrepancyNote;
import org.akaza.openclinica.domain.datamap.DiscrepancyNoteType;
import org.akaza.openclinica.domain.datamap.DnItemDataMap;
import org.akaza.openclinica.domain.datamap.DnItemDataMapId;
import org.akaza.openclinica.domain.datamap.Item;
import org.akaza.openclinica.domain.datamap.ItemData;
import org.akaza.openclinica.domain.user.UserAccount;
import org.akaza.openclinica.web.SQLInitServlet;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
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
    private StudyDao studyDao;

    @Autowired
    private ApplicationContext appContext;

    @Override
    public void process(QueryServiceHelperBean helperBean, SubmissionContainer container, Node itemNode, int itemOrdinal) throws Exception {
        helperBean.setContainer(container);
        helperBean.setItemOrdinal(itemOrdinal);
        helperBean.setItemNode(itemNode);
        helperBean.setItemData(getItemData(helperBean));
        if (helperBean.getItemData() == null) {
            helperBean.setItemData(createBlankItemData(helperBean));
        }
        QueriesBean queries = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            queries = objectMapper.readValue(itemNode.getTextContent(), QueriesBean.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        }

        List<Integer> idList = new ArrayList();
        List<QueryBean> qBeans = queries.getQueries();
        QueryBean queryBean = null;
        DiscrepancyNote childDN = null;
        DiscrepancyNote parentDN = null;
        List<DiscrepancyNote> childDns = null;
        if (qBeans.size() > 0) {
            for (QueryBean qBean : qBeans) {
                idList.add(Integer.valueOf(qBean.getId()));
            }
            Collections.reverse(idList);
            queryBean = qBeans.get(0);
            parentDN = findQueryParent(helperBean);

            if (parentDN == null) {
                parentDN = createQuery(helperBean, queryBean);
                parentDN = discrepancyNoteDao.saveOrUpdate(parentDN);
                helperBean.setDn(parentDN);
                saveQueryItemDatamap(helperBean);
            }

            childDns = findChildQueries(helperBean);
            if (childDns.size() < qBeans.size()) {

                // Enketo passes JSON "id" attribute for unsubmitted queries only
                // if (StringUtils.isEmpty(queryBean.getId())){

                childDN = createQuery(helperBean, queryBean);
                childDN.setParentDiscrepancyNote(parentDN);
                childDN = discrepancyNoteDao.saveOrUpdate(childDN);

                parentDN.setUserAccount(childDN.getUserAccount());
                parentDN = discrepancyNoteDao.saveOrUpdate(parentDN);

                helperBean.setDn(childDN);
                saveQueryItemDatamap(helperBean);
                handleEmailNotification(helperBean, queryBean);
            }
        }
    }

    private DiscrepancyNote createQuery(QueryServiceHelperBean helperBean, QueryBean queryBean) throws Exception {
        DiscrepancyNote dn = new DiscrepancyNote();
        dn.setStudy(helperBean.getContainer().getStudy());
        dn.setEntityType("itemData");
        dn.setDescription("description");

        dn.setDetailedNotes(queryBean.getComment());
        dn.setDiscrepancyNoteType(new DiscrepancyNoteType(3));

        if (queryBean.getStatus().equals("new")) {
            dn.setResolutionStatus(resolutionStatusDao.findById(1));
        } else if (queryBean.getStatus().equals("updated")) {
            dn.setResolutionStatus(resolutionStatusDao.findById(2));
        } else if (queryBean.getStatus().equals("closed")) {
            dn.setResolutionStatus(resolutionStatusDao.findById(4));
        }

        String assignedTo = queryBean.getAssigned_to();
        if (!StringUtils.isEmpty(assignedTo)) {
            int endIndex = assignedTo.indexOf(")");
            int begIndex = assignedTo.indexOf("(");
            String userName = assignedTo.substring(begIndex + 1, endIndex);
            UserAccount userAccount = userAccountDao.findByUserName(userName);
            helperBean.setUserAccount(userAccount);
            dn.setUserAccount(userAccount);
        }
        dn.setUserAccountByOwnerId(helperBean.getContainer().getUser());
        // create itemData when a query is created without an autosaved itemdata
        if (helperBean.getItemData() == null) {
            helperBean.setItemData(createBlankItemData(helperBean));
        }
        dn.setDateCreated(new Date());
        return dn;
    }

    private void handleEmailNotification(QueryServiceHelperBean helperBean, QueryBean queryBean) throws Exception {
        if (queryBean.getNotify() != true) {
            return;
        }
        prepareEmail(helperBean);
    }

    private ItemData getItemData(QueryServiceHelperBean helperBean) {
        ItemData id = itemDataDao.findByEventCrfItemName(helperBean.getContainer().getEventCrf().getEventCrfId(), helperBean.getParentElementName(),
                helperBean.getItemOrdinal());
        return id;
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
        itemDataDao.saveOrUpdate(itemData);
        return itemData;
    }

    private DiscrepancyNote findQueryParent(QueryServiceHelperBean helperBean) {
        DiscrepancyNote parentDiscrepancyNote = discrepancyNoteDao.findParentQueryByItemData(helperBean.getItemData().getItemDataId());
        return parentDiscrepancyNote;
    }

    private List<DiscrepancyNote> findChildQueries(QueryServiceHelperBean helperBean) {
        List<DiscrepancyNote> childDiscrepancyNotes = discrepancyNoteDao.findChildQueriesByItemData(helperBean.getItemData().getItemDataId());
        return childDiscrepancyNotes;
    }

    private void saveQueryItemDatamap(QueryServiceHelperBean helperBean) {
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

        message.append(
                MessageFormat.format(respage.getString("mailDNHeader"), helperBean.getUserAccount().getFirstName(), helperBean.getUserAccount().getLastName()));
        message.append("<A HREF='" + SQLInitServlet.getField("sysURL.base") + "ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.user="
                + helperBean.getUserAccount().getUserName() + "&listNotes_f_entityName=" + helperBean.getParentElementName() + "'>"
                + SQLInitServlet.getField("sysURL.base") + "</A><BR/>");
        message.append(respage.getString("you_received_this_from"));
        message.append(respage.getString("email_body_separator"));
        message.append(respage.getString("disc_note_info"));
        message.append(respage.getString("email_body_separator"));
        message.append(MessageFormat.format(respage.getString("mailDNParameters1"), helperBean.getDn().getDescription(), helperBean.getDn().getDetailedNotes(),
                helperBean.getUserAccount().getUserName()));
        message.append(respage.getString("email_body_separator"));
        message.append(respage.getString("entity_information"));
        message.append(respage.getString("email_body_separator"));
        message.append(
                MessageFormat.format(respage.getString("mailDNParameters2"), helperBean.getDn().getStudy().getName(), helperBean.getDn().getDescription()));

        if (!("studySub".equalsIgnoreCase(helperBean.getDn().getEntityType()) || "subject".equalsIgnoreCase(helperBean.getDn().getEntityType()))) {
            message.append(MessageFormat.format(respage.getString("mailDNParameters3"),
                    helperBean.getContainer().getStudyEvent().getStudyEventDefinition().getName()));
            if (!"studyEvent".equalsIgnoreCase(helperBean.getDn().getEntityType())) {
                message.append(MessageFormat.format(respage.getString("mailDNParameters4"), helperBean.getContainer().getCrfVersion().getCrf().getName()));
                if (!"eventCrf".equalsIgnoreCase(helperBean.getDn().getEntityType())) {
                    message.append(MessageFormat.format(respage.getString("mailDNParameters6"), helperBean.getParentElementName()));
                }
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
                e.printStackTrace();
            }
            return null;
        }).toArray(InternetAddress[]::new);

        return addressTo;

    }
}
