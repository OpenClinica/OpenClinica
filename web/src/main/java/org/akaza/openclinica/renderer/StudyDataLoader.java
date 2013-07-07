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
  public static JSONArray ensureArray(JSON jsonToTest) {
    JSONArray jsonArray;
    if (jsonToTest == null) {
      return null;
    }
    if (jsonToTest instanceof JSONObject) {
      jsonArray = new JSONArray();
      jsonArray.add(jsonToTest);
    }
    else {
      jsonArray = (JSONArray)jsonToTest; 
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
    if (studyRenderer.app_studyDetails == null) {
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
  public void loadBasicDefinitiions() {
    if (studyRenderer.study.getJSONObject("BasicDefinitions") == null) {
      return;
    }
    studyRenderer.appBasicDefinitions =  new TreeMap<String,String>();
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
    JSONArray codeLists = StudyDataLoader.ensureArray(studyRenderer.study.getJSONObject("MetaDataVersion").getJSONArray("CodeList")); 
    if (codeLists == null) {
      return;
    } 
    for (int i=0;i< codeLists.size();i++) {
      List currentCodeList = new ArrayList();
      String codeListKey = codeLists.getJSONObject(i).getString("@OID"); 
      JSONArray codeListItems = codeLists.getJSONObject(i).getJSONArray("CodeListItem");
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
  

}