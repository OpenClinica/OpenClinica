package org.akaza.openclinica.control;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.RichTextString;
import org.jmesa.core.CoreContext;
import org.jmesa.view.component.Column;
import org.jmesa.view.component.Row;
import org.jmesa.view.component.Table;
import org.jmesa.view.excel.ExcelView;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Update setCellValue to pass RichTextString instead of HSSFRichTextString because new version of POI setCellValue
 * method expects RichTextString
 * @author Shu Lin Chan
 */
public class OCExcelView extends ExcelView {

    public OCExcelView(Table table, CoreContext coreContext) {
        super(table, coreContext);
    }

    public Object render() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        Table table = this.getTable();
        String caption = table.getCaption();
        if (StringUtils.isEmpty(caption)) {
            caption = "JMesa Export";
        }

        HSSFSheet sheet = workbook.createSheet(caption);
        Row row = table.getRow();
        row.getRowRenderer();
        List<Column> columns = table.getRow().getColumns();
        HSSFRow hssfRow = sheet.createRow(0);
        int columncount = 0;
        Iterator i$ = columns.iterator();

        while (i$.hasNext()) {
            Column col = (Column) i$.next();
            HSSFCell cell = hssfRow.createCell((short) (columncount++));
            RichTextString richTextString = workbook.getCreationHelper().createRichTextString(col.getTitle());
            cell.setCellValue(richTextString);
        }

        Collection<?> items = this.getCoreContext().getPageItems();
        int rowcount = 1;
        i$ = items.iterator();

        while (i$.hasNext()) {
            Object item = i$.next();
            HSSFRow r = sheet.createRow(rowcount++);
            columncount = 0;
            i$ = columns.iterator();

            while (i$.hasNext()) {
                Column col = (Column) i$.next();
                HSSFCell cell = r.createCell((short) (columncount++));
                Object value = col.getCellRenderer().render(item, rowcount);
                if (value == null) {
                    value = "";
                }

                if (value instanceof Number) {
                    Double number = Double.valueOf(value.toString());
                    cell.setCellValue(number);
                } else {
                    RichTextString richTextString = workbook.getCreationHelper().createRichTextString(value.toString());
                    cell.setCellValue(richTextString);
                }
            }
        }

        return workbook;
    }
}
