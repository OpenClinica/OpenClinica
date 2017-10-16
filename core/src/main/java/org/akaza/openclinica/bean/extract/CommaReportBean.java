/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
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
