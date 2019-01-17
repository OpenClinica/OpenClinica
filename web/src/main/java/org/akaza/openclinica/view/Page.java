/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.view;

/**
 * 
 * 
 *         Provides a type-safe enumeration for JSP Page,converted from original static class.
 *         @author jnyayapathi
 */
public enum Page {
	
	
	
          
  

    /**
     * Page for logging in
     */
      	LOGIN("/WEB-INF/jsp/login/login.jsp", "OpenClinica Login"),
    	LOGIN_USER_ACCOUNT_DELETED("/WEB-INF/jsp/login/login.jsp" + "?action=userAccountDeleted",
                "Unsuccessful Login Due to Account Deletion"),
                ENTERPRISE("/WEB-INF/jsp/login/enterprise.jsp", "OpenClinica Enterprise"),
   /**
     * Page for logging out
     */
                LOGOUT("/WEB-INF/jsp/login/logout.jsp", "OpenClinica JsonLog Out"),

    /**
     * Page to show the main menu of openclinica
     */
                MENU("/WEB-INF/jsp/menu.jsp", "Welcome to OpenClinica"),
                MENU_SERVLET("/MainMenu", "Welcome to OpenClinica Main Servlet"),
                NO_ACCESS("/WEB-INF/jsp/noAccess.jsp", "No Access"),


 // YW 06-25-2007 <<
    /**
     * Page for reset password when password is expired.
     */
                RESET_PASSWORD("/WEB-INF/jsp/login/resetPassword.jsp", "Reset your expired password"),
                
    // YW >>

    /**
     * Page for user to update profile
     */
                UPDATE_PROFILE("/WEB-INF/jsp/login/updateProfile.jsp", "Update your profile"),
                

    /**
     * Page for user to confirm inputs of updating profile
     */
                UPDATE_PROFILE_CONFIRM("/WEB-INF/jsp/login/updateProfileConfirm.jsp", "Confirm your profile"),
                

    /**
     * Page for user to request password
     */
                CONTACT("/WEB-INF/jsp/login/contact.jsp", "Contact Form"),
                

    /**
     * Page for user to request password
     */
                REQUEST_PWD("/WEB-INF/jsp/login/requestPassword.jsp","Request passwod form"),
                REQUEST_STUDY("/WEB-INF/jsp/login/requestStudy.jsp", "Request study access"),


    /**
     * Page for user to request study access
     */
    REQUEST_STUDY_CONFIRM ("/WEB-INF/jsp/login/requestStudyConfirm.jsp", "Request study access Confirm"),

    /**
     * Page for changing study
     */
    CHANGE_STUDY ("/WEB-INF/jsp/login/changeStudy.jsp", "Change Study"),
    CHANGE_STUDY_SERVLET ("/ChangeStudy", "Change Study Servlet"),

    REQUEST_ACCOUNT("/WEB-INF/jsp/login/requestAccount.jsp", "Request account form"),

    REQUEST_ACCOUNT_CONFIRM ("/WEB-INF/jsp/login/requestAccountConfirm.jsp", "Request account confirm"),
    /**
     * Page for user to confirm inputs of requesting password
     */
   REQUEST_PWD_CONFIRM ("/WEB-INF/jsp/login/requestPasswordConfirm.jsp", "Request passwod Confirm"),

    /**
     * Page for creating a user account.
     */
    CREATE_ACCOUNT("/WEB-INF/jsp/admin/createuseraccount.jsp", "Create an account"),

    /**
     * Page for creating a user account.
     */
    TEST_OBJECT("/WEB-INF/jsp/admin/createtestObject.jsp", "Create an account"),

    /**
     * Page for editing a user account, and confirmation page.
     */
    EDIT_ACCOUNT("/WEB-INF/jsp/admin/edituseraccount.jsp", "Edit an account"),
     EDIT_ACCOUNT_CONFIRM ("/WEB-INF/jsp/admin/edituseraccountconfirm.jsp", "Edit an account"),

    /**
     * Page for viewing all user accounts (for admin)
     */
    LIST_USER_ACCOUNTS ("/WEB-INF/jsp/admin/listuseraccounts.jsp", "List user accounts"),
    LIST_USER_ACCOUNTS_SERVLET ("/ListUserAccounts", "List user accounts"),

    /**
     * Page for viewing a single user account (for admin)
     */
     VIEW_USER_ACCOUNT("/WEB-INF/jsp/admin/viewuseraccount.jsp", "View user account"),
 VIEW_USER_ACCOUNT_SERVLET ("/ViewUserAccount", "View user account servlet"),

    CONFIGURATION ("/WEB-INF/jsp/admin/configuration.jsp", "Configuration"),
   CONFIGURATION_PASSWORD_REQUIREMENTS("/WEB-INF/jsp/admin/configurationPasswordRequirements.jsp", "Configuration"),
   SYSTEM_STATUSe("/WEB-INF/jsp/admin/systemStatus.jsp", "System Status"),

    /**
     * Page for creating a study.
     */
     CREATE_STUDY1 ("/WEB-INF/jsp/managestudy/createStudy1.jsp", "Create a new Study first section"),
    CREATE_STUDY2 ("/WEB-INF/jsp/managestudy/createStudy2.jsp", "Create a new Study second section"),
    CREATE_STUDY3 ("/WEB-INF/jsp/managestudy/createStudy3.jsp", "Create a new Study third section"),
    CREATE_STUDY4 ("/WEB-INF/jsp/managestudy/createStudy4.jsp", "Create a new Study forth section"),
    CREATE_STUDY5 ("/WEB-INF/jsp/managestudy/createStudy5.jsp", "Create a new Study fifth section"),
    CREATE_STUDY6 ("/WEB-INF/jsp/managestudy/createStudy6.jsp", "Create a new Study sixth section"),
    CREATE_STUDY7 ("/WEB-INF/jsp/managestudy/createStudy7.jsp", "Create a new Study seventh section"),
     CREATE_STUDY8 ("/WEB-INF/jsp/managestudy/createStudy8.jsp", "Create a new Study last section"),

