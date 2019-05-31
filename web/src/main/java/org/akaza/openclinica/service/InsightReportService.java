package org.akaza.openclinica.service;

import java.util.HashMap;

public interface InsightReportService {

    String getFileName();

    String getFilePath(String label, String reportName, String filePath);

    void saveToFile(String content, String fullPath);

    String doPost(String api, HashMap<String, String> mapHeaders,
                  String body, HashMap<String, String> queries);

    String runReport(String username, String password, String insightURL, String[] participantLabels,
                     String fullPath, String reportId);
}
