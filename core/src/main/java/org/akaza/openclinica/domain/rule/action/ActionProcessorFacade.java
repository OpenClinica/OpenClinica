package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.sql.DataSource;

public class ActionProcessorFacade {

    public static ActionProcessor getActionProcessor(ActionType actionType, DataSource ds, JavaMailSenderImpl mailSender,
            DynamicsMetadataService itemMetadataService, RuleSetBean ruleSet) throws OpenClinicaSystemException {
        switch (actionType) {
        case FILE_DISCREPANCY_NOTE:
            return new DiscrepancyNoteActionProcessor(ds);
        case EMAIL:
            return new EmailActionProcessor(ds, mailSender);
        case SHOW:
            return new ShowActionProcessor(ds, itemMetadataService);
        case HIDE:
            return new HideActionProcessor(ds, itemMetadataService);
        case INSERT:
            return new InsertActionProcessor(ds, itemMetadataService, ruleSet);
        default:
            throw new OpenClinicaSystemException("actionType", "Unrecognized action type!");
        }
    }
}
