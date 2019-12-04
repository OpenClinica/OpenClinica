package core.org.akaza.openclinica.service;

import org.akaza.openclinica.service.UserService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

@Service( "insightReportService" )
public class InsightReportServiceImpl implements InsightReportService {

    @Autowired
    private UserService userService;

    private String fileName;
    public static final String UNDERSCORE = "_";

    SimpleDateFormat sdf_fileName = new SimpleDateFormat("yyyy-MM-dd'-'HHmmssSSS'Z'");
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getFilePath(String label, String reportNames, String filePath) {
        sdf_fileName.setTimeZone(TimeZone.getTimeZone("GMT"));
        fileName = label + UNDERSCORE + reportNames +
                UNDERSCORE + sdf_fileName.format(new Date()) + ".csv";
        return filePath + File.separator + fileName;
    }

    @Override
    public void saveToFile(String content, String fullPath) {
        FileOutputStream fop = null;
        File file;
        try {

            file = new File(fullPath);
            fop = new FileOutputStream(file);

            // get the content in bytes
            byte[] contentInBytes = content.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            logger.error("Error creating file : ", e);
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                logger.error("Error closing output stream : ", e);
            }
        }
    }

    private String doPost(String api, HashMap<String, String> mapHeaders, String body, HashMap<String, String> queries) {
        try {
            HttpHeaders headers = new HttpHeaders();
            for (String i : mapHeaders.keySet()) {
                headers.add(i, mapHeaders.get(i).toString());
            }

            HttpEntity<String> request;
            if (body != null) {
                request = new HttpEntity<>(body, headers);
            } else {
                request = new HttpEntity<String>(headers);
            }
            RestTemplate rest = new RestTemplate();
            ResponseEntity<String> response;
            if (queries != null) {
                response = rest.postForEntity(api, request, String.class, queries);
            } else {
                response = rest.postForEntity(api, request, String.class);
            }
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error sending request : ", e);
            return null;
        }
    }

    @Override
    public String runReport(String username, String password, String insightURL, String[] participantLabels,
                            String fullPath, String reportId) {
        // get Session ID
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        String jsonCredential = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        String strSession = doPost(insightURL + "/api/session", headers,
                jsonCredential, null);

        if (strSession == null) {
            return null;
        }

        JSONObject session = new JSONObject(strSession);

        // get content from insight
        headers = new HashMap<String, String>();
        headers.put("X-Metabase-Session", session.get("id").toString());

        String origin = "{\"type\":\"category\"," +
                "\"target\":[\"dimension\",[\"template-tag\",\"original_id\"]]," +
                "\"value\":[\"" + participantLabels[0] + "\"]}";
        String replica = "{\"type\":\"category\"," +
                "\"target\":[\"dimension\",[\"template-tag\",\"replicate_id\"]]," +
                "\"value\":[\"" + participantLabels[1] + "\"]}";

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("origin", origin);
        params.put("replica", replica);

        String url = insightURL + "/api/card/" + reportId + "/query/csv?parameters=[{origin},{replica}]";
        String response = doPost(url, headers, null, params);
        return response;
    }
}