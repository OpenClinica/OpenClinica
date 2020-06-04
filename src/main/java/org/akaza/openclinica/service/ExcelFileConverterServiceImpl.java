package org.akaza.openclinica.service;

import com.google.common.collect.Streams;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Converts excel file into pipe-delimited text file.
 * @author svadla@openclinica.com
 */
@Service("excelFileConverterService")
public class ExcelFileConverterServiceImpl implements FileConverterService {
  private final static Logger logger = LoggerFactory.getLogger(ExcelFileConverterServiceImpl.class);

  @Override
  public File convert(File xlsFile) throws IOException {
    File delimitedOutputFile = File.createTempFile(xlsFile.getName(), OUTPUT_FILE_SUFFIX);
    try (FileOutputStream outputStream = new FileOutputStream(delimitedOutputFile);
         PrintStream printStream = new PrintStream(outputStream, true, CHARACTER_ENCODING);
         XSSFWorkbook workbook = new XSSFWorkbook(xlsFile)) {
      DataFormatter formatter = new DataFormatter();
      Sheet sheet = workbook.getSheetAt(0);
      Row headerRow = sheet.getRow(sheet.getTopRow());
      int numberOfColumns = headerRow.getLastCellNum();
      sheet.forEach(row -> {
        for (int cellNumber = 0; cellNumber < numberOfColumns; cellNumber++) {
          Cell cell = row.getCell(cellNumber, Row.CREATE_NULL_AS_BLANK);
          if (cell.getColumnIndex() != 0) {
            printStream.print(OUTPUT_DELIMITER);
          }
          String cellValue = formatter.formatCellValue(cell);
          // Replace any return characters with blank string
          cellValue = cellValue.replaceAll("\n", "");
          printStream.print(cellValue);
        }
        printStream.println();
      });
    } catch (InvalidFormatException e) {
      logger.error("Invalid excel format", e);
      return null;
    }
    return delimitedOutputFile;
  }
}