    /**
     * Page for confirming a new study.
     */
    STUDY_CREATE_CONFIRM ("/WEB-INF/jsp/managestudy/studyCreateConfirm.jsp", "Confirm a new Study"),

    /**
     * Page for update a study.
     */
    UPDATE_STUDY1("/WEB-INF/jsp/managestudy/updateStudy1.jsp", "Update a Study first section"),
    UPDATE_STUDY2("/WEB-INF/jsp/managestudy/updateStudy2.jsp", "Update a Study second section"),
    UPDATE_STUDY3("/WEB-INF/jsp/managestudy/updateStudy3.jsp", "Update a Study third section"),
    UPDATE_STUDY4 ("/WEB-INF/jsp/managestudy/updateStudy4.jsp", "Update a Study forth section"),
    UPDATE_STUDY5("/WEB-INF/jsp/managestudy/updateStudy5.jsp", "Update a Study fifth section"),
    UPDATE_STUDY6 ("/WEB-INF/jsp/managestudy/updateStudy6.jsp", "Update a Study sixth section"),
    UPDATE_STUDY7("/WEB-INF/jsp/managestudy/updateStudy7.jsp", "Update a Study seventh section"),
    UPDATE_STUDY8 ("/WEB-INF/jsp/managestudy/updateStudy8.jsp", "Update a Study last section"),

    LIST_STUDY_SUBJECTS ("/WEB-INF/jsp/managestudy/findSubjects.jsp", "List Study Subjects"),
    LIST_STUDY_SUBJECTS_SERVLET("/ListStudySubjects", "List Study Subjects"),

     UPDATE_STUDY_SERVLET_NEW ("/UpdateStudyNew", "Update a Study"),
    UPDATE_STUDY_NEW ("/WEB-INF/jsp/managestudy/updateStudyNew.jsp", "Update a Study"),

    VIEW_STUDY("/WEB-INF/jsp/admin/viewStudy.jsp", "View study"),
    VIEW_FULL_STUDY("/WEB-INF/jsp/admin/viewFullStudy.jsp", "View full study"),

    REMOVE_CRF("/WEB-INF/jsp/admin/removeCRF.jsp", "Remove a CRF"),
    RESTORE_CRF ("/WEB-INF/jsp/admin/restoreCRF.jsp", "Restore a CRF"),
    /**
     * Page for confirming an existing study.
     */
    STUDY_UPDATE_CONFIRM ("/WEB-INF/jsp/managestudy/studyUpdateConfirm.jsp", "Confirm a Study"),

    /**
     * Page for creating a new sub study.
     */
    CREATE_SUB_STUDY ("/WEB-INF/jsp/managestudy/createSubStudy.jsp", "Create a sub Study"),

    /**
     * Page for confirming a new sub study.
     */
    CONFIRM_CREATE_SUB_STUDY("/WEB-INF/jsp/managestudy/createSubStudyConfirm.jsp", "Confirm a new site"),

    /**
     * Page for confirming a new sub study.
     */
    CONFIRM_UPDATE_SUB_STUDY ("/WEB-INF/jsp/managestudy/updateSubStudyConfirm.jsp", "Confirm a site"),

    /**
     * Page for updating a new sub study.
     */
    UPDATE_SUB_STUDY("/WEB-INF/jsp/managestudy/updateSubStudy.jsp", "Update a sub Study"),

    /**
     * Page for viewing a new sub study.
     */
    VIEW_SITE("/WEB-INF/jsp/managestudy/viewSite.jsp", "View a sub Study"),
    VIEW_SITE_SERVLET ("/ViewSite", "View a sub Study"),

    REMOVE_STUDY ("/WEB-INF/jsp/admin/removeStudy.jsp", "Remove a Study"),

    /**
     * Page for restoring a study.
     */
    RESTORE_STUDY ("/WEB-INF/jsp/admin/restoreStudy.jsp", "Restore a Study"),
    /**
     * Page for removing a sub study.
     */
    REMOVE_SITE ("/WEB-INF/jsp/managestudy/removeSite.jsp", "View a sub Study"),

    /**
     * Page for restoring a sub study.
     */
    RESTORE_SITE ("/WEB-INF/jsp/managestudy/restoreSite.jsp", "Restore a sub Study"),
    /**
     * Page for editing a study user role.
     */
    EDIT_STUDY_USER_ROLE ("/WEB-INF/jsp/admin/editstudyuserrole.jsp", "Edit Study User Role"),

    /**
     * Page for view all users of a study and its sites.
     */
    STUDY_USER_LIST ("/WEB-INF/jsp/managestudy/studyUserList.jsp", "View Study Users"),
    /**
     * Page for view all studies.
     */
    STUDY_LIST ("/WEB-INF/jsp/managestudy/studyList.jsp", "View All Studies"),

    /**
     * Page for view all sites.
     */
     SITE_LIST ("/WEB-INF/jsp/managestudy/siteList.jsp", "View All Sites"),
     SITE_LIST_SERVLET ("/ListSite", "View All Sites Servlet"),

    /*
     * Page for sign study subject
     */

    SIGN_STUDY_SUBJECT_SERVLET ("/SignStudySubject", "Sign Study Subject"),
    SIGN_STUDY_SUBJECT ("/WEB-INF/jsp/managestudy/signStudySubject.jsp", "Sign Study Subject"),

    /**
     * Page for view all study group classes.
     */
    SUBJECT_GROUP_CLASS_LIST ("/WEB-INF/jsp/managestudy/subjectGroupClassList.jsp", "View All Group Class"),
    SUBJECT_GROUP_CLASS_LIST_SERVLET ("/ListSubjectGroupClass", "View Subject Group Class Servlet"),
    CREATE_SUBJECT_GROUP_CLASS ("/WEB-INF/jsp/managestudy/createSubjectGroupClass.jsp", "Create Subject Group Class"),
    CREATE_SUBJECT_GROUP_CLASS_CONFIRM ("/WEB-INF/jsp/managestudy/createSubjectGroupClassConfirm.jsp",
            "Create Subject Group Class Confirm"),
    UPDATE_SUBJECT_GROUP_CLASS ("/WEB-INF/jsp/managestudy/updateSubjectGroupClass.jsp", "Update Subject Group Class"),
    UPDATE_SUBJECT_GROUP_CLASS_CONFIRM ("/WEB-INF/jsp/managestudy/updateSubjectGroupClassConfirm.jsp",
            "Update Subject Group Class Confirm"),
    VIEW_SUBJECT_GROUP_CLASS ("/WEB-INF/jsp/managestudy/viewSubjectGroupClass.jsp", "View Subject Group Class"),
    REMOVE_SUBJECT_GROUP_CLASS ("/WEB-INF/jsp/managestudy/removeSubjectGroupClass.jsp", "Remove Subject Group Class"),
    RESTORE_SUBJECT_GROUP_CLASS ("/WEB-INF/jsp/managestudy/restoreSubjectGroupClass.jsp", "Restore Subject Group Class"),

