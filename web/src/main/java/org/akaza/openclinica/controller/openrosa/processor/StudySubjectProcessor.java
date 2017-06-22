package org.akaza.openclinica.controller.openrosa.processor;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.controller.openrosa.SubmissionContainer;
import org.akaza.openclinica.dao.hibernate.StudyDao;
import org.akaza.openclinica.dao.hibernate.StudySubjectDao;
import org.akaza.openclinica.dao.hibernate.SubjectDao;
import org.akaza.openclinica.dao.hibernate.UserAccountDao;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.Study;
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
    @Autowired
    StudyDao studyDao;
    
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    
    @Override
    public void process(SubmissionContainer container) throws Exception {
        logger.info("Executing study subject processor.");

        String studySubjectOid = container.getSubjectContext().get("studySubjectOID");
        String embeddedStudySubjectId = getEmbeddedStudySubjectOid(container);
        Date currentDate = new Date();
        UserAccount rootUser = userAccountDao.findByUserId(1);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd.HHmmss");

        // Standard Participant Dashboard submission
        if (studySubjectOid != null)  {
            StudySubject studySubject = studySubjectDao.findByOcOID(studySubjectOid);
            container.setSubject(studySubject);
            
            if (studySubject.getStatus() != Status.AVAILABLE) {
                container.getErrors().reject("value.incorrect.STATUS");
                throw new Exception("StudySubject status is not Available.");
            }
        // Embedded Study Subject ID in form.  An offline submission.
        } else if (embeddedStudySubjectId != null) {
            Study study = studyDao.findByOcOID(container.getSubjectContext().get("studyOID"));
            StudySubject embeddedStudySubject = studySubjectDao.findByLabelAndStudyOrParentStudy(embeddedStudySubjectId, study);

            //If Study Subject exists in current study and is available use that
            if (embeddedStudySubject != null && embeddedStudySubject.getStatus() == Status.AVAILABLE) {
                container.setSubject(embeddedStudySubject);
            //If it exists but is in the wrong status, throw an exception
            } else if (embeddedStudySubject != null && embeddedStudySubject.getStatus() != Status.AVAILABLE) {
                container.getErrors().reject("value.incorrect.STATUS");
                throw new Exception("Embedded StudySubject status is not Available");
            //If Study Subject exists in a parent/sibling study, create study subject with 'FIXME-<timestamp>' label to avoid data loss and mark it
            } else if (subjectExistsInParentSiblingStudy(embeddedStudySubjectId, study)) {
                String subjectLabel = "FIXME-" + dateFormatter.format(currentDate);
                Subject subject = createSubject(currentDate, rootUser);
                StudySubject studySubject = createStudySubject(subjectLabel, subject, study,rootUser,currentDate,embeddedStudySubjectId);
                container.setSubject(studySubject);
            //Study Subject does not exist. Create it
            } else {
                Subject subject = createSubject(currentDate, rootUser);
                StudySubject studySubject = createStudySubject(embeddedStudySubjectId, subject, study,rootUser,currentDate, null);
                container.setSubject(studySubject);
            }
        // Anonymous submission or offline submission with no embedded Study Subject ID
        } else {
            // create Subject & Study Subject
            Study study = studyDao.findByOcOID(container.getSubjectContext().get("studyOID"));
            int nextLabel = studySubjectDao.findTheGreatestLabelByStudy(study.getStudyId()) + 1;
            Subject subject = createSubject(currentDate, rootUser);
            StudySubject studySubject = createStudySubject(Integer.toString(nextLabel), subject, study,rootUser,currentDate, null);
            container.setSubject(studySubject);
        }
    }

    private boolean subjectExistsInParentSiblingStudy(String embeddedStudySubjectId, Study study) {
        boolean subjectExists = false;
        
        // Check parent studies
        if (study.getStudy() != null && studySubjectDao.findByLabelAndStudy(embeddedStudySubjectId, study.getStudy()) != null) subjectExists = true;
        // Check sibling studies
        if (study.getStudy() != null) {
            List<StudySubject> siblingSubjects = studySubjectDao.findByLabelAndParentStudy(embeddedStudySubjectId, study.getStudy());
            for (StudySubject subject:siblingSubjects) {
                if (subject.getStudy().getStudyId() != study.getStudyId()) subjectExists = true;
            }
        }
        return subjectExists;
    }

    @Override
    public int getOrder() {
        return 1;
    }
    
    private Subject createSubject(Date currentDate, UserAccount rootUser) {
        Subject subject = new Subject();
        subject.setUserAccount(rootUser);
        subject.setStatus(Status.AVAILABLE);
        subject.setDobCollected(false);
        subject.setDateCreated(currentDate);
        subject.setUniqueIdentifier("");
        subject = subjectDao.saveOrUpdate(subject);
        return subject;
    }
    
    private StudySubject createStudySubject(String label, Subject subject, Study study, UserAccount rootUser, Date currentDate, String secondaryLabel) {
        StudySubject studySubject = new StudySubject();
        studySubject.setStudy(study);
        studySubject.setSubject(subject);
        studySubject.setStatus(Status.AVAILABLE);
        studySubject.setUserAccount(rootUser);
        studySubject.setEnrollmentDate(currentDate);
        studySubject.setDateCreated(currentDate);
        studySubject.setSecondaryLabel("");
        studySubject.setLabel(label);
        if (secondaryLabel != null && !secondaryLabel.equals("")) studySubject.setSecondaryLabel(secondaryLabel);
        String studySubjectOid = studySubjectDao.getValidOid(studySubject,new ArrayList<String>());
        studySubject.setOcOid(studySubjectOid);
        studySubject = studySubjectDao.saveOrUpdate(studySubject);
        return studySubject;
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
