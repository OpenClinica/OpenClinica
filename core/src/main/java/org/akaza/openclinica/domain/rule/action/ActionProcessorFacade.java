package org.akaza.openclinica.domain.rule.action;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.sql.DataSource;

public class ActionProcessorFacade {

    public static ActionProcessor getActionProcessor(ActionType actionType, DataSource ds, JavaMailSenderImpl mailSender) throws OpenClinicaSystemException {
        switch (actionType) {
        case FILE_DISCREPANCY_NOTE:
            return new DiscrepancyNoteActionProcessor(ds);
        case EMAIL:
            return new EmailActionProcessor(ds, mailSender);
        case SHOW:
            return new ShowActionProcessor(ds);
        default:
            throw new OpenClinicaSystemException("actionType", "Unrecognized action type!");
        }
    }
}