    /**
     * Page for defining a study event.
     */
    DEFINE_STUDY_EVENT1 ("/WEB-INF/jsp/managestudy/defineStudyEvent1.jsp", "Define Study Event"),
    DEFINE_STUDY_EVENT2 ("/WEB-INF/jsp/managestudy/defineStudyEvent2.jsp", "Define Study Event"),
    DEFINE_STUDY_EVENT3 ("/WEB-INF/jsp/managestudy/defineStudyEvent3.jsp", "Define Study Event"),
    DEFINE_STUDY_EVENT4 ("/WEB-INF/jsp/managestudy/defineStudyEvent4.jsp", "Define Study Event"),
    DEFINE_STUDY_EVENT_CONFIRM("/WEB-INF/jsp/managestudy/defineStudyEventConfirm.jsp", "Define Study Event Confirm"),

    /**
     * Page for updating a study event definition.
     */
   UPDATE_EVENT_DEFINITION1 ("/WEB-INF/jsp/managestudy/updateEventDefinition1.jsp", "Update Event Definition"),
    UPDATE_EVENT_DEFINITION2 ("/WEB-INF/jsp/managestudy/updateEventDefinition2.jsp", "Update Event Definition"),
    UPDATE_EVENT_DEFINITION_CONFIRM ("/WEB-INF/jsp/managestudy/updateEventDefinitionConfirm.jsp",
            "Update Event Definition Confirm"),

    /**
     * Page for viewing definition
     */
    VIEW_EVENT_DEFINITION ("/WEB-INF/jsp/managestudy/viewEventDefinition.jsp", "View Event Definition"),
    VIEW_EVENT_DEFINITION_READONLY ("/WEB-INF/jsp/managestudy/viewEventDefinitionReadOnly.jsp", "View Event Definition"),
    VIEW_EVENT_DEFINITION_NOSIDEBAR ("/WEB-INF/jsp/managestudy/viewEventDefinitionNoSidebar.jsp", "View Event Definition"),
    VIEW_EVENT_DEFINITION_SERVLET ("/ViewEventDefinition", "View Event Definition Servlet"),

    /**
     * Page for removing definition
     */
    REMOVE_DEFINITION("/WEB-INF/jsp/managestudy/removeDefinition.jsp", "Remove Event Definition"),
    /**
     * Page for removing definition
     */
    RESTORE_DEFINITION("/WEB-INF/jsp/managestudy/restoreDefinition.jsp", "Restore Event Definition"),

    /**
     * Page for locking definition
     */
    LOCK_DEFINITION ("/WEB-INF/jsp/managestudy/lockDefinition.jsp", "Lock Event Definition"),
    /**
     * Page for unlocking definition
     */
    UNLOCK_DEFINITION ("/WEB-INF/jsp/managestudy/unlockDefinition.jsp", "Unlock Event Definition"),
    /**
     * Page for listing seds
     */
    STUDY_EVENT_DEFINITION_LIST ("/WEB-INF/jsp/managestudy/studyEventDefinitionList.jsp", "List all CRFs"),

    /**
     * Page for view all seds.
     */
     LIST_DEFINITION_SERVLET ("/ListEventDefinition", "View All Definitions"),

    /**
     * Page for listing crfs.
     */
    CRF_LIST ("/WEB-INF/jsp/admin/listCRF.jsp", "List all CRFs"),
    CRF_LIST_SERVLET ("/ListCRF", "List all CRFs servlet"),

    /**
     * Page for creating crf.
     */
    VIEW_CRF ("/WEB-INF/jsp/admin/viewCRF.jsp", "View a CRF"),

    /**
     * Page for creating crf.
     */
    BATCH_CRF_MIGRATION ("/WEB-INF/jsp/admin/batchCRFMigration.jsp", "batch CRF Migration"),
    /**
     * Page for viewing audit user activity.
     */
    AUDIT_USER_ACTIVITY ("/WEB-INF/jsp/admin/auditUserActivity.jsp", "Audit User Activity"),

    /**
     * Page for viewing audit database
     */
    AUDIT_DATABASE ("/WEB-INF/jsp/admin/auditDatabase.jsp", "Audit Database"),

    /**
     * Page for creating crf.
     */
     CREATE_CRF ("/WEB-INF/jsp/admin/createCRF.jsp", "Create a new CRF"),

    /**
     * Page for updating crf.
     */
    UPDATE_CRF ("/WEB-INF/jsp/admin/updateCRF.jsp", "Update a CRF"),
    UPDATE_CRF_CONFIRM ("/WEB-INF/jsp/admin/updateCRFConfirm.jsp", "Update a CRF Confirm"),

    /**
     * Page for creating crf confirm.
     */
    CREATE_CRF_CONFIRM ("/WEB-INF/jsp/admin/createCRFConfirm.jsp", "Create a new CRF Confirm"),

    /**
     * Page for creating crf version.
     */
    CREATE_CRF_VERSION ("/WEB-INF/jsp/admin/createCRFVersion.jsp", "Create a new CRF Version"),
    UPLOAD_CRF_VERSION ("/WEB-INF/jsp/admin/uploadCRFVersionFile.jsp", "Upload a new CRF Version"),

