/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */

package org.akaza.openclinica.exception;

/**
 * Subclass of OpenClinicaException
 *
 * @author ywang (Mar. 2008)
 *
 */
public class ScoreException extends OpenClinicaException {
    public ScoreException(String message, String errorId) {
        super(message, errorId);
    }
}