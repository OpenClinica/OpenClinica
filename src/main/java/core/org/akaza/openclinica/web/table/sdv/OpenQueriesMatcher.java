package core.org.akaza.openclinica.web.table.sdv;

import org.jmesa.core.filter.FilterMatcher;

public class OpenQueriesMatcher implements FilterMatcher {

    public boolean evaluate(Object itemValue, String filterValue) {
        String filter = String.valueOf(filterValue);
        return filter.equalsIgnoreCase("Yes") || filter.equalsIgnoreCase("No");
    }
}
