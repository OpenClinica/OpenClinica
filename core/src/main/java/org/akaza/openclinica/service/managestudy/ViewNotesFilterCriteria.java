/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.service.managestudy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jmesa.limit.Filter;
import org.jmesa.limit.FilterSet;
import org.jmesa.limit.Limit;
import org.jmesa.limit.RowSelect;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class ViewNotesFilterCriteria {

    private static final Map<String, String> FILTER_BY_TABLE_COLUMN = new HashMap<String, String>();

    private static final String[] NUMERIC_FILTERS = {
        "discrepancy_note_type_id","resolution_status_id","days","age"
    };

    private static final String[] DATE_FILTERS = {
        "date_created", "date_updated"
    };

    static {
        FILTER_BY_TABLE_COLUMN.put("studySubject.label", "label");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.disType", "discrepancy_note_type_id");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.resolutionStatus", "resolution_status_id");
        FILTER_BY_TABLE_COLUMN.put("siteId", "site_id");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.createdDate", "date_created");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.updatedDate", "date_updated");
        FILTER_BY_TABLE_COLUMN.put("days", "days");
        FILTER_BY_TABLE_COLUMN.put("age", "age");
        FILTER_BY_TABLE_COLUMN.put("eventName", "event_name");
        FILTER_BY_TABLE_COLUMN.put("crfName", "crf_name");
        FILTER_BY_TABLE_COLUMN.put("entityName", "entity_name");
        FILTER_BY_TABLE_COLUMN.put("entityValue", "value");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.entityType", "entity_type");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.description", "description");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.user", "user");
    }

    private final Map<String, Object> filters = new HashMap<String, Object>();

    private Integer pageNumber;

    private Integer pageSize;

    public static ViewNotesFilterCriteria buildFilterCriteria(
    		Map<String,String> filters,
    		String datePattern,
    		Map<String, String> discrepancyNoteTypeDecoder,
    		Map<String, String> resolutionTypeDecoder) {
    	DateFormat df = new SimpleDateFormat(datePattern);
        ViewNotesFilterCriteria criteria = new ViewNotesFilterCriteria();

        for (Map.Entry<String, String> e: filters.entrySet()) {
            String columnName = e.getKey();
            String filterName = FILTER_BY_TABLE_COLUMN.get(columnName);
            if (filterName == null) {
                throw new IllegalArgumentException("No query fragment available for column '" + columnName + "'");
            }
            String value = e.getValue();
            if (filterName.equals("discrepancy_note_type_id")) {
                value = discrepancyNoteTypeDecoder.get(value);
            } else if (filterName.equals("resolution_status_id")) {
                value = resolutionTypeDecoder.get(value);
            }

            criteria.getFilters().put(filterName, processValue(filterName, value, df));
        }
        return criteria;
    }

    public static ViewNotesFilterCriteria buildFilterCriteria(Limit limit, String datePattern,
            Map<String, String> discrepancyNoteTypeDecoder, Map<String, String> resolutionTypeDecoder) {
        ViewNotesFilterCriteria criteria = new ViewNotesFilterCriteria();

        FilterSet filterSet = limit.getFilterSet();
        if (filterSet != null) {
            DateFormat df = new SimpleDateFormat(datePattern);
            for (Filter filter : filterSet.getFilters()) {
                String columnName = filter.getProperty();
                String filterName = FILTER_BY_TABLE_COLUMN.get(columnName);
                if (filterName == null) {
                    throw new IllegalArgumentException("No query fragment available for column '" + columnName + "'");
                }
                String value = filter.getValue();
                if (filterName.equals("discrepancy_note_type_id")) {
                    value = discrepancyNoteTypeDecoder.get(value);
                } else if (filterName.equals("resolution_status_id")) {
                    value = resolutionTypeDecoder.get(value);
                }
                criteria.getFilters().put(filterName, processValue(filterName, value, df));
            }
        }

        RowSelect rowSelect = limit.getRowSelect();
        if (rowSelect != null) {
            criteria.pageNumber = rowSelect.getPage();
            criteria.pageSize = rowSelect.getMaxRows();
        }

        return criteria;
    }

    public Map<String, Object> getFilters() {
        return filters;
    }

    /**
     * Processes a filter value selected by the user, converting it to the appropriate type to be used in the SQL query.
     * @param filterName
     * @param value
     * @param df
     * @return
     */
    protected static Object processValue(String filterName, String value, DateFormat df) {
        if (Arrays.asList(NUMERIC_FILTERS).contains(filterName)) {
            // Check if the numeric value is a comma-separated list of values.
            String multipleValues[] = StringUtils.split(value, ',');
            if (multipleValues != null && multipleValues.length > 1) {
                // Parse value to a list of integers.
                List<Integer> intList = new ArrayList<Integer>(multipleValues.length);
                for (int i = 0; i < multipleValues.length; i++) {
                    intList.add(Integer.parseInt(multipleValues[i]));
                }
                return intList;
            } else {
                return Integer.parseInt(value);
            }
        } else if (Arrays.asList(DATE_FILTERS).contains(filterName)) {
            try {
                return df.parse(value);
            } catch (ParseException e) {
                return new Date(0); // Returns January 1, 1970 whenever the date cannot be parsed
            }
        }
        return "%" + StringUtils.trim(value) + "%";
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public Integer getPageSize() {
        return pageSize;
    }



}
