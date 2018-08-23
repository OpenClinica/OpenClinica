/* OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */
package org.akaza.openclinica.bean.extract.odm;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.odmbeans.*;
import org.akaza.openclinica.bean.submit.crfdata.*;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfDao;
import org.akaza.openclinica.dao.hibernate.EventDefinitionCrfPermissionTagDao;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.domain.EventCRFStatus;
import org.akaza.openclinica.domain.Status;
import org.akaza.openclinica.domain.datamap.*;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.service.PermissionService;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create ODM XML ClinicalData Element for a study.
 * 
 * @author ywang (May, 2008)
 */

public class ClinicalDataReportBean extends OdmXmlReportBean {
    private OdmClinicalDataBean clinicalData;
    private DataSource dataSource;
    private UserAccountBean userBean;
    protected Locale locale = ResourceBundleProvider.getLocale();
    private final String COMMON = "common";
    private StudyDAO sdao;
    private String[] permissionTagsStringArray;
    private boolean crossForm;

    public ClinicalDataReportBean(OdmClinicalDataBean clinicaldata, DataSource dataSource, UserAccountBean userBean , boolean crossForm,String[] permissionTagsStringArray) {
        super();
        this.clinicalData = clinicaldata;
        this.dataSource = dataSource;
        this.userBean = userBean;
        this.permissionTagsStringArray=permissionTagsStringArray;
        this.crossForm=crossForm;

    }

