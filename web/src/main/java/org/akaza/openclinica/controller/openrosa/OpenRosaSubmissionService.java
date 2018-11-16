package org.akaza.openclinica.controller.openrosa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;
import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.Study;
import org.akaza.openclinica.domain.user.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

@Component
public class OpenRosaSubmissionService {

    @Autowired
    private SubmissionProcessorChain submissionProcessorChain;

    @Autowired
    private StudyDao studyDao;

    @Autowired
    private CrfVersionDao crfVersionDao;

    @Transactional
    public void processRequest(Study study, HashMap<String, String> subjectContext, String requestBody, Errors errors, Locale locale,
            ArrayList<HashMap> listOfUploadFilePaths, SubmissionContainer.FieldRequestTypeEnum requestType,UserAccount userAccount) throws Exception {
        // Execute save as Hibernate transaction to avoid partial imports
        CrfVersion crfVersion = crfVersionDao.findByOcOID(subjectContext.get("crfVersionOID"));
        String requestPayload = parseSubmission(requestBody, crfVersion);
        runAsTransaction(study, requestPayload, subjectContext, errors, locale, listOfUploadFilePaths, requestType,userAccount);
    }

    @Transactional
    public void processFieldSubmissionRequest(Study study, HashMap<String, String> subjectContext, String instanceId, String requestBody, Errors errors,
            Locale locale, ArrayList<HashMap> listOfUploadFilePaths, SubmissionContainer.FieldRequestTypeEnum requestType,UserAccount userAccount) throws Exception {
        SubmissionContainer container = new SubmissionContainer(study, requestBody, subjectContext, errors, locale, listOfUploadFilePaths, requestType,
                instanceId);
        container.setUser(userAccount);
        container.setProcessorEnum(checkInitialInstanceIdSubmission(requestBody));
        container.setFieldSubmissionFlag(true);
        submissionProcessorChain.processSubmission(container);
    }

    private void runAsTransaction(Study study, String requestBody, HashMap<String, String> subjectContext, Errors errors, Locale locale,
                                  ArrayList<HashMap> listOfUploadFilePaths, SubmissionContainer.FieldRequestTypeEnum requestType, UserAccount userAccount) throws Exception {

        SubmissionContainer container = new SubmissionContainer(study, requestBody, subjectContext, errors, locale, listOfUploadFilePaths, requestType, null);
        container.setUser(userAccount);
        container.setProcessorEnum(ProcessorEnum.SUBMISSION_PROCESSOR);
        container.setFieldSubmissionFlag(false);
        submissionProcessorChain.processSubmission(container);

    }

    private String parseSubmission(String body, CrfVersion crfVersion) {
        if (crfVersion.getXform() != null && !crfVersion.getXform().equals("")) {
            body = body.substring(body.indexOf("<" + crfVersion.getXformName()));
            int length = body.indexOf(" ");
            body = body.replace(body.substring(body.lastIndexOf("<meta>"), body.lastIndexOf("</meta>") + 7), "");
            body = body.substring(0, body.lastIndexOf("</" + crfVersion.getXformName()) + length + 2);
            body = "<instance>" + body + "</instance>";
        } else {
            body = body.substring(body.indexOf("<F_"));
            int length = body.indexOf(" ");
            body = body.replace(body.substring(body.indexOf("<meta>"), body.indexOf("</meta>") + 7), "");
            body = body.substring(0, body.indexOf("</F_") + length + 2);
            body = "<instance>" + body + "</instance>";
        }
        return body;
    }

    private ProcessorEnum checkInitialInstanceIdSubmission(String body) {
        if (body.indexOf("<instanceID>") > 0 && body.indexOf("</instanceID>") > 0) {
            return ProcessorEnum.INSTANCE_ID_PROCESSOR;
        } else {
            return ProcessorEnum.FIELD_SUBMISSION_RPOCESSOR;
        }
    }

}
