package org.akaza.openclinica.web.pmanage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

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
        JSONObject json = JSONObject.fromObject(response);
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
        StringBuffer urlBuilder = buildUrl(pManageUrl, studyHost);

        if (!studyHost.isNullObject())
            return "http://" + urlBuilder.toString() + "/#/login";
        else
            return "";
    }

    public StringBuffer buildUrl(String pManageUrl, JSONObject studyHost) throws Exception {

        String[] arrayUrl = pManageUrl.split(":");
        String tmpProtocol = arrayUrl[0];
        String tmpUrl = arrayUrl[1];
        String tmpPort = arrayUrl[2];
        tmpUrl = tmpUrl.substring(2);
        String[] tmpArr = tmpUrl.split("\\.");
        String[] tmpMore = new String[tmpArr.length + 1];
        if (tmpArr.length > 2) {
            tmpArr[0] = studyHost.getString("host");
        } else {
            tmpMore[0] = studyHost.getString("host");
            System.arraycopy(tmpArr, 0, tmpMore, 1, tmpArr.length);
            tmpArr = tmpMore;
        }
        StringBuffer urlBuilder = new StringBuffer();
        for (int i = 0; i < tmpArr.length; i++) {
            urlBuilder.append(tmpArr[i]);
            if (i != tmpArr.length - 1) {
                urlBuilder.append(".");
            } else {
                urlBuilder.append(":" + tmpPort);
            }
        }
        return urlBuilder;
    }
}
