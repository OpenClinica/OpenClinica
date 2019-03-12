package org.akaza.openclinica.dao.managestudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class ListNotesFilter implements CriteriaCommand {

    List<Filter> filters = new ArrayList<Filter>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();
    
    public static final String filterDnTypeQueryAndFailedValidationCheck = "31";
    public static final String filterResStatusNewAndUpdated = "21";
    public static final String filterResStatusClosedAndClosedModified = "64";

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
        if (value == null){
            return criteria;
        }

        String inputValue = value.toString();

        // This pattern is sanitizing input that could lead to SQL injection.
        // The following patterns are removed: '  \"  "  --  /\*\*/ ;
        Pattern scriptPattern;
        scriptPattern = Pattern.compile("'|%27|\\\"|%22|--|%2d%2d|/\\*\\*/|%2f\\*\\*%2f|;|%3b", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        inputValue = scriptPattern.matcher(inputValue).replaceAll("");

        if (property.equals("studySubject.labelExact")) {
            criteria = criteria + " and ";
            criteria = criteria + " UPPER(" + columnMapping.get(property) + ") = UPPER('" + inputValue + "')" + " ";
        } else if (property.equals("studySubject.label") || property.equals("discrepancyNoteBean.description") || property.equals("discrepancyNoteBean.user")) {
            criteria = criteria + " and ";
            criteria = criteria + " UPPER(" + columnMapping.get(property) + ") like UPPER('%" + inputValue + "%')" + " ";
        } else if (property.equals("siteId")) {
            criteria = criteria + " and ";
            criteria = criteria + "ss.study_id in ( SELECT study_id FROM study WHERE unique_identifier like '%" + inputValue + "%')";
        } else if (property.equals("age")) {
            if (inputValue.startsWith(">") || inputValue.startsWith("<")
                    || inputValue.startsWith("=")) {
                criteria = criteria + " and ";
                criteria = criteria + " age " + inputValue;
            }
        } else if (property.equals("days")) {
            if (inputValue.startsWith(">") || inputValue.startsWith("<")
                    || inputValue.startsWith("=")) {
                criteria = criteria + " and ";
                criteria = criteria + " days " + inputValue;
            }
        } else if ("discrepancyNoteBean.disType".equalsIgnoreCase(property)) {
            if (filterDnTypeQueryAndFailedValidationCheck.equals(inputValue)) {
                criteria = criteria + " and ";
                criteria = criteria + " (dn.discrepancy_note_type_id = 1 or dn.discrepancy_note_type_id = 3)";
            } else {
                criteria = criteria + " and ";
                criteria = criteria + " " + columnMapping.get(property) + " = '" + inputValue + "' ";
            }
        } else if ("discrepancyNoteBean.resolutionStatus".equalsIgnoreCase(property)) {
            if (filterResStatusNewAndUpdated.equals(inputValue)) {
                criteria = criteria + " and ";
                criteria = criteria + " (dn.resolution_status_id = 1 or dn.resolution_status_id = 2)";
            } else if (filterResStatusClosedAndClosedModified.equals(inputValue)) {
                criteria = criteria + " and ";
                criteria = criteria + " (dn.resolution_status_id = 4 or dn.resolution_status_id = 6)";
            } else {
                criteria = criteria + " and ";
                criteria = criteria + " " + columnMapping.get(property) + " = '" + inputValue + "' ";
            }
        } else if ("discrepancyNoteBean.createdDate".equalsIgnoreCase(property) || "discrepancyNoteBean.updatedDate".equalsIgnoreCase(property)) {
            criteria = criteria + " and ";
            criteria = criteria + " " + columnMapping.get(property) + "::timestamp::date = '" + inputValue + "' ";
        } else {
            criteria = criteria + " and ";
            criteria = criteria + " " + columnMapping.get(property) + " = '" + inputValue + "' ";
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
