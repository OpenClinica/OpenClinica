/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.dao.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;

import org.akaza.openclinica.dao.cache.EhCacheWrapper;
import org.springframework.core.io.ResourceLoader;
import org.xml.sax.SAXException;

/**
 * Provides a singleton SQLFactory instance
 * 
 * @author thickerson
 * @author Jun Xu
 * 
 */
public class SQLFactory {

    // DAO KEYS TO USE FOR RETRIEVING DIGESTER
    public final String DAO_USERACCOUNT = "useraccount";
    public final String DAO_STUDY = "study";
    public final String DAO_STUDYEVENTDEFNITION = "studyeventdefintion";
    public final String DAO_SUBJECT = "subject";
    public final String DAO_STUDYSUBJECT = "study_subject";
    public final String DAO_STUDYGROUP = "study_group";
    public final String DAO_STUDYGROUPCLASS = "study_group_class";
    public final String DAO_SUBJECTGROUPMAP = "subject_group_map";
    public final String DAO_STUDYEVENT = "study_event";
    public final String DAO_EVENTDEFINITIONCRF = "event_definition_crf";
    public final String DAO_AUDITEVENT = "audit_event";
    public final String DAO_AUDIT = "audit";

    //
    //    public final String DAO_DATAVIEW = "dataview_dao";
    public final String DAO_ITEM = "item";
    public final String DAO_ITEMDATA = "item_data";
    public final String DAO_ITEMFORMMETADATA = "item_form_metadata";
    public final String DAO_CRF = "crf";
    public final String DAO_CRFVERSION = "crfversion";
    public final String DAO_DATASET = "dataset";
    public final String DAO_SECTION = "section";
    public final String DAO_MASKING = "masking";
    public final String DAO_FILTER = "filter";

    public final String DAO_EVENTCRF = "eventcrf";
    public final String DAO_ARCHIVED_DATASET_FILE = "archived_dataset_file";
    public final String DAO_DISCREPANCY_NOTE = "discrepancy_note";
    public final String DAO_STUDY_PARAMETER = "study_parameter";
    public final String DAO_ITEM_GROUP = "item_group";
    public final String DAO_ITEM_GROUP_METADATA = "item_group_metadata";
    public final String DAO_RULESET = "ruleset";
    public final String DAO_RULE = "rule";
    public final String DAO_RULE_ACTION = "action";
    public final String DAO_EXPRESSION = "expression";
    public final String DAO_RULESET_RULE = "rulesetrule";
    public final String DAO_RULESET_AUDIT = "rulesetaudit";
    public final String DAO_RULESETRULE_AUDIT = "rulesetruleaudit";
    public final String DAO_SUBJECTTRANSFER = "subjecttransfer";
    // YW, 05-2008, for odm extract
    public final String DAO_ODM_EXTRACT = "odm_extract";

    
    // EhCacheManagerFactoryBean cacheManagerBean = new EhCacheManagerFactoryBean();
    //  cacheManagerBean.setConfigLocation= (new org.springframework.core.io.FileSystemResource("classpath:org/akaza/openclinica/ehcache.xml") );
    // cacheManagerBean.setConfigLocation(new FileSystemReour(""));
     

    private SQLFactory(){
    	//to thwart any instantiation of this class
    }
    
    
    public static EhCacheWrapper ehCacheWrapper;
    

    public EhCacheWrapper getEhCacheWrapper() {
        return ehCacheWrapper;
    }

    public void setEhCacheWrapper(EhCacheWrapper ehCacheWrapper) {
        this.ehCacheWrapper = ehCacheWrapper;
    }

    private static Hashtable digesters = new Hashtable();

    private final String dbName = "";

    /**
     * A handle to the unique SQLFactory instance.
     */
    static private SQLFactory facInstance = null;

    /**
     * @return The unique instance of this class. <b>WARNING this directory will
     *         need to be changed to run unit tests on other systems!!!</b>
     */
    static public SQLFactory getInstance() {
        // set so that we could test an xml file in a unit test, tbh
        if (facInstance == null) {
        	synchronized(SQLFactory.class) {
            facInstance = new SQLFactory();
        	}
        }
        return facInstance;
    }

    // name should be one of the public static final Strings above
    public void addDigester(String name, DAODigester dig) {
        digesters.put(name, dig);
    }

    // name should be one of the public static final Strings above
    public DAODigester getDigester(String name) {
        return (DAODigester) digesters.get(name);
    }

    
    
    
    