    REMOVE_CRF_VERSION ("/WEB-INF/jsp/admin/removeCRFVersion.jsp", "Remove CRF Version"),
    RESTORE_CRF_VERSION ("/WEB-INF/jsp/admin/restoreCRFVersion.jsp", "Restore CRF Version"),

    CREATE_XFORM_CRF_VERSION_SERVLET ("/WEB-INF/jsp/admin/createXformCRFVersion.jsp", "Create a new Xform CRF Version"),

    /**
     * Page for creating crf data imports
     */
    IMPORT_CRF_DATA ("/WEB-INF/jsp/submit/import.jsp", "Import CRF Data"),
    VERIFY_IMPORT_SERVLET ("/VerifyImportedCRFData", "Verify Imported CRF Data Servlet"),
    VERIFY_IMPORT_CRF_DATA ("/WEB-INF/jsp/submit/verifyImport.jsp", "Verify Imported CRF Data"),

    /**
     * Page for creating rule imports
     */
    IMPORT_RULES ("/WEB-INF/jsp/submit/importRules.jsp", "Import Rules"),
    VIEW_RULE_SETS ("/WEB-INF/jsp/submit/listRuleSets.jsp", "List Rule Assignments"),
    VIEW_RULE_SETS2 ("/WEB-INF/jsp/submit/listRuleSets2.jsp", "List Rule Assignments"),
    VIEW_RULE_SETS_DESIGNER ("/WEB-INF/jsp/submit/listRuleSetsDesigner.jsp", "List Rule Assignments"),
    VERIFY_RULES_IMPORT_SERVLET ("/VerifyImportedRule", "Verify Imported Rule Servlet"),
    VERIFY_RULES_IMPORT ("/WEB-INF/jsp/submit/verifyImportRule.jsp", "Verify Imported Rules"),
    VIEW_RULES ("/WEB-INF/jsp/submit/viewRules.jsp", "View Rules"),
    VIEW_EXECUTED_RULES ("/WEB-INF/jsp/submit/viewExecutedRules.jsp", "View Rules"),
    LIST_RULE_SETS_SERVLET ("/ViewRuleAssignment", "List Rule Assignments"),
    REMOVE_RULE_SET ("/WEB-INF/jsp/submit/removeRuleSet.jsp", "Remove RuleSet"),
    RESTORE_RULE_SET ("/WEB-INF/jsp/submit/restoreRuleSet.jsp", "Remove RuleSet"),
    VIEW_EXECUTED_RULES_FROM_CRF ("/WEB-INF/jsp/submit/viewExecutedRulesFromCrf.jsp", "View Results"),
    VIEW_RULESET_AUDITS ("/WEB-INF/jsp/submit/viewRuleSetAudits.jsp", "View Rule Audits"),
    TEST_RULES ("/WEB-INF/jsp/submit/testRules.jsp", "Test Rules"),

    /**
     * Page for creating crf version.
     */
    CREATE_CRF_VERSION_SERVLET ("/CreateCRFVersion", "Create a new CRF Version Servlet"),
    /**
     * Page for confirming crf version.
     */
    CREATE_CRF_VERSION_CONFIRM ("/WEB-INF/jsp/admin/createCRFVersionConfirm.jsp", "Create a new CRF Version Confirm"),
    CREATE_CRF_VERSION_CONFIRMSQL ("/WEB-INF/jsp/admin/createCRFVersionConfirmSQL.jsp",
            "Create a new CRF Version Confirm SQL"),
    CREATE_CRF_VERSION_DONE ("/WEB-INF/jsp/admin/createCRFVersionDone.jsp", "Create a new CRF Version Done"),
    REMOVE_CRF_VERSION_CONFIRM ("/WEB-INF/jsp/admin/removeCRFVersionConfirm.jsp", "Remove CRF Version Confirm"),
    CREATE_CRF_VERSION_NODELETE("/WEB-INF/jsp/admin/createCRFVersionNoDelete.jsp", "Create a new CRF cannot delete version"),
    CREATE_CRF_VERSION_ERROR ("/WEB-INF/jsp/admin/createCRFVersionError.jsp", "Create a new CRF error"),
    REMOVE_CRF_VERSION_DEF ("/WEB-INF/jsp/admin/removeCRFVersionDef.jsp", "Remove CRF Version From Definition"),

    AUDIT_LOG_USER ("/WEB-INF/jsp/admin/auditLogUser.jsp", "Audit JsonLog display by User"),
    AUDIT_LOG_STUDY ("/WEB-INF/jsp/admin/auditLogStudy.jsp", "Audit JsonLog display by Study"),
    AUDIT_LOGS_STUDY ("/WEB-INF/jsp/admin/studyAuditLog.jsp", "Audit JsonLog display by Study"),

    AUDIT_LOGS_ITEMS ("/WEB-INF/jsp/admin/auditItem.jsp", "Audit JsonLog for Item"),

    /**
     * Page for extract datasets main, tbh
     */
    EXTRACT_DATASETS_MAIN ("/WEB-INF/jsp/extract/extractDatasetsMain.jsp", "Extract Datasets Main Page"),

    /**
     * Page for view all datasets, tbh
     */
    VIEW_DATASETS ("/WEB-INF/jsp/extract/viewDatasets.jsp", "View Datasets"),
    VIEW_EMPTY_DATASETS ("/WEB-INF/jsp/extract/viewEmptyDatasets.jsp", "View Datasets"),
    VIEW_DATASET_DETAILS ("/WEB-INF/jsp/extract/viewDatasetDetails.jsp", "View Dataset Details"),

    EXPORT_DATASETS ("/WEB-INF/jsp/extract/exportDatasets.jsp", "Export Dataset"),
    GENERATE_DATASET ("/WEB-INF/jsp/extract/generatedDataset.jsp", "Generate Dataset"),
    GENERATE_DATASET_HTML ("/WEB-INF/jsp/extract/generatedDatasetHtml.jsp", "Generate Dataset"),
    GENERATE_EXCEL_DATASET ("/WEB-INF/jsp/extract/generatedExcelDataset.jsp", "Generate Excel Dataset"),

