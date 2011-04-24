package org.akaza.openclinica.dao.managestudy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListNotesSort implements CriteriaCommand {
    List<Sort> sorts = new ArrayList<Sort>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();

    public ListNotesSort() {
        columnMapping.put("studySubject.label", "label");
        columnMapping.put("discrepancyNoteBean.createdDate", "date_created");
        columnMapping.put("discrepancyNoteBean.updatedDate", "date_created");
        columnMapping.put("discrepancyNoteBean.user", "ua.user_name");
        columnMapping.put("discrepancyNoteBean.disType", "dn.discrepancy_note_type_id");
        columnMapping.put("discrepancyNoteBean.resolutionStatus", "dn.resolution_status_id");
        columnMapping.put("discrepancyNoteBean.entityType", "dn.entity_type");
        columnMapping.put("age", "age");
        columnMapping.put("days", "days");
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
            if (columnMapping.get(sort.getProperty()) == null || columnMapping.get(sort.getProperty()).length() == 0) {
                continue;
            }
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
