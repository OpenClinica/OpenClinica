package org.akaza.openclinica.web.pform;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EnketoAPI {

    private String enketoURL = null;
    private String token = null;
    private String ocURL = null;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public EnketoAPI (EnketoCredentials credentials)
    {
        this.enketoURL = credentials.getServerUrl();
        this.token = credentials.getApiKey();
        this.ocURL = credentials.getOcInstanceUrl();
    }

    public String getFormURL(String crfOID) throws Exception
    {
        URL eURL = new URL(enketoURL + "/api/v1/survey/iframe");
        return getURL(eURL,crfOID,"url");
    }

    public String getFormPreviewURL(String crfOID) throws Exception
    {
        URL eURL = new URL(enketoURL + "/api/v1/survey/preview");
        return getURL(eURL,crfOID,"preview_url");
    }

    private String getURL(URL url, String crfOID, String responseName) throws Exception
    {
        String crfURL = "";

        try
        {
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
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

            String response = IOUtils.toString(con.getInputStream(), "UTF-8");
            JSONObject json = JSONObject.fromObject(response);
            if (json.getString("code") != null && Integer.valueOf(json.getString("code")) > 204) throw new Exception("Error retrieving Enketo URL.  Received error code: " + json.getString("code"));
            else if (json.getString(responseName) != null) crfURL = json.getString(responseName);
            else throw new Exception("Error retrieving Enketo URL.  Did not receive a valid response from server.");
        }
        catch (Exception e)
        {
            logger.debug(e.getMessage());
            logger.debug(ExceptionUtils.getStackTrace(e));
            throw new Exception(e);
        }
        return crfURL;
    }
    
    

}