    CREATE_DATASET_1 ("/WEB-INF/jsp/extract/createDatasetBegin.jsp", "Create Dataset Begin"),
    CREATE_DATASET_2 ("/WEB-INF/jsp/extract/createDatasetStep2.jsp", "Create Dataset Step Two"),
    CREATE_DATASET_3 ("/WEB-INF/jsp/extract/createDatasetStep3.jsp", "Create Dataset Step Three"),
    CREATE_DATASET_4 ("/WEB-INF/jsp/extract/createDatasetStep4.jsp", "Create Dataset Step Four"),
    CONFIRM_DATASET ("/WEB-INF/jsp/extract/createDatasetConfirmMetadata.jsp", "Create Dataset Step Four"),

    CREATE_DATASET_EVENT_ATTR ("/WEB-INF/jsp/extract/selectEventAttribute.jsp", "Create Dataset and select event Attribute"),
    CREATE_DATASET_SUB_ATTR ("/WEB-INF/jsp/extract/selectSubAttribute.jsp", "Create Dataset and select subject Attribute"),
    CREATE_DATASET_GROUP_ATTR ("/WEB-INF/jsp/extract/selectGroupAttribute.jsp", "Create Dataset and select group Attribute"),
    CREATE_DATASET_CRF_ATTR ("/WEB-INF/jsp/extract/selectCRFAttributes.jsp", "Create Dataset and select CRF Attribute"),
    // CREATE_DATASET_DISC_ATTR = new
    // Page("/WEB-INF/jsp/extract/selectDiscrepancyAttributes.jsp","Create
    // Dataset and select discrepancy Attribute"),
    CREATE_DATASET_SELECT_ITEMS ("/WEB-INF/jsp/extract/selectItems.jsp", "Create Dataset and select Items"),

   // CREATE_DATASET_APPLY_FILTER_SERVLET ("CreateDatasetApplyFilter", "Create Dataset Apply Filter"),
    CREATE_DATASET_APPLY_FILTER ("/WEB-INF/jsp/extract/createDatasetApplyFilter.jsp", "Create Dataset Apply Filter"),

    CREATE_DATASET_VIEW_SELECTED ("/WEB-INF/jsp/extract/viewSelected.jsp", "View Selected Items"),
    CREATE_DATASET_VIEW_SELECTED_HTML ("/WEB-INF/jsp/extract/viewSelectedHtml.jsp", "View Selected Items in a static way"),
    REMOVE_DATASET ("/WEB-INF/jsp/extract/removeDataset.jsp", "Remove Dataset"),
    RESTORE_DATASET ("/WEB-INF/jsp/extract/restoreDataset.jsp", "Restore Dataset"),

    ITEM_DETAIL ("/WEB-INF/jsp/extract/itemDetail.jsp", "Remove Dataset"),
    /**
     * Pages for create and show all filters, tbh
     * 
     */
    APPLY_FILTER ("/WEB-INF/jsp/extract/applyFilter.jsp", "Apply Filter"),
    CREATE_FILTER_SCREEN_1 ("/WEB-INF/jsp/extract/createFilterScreen1.jsp", "Create Filter Screen One"),
    CREATE_FILTER_SCREEN_2 ("/WEB-INF/jsp/extract/createFilterScreen2.jsp", "Create Filter Screen Two"),
    CREATE_FILTER_SCREEN_3 ("/WEB-INF/jsp/extract/createFilterScreen3.jsp", "Create Filter Screen Three"),
    CREATE_FILTER_SCREEN_3_1 ("/WEB-INF/jsp/extract/createFilterScreen3_1.jsp", "Create Filter Screen Three Point One"),
    CREATE_FILTER_SCREEN_3_2 ("/WEB-INF/jsp/extract/createFilterScreen3_2.jsp", "Create Filter Screen Three Point Two"),
    CREATE_FILTER_SCREEN_4 ("/WEB-INF/jsp/extract/createFilterScreen4.jsp", "Create Filter Screen Four"),
    CREATE_FILTER_SCREEN_5 ("/WEB-INF/jsp/extract/createFilterScreen5.jsp", "Create Filter Screen Five"),
    CREATE_FILTER_CONFIRM ("/WEB-INF/jsp/extract/createFilterConfirm.jsp", "Create Filter Confirm"),
    VIEW_FILTER_DETAILS ("/WEB-INF/jsp/extract/viewFilterDetails.jsp", "View Filter Details"),
    EDIT_FILTER ("/WEB-INF/jsp/extract/editFilter.jsp", "Edit Filter"),
    EDIT_DATASET ("/WEB-INF/jsp/extract/editDataset.jsp", "Edit Dataset"),
    VALIDATE_EDIT_FILTER ("/WEB-INF/jsp/extract/validateEditFilter.jsp", "Validate Edited Filter"),
    REMOVE_FILTER ("/WEB-INF/jsp/extract/removeFilter.jsp", "Remove Filter"),
    /**
     * Page to show errors
     */
    ERROR ("/WEB-INF/jsp/error.jsp", "Error Page of OpenClinica"),



    ADMIN_SYSTEM ("/WEB-INF/jsp/" + "admin/index.jsp", "Administer System Menu"),
    MANAGE_STUDY ("/WEB-INF/jsp/" + "managestudy/index.jsp", "Manage Study Menu"),
    MANAGE_STUDY_BODY ("/WEB-INF/jsp/" + "managestudy/managestudy_body.jsp", "Manage Study Menu"),

