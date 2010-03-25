package org.akaza.openclinica.control.admin;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: Jun 15, 2007
 *
 */
public interface Preview {
    Map<String, Map> createCrfMetaObject(Workbook workbook);

    Map<Integer, Map<String, String>> createItemsOrSectionMap(Workbook workbook, String itemsOrSection);

    Map<Integer, Map<String, String>> createGroupsMap(Workbook workbook);

    Map<String, String> createCrfMap(Workbook workbook);
}
