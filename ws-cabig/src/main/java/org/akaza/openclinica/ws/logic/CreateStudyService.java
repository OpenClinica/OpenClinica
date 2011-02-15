/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2010-2011 Akaza Research

 * Development of this web service or portions thereof has been funded
 * by Federal Funds from the National Cancer Institute, 
 * National Institutes of Health, under Contract No. HHSN261200800001E.
 * In addition to the GNU LGPL license, this code is also available
 * from NCI CBIIT repositories under the terms of the caBIG Software License. 
 * For details see: https://cabig.nci.nih.gov/adopt/caBIGModelLicense
 */
package org.akaza.openclinica.ws.logic;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.w3c.dom.Node;

import java.util.ArrayList;

public class CreateStudyService {

    private final String CONNECTOR_NAMESPACE_V1 = "http://clinicalconnector.nci.nih.gov";

    public CreateStudyService() {

    }

    public StudyBean generateStudyBean(UserAccountBean user, Node study) throws Exception {
        StudyBean studyBean = new StudyBean();
        studyBean.setOwner(user);
        DomParsingService xmlService = new DomParsingService();
        studyBean.setIdentifier(xmlService.getElementValue(study, CONNECTOR_NAMESPACE_V1, "identifier", "extension"));
        studyBean.setStatus(Status.AVAILABLE);// coordinatingCenterStudyStatusCode?
        studyBean.setName(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "officialTitle", "value"));
        studyBean.setPhase(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "phaseCode", "code"));
        // tbh 7619
        // studyBean.setPurpose(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "primaryPurposeCode", "code"));
        studyBean.setSummary(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "publicDescription", "value"));
        studyBean.setSecondaryIdentifier(xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, "publicTitle", "value"));
        int enrollment = xmlService.getTargetAccrualNumberRange(study);// xmlService.getElementValue(study, this.CONNECTOR_NAMESPACE_V1, xmlLine, attrName)
        studyBean.setExpectedTotalEnrollment(enrollment);
        System.out.println("found enrollment " + enrollment);
        studyBean = xmlService.getStudyInvestigator(studyBean, study);
        studyBean = xmlService.getStudyCenter(studyBean, study);
        studyBean = xmlService.getSponsorName(studyBean, study);
        // ArrayList<StudyBean> sites = xmlService.getSites(studyBean, study);
        return studyBean;
    }

    public ArrayList<StudyBean> generateSites(UserAccountBean user, StudyBean parent, Node study) throws Exception {
        DomParsingService xmlService = new DomParsingService();
        // above dry?
        ArrayList<StudyBean> sites = xmlService.getSites(parent, study);
        for (StudyBean site : sites) {
            site.setOwner(user);
            site.setStatus(Status.AVAILABLE);
        }
        return sites;
    }

}
