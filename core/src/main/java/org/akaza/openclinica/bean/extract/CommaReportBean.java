/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.bean.extract;

/**
 * @author jxu
 *
 */
public class CommaReportBean extends TextReportBean {
    public CommaReportBean() {
        end = "\n";// ending character
        sep = ",";// seperating character
    }
}
