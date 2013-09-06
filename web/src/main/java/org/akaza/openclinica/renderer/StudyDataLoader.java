/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2013 OpenClinica
 */

package org.akaza.openclinica.renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import freemarker.template.Configuration;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

public class StudyDataLoader {


  public StudyDataLoader(StudyRenderer studyRenderer) {
    this.studyRenderer = studyRenderer;
  }
  
  private StudyRenderer studyRenderer;
  
  
  /* util_ensureArray(jsonObjectToTest)
  * A kind of factory function for the different study
  * rendering scenarios.
  * @param jsonObjectToTest: The passed in json object
  * @return a json object, json array, or undefined
  */ 
  public static JSONArray ensureArray(JSONObject jsonToTest, String jsonKeyToTest) {
    JSONArray jsonArray = null;
    if (jsonToTest.isNullObject()) {
      return null;
    }
    if (jsonToTest.get(jsonKeyToTest) instanceof JSONObject) {
      jsonArray = new JSONArray();
      jsonArray.add(jsonToTest.getJSONObject(jsonKeyToTest));
    }
    else if (jsonToTest.get(jsonKeyToTest) instanceof JSONArray) {
      jsonArray = (JSONArray)jsonToTest.getJSONArray(jsonKeyToTest); 
    }
    return jsonArray;
  } 
  
  
   /* getStudyParamValue(studyParamList, listId) 
    * A convenience function to get the study detail parameter value
    */
  public String getStudyParamValue(JSONArray studyParamList, String listId) {
      for (int i=0;i< studyParamList.size();i++) {
        if ((studyParamList.getJSONObject(i).getString("@StudyParameterListID")).equals(listId)) {
        return studyParamList.getJSONObject(i).getString("@Value");
      }
    }
    return null;  
  }
  
