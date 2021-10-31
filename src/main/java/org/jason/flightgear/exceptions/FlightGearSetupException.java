package org.jason.flightgear.exceptions;

public class FlightGearSetupException extends Exception {
	private static final long serialVersionUID = 3529220294960487143L;

	public FlightGearSetupException() {
	}

	public FlightGearSetupException(String message) {
		super(message);
	}

	public FlightGearSetupException(Throwable cause) {
		super(cause);
	}

	public FlightGearSetupException(String message, Throwable cause) {
		super(message, cause);
	}

	public FlightGearSetupException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
