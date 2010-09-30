/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.extract;

import org.akaza.openclinica.bean.core.Term;

/**
 * @author thickerson
 *
 */

public class ExportFormatBean extends Term {

    public static final ExportFormatBean TXTFILE = new ExportFormatBean(1, "text/plain");
    public static final ExportFormatBean CSVFILE = new ExportFormatBean(2, "text/plain");
    public static final ExportFormatBean EXCELFILE = new ExportFormatBean(3, "application/vnd.ms-excel");
    // To allow this type, another data type (an addition row) is added
    // as "text/plain" in the table export_format
    public static final ExportFormatBean XMLFILE = new ExportFormatBean(4, "text/plain");
    public static final ExportFormatBean PDFFILE = new ExportFormatBean(5, "application/pdf");
    // may have to add a #6 to export formats, tbh
    private int exportFormatId;
    private String mimeType;

    private ExportFormatBean(int efid, String mime) {
        super(efid, mime);
        this.setMimeType(mime);
    }

    /**
     * @return Returns the exportFormatId.
     */
    public int getExportFormatId() {
        return exportFormatId;
    }

    /**
     * @param exportFormatId
     *            The exportFormatId to set.
     */
    public void setExportFormatId(int exportFormatId) {
        this.exportFormatId = exportFormatId;
    }

    /**
     * @return Returns the mimeType.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType
     *            The mimeType to set.
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String getName() {
        return name;
    }
}
