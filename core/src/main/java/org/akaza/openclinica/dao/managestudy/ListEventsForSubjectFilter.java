package org.akaza.openclinica.dao.managestudy;

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
        for (Filter filter : filters) {
            theCriteria += buildCriteria(criteria, filter.getProperty(), filter.getValue());
        }

        return theCriteria;
    }

    private String buildCriteria(String criteria, String property, Object value) {
        value = StringEscapeUtils.escapeSql(value.toString());
        if (value != null) {
            if (property.equals("studySubject.status")) {
                criteria = criteria + " and ";
                criteria = criteria + " " + columnMapping.get(property) + " = " + value.toString() + " ";
            } else if (property.equals("event.status")) {
                if (!value.equals("2")) {
                    criteria += " and ";
                    criteria += " ( se.study_event_definition_id = " + studyEventDefinitionId;
                    criteria += " and se.subject_event_status_id = " + value + " )";
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
                if (value.equals("3") || value.equals("6")) { // DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE
                    criteria += " and  se.study_EVENT_ID  in (select study_event_id from  event_crf ec,crf_version cv where " +
                            "ec.crf_version_id = cv.crf_version_id and crf_id=" + crfId +
                            " and ec.validator_id= 0 and DATE_COMPLETED is not null )" +
                            " and se.study_event_definition_id = "+studyEventDefinitionId;
                } 
                else if(value.equals("5")){
                    criteria += " and  se.study_EVENT_ID  in (select study_event_id from  event_crf ec,crf_version cv where " +
                    "ec.crf_version_id = cv.crf_version_id and crf_id=" + crfId +
                    " and ec.validator_id= 0 and date_validate_completed is not null )" +
                    " and se.study_event_definition_id = "+studyEventDefinitionId;
                }
                else if (value.equals("2")){ //DAtaEntryStage.Initial_data_entry with subject_event_status is DES and status is AVAILABLE
                    criteria += " and  se.study_EVENT_ID  in(select study_event_id from  event_crf ec,crf_version cv where " +
                    		"ec.crf_version_id = cv.crf_version_id and crf_id= "+crfId+"  and ( date_validate_completed is  null  or DATE_COMPLETED is NULL ) )"+
                    		 "and se.study_event_definition_id =" +studyEventDefinitionId +" and se.subject_event_status_id = 3 and se.status_id = 1";
 
        
                }
                else if (value.equals("4")){
                   //DAtaEntryStage.double data entry
                        criteria += " and  se.study_EVENT_ID  in(select study_event_id from  event_crf ec,crf_version cv where " +
                                "ec.crf_version_id = cv.crf_version_id and crf_id= "+crfId+"  and ( DATE_COMPLETED is not NULL and date_validate_completed is null ) )"+
                                 "and se.study_event_definition_id =" +studyEventDefinitionId +" and se.subject_event_status_id = 3";
     
                }
                else if (value.equals("7"))
                {
                    criteria += " and  se.study_EVENT_ID  in(select study_event_id from  event_crf ec,crf_version cv where " +
                    "ec.crf_version_id = cv.crf_version_id and crf_id= "+crfId+"  and ( DATE_COMPLETED is not NULL and date_validate_completed is null ) )"+
                     "and se.study_event_definition_id =" +studyEventDefinitionId +" and se.subject_event_status_id = 7";
                }
              /*  else if (!value.equals("1")) { // crf data entry stages other than
                    // DataEntryStage.UNCOMPLETED
                    int stage = getStatusForStage(Integer.parseInt(value.toString()));
                    criteria +=
                        "AND "
                            + stage
                            + " = ("
                            + "SELECT event_crf.status_id FROM event_crf event_crf, crf_version crf_version WHERE study_event_id in (SELECT se.study_event_id FROM study_event se, study_event_definition sed"
                            + " WHERE se.study_subject_id=SS.SUBJECT_ID" + " and se.study_event_definition_id = " + studyEventDefinitionId
                            + " and se.study_event_definition_id= sed.study_event_definition_id)" + " and crf_version.crf_id = " + crfId
                            + " and event_crf.crf_version_id = crf_version.crf_version_id";
                    
                    criteria += " order by event_crf_id asc" + ")";
                } */
                else {// DataEntryStage.UNCOMPLETED
                    criteria +=" AND ( ( SELECT count(*) FROM event_crf event_crf, crf_version crf_version WHERE study_event_id in  " +
                    		" (SELECT se.study_event_id FROM study_event study_event, study_event_definition sed " +
                    		"WHERE se.study_subject_id=SS.SUBJECT_ID and se.study_event_definition_id = "+studyEventDefinitionId +
                    "and se.study_event_definition_id= sed.study_event_definition_id  ) and crf_version.crf_id =" +crfId +" and  " +
                    		"        event_crf.crf_version_id = crf_version.crf_version_id ) =0 " +
                    		"and  se.study_EVENT_ID not in (select study_event_id from  event_crf ec,crf_version cv where " +
                    		"ec.crf_version_id = cv.crf_version_id and crf_id= " + crfId +
                    		" ) and se.study_event_definition_id = " +studyEventDefinitionId +
                    		")";
                   /* criteria += " AND (se.study_subject_id is null or (se.study_event_definition_id != " + studyEventDefinitionId;
                    criteria += " AND (select count(*) from  study_subject ss1 LEFT JOIN study_event ON ss1.study_subject_id = study_event.study_subject_id";
                    criteria +=
                        " where  study_event.study_event_definition_id =" + studyEventDefinitionId + " and ss.study_subject_id = ss1.study_subject_id) =0))";*/
                }

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