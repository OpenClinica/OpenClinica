package org.akaza.openclinica.bean.rule;

import java.util.ArrayList;
import java.util.Date;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.rule.RuleDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author Krikor Krumlian
 */

public class RuleExecutionBusinessObject {

    private final SessionManager sm;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected StudyBean currentStudy;
    protected UserAccountBean ub;

    public RuleExecutionBusinessObject(SessionManager sm, StudyBean currentStudy, UserAccountBean ub) {
        this.sm = sm;
        this.currentStudy = currentStudy;
        this.ub = ub;
    }

    public void runRule(int eventCrfId) {
        // int eventCrfId = 11;
        EventCRFBean eventCrfBean = getEventCRFBean(eventCrfId);
        RuleSetBean ruleSetBean = getRuleSetBean(eventCrfBean);
        ArrayList<RuleBean> rules = getRuleBeans(ruleSetBean);
        for (RuleBean rule : rules) {
            initializeRule(rule);
        }
    }

    public void initializeRule(RuleBean rule) {
        // source data
        // ItemDataBean sourceItemDataBean = rule.getSourceItemDataBean();
        ItemDataBean sourceItemDataBean = null;

        // target data
        // ItemDataBean targetItemDataBean = rule.getTargetItemDataBean();
        ItemDataBean targetItemDataBean = null;

        // fireRules on source & target
        // TODO KK FIX HERE
        boolean sourceResult = true;// fireRule(sourceItemDataBean,rule.getSourceItemValue(),sourceItemFormMetadataBean,rule.getSourceOperator());
        boolean targetResult = true;// fireRule(targetItemDataBean,rule.getTargetItemValue(),targetItemFormMetadataBean,rule.getTargetOperator());

        if (sourceResult && targetResult) {
            // We are good
        }
        if (sourceResult == true && targetResult == false) {
            // file a descrepancy Note
            createDiscrepancyNote(rule.toString(), targetItemDataBean, sourceItemDataBean);
        }

    }

    private void createDiscrepancyNote(String description, ItemDataBean targetItemDataBean, ItemDataBean sourceItemDataBean) {

        DiscrepancyNoteBean note = new DiscrepancyNoteBean();
        note.setDescription(description);
        note.setDetailedNotes("");
        note.setOwner(ub);
        note.setCreatedDate(new Date());
        note.setResolutionStatusId(1);
        note.setDiscrepancyNoteTypeId(1);
        // note.setParentDnId(parentId);
        // note.setField(field);
        note.setEntityId(targetItemDataBean.getId());
        note.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
        note.setColumn("value");
        note.setStudyId(currentStudy.getId());

        DiscrepancyNoteDAO discrepancyNoteDao = new DiscrepancyNoteDAO(sm.getDataSource());
        note = (DiscrepancyNoteBean) discrepancyNoteDao.create(note);
        discrepancyNoteDao.createMapping(note);

    }

    // These are dao mostly calls see how to reduce redundancy
    private EventCRFBean getEventCRFBean(int eventCrfBeanId) {
        EventCRFDAO eventCrfDao = new EventCRFDAO(sm.getDataSource());
        return eventCrfBeanId > 0 ? (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId) : null;
    }

    private RuleSetBean getRuleSetBean(EventCRFBean eventCrfBean) {
        // RuleSetDAO ruleSetDao = new RuleSetDAO(sm.getDataSource());
        return null;
    }

    private ArrayList<RuleBean> getRuleBeans(RuleSetBean ruleSet) {
        RuleDAO ruleDao = new RuleDAO(sm.getDataSource());
        return ruleSet != null ? ruleDao.findByRuleSet(ruleSet) : new ArrayList<RuleBean>();
    }

}
