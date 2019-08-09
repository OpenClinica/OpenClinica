package org.akaza.openclinica.service;

import org.akaza.openclinica.dao.core.CoreResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Rest client to call dicom-service.
 * @author svadla@openclinica.com
 */
@Service
public class DicomServiceClient {
    private static final String SBS_URL = CoreResources.getField("SBSUrl");
    private static final String DICOM_SERVICE_URL = getDicomServiceUrl();

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public ResponseEntity<Object> uploadDicom(String accessToken, MultipartFile dicomFile, String participantId, String accessionId, String targetPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        try {
            Path tempDicomFile = Files.createTempFile("dicomFile" + System.nanoTime(), ".zip");
            Files.write(tempDicomFile, dicomFile.getBytes());
            File dicomFileToUpload = tempDicomFile.toFile();
            requestBody.add("dicomFile", new FileSystemResource(dicomFileToUpload));
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestBody.add("participantId", participantId);
        requestBody.add("accessionId", accessionId);
        requestBody.add("targetPath", targetPath);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String uploadDicomUrl = DICOM_SERVICE_URL + "/dicom";
        try {
            logger.debug("Calling dicom-service to upload dicom file");
            return restTemplate.postForEntity(uploadDicomUrl, requestEntity, Object.class);
        } catch (HttpClientErrorException e) {
            logger.error("Failed to upload dicom file to dicom-service", e);
            return ResponseEntity
                    .status(e.getRawStatusCode())
                    .body(e.getResponseBodyAsString());
        }
    }

    private static String getDicomServiceUrl() {
        return UriComponentsBuilder.fromUriString(SBS_URL)
                .replacePath("/dicom-service/api")
                .build()
                .toUriString();

    }
}
