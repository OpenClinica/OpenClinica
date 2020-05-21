package core.org.akaza.openclinica.web.table.sdv;

import org.jmesa.core.filter.FilterMatcher;

public class LockStatusMatcher implements FilterMatcher {
    public boolean evaluate(Object itemValue, String filterValue) {
        return true;
    }
}
