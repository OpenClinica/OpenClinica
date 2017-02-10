/* OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */
package org.akaza.openclinica.bean.extract.odm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.akaza.openclinica.bean.odmbeans.AuditLogBean;
import org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import org.akaza.openclinica.bean.odmbeans.ChildNoteBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNotesBean;
import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportFormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportStudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectGroupDataBean;
import org.apache.commons.lang.StringEscapeUtils;

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

    public void addNodeClinicalData(boolean header, boolean footer) {
        String ODMVersion = this.getODMVersion();
        // when collecting data, only item with value has been collected.
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        String nls = System.getProperty("line.separator");
        if (header) {
            xml.append(indent + "<ClinicalData StudyOID=\"" + StringEscapeUtils.escapeXml(clinicalData.getStudyOID()) + "\" MetaDataVersionOID=\""
                    + StringEscapeUtils.escapeXml(this.clinicalData.getMetaDataVersionOID()) + "\">");
            xml.append(nls);
        }
        ArrayList<ExportSubjectDataBean> subs = (ArrayList<ExportSubjectDataBean>) this.clinicalData.getExportSubjectData();
        for (ExportSubjectDataBean sub : subs) {
            xml.append(indent + indent + "<SubjectData SubjectKey=\"" + StringEscapeUtils.escapeXml(sub.getSubjectOID()));
            if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                xml.append("\" OpenClinica:StudySubjectID=\"" + StringEscapeUtils.escapeXml(sub.getStudySubjectId()));
                String uniqueIdentifier = sub.getUniqueIdentifier();
                if (uniqueIdentifier != null && uniqueIdentifier.length() > 0) {
                    xml.append("\" OpenClinica:UniqueIdentifier=\"" + StringEscapeUtils.escapeXml(uniqueIdentifier));
                }
                String status = sub.getStatus();
                if (status != null && status.length() > 0) {
                    xml.append("\" OpenClinica:Status=\"" + StringEscapeUtils.escapeXml(status));
                }
                String secondaryId = sub.getSecondaryId();
                if (secondaryId != null && secondaryId.length() > 0) {
                    xml.append("\"  OpenClinica:SecondaryID=\"" + StringEscapeUtils.escapeXml(secondaryId));
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
                    xml.append("\" OpenClinica:Sex=\"" + StringEscapeUtils.escapeXml(gender));
                }
                String enrollmentDate = sub.getEnrollmentDate();
                if (enrollmentDate != null && enrollmentDate.length() > 0) {
                    xml.append("\" OpenClinica:EnrollmentDate=\"" + enrollmentDate);

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
                        xml.append("\" OpenClinica:StudyEventLocation=\"" + StringEscapeUtils.escapeXml(location));
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
                        xml.append("\" OpenClinica:Status=\"" + StringEscapeUtils.escapeXml(status));
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
                        String formLayout = form.getFormLayout();

                        if (formLayout != null && formLayout.length() > 0) {
                            xml.append("\" OpenClinica:FormLayoutOID=\"" + StringEscapeUtils.escapeXml(formLayout));
                        }
                        String interviewerName = form.getInterviewerName();
                        if (interviewerName != null && interviewerName.length() > 0) {
                            xml.append("\" OpenClinica:InterviewerName=\"" + StringEscapeUtils.escapeXml(interviewerName));
                        }
                        if (form.getInterviewDate() != null && form.getInterviewDate().length() > 0) {
                            xml.append("\" OpenClinica:InterviewDate=\"" + form.getInterviewDate());
                        }
                        String status = form.getStatus();
                        if (status != null && status.length() > 0) {
                            xml.append("\" OpenClinica:Status=\"" + StringEscapeUtils.escapeXml(status));
                        }
                    }
                    xml.append("\">");
                    xml.append(nls);
                    //
                    ArrayList<ImportItemGroupDataBean> igs = form.getItemGroupData();
                    sortImportItemGroupDataBeanList(igs);
                    for (ImportItemGroupDataBean ig : igs) {
                        xml.append(indent + indent + indent + indent + indent + "<ItemGroupData ItemGroupOID=\""
                                + StringEscapeUtils.escapeXml(ig.getItemGroupOID()) + "\" ");
                        if (!"-1".equals(ig.getItemGroupRepeatKey())) {
                            xml.append("ItemGroupRepeatKey=\"" + ig.getItemGroupRepeatKey() + "\" ");
                        }
                        if (ig.getItemData().get(0).isDeleted()) {
                            xml.append("OpenClinica:Removed=\"" + (ig.getItemData().get(0).isDeleted() ? "Yes" : "No") + "\" ");
                        }
                        xml.append("TransactionType=\"Insert\">");
                        xml.append(nls);
                        ArrayList<ImportItemDataBean> items = ig.getItemData();
                        sortImportItemDataBeanList(items);
                        for (ImportItemDataBean item : items) {
                            boolean printValue = true;
                            xml.append(indent + indent + indent + indent + indent + indent + "<ItemData ItemOID=\""
                                    + StringEscapeUtils.escapeXml(item.getItemOID()) + "\" ");
                            if ("Yes".equals(item.getIsNull())) {
                                xml.append("IsNull=\"Yes\"");
                                if (!item.isHasValueWithNull()) {
                                    printValue = false;
                                }
                                if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                                    xml.append(" OpenClinica:ReasonForNull=\"" + StringEscapeUtils.escapeXml(item.getReasonForNull()) + "\" ");
                                    if (!printValue) {
                                        xml.append("/>");
                                        xml.append(nls);
                                    }
                                }
                            }
                            if (printValue) {
                                Boolean hasElm = false;
                                xml.append("Value=\"" + StringEscapeUtils.escapeXml(item.getValue()) + "\"");

                                String muRefOid = item.getMeasurementUnitRef().getElementDefOID();
                                if (muRefOid != null && muRefOid.length() > 0) {
                                    if (hasElm) {
                                    } else {
                                        xml.append(">");
                                        xml.append(nls);
                                        hasElm = true;
                                    }
                                    xml.append(indent + indent + indent + indent + indent + indent + indent + "<MeasurementUnitRef MeasurementUnitOID=\""
                                            + StringEscapeUtils.escapeXml(muRefOid) + "\"/>");
                                    xml.append(nls);
                                }
                                //

                                if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                                    if (item.getAuditLogs() != null && item.getAuditLogs().getAuditLogs().size() > 0) {
                                        if (hasElm) {
                                        } else {
                                            xml.append(">");
                                            xml.append(nls);
                                            hasElm = true;
                                        }
                                        this.addAuditLogs(item.getAuditLogs(), indent + indent + indent + indent + indent + indent + indent, "item");
                                    }
                                    //
                                    if (item.getDiscrepancyNotes() != null && item.getDiscrepancyNotes().getDiscrepancyNotes().size() > 0) {
                                        if (hasElm) {
                                        } else {
                                            xml.append(">");
                                            xml.append(nls);
                                            hasElm = true;
                                        }
                                        this.addDiscrepancyNotes(item.getDiscrepancyNotes(), indent + indent + indent + indent + indent + indent + indent);
                                    }
                                }
                                if (hasElm) {
                                    xml.append(indent + indent + indent + indent + indent + indent + "</ItemData>");
                                    xml.append(nls);
                                    hasElm = false;
                                } else {
                                    xml.append("/>");
                                    xml.append(nls);
                                }
                            }
                        }
                        xml.append(indent + indent + indent + indent + indent + "</ItemGroupData>");
                        xml.append(nls);
                    }
                    //
                    if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                        if (form.getAuditLogs() != null && form.getAuditLogs().getAuditLogs().size() > 0) {
                            this.addAuditLogs(form.getAuditLogs(), indent + indent + indent + indent + indent, "form");
                        }
                        //
                        if (form.getDiscrepancyNotes() != null && form.getDiscrepancyNotes().getDiscrepancyNotes().size() > 0) {
                            this.addDiscrepancyNotes(form.getDiscrepancyNotes(), indent + indent + indent + indent + indent);
                        }
                    }
                    xml.append(indent + indent + indent + indent + "</FormData>");
                    xml.append(nls);
                }
                //
                if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                    if (se.getAuditLogs() != null && se.getAuditLogs().getAuditLogs().size() > 0) {
                        this.addAuditLogs(se.getAuditLogs(), indent + indent + indent + indent, "se");
                    }
                    //
                    if (se.getDiscrepancyNotes() != null && se.getDiscrepancyNotes().getDiscrepancyNotes().size() > 0) {
                        this.addDiscrepancyNotes(se.getDiscrepancyNotes(), indent + indent + indent + indent);
                    }
                }
                xml.append(indent + indent + indent + "</StudyEventData>");
                xml.append(nls);
            }
            if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                ArrayList<SubjectGroupDataBean> sgddata = (ArrayList<SubjectGroupDataBean>) sub.getSubjectGroupData();
                if (sgddata.size() > 0) {
                    for (SubjectGroupDataBean sgd : sgddata) {
                        String cid = sgd.getStudyGroupClassId() != null
                                ? "OpenClinica:StudyGroupClassID=\"" + StringEscapeUtils.escapeXml(sgd.getStudyGroupClassId()) + "\" " : "";
                        if (cid.length() > 0) {
                            String cn = sgd.getStudyGroupClassName() != null
                                    ? "OpenClinica:StudyGroupClassName=\"" + StringEscapeUtils.escapeXml(sgd.getStudyGroupClassName()) + "\" " : "";
                            String gn = sgd.getStudyGroupName() != null
                                    ? "OpenClinica:StudyGroupName=\"" + StringEscapeUtils.escapeXml(sgd.getStudyGroupName()) + "\" " : "";
                            xml.append(indent + indent + indent + "<OpenClinica:SubjectGroupData " + cid + cn + gn);
                        }
                        xml.append(" />");
                        xml.append(nls);
                    }
                }
                //
                if (sub.getAuditLogs() != null && sub.getAuditLogs().getAuditLogs().size() > 0) {
                    this.addAuditLogs(sub.getAuditLogs(), indent + indent + indent, "sub");
                }
                //
                if (sub.getDiscrepancyNotes() != null && sub.getDiscrepancyNotes().getDiscrepancyNotes().size() > 0) {
                    this.addDiscrepancyNotes(sub.getDiscrepancyNotes(), indent + indent + indent);
                }
            }
            xml.append(indent + indent + "</SubjectData>");
            xml.append(nls);
        }
        if (footer) {
            xml.append(indent + "</ClinicalData>");
            xml.append(nls);
        }
    }

    protected void addAuditLogs(AuditLogsBean auditLogs, String currentIndent, String entity) {
        int count = 0;
        if (auditLogs != null) {
            ArrayList<AuditLogBean> audits = auditLogs.getAuditLogs();
            if (audits != null && audits.size() > 0) {
                for (AuditLogBean audit : audits) {
                    if (entity == "item" && audit.getOldValue().equals("") && audit.getNewValue().equals("")) {
                        count++;
                    }
                }
                if (count != audits.size()) {

                    StringBuffer xml = this.getXmlOutput();
                    String indent = this.getIndent();
                    String nls = System.getProperty("line.separator");
                    xml.append(currentIndent + "<OpenClinica:AuditLogs EntityID=\"" + auditLogs.getEntityID() + "\">");
                    xml.append(nls);
                    for (AuditLogBean audit : audits) {
                        if (!(entity == "item" && audit.getOldValue().equals("") && audit.getNewValue().equals(""))) {
                            this.addOneAuditLog(audit, currentIndent + indent);
                        }
                    }
                    xml.append(currentIndent + "</OpenClinica:AuditLogs>");
                    xml.append(nls);
                }
            }
        }
    }

    protected void addOneAuditLog(AuditLogBean audit, String currentIndent) {
        if (audit != null) {
            StringBuffer xml = this.getXmlOutput();
            String indent = this.getIndent();
            String nls = System.getProperty("line.separator");
            String i = audit.getOid();
            String u = audit.getUserId();
            String userName = audit.getUserName();
            String name = audit.getName();
            Date d = audit.getDatetimeStamp();
            String t = audit.getType();
            String r = audit.getReasonForChange();
            String o = audit.getOldValue();
            String n = audit.getNewValue();
            String vt = audit.getValueType();

            Boolean p = i.length() > 0 || u.length() > 0 || d != null || t.length() > 0 || r.length() > 0 || o.length() > 0 || n.length() > 0 ? true : false;
            if (p) {
                xml.append(currentIndent + "<OpenClinica:AuditLog ");
                if (i.length() > 0) {
                    xml.append("ID=\"" + StringEscapeUtils.escapeXml(i) + "\" ");
                }
                if (u.length() > 0) {
                    xml.append("UserID=\"" + StringEscapeUtils.escapeXml(u) + "\" ");
                }
                if (userName.length() > 0) {
                    xml.append("UserName=\"" + StringEscapeUtils.escapeXml(userName) + "\" ");
                }
                if (name.length() > 0) {
                    xml.append("Name=\"" + StringEscapeUtils.escapeXml(name) + "\" ");
                }
                if (d != null) {
                    xml.append("DateTimeStamp=\"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(d) + "\" ");
                }
                if (t.length() > 0) {
                    xml.append(nls);
                    xml.append(currentIndent + "                      AuditType=\"" + t + "\" ");
                }
                if (r.length() > 0) {
                    xml.append(nls);
                    xml.append(currentIndent + "                      ReasonForChange=\"" + StringEscapeUtils.escapeXml(r) + "\" ");
                }
                if (o.length() > 0) {
                    xml.append(nls);
                    xml.append(currentIndent + "                      OldValue=\"" + StringEscapeUtils.escapeXml(o) + "\" ");
                }
                if (n.length() > 0) {
                    xml.append(nls);
                    xml.append(currentIndent + "                      NewValue=\"" + StringEscapeUtils.escapeXml(n) + "\"");
                }
                if (vt.length() > 0) {
                    xml.append(nls);
                    xml.append(currentIndent + "                      ValueType=\"" + StringEscapeUtils.escapeXml(vt) + "\"");
                }
                xml.append("/>");
                xml.append(nls);
            }
        }
    }

    protected void addDiscrepancyNotes(DiscrepancyNotesBean DNs, String currentIndent) {
        if (DNs != null) {
            ArrayList<DiscrepancyNoteBean> dns = DNs.getDiscrepancyNotes();
            if (dns != null && dns.size() > 0) {
                StringBuffer xml = this.getXmlOutput();
                String indent = this.getIndent();
                String nls = System.getProperty("line.separator");
                xml.append(currentIndent + "<OpenClinica:DiscrepancyNotes EntityID=\"" + DNs.getEntityID() + "\">");
                xml.append(nls);
                for (DiscrepancyNoteBean dn : dns) {
                    this.addOneDN(dn, currentIndent + indent);
                }
                xml.append(currentIndent + "</OpenClinica:DiscrepancyNotes>");
                xml.append(nls);
            }
        }
    }

    protected void addOneDN(DiscrepancyNoteBean dn, String currentIndent) {
        StringBuffer xml = this.getXmlOutput();
        String indent = this.getIndent();
        String nls = System.getProperty("line.separator");
        // Boolean p = s.length()>0||i.length()>0||d.toString().length()>0||n>0 ? true : false;
        xml.append(currentIndent + "<OpenClinica:DiscrepancyNote ");
        if (dn.getOid() != null) {
            String i = dn.getOid();
            if (i.length() > 0) {
                xml.append("ID=\"" + StringEscapeUtils.escapeXml(i) + "\" ");
            }
        }
        if (dn.getStatus() != null) {
            String s = dn.getStatus();
            if (s.length() > 0) {
                xml.append("Status=\"" + s + "\" ");
            }
        }
        if (dn.getNoteType() != null) {
            String s = dn.getNoteType();
            if (s.length() > 0) {
                xml.append("NoteType=\"" + s + "\" ");
            }
        }
        if (dn.getDateUpdated() != null) {
            Date d = dn.getDateUpdated();
            if (d.toString().length() > 0) {
                xml.append("DateUpdated=\"" + new SimpleDateFormat("yyyy-MM-dd").format(d) + "\" ");
            }
        }
        if (dn.getEntityName() != null) {
            String s = dn.getEntityName();
            if (s.length() > 0) {
                xml.append("EntityName=\"" + s + "\" ");
            }
        }
        int n = dn.getNumberOfChildNotes();
        if (n > 0) {
            xml.append("NumberOfChildNotes=\"" + dn.getNumberOfChildNotes() + "\"");
        }
        xml.append(">");
        xml.append(nls);
        if (dn.getChildNotes() != null && dn.getChildNotes().size() > 0) {
            for (ChildNoteBean cn : dn.getChildNotes()) {
                xml.append(currentIndent + indent + "<OpenClinica:ChildNote ");

                if (cn.getOid() != null) {
                    String s = cn.getOid();
                    if (s.length() > 0) {
                        xml.append("ID=\"" + s + "\" ");
                    }
                }
                if (cn.getStatus() != null) {
                    String s = cn.getStatus();
                    if (s.length() > 0) {
                        xml.append("Status=\"" + s + "\" ");
                    }
                }
                if (cn.getDateCreated() != null) {
                    Date d = cn.getDateCreated();
                    if (d.toString().length() > 0) {
                        xml.append("DateCreated=\"" + new SimpleDateFormat("yyyy-MM-dd").format(d) + "\" ");
                    }
                }
                if (cn.getOwnerUserName() != "") {
                    String ownerUserName = cn.getOwnerUserName();
                    if (ownerUserName.length() > 0) {
                        xml.append("UserName=\"" + ownerUserName + "\" ");
                    }

                }
                if (cn.getOwnerFirstName() != "" || cn.getOwnerLastName() != "") {
                    String ownerLastName = cn.getOwnerLastName();
                    String ownerFirstName = cn.getOwnerFirstName();
                    if (ownerLastName.length() > 0 || ownerFirstName.length() > 0) {
                        xml.append("Name=\"" + ownerFirstName + " " + ownerLastName + "\"");
                    }

                }
                xml.append(">");
                xml.append(nls);
                if (cn.getDescription() != null) {
                    String dc = cn.getDescription();
                    if (dc.length() > 0) {
                        xml.append(
                                currentIndent + indent + indent + "<OpenClinica:Description>" + StringEscapeUtils.escapeXml(dc) + "</OpenClinica:Description>");
                        xml.append(nls);
                    }
                }
                if (cn.getDetailedNote() != null) {
                    String nt = cn.getDetailedNote();
                    if (nt.length() > 0) {
                        xml.append(currentIndent + indent + indent + "<OpenClinica:DetailedNote>" + StringEscapeUtils.escapeXml(nt)
                                + "</OpenClinica:DetailedNote>");
                        xml.append(nls);
                    }
                }

                if (cn.getUserRef() != null) {
                    String uid = cn.getUserRef().getElementDefOID();
                    String userName = cn.getUserRef().getUserName();
                    String fullName = cn.getUserRef().getFullName();
                    String temp = "";
                    if (userName.length() > 0) {
                        temp += " OpenClinica:UserName=\"" + StringEscapeUtils.escapeXml(userName) + "\"";
                    }
                    if (fullName.length() > 0) {
                        temp += " OpenClinica:FullName=\"" + StringEscapeUtils.escapeXml(fullName) + "\"";
                    }
                    if (uid.length() > 0) {
                        xml.append(currentIndent + indent + indent + "<UserRef UserOID=\"" + StringEscapeUtils.escapeXml(uid) + " \"" + temp + "/>");
                        xml.append(nls);
                    }
                }
                xml.append(currentIndent + indent + "</OpenClinica:ChildNote>");
                xml.append(nls);
            }
        }
        xml.append(currentIndent + "</OpenClinica:DiscrepancyNote>");
        xml.append(nls);
    }

    public void setClinicalData(OdmClinicalDataBean clinicaldata) {
        this.clinicalData = clinicaldata;
    }

    public OdmClinicalDataBean getClinicalData() {
        return this.clinicalData;
    }

    @SuppressWarnings("unchecked")
    private void sortImportItemGroupDataBeanList(ArrayList<ImportItemGroupDataBean> igs) {

        Collections.sort(igs, new Comparator() {

            public int compare(Object o1, Object o2) {

                String x1 = ((ImportItemGroupDataBean) o1).getItemGroupOID();
                String x2 = ((ImportItemGroupDataBean) o2).getItemGroupOID();
                int sComp = x1.compareTo(x2);

                if (sComp != 0) {
                    return sComp;
                } else {
                    Integer i1 = Integer.valueOf(((ImportItemGroupDataBean) o1).getItemGroupRepeatKey());
                    Integer i2 = Integer.valueOf(((ImportItemGroupDataBean) o2).getItemGroupRepeatKey());
                    return i1.compareTo(i2);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void sortImportItemDataBeanList(ArrayList<ImportItemDataBean> items) {

        Collections.sort(items, new Comparator() {

            public int compare(Object o1, Object o2) {

                String i1 = ((ImportItemDataBean) o1).getItemOID();
                String i2 = ((ImportItemDataBean) o2).getItemOID();
                // Integer i1 = ((ImportItemDataBean) o1).getItemId();
                // Integer i2 = ((ImportItemDataBean) o2).getItemId();
                return i1.compareTo(i2);
            }
        });
    }

}