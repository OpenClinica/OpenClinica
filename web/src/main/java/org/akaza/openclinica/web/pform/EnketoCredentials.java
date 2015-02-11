package org.akaza.openclinica.web.pform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.akaza.openclinica.dao.core.CoreResources;
import org.apache.commons.io.IOUtils;

public class EnketoCredentials {
	private String serverUrl = null;
	private String apiKey = null;
	private String ocInstanceUrl = null;
	
	private EnketoCredentials()
	{
		
	}

	public static EnketoCredentials getInstance(String studyOid) throws Exception
	{
		EnketoCredentials credentials = new EnketoCredentials();		
		
		String pManageUrl = CoreResources.getField("portalURL");
		String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
		URL eURL = new URL(pManageUrl + "/app/rest/oc/authorizations" + "?studyoid=" + studyOid + "&instanceurl=" + ocUrl);
		HttpURLConnection con = (HttpURLConnection)eURL.openConnection();
		
		con.setConnectTimeout(5000);
		con.setReadTimeout(5000);
		con.setRequestMethod("GET");	

		BufferedReader inputReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String response = IOUtils.toString(con.getInputStream(), "UTF-8");

		JSONObject json = JSONObject.fromObject(response);
	//	JSONObject json = JSONArray.fromObject(response).getJSONObject(0);
		credentials.setServerUrl(json.getString("pformUrl"));
		credentials.setApiKey(json.getString("pformApiKey"));
		credentials.setOcInstanceUrl(ocUrl);		
		
		return credentials;
	}
	public String getServerUrl() {
		return serverUrl;
	}
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getOcInstanceUrl() {
		return ocInstanceUrl;
	}
	public void setOcInstanceUrl(String ocInstanceUrl) {
		this.ocInstanceUrl = ocInstanceUrl;
	}
	
}
