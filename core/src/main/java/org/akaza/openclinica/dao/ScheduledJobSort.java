package org.akaza.openclinica.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



import org.akaza.openclinica.dao.managestudy.CriteriaCommand;
/**
 * 
 * @author jnyayapathi
 *
 */
public class ScheduledJobSort implements CriteriaCommand {
    private String fireTime;
    private String datasetId;
    private String scheduledFireTime;
    HashMap<String, String> columnMapping = new HashMap<String, String>();
    List<Sort> sorts = new ArrayList<Sort>();
    
    public void addSort(String property, String order) {
        sorts.add(new Sort(property, order));
    }

    public List<Sort> getSorts() {
        return sorts;
    }
    public ScheduledJobSort(){
        fireTime="";
        datasetId="";
        scheduledFireTime="";
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
