/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 *
 * Created on Sep 21, 2005
 */
package org.akaza.openclinica.control.form;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * A class for validating a form which may have discrepancy notes attached
 *
 * This class executes the rule that if a form field has a discrepancy note
 * attached, it should not be validated. The mechanism is via the
 * <code>validate(String, Validation)</code> method. When this method is
 * executed by this class, the class checks to see if a discrepancy note is
 * available for the field. If so, it takes no action. Otherwise, it executes
 * the validation using the superclass's validate method.
 *
 * You can use this class exactly as you use the Validator class. Simple declar
 * objects to be DiscrepancyValidator, rather than Validator, objects, and
 * provide the appropriate FormDiscrepancyNotes object in the constructor.
 *
 * @author ssachs
 */
public class DiscrepancyValidator extends Validator {
    private final FormDiscrepancyNotes notes;

    public DiscrepancyValidator(HttpServletRequest request, FormDiscrepancyNotes notes) {
        super(request);
        this.notes = notes;
    }

    @Override
    protected HashMap validate(String fieldName, Validation v) {
        if (!v.isAlwaysExecuted()) {
            if (notes.hasNote(fieldName) || notes.getNumExistingFieldNotes(fieldName) > 0) {
                return errors;
            }
        }

        return super.validate(fieldName, v);
    }

    public void alwaysExecuteLastValidation(String fieldName) {
        ArrayList fieldValidations = getFieldValidations(fieldName);

        if (validations.size() >= 1) {
            Validation v = (Validation) fieldValidations.get(fieldValidations.size() - 1);
            v.setAlwaysExecuted(true);
            fieldValidations.set(fieldValidations.size() - 1, v);
        }
        validations.put(fieldName, fieldValidations);
    }
}