    CREATE_JOB_EXPORT ("/WEB-INF/jsp/" + "admin/createExportJob.jsp", "Create Export Job"),
    UPDATE_JOB_EXPORT ("/WEB-INF/jsp/" + "admin/updateExportJob.jsp", "Update Export Job"),
    CREATE_JOB_IMPORT ("/WEB-INF/jsp/" + "admin/createImportJob.jsp", "Create Import Job"),
    UPDATE_JOB_IMPORT ("/WEB-INF/jsp/" + "admin/updateImportJob.jsp", "Update Import Job"),
    VIEW_JOB ("/WEB-INF/jsp/" + "admin/viewJobs.jsp", "View Jobs"),
    VIEW_ALL_JOBS ("/WEB-INF/jsp/" + "admin/viewAllJobs.jsp", "View Jobs"),
    VIEW_IMPORT_JOB ("/WEB-INF/jsp/" + "admin/viewImportJobs.jsp", "View Import Jobs"),
    VIEW_IMPORT_JOB_SERVLET ("/ViewImportJob", "View Import Jobs"),
    VIEW_LOG_MESSAGE ("/WEB-INF/jsp/" + "admin/viewLogMessage.jsp", "View JsonLog Message"),
    // below line for redirect without having to generate the table, tbh
    VIEW_JOB_SERVLET ("/ViewJob", "View Jobs"),
    VIEW_SINGLE_JOB ("/WEB-INF/jsp/" + "admin/viewSingleJob.jsp", "View Jobs"),
    // job creation and viewing pages, all under admin
    TECH_ADMIN_SYSTEM ("/WEB-INF/jsp/" + "techadmin/index.jsp", "Technical Administrator Menu"),
    VIEW_SCHEDULER ("/WEB-INF/jsp/" + "admin/viewScheduler.jsp", "View System Scheduler"),
    ADMIN_SYSTEM_SERVLET ("/AdminSystem", "Administer System Servlet"),
    MANAGE_STUDY_SERVLET ("/ManageStudy", "Manage Study Servlet"),

    SUBMIT_DATA ("/WEB-INF/jsp/" + "submit/index.jsp", "Submit Data Menu"),
    // SUBMIT_DATA_SERVLET ("/SubmitData",
    // "Submit Data Menu"),
    SUBMIT_DATA_SERVLET ("/ListStudySubjectsSubmit", "Submit Data Menu"),

    CREATE_NEW_STUDY_EVENT ("/WEB-INF/jsp/" + "submit/createNewStudyEvent.jsp", "Create a New Study Event"),
    CREATE_NEW_STUDY_EVENT_SERVLET ("/CreateNewStudyEvent", "Create a New Study Event"),

    INSTRUCTIONS_ENROLL_SUBJECT ("/WEB-INF/jsp/" + "submit/instructionsEnrollSubject.jsp", "Enroll New Subject - Instructions"),
    ADD_NEW_SUBJECT ("/WEB-INF/jsp/" + "submit/addNewSubject.jsp", "Enroll New Subject"),
    ADD_EXISTING_SUBJECT ("/WEB-INF/jsp/" + "submit/addExistingSubject.jsp", "Enroll An Existing Subject"),

    INSTRUCTIONS_ENROLL_SUBJECT_SERVLET ("/AddNewSubject?instr=1", "Enroll New Subject Servlet"),

    FIND_STUDY_EVENTS_SERVLET ("/FindStudyEvents", "Find Study Events"),
    FIND_STUDY_EVENTS_STEP1 ("/WEB-INF/jsp/" + "submit/findStudyEventsStep1.jsp", "Find Study Events - Step 1"),
    FIND_STUDY_EVENTS_STEP2 ("/WEB-INF/jsp/" + "submit/findStudyEventsStep2.jsp", "Find Study Events - Step 2"),
    FIND_STUDY_EVENTS_STEP3 ("/WEB-INF/jsp/" + "submit/findStudyEventsStep3.jsp", "Find Study Events - Step 3"),

    ENTER_DATA_FOR_STUDY_EVENT ("/WEB-INF/jsp/" + "submit/enterDataForStudyEvent.jsp", "Enter Data for a Study Event"),
    ENTER_DATA_FOR_STUDY_EVENT_SERVLET ("/EnterDataForStudyEvent", "Enter Data for a Study Event"),

    TABLE_OF_CONTENTS ("/WEB-INF/jsp/" + "submit/tableOfContents.jsp", "Event CRF Data Submission"),
    TABLE_OF_CONTENTS_SERVLET ("/TableOfContents", "Event CRF Data Submission"),
    INTERVIEWER ("/WEB-INF/jsp/" + "submit/interviewer.jsp", "Event CRF Interview Info Submission"),
    INTERVIEWER_ENTIRE_PAGE ("/WEB-INF/jsp/" + "submit/interviewerEntirePage.jsp", "Event CRF Interview Info Submission"),

    INITIAL_DATA_ENTRY ("/WEB-INF/jsp/" + "submit/initialDataEntry.jsp", "Initial Data Entry"),
    INITIAL_DATA_ENTRY_SERVLET ("/InitialDataEntry", "Initial Data Entry"),

    DOUBLE_DATA_ENTRY ("/WEB-INF/jsp/" + "submit/doubleDataEntry.jsp", "Double Data Entry"),
    DOUBLE_DATA_ENTRY_SERVLET ("/DoubleDataEntry", "Double Data Entry"),

    MARK_EVENT_CRF_COMPLETE ("/WEB-INF/jsp/" + "submit/markEventCRFComplete.jsp", "Mark Event CRF Complete"),

    ADMIN_EDIT ("/WEB-INF/jsp/" + "submit/administrativeEditing.jsp", "Administrative Editing"),
    ADMIN_EDIT_SERVLET ("/AdministrativeEditing", "Administrative Editing Servlet"),

    LIST_USER_IN_STUDY ("/WEB-INF/jsp/" + "managestudy/listUserInStudy.jsp", "list users in a study"),
    VIEW_USER_IN_STUDY ("/WEB-INF/jsp/" + "managestudy/viewUserInStudy.jsp", "view a user in a study"),
    SET_USER_ROLE_IN_STUDY ("/WEB-INF/jsp/" + "managestudy/setUserRoleInStudy.jsp", "set a user role in a study"),
    REMOVE_USER_ROLE_IN_STUDY ("/WEB-INF/jsp/" + "managestudy/removeStudyUserRole.jsp", "remove a user role in a study"),
    RESTORE_USER_ROLE_IN_STUDY ("/WEB-INF/jsp/" + "managestudy/restoreStudyUserRole.jsp", "restore a user role in a study"),
    LIST_USER_IN_STUDY_SERVLET ("/ListStudyUser", "list users in a study"),

