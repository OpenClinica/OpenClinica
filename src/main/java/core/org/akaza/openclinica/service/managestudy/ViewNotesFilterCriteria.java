/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package core.org.akaza.openclinica.service.managestudy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.org.akaza.openclinica.dao.hibernate.CrfDao;
import core.org.akaza.openclinica.dao.hibernate.CrfVersionDao;
import core.org.akaza.openclinica.dao.hibernate.ItemDao;
import core.org.akaza.openclinica.dao.hibernate.ItemFormMetadataDao;
import core.org.akaza.openclinica.domain.datamap.*;
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
    private static final String CHECKBOX = "checkbox";
    private static final String MULTI_SELECT = "multi-select";
    private static final String RADIO = "radio";
    private static final String SINGLE_SELECT = "single-select";

    public static ItemDao itemDao;
    public static CrfDao crfDao;
    public static CrfVersionDao crfVersionDao;
    public static ItemFormMetadataDao itemFormMetadataDao;



    private static final String[] NUMERIC_FILTERS = {
            "discrepancy_note_type_id","resolution_status_id","days","age","thread_number"
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
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.detailedNotes", "detailed_notes");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.user", "user");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.discrepancyNoteTypeId", "discrepancy_note_type_id");
        FILTER_BY_TABLE_COLUMN.put("discrepancyNoteBean.threadNumber", "thread_number");
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

        for (Map.Entry<String, String> filter: filters.entrySet()) {
            String columnName = filter.getKey();
            String filterName = null;
            if (columnName.startsWith("SE_") && columnName.contains(".F_") && columnName.contains(".I_"))
                filterName = columnName;
            else
                filterName = FILTER_BY_TABLE_COLUMN.get(columnName);

            if (filterName == null) {
                throw new IllegalArgumentException("No query fragment available for column '" + columnName + "'");
            }
            String value = filter.getValue();
            if (filterName.equals("discrepancy_note_type_id")) {
                value = discrepancyNoteTypeDecoder.get(value);
            } else if (filterName.equals("resolution_status_id")) {
                value = resolutionTypeDecoder.get(value);
            } else if (filterName.equals("entity_name")) {
                // translate value need to replace space with _
                value = value.replace(" ", "_");
            }  else if (filterName.startsWith("SE_") && filterName.contains(".F_") && filterName.contains(".I_")) {
                String formOid = filterName.split("\\.")[1];
                String itemOid = filterName.split("\\.")[2];

                Item item = itemDao.findByOcOID(itemOid);
                CrfBean crf = crfDao.findByOcOID(formOid);
                List<CrfVersion> crfVersions = crfVersionDao.findAllByCrfId(crf.getCrfId());
                ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersions.get(0).getCrfVersionId());
                ResponseSet responseSet = itemFormMetadata.getResponseSet();
                String responseType = responseSet.getResponseType().getName();
                if (responseType.equals(CHECKBOX) || responseType.equals(MULTI_SELECT) || responseType.equals(RADIO) || responseType.equals(SINGLE_SELECT)) {
                    List<String> itemTexts = Arrays.asList(filter.getValue().split("\\s*,\\s*"));
                    String[] optionValues = responseSet.getOptionsValues().split("\\s*,\\s*");
                    String[] optionTexts = responseSet.getOptionsText().split("\\s*,\\s*");
                    String output = null;
                    for (int i = 0; i < optionValues.length; i++) {
                        for (String it : itemTexts) {
                            if (optionTexts[i].equalsIgnoreCase(it.trim())) {
                                if (output == null) {
                                    output = optionValues[i];
                                } else {
                                    output = output + "," + optionValues[i];
                                }
                                break;
                            }
                        }
                    }
                    value = output != null ? output : value;
                }
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
                String filterName = null;
                if (columnName.startsWith("SE_") && columnName.contains(".F_") && columnName.contains(".I_"))
                    filterName = columnName;
                else
                    filterName = FILTER_BY_TABLE_COLUMN.get(columnName);

                if (filterName == null) {
                    throw new IllegalArgumentException("No query fragment available for column '" + columnName + "'");
                }
                String value = filter.getValue();
                if (filterName.equals("discrepancy_note_type_id")) {
                    value = discrepancyNoteTypeDecoder.get(value);
                } else if (filterName.equals("resolution_status_id")) {
                    value = resolutionTypeDecoder.get(value);
                } else if (filterName.equals("entity_name")) {
                    // translate value need to replace space with _
                    value = value.replace(" ", "_");
                }             else if (filterName.startsWith("SE_") && filterName.contains(".F_") && filterName.contains(".I_")) {
                    String formOid = filterName.split("\\.")[1];
                    String itemOid = filterName.split("\\.")[2];

                    Item item = itemDao.findByOcOID(itemOid);
                    CrfBean crf = crfDao.findByOcOID(formOid);
                    List<CrfVersion> crfVersions = crfVersionDao.findAllByCrfId(crf.getCrfId());
                    ItemFormMetadata itemFormMetadata = itemFormMetadataDao.findByItemCrfVersion(item.getItemId(), crfVersions.get(0).getCrfVersionId());
                    ResponseSet responseSet = itemFormMetadata.getResponseSet();
                    String responseType = responseSet.getResponseType().getName();
                    if (responseType.equals(CHECKBOX) || responseType.equals(MULTI_SELECT) || responseType.equals(RADIO) || responseType.equals(SINGLE_SELECT)) {
                        List<String> itemTexts = Arrays.asList(filter.getValue().split("\\s*,\\s*"));
                        String[] optionValues = responseSet.getOptionsValues().split("\\s*,\\s*");
                        String[] optionTexts = responseSet.getOptionsText().split("\\s*,\\s*");
                        String output = null;
                        for (int i = 0; i < optionValues.length; i++) {
                            for (String it : itemTexts) {
                                if (optionTexts[i].equalsIgnoreCase(it.trim())) {
                                    if (output == null) {
                                        output = optionValues[i];
                                    } else {
                                        output = output + "," + optionValues[i];
                                    }
                                    break;
                                }
                            }
                        }
                        value = output != null ? output : value;
                    }
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
