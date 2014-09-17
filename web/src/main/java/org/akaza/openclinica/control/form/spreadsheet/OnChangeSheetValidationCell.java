/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.control.form.spreadsheet;

import java.util.Arrays;



/**
 * <p>One spreadsheet cell to be validated for instant-calculation func: onChange.<br/>
 * For func: onChange(oriName, optionValue), arguments[0] is origin item name;
 * arguments[1] is optionValue.</p>
 *
 */
//ywang (Aug., 2011)
public class OnChangeSheetValidationCell {

    private final OnChangeSheetValidationType type;
    private final SheetValidationCell sheetValidationCell;
    /**
     * If true, always validate OnChangeType first.
     */
    private Boolean onChangeTypeFirst;

    /**
     * final OnChangeSheetValidationType has been set as NONE;
     * onChangeTypeFirst has been set as false;
     * sheetValidationType has been set as NONE.
     *
     * @param sheetCell
     */
    public OnChangeSheetValidationCell(SheetCell sheetCell) {
        this.sheetValidationCell = new SheetValidationCell(sheetCell);
        this.type = OnChangeSheetValidationType.NONE;
        this.onChangeTypeFirst = Boolean.FALSE;
    }

    /**
     * onChangeTypeFirst has been set as true.
     * sheetValidationType has been set as NONE.
     *
     * @param onValidationType
     * @param sheetCell
     */
    public OnChangeSheetValidationCell(OnChangeSheetValidationType onValidationType, SheetCell sheetCell) {
        this.sheetValidationCell = new SheetValidationCell(sheetCell);
        this.type = onValidationType;
        this.onChangeTypeFirst = Boolean.TRUE;
    }

    /**
     * onChangeTypeFirst has been set as true.
     *
     * @param onValidationType
     * @param sheetValidationType
     * @param sheetCell
     */
    public OnChangeSheetValidationCell(OnChangeSheetValidationType onValidationType,
            SheetValidationType sheetValidationType, SheetCell sheetCell) {
        this.sheetValidationCell = new SheetValidationCell(sheetValidationType, sheetCell);
        this.type = onValidationType;
        this.onChangeTypeFirst = Boolean.TRUE;
    }

    /**
     * Specially for funcOnChangeStr = func: onChange(oriName, optionValue)
     * @param funcOnChangeStr
     * @return
     */
    public static String[] funcOnChangeArguments(String funcOnChangeStr) {
        String[] al = new String[2];
        al[0] = funcOnChangeStr.substring(funcOnChangeStr.indexOf("(", 0)+1, funcOnChangeStr.indexOf(",", 0)).trim();
        al[1] = funcOnChangeStr.substring(funcOnChangeStr.indexOf(",", 0)+1, funcOnChangeStr.indexOf(")", 0)).trim();
        return al;
    }

    /**
     * Set arguments specially for func: onChange(oriName, optionValue)
     */
    public void setOriAndOption() {
        String[] al = OnChangeSheetValidationCell.funcOnChangeArguments(sheetValidationCell.getSheetArgumentCell().getSheetCell().getColValue());
        sheetValidationCell.getSheetArgumentCell().setArguments(Arrays.asList(al));
    }

    /**
     * Precodition: arguments have been valid initialized
     * @return
     */
    protected String getOriName() {
        return sheetValidationCell.getSheetArgumentCell().getArguments().get(0).toString();
    }

    /**
     * Precodition: arguments have been valid initialized
     * @return
     */
    protected String getOptionValue() {
        return sheetValidationCell.getSheetArgumentCell().getArguments().get(1).toString();
    }

    public OnChangeSheetValidationType getType() {
        return type;
    }

    public SheetValidationCell getSheetValidationCell() {
        return sheetValidationCell;
    }

    public void setOnChangeTypeFirst(Boolean onChangeTypeFirst) {
        this.onChangeTypeFirst = onChangeTypeFirst;
    }

    public Boolean getOnChangeTypeFirst() {
        return onChangeTypeFirst;
    }
}