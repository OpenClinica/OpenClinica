package org.akaza.openclinica.dao.managestudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListNotesFilter implements CriteriaCommand {

    List<Filter> filters = new ArrayList<Filter>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();

    public ListNotesFilter() {
        columnMapping.put("studySubject.label", "ss.label");
        columnMapping.put("siteId", "ss.label");
        columnMapping.put("studySubject.labelExact", "ss.label");
        columnMapping.put("discrepancyNoteBean.createdDate", "dn.date_created");
        columnMapping.put("discrepancyNoteBean.updatedDate", "dn.date_created");
        columnMapping.put("discrepancyNoteBean.description", "dn.description");
        columnMapping.put("discrepancyNoteBean.user", "ua.user_name");
        columnMapping.put("discrepancyNoteBean.disType", "dn.discrepancy_note_type_id");
        columnMapping.put("discrepancyNoteBean.entityType", "dn.entity_type");
        columnMapping.put("discrepancyNoteBean.resolutionStatus", "dn.resolution_status_id");
        columnMapping.put("age", "age");
        columnMapping.put("days", "days");
    }

    public void addFilter(String property, Object value) {
        filters.add(new Filter(property, value));
    }

    public String execute(String criteria) {
        String theCriteria = "";
        for (Filter filter : filters) {
            if (columnMapping.get(filter.getProperty()) == null) {
                continue;
            }
            theCriteria += buildCriteria(criteria, filter.getProperty(), filter.getValue());
        }
        return theCriteria;
    }

    private String buildCriteria(String criteria, String property, Object value) {
        if (value != null) {
			if (property.equals("studySubject.labelExact")){
                criteria = criteria + " and ";
                criteria = criteria + " UPPER(" + columnMapping.get(property) + ") = UPPER('" + value.toString() + "')" + " ";
            }else if (property.equals("studySubject.label") || property.equals("discrepancyNoteBean.description") || property.equals("discrepancyNoteBean.user")) {
                criteria = criteria + " and ";
                criteria = criteria + " UPPER(" + columnMapping.get(property) + ") like UPPER('%" + value.toString() + "%')" + " ";
            } else if (property.equals("siteId")) {
                criteria = criteria + " and ";
                criteria = criteria + "ss.study_id in ( SELECT study_id FROM study WHERE unique_identifier like '%"+ value.toString() +"%')";
            } else if (property.equals("age")) {
                if(value.toString().startsWith(">") || value.toString().startsWith("<")
                        || value.toString().startsWith("=")){
                    criteria = criteria + " and ";
                    criteria = criteria + " age " + value.toString();
                }
            } else if (property.equals("days")) {
                if(value.toString().startsWith(">") || value.toString().startsWith("<")
                        || value.toString().startsWith("=")){
                    criteria = criteria + " and ";
                    criteria = criteria + " days " + value.toString();
                }
            } else if ("discrepancyNoteBean.disType".equalsIgnoreCase(property)) {
                if("31".equals(value.toString())) {
                    criteria = criteria + " and ";
                    criteria = criteria + " (dn.discrepancy_note_type_id = 1 or dn.discrepancy_note_type_id = 3)";
                } else {
                    criteria = criteria + " and ";
                    criteria = criteria + " " + columnMapping.get(property) + " = '" + value.toString() + "' ";
                }
            } else if ("discrepancyNoteBean.resolutionStatus".equalsIgnoreCase(property)) {
                if("21".equals(value.toString())) {
                    criteria = criteria + " and ";
                    criteria = criteria + " (dn.resolution_status_id = 1 or dn.resolution_status_id = 2)";
                } else {
                    criteria = criteria + " and ";
                    criteria = criteria + " " + columnMapping.get(property) + " = '" + value.toString() + "' ";
                }
            } else if ("discrepancyNoteBean.createdDate".equalsIgnoreCase(property) || "discrepancyNoteBean.updatedDate".equalsIgnoreCase(property) ) {                
                    criteria = criteria + " and ";
                    criteria = criteria + " " + columnMapping.get(property) + "::timestamp::date = '" + value.toString() + "' ";                
            } else {
                criteria = criteria + " and ";
                criteria = criteria + " " + columnMapping.get(property) + " = '" + value.toString() + "' ";
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
    
}
