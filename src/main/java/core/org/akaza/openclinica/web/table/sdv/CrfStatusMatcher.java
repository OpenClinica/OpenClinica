package core.org.akaza.openclinica.web.table.sdv;

import org.jmesa.core.filter.FilterMatcher;

/**
 * The filter for matching the values of an event CRF's status (as in "completed" or "data entry started").
 */
public class CrfStatusMatcher implements FilterMatcher {
    public boolean evaluate(Object itemValue, String filterValue) {
        return true;
    }
}
