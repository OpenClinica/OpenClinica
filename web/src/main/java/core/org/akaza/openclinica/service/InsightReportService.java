package core.org.akaza.openclinica.service;

public interface InsightReportService {

    String getFileName();

    String getFilePath(String label, String reportName, String filePath);

    void saveToFile(String content, String fullPath);

    String runReport(String username, String password, String insightURL, String[] participantLabels,
                     String fullPath, String reportId);
}
