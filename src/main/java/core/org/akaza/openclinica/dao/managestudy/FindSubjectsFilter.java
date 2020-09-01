package core.org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.domain.enumsupport.StudyEventWorkflowStatusEnum;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FindSubjectsFilter implements CriteriaCommand {

    List<Filter> filters = new ArrayList<Filter>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();

    public FindSubjectsFilter() {
        columnMapping.put("studySubject.label", "ss.label");
        columnMapping.put("studySubject.status", "ss.status_id");
        columnMapping.put("studySubject.oid", "ss.oc_oid");
        columnMapping.put("enrolledAt", "ST.unique_identifier");
        columnMapping.put("studySubject.secondaryLabel", "ss.secondary_label");
        columnMapping.put("subject.charGender", "s.gender");
        columnMapping.put("participate.status", "ss.user_status_id");

    }

    public void addFilter(String property, Object value) {
        filters.add(new Filter(property, value));
    }

    public void removeFilter(Filter filter) {
        filters.remove(filter);
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
                criteria+= " INTERSECT " + mainQuery();
                criteria = criteria + " where ";
                criteria = criteria + " " + columnMapping.get(property) + " = " + value.toString() + " ";
            } else if (property.startsWith("sed_")) {

                criteria+= " INTERSECT " + mainQuery() +
                        "  JOIN study_event se  ON  se.study_subject_id=ss.study_subject_id ";

                    criteria += " and ";
                    criteria += " ( se.study_event_definition_id = " + property.substring(4);
                    String status = value.toString();
                    if (status.equalsIgnoreCase(resterm.getString(LOCKED.toLowerCase()))) {
                    criteria += " and se.locked = 'true' )";
                } else if (status.equalsIgnoreCase(resterm.getString(NOT_LOCKED.toLowerCase()))) {
                    criteria += " and (se.locked = 'false' or se.locked isNull) )";
                } else if (status.equalsIgnoreCase(resterm.getString(SIGNED.toLowerCase()))) {
                    //Signed in UI is shown as Signed but the term.properties is signed
                    criteria += " and se.signed = 'true' )";
                } else if (status.equalsIgnoreCase(resterm.getString(NOT_SIGNED.toLowerCase()))) {
                    criteria += " and (se.signed = 'false' or se.signed isNull) )";
                } else {
                    criteria += " and se.workflow_status = '" + value + "' )";
                }

            } else if (property.startsWith("sgc_")) {
                int study_group_class_id = Integer.parseInt(property.substring(4));

                int group_id = Integer.parseInt(value.toString());
                criteria +=
                        "AND " + group_id + " = (" + " select distinct sgm.study_group_id"
                                + " FROM SUBJECT_GROUP_MAP sgm, STUDY_GROUP sg, STUDY_GROUP_CLASS sgc, STUDY s" + " WHERE " + " sgm.study_group_class_id = "
                                + study_group_class_id + " AND sgm.study_subject_id = SS.study_subject_id" + " AND sgm.study_group_id = sg.study_group_id"
                                + " AND (s.parent_study_id = sgc.study_id OR SS.study_id = sgc.study_id)" + " AND sgm.study_group_class_id = sgc.study_group_class_id"
                                + " ) ";

            }else if(property.startsWith("SE_") && property.contains(".F_") && property.contains(".I_")){
                String sedOid = property.split("\\.")[0];
                String formOid = property.split("\\.")[1];
                String itemOid = property.split("\\.")[2];

                criteria+= " INTERSECT " +
                        " select ss.*  from item_data id JOIN event_crf ec ON id.event_crf_id=ec.event_crf_id\n" +
                        "                                          JOIN study_event se ON se.study_event_id=ec.study_event_id\n" +
                        "                                          JOIN crf_version cv ON cv.crf_version_id=ec.crf_version_id\n" +
                        "                                          JOIN crf c ON c.crf_id=cv.crf_id\n" +
                        "                                          JOIN item i ON i.item_id=id.item_id\n" +
                        "                                          JOIN study_event_definition sed ON sed.study_event_definition_id=se.study_event_definition_id\n" +
                        "                                          JOIN study_subject ss ON ss.study_subject_id=se.study_subject_id\n"+
                        "                                          JOIN study st ON ss.study_id=st.study_id or ss.study_id=st.parent_study_id"+
                        "                                          where\n" +
                        "                                          sed.oc_oid=\'"+sedOid+"\' AND\n" +
                        "                                          c.oc_oid=\'"+formOid+"\' AND\n" +
                        "                                          i.oc_oid=\'"+itemOid+"\' AND\n" +
                        "                                          UPPER(id.value)like  \'%"+((String) value).toUpperCase()+"%\'";



            } else if (property.equals("participate.status")) {
                criteria+= " INTERSECT " + mainQuery();
                criteria = criteria + " where ";
                criteria = criteria  + columnMapping.get(property) + " = " + value  + " ";

            }   else {
                criteria+= " INTERSECT " + mainQuery();
                criteria = criteria + " where ";
                criteria = criteria + " UPPER(" + columnMapping.get(property) + ") like ('%" + ((String) value).toUpperCase() + "%')" + " ";
            }
        }
        return criteria;
    }

    public static class Filter {
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

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    private String mainQuery(){
        return  " select ss.*  from study_subject ss JOIN study st ON st.study_id=ss.study_id ";
    }

}