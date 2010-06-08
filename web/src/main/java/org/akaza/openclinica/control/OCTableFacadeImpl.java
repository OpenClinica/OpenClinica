package org.akaza.openclinica.control;

import org.jmesa.core.CoreContext;
import org.jmesa.facade.TableFacadeImpl;
import org.jmesa.limit.ExportType;
import org.jmesa.view.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OCTableFacadeImpl extends TableFacadeImpl {

    private final HttpServletResponse response;
    private final String fileName;

    public OCTableFacadeImpl(String id, HttpServletRequest request, HttpServletResponse response, String fileName) {
        super(id, request);
        this.response = response;
        this.fileName = fileName;
    }

    @Override
    protected void renderExport(ExportType exportType, View view) {

        try {
            CoreContext cc = getCoreContext();

            if (exportType == ExportType.CSV) {
                new OCCsvViewExporter(view, cc, response, fileName).export();
            } else {
                super.renderExport(exportType, view);
            }
        } catch (Exception e) {
            //logger.error("Not able to perform the " + exportType + " export.");
        }
    }

}
