/* OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 *//* OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */
package org.akaza.openclinica.bean.extract.odm;

import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportFormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportStudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectGroupDataBean;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;

/**
 * Create ODM XML ClinicalData Element for a study.
 * 
 * @author ywang (May, 2008)
 */

public class ClinicalDataReportBean extends OdmXmlReportBean {
    private OdmClinicalDataBean clinicalData;

    public ClinicalDataReportBean(OdmClinicalDataBean clinicaldata) {
        super();
        this.clinicalData = clinicaldata;
    }

    /**
     * has not been implemented yet
     */
    @Override
    public void createOdmXml(boolean isDataset) {
        // this.addHeading();
        // this.addRootStartLine();
        // addNodeClinicalData();
        // this.addRootEndLine();
    }

    public void addNodeClinicalData() {
        String ODMVersion = this.getODMVersion();
        // when collecting data, only item with value has been collected.
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        String nls = System.getProperty("line.separator");
        xml.append(indent + "<ClinicalData StudyOID=\"" + StringEscapeUtils.escapeXml(clinicalData.getStudyOID()) + "\" MetaDataVersionOID=\""
            + StringEscapeUtils.escapeXml(this.clinicalData.getMetaDataVersionOID()) + "\">");
        xml.append(nls);
        ArrayList<ExportSubjectDataBean> subs = (ArrayList<ExportSubjectDataBean>) this.clinicalData.getExportSubjectData();
        for (ExportSubjectDataBean sub : subs) {
            xml.append(indent + indent + "<SubjectData SubjectKey=\"" + StringEscapeUtils.escapeXml(sub.getSubjectOID()));
            if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                xml.append("\" OpenClinica:StudySubjectId=\"" + StringEscapeUtils.escapeXml(sub.getStudySubjectId()));
                String uniqueIdentifier = sub.getUniqueIdentifier();
                if (uniqueIdentifier != null && uniqueIdentifier.length() > 0) {
                    xml.append("\" OpenClinica:UniqueIdentifier=\"" + uniqueIdentifier);
                }
                String status = sub.getStatus();
                if (status != null && status.length() > 0) {
                    xml.append("\" OpenClinica:Status=\"" + status);
                }
                String secondaryId = sub.getSecondaryId();
                if (secondaryId != null && secondaryId.length() > 0) {
                    xml.append("\"  OpenClinica:SecondaryId=\"" + StringEscapeUtils.escapeXml(secondaryId));
                }
                Integer year = sub.getYearOfBirth();
                if (year != null) {
                    xml.append("\" OpenClinica:YearOfBirth=\"" + sub.getYearOfBirth());
                } else {
                    if (sub.getDateOfBirth() != null) {
                        xml.append("\" OpenClinica:DateOfBirth=\"" + sub.getDateOfBirth());
                    }
                }
                String gender = sub.getSubjectGender();
                if (gender != null && gender.length() > 0) {
                    xml.append("\" OpenClinica:Sex=\"" + gender);
                }
            }
            xml.append("\">");
            xml.append(nls);
            //
            ArrayList<ExportStudyEventDataBean> ses = (ArrayList<ExportStudyEventDataBean>) sub.getExportStudyEventData();
            for (ExportStudyEventDataBean se : ses) {
                xml.append(indent + indent + indent + "<StudyEventData StudyEventOID=\"" + StringEscapeUtils.escapeXml(se.getStudyEventOID()));
                if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                    String location = se.getLocation();
                    if (location != null && location.length() > 0) {
                        xml.append("\" OpenClinica:StudyEventLocation=\"" + location);
                    }
                    String startDate = se.getStartDate();
                    if (startDate != null && startDate.length() > 0) {
                        xml.append("\" OpenClinica:StartDate=\"" + startDate);
                    }
                    String endDate = se.getEndDate();
                    if (endDate != null && endDate.length() > 0) {
                        xml.append("\" OpenClinica:EndDate=\"" + endDate);
                    }
                    String status = se.getStatus();
                    if (status != null && status.length() > 0) {
                        xml.append("\" OpenClinica:Status=\"" + status);
                    }
                    if (se.getAgeAtEvent() != null) {
                        xml.append("\" OpenClinica:SubjectAgeAtEvent=\"" + se.getAgeAtEvent());
                    }
                }
                xml.append("\"");
                if (!"-1".equals(se.getStudyEventRepeatKey())) {
                    xml.append(" StudyEventRepeatKey=\"" + se.getStudyEventRepeatKey() + "\"");
                }
                xml.append(">");
                xml.append(nls);
                //
                ArrayList<ExportFormDataBean> forms = se.getExportFormData();
                for (ExportFormDataBean form : forms) {
                    xml.append(indent + indent + indent + indent + "<FormData FormOID=\"" + StringEscapeUtils.escapeXml(form.getFormOID()));
                    if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                        String crfVersion = form.getCrfVersion();
                        if (crfVersion != null && crfVersion.length() > 0) {
                            xml.append("\" OpenClinica:Version=\"" + crfVersion);
                        }
                        String interviewerName = form.getInterviewerName();
                        if (interviewerName != null && interviewerName.length() > 0) {
                            xml.append("\" OpenClinica:InterviewerName=\"" + interviewerName);
                        }
                        if (form.getInterviewDate() != null) {
                            xml.append("\" OpenClinica:InterviewDate=\"" + form.getInterviewDate());
                        }
                        String status = form.getStatus();
                        if (status != null && status.length() > 0) {
                            xml.append("\" OpenClinica:Status=\"" + status);
                        }
                    }
                    xml.append("\">");
                    xml.append(nls);
                    //
                    ArrayList<ImportItemGroupDataBean> igs = form.getItemGroupData();
                    for (ImportItemGroupDataBean ig : igs) {
                        xml.append(indent + indent + indent + indent + indent + "<ItemGroupData ItemGroupOID=\""
                            + StringEscapeUtils.escapeXml(ig.getItemGroupOID()) + "\" ");
                        if (!"-1".equals(ig.getItemGroupRepeatKey())) {
                            xml.append("ItemGroupRepeatKey=\"" + ig.getItemGroupRepeatKey() + "\" ");
                        }
                        xml.append("TransactionType=\"Insert\">");
                        xml.append(nls);
                        ArrayList<ImportItemDataBean> items = ig.getItemData();
                        for (ImportItemDataBean item : items) {
                            String muRefOid = item.getMeasurementUnitRef().getElementDefOID();
                            if (muRefOid != null && muRefOid.length() > 0) {
                                xml.append(indent + indent + indent + indent + indent + indent + "<ItemData ItemOID=\""
                                    + StringEscapeUtils.escapeXml(item.getItemOID()) + "\" Value=\"" + StringEscapeUtils.escapeXml(item.getValue()) + "\">");
                                xml.append(nls);
                                xml.append(indent + indent + indent + indent + indent + indent + indent + "<MeasurementUnitRef MeasurementUnitOID=\""
                                    + StringEscapeUtils.escapeXml(muRefOid) + "\"/>");
                                xml.append(nls);
                                xml.append(indent + indent + indent + indent + indent + indent + "</ItemData>");
                                xml.append(nls);
                            } else {
                                xml.append(indent + indent + indent + indent + indent + indent + "<ItemData ItemOID=\""
                                    + StringEscapeUtils.escapeXml(item.getItemOID()) + "\" Value=\"" + StringEscapeUtils.escapeXml(item.getValue()) + "\"/>");
                                xml.append(nls);
                            }
                        }
                        xml.append(indent + indent + indent + indent + indent + "</ItemGroupData>");
                        xml.append(nls);
                    }
                    xml.append(indent + indent + indent + indent + "</FormData>");
                    xml.append(nls);
                }
                xml.append(indent + indent + indent + "</StudyEventData>");
                xml.append(nls);
            }
            if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                ArrayList<SubjectGroupDataBean> sgddata = (ArrayList<SubjectGroupDataBean>) sub.getSubjectGroupData();
                if (sgddata.size() > 0) {
                    for (SubjectGroupDataBean sgd : sgddata) {
                        String cid =
                            sgd.getStudyGroupClassId() != null ? "OpenClinica:StudyGroupClassID=\"" + StringEscapeUtils.escapeXml(sgd.getStudyGroupClassId())
                                + "\" " : "";
                        if (cid.length() > 0) {
                            String cn =
                                sgd.getStudyGroupClassName() != null ? "OpenClinica:StudyGroupClassName=\""
                                    + StringEscapeUtils.escapeXml(sgd.getStudyGroupClassName()) + "\" " : "";
                            String gn =
                                sgd.getStudyGroupName() != null ? "OpenClinica:StudyGroupName=\"" + StringEscapeUtils.escapeXml(sgd.getStudyGroupName())
                                    + "\" " : "";
                            xml.append(indent + indent + indent + "<OpenClinica:SubjectGroupData " + cid + cn + gn);
                        }
                        xml.append(" />");
                        xml.append(nls);
                    }
                }
            }
            xml.append(indent + indent + "</SubjectData>");
            xml.append(nls);
        }
        xml.append(indent + "</ClinicalData>");
        xml.append(nls);
    }

    public void setClinicalData(OdmClinicalDataBean clinicaldata) {
        this.clinicalData = clinicaldata;
    }

    public OdmClinicalDataBean getClinicalData() {
        return this.clinicalData;
    }
}