    public void run(String dbName, ResourceLoader resourceLoader) {
        // we get the type of the database and run the factory, picking
        // up all the queries. NOTE that this should only be run
        // during the init servlets' action, and then it will
        // remain in static memory. tbh 9/8/04

        // ssachs 20041011
        // modified this section so that files are added using the
        // public static final strings above which are not specific to the
        // database

        // key is the public static final sting used above; value is the actual
        // filename
        HashMap fileList = new HashMap();
        CacheManager cacheManager = null;
        
        
       
      
        try {
            if(resourceLoader!=null)
            cacheManager = new CacheManager(resourceLoader.getResource("classpath:org/akaza/openclinica/ehcache.xml").getInputStream());
        } catch (CacheException e) {
          
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EhCacheWrapper ehCache = new EhCacheWrapper("com.akaza.openclinica.dao.core.DAOCache",cacheManager);
        
        
        setEhCacheWrapper(ehCache);
        
        if ("oracle".equals(dbName)) {
            // logger.warn("Oracle Test");
            fileList.put(this.DAO_USERACCOUNT, "oracle_useraccount_dao.xml");
            fileList.put(this.DAO_ARCHIVED_DATASET_FILE, "oracle_archived_dataset_file_dao.xml");
            fileList.put(this.DAO_STUDY, "oracle_study_dao.xml");
            fileList.put(this.DAO_STUDYEVENTDEFNITION, "oracle_studyeventdefinition_dao.xml");
            fileList.put(this.DAO_STUDYEVENT, "oracle_study_event_dao.xml");
            fileList.put(this.DAO_STUDYGROUP, "oracle_study_group_dao.xml");
            fileList.put(this.DAO_STUDYGROUPCLASS, "oracle_study_group_class_dao.xml");
            fileList.put(this.DAO_STUDYSUBJECT, "oracle_study_subject_dao.xml");
            fileList.put(this.DAO_SUBJECT, "oracle_subject_dao.xml");
            fileList.put(this.DAO_SUBJECTGROUPMAP, "oracle_subject_group_map_dao.xml");
            fileList.put(this.DAO_EVENTDEFINITIONCRF, "oracle_event_definition_crf_dao.xml");
            fileList.put(this.DAO_AUDITEVENT, "oracle_audit_event_dao.xml");
            fileList.put(this.DAO_AUDIT, "oracle_audit_dao.xml");
            fileList.put(this.DAO_ITEM, "oracle_item_dao.xml");
            fileList.put(this.DAO_ITEMDATA, "oracle_itemdata_dao.xml");
            fileList.put(this.DAO_CRF, "oracle_crf_dao.xml");
            fileList.put(this.DAO_CRFVERSION, "oracle_crfversion_dao.xml");
            fileList.put(this.DAO_DATASET, "oracle_dataset_dao.xml");
            fileList.put(this.DAO_SECTION, "oracle_section_dao.xml");
            fileList.put(this.DAO_FILTER, "oracle_filter_dao.xml");
            fileList.put(this.DAO_MASKING, "oracle_masking_dao.xml");
            fileList.put(this.DAO_EVENTCRF, "oracle_eventcrf_dao.xml");
            fileList.put(this.DAO_ITEMFORMMETADATA, "oracle_item_form_metadata_dao.xml");
            fileList.put(this.DAO_DISCREPANCY_NOTE, "oracle_discrepancy_note_dao.xml");
            fileList.put(this.DAO_STUDY_PARAMETER, "oracle_study_parameter_value_dao.xml");
            fileList.put(this.DAO_ITEM_GROUP, "oracle_item_group_dao.xml");
            fileList.put(this.DAO_ITEM_GROUP_METADATA, "oracle_item_group_metadata_dao.xml");
            fileList.put(this.DAO_RULESET, "oracle_ruleset_dao.xml");
            fileList.put(this.DAO_RULE, "oracle_rule_dao.xml");
            fileList.put(this.DAO_RULE_ACTION, "oracle_action_dao.xml");
            fileList.put(this.DAO_EXPRESSION, "oracle_expression_dao.xml");
            fileList.put(this.DAO_RULESET_RULE, "oracle_rulesetrule_dao.xml");
            fileList.put(this.DAO_RULESET_AUDIT, "oracle_ruleset_audit_dao.xml");
            fileList.put(this.DAO_RULESETRULE_AUDIT, "oracle_rulesetrule_audit_dao.xml");
            fileList.put(this.DAO_ODM_EXTRACT, "oracle_odm_extract_dao.xml");
//            fileList.put(this.DAO_SUBJECTTRANSFER, "oracle_subjecttransfer_dao.xml");
        } else if ("postgres".equals(dbName)) {
            fileList.put(this.DAO_USERACCOUNT, "useraccount_dao.xml");
            fileList.put(this.DAO_ARCHIVED_DATASET_FILE, "archived_dataset_file_dao.xml");
            fileList.put(this.DAO_STUDY, "study_dao.xml");
            fileList.put(this.DAO_STUDYEVENTDEFNITION, "studyeventdefinition_dao.xml");
            fileList.put(this.DAO_STUDYEVENT, "study_event_dao.xml");
            fileList.put(this.DAO_STUDYGROUP, "study_group_dao.xml");
            fileList.put(this.DAO_STUDYGROUPCLASS, "study_group_class_dao.xml");
            fileList.put(this.DAO_STUDYSUBJECT, "study_subject_dao.xml");
            fileList.put(this.DAO_SUBJECT, "subject_dao.xml");
            fileList.put(this.DAO_SUBJECTGROUPMAP, "subject_group_map_dao.xml");
            fileList.put(this.DAO_EVENTDEFINITIONCRF, "event_definition_crf_dao.xml");
            fileList.put(this.DAO_AUDITEVENT, "audit_event_dao.xml");
            fileList.put(this.DAO_AUDIT, "audit_dao.xml");
            fileList.put(this.DAO_ITEM, "item_dao.xml");
            fileList.put(this.DAO_ITEMDATA, "itemdata_dao.xml");
            fileList.put(this.DAO_CRF, "crf_dao.xml");
            fileList.put(this.DAO_CRFVERSION, "crfversion_dao.xml");
            fileList.put(this.DAO_DATASET, "dataset_dao.xml");
            fileList.put(this.DAO_SECTION, "section_dao.xml");
            fileList.put(this.DAO_FILTER, "filter_dao.xml");
            fileList.put(this.DAO_MASKING, "masking_dao.xml");
            fileList.put(this.DAO_EVENTCRF, "eventcrf_dao.xml");
            fileList.put(this.DAO_ITEMFORMMETADATA, "item_form_metadata_dao.xml");
            fileList.put(this.DAO_DISCREPANCY_NOTE, "discrepancy_note_dao.xml");
            fileList.put(this.DAO_STUDY_PARAMETER, "study_parameter_value_dao.xml");
            fileList.put(this.DAO_ITEM_GROUP, "item_group_dao.xml");
            fileList.put(this.DAO_ITEM_GROUP_METADATA, "item_group_metadata_dao.xml");

            fileList.put(this.DAO_RULESET, "ruleset_dao.xml");
            fileList.put(this.DAO_RULE, "rule_dao.xml");
            fileList.put(this.DAO_RULE_ACTION, "action_dao.xml");
            fileList.put(this.DAO_EXPRESSION, "expression_dao.xml");
            fileList.put(this.DAO_RULESET_RULE, "rulesetrule_dao.xml");
            fileList.put(this.DAO_RULESET_AUDIT, "ruleset_audit_dao.xml");
            fileList.put(this.DAO_RULESETRULE_AUDIT, "rulesetrule_audit_dao.xml");
            fileList.put(this.DAO_SUBJECTTRANSFER, "subjecttransfer_dao.xml");

            fileList.put(this.DAO_ODM_EXTRACT, "odm_extract_dao.xml");

            // add files here as we port over to postgres, tbh
        }// should be either oracle or postgres, but what if the file is
        // gone?
        else {
            // throw an exception here, ssachs
        }

        Set DAONames = fileList.keySet();
        Iterator DAONamesIt = DAONames.iterator();

        while (DAONamesIt.hasNext()) {
            String DAOName = (String) DAONamesIt.next();
            String DAOFileName = (String) fileList.get(DAOName);

            DAODigester newDaoDigester = new DAODigester();

            try {
         
                if (System.getProperty("catalina.home") == null) {
                    String path = getPropertiesDir();
                    newDaoDigester.setInputStream(new FileInputStream(path + DAOFileName));
                } else {
                    String path = CoreResources.PROPERTIES_DIR;
                    newDaoDigester.setInputStream(resourceLoader.getResource("classpath:properties/" + DAOFileName).getInputStream());
                    //newDaoDigester.setInputStream(new FileInputStream(path + DAOFileName));
                }
                try {
                    newDaoDigester.run();
                    digesters.put(DAOName, newDaoDigester);
                } catch (SAXException saxe) {
                    saxe.printStackTrace();
                }// end try block for xml
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }// end try block for files
        }// end for loop

    }

    public String getPropertiesDir() {
        String resource = "properties/placeholder.properties";
        String absolutePath = null;
        URL path = this.getClass().getClassLoader().getResource(resource);
        if (null != path) {
            absolutePath = path.getPath();
        }else{
            throw new RuntimeException("Could not get a path please investigate !!");
        }
        absolutePath = absolutePath.replaceAll("placeholder.properties", "");
        return absolutePath;
    }

}
