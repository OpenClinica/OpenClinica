package org.akaza.openclinica.control.admin;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: Jun 15, 2007
 *
 */
public interface Preview {
    Map<String, Map> createCrfMetaObject(HSSFWorkbook workbook);

    Map<Integer, Map<String, String>> createItemsOrSectionMap(HSSFWorkbook workbook, String itemsOrSection);

    Map<Integer, Map<String, String>> createGroupsMap(HSSFWorkbook workbook);

    Map<String, String> createCrfMap(HSSFWorkbook workbook);
}
