/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * copyright 2003-2011 Akaza Research
 */
package org.akaza.openclinica.control.form.spreadsheet;

/**
 * For CRF SpreadSheet uploading validation
 *
 */
//ywang (Aug. 2011)
public interface SpreadSheetValidator {
    public SheetErrors getSheetErrors();
    public void validate();
}
