package core.org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListEventsForSubjectFilter implements CriteriaCommand {

    List<Filter> filters = new ArrayList<Filter>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();
    Integer studyEventDefinitionId;

    public ListEventsForSubjectFilter(Integer studyEventDefinitionId) {
        columnMapping.put("studySubject.label", "ss.label");
        columnMapping.put("studySubject.status", "ss.status_id");
        columnMapping.put("studySubject.oid", "ss.oc_oid");
        columnMapping.put("studySubject.secondaryLabel", "ss.secondary_label");
        columnMapping.put("enrolledAt", "ST.unique_identifier");
        columnMapping.put("subject.charGender", "s.gender");
        this.studyEventDefinitionId = studyEventDefinitionId;

    }

    public void addFilter(String property, Object value) {
        filters.add(new Filter(property, value));
    }

    public String execute(String criteria) {
        String theCriteria = "";
        int formCount=0;
        for (Filter filter : filters) {
            formCount++;
            theCriteria += buildCriteria(criteria, filter.getProperty(), filter.getValue(),formCount);
        }

        return theCriteria;
    }

    private String buildCriteria(String criteria, String property, Object value,int formCount) {
        value = StringEscapeUtils.escapeSql(value.toString());
        if (value != null) {
            if (property.equals("studySubject.status")) {
                criteria = criteria + " and ";
                criteria = criteria + " " + columnMapping.get(property) + " = " + value.toString() + " ";
            } else if (property.equals("event.status")) {
                if (!value.equals("2")) {
                    criteria +=   "  JOIN study_event se  ON  se.study_subject_id=ss.study_subject_id ";
                    criteria += " and ";
                    criteria += " ( se.study_event_definition_id = " + studyEventDefinitionId;
                    String status = value.toString();
                    if (status.equalsIgnoreCase(resterm.getString(LOCKED.toLowerCase()))) {
                        criteria += " and se.locked = 'true' )";
                    } else if (status.equalsIgnoreCase(resterm.getString(NOT_LOCKED.toLowerCase()))) {
                        criteria += " and (se.locked = 'false' or se.locked isNull) )";
                    } else if (status.equalsIgnoreCase(resterm.getString(SIGNED.toLowerCase()))) {
                        criteria += " and se.signed = 'true' )";
                    } else if (status.equalsIgnoreCase(resterm.getString(NOT_SIGNED.toLowerCase()))) {
                        criteria += " and (se.signed = 'false' or se.signed isNull) )";
                    } else {
                        criteria += " and se.workflow_status = '" + value + "' )";
                    }

                } else {
                    criteria += " AND (se.study_subject_id is null or (se.study_event_definition_id != " + studyEventDefinitionId;
                    criteria += " AND (select count(*) from  study_subject ss1 LEFT JOIN study_event ON ss1.study_subject_id = study_event.study_subject_id";
                    criteria +=
                        " where  study_event.study_event_definition_id =" + studyEventDefinitionId + " and ss.study_subject_id = ss1.study_subject_id) =0))";

                }
            } else if (property.startsWith("sgc_")) {
                int study_group_class_id = Integer.parseInt(property.substring(4));

                int group_id = Integer.parseInt(value.toString());
                criteria +=
                    "AND " + group_id + " = (" + " select distinct sgm.study_group_id" + " FROM SUBJECT_GROUP_MAP sgm, STUDY_GROUP sg, STUDY_GROUP_CLASS sgc, STUDY s"
                        + " WHERE " + " sgm.study_group_class_id = " + study_group_class_id + " AND sgm.study_subject_id = SS.study_subject_id"
                        + " AND sgm.study_group_id = sg.study_group_id" + " AND (s.parent_study_id = sgc.study_id OR SS.study_id = sgc.study_id)"
                        + " AND sgm.study_group_class_id = sgc.study_group_class_id" + " ) ";

            } else if (property.startsWith("crf_")) {
                int crfId = Integer.parseInt(property.toString().substring(4));
                criteria += "  JOIN event_crf ec"+formCount+"   ON  ec"+formCount+".study_subject_id=ss.study_subject_id ";
                criteria += "  JOIN study_event se"+formCount+"  ON  se"+formCount+".study_event_id=ec"+formCount+".study_event_id ";
                criteria += " and ec"+formCount+".form_layout_id in (select form_layout_id from form_layout where crf_id=" + crfId + ") ";
                criteria += " and se"+formCount+".study_event_definition_id = " + studyEventDefinitionId;
                criteria += " and ec"+formCount+".workflow_status= '" + value + "' " ;

            } else {
                criteria = criteria + " and ";
                criteria = criteria + " UPPER(" + columnMapping.get(property) + ") like UPPER('%" + value.toString() + "%')" + " ";
            }
        }
        return criteria;
    }

    /*
     * Method to convert DataEntryStage id value into status value to be match
     * it with event_crf table rows.
     */
    private int getStatusForStage(int stage) {
        int status = 0;

        if (stage == 2) // DataEntryStage.INITIAL_DATA_ENTRY
        {
            status = 1;
        }

        if (stage == 3 || stage == 4) // DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE
        // / DataEntryStage.DOUBLE_DATA_ENTRY
        {
            status = 4;
        }

        if (stage == 5) // DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE
        {
            status = 2;
        }

        if (stage == 6) // DataEntryStage.ADMINISTRATIVE_EDITING
        {
            status = 6;
        }

        if (stage == 7) // DataEntryStage.LOCKED
        {
            status = 7;
        }
        return status;
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