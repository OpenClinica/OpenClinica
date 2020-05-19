package org.akaza.openclinica.service;

import com.epam.parso.CSVDataWriter;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.CSVDataWriterImpl;
import com.epam.parso.impl.SasFileReaderImpl;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * Converts SAS data file into pipe-delimited text file.
 * @author svadla@openclinica.com
 */
@Service("sasFileConverterService")
public class SasFileConverterServiceImpl implements FileConverterService {
  @Override
  public File convert(File sasFile) throws IOException {
    InputStream inputStream = new FileInputStream(sasFile);
    SasFileReader sasFileReader = new SasFileReaderImpl(inputStream);
    File delimitedOutputFile = File.createTempFile(sasFile.getName(), OUTPUT_FILE_SUFFIX);
    Writer writer = new FileWriter(delimitedOutputFile);
    CSVDataWriter csvDataWriter = new CSVDataWriterImpl(writer, Character.toString(OUTPUT_DELIMITER));
    csvDataWriter.writeColumnNames(sasFileReader.getColumns());
    csvDataWriter.writeRowsArray(sasFileReader.getColumns(), sasFileReader.readAll());
    writer.flush();
    return delimitedOutputFile;
  }
}
