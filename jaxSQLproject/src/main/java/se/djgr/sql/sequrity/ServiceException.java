package se.djgr.sql.sequrity;

public final class ServiceException extends Exception {

	private static final long serialVersionUID = -447930661315280450L;

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(String message) {
		super(message);
	}

}
