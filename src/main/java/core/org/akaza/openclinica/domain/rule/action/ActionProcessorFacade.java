package core.org.akaza.openclinica.domain.rule.action;

import core.org.akaza.openclinica.dao.hibernate.RuleActionRunLogDao;
import core.org.akaza.openclinica.domain.rule.RuleSetBean;
import core.org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import core.org.akaza.openclinica.exception.OpenClinicaSystemException;
import core.org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.sql.DataSource;

public class ActionProcessorFacade {

    public static ActionProcessor getActionProcessor(ActionType actionType, DataSource ds, JavaMailSenderImpl mailSender,
            DynamicsMetadataService itemMetadataService, RuleSetBean ruleSet, RuleActionRunLogDao ruleActionRunLogDao, RuleSetRuleBean ruleSetRule)
            throws OpenClinicaSystemException {

        // Removed the old item-based action processors from here, just notification action is left.
        // Event action was never processed here, it seems to be handled elsewhere. jmcinerney - May 2020
        switch (actionType) {
        case NOTIFICATION:
            return new NotificationActionProcessor(ds, mailSender, ruleSetRule);
        default:
            throw new OpenClinicaSystemException("actionType", "Unrecognized action type!");
        }
    }
}
