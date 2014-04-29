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

import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.StudyEventDefinitionDao;

import org.akaza.openclinica.domain.datamap.StudyEvent;
import org.akaza.openclinica.domain.datamap.StudyEventDefinition;
import org.akaza.openclinica.domain.rule.expression.ExpressionBeanObjectWrapper;
import org.apache.commons.lang.time.DateUtils;
//import org.mvel2.MVEL;
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

    public static String STUDYEVENTKEY="SE";

    /*
     * The variables below are used as a small Cache so that we don't go to the
     * database every time we want to get an Object by it's OID. This is a very
     * stripped down cache which will help performance in a single
     * request/response cycle.
     */
    private HashMap<String, StudyEventDefinition> studyEventDefinitions;
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

    private boolean checkIfForScheduling(String value){
    	boolean test = false;
    	if(value.startsWith(STUDYEVENTKEY)&& (value.endsWith(ExpressionService.STARTDATE)||value.endsWith(ExpressionService.STATUS)))
    	{
    		test = true;
    	}
    	return test;
    }
    
    public String evaluateExpression(String test){
        String value =null;
        String temp = null;
        String oid = null;
        int index = 0;
        // TODO fix this
        System.out.println("TEST  :: " + test);
        /*if (test.startsWith("SS")){
            value = String.valueOf(MVEL.eval(test.replaceFirst("SS.",""), expressionBeanWrapper.getStudySubjectBean()));
           // MVEL.eval("label", ctx)
            System.out.println("Val::::"+value);
        }
        else
        	*/if(checkIfForScheduling(test)){
        	
        	
        	Integer subjectId = expressionBeanWrapper.getStudySubjectBean().getSubjectId();
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");//TODO: get the format from data format properties.??
        	index = test.indexOf(".");
        	oid = test.substring(0,index);
        	temp = test.substring(index,test.length());
        	StudyEvent studyEvent= expressionBeanWrapper.getStudyEventDaoHib().fetchByStudyEventDefOID(oid, subjectId);
        	
        	if(ExpressionService.STARTDATE.endsWith(temp)){
        		value = 				String.valueOf(DateUtils.truncate((java.sql.Timestamp)studyEvent.getDateStart(), Calendar.DATE));
        		value = sdf.format(DateUtils.truncate((java.sql.Timestamp)studyEvent.getDateStart(), Calendar.DATE));
        		value = value.replace(("00:00:00.0"),"");
                value = value.trim();
        	}
        	
        	
        	
        	//StudyEventDefinition studyEventDefinitionBean =null;
        	
        	/*StudyEventDefinitionDao studyEventDefDao = new StudyEventDefinitionDao<String, ArrayList>(this.ds);
        	StudyEventDAO studyEventDao = new StudyEventDAO(this.ds);
        	SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");
        	StudyEvent studyEvent = null;
        	index = test.indexOf(".");
        	oid = test.substring(0,index);
        	temp = test.substring(index,test.length());//property(startDate for ex)
        	
        
        	*/
        	
        	
        /*	//studyEventDefinitionBean.setOid(oid);
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
        	*/
        	
        }
        return value;
    }




    private boolean match(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

  
    public void setExpressionBeanWrapper(ExpressionBeanObjectWrapper expressionBeanWrapper) {
        this.expressionBeanWrapper = expressionBeanWrapper;
    }

}