    LIST_SUBJECT ("/WEB-INF/jsp/" + "managestudy/listSubject.jsp", "list subjects in a study"),
    LIST_SUBJECT_SERVLET ("/ListSubject", "list subjects in a study"),
    VIEW_SUBJECT ("/WEB-INF/jsp/" + "admin/viewSubject.jsp", "View Subject"),

    VIEW_CRF_VERSION ("/WEB-INF/jsp/" + "managestudy/viewCRFVersion.jsp", "View a CRF Version"),

    // TODO do we need both versions here??? tbh
    LIST_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/listStudySubject.jsp", "list subjects in a study"),
    LIST_STUDY_SUBJECT_SERVLET ("/ListStudySubject", "list subjects in a study"),
    VIEW_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/viewStudySubject.jsp", "View Subject in a study"),
    VIEW_STUDY_SUBJECT_AUDIT ("/WEB-INF/jsp/" + "managestudy/viewStudySubjectAudit.jsp", "View Subject in a study Audit"),
    VIEW_STUDY_SUBJECT_SERVLET ("/ViewStudySubject", "View Subject in a study Servlet"),

    UPDATE_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/updateStudySubject.jsp", "update Subject in a study"),
    UPDATE_STUDY_SUBJECT_SERVLET ("/UpdateStudySubject", "update Subject in a study"),
    UPDATE_STUDY_SUBJECT_CONFIRM ("/WEB-INF/jsp/" + "managestudy/updateStudySubjectConfirm.jsp", "update Subject in a study Confirm"),

    REMOVE_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/removeStudySubject.jsp", "Remove Subject from a study"),
    RESTORE_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/restoreStudySubject.jsp", "Restore Subject to a study"),

    REMOVE_STUDY_EVENT ("/WEB-INF/jsp/" + "managestudy/removeStudyEvent.jsp", "Remove Event from a study"),
    RESTORE_STUDY_EVENT ("/WEB-INF/jsp/" + "managestudy/restoreStudyEvent.jsp", "Restore Event to a study"),
    DELETE_STUDY_EVENT ("/WEB-INF/jsp/" + "managestudy/deleteStudyEvent.jsp", "Delete Event from a study"),

    REMOVE_EVENT_CRF ("/WEB-INF/jsp/" + "managestudy/removeEventCRF.jsp", "Remove CRF from event"),
    RESTORE_EVENT_CRF ("/WEB-INF/jsp/" + "managestudy/restoreEventCRF.jsp", "Restore CRF to event"),
    VIEW_EVENT_CRF ("/WEB-INF/jsp/" + "managestudy/viewEventCRF.jsp", "View Event CRF Data"),
    DELETE_EVENT_CRF ("/WEB-INF/jsp/" + "admin/deleteEventCRF.jsp", "Delete CRF from event"),

    UPDATE_SUBJECT ("/WEB-INF/jsp/" + "admin/updateSubject.jsp", "update a subject"),
    UPDATE_SUBJECT_SERVLET ("/UpdateSubject", "update a subject"),
    UPDATE_SUBJECT_CONFIRM ("/WEB-INF/jsp/" + "admin/updateSubjectConfirm.jsp", "confirm update a subject"),
    REASSIGN_STUDY_SUBJECT ("/WEB-INF/jsp/" + "managestudy/reassignStudySubject.jsp", "reassign a subject"),
    REASSIGN_STUDY_SUBJECT_CONFIRM ("/WEB-INF/jsp/" + "managestudy/reassignStudySubjectConfirm.jsp", "confirm reassign a subject"),

    REMOVE_SUBJECT ("/WEB-INF/jsp/" + "admin/removeSubject.jsp", "remove a subject"),
    RESTORE_SUBJECT ("/WEB-INF/jsp/" + "admin/restoreSubject.jsp", "restore a subject"),

    SET_USER_ROLE ("/WEB-INF/jsp/" + "admin/setUserRole.jsp", "set a study user role for a user"),
    /**
     * Page for listing subjects.
     */
    SUBJECT_LIST ("/WEB-INF/jsp/admin/listSubject.jsp", "List all Subjects"),
    SUBJECT_LIST_SERVLET ("/ListSubject", "List all subjects servlet"),

    VIEW_TABLE_OF_CONTENT ("/WEB-INF/jsp/managestudy/viewTableOfContents.jsp", "View Table Of Contents"),
    VIEW_TABLE_OF_CONTENT_SERVLET ("/ViewTableOfContent", "View Table Of Contents Servlet"),
    VIEW_SECTION_DATA_ENTRY ("/WEB-INF/jsp/managestudy/viewSectionDataEntry.jsp", "View Section Data Entry"),
    VIEW_SECTION_DATA_PREVIEW ("/WEB-INF/jsp/managestudy/viewSectionDataPreview.jsp", "View Section Data Preview"),
    // YW 07-23-2007 << for preview in the issue000937
    VIEW_SECTION_DATA_ENTRY_PREVIEW ("/SectionPreview", "Preview CRF Servlet"),
    // YW >>

    VIEW_SECTION_DATA_ENTRY_PRINT ("/WEB-INF/jsp/managestudy/viewSectionDataEntryHtml.jsp", "View Section Data Entry Html"),

    VIEW_SECTION_DATA_ENTRY_SERVLET ("/ViewSectionDataEntry", "View Section Data Entry Servlet"),

    EXPORT_DATA_CUSTOM ("", "Dataset Export"),
    VIEW_EVENT_CRF_CONTENT ("/WEB-INF/jsp/managestudy/viewEventCRFContent.jsp", "View Event CRF Content"),

    UPDATE_STUDY_EVENT ("/WEB-INF/jsp/managestudy/updateStudyEvent.jsp", "Upate Study Event"),
    UPDATE_STUDY_EVENT_SERVLET ("/UpdateStudyEvent", "Upate Study Event"),
    UPDATE_STUDY_EVENT_SIGNED ("/WEB-INF/jsp/managestudy/updateStudyEventSigned.jsp", "Upate Study Event"),
    VIEW_STUDY_EVENTS ("/WEB-INF/jsp/managestudy/viewStudyEvents.jsp", "View Study Events"),

