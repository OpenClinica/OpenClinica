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
	

}