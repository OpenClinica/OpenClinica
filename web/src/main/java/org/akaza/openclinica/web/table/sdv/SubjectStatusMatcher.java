package org.akaza.openclinica.web.table.sdv;

import org.jmesa.core.filter.FilterMatcher;

/**
 * Created by IntelliJ IDEA.
 * User: bruceperry
 * Date: May 19, 2009
 */
public class SubjectStatusMatcher implements FilterMatcher {
   public boolean evaluate(Object itemValue, String filterValue) {

      String item = String.valueOf(itemValue);
      String filter = String.valueOf(filterValue);

     return filter.equalsIgnoreCase(item);
   }
}
