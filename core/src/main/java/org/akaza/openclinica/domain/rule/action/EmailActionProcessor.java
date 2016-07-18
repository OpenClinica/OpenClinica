package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.rulerunner.ExecutionMode;
import org.akaza.openclinica.logic.rulerunner.RuleRunner.RuleRunnerMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

public class EmailActionProcessor implements ActionProcessor {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    DataSource ds;
    EmailEngine emailEngine;
    JavaMailSenderImpl mailSender;
    RuleActionRunLogDao ruleActionRunLogDao;
    RuleSetRuleBean ruleSetRule;

    public EmailActionProcessor(DataSource ds, JavaMailSenderImpl mailSender, RuleActionRunLogDao ruleActionRunLogDao, RuleSetRuleBean ruleSetRule) {
        this.ds = ds;
        this.mailSender = mailSender;
        this.ruleSetRule = ruleSetRule;
        this.ruleActionRunLogDao = ruleActionRunLogDao;
    }

    public RuleActionBean execute(RuleRunnerMode ruleRunnerMode, ExecutionMode executionMode, RuleActionBean ruleAction, ItemDataBean itemDataBean,
            String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {
        switch (executionMode) {
        case DRY_RUN: {
            return ruleAction;
        }

        case SAVE: {
            HashMap<String, String> arg0 = (HashMap<String, String>) arguments[0];
            sendEmail(ruleAction, ub, arg0.get("body"), arg0.get("subject"));
            RuleActionRunLogBean ruleActionRunLog =
                new RuleActionRunLogBean(ruleAction.getActionType(), itemDataBean, itemDataBean.getValue(), ruleSetRule.getRuleBean().getOid());
            ruleActionRunLogDao.saveOrUpdate(ruleActionRunLog);
            return null;
        }
        default:
            return null;
        }
    }

    private void sendEmail(RuleActionBean ruleAction, UserAccountBean ub, String body, String subject) throws OpenClinicaSystemException {

        logger.info("Sending email...");
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setFrom(EmailEngine.getAdminEmail());
            helper.setTo(processMultipleImailAddresses(((EmailActionBean) ruleAction).getTo().trim()));
            helper.setSubject(subject);
            helper.setText(body);

            mailSender.send(mimeMessage);
            System.out.println("Sending Email thru Email Action");
            logger.debug("Email sent successfully on {}", new Date());
        } catch (MailException me) {
            logger.error("Email could not be sent");
            throw new OpenClinicaSystemException(me.getMessage());
        } catch (MessagingException me) {
            logger.error("Email could not be sent");
            throw new OpenClinicaSystemException(me.getMessage());
        }
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
