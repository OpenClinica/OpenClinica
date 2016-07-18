package org.akaza.openclinica.ws.client;

import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.managestudy.SubjectTransferBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.service.subject.SubjectServiceInterface;
import org.springframework.util.xml.DomUtils;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import java.util.Date;
import java.util.List;

public class SoapCreateSubject extends WebServiceGatewaySupport implements SubjectServiceInterface {

    private static final String NAMESPACE_URI = "http://openclinica.org/create-subject";

    public String createSubject(SubjectBean subjectBean, StudyBean studyBean, Date enrollmentDate, String secondaryId) {
        Element requestElement = prepareRequest(subjectBean, studyBean);
        DOMSource source = new DOMSource(requestElement);
        DOMResult result = new DOMResult();
        getWebServiceTemplate().sendSourceAndReceiveToResult(source, result);
        return processResponse(result.getNode());
    }

    private Element prepareRequest(SubjectBean subjectBean, StudyBean studyBean) {
        Document document = getDocument();
        Element requestElement = document.createElementNS(NAMESPACE_URI, "createSubjectRequest");
        requestElement.appendChild(mapSubject(document, subjectBean));
        requestElement.appendChild(mapStudy(document, studyBean));
        return requestElement;
    }

    private Element mapSubject(Document document, SubjectBean subject) {
        Element subjectElement = document.createElementNS(NAMESPACE_URI, "subject");
        subjectElement.setAttribute("uniqueIdentifier", subject.getUniqueIdentifier());
        return subjectElement;
    }

    private Element mapStudy(Document document, StudyBean study) {
        Element subjectElement = document.createElementNS(NAMESPACE_URI, "study");
        subjectElement.setAttribute("identifier", study.getIdentifier());
        return subjectElement;
    }

    private Document getDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private String processResponse(Node node) {
        Element responseElement = (Element) node.getFirstChild();
        return mapRewardConfirmation(DomUtils.getChildElementByTagName(responseElement, "result"));
    }

    @SuppressWarnings("unchecked")
    private String mapRewardConfirmation(Element confirmationElement) {
        String confirmationNumber = confirmationElement.getAttribute("success");
        return confirmationNumber;
    }

    public boolean validate(SubjectTransferBean subjectTransferBean) {
        // TODO Auto-generated method stub
        return false;
    }
    
    public List<StudySubjectBean> getStudySubject(StudyBean study){
        return null;
    }

}
