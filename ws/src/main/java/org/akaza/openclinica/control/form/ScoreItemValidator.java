/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 *
 * Created on Sep 21, 2005
 */
package org.akaza.openclinica.control.form;

import javax.servlet.http.HttpServletRequest;

/**
 * A Validator for 'calculation' and 'group-calculation' type Items whose
 * fieldNames are always from request attribute.
 *
 * @author ywang (Feb. 2008)
 *
 */
public class ScoreItemValidator extends DiscrepancyValidator {
    private FormDiscrepancyNotes notes;

    public ScoreItemValidator(HttpServletRequest request, FormDiscrepancyNotes notes) {
        // super(request);
        super(request, notes);
        this.notes = notes;
    }

    @Override
    protected String getFieldValue(String fieldName) {
        return (String) request.getAttribute(fieldName);
    }

}
