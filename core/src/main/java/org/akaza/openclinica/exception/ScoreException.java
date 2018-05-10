/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
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