  /* loadStudyDetails()
   */
  public void loadStudyDetails() {
    studyRenderer.app_studyDetails = studyRenderer.study.getJSONObject("MetaDataVersion").getJSONObject("OpenClinica:StudyDetails");
    
    if (studyRenderer.app_studyDetails.isNullObject()) {
      return;
    }
    if (studyRenderer.app_studyDetails.getJSONObject("OpenClinica:StudyParameterConfiguration").isNullObject()) {
      return;
    }
    if ((studyRenderer.app_studyDetails.getJSONObject("OpenClinica:StudyParameterConfiguration").getJSONArray("OpenClinica:StudyDetails")).isEmpty()) {
      return;
    }
    
    
    
    JSONArray studyParamList = studyRenderer.app_studyDetails.getJSONObject("OpenClinica:StudyParameterConfiguration").getJSONArray("OpenClinica:StudyDetails"); 
    studyRenderer.app_collectSubjectDOB = getStudyParamValue(studyParamList, "SPL_collectDob");
    studyRenderer.app_personIDRequired = getStudyParamValue(studyParamList, "SPL_collectDob");;
    studyRenderer.app_showPersonID = getStudyParamValue(studyParamList, "SPL_collectDob");;
    studyRenderer.app_interviewerNameRequired = getStudyParamValue(studyParamList, "SPL_collectDob");;
    studyRenderer.app_interviewDateRequired = getStudyParamValue(studyParamList, "SPL_collectDob");;
    studyRenderer.app_secondaryLabelViewable = getStudyParamValue(studyParamList, "SPL_collectDob");;
    studyRenderer.app_eventLocationRequired = getStudyParamValue(studyParamList, "SPL_collectDob");;
    studyRenderer.app_secondaryIDs = getStudyParamValue(studyParamList, "SPL_collectDob");;
  } 
  
  
  /* loadBasicDefinitions()
  */
  public void loadBasicDefinitions() {
    studyRenderer.appBasicDefinitions =  new TreeMap<String,String>();
    if (studyRenderer.study.getJSONObject("BasicDefinitions") == null || studyRenderer.study.getJSONObject("BasicDefinitions").getJSONArray("MeasurementUnit") == null) {
      return;
    }
    JSONArray basicDefinitions = studyRenderer.study.getJSONObject("BasicDefinitions").getJSONArray("MeasurementUnit");
    
    for (int i=0;i< basicDefinitions.size();i++) {
      String key = basicDefinitions.getJSONObject(i).getString("@OID");
      String value = basicDefinitions.getJSONObject(i).getString("@Name");
      studyRenderer.appBasicDefinitions.put(key, value);
    }
  }
  
  
  /* loadCodeLists()
   */
  public void loadCodeLists() {
    studyRenderer.appCodeLists = new TreeMap<String,List>();
    JSONArray codeLists = StudyDataLoader.ensureArray(studyRenderer.study.getJSONObject("MetaDataVersion"), "CodeList"); 
    if (codeLists == null) {
      return;
    } 
    for (int i=0;i< codeLists.size();i++) {
      List currentCodeList = new ArrayList();
      String codeListKey = codeLists.getJSONObject(i).getString("@OID"); 
      JSONArray codeListItems = StudyDataLoader.ensureArray(codeLists.getJSONObject(i), "CodeListItem");
      for (int j=0;j< codeListItems.size();j++) {
        JSONObject codeListItem = codeListItems.getJSONObject(j);
        String id = codeListItem.getString("@CodedValue");
        String label = codeListItem.getJSONObject("Decode").getString("TranslatedText");
        CodeListItem currentCodeListItem = new CodeListItem(id,label);
        currentCodeList.add(currentCodeListItem);
      }
      studyRenderer.appCodeLists.put(codeListKey, currentCodeList);
    }
  }
  
  
  /* loadMultiSelectLists()
   */
  public void loadMultiSelectLists() {
    studyRenderer.appMultiSelectLists = new TreeMap<String,List>();
    JSONArray multiSelectLists = StudyDataLoader.ensureArray(studyRenderer.study.getJSONObject("MetaDataVersion"), "OpenClinica:MultiSelectList"); 
    if (multiSelectLists == null) {
      return;
    } 
    for (int i=0;i< multiSelectLists.size();i++) {
      List currentMultiSelectList = new ArrayList();
      String multiSelectListKey = multiSelectLists.getJSONObject(i).getString("@ID"); 
      JSONArray multiSelectListItems = StudyDataLoader.ensureArray(multiSelectLists.getJSONObject(i), "OpenClinica:MultiSelectListItem");
      for (int j=0;j< multiSelectListItems.size();j++) {
        JSONObject multiSelectListItem = multiSelectListItems.getJSONObject(j);
        String id = multiSelectListItem.getString("@CodedOptionValue");
        String label = multiSelectListItem.getJSONObject("Decode").getString("TranslatedText");
        CodeListItem currentMultiSelectListItem = new CodeListItem(id,label);
        currentMultiSelectList.add(currentMultiSelectListItem);
      }
      studyRenderer.appMultiSelectLists.put(multiSelectListKey, currentMultiSelectList);
    }
  }
  
  
  /* loadItemGroupDefs(formDef)
   * Associate all Items with their ItemGroups
   */
  public void loadItemGroupDefs(JSONObject formDef) {
  
    studyRenderer.appItemGroupDefs = new TreeMap<String,ItemGroup>();
    studyRenderer.appItemGroupMap = new TreeMap<String,Item>();
    JSONArray itemGroupDefs = StudyDataLoader.ensureArray(studyRenderer.study.getJSONObject("MetaDataVersion"), "ItemGroupDef"); 
    if (itemGroupDefs == null) {
      return;
    } 
    
    for (int i=0;i< itemGroupDefs.size();i++) {
      JSONObject itemGroupDef = itemGroupDefs.getJSONObject(i);
      String itemGroupKey = itemGroupDef.getString("@OID");
      String itemGroupName = itemGroupDef.getString("@Name");
      JSONObject itemGroupDetails = itemGroupDef.getJSONObject("OpenClinica:ItemGroupDetails"); 
      int repeatNumber = 1;
      int repeatMax = 1;
      String groupHeader = "";
      //String groupHeader = itemGroupDetails.getJSONObject("OpenClinica:PresentInForm").getString("OpenClinica:ItemGroupHeader"); 
      JSONArray presentInForm = StudyDataLoader.ensureArray(itemGroupDetails, "OpenClinica:PresentInForm"); 
      for (int j=0;j< presentInForm.size();j++) {
        if ((presentInForm.getJSONObject(j).getString("@FormOID")).equals(formDef.getString("@OID"))) {
          repeatNumber = presentInForm.getJSONObject(j).getJSONObject("OpenClinica:ItemGroupRepeat").getInt("@RepeatNumber");
          repeatMax = presentInForm.getJSONObject(j).getJSONObject("OpenClinica:ItemGroupRepeat").getInt("@RepeatMax");
          boolean repeating = ParseUtil.parseYesNo(itemGroupDef.getString("@Repeating"));
          ItemGroup currentItemGroup = new ItemGroup(repeatNumber, repeating, repeatMax, itemGroupName, groupHeader);
          studyRenderer.appItemGroupDefs.put(itemGroupKey, currentItemGroup); 
          JSONArray itemRefs = StudyDataLoader.ensureArray(itemGroupDef, "ItemRef"); 
          
          for (int k=0;k< itemRefs.size();k++) {
            String itemKey = itemRefs.getJSONObject(k).getString("@ItemOID");
            int orderNumber = itemRefs.getJSONObject(k).getInt("@OrderNumber");
            boolean mandatory = ParseUtil.parseYesNo(itemRefs.getJSONObject(k).getString("@Mandatory"));
            Item currentItem = new Item(orderNumber, mandatory, itemGroupKey, itemRefs.size());
            studyRenderer.appItemGroupMap.put(itemKey, currentItem); 
          }
          break;
        }
      }
    }
  }

  
    
  /* loadStudyEventDefs()
   * Load all StudyEvents
   */ 
  public void loadStudyEventDefs() {
    studyRenderer.appStudyEventDefs = StudyDataLoader.ensureArray(studyRenderer.study.getJSONObject("MetaDataVersion"), "StudyEventDef"); 
  }
   
   
  /* loadItemDefs()
   * Load all ItemDefs
   */
  public void loadItemDefs() {
    studyRenderer.appItemDefs = StudyDataLoader.ensureArray(studyRenderer.study.getJSONObject("MetaDataVersion"), "ItemDef"); 
  }
  
  
  /* loadFormDefs()
   * Load all FormDefs
   */
  public void loadFormDefs() {
    studyRenderer.appFormDefs = StudyDataLoader.ensureArray(studyRenderer.study.getJSONObject("MetaDataVersion"), "FormDef"); 
  }
   
   
  
  
  /* loadStudyLists()
  */
  public void loadStudyLists() {
    loadBasicDefinitions();
    loadCodeLists();
    loadMultiSelectLists();
    loadItemDefs();
    loadFormDefs();
    loadStudyEventDefs();
    loadStudyDetails();
  }


}