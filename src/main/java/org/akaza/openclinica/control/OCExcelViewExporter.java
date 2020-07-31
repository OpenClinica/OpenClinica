package org.akaza.openclinica.control;

import org.jmesa.core.CoreContext;
import org.jmesa.view.View;
import org.jmesa.view.excel.ExcelViewExporter;

import javax.servlet.http.HttpServletResponse;

/**
 * Update ExcelView to use OCExcelView instead because POI expects a RichTextString instead of HSSFRichTextString
 * in the setCell method
 * @author Shu Lin Chan
 */
public class OCExcelViewExporter extends ExcelViewExporter {

    public OCExcelViewExporter(View view, CoreContext coreContext, HttpServletResponse response, String fileName) {
        super(view, coreContext, response, fileName);
        setView(new OCExcelView(super.getView().getTable(), coreContext));
    }
}