    /**
     * has not been implemented yet
     */
    public void createOdmXml(boolean isDataset, boolean enketo) {
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
        Role role = null; // OpenClinica:
        StudyBean publicBean = CoreResources.getPublicStudy(clinicalData.getStudyOID(), dataSource);
        if (publicBean != null) {
            StudyUserRoleBean userRole = userBean.getRoleByStudy(publicBean.getId());
            if (userRole == null || !userRole.isActive())
                userRole = userBean.getRoleByStudy(publicBean.getParentStudyId());
            role = userRole.getRole();
        }

        if (crossForm) {
            xml.append(indent + indent + "<UserInfo OpenClinica:UserName=\"" + StringEscapeUtils.escapeXml(userBean.getName()) + "\" OpenClinica:UserRole=\"" + StringEscapeUtils.escapeXml(role.getDescription()) + "\"/>");
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

            ArrayList<ExportStudyEventDataBean> ses = (ArrayList<ExportStudyEventDataBean>) sub.getExportStudyEventData();// *****************

            EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(dataSource);
            StudyEventDefinitionDAO<String, ArrayList> seddao = new StudyEventDefinitionDAO(dataSource);
            CRFDAO crfdao = new CRFDAO(dataSource);
            StudyBean parentStudyBean = getParentStudy(clinicalData.getStudyOID());
            StudyBean studyBean = getStudy(clinicalData.getStudyOID());
            // List<EventDefinitionCRFBean> edcs = edcdao.findAllByStudy(parentStudyBean);


            List<EventDefinitionCRFBean> edcs = (List<EventDefinitionCRFBean>) edcdao.findAllStudySiteFiltered(studyBean,permissionTagsStringArray );

            // Subject
            // ***************** OpenClinica: Subject Links Start**************
            if (role != null && !role.getName().equals("invalid")) {
                xml.append(indent + indent + indent + "<OpenClinica:links>");
                xml.append(nls);

                for (EventDefinitionCRFBean edc : edcs) {
                    if (role != null && !role.equals(Role.MONITOR) && !edc.getStatus().equals(org.akaza.openclinica.bean.core.Status.AUTO_DELETED)
                            && !edc.getStatus().equals(org.akaza.openclinica.bean.core.Status.DELETED)) {
                        StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seddao.findByPK(edc.getStudyEventDefinitionId());
                        CRFBean crf = (CRFBean) crfdao.findByPK(edc.getCrfId());

                        if (studyBean.getParentStudyId() == 0 || (studyBean.getParentStudyId() != 0 && !edc.isHideCrf())) {
                            if (sed.getType().equals(COMMON) && !sub.getStatus().equals("removed")) {
                                if (sed.isRepeating() || (!sed.isRepeating() && validateAddNewForNonRepeating(sub, crf, sed))) {
                                    xml.append(indent + indent + indent + indent + "<OpenClinica:link rel=\"common-add-new\" tag=\""
                                            + StringEscapeUtils.escapeXml(sed.getOid() + "." + crf.getOid()) + "\"" + " href=\"/pages/api/addAnotherForm?studyoid="
                                            + StringEscapeUtils.escapeXml(clinicalData.getStudyOID()) + "&amp;studysubjectoid="
                                            + StringEscapeUtils.escapeXml(sub.getSubjectOID()) + "&amp;studyeventdefinitionoid="
                                            + StringEscapeUtils.escapeXml(sed.getOid()) + "&amp;crfoid=" + StringEscapeUtils.escapeXml(crf.getOid()) + "\"");
                                    xml.append("/>");
                                    xml.append(nls);
                                }
                            }
                        }
                    }
                }
                xml.append(indent + indent + indent + "</OpenClinica:links>");
                xml.append(nls);
            }
            // ***************** OpenClinica: Subject Links End **************

            //
            for (ExportStudyEventDataBean se : ses) {

                if ((!crossForm || (crossForm && !se.getStatus().equals(SubjectEventStatus.INVALID.getI18nDescription(getLocale())))) && se.getExportFormData().size()!=0) {
                    // For developers, please do not change order of properties sorted, it will break OpenRosaService
                    // Manifest Call for odm file
                    xml.append(indent + indent + indent + "<StudyEventData StudyEventOID=\"" + StringEscapeUtils.escapeXml(se.getStudyEventOID()));
                    if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                        xml.append("\" StudyEventRepeatKey=\"" + se.getStudyEventRepeatKey());
                        String eventName = se.getEventName();
                        if (eventName != null && eventName.length() > 0) {
                            xml.append("\" OpenClinica:EventName=\"" + StringEscapeUtils.escapeXml(eventName));
                        }
                        String location = se.getLocation();
                        if (location != null && location.length() > 0) {
                            xml.append("\" OpenClinica:StudyEventLocation=\"" + StringEscapeUtils.escapeXml(location));
                        }
                        String startDate = se.getStartDate();
                        if (startDate != null && startDate.length() > 0) {
                            xml.append("\" OpenClinica:StartDate=\"" + StringEscapeUtils.escapeXml(startDate));
                        }
                        String endDate = se.getEndDate();
                        if (endDate != null && endDate.length() > 0) {
                            xml.append("\" OpenClinica:EndDate=\"" + StringEscapeUtils.escapeXml(endDate));
                        }
                        String status = se.getStatus();
                        if (status != null && status.length() > 0) {
                            xml.append("\" OpenClinica:Status=\"" + StringEscapeUtils.escapeXml(status));
                        }
                        if (se.getAgeAtEvent() != null) {
                            xml.append("\" OpenClinica:SubjectAgeAtEvent=\"" + se.getAgeAtEvent());
                        }
                    }
                    xml.append("\">");
                    xml.append(nls);

                    StudySubject studySubject = sub.getStudySubject();
                    StudyEvent studyEvent = se.getStudyEvent();

                    // ***************** OpenClinica: Event Links Start **************
                    if (role != null && !role.getName().equals("invalid")) {
                        xml.append(indent + indent + indent + indent + "<OpenClinica:links>");
                        xml.append(nls);

                        if (se.getExportFormData().size() != 0) {
                            if (se.getStudyEventDefinition().getType().equals(COMMON)
                                    && se.getExportFormData().get(0).getEventDefinitionCrf().getStatusId() != Status.AUTO_DELETED.getCode()
                                    && se.getExportFormData().get(0).getEventDefinitionCrf().getStatusId() != Status.DELETED.getCode()) {

                                // ***************** OpenClinica:Link REMOVE EVENT **************
                                if (studyEvent.getStatusId() != Status.DELETED.getCode() && studyEvent.getStatusId() != Status.AUTO_DELETED.getCode()) {
                                    if (!role.equals(Role.MONITOR) && studySubject.getStatus().equals(Status.AVAILABLE)
                                            && studyBean.getStatus().equals(org.akaza.openclinica.bean.core.Status.AVAILABLE)) {
                                        String removeUrl = "/RemoveStudyEvent?action=confirm&id=" + studyEvent.getStudyEventId() + "&studySubId="
                                                + studySubject.getStudySubjectId();
                                        xml.append(indent + indent + indent + indent + indent + "<OpenClinica:link rel=\"remove\" href=\""
                                                + StringEscapeUtils.escapeXml(removeUrl) + "\"");
                                        xml.append("/>");
                                        xml.append(nls);

                                    }
                                } else {
                                    // ***************** OpenClinica:Link RESTORE EVENT **************
                                    // userRole.manageStudy &&
                                    if (!role.equals(Role.MONITOR)  && studySubject.getStatus().equals(Status.AVAILABLE)
                                            && studyBean.getStatus().equals(org.akaza.openclinica.bean.core.Status.AVAILABLE)
                                            && studyEvent.getStudyEventDefinition().getStatus().equals(Status.AVAILABLE)) {
                                        String restoreUrl = "/RestoreStudyEvent?action=confirm&id=" + studyEvent.getStudyEventId() + "&studySubId="
                                                + studySubject.getStudySubjectId();
                                        xml.append(indent + indent + indent + indent + indent + "<OpenClinica:link rel=\"restore\" href=\""
                                                + StringEscapeUtils.escapeXml(restoreUrl) + "\"");
                                        xml.append("/>");
                                        xml.append(nls);
                                    }
                                }

                                // ***************** OpenClinica:Link SIGN EVENT **************

                                if (role.equals(Role.INVESTIGATOR)
                                        && (studyEvent.getSubjectEventStatusId() == SubjectEventStatus.COMPLETED.getCode()
                                                || studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SKIPPED.getCode()
                                                || studyEvent.getSubjectEventStatusId() == SubjectEventStatus.STOPPED.getCode())
                                        && studySubject.getStatus().equals(Status.AVAILABLE)
                                        && studyBean.getStatus().equals(org.akaza.openclinica.bean.core.Status.AVAILABLE)) {
                                    String signUrl = "/UpdateStudyEvent?action=submit&event_id=" + studyEvent.getStudyEventId() + "&ss_id="
                                            + studySubject.getStudySubjectId() + "&statusId=8";

                                    xml.append(indent + indent + indent + indent + indent + "<OpenClinica:link rel=\"sign\" href=\""
                                            + StringEscapeUtils.escapeXml(signUrl) + "\"");
                                    xml.append("/>");
                                    xml.append(nls);
                                }

                                // ***************** OpenClinica:Link LOCK EVENT **************

                                if (studyEvent.getStatusId() != Status.DELETED.getCode() && studyEvent.getStatusId() != Status.AUTO_DELETED.getCode()
                                        && studySubject.getStatus().equals(Status.AVAILABLE)
                                        && studyBean.getStatus().equals(org.akaza.openclinica.bean.core.Status.AVAILABLE)) {
                                    if ((!studyEvent.getStudyEventDefinition().getType().equals(COMMON) && !role.equals(Role.MONITOR))
                                            || (studyEvent.getStudyEventDefinition().getType().equals(COMMON)
                                                    && (role.equals(Role.STUDY_STUDYDIRECTOR) || role.equals(Role.COORDINATOR)))) {
                                        String lockUrl = "/UpdateStudyEvent?event_id=" + studyEvent.getStudyEventId() + "&ss_id="
                                                + studySubject.getStudySubjectId();

                                        xml.append(indent + indent + indent + indent + indent + "<OpenClinica:link rel=\"lock\" href=\""
                                                + StringEscapeUtils.escapeXml(lockUrl) + "\"");
                                        xml.append("/>");
                                        xml.append(nls);

                                    }
                                }
                            }
                        }
                        xml.append(indent + indent + indent + indent + "</OpenClinica:links>");
                        xml.append(nls);
                    }
                    // ***************** OpenClinica: Event Links End **************

                    //
                    ArrayList<ExportFormDataBean> forms = se.getExportFormData();
                    for (ExportFormDataBean form : forms) {
                        if (!crossForm || (crossForm && !form.getStatus().equals(EventCRFStatus.INVALID.getI18nDescription(getLocale())))) {

                            xml.append(indent + indent + indent + indent + "<FormData FormOID=\"" + StringEscapeUtils.escapeXml(form.getFormOID()));
                            if ("oc1.2".equalsIgnoreCase(ODMVersion) || "oc1.3".equalsIgnoreCase(ODMVersion)) {
                                String formName = form.getFormName();
                                if (!StringUtils.isEmpty(formName)) {
                                    xml.append("\" OpenClinica:FormName=\"" + StringEscapeUtils.escapeXml(formName));
                                }
                                String formLayout = form.getFormLayoutName();
                                if (!StringUtils.isEmpty(formLayout)) {
                                    xml.append("\" OpenClinica:FormLayoutOID=\"" + StringEscapeUtils.escapeXml(formLayout));
                                }
                                String interviewerName = form.getInterviewerName();
                                if (interviewerName != null && interviewerName.length() > 0) {
                                    xml.append("\" OpenClinica:InterviewerName=\"" + StringEscapeUtils.escapeXml(interviewerName));
                                }
                                if (form.getInterviewDate() != null && form.getInterviewDate().length() > 0) {
                                    xml.append("\" OpenClinica:InterviewDate=\"" + form.getInterviewDate());
                                }
                                Date createdDate = form.getCreatedDate();
                                if (createdDate != null) {
                                    xml.append("\" OpenClinica:CreatedDate=\""
                                            + StringEscapeUtils.escapeXml(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(createdDate)));
                                }
                                String createdBy = form.getCreatedBy();
                                if (createdBy != null && createdBy.length() > 0) {
                                    xml.append("\" OpenClinica:CreatedBy=\"" + StringEscapeUtils.escapeXml(createdBy));
                                }
                                Date updatedDate = form.getUpdatedDate();
                                if (updatedDate != null) {
                                    xml.append("\" OpenClinica:UpdatedDate=\""
                                            + StringEscapeUtils.escapeXml(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(updatedDate)));
                                }
                                String updatedBy = form.getUpdatedBy();
                                if (updatedBy != null && updatedBy.length() > 0) {
                                    xml.append("\" OpenClinica:UpdatedBy=\"" + StringEscapeUtils.escapeXml(updatedBy));
                                }
                                String status = form.getStatus();
                                if (status != null && status.length() > 0) {
                                    xml.append("\" OpenClinica:Status=\"" + StringEscapeUtils.escapeXml(status));
                                }
                            }
                            xml.append("\">");
                            xml.append(nls);

                            EventCrf eventCrf = form.getEventCrf();
                            FormLayout formLayout = form.getFormLayout();
                            EventDefinitionCrf eventDefinitionCrf = form.getEventDefinitionCrf();

                            // ***************** OpenClinica: Form Links Start **************
                            if (role != null && !role.getName().equals("invalid")) {

                                xml.append(indent + indent + indent + indent + indent + "<OpenClinica:links>");
                                xml.append(nls);

                                String formUrl = "/EnketoFormServlet?formLayoutId=" + formLayout.getFormLayoutId() + "&studyEventId="
                                        + studyEvent.getStudyEventId() + "&eventCrfId=" + eventCrf.getEventCrfId() + "&originatingPage=ViewStudySubject%3Fid%3D"
                                        + studySubject.getStudySubjectId();

                                // ***************** OpenClinica:Link ENKETO VIEW MODE **************
                                // No Restrictions
                                xml.append(indent + indent + indent + indent + indent + indent + "<OpenClinica:link rel=\"view\" href=\""
                                        + StringEscapeUtils.escapeXml(formUrl + "&mode=view") + "\"");
                                xml.append("/>");
                                xml.append(nls);

                                // ***************** OpenClinica:Link ENKETO EDIT MODE **************

                                if (!(form.getEventDefinitionCrf().getStatusId() == Status.DELETED.getCode())
                                        && !(form.getEventDefinitionCrf().getStatusId() == Status.AUTO_DELETED.getCode())) {

                                    if (!formLayout.getStatus().equals(Status.LOCKED)
                                            && !role.equals(Role.MONITOR) && eventCrf.getStatusId() != Status.DELETED.getCode()
                                            && eventCrf.getStatusId() != Status.AUTO_DELETED.getCode() && eventCrf.getStatusId() != Status.LOCKED.getCode()
                                            && studyEvent.getSubjectEventStatusId() != SubjectEventStatus.LOCKED.getCode()
                                            && studyEvent.getSubjectEventStatusId() != SubjectEventStatus.SKIPPED.getCode()
                                            && studyEvent.getStatusId() != Status.DELETED.getCode() && studyEvent.getStatusId() != Status.AUTO_DELETED.getCode()
                                            && studyBean.getStatus().equals(org.akaza.openclinica.bean.core.Status.AVAILABLE)) {
                                        xml.append(indent + indent + indent + indent + indent + indent + "<OpenClinica:link rel=\"edit\" href=\""
                                                + StringEscapeUtils.escapeXml(formUrl + "&mode=edit") + "\"");
                                        xml.append("/>");
                                        xml.append(nls);
                                    }
                                    // ***************** OpenClinica:Link REMOVE EVENT CRF **************
                                    if (!studyEvent.getStudyEventDefinition().getType().equals(COMMON)) {
                                        if (eventCrf.getStatusId() != Status.DELETED.getCode() && eventCrf.getStatusId() != Status.AUTO_DELETED.getCode()) {
                                            if ((!role.equals(Role.MONITOR))
                                                    && studySubject.getStatus().equals(Status.AVAILABLE)
                                                    && studyBean.getStatus().equals(org.akaza.openclinica.bean.core.Status.AVAILABLE)) {
                                                String removeUrl = "/RemoveEventCRF?action=confirm&eventCrfId=" + eventCrf.getEventCrfId() + "&studySubId="
                                                        + studySubject.getStudySubjectId();
                                                xml.append(indent + indent + indent + indent + indent + indent + "<OpenClinica:link rel=\"remove\" href=\""
                                                        + StringEscapeUtils.escapeXml(removeUrl) + "\"");
                                                xml.append("/>");
                                                xml.append(nls);

                                            }
                                        } else {
                                            // ***************** OpenClinica:Link RESTORE EVENT CRF **************
                                            // userRole.manageStudy &&
                                            if ((!role.equals(Role.MONITOR) )
                                                    && studyEvent.getStatusId() != Status.AUTO_DELETED.getCode()
                                                    && eventCrf.getStatusId() != Status.AUTO_DELETED.getCode()
                                                    && studySubject.getStatus().equals(Status.AVAILABLE)
                                                    && studyEvent.getStatusId() == Status.AVAILABLE.getCode()
                                                    && studyBean.getStatus().equals(org.akaza.openclinica.bean.core.Status.AVAILABLE)) {
                                                String restoreUrl = "/RestoreEventCRF?action=confirm&id=" + eventCrf.getEventCrfId() + "&studySubId="
                                                        + studySubject.getStudySubjectId();
                                                xml.append(indent + indent + indent + indent + indent + indent + "<OpenClinica:link rel=\"restore\" href=\""
                                                        + StringEscapeUtils.escapeXml(restoreUrl) + "\"");
                                                xml.append("/>");
                                                xml.append(nls);
                                            }
                                        }
                                    }
                                    // ***************** OpenClinica:Link REASSIGN EVENT CRF **************

                                    // (userRole.director || userRole.coordinator) &&
                                    if ((role.equals(Role.STUDYDIRECTOR) || role.equals(Role.COORDINATOR))
                                            && studyBean.getStatus().equals(org.akaza.openclinica.bean.core.Status.AVAILABLE)
                                            && !(studyEvent.getSubjectEventStatusId() == SubjectEventStatus.LOCKED.getCode()
                                                    || studyEvent.getSubjectEventStatusId() == SubjectEventStatus.SKIPPED.getCode())) {

                                        String reassignUrl = "/pages/managestudy/chooseCRFVersion?crfId=" + formLayout.getCrf().getCrfId() + "&crfName="
                                                + formLayout.getCrf().getName() + "&formLayoutId=" + formLayout.getFormLayoutId() + "&formLayoutName="
                                                + form.getFormLayoutName() + "&studySubjectLabel=" + studySubject.getLabel() + "&studySubjectId="
                                                + studySubject.getStudySubjectId() + "&eventCRFId=" + eventCrf.getEventCrfId() + "&eventDefinitionCRFId="
                                                + eventDefinitionCrf.getEventDefinitionCrfId();

                                        xml.append(indent + indent + indent + indent + indent + indent + "<OpenClinica:link rel=\"reassign\" href=\""
                                                + StringEscapeUtils.escapeXml(reassignUrl) + "\"");
                                        xml.append("/>");
                                        xml.append(nls);

                                    }
                                }
                                xml.append(indent + indent + indent + indent + indent + "</OpenClinica:links>");
                                xml.append(nls);
                            }
                            // ***************** OpenClinica: Form Links End **************

                            //
                            ArrayList<ImportItemGroupDataBean> igs = form.getItemGroupData();
                            sortImportItemGroupDataBeanList(igs);
                            for (ImportItemGroupDataBean ig : igs) {
                                xml.append(indent + indent + indent + indent + indent + "<ItemGroupData ItemGroupOID=\""
                                        + StringEscapeUtils.escapeXml(ig.getItemGroupOID()) + "\" ");
                                if (!"-1".equals(ig.getItemGroupRepeatKey())) {
                                    xml.append("ItemGroupRepeatKey=\"" + ig.getItemGroupRepeatKey() + "\" ");
                                }
                                String itemGroupName = ig.getItemGroupName();
                                if (!StringUtils.isEmpty(itemGroupName)) {
                                    xml.append("OpenClinica:ItemGroupName=\"" + itemGroupName + "\" ");
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
                                    String itemName = item.getItemName();
                                    if (!StringUtils.isEmpty(itemName)) {
                                        xml.append(" OpenClinica:ItemName=\"" + StringEscapeUtils.escapeXml(itemName) + "\" ");
                                    }
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
                                            xml.append(indent + indent + indent + indent + indent + indent + indent
                                                    + "<MeasurementUnitRef MeasurementUnitOID=\"" + StringEscapeUtils.escapeXml(muRefOid) + "\"/>");
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
                                                this.addDiscrepancyNotes(item.getDiscrepancyNotes(),
                                                        indent + indent + indent + indent + indent + indent + indent);
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
            String details = (audit.getDetails() != null) ? audit.getDetails() : "";

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
                if (details.length() > 0) {
                    xml.append(nls);
                    xml.append(currentIndent + "                      Details=\"" + StringEscapeUtils.escapeXml(details) + "\"");
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
                xml.append("DateUpdated=\"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(d) + "\" ");
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
                        xml.append("DateCreated=\"" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(d) + "\" ");
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

    public Locale getLocale() {
        return locale;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.akaza.openclinica.bean.extract.odm.OdmXmlReportBean#createOdmXml(boolean)
     */
    @Override
    public void createOdmXml(boolean isDataset) {
        // TODO Auto-generated method stub

    }

    private StudyBean getParentStudy(String studyOid) {
        StudyBean study = getStudy(studyOid);
        if (study.getParentStudyId() == 0) {
            return study;
        } else {
            StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
            return parentStudy;
        }
    }

    private StudyBean getStudy(String oid) {
        sdao = new StudyDAO(dataSource);
        StudyBean studyBean = (StudyBean) sdao.findByOid(oid);
        return studyBean;
    }

    private boolean validateAddNewForNonRepeating(ExportSubjectDataBean sub, CRFBean crf, StudyEventDefinitionBean sed) {
        List<ExportStudyEventDataBean> studyEventDataBeans = sub.getExportStudyEventData();

        EventCRFDAO edao = new EventCRFDAO<>(dataSource);
        StudyEventDAO sedao = new StudyEventDAO(dataSource);

        for (ExportStudyEventDataBean studyEventDataBean : studyEventDataBeans) {
            if (studyEventDataBean.getStudyEventDefinition().getStudyEventDefinitionId() == sed.getId()) {
                StudyEventBean studyEventBean = (StudyEventBean) sedao.findByPK(studyEventDataBean.getStudyEvent().getStudyEventId());
                List<EventCrf> eventCrfs = (List<EventCrf>) edao.findAllByStudyEventAndCrfOrCrfVersionOid(studyEventBean, crf.getOid());
                if (eventCrfs.size() != 0) {
                    logger.error(
                            "EventCrf with StudyEventDefinition Oid {},Crf Oid {} and StudySubjectOid {} already exist in the System and it is a non repeating event.",
                            studyEventDataBean.getStudyEventDefinition().getOc_oid(), crf.getOid(), sub.getSubjectOID());
                    return false;
                }
            }
        }
        return true;
    }



}