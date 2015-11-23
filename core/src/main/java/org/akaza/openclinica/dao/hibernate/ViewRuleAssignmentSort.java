package org.akaza.openclinica.dao.hibernate;

import org.akaza.openclinica.dao.managestudy.CriteriaCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewRuleAssignmentSort implements CriteriaCommand {
    List<Sort> sorts = new ArrayList<Sort>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();

    public ViewRuleAssignmentSort() {
        columnMapping.put("studyId", "rs.study_id");
        columnMapping.put("targetValue", "revalue");
        columnMapping.put("studyEventDefinitionName", "sedname");
        columnMapping.put("crfName", "cname");
        columnMapping.put("crfVersionName", "cvname");
        columnMapping.put("groupLabel", "igname");
        columnMapping.put("itemName", "iname");
        columnMapping.put("ruleExpressionValue", "rervalue");
        columnMapping.put("ruleOid", "rocoid");
        columnMapping.put("ruleDescription", "rdescription");
        columnMapping.put("ruleName", "rname");
        columnMapping.put("ruleSetRuleStatus", "rsr.status_id");
        // columnMapping.put("validations", "validations");
        columnMapping.put("actionExecuteOn", "ra.expression_evaluates_to");
        columnMapping.put("actionType", "ra.action_type");
        columnMapping.put("actionSummary", "ra.message");
        columnMapping.put("ruleSetRunSchedule", "rs.run_schedule");
        columnMapping.put("ruleSetRunTime", "rs.run_time");
    }

    public void addSort(String property, String order) {
        sorts.add(new Sort(property, order));
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public String execute(String criteria) {
        String theCriteria = "";
        for (Sort sort : sorts) {
            if (theCriteria.length() == 0) {
                theCriteria += buildCriteriaInitial(criteria, sort.getProperty(), sort.getOrder());
            } else {
                theCriteria += buildCriteria(criteria, sort.getProperty(), sort.getOrder());
            }

        }

        return theCriteria;
    }

    private String buildCriteriaInitial(String criteria, String property, String order) {
        if (order.equals(Sort.ASC)) {
            criteria = criteria + " order by " + columnMapping.get(property) + " asc ";
        } else if (order.equals(Sort.DESC)) {
            criteria = criteria + " order by " + columnMapping.get(property) + " desc ";
        }
        return criteria;
    }

    private String buildCriteria(String criteria, String property, String order) {
        if (order.equals(Sort.ASC)) {
            criteria = criteria + " , " + columnMapping.get(property) + " asc ";
        } else if (order.equals(Sort.DESC)) {
            criteria = criteria + " , " + columnMapping.get(property) + " desc ";
        }
        return criteria;
    }

    private static class Sort {
        public final static String ASC = "asc";
        public final static String DESC = "desc";

        private final String property;
        private final String order;

        public Sort(String property, String order) {
            this.property = property;
            this.order = order;
        }

        public String getProperty() {
            return property;
        }

        public String getOrder() {
            return order;
        }
    }
}
