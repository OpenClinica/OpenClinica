/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.control.form.spreadsheet;


/**
 * One spreadsheet cell to be validated upon CRF spreadsheet loading.
 *
 */
//ywang (Aug., 2011)
public class SheetCell {
    /*
     * YW: created file at Aug., 2011 with OnChangeSheetValidator
     * cooperating with current spreadsheet loading validation style.
     */
    /**
     * Could be Item_name, group_label, section_label.
     */
    private final String rowName;
    /**
     * e.g., for UNITS column: kg
     */
    private final String colValue;
    /**
     * e.g., UNITS_column
     */
    private final String colTitle;
    private final int sheetNum;
    private final int rowNum;
    private final int colNum;
    /**
     * e.g., validation is for "instant_calculation"
     */
    private final String forWhich;

    public static class Builder {
        private String rowName = "";
        private String colValue = "";
        private String colTitle = "";
        private int sheetNum = -1;
        private int rowNum = -1;
        private int colNum = -1;
        private String forWhich = "";

        public Builder() {}

        public Builder rowName(String rowName) {
            this.rowName = rowName;  return this;
        }
        public Builder colValue(String colValue) {
            this.colValue = colValue;   return this;
        }
        public Builder sheetNum(int sheetNum) {
            this.sheetNum = sheetNum;   return this;
        }
        public Builder rowNum(int rowNum) {
            this.rowNum = rowNum;   return this;
        }
        public Builder colNum(int colNum) {
            this.colNum = colNum;   return this;
        }
        public Builder colTitle(String colTitle) {
            this.colTitle = colTitle;   return this;
        }
        public Builder forWhich(String forWhich) {
            this.forWhich = forWhich;   return this;
        }

        public SheetCell build() {
            return new SheetCell(this);
        }
    }

    private SheetCell(Builder builder) {
        this.rowName = builder.rowName;
        this.colValue = builder.colValue;
        this.colTitle = builder.colTitle;
        this.sheetNum = builder.sheetNum;
        this.rowNum = builder.rowNum;
        this.colNum = builder.colNum;
        this.forWhich = builder.forWhich;
    }

    public String getRowName() {
        return rowName;
    }
    public String getColValue() {
        return colValue;
    }
    public String getColTitle() {
        return colTitle;
    }
    public int getSheetNum() {
        return sheetNum;
    }
    public int getRowNum() {
        return rowNum;
    }
    public int getColNum() {
        return colNum;
    }
    public String getForWhich() {
        return forWhich;
    }
}