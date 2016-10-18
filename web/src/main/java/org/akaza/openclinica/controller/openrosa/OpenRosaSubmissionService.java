package org.akaza.openclinica.controller.openrosa;

import org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.domain.datamap.CrfVersion;
import org.akaza.openclinica.domain.datamap.Study;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static org.akaza.openclinica.controller.openrosa.SubmissionProcessorChain.ProcessorEnum;

@Component
public class OpenRosaSubmissionService {

    @Autowired
    private SubmissionProcessorChain submissionProcessorChain;

    @Autowired
    private StudyDao studyDao;
    
    @Autowired
    private CrfVersionDao crfVersionDao;
    
    @Transactional
    public void processRequest(Study study, HashMap<String,String> subjectContext, String requestBody, Errors errors, Locale locale, ArrayList <HashMap> listOfUploadFilePaths) throws Exception {
        // Execute save as Hibernate transaction to avoid partial imports
        CrfVersion crfVersion = crfVersionDao.findByOcOID(subjectContext.get("crfVersionOID"));
        String requestPayload = parseSubmission(requestBody, crfVersion);
        runAsTransaction(study, requestPayload, subjectContext, errors, locale ,listOfUploadFilePaths);
    }

    @Transactional
    public void processFieldSubmissionRequest(Study study, HashMap<String,String> subjectContext, String instanceId, String requestBody, Errors errors, Locale locale, ArrayList <HashMap> listOfUploadFilePaths) throws Exception {
        // Execute save as Hibernate transaction to avoid partial imports
        // is this the initial form submission with just instanceId?
        CrfVersion crfVersion = crfVersionDao.findByOcOID(subjectContext.get("crfVersionOID"));
        processFieldPayload(study, requestBody, subjectContext, errors, locale ,listOfUploadFilePaths);
    }

    private void runAsTransaction(Study study, String requestBody, HashMap<String, String> subjectContext, Errors errors, Locale locale,ArrayList <HashMap> listOfUploadFilePaths) throws Exception{

        SubmissionContainer container = new SubmissionContainer(study,requestBody,subjectContext,errors,locale ,listOfUploadFilePaths);
        container.setProcessorEnum(ProcessorEnum.SUBMISSION_PROCESSOR);
        submissionProcessorChain.processSubmission(container, false);

    }

    private void processFieldPayload(Study study, String requestBody, HashMap<String, String> subjectContext, Errors errors, Locale locale,ArrayList <HashMap> listOfUploadFilePaths) throws Exception{
        SubmissionContainer container = new SubmissionContainer(study,requestBody,subjectContext,errors,locale ,listOfUploadFilePaths);
        container.setProcessorEnum(checkInitialInstanceIdSubmission(requestBody));
        submissionProcessorChain.processSubmission(container, true);

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
        if (body.indexOf("<instanceID>")  > 0 &&
                body.indexOf("</instanceID>")  > 0) {
            return ProcessorEnum.INSTANCE_ID_PROCESSOR;
        } else {
            return ProcessorEnum.FIELD_SUBMISSION_RPOCESSOR;
        }
        /*
        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(body)));
            final XPathExpression xpath = XPathFactory.newInstance().newXPath().compile("//*[count(./*) = 0]");
            final NodeList nodeList = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);
            for(int i = 0; i < nodeList.getLength(); i++) {
                final Element el = (Element) nodeList.item(i);
                System.out.println(el.getNodeName());
                System.out.println(el.getTextContent());
                if (el.getNodeName().equalsIgnoreCase("instanceID"))
                    return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
