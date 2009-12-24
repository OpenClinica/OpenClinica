package org.akaza.openclinica.bean.rule.action;

import org.akaza.openclinica.exception.OpenClinicaSystemException;

import javax.sql.DataSource;

public class ActionProcessorFacade {

    public static ActionProcessor getActionProcessor(ActionType actionType, DataSource ds) throws OpenClinicaSystemException {
        switch (actionType) {
        case FILE_DISCREPANCY_NOTE:
            return new DiscrepancyNoteActionProcessor(ds);
        case EMAIL:
            return new EmailActionProcessor(ds);
        default:
            throw new OpenClinicaSystemException("actionType", "Unrecognized action type!");
        }
    }
}
