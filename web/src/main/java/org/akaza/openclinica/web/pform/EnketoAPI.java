package org.akaza.openclinica.web.pform;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EnketoAPI {

	private String enketoURL = null;
	private String token = null;
	private String ocURL = null;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
	
	public EnketoAPI (String enketoURL, String apiToken, String ocURL)
	{
		this.enketoURL = enketoURL;
		this.token = apiToken;
		this.ocURL = ocURL;
	}
	
	public String getFormURL(String crfOID) throws Exception
	{
		String crfURL = "";

		try
		{
			URL eURL = new URL(enketoURL + "/survey");
			HttpURLConnection con = (HttpURLConnection)eURL.openConnection();
			String userPasswdCombo = new String(Base64.encodeBase64((token + ":").getBytes()));
			
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.setRequestMethod("POST");			
			con.setRequestProperty("Authorization", "Basic " + userPasswdCombo);
			con.setRequestProperty("Accept-Charset", "UTF-8");
			con.setDoOutput(true); 
			con.setDoInput(true);	
	
			String openClinicaURL = "server_url=" + ocURL + "&form_id=" + crfOID;
			DataOutputStream output = new DataOutputStream(con.getOutputStream());  
			output.writeBytes(openClinicaURL);
			output.flush();
			output.close();
		      	
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String currLine = null;
			while ((currLine = inputReader.readLine()) != null)
			{		
				if (currLine.contains(":"))
				{
					String tokenName = currLine.split(":")[0];
					if (tokenName.contains("url")) crfURL = currLine.substring(currLine.indexOf(":")+3,currLine.length()-2);
					else if (tokenName.contains("code"))
					{
						String code = currLine.split(":")[1];
						if (Integer.valueOf(code.trim()) > 299) throw new Exception("Error retrieving Enketo URL.  Received error code: " + code);
					}			
				}
			}
			inputReader.close();
		}
		catch (Exception e)
		{
			logger.debug(e.getMessage());
			logger.debug(ExceptionUtils.getStackTrace(e));
		}
		
		return crfURL;
	}
	
	public String getFormPreviewURL(String crfOID) throws Exception
	{
		String crfURL = "";

		try
		{
			URL eURL = new URL(enketoURL + "/survey/preview");
			HttpURLConnection con = (HttpURLConnection)eURL.openConnection();
			String userPasswdCombo = new String(Base64.encodeBase64((token + ":").getBytes()));
			
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);
			con.setRequestMethod("POST");			
			con.setRequestProperty("Authorization", "Basic " + userPasswdCombo);
			con.setRequestProperty("Accept-Charset", "UTF-8");
			con.setDoOutput(true); 
			con.setDoInput(true);	
	
			String openClinicaURL = "server_url=" + ocURL + "&form_id=" + crfOID;
			DataOutputStream output = new DataOutputStream(con.getOutputStream());  
			output.writeBytes(openClinicaURL);
			output.flush();
			output.close();
		      	
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String currLine = null;
			while ((currLine = inputReader.readLine()) != null)
			{		
				if (currLine.contains(":"))
				{
					String tokenName = currLine.split(":")[0];
					if (tokenName.contains("url")) crfURL = currLine.substring(currLine.indexOf(":")+3,currLine.length()-2);
					else if (tokenName.contains("code"))
					{
						String code = currLine.split(":")[1];
						if (Integer.valueOf(code.trim()) > 299) throw new Exception("Error retrieving Enketo URL.  Received error code: " + code);
					}			
				}
			}
			inputReader.close();
		}
		catch (Exception e)
		{
			logger.debug(e.getMessage());
			logger.debug(ExceptionUtils.getStackTrace(e));
		}
		
		return crfURL;
	}
	
}