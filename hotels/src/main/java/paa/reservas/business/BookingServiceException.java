package paa.reservas.business;

public class BookingServiceException extends Exception {
    private static final long serialVersionUID = 4964804175316089172L;

	public BookingServiceException(String message) {
        super(message);
    }

    public BookingServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public BookingServiceException(Throwable cause) {
        super(cause);
    }
}
