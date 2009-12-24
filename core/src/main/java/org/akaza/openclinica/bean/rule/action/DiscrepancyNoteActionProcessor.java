package org.akaza.openclinica.bean.rule.action;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.service.managestudy.DiscrepancyNoteService;

import javax.sql.DataSource;

public class DiscrepancyNoteActionProcessor implements ActionProcessor {

    DataSource ds;
    DiscrepancyNoteService discrepancyNoteService;

    public DiscrepancyNoteActionProcessor(DataSource ds) {
        this.ds = ds;
    }

    public void execute(RuleActionBean ruleAction, int itemDataBeanId, String itemData, StudyBean currentStudy, UserAccountBean ub, Object... arguments) {
        getDiscrepancyNoteService().saveFieldNotes(ruleAction.getCuratedMessage(), itemDataBeanId, itemData, currentStudy, ub);
    }

    private DiscrepancyNoteService getDiscrepancyNoteService() {
        discrepancyNoteService = this.discrepancyNoteService != null ? discrepancyNoteService : new DiscrepancyNoteService(ds);
        return discrepancyNoteService;
    }

}