    VIEW_STUDY_EVENTS_PRINT ("/WEB-INF/jsp/managestudy/viewStudyEventsPrint.jsp", "View Study Events"),

    DELETE_CRF_VERSION ("/WEB-INF/jsp/admin/deleteCRFVersion.jsp", "delete CRF Version"),

    ADD_DISCREPANCY_NOTE ("/WEB-INF/jsp/submit/addDiscrepancyNote.jsp", "Add Discrepancy Note"),
    ADD_DISCREPANCY_NOTE_SERVLET ("/CreateDiscrepancyNote", "Add Discrepancy Note"),
    ADD_DISCREPANCY_NOTE_DONE ("/WEB-INF/jsp/submit/addDiscrepancyNoteDone.jsp", "Add Discrepancy Note Done"),
    ADD_DISCREPANCY_NOTE_SAVE_DONE ("/WEB-INF/jsp/submit/addDiscrepancyNoteSaveDone.jsp", "Add Discrepancy Note Save Done"),

    VIEW_DISCREPANCY_NOTE ("/WEB-INF/jsp/submit/viewDiscrepancyNote.jsp", "View Discrepancy Note"),
    VIEW_DISCREPANCY_NOTES_IN_STUDY ("/WEB-INF/jsp/managestudy/viewNotes.jsp", "View Discrepancy Notes in Study"),
    VIEW_DISCREPANCY_NOTES_IN_STUDY_PRINT ("/WEB-INF/jsp/managestudy/viewNotesPrint.jsp",
            "View Discrepancy Notes in Study Print"),
    VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET ("/ViewNotes", "View Discrepancy Notes in Study"),
    VIEW_SINGLE_NOTE ("/WEB-INF/jsp/managestudy/viewNote.jsp", "View Single Note"),

    LIST_EVENTS_FOR_SUBJECT ("/WEB-INF/jsp/submit/listEventsForSubject.jsp", "List Events For Subject"),
    LIST_EVENTS_FOR_SUBJECTS ("/WEB-INF/jsp/managestudy/listEventsForSubjects.jsp", "List Events For Subject"),
    INITIAL_DATA_ENTRY_NW ("/WEB-INF/jsp/submit/initialDataEntryNw.jsp", "Data Entry"),
    VIEW_SECTION_DATA_ENTRY_PRINT_GROUPS ("/WEB-INF/jsp/managestudy/viewGroupSectionsPrint.jsp",
            "Print View for Group Tables"),
    LIST_SUBJECT_DISC_NOTE ("/WEB-INF/jsp/managestudy/listSubjectDiscNote.jsp", "List Disc Notes By Subject"),
    LIST_DNOTES_FOR_CRF ("/WEB-INF/jsp/submit/listDNotesForCRF.jsp", "List Disc Notes By Subject and CRF"),
    CHOOSE_DOWNLOAD_FORMAT ("/WEB-INF/jsp/submit/chooseDownloadFormat.jsp", "Choose download format"),
    LIST_SUBJECT_DISC_NOTE_SERVLET ("/ListDiscNotesSubjectServlet", "List Disc Notes Servlet"),

    FILE_UPLOAD ("/WEB-INF/jsp/submit/uploadFile.jsp", "Form For File Uploading"),
    //UPLOAD_FILE_SERVLET ("/UploadFile", "Upload File"),
    DOWNLOAD_ATTACHED_FILE ("/WEB-INF/jsp/submit/downloadAttachedFile.jsp", "Download Attached File"),

    CONFIRM_LOCKING_CRF_VERSION ("/WEB-INF/jsp/managestudy/confirmLockingCRFVersion.jsp", "confirm locking crf version"),
    CONFIRM_UNLOCKING_CRF_VERSION ("/WEB-INF/jsp/managestudy/confirmUnlockingCRFVersion.jsp",
            "confirm unlocking crf version"),

    // MANAGE_STUDY_MODULE = new
    // Page("pages/studymodule", "Manage study");


    VIEW_ALL_SITE_DEFAULT_CRF_VERSIONS_PRINT ("/WEB-INF/jsp/managestudy/defaultAllSiteCrfVersionPrint.jsp",
            "View default crf versions print"),
    VIEW_DEFAULT_CRF_VERSIONS_PRINT ("/WEB-INF/jsp/managestudy/defaultCrfVersionPrint.jsp",
            "View default crf versions print"),
    VIEW_ALL_DEFAULT_CRF_VERSIONS_PRINT ("/WEB-INF/jsp/managestudy/defaultAllCrfVersionPrint.jsp",
            "View default crf versions print"),
     MANAGE_STUDY_MODULE ( "/pages/studymodule",null),
     VIEW_SECTION_DATA_ENTRY_SERVLET_REST_URL ("/ViewSectionDataEntryRESTUrlServlet", "View Section Data Entry Servlet for REST Url call"),
     PARTICIPANT_FORM_SERVLET("/WEB-INF/jsp/submit/participantFormServlet.jsp","Participant Form Servlet"),
     UPLOAD_CRF_DATA_TO_MIRTH ("/WEB-INF/jsp/submit/uploadFileToMirth.jsp", "Upload CRF Data To Mirth"),
     ENKETO_FORM_SERVLET("/WEB-INF/jsp/submit/enketoFormServlet.jsp","Enketo Form Servlet");
      	
  //  private final static String path = "/WEB-INF/jsp/";
  //  public final static String servletPath = "/OpenClinica";
    
	private String fileName;
	private String title;
	
	/**
     * Constructs the JSP Page instance
     * 
     * @param fileName The filename of the JSP page
     * @param title The title of the JSP page
     */
    private Page(String fileName, String title) {
        this.fileName = fileName;
        this.title = title;
    }

    /**
     * Gets the title attribute of the Page object.
     * 
     * @return The title value
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets the fileName attribute of the Page object.
     * 
     * @return The fileName value
     */
    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String newFileName) {
        this.fileName = newFileName;
    }

    
    
  /*  public static  Page setNewPage(String fileName, String title) {
    
    	for (Page p : Page.values())
    	 {
    		if( p.fileName == fileName &&	 p.title == title)
    			return p;
    	 }
    	return null;
    }*/

}
