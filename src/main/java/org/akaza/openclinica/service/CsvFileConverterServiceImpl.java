package org.akaza.openclinica.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Converts CSV file into pipe-delimited text file.
 * @author svadla@openclinica.com
 */
@Service("csvFileConverterService")
public class CsvFileConverterServiceImpl implements FileConverterService {
  private final static Logger logger = LoggerFactory.getLogger(CsvFileConverterServiceImpl.class);
  private static final char DEFAULT_DELIMITER = ',';

  @Override
  public File convert(File csvFile) throws IOException {
    return convert(csvFile, DEFAULT_DELIMITER);
  }

  /**
   * Converts the given file to pipe-delimited text file.
   * @param csvFile file to convert
   * @param delimiter the character used as a delimiter in the input file
   * @return pipe-delimited text file.
   * @throws IOException if there is an error reading/writing files.
   */
  public File convert(File csvFile, Character delimiter) throws IOException {
    CSVFormat inputCsvFormat = CSVFormat.DEFAULT
            .withDelimiter(delimiter);
    CSVParser parser = CSVParser.parse(csvFile, Charset.forName(CHARACTER_ENCODING), inputCsvFormat);
    CSVFormat outputCsvFormat = CSVFormat.DEFAULT
            .withDelimiter(OUTPUT_DELIMITER);

    File delimitedOutputFile = File.createTempFile(csvFile.getName(), OUTPUT_FILE_SUFFIX);
    try (CSVPrinter printer = new CSVPrinter(new FileWriter(delimitedOutputFile), outputCsvFormat)) {
      for (CSVRecord csvRecord : parser.getRecords()) {
        Iterable<String> iterableCsvRecord = StreamSupport.stream(csvRecord.spliterator(), true)
                .map(cellValue -> {
                  // Replace any return characters with blank string and non-printable characters from Unicode
                  cellValue = cellValue.replaceAll("\\p{C}", "");
                  cellValue = cellValue.replaceAll("\n", "");
                  return cellValue;
                })
                .collect(Collectors.toList());

        printer.printRecord(iterableCsvRecord);
      }
    } catch (IOException e) {
      logger.error("Unable to read/write csv file", e);
      return null;
    }
    return delimitedOutputFile;
  }
}
