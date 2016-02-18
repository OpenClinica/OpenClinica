package org.akaza.openclinica.controller.openrosa.processor;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.SubjectDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.StudySubject;
import org.akaza.openclinica.domain.datamap.Subject;
import org.akaza.openclinica.domain.user.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@Component
public class StudySubjectProcessor implements Processor, Ordered {

    @Autowired
    StudySubjectDao studySubjectDao;
    @Autowired
    UserAccountDao userAccountDao;
    @Autowired
    SubjectDao subjectDao;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    public void process(SubmissionContainer container) throws Exception {
        logger.debug("Executing study subject processor.");
        
        String studySubjectOid = container.getSubjectContext().get("studySubjectOID");
        String embeddedStudySubjectId = getEmbeddedStudySubjectOid(container);
        StudySubject embeddedStudySubject = null;
        if (embeddedStudySubjectId != null) embeddedStudySubject = studySubjectDao.findByLabelAndStudy(embeddedStudySubjectId, container.getStudy());
        if (studySubjectOid != null)  {
            StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOid);
            container.setSubject(studySubject);
            
            if (studySubject.getStatus() != Status.AVAILABLE) {
                container.getErrors().reject("value.incorrect.STATUS");
                throw new Exception("StudySubject status is not Available.");
            }
        } else if (embeddedStudySubject != null) {
            if (embeddedStudySubject.getStatus() != Status.AVAILABLE) {
                container.getErrors().reject("value.incorrect.STATUS");
                throw new Exception("Embedded StudySubject status is not Available");
            }
            container.setSubject(embeddedStudySubject);
        } else {
            UserAccount rootUser = userAccountDao.findByUserId(1);
            int nextLabel = studySubjectDao.findTheGreatestLabel() + 1;
            
            // create subject
            Subject subject = new Subject();
            subject.setUserAccount(rootUser);
            subject.setStatus(Status.AVAILABLE);
            Date currentDate = new Date();
            String uniqueIdentifier = "anonymous-" + String.valueOf(nextLabel) + "-" + Long.toString(currentDate.getTime());
            subject.setUniqueIdentifier(uniqueIdentifier);
            subject.setDobCollected(false);
            subjectDao.saveOrUpdate(subject);
            subject = subjectDao.findByUniqueIdentifier(uniqueIdentifier);

            // create study subject
            StudySubject studySubject = new StudySubject();
            studySubject.setStudy(container.getStudy());
            studySubject.setSubject(subject);
            studySubject.setStatus(Status.AVAILABLE);
            studySubject.setUserAccount(rootUser);
            studySubject.setEnrollmentDate(currentDate);
            studySubject.setDateCreated(currentDate);
            studySubject.setLabel(Integer.toString(nextLabel));
            studySubjectOid = studySubjectDao.getValidOid(studySubject,new ArrayList<String>());
            studySubject.setOcOid(studySubjectOid);
            studySubjectDao.saveOrUpdate(studySubject);
            container.setSubject(studySubjectDao.findByOcOID(studySubjectOid));
      
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
    
    private String getEmbeddedStudySubjectOid(SubmissionContainer container) throws Exception {
        String studySubjectId = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(container.getRequestBody()));
        Document doc = db.parse(is);
        Node instanceNode = doc.getElementsByTagName("instance").item(0);
        NodeList crfNodeList = instanceNode.getChildNodes();

        // Form loop
        for (int j = 0; j < crfNodeList.getLength(); j = j + 1) {
            Node crfNode = crfNodeList.item(j);
            if (!(crfNode instanceof Element)) continue;
            NodeList groupNodeList = crfNode.getChildNodes();

            // Group loop
            for (int k = 0; k < groupNodeList.getLength(); k = k + 1) {
                Node groupNode = groupNodeList.item(k);
                if (!(groupNode instanceof Element && !groupNode.getNodeName().startsWith("SECTION_"))) continue; 
                NodeList itemNodeList = groupNode.getChildNodes();

                // Item loop
                for (int m = 0; m < itemNodeList.getLength(); m = m + 1) {
                    Node itemNode = itemNodeList.item(m);
                    if (itemNode instanceof Element && itemNode.getNodeName().equals("OC.STUDY_SUBJECT_ID")) { //{
                        String nodeValue = itemNode.getTextContent();
                        if (nodeValue != null && !nodeValue.equals("")) studySubjectId = nodeValue;
                    }
                } // Item loop
            } // Group loop
        } // Form loop
        return studySubjectId;
    }


}
