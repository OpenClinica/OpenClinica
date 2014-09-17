/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */

package org.akaza.openclinica.control.form.spreadsheet;

import org.akaza.openclinica.domain.crfdata.InstantOnChangeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * SpreadSheet loading validate for instant_calculation func:onChange
 *
 */
//ywang (Aug., 2011)
public class OnChangeSheetValidator implements SpreadSheetValidator{

    private final SheetValidationContainer sheetValidationContainer;
    private List<OnChangeSheetValidationCell> cells;
    private SheetErrors sheetErrors;


    public OnChangeSheetValidator(SheetValidationContainer spreadSheetValidationContainer,
            ResourceBundle resPageMsg) {
        this.sheetValidationContainer = spreadSheetValidationContainer;
        cells = new ArrayList<OnChangeSheetValidationCell>();
        sheetErrors = new SheetErrors(resPageMsg);
    }

    /**
     * Validate cells List.
     */
    public void validate() {
        for(OnChangeSheetValidationCell cell: this.cells) {
            validateWithOrder(cell);
        }
    }

    /*
     * If onChangeTypeFirst of cell is true, validate cell's OnChangeValidationType first, then cell's SheetValidationType;
     * vise versa.
     */
    private void validateWithOrder(OnChangeSheetValidationCell cell) {
        if(cell.getOnChangeTypeFirst() == Boolean.TRUE) {
            validateOnChangeType(cell);
            SheetValidatorHelper.validateSheetValidationCell(cell.getSheetValidationCell(),sheetErrors);
        } else {
            SheetValidatorHelper.validateSheetValidationCell(cell.getSheetValidationCell(),sheetErrors);
            validateOnChangeType(cell);
        }
    }

    /*
     *  Validate only OnChangeValidationType of an OnChangeSheetValidationCell
     *
     * @param cell
     */
    private void validateOnChangeType(OnChangeSheetValidationCell cell) {
        if(cell.getType() == OnChangeSheetValidationType.ALL) {
            validateCellResValueAll(cell);
        }
    }


    private void validateCellResValueAll(OnChangeSheetValidationCell onChangeSheetValidationCell) {
        boolean printHtmlErr = false;
        SheetCell cell = onChangeSheetValidationCell.getSheetValidationCell().getSheetArgumentCell().getSheetCell();
        ResourceBundle resPageMsg = sheetErrors.getResPageMsg();
        //IS_REQUIRED
        if(cell.getColValue() == null || cell.getColValue().isEmpty()) {
            StringBuffer message = sheetErrors.errorMessage(resPageMsg.getString(cell.getColTitle()),
                    resPageMsg.getString(SheetValidationType.IS_REQUIRED.getDescription()), resPageMsg.getString(cell.getForWhich()));
            sheetErrors.addError(cell.getRowNum(), message);
            sheetErrors.putHtmlError(cell.getSheetNum(), cell.getRowNum(), cell.getColNum(), resPageMsg.getString("required_field"));
        } else {
        //switch (cell.getOnChangeType()) {
        //case SHOULD_BE_FUNC_ONCHANGE:
            if(!OnChangeSheetValidator.isValidInstantOnChangeSyntax(cell.getColValue())) {
                StringBuffer message = sheetErrors.errorMessage(resPageMsg.getString(cell.getColTitle()),
                        resPageMsg.getString(OnChangeSheetValidationType.SHOULD_BE_FUNC_ONCHANGE.getDescription()), resPageMsg.getString(cell.getForWhich()));
                sheetErrors.addError(cell.getRowNum(), message);
                printHtmlErr = true;
            } else {
                onChangeSheetValidationCell.setOriAndOption();
       //     break;
        //case SHOULD_BE_FUNC_ONCHANGE_TYPE:
                    if(!OnChangeSheetValidator.isValidInstantOnChangeType(onChangeSheetValidationCell)) {
                        StringBuffer message = sheetErrors.errorMessage(resPageMsg.getString(cell.getColTitle()),
                                resPageMsg.getString(OnChangeSheetValidationType.SHOULD_BE_FUNC_ONCHANGE_TYPE.getDescription()), resPageMsg.getString(cell.getForWhich()));
                        sheetErrors.addError(cell.getRowNum(), message);
                        printHtmlErr = true;
                    }
                //    break;
                //case IS_ITEM_PAIR:
                if(!isValidInstantOnChangeItemPair(onChangeSheetValidationCell)) {
                    //printHtmlErr = true;
                }
                //    break;
                //case NONE: break;
                //}
            }
        }
        //if(printHtmlErr)
        //    errors.putHtmlError(cell.getSheetNum(), cell.getRowNum(), cell.getColNum(), resPageMsg.getString("INVALID_FIELD"));
        //print other message first and "INVALID_FIELD" only once
        if(printHtmlErr && !sheetErrors.htmlErrors.containsKey(sheetErrors.htmlErrorKey(cell.getSheetNum(), cell.getRowNum(), cell.getColNum())))
            sheetErrors.putHtmlError(cell.getSheetNum(), cell.getRowNum(), cell.getColNum(), resPageMsg.getString("INVALID_FIELD"));
    }

