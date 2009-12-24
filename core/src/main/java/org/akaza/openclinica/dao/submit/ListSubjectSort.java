package org.akaza.openclinica.dao.submit;

import org.akaza.openclinica.dao.managestudy.CriteriaCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListSubjectSort implements CriteriaCommand {
    List<Sort> sorts = new ArrayList<Sort>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();

    public ListSubjectSort() {
        columnMapping.put("subject.uniqueIdentifier", "unique_identifier");
        columnMapping.put("subject.gender", "gender");
        columnMapping.put("subject.createdDate", "date_created");
        columnMapping.put("subject.owner", "user_name");
        columnMapping.put("subject.updatedDate", "date_updated");
        columnMapping.put("subject.updater", "user_name");
        columnMapping.put("subject.status", "status_id");
        columnMapping.put("studySubjectIdAndStudy", "");
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
