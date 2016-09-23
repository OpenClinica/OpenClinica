package org.akaza.openclinica.service.rule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.patterns.ocobserver.OnStudyEventUpdated;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

@Service
public class RuleSetListenerService implements ApplicationListener<OnStudyEventUpdated>  {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());

	private RuleSetService ruleSetService;

	private RuleSetDao ruleSetDao;




@Override
	public void onApplicationEvent(final OnStudyEventUpdated event) {
	
		LOGGER.debug("listening");
	if (event.getContainer().getChangeDetails().getStartDateChanged() || event.getContainer().getChangeDetails().getStatusChanged()){ 
       
		StudyEvent studyEvent = event.getContainer().getEvent();
		
		Integer studyEventDefId = studyEvent.getStudyEventDefinition().getStudyEventDefinitionId();
		Integer studyEventOrdinal = studyEvent.getSampleOrdinal();
		Integer userId = studyEvent.getUpdateId();
		
		if(userId==null && studyEvent.getUserAccount()!=null ) userId=  studyEvent.getUserAccount().getUserId();
		  
		StudyEventBean studyEventBean = new StudyEventBean();
		studyEventBean.setId(studyEvent.getStudyEventId());

		ArrayList<RuleSetBean> ruleSets = (ArrayList<RuleSetBean>) createRuleSet(studyEventDefId);
		for (RuleSetBean ruleSet : ruleSets){
			ArrayList<RuleSetBean> ruleSetBeans = new ArrayList();		
	            ExpressionBean eBean = new ExpressionBean();
    			eBean.setValue(ruleSet.getTarget().getValue()+".A.B");
    			ruleSet.setTarget(eBean);
    			ruleSet.addExpression(getRuleSetService().replaceSEDOrdinal(ruleSet.getTarget(), studyEventBean));
			ruleSetBeans.add(ruleSet);
			getRuleSetService().runIndividualRulesInBeanProperty(ruleSetBeans, userId,event.getContainer().getChangeDetails() , studyEventOrdinal);
        	}	
		   
		}

}

public RuleSetService getRuleSetService() {
	return ruleSetService;
}


public void setRuleSetService(RuleSetService ruleSetService) {
	this.ruleSetService = ruleSetService;
}


public RuleSetDao getRuleSetDao() {
	return ruleSetDao;
}


public void setRuleSetDao(RuleSetDao ruleSetDao) {
	this.ruleSetDao = ruleSetDao;
}

private List<RuleSetBean> createRuleSet(Integer studyEventDefId) {	
    List<RuleSetBean> ruleSetsDB = new ArrayList<RuleSetBean>();
    List<RuleSetBean> ruleSetCopies = new ArrayList<RuleSetBean>();
	ruleSetsDB = getRuleSetDao().findAllByStudyEventDefIdWhereItemIsNull(studyEventDefId);
	
	for (RuleSetBean ruleSetDB:ruleSetsDB) { 
        RuleSetBean ruleSetCopy = deepCopyRuleSet(ruleSetDB);
	    ruleSetCopies.add(ruleSetCopy);
	}
	return ruleSetCopies;
}

private RuleSetBean deepCopyRuleSet(RuleSetBean ruleSetDB) {
    RuleSetBean ruleSetCopy = null;
    ObjectOutputStream objOutputStream = null;
    ObjectInputStream objInputStream = null;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try {
        objOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objOutputStream.writeObject(ruleSetDB);
        objOutputStream.flush();
        ByteArrayInputStream bin = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        objInputStream = new ObjectInputStream(bin);
        ruleSetCopy = (RuleSetBean) objInputStream.readObject();
    } catch (Exception e){ 
        LOGGER.error(e.getMessage());
        LOGGER.error(ExceptionUtils.getStackTrace(e));
    } finally {
        try {
            objOutputStream.close();
            objInputStream.close();
        } catch (IOException ioe) {
            LOGGER.error(ioe.getMessage());
            LOGGER.error(ExceptionUtils.getStackTrace(ioe));
        }
    }
    return ruleSetCopy;
}

}
