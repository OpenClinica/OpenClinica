/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.table.sdv;

import org.jmesa.core.filter.FilterMatcher;

/**
 *  A FilterMatcher designed to filter values of source data verification in a Jmesa table cell.
 */
public class SdvStatusMatcher implements FilterMatcher {
    public boolean evaluate(Object itemValue, String filterValue) {

        String item = String.valueOf(itemValue);
        String filter = String.valueOf(filterValue);

        return (filter.equalsIgnoreCase("none")) || (filter.equalsIgnoreCase("complete") && (item.contains("icon_DoubleCheck")));
    }
}
