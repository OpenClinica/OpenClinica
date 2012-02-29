/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2011 Akaza Research
 */

package org.akaza.openclinica.control.form.spreadsheet;

/**
 * Instant-calculation func:onchange validation type for loading CRF spreadsheet
 */
//ywang (Aug., 2011)
public enum OnChangeSheetValidationType {
    NONE(0, "none"), ALL(1, "all"), SHOULD_BE_FUNC_ONCHANGE(2, "response_value_is_func_onchange"),
    SHOULD_BE_FUNC_ONCHANGE_TYPE(3, "value_should_be_func_onchange_type"),
    SHOULD_BE_VALID_PAIR(4, "two_items_should_be_valid_pair"),
    SHOULD_IN_SAME_SECTION(5, "should_in_same_section"),
    SHOULD_IN_SAME_REPEATING_GROUP(6, "should_in_same_repeating_group");

    int code;
    String description;

    private OnChangeSheetValidationType(int code, String description) {
        this.code = code;
        this.description = description;
    }


    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
