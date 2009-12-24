package org.akaza.openclinica.bean.rule;

import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.rule.RuleDAO;
import org.akaza.openclinica.dao.rule.RuleSetDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * @author Krikor Krumlian
 */

public class RuleExecutionBusinessObject {

    private SessionManager sm;
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
        ItemBean sourceItemBean = getItemBean(sourceItemDataBean);
        ItemFormMetadataBean sourceItemFormMetadataBean = getItemFormMetadaBean(sourceItemBean);

        // target data
        // ItemDataBean targetItemDataBean = rule.getTargetItemDataBean();
        ItemDataBean targetItemDataBean = null;
        ItemBean targetItemBean = getItemBean(targetItemDataBean);
        ItemFormMetadataBean targetItemFormMetadataBean = getItemFormMetadaBean(targetItemBean);

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

    private boolean fireRule(ItemDataBean itemDataBean, String valueProvidedInRule, ItemFormMetadataBean itemFormMetadataBean, Operator operator) {

        ResponseType rt = itemFormMetadataBean.getResponseSet().getResponseType();

        if (rt.equals(ResponseType.TEXT) || rt.equals(ResponseType.TEXTAREA)) {
            StringEditCheck editCheck = new StringEditCheck(itemDataBean.getValue(), valueProvidedInRule, operator);
            boolean result = editCheck.check();
            logger.info("  TEXT or TEXTAREA : Edit Check Result : " + result);
            return result;
        }

        if (rt.equals(ResponseType.RADIO) || rt.equals(ResponseType.SELECT)) {
            String theValue = matchValueWithOptions(valueProvidedInRule, itemFormMetadataBean.getResponseSet().getOptions());
            StringEditCheck editCheck = new StringEditCheck(itemDataBean.getValue(), theValue, operator);
            boolean result = editCheck.check();
            logger.info("  RADIO or SELECT : Edit Check Result : " + result);
            return result;
        }
        return false;
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
        note.setEntityType("ItemData");
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
        RuleSetDAO ruleSetDao = new RuleSetDAO(sm.getDataSource());
        return null;
    }

    private ArrayList<RuleBean> getRuleBeans(RuleSetBean ruleSet) {
        RuleDAO ruleDao = new RuleDAO(sm.getDataSource());
        return ruleSet != null ? ruleDao.findByRuleSet(ruleSet) : new ArrayList<RuleBean>();
    }

    private ItemBean getItemBean(ItemDataBean itemDataBean) {
        ItemDAO itemDao = new ItemDAO(sm.getDataSource());
        return itemDataBean != null ? (ItemBean) itemDao.findByPK(itemDataBean.getItemId()) : null;
    }

    private ItemFormMetadataBean getItemFormMetadaBean(ItemBean itemBean) {
        ArrayList<ItemFormMetadataBean> itemFormMetadataBeans = null;
        ItemFormMetadataDAO itemFormMetadataDao = new ItemFormMetadataDAO(sm.getDataSource());
        itemFormMetadataBeans = itemBean != null ? itemFormMetadataDao.findAllByItemId(itemBean.getId()) : new ArrayList<ItemFormMetadataBean>();
        return !itemFormMetadataBeans.isEmpty() ? itemFormMetadataBeans.get(0) : null;
    }

    // Utility methods
    private String matchValueWithOptions(String value, List options) {
        String returnedValue = null;
        if (!options.isEmpty()) {
            for (Object responseOption : options) {
                if (((ResponseOptionBean) responseOption).getText().equals(value)) {
                    return ((ResponseOptionBean) responseOption).getValue();

                }
            }
        }
        return returnedValue;
    }

}
