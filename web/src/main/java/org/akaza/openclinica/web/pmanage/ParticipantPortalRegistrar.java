package org.akaza.openclinica.web.pmanage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.web.pmanage.Authorization;
import org.akaza.openclinica.web.pmanage.Study;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticipantPortalRegistrar {

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public String getRegistrationStatus(String studyOid) throws Exception {

        String pManageUrl = CoreResources.getField("portalURL");
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        URL eURL = new URL(pManageUrl + "/app/rest/oc/authorizations" + "?studyoid=" + studyOid + "&instanceurl=" + ocUrl);
        HttpURLConnection con = null;
        DataInputStream input = null;
        String response = null;

        try {
            con = (HttpURLConnection) eURL.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestMethod("GET");

            input = new DataInputStream(con.getInputStream());
            response = IOUtils.toString(input, "UTF-8");
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            // input.close();
        }
        if (response.isEmpty())
            return "";
        JSONObject json = JSONArray.fromObject(response).getJSONObject(0);
        if (json.isEmpty())
            return "";
        JSONObject authStatus = json.getJSONObject("authorizationStatus");

        if (!authStatus.isNullObject())
            return authStatus.getString("status");
        else
            return "";
    }

    public String registerStudy(String studyOid) throws Exception {

        String pManageUrl = CoreResources.getField("portalURL");
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        URL eURL = new URL(pManageUrl + "/app/rest/oc/authorizations" + "?studyoid=" + studyOid + "&instanceurl=" + ocUrl);
        HttpURLConnection con = null;
        DataOutputStream output = null;
        DataInputStream input = null;
        String response = null;
        try {
            Authorization authRequest = new Authorization();
            Study authStudy = new Study();
            authStudy.setStudyOid(studyOid);
            authStudy.setInstanceUrl(ocUrl);
            authRequest.setStudy(authStudy);
            JSONObject json = JSONObject.fromObject(authRequest);

            con = (HttpURLConnection) eURL.openConnection();

            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestProperty("Accept-Charset", "UTF-8");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestMethod("POST");

            con.setDoOutput(true);
            con.setDoInput(true);

            output = new DataOutputStream(con.getOutputStream());
            output.writeBytes(json.toString());
            output.flush();

            input = new DataInputStream(con.getInputStream());
            response = IOUtils.toString(input, "UTF-8");
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            output.close();
            // input.close();
        }

        JSONObject json = JSONObject.fromObject(response);
        if (json.isNullObject())
            return "";
        JSONObject authStatus = json.getJSONObject("authorizationStatus");
        if (!authStatus.isNullObject())
            return authStatus.getString("status");
        else
            return "";

    }

    public String getStudyHost(String studyOid) throws Exception {

        String pManageUrl = CoreResources.getField("portalURL");
        String ocUrl = CoreResources.getField("sysURL.base") + "rest2/openrosa/" + studyOid;
        URL eURL = new URL(pManageUrl + "/app/rest/oc/authorizations" + "?studyoid=" + studyOid + "&instanceurl=" + ocUrl);
        HttpURLConnection con = null;
        DataInputStream input = null;
        String response = null;

        try {
            con = (HttpURLConnection) eURL.openConnection();
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setRequestMethod("GET");

            input = new DataInputStream(con.getInputStream());
            response = IOUtils.toString(input, "UTF-8");
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.error(ExceptionUtils.getStackTrace(e));
        } finally {
            // input.close();
        }
        if (response.isEmpty())
            return "";
        JSONObject json = JSONObject.fromObject(response);
        if (json.isEmpty())
            return "";

        JSONObject studyHost = json.getJSONObject("study");

        if (!studyHost.isNullObject())
            return "http://" + studyHost.getString("host") + "." + pManageUrl.substring(7) + "#/login ";
        else
            return "";
    }

}
