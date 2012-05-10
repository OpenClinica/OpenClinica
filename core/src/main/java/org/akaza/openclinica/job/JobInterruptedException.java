package org.akaza.openclinica.job;

/**
 * Thrown to indicate that a job was interrupted. 
 * @author Leonel Gayard, leonel.gayard@openclinica.com
 */
@SuppressWarnings("serial")
public class JobInterruptedException extends Exception {
	public JobInterruptedException() {}

	public JobInterruptedException(String message) {
		super(message);
	}
	
	public JobInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}
}
