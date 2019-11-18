/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.control.form.spreadsheet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * For validation of spreadsheet loading on SheetValidationType.
 *
 */
public class SheetValidator implements SpreadSheetValidator{
    /*
     * YW: created file at Aug., 2011 with OnChangeSheetValidator
     * cooperating with current spreadsheet loading validation style.
     * Shall it be updated upon spreadsheet validation refactoring.
     */

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private SheetValidationContainer sheetValidationContainer;
    private List<SheetValidationCell> cells;
    private SheetErrors sheetErrors;



    public SheetValidator() {
        sheetValidationContainer = new SheetValidationContainer();
        cells = new ArrayList<SheetValidationCell>();
        sheetErrors = new SheetErrors();
    }
    public SheetValidator(SheetValidationContainer spreadSheetValidationContainer, ResourceBundle resPageMsg) {
        this.sheetValidationContainer = spreadSheetValidationContainer;
        sheetErrors = new SheetErrors(resPageMsg);
    }

    public void validate() {
        for(SheetValidationCell cell: this.cells) {
            validate(cell);
        }
    }

    public void validate(SheetValidationCell sheetValidationCell) {
        SheetValidatorHelper.validateSheetValidationCell(sheetValidationCell, sheetErrors);
    }



    public SheetValidationContainer getSheetValidationContainer() {
        return sheetValidationContainer;
    }
    public void setSheetValidationContainer(SheetValidationContainer sheetValidationContainer) {
        this.sheetValidationContainer = sheetValidationContainer;
    }
    public List<SheetValidationCell> getCells() {
        return cells;
    }
    public void setCells(List<SheetValidationCell> cells) {
        this.cells = cells;
    }
    public SheetErrors getSheetErrors() {
        return sheetErrors;
    }
    public void setSheetErrors(SheetErrors sheetErrors) {
        this.sheetErrors = sheetErrors;
    }
}