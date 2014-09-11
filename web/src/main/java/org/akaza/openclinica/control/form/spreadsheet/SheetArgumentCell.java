/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.control.form.spreadsheet;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Abstract class contains final SheetCell initialized in Constructor.
 * It also contains arguments List which might need additional validation.</p>
 */
//ywang (Aug. 2011)
public class SheetArgumentCell {
    private final SheetCell sheetCell;
    private List<? extends Object> arguments;

    public SheetArgumentCell(SheetCell sheetCell) {
        this.sheetCell = sheetCell;
        this.arguments = new ArrayList<Object>();
    }


    public SheetCell getSheetCell() {
        return sheetCell;
    }
    public List<? extends Object> getArguments() {
        return arguments;
    }
    public void setArguments(List<? extends Object> arguments) {
        this.arguments = arguments;
    }
}