    public void addValidationCells(OnChangeSheetValidationCell cell) {
        this.cells.add(cell);
    }

    /**
     * For func: onChange(item, option)
     * @return
     */
    public static boolean isValidInstantOnChangeSyntax(String resValues) {
        String value = resValues.trim();
        if (value.startsWith("func:")) {
            String ss = value.substring(5).trim();
            String s = ss.substring(0, 8).trim();
            if("onChange".equalsIgnoreCase(s)) {
                s = ss.substring(8).trim();
                if(s.startsWith("(") && s.endsWith(")")) {
                    s = s.substring(1,s.length()-1).trim();
                    String[] t = s.split(",");
                    if(t.length == 2)  return true;
                }
            }
        }
        return false;
    }

    private boolean isValidInstantOnChangeItemPair(OnChangeSheetValidationCell onChangeSheetValidationCell) {
        SheetCell cell = onChangeSheetValidationCell.getSheetValidationCell().getSheetArgumentCell().getSheetCell();
        ResourceBundle resPageMsg = sheetErrors.getResPageMsg();
        Map<String, String> allItems = sheetValidationContainer.getAllItems();
        if(cell.getColValue() != null && cell.getColValue().length()>0 && allItems.containsKey(onChangeSheetValidationCell.getOriName())) {
            if(sheetValidationContainer.inSameSection(cell.getRowName(), onChangeSheetValidationCell.getOriName())) {
                if(isValidInstantOnChangeGroupPair(onChangeSheetValidationCell.getOriName(), cell.getRowName())) {
                    return true;
                } else {
                    StringBuffer s = new StringBuffer(onChangeSheetValidationCell.getOriName());
                    s.append(" ");
                    s.append(resPageMsg.getString("and"));
                    s.append(" ");
                    s.append(cell.getRowName());
                    s.append(" ");
                    s.append(resPageMsg.getString(OnChangeSheetValidationType.SHOULD_IN_SAME_REPEATING_GROUP.getDescription()));
                    StringBuffer message = sheetErrors.errorMessage(resPageMsg.getString(cell.getColTitle()), s.toString(),
                            resPageMsg.getString(cell.getForWhich()));
                    sheetErrors.addError(cell.getRowNum(), message);
                    sheetErrors.putHtmlError(cell.getSheetNum(), cell.getRowNum(), 6, resPageMsg.getString("INVALID_FIELD"));
                }
            }else {
                StringBuffer s = new StringBuffer(cell.getRowName());
                s.append(", ");
                s.append(onChangeSheetValidationCell.getOriName());
                s.append(" ");
                s.append(resPageMsg.getString(OnChangeSheetValidationType.SHOULD_IN_SAME_SECTION.getDescription()));
                StringBuffer message = sheetErrors.errorMessage(resPageMsg.getString(cell.getColTitle()),
                        s.toString(), resPageMsg.getString(cell.getForWhich()));
                sheetErrors.addError(cell.getRowNum(), message);
                sheetErrors.putHtmlError(cell.getSheetNum(), cell.getRowNum(), 5, resPageMsg.getString("INVALID_FIELD"));
            }
        } else {
            StringBuffer s = new StringBuffer(onChangeSheetValidationCell.getOriName());
            s.append(" ");
            s.append(resPageMsg.getString(SheetValidationType.ITEM_NAME_SHOULD_PROVIDED.getDescription()));
            StringBuffer message = sheetErrors.errorMessage(resPageMsg.getString(cell.getColTitle()),
                    s.toString(), resPageMsg.getString(cell.getForWhich()));
            sheetErrors.addError(cell.getRowNum(), message);
            if(!sheetErrors.htmlErrors.containsKey(sheetErrors.htmlErrorKey(cell.getSheetNum(), cell.getRowNum(), cell.getColNum())))
                sheetErrors.putHtmlError(cell.getSheetNum(), cell.getRowNum(), cell.getColNum(), resPageMsg.getString("INVALID_FIELD"));
        }
        return sheetErrors.getErrors().size() == 0;
    }

    public static boolean isValidInstantOnChangeType(OnChangeSheetValidationCell cell) {
        return cell.getOptionValue() != null && cell.getOptionValue().length()>0
            && InstantOnChangeType.isValidTypeByDescription(cell.getOptionValue());
    }


    public boolean isValidInstantOnChangeGroupPair(String oriName, String destName) {
        if(!sheetValidationContainer.inRepeatingGroup(oriName)
                && !sheetValidationContainer.inRepeatingGroup(destName)) {
            return true;
        } else {
            return sheetValidationContainer.inSameRepeatingGroup(oriName, destName);
        }
    }

    public SheetErrors getSheetErrors() {
        return sheetErrors;
    }

    public List<OnChangeSheetValidationCell> getCells() {
        return cells;
    }

    public void setCells(List<OnChangeSheetValidationCell> cells) {
        this.cells = cells;
    }

    public SheetValidationContainer getSheetValidationContainer() {
        return sheetValidationContainer;
    }

    public void setSheetErrors(SheetErrors sheetErrors) {
        this.sheetErrors = sheetErrors;
    }
}