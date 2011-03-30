package org.akaza.openclinica.control;

import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.jmesa.core.CoreContext;
import org.jmesa.facade.TableFacadeImpl;
import org.jmesa.limit.ExportType;
import org.jmesa.view.View;
import org.jmesa.view.csv.CsvViewExporter;
import org.jmesa.view.excel.ExcelViewExporter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OCTableFacadeImpl extends TableFacadeImpl {

    private final HttpServletResponse response;
    private final HttpServletRequest request;
    private final String fileName;

    public OCTableFacadeImpl(String id, HttpServletRequest request, HttpServletResponse response, String fileName) {
        super(id, request);
        this.response = response;
        this.fileName = fileName + System.currentTimeMillis();
        this.request = request;
    }

    @Override
    protected View getExportView(ExportType exportType) {

        if (exportType == ExportType.PDF) {
            return new XmlView(getTable(), getCoreContext());
        } else {
            return super.getExportView(exportType);
        }
    }

    @Override
    protected void renderExport(ExportType exportType, View view) {

        try {
            CoreContext cc = getCoreContext();

            if (exportType == ExportType.CSV) {
//                new OCCsvViewExporter(view, cc, response, fileName).export();
                 new CsvViewExporter(view, cc, response, fileName + ".txt").export();
            } else if (exportType == ExportType.EXCEL) {
                new ExcelViewExporter(view, cc, response, fileName + ".xls").export();
            } else if (exportType == ExportType.PDF) {
                new XmlViewExporter(view, cc, request, response).export();
            } else {
                super.renderExport(exportType, view);
            }
        } catch (Exception e) {
            throw new OpenClinicaSystemException(e);
            // logger.error("Not able to perform the " + exportType + " export.");
        }
    }
}
