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
  } 
  
  
  
  
  /* loadStudyDetails()
    this.loadStudyDetails = function() {
  debug("loading study details", util_logDebug );
   app_studyDetails = this.study["MetaDataVersion"]["OpenClinica:StudyDetails"];
      if (!app_studyDetails) {
        return;
      }
      var studyParamList = app_studyDetails["OpenClinica:StudyParameterConfiguration"]["OpenClinica:StudyParameterListRef"];
      app_collectSubjectDOB =  this.getStudyParamValue(studyParamList, "SPL_collectDob");
      app_personIDRequired = this.getStudyParamValue(studyParamList, "SPL_subjectPersonIdRequired");
      app_showPersonID = this.getStudyParamValue(studyParamList, "SPL_personIdShownOnCRF");
      app_interviewerNameRequired = this.getStudyParamValue(studyParamList, "SPL_interviewerNameRequired");
      app_interviewDateRequired = this.getStudyParamValue(studyParamList, "SPL_interviewDateRequired");
      app_secondaryLabelViewable = this.getStudyParamValue(studyParamList, "SPL_secondaryLabelViewable");
      app_eventLocationRequired = this.getStudyParamValue(studyParamList, "SPL_eventLocationRequired");
      app_secondaryIDs = app_studyDetails["OpenClinica:StudyDescriptionAndStatus"]["@SecondaryIDs"];
    }
     */
  

}