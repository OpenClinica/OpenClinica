package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.dao.managestudy.CriteriaCommand;
import org.apache.commons.lang.StringEscapeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewRuleAssignmentFilter implements CriteriaCommand {

    List<Filter> filters = new ArrayList<Filter>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();

    public ViewRuleAssignmentFilter() {
        columnMapping.put("studyId", "rs.study_id");
        columnMapping.put("targetValue", "re.value");
        columnMapping.put("studyEventDefinitionName", "sed.name");
        columnMapping.put("crfName", "c.name");
        columnMapping.put("crfVersionName", "cv.name");
        columnMapping.put("groupLabel", "ig.name");
        columnMapping.put("itemName", "i.name");
        columnMapping.put("ruleExpressionValue", "rer.value");
        columnMapping.put("ruleOid", "r.oc_oid");
        columnMapping.put("ruleDescription", "r.description");
        columnMapping.put("ruleName", "r.name");
        columnMapping.put("ruleSetRuleStatus", "rsr.status_id");
        //columnMapping.put("validations", "validations");
        columnMapping.put("actionExecuteOn", "ra.expression_evaluates_to");
        columnMapping.put("actionType", "ra.action_type");
        columnMapping.put("actionSummary", "ra.message");
        columnMapping.put("ruleSetRunSchedule", "rs.run_schedule");
        columnMapping.put("ruleSetRunTime", "rs.run_time");
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void addFilter(String property, Object value) {
        filters.add(new Filter(property, value));
    }

    public String execute(String criteria) {
        String theCriteria = "";
        for (int i = 0; i < filters.size(); i++) {
            theCriteria += (i == 0 ? "" : " AND ") + buildCriteria(criteria, filters.get(i).getProperty(), filters.get(i).getValue());
        }
        return theCriteria;
    }

    /*
    public String execute(String criteria) {
        String theCriteria = "";
        Boolean ruleSetRuleStatusFilterSelected = true;
        for (int i = 0; i < filters.size(); i++) {
            if (filters.get(i).getProperty().equals("ruleSetRuleStatus")) {
                ruleSetRuleStatusFilterSelected = false;
            }
            theCriteria += (i == 0 ? "" : " AND ") + buildCriteria(criteria, filters.get(i).getProperty(), filters.get(i).getValue());
        }
        if (ruleSetRuleStatusFilterSelected) {
            addFilter("ruleSetRuleStatus", "1");
        }

        return theCriteria;
    }*/

    private String buildCriteria(String criteria, String property, Object value) {
        value = StringEscapeUtils.escapeSql(value.toString());
        if (value != null) {
            if (property.equals("studyId") || property.equals("ruleSetRunSchedule") ||  property.equals("actionType") || property.equals("actionExecuteOn") || property.equals("ruleSetRuleStatus")) {
                criteria = criteria + " " + columnMapping.get(property) + " = " + value.toString() + " ";
            }
            else {
                criteria += " UPPER(" + columnMapping.get(property) + ") like UPPER('%" + value.toString() + "%')" + " ";
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

}
