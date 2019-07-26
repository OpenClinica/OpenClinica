package org.akaza.openclinica.dao;

import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.CriteriaCommand;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventCRFSDVFilter implements CriteriaCommand {

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
        columnMapping.put("sdvRequirementDefinition", "");
        columnMapping.put("crfStatus", "ec.status_id");

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
        value = StringEscapeUtils.escapeSql(value.toString());
        if (value != null) {
            if (property.equals("sdvStatus")) {
                String dbType = CoreResources.getDBName();
                String theTrue = dbType.equals("postgres") ? " true " : " 1 ";
                String theFalse = dbType.equals("postgres") ? " false " : " 0 ";
                if (value.equals("complete")) {
                    criteria = criteria + " and ";
                    criteria = criteria + " " + columnMapping.get(property) + " = " + theTrue;
                } else {
                    criteria = criteria + " and ";
                    criteria = criteria + " " + columnMapping.get(property) + " = " + theFalse;
                }
            } else if (property.equals("sdvRequirementDefinition")) {
                ArrayList<Integer> reqs = new ArrayList<Integer>();
                String sdvRequirement = value.toString().trim();
                if (sdvRequirement.contains("&")) {
                    for (String requirement : sdvRequirement.split("&")) {
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
            } else if (property.equals("crfStatus")) {
                if (value.equals("Completed")) {
                    criteria = criteria + " and ";
                    criteria =
                        criteria + " ( " + columnMapping.get(property)
                            + " = 2 and  se.subject_event_status_id != 5 and se.subject_event_status_id != 6 and se.subject_event_status_id != 7 ) ";
                } else {
                    criteria = criteria + " and ";
                    criteria =
                        criteria + " ( " + columnMapping.get(property)
                            + " = 6 or ( se.subject_event_status_id = 5 or se.subject_event_status_id = 6 or se.subject_event_status_id = 7 ) )";
                }
            } else {
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
