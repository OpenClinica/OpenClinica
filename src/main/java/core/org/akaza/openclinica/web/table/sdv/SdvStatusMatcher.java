package core.org.akaza.openclinica.web.table.sdv;

import org.jmesa.core.filter.FilterMatcher;

/**
 *  A FilterMatcher designed to filter values of source data verification in a Jmesa table cell.
 */
public class SdvStatusMatcher implements FilterMatcher {
    public boolean evaluate(Object itemValue, String filterValue) {

        String item = String.valueOf(itemValue);
        String filter = String.valueOf(filterValue);

        return (filter.equalsIgnoreCase("ready_to_verify_and_change_since_verified")) || filter.equalsIgnoreCase("ready_to_verify") ||
                filter.equalsIgnoreCase("change_since_verified") || (filter.equalsIgnoreCase("verified") && (item.contains("icon-icon-SDV-doubleCheck")));
    }
}
