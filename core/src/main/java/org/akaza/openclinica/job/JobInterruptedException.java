/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.job;

/**
 * Thrown to indicate that a job was interrupted.
 * @author Leonel Gayard, leonel.gayard@openclinica.com
 */
@SuppressWarnings("serial")
public class JobInterruptedException extends RuntimeException {
	public JobInterruptedException() {}

	public JobInterruptedException(String message) {
		super(message);
	}

	public JobInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}
}
