/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.service.managestudy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.akaza.openclinica.core.util.Pair;
import org.jmesa.limit.Sort;
import org.jmesa.limit.SortSet;

/**
 * @author Doug Rodrigues (douglas.rodrigues@openclinica.com)
 *
 */
public class ViewNotesSortCriteria {

    private static final Map<String, String> SORT_BY_TABLE_COLUMN = new HashMap<String, String>();

    static {
        SORT_BY_TABLE_COLUMN.put("studySubject.label", "label");
        SORT_BY_TABLE_COLUMN.put("discrepancyNoteBean.createdDate", "date_created");
        SORT_BY_TABLE_COLUMN.put("days", "days");
        SORT_BY_TABLE_COLUMN.put("age", "age");
    }

    private final Map<String, String> sorters = new HashMap<String, String>();

    public static ViewNotesSortCriteria buildFilterCriteria(List<Pair<String,String>> sorts) {
        ViewNotesSortCriteria criteria = new ViewNotesSortCriteria();
        for (Pair<String,String> p: sorts) {
        	String
        		sortField = SORT_BY_TABLE_COLUMN.get(p.getFirst()),
        		sortOrder = p.getSecond();
        	if (sortField != null) {
        		criteria.getSorters().put(sortField, sortOrder);
        	}
        }
        return criteria;
    }
    
    public static ViewNotesSortCriteria buildFilterCriteria(SortSet sortSet) {
        ViewNotesSortCriteria criteria = new ViewNotesSortCriteria();
        for (Sort sort : sortSet.getSorts()) {
            String sortField = SORT_BY_TABLE_COLUMN.get(sort.getProperty());
            criteria.getSorters().put(sortField, sort.getOrder().name());
        }
        return criteria;
    }

    public Map<String, String> getSorters() {
        return sorters;
    }

}
