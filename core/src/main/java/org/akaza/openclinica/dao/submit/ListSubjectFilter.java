package org.akaza.openclinica.dao.submit;

import org.akaza.openclinica.dao.managestudy.CriteriaCommand;
import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.akaza.openclinica.bean.core.Status;

public class ListSubjectFilter implements CriteriaCommand {

    List<Filter> filters = new ArrayList<Filter>();
    HashMap<String, String> columnMapping = new HashMap<String, String>();
    HashMap<String, String> monthMapping = new HashMap<String, String>();
    Integer studyEventDefinitionId;
    String defaultFormat = "yyyy-MM-dd";
    DateFormat theDefaultFormat;
    String i18Format;

    public ListSubjectFilter(String dateFormat) {

        theDefaultFormat = new SimpleDateFormat(defaultFormat);
        i18Format = dateFormat;

        columnMapping.put("subject.uniqueIdentifier", "s.unique_identifier");
        columnMapping.put("subject.gender", "s.gender");
        columnMapping.put("subject.createdDate", "s.date_created");
        columnMapping.put("subject.owner", "ua.user_name");
        columnMapping.put("subject.updatedDate", "s.date_updated");
        columnMapping.put("subject.updater", "ua.user_name");
        columnMapping.put("subject.status", "s.status_id");
        columnMapping.put("studySubjectIdAndStudy", "");

        monthMapping.put("JANUARY", "1");
        monthMapping.put("FEBRUARY", "2");
        monthMapping.put("MARCH", "3");
        monthMapping.put("APRIL", "4");
        monthMapping.put("MAY", "5");
        monthMapping.put("JUNE", "6");
        monthMapping.put("JULY", "7");
        monthMapping.put("AUGUST", "8");
        monthMapping.put("SEPTEMBER", "9");
        monthMapping.put("OCTOBER", "10");
        monthMapping.put("NOVEMBER", "11");
        monthMapping.put("DECEMBER", "12");
        monthMapping.put("JAN", "1");
        monthMapping.put("FEB", "2");
        monthMapping.put("MAR", "3");
        monthMapping.put("APR", "4");
        monthMapping.put("AUG", "8");
        monthMapping.put("SEPT", "9");
        monthMapping.put("OCT", "10");
        monthMapping.put("NOV", "11");
        monthMapping.put("DEC", "12");
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
            if (property.equals("subject.status")) {
                criteria = criteria + " and ";
                criteria = criteria + " " + columnMapping.get(property) + " = " + Status.getByName(value.toString()).getId() + " ";
            } else if (property.equals("subject.createdDate") || property.equals("subject.updatedDate")) {
                criteria += onlyYearAndMonthAndDay(String.valueOf(value), columnMapping.get(property));
                criteria += onlyYear(String.valueOf(value), columnMapping.get(property));
                criteria += onlyMonth(String.valueOf(value), columnMapping.get(property));
            } else if (property.equals("subject.owner")) {
                criteria = criteria + " and s.owner_id = ua.user_id and ";
                criteria = criteria + " UPPER(" + columnMapping.get(property) + ") like UPPER('%" + value.toString() + "%')" + " ";
            } else if (property.equals("subject.updater")) {
                criteria = criteria + " and s.update_id = ua.user_id and ";
                criteria = criteria + " UPPER(" + columnMapping.get(property) + ") like UPPER('%" + value.toString() + "%')" + " ";
            } else if (property.equals("studySubjectIdAndStudy")) {
                criteria = criteria + " and ";
                criteria = criteria + " ( UPPER(study.unique_identifier) like UPPER('%" + value.toString() + "%')" + " ";
                criteria = criteria + " or ";
                criteria = criteria + "  UPPER(ss.label) like UPPER('%" + value.toString() + "%')" + " ) ";
            } else {
                criteria = criteria + " and ";
                criteria = criteria + " UPPER(" + columnMapping.get(property) + ") like UPPER('%" + value.toString() + "%')" + " ";
            }
        }
        return criteria;
    }

    private String onlyYear(String value, String column) {
        String criteria = "";
        try {
            DateFormat format = new SimpleDateFormat("yyyy");
            Date startDate = format.parse(value);
            DateTime dt = new DateTime(startDate.getTime());
            dt = dt.plusYears(1);
            Date endDate = dt.toDate();
            if (format.format(startDate).equals(value)) {
                criteria = " AND ( " + column + " between '" + theDefaultFormat.format(startDate) + "' and '" + theDefaultFormat.format(endDate) + "')";
            }

        } catch (Exception e) {
            // Do nothing
        }
        return criteria;
    }

    private String onlyMonth(String value, String column) {
        String criteria = "";
        if (monthMapping.get(value.toUpperCase()) != null)
            criteria = " AND (   extract(month from " + column + ") = " +monthMapping.get(value.toUpperCase())+ " )";
        return criteria;
    }

    private String onlyYearAndMonthAndDay(String value, String column) {
        String criteria = "";
        try {
            DateFormat format = new SimpleDateFormat(i18Format);
            Date startDate = format.parse(value);
            DateTime dt = new DateTime(startDate.getTime());
            dt = dt.plusDays(1);
            Date endDate = dt.toDate();
            if (format.format(startDate).equals(value)) {
                criteria = " AND (  " + column + " between '" + theDefaultFormat.format(startDate) + "' and '" + theDefaultFormat.format(endDate) + "')";
            }
        } catch (Exception e) {
            // Do nothing
        }
        return criteria;
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
