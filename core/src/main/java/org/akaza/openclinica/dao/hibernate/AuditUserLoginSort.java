package org.akaza.openclinica.dao.hibernate;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;

import java.util.ArrayList;
import java.util.List;

public class AuditUserLoginSort implements CriteriaCommand {
    List<Sort> sorts = new ArrayList<Sort>();

    public void addSort(String property, String order) {
        sorts.add(new Sort(property, order));
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public Criteria execute(Criteria criteria) {
        for (Sort sort : sorts) {
            buildCriteria(criteria, sort.getProperty(), sort.getOrder());
        }

        return criteria;
    }

    private void buildCriteria(Criteria criteria, String property, String order) {
        if (order.equals(Sort.ASC)) {
            criteria.addOrder(Order.asc(property));
        } else if (order.equals(Sort.DESC)) {
            criteria.addOrder(Order.desc(property));
        }
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
