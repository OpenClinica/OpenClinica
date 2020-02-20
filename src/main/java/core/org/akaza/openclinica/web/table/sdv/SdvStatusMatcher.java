package core.org.akaza.openclinica.web.table.sdv;

import org.akaza.openclinica.domain.enumsupport.SdvStatus;
import org.jmesa.core.filter.FilterMatcher;

/**
 *  A FilterMatcher designed to filter values of source data verification in a Jmesa table cell.
 */
public class SdvStatusMatcher implements FilterMatcher {
    public boolean evaluate(Object itemValue, String filterValue) {

        String item = String.valueOf(itemValue);
        String filter = String.valueOf(filterValue);

        return (filter.equalsIgnoreCase(SdvStatus.NOT_VERIFIED.getDisplayValue() +" + " + SdvStatus.CHANGED_AFTER_VERIFIED.getDisplayValue() ) || filter.equalsIgnoreCase(SdvStatus.NOT_VERIFIED.getDisplayValue()) ||
                filter.equalsIgnoreCase(SdvStatus.CHANGED_AFTER_VERIFIED.getDisplayValue()) || (filter.equalsIgnoreCase(SdvStatus.VERIFIED.getDisplayValue()) && (item.contains("icon-icon-SDV-doubleCheck"))));
    }
}
