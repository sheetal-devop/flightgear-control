package org.jason.fgcontrol.exceptions;

public class AircraftStartupException extends Exception {
	
	private static final long serialVersionUID = -369184535060898991L;
	
	public AircraftStartupException() {
		super();
	}

	public AircraftStartupException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AircraftStartupException(String message, Throwable cause) {
		super(message, cause);
	}

	public AircraftStartupException(String message) {
		super(message);
	}

	public AircraftStartupException(Throwable cause) {
		super(cause);
	}
}
