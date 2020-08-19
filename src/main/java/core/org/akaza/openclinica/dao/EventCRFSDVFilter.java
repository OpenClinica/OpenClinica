package core.org.akaza.openclinica.dao;

import core.org.akaza.openclinica.dao.core.CoreResources;
import core.org.akaza.openclinica.dao.managestudy.CriteriaCommand;
import core.org.akaza.openclinica.domain.SourceDataVerification;
import core.org.akaza.openclinica.domain.datamap.SubjectEventStatus;
import core.org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import core.org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.akaza.openclinica.domain.enumsupport.EventCrfWorkflowStatusEnum;
import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jmesa.view.html.editor.DroplistFilterEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class EventCRFSDVFilter implements CriteriaCommand {
    private static final Logger logger = LoggerFactory.getLogger(EventCRFSDVFilter.class);
    List<Filter> filters = new ArrayList<Filter>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();
    Integer studyId;
    static String NON_SDVD_STUDY_SUBJECTS =
        " AND ( 0 = (select count(ec.event_crf_id) from event_crf ec, study_event se, study_subject ss,crf_version cv,study s where ec.study_event_id = se.study_event_id AND ss.study_subject_id = se.study_subject_id AND ec.crf_version_id = cv.crf_version_id AND ss.study_id = s.study_id AND se.subject_event_status_id = 4 AND ss.study_subject_id = mss.study_subject_id ) OR 0 < (select count(ec.event_crf_id) from event_crf ec, study_event se, study_subject ss,crf_version cv,study s where ec.study_event_id = se.study_event_id AND ss.study_subject_id = se.study_subject_id AND ec.crf_version_id = cv.crf_version_id AND ss.study_id = s.study_id AND se.subject_event_status_id = 4 AND ec.sdv_status = false AND ss.study_subject_id = mss.study_subject_id AND (  ((1 = ( select edc.source_data_verification_code from event_definition_crf edc where study_id = s.parent_study_id and crf_id = cv.crf_id and study_event_definition_id = se.study_event_definition_id ) OR 2 = ( select edc.source_data_verification_code from event_definition_crf edc where study_id = s.parent_study_id and crf_id = cv.crf_id and study_event_definition_id = se.study_event_definition_id )) AND 0 = ( select count(edc.source_data_verification_code) from event_definition_crf edc where study_id = s.study_id and crf_id = cv.crf_id and study_event_definition_id = se.study_event_definition_id )) OR ( 1 = ( select edc.source_data_verification_code from event_definition_crf edc where study_id = s.study_id and crf_id = cv.crf_id and study_event_definition_id = se.study_event_definition_id ) or 2 = ( select edc.source_data_verification_code from event_definition_crf edc where study_id = s.study_id and crf_id = cv.crf_id and study_event_definition_id = se.study_event_definition_id ) )))) ";
    static String SDVD_STUDY_SUBJECTS =
        " AND ( 0 < (select count(ec.event_crf_id) from event_crf ec, study_event se, study_subject ss,crf_version cv,study s where ec.study_event_id = se.study_event_id AND ss.study_subject_id = se.study_subject_id AND ec.crf_version_id = cv.crf_version_id AND ss.study_id = s.study_id AND se.subject_event_status_id = 4 AND ss.study_subject_id = mss.study_subject_id ) AND 0 = (select count(ec.event_crf_id) from event_crf ec, study_event se, study_subject ss,crf_version cv,study s where ec.study_event_id = se.study_event_id AND ss.study_subject_id = se.study_subject_id AND ec.crf_version_id = cv.crf_version_id AND ss.study_id = s.study_id AND se.subject_event_status_id = 4 AND ec.sdv_status = false AND ss.study_subject_id = mss.study_subject_id AND (  ((1 = ( select edc.source_data_verification_code from event_definition_crf edc where study_id = s.parent_study_id and crf_id = cv.crf_id and study_event_definition_id = se.study_event_definition_id ) OR 2 = ( select edc.source_data_verification_code from event_definition_crf edc where study_id = s.parent_study_id and crf_id = cv.crf_id and study_event_definition_id = se.study_event_definition_id )) AND 0 = ( select count(edc.source_data_verification_code) from event_definition_crf edc where study_id = s.study_id and crf_id = cv.crf_id and study_event_definition_id = se.study_event_definition_id )) OR ( 1 = ( select edc.source_data_verification_code from event_definition_crf edc where study_id = s.study_id and crf_id = cv.crf_id and study_event_definition_id = se.study_event_definition_id ) or 2 = ( select edc.source_data_verification_code from event_definition_crf edc where study_id = s.study_id and crf_id = cv.crf_id and study_event_definition_id = se.study_event_definition_id ) )))) ";

    public EventCRFSDVFilter(Integer studyId) {
        this.studyId = studyId;
        columnMapping.put("sdvStatus", "ec.sdv_status");
        columnMapping.put("studySubjectId", "ss.label");
        columnMapping.put("studyIdentifier", "s.unique_identifier");
        columnMapping.put("eventName", "sed.name");
        columnMapping.put("crfName", "crf.name");
        columnMapping.put("sdvRequirementDefinition", "");
        columnMapping.put("crfStatus", "ec.workflow_status");
        columnMapping.put("subjectEventStatus","se.workflow_status");
        columnMapping.put("lockStatus", "se.locked");
    }

    public void addFilter(String property, Object value) {
        filters.add(new Filter(property, value));
    }

    public String execute(String criteria) {
        String theCriteria = "";
        for (Filter filter : filters) {
            theCriteria += buildCriteria(criteria, filter.getProperty(), filter.getValue());
        }
        return theCriteria;
    }

    private String buildCriteria(String criteria, String property, Object value) {
        if(value != null && StringUtils.isNotBlank(value.toString())){
            value = StringEscapeUtils.escapeSql(value.toString());
            if (property.equals("sdvStatus")) {
                String valueString = value.toString().trim();
                try {
                    valueString = URLDecoder.decode(valueString, StandardCharsets.UTF_8.toString());
                } catch (UnsupportedEncodingException e) {
                    logger.error("Unsupported encoding");
                }

                ArrayList<String> sdvStatusFilterArray = new ArrayList<>();
                for(SdvStatus sdvStatus: SdvStatus.values()){
                    if(valueString.contains(sdvStatus.getDisplayValue()))
                        sdvStatusFilterArray.add(sdvStatus.toString());
                }

                criteria = criteria + " and (";

                for(int i = 0 ; i < sdvStatusFilterArray.size(); i++){
                    if(i == 0)
                        criteria = criteria + " " + columnMapping.get(property) + " = '" + sdvStatusFilterArray.get(i) + "'";
                    else
                        criteria = criteria + " or " + columnMapping.get(property) + " = '" + sdvStatusFilterArray.get(i) + "'";
                    if(sdvStatusFilterArray.get(i).equals(SdvStatus.NOT_VERIFIED.toString()))
                        criteria = criteria + " or " + columnMapping.get(property) +" is null ";
                }
                criteria = criteria +" ) ";
            } else if (property.equals("sdvRequirementDefinition")) {
                ArrayList<Integer> reqs = new ArrayList<Integer>();
                String sdvRequirement = value.toString().trim();
                if (sdvRequirement.contains("+")) {
                    for (String requirement : sdvRequirement.split("\\+")) {
                        reqs.add(SourceDataVerification.getByI18nDescription(requirement.trim()).getCode());
                    }
                } else {
                    reqs.add(SourceDataVerification.getByI18nDescription(sdvRequirement.trim()).getCode());
                }
                if (reqs.size() > 0) {
                    criteria = criteria + " and ";
                    criteria =
                        criteria
                            + " ec.crf_version_id in (select distinct crf_version_id from crf_version crfv, crf cr, event_definition_crf edc where crfv.crf_id = cr.crf_id AND cr.crf_id = edc.crf_id AND edc.crf_id in (select crf_id from event_definition_crf where (study_id  = "
                            + studyId + " or study_id in (select parent_study_id from study where study_id = " + studyId + ")) ";
                    criteria += " AND ( ";
                    for (int i = 0; i < reqs.size(); i++) {
                        criteria += i != 0 ? " OR " : "";
                        criteria += " source_data_verification_code = " + reqs.get(i);
                    }
                    criteria += " ) )) ";
                }
            } else if(property.equals("crfStatus")){
                criteria = criteria + " and ";
                String[] formAttributes = (value.toString().split("and"));

                for(int i = 0; i < formAttributes.length; i++) {
                    String attributeValue = formAttributes[i].trim();
                    if(i > 0)
                        criteria = criteria + " and ";

                    if (attributeValue.equalsIgnoreCase("locked")) {
                        criteria += " ( se.locked  = true ) ";
                    } else if(attributeValue.equalsIgnoreCase("signed")){
                        criteria += " ( se.signed  = true ) ";
                    }
                    else if(attributeValue.equalsIgnoreCase("not locked")){
                        criteria += " ( se.locked is null  or se.locked = false ) ";
                    }
                    else if(attributeValue.equalsIgnoreCase("not signed")){
                        criteria += " ( se.signed is null  or se.signed = false ) ";
                    }
                }
            } else if (property.equals("subjectEventStatus")){
                if (value.equals(resterm.getString(LOCKED.toLowerCase()))) {
                    criteria += " and se.locked = 'true' ";
                } else if (value.equals(resterm.getString(NOT_LOCKED.toLowerCase()))) {
                    criteria += " and (se.locked = 'false' or se.locked isNull) ";
                } else if (value.equals(resterm.getString(SIGNED.toLowerCase()))) {
                    criteria += " and se.signed = 'true' ";
                } else if (value.equals(resterm.getString(NOT_SIGNED.toLowerCase()))) {
                    criteria += " and (se.signed = 'false' or se.signed isNull) ";
                } else {
                    criteria += " and se.workflow_status = '" + StudyEventWorkflowStatusEnum.getByI18nDescription(value.toString().trim()) + "'  ";
                }

            }else if(property.equals("openQueries")){
                String openQueriesQuery ="(select count(*) from discrepancy_note dn " +
                        " join dn_item_data_map didm on didm.discrepancy_note_id = dn.discrepancy_note_id " +
                        " join discrepancy_note_type dnt on dn.discrepancy_note_type_id = dnt.discrepancy_note_type_id " +
                        " join item_data id on didm.item_data_id = id.item_data_id " +
                        " where dn.parent_dn_id is null and dn.discrepancy_note_type_id= 3 " +
                        " and (dn.resolution_status_id = 1 or dn.resolution_status_id = 2 ) " +
                        " and id.event_crf_id = ec.event_crf_id ) ";
                if(value.toString().equals("Yes"))
                    criteria = criteria +" and "+openQueriesQuery +" > 0 ";
                else
                    criteria = criteria +" and "+openQueriesQuery +" = 0 ";
            }
            else {
                criteria = criteria + " and ";
                criteria = criteria + " UPPER(" + columnMapping.get(property) + ") like UPPER('%" + value.toString() + "%')" + " ";
            }
        }
        return criteria;
    }

    private static class Filter {
        private final String property;
        private final Object value;

        public Filter(String property, Object value) {
            this.property = property;
            this.value = value;
        }

        public String getProperty() {
            return property;
        }

        public Object getValue() {
            return value;
        }
    }

}
