package core.org.akaza.openclinica.bean.rule.action;

import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.domain.datamap.Study;
import core.org.akaza.openclinica.service.managestudy.DiscrepancyNoteService;

import javax.sql.DataSource;

public class DiscrepancyNoteActionProcessor implements ActionProcessor {

    DataSource ds;
    DiscrepancyNoteService discrepancyNoteService;

    public DiscrepancyNoteActionProcessor(DataSource ds) {
        this.ds = ds;
    }

    public void execute(RuleActionBean ruleAction, int itemDataBeanId, String itemData, Study currentStudy, UserAccountBean ub, Object... arguments) {
        getDiscrepancyNoteService().saveFieldNotes(ruleAction.getCuratedMessage(), itemDataBeanId, itemData, currentStudy, ub);
    }

    private DiscrepancyNoteService getDiscrepancyNoteService() {
        discrepancyNoteService = this.discrepancyNoteService != null ? discrepancyNoteService : new DiscrepancyNoteService(ds);
        return discrepancyNoteService;
    }

}
