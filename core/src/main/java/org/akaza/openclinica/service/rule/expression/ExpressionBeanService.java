/*
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * OpenClinica is distributed under the
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.service.rule.expression;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.apache.commons.lang.time.DateUtils;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionBeanService {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String SEPERATOR = ".";
    private final String ESCAPED_SEPERATOR = "\\.";
    @SuppressWarnings("unused")
	private final String STUDY_EVENT_DEFINITION_OR_ITEM_GROUP_PATTERN = "[A-Z_0-9]+|[A-Z_0-9]+\\[(ALL|[1-9]\\d*)\\]$";
    private final String STUDY_EVENT_DEFINITION_OR_ITEM_GROUP_PATTERN_NO_ALL = "[A-Z_0-9]+|[A-Z_0-9]+\\[[1-9]\\d*\\]$";
    @SuppressWarnings("unused")
	private final String STUDY_EVENT_DEFINITION_OR_ITEM_GROUP_PATTERN_WITH_ORDINAL = "[A-Z_0-9]+\\[(END|ALL|[1-9]\\d*)\\]$";
    private final String STUDY_EVENT_DEFINITION_OR_ITEM_GROUP_PATTERN_WITH_END = "[A-Z_0-9]+|[A-Z_0-9]+\\[(END|ALL|[1-9]\\d*)\\]$";
    private final String PRE = "[A-Z_0-9]+\\[";
    private final String POST = "\\]";
    private final String CRF_OID_OR_ITEM_DATA_PATTERN = "[A-Z_0-9]+";
    private final String BRACKETS_AND_CONTENTS = "\\[(END|ALL|[1-9]\\d*)\\]";
    private final String ALL_IN_BRACKETS = "ALL";
    private final String OPENNIG_BRACKET = "[";
    private final String CLOSING_BRACKET = "]";

    DataSource ds;
    Pattern[] pattern;
    Pattern[] rulePattern;
    Pattern[] ruleActionPattern;
    ExpressionBeanObjectWrapper expressionBeanWrapper;

    private ItemDAO itemDao;
    private StudyEventDefinitionDAO studyEventDefinitionDao;
    private EventDefinitionCRFDAO eventDefinitionCRFDao;
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    private ItemGroupMetadataDAO itemGroupMetadataDao;
    private EventCRFDAO eventCRFDao;
    private ItemGroupDAO itemGroupDao;
    private CRFDAO crfDao;
    private CRFVersionDAO crfVersionDao;
    private ItemDataDAO itemDataDao;
    private StudyEventDAO studyEventDao;

    /*
     * The variables below are used as a small Cache so that we don't go to the
     * database every time we want to get an Object by it's OID. This is a very
     * stripped down cache which will help performance in a single
     * request/response cycle.
     */
    private HashMap<String, StudyEventDefinitionBean> studyEventDefinitions;
    private HashMap<String, ItemGroupBean> itemGroups;
    private HashMap<String, ItemBean> items;

    public ExpressionBeanService(DataSource ds) {
        init(ds, null);
    }

    public ExpressionBeanService(ExpressionBeanObjectWrapper expressionBeanWrapper) {
        init(expressionBeanWrapper.getDs(), expressionBeanWrapper);
    }

    private void init(DataSource ds, ExpressionBeanObjectWrapper expressionBeanWrapper) {
        //TODO add stuff here
        this.ds = ds;
        this.expressionBeanWrapper = expressionBeanWrapper;

    }

    public String evaluateExpression(String test){
        String value =null;
        String temp = null;
        String oid = null;
        int index = 0;
        // TODO fix this
        System.out.println("TEST  :: " + test);
        if (test.startsWith("SS")){
            value = String.valueOf(MVEL.eval(test.replaceFirst("SS.",""), expressionBeanWrapper.getStudySubjectBean()));
           // MVEL.eval("label", ctx)
            System.out.println("Val::::"+value);
        }
        else if(test.startsWith("SE")){
        	StudyEventDefinitionBean studyEventDefinitionBean =null;
        	StudyEventDefinitionDAO studyEventDefDao = new StudyEventDefinitionDAO<String, ArrayList>(this.ds);
        	StudyEventDAO studyEventDao = new StudyEventDAO(this.ds);
        	SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");
        	StudyEventBean studyEvent = null;
        	index = test.indexOf(".");
        	oid = test.substring(0,index);
        	temp = test.substring(index,test.length());//property(startDate for ex)
        	
        	//studyEventDefinitionBean.setOid(oid);
        	studyEventDefinitionBean = studyEventDefDao.findByOid(oid);
        	studyEvent = (StudyEventBean) studyEventDao.findByStudySubjectIdAndDefinitionIdAndOrdinal(expressionBeanWrapper.getStudySubjectBean().getId(), studyEventDefinitionBean.getId(), 1);
        	//grab study event name
        	System.out.println("vl"+ MVEL.getProperty(temp,studyEvent));
        	Object obj =  MVEL.getProperty(temp,studyEvent);
  
        	if(temp.contains("date"))//not a good way of recognizing date fields, but works for now.
        	{
        		
        		value = String.valueOf(DateUtils.truncate((java.sql.Timestamp)obj, Calendar.DATE));
        		value = sdf.format(DateUtils.truncate((java.sql.Timestamp)obj, Calendar.DATE));
        	}
        	else
        		value = String.valueOf(obj);
        	
        	value = value.replace(("00:00:00.0"),"");
           value = value.trim();
        			
        	
        
				//value = (String) (((obj.getClass()).isInstance((java.util.Date.class)))?(DateUtils.truncate((java.sql.Date)obj,Calendar.DATE)):String.valueOf(obj));
			
        //	value = String.valueOf( MVEL.getProperty(temp,studyEvent));
        	
        	//eventName = temp.substring(temp.indexOf("."));
        	logger.info("value now is::"+value);
        	logger.info("OID::"+oid);
        	
        }
        return value;
    }




    private boolean match(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    private ItemDAO getItemDao() {
       // itemDao = this.itemDao != null ? itemDao : new ItemDAO(ds);
        return new ItemDAO(ds);
    }

    private ItemDataDAO getItemDataDao() {
    //    itemDataDao = this.itemDataDao != null ? itemDataDao : new ItemDataDAO(ds);
        return  new ItemDataDAO(ds);
    }

    private CRFVersionDAO getCrfVersionDao() {
    //    crfVersionDao = this.crfVersionDao != null ? crfVersionDao : new CRFVersionDAO(ds);
        return new CRFVersionDAO(ds);
    }

    private CRFDAO getCrfDao() {
      //  crfDao = this.crfDao != null ? crfDao : new CRFDAO(ds);
        return new CRFDAO(ds);
    }

    private ItemGroupDAO getItemGroupDao() {
        //itemGroupDao = this.itemGroupDao != null ? itemGroupDao : new ItemGroupDAO(ds);
        return new ItemGroupDAO(ds);
    }

    private ItemGroupMetadataDAO getItemGroupMetadataDao() {
        //itemGroupMetadataDao = this.itemGroupMetadataDao != null ? itemGroupMetadataDao : new ItemGroupMetadataDAO(ds);
        //return itemGroupMetadataDao;
        return new ItemGroupMetadataDAO(ds);
    }

    private EventDefinitionCRFDAO getEventDefinitionCRFDao() {
    //    eventDefinitionCRFDao = this.eventDefinitionCRFDao != null ? eventDefinitionCRFDao : new EventDefinitionCRFDAO(ds);
  //      return eventDefinitionCRFDao;
        return  new EventDefinitionCRFDAO(ds);
    }

    private StudyEventDefinitionDAO getStudyEventDefinitionDao() {
      //  studyEventDefinitionDao = this.studyEventDefinitionDao != null ? studyEventDefinitionDao : new StudyEventDefinitionDAO(ds);
       // return studyEventDefinitionDao;
        return new StudyEventDefinitionDAO(ds);
    }

    private StudyEventDAO getStudyEventDao() {
    //    studyEventDao = this.studyEventDao != null ? studyEventDao : new StudyEventDAO(ds);
     //   return studyEventDao;
        return  new StudyEventDAO(ds);
    }

    private EventCRFDAO getEventCRFDao() {
   //     eventCRFDao = this.eventCRFDao != null ? eventCRFDao : new EventCRFDAO(ds);
    //    return eventCRFDao;
        return  new EventCRFDAO(ds);
    }

    public void setExpressionBeanWrapper(ExpressionBeanObjectWrapper expressionBeanWrapper) {
        this.expressionBeanWrapper = expressionBeanWrapper;
    }

}
