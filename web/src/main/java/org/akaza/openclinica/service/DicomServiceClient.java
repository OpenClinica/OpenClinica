package org.akaza.openclinica.service;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.web.util.ErrorConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Rest client to call dicom-service.
 * @author svadla@openclinica.com
 */
@Service
public class DicomServiceClient {
    private static final String SBS_URL = CoreResources.getField("SBSUrl");
    private static final String DICOM_SERVICE_URL = getDicomServiceUrl();
    private static final String DICOM_PATH = "/dicom";
    private static final String DICOM_FILE_PARAM = "dicomFile";
    private static final String PARTICIPANT_ID_PARAM = "participantId";
    private static final String ACCESSION_ID_PARAM = "accessionId";
    private static final String TARGET_PATH_PARAM = "targetPath";

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    /**
     * Uploads the given DICOM file to dicom-service.
     * @param accessToken access token used for authenticating with dicom-service
     * @param dicomFile file to upload
     * @param participantId participant id
     * @param accessionId accession id
     * @param targetPath path to the item for saving the result of upload
     * @return the response returned by dicom-service
     */
    public ResponseEntity<String> uploadDicom(String accessToken, MultipartFile dicomFile, String participantId, String accessionId, String targetPath) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        try {
            Path tempDicomFile = Files.createTempFile("dicomFile" + System.nanoTime(), ".zip");
            Files.write(tempDicomFile, dicomFile.getBytes());
            File dicomFileToUpload = tempDicomFile.toFile();
            requestBody.add(DICOM_FILE_PARAM, new FileSystemResource(dicomFileToUpload));
        } catch (IOException e) {
            logger.error("Unable to read the zip file", e);
            return ResponseEntity.badRequest()
                    .body(ErrorConstants.ERR_UNABLE_TO_READ_FILE);
        }
        requestBody.add(PARTICIPANT_ID_PARAM, participantId);
        requestBody.add(ACCESSION_ID_PARAM, accessionId);
        requestBody.add(TARGET_PATH_PARAM, targetPath);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();
        String uploadDicomUrl = DICOM_SERVICE_URL + DICOM_PATH;
        try {
            logger.debug("Calling dicom-service to upload dicom file");
            restTemplate.postForEntity(uploadDicomUrl, requestEntity, Object.class);
            // Returning 200 instead of the 202 returned by dicom-service as nginx doesn't seem to like the 202 response and closes the
            // connection prematurely
            return ResponseEntity.ok().build();
        } catch (HttpClientErrorException e) {
            logger.error("Failed to upload dicom file to dicom-service", e);
            return ResponseEntity
                    .status(e.getRawStatusCode())
                    .body(e.getResponseBodyAsString());
        }
    }

    /**
     * Retrieves the view url used for viewing DICOM images.
     * @param accessToken access token used for authenticating with dicom-service
     * @param participantId participant id
     * @param accessionId accession id
     * @return the url to view DICOM images.
     */
    public String getDicomViewUrl(String accessToken, String participantId, String accessionId) {

        if (StringUtils.isBlank(participantId) && StringUtils.isBlank(accessionId)) {
            logger.error("Participant Id and Accession Id cannot be blank");
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        String viewDicomUrl = DICOM_SERVICE_URL + DICOM_PATH;
        UriComponentsBuilder viewDicomUrlBuilder = UriComponentsBuilder.fromHttpUrl(viewDicomUrl);

        if (StringUtils.isNotBlank(participantId)) {
            viewDicomUrlBuilder.queryParam(PARTICIPANT_ID_PARAM, participantId);
        }
        if (StringUtils.isNotBlank(accessionId)) {
            viewDicomUrlBuilder.queryParam(ACCESSION_ID_PARAM, accessionId);
        }

        RestTemplate restTemplate = new RestTemplate();
        try {
            logger.debug("Calling dicom-service to retrieve dicom view url");
            ResponseEntity<String> response = restTemplate.exchange(viewDicomUrlBuilder.toUriString(), HttpMethod.GET, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Failed to retrieve dicom view url", e);
            return null;
        }
    }

    /**
     * @return the url to call dicom-service.
     */
    private static String getDicomServiceUrl() {
        return UriComponentsBuilder.fromUriString(SBS_URL)
                .replacePath("/dicom-service/api")
                .build()
                .toUriString();

    }
}
