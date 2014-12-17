package org.akaza.openclinica.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.web.pmanage.Authorization;
import org.akaza.openclinica.web.pmanage.Study;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/pmanage")
public class ParticipantPortalRegController {

	@Autowired
	private CoreResources core;

	@Autowired
	ServletContext context;

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	@RequestMapping(value = "/regStatus", method = RequestMethod.GET)
	public @ResponseBody String getRegistrationStatus(@RequestParam("studyoid") String studyOid)
			throws Exception {
		
		String pManageUrl = CoreResources.getField("portalURL");
		String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
		URL eURL = new URL(pManageUrl + "/app/rest/oc/authorizations" + "?studyoid=" + studyOid + "&instanceurl=" + ocUrl);
		HttpURLConnection con = null;
		DataInputStream input = null;
		String response = null;
		
		try
		{					
			con = (HttpURLConnection)eURL.openConnection();		
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.setRequestMethod("GET");	
	
			input = new DataInputStream( con.getInputStream());
			response = IOUtils.toString(input, "UTF-8");
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		finally{
			input.close();
		}
		
		JSONArray jsonArray = JSONArray.fromObject(response);
		if (jsonArray.size() == 0) return "";
		JSONObject authStatus = jsonArray.getJSONObject(0).getJSONObject("authorizationStatus");
		if (!authStatus.isNullObject()) return authStatus.getString("status");
		else return "NULLAUTH";
	}

	@RequestMapping(value = "/regSubmit", method = RequestMethod.GET)
	public @ResponseBody String registerStudy(@RequestParam("studyoid") String studyOid)
			throws Exception {
		
		String pManageUrl = CoreResources.getField("portalURL");
		String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
		URL eURL = new URL(pManageUrl + "/app/rest/oc/authorizations" + "?studyoid=" + studyOid + "&instanceurl=" + ocUrl);
		HttpURLConnection con = null;
		DataOutputStream output = null;
		try
		{
			Authorization authRequest = new Authorization();
			Study authStudy = new Study();
			authStudy.setStudyOid(studyOid);
			authStudy.setInstanceUrl(ocUrl);
			authRequest.setStudy(authStudy);
			JSONObject json = JSONObject.fromObject(authRequest);
				
			con = (HttpURLConnection)eURL.openConnection();
			
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
	        con.setRequestProperty("Accept-Charset", "UTF-8");
	        con.setRequestProperty("Content-Type", "application/json");
			con.setRequestMethod("POST");	
	
			con.setDoOutput(true);
			output = new DataOutputStream(con.getOutputStream ());
			output.writeBytes(json.toString());
			output.flush();
			output.close();
		}
		catch (Exception e)
		{
			logger.error(e.getMessage());
			logger.error(ExceptionUtils.getStackTrace(e));
		}
		finally{
			output.close();
		}
		if (con.getResponseCode() >= 400) 
		{
			logger.debug("Error sending PManage registration request.  Received response code: " + con.getResponseCode() + ".  Message: " + con.getResponseMessage() + ".");
			return "REQUEST FAILED";
		}
		else return "SUBMITTED";
		
	}

}

