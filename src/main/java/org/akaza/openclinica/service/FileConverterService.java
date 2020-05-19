package org.akaza.openclinica.service;

import java.io.File;
import java.io.IOException;

/**
 * Interface for converting files into pipe-delimited text file.
 * @author svadla@openclinica.com
 */
public interface FileConverterService {
  char OUTPUT_DELIMITER = '|';
  String OUTPUT_FILE_SUFFIX = ".txt";
  String CHARACTER_ENCODING = "UTF-8";

  /**
   * Converts the given file to pipe-delimited text file.
   * @param fileToConvert file to convert
   * @return pipe-delimited text file.
   * @throws IOException if there is an error reading/writing files.
   */
  File convert(File fileToConvert) throws IOException;
}
