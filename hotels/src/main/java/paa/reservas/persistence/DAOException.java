package paa.reservas.persistence;

public class DAOException extends RuntimeException {
    private static final long serialVersionUID = -6110113443255494262L;

	public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(Throwable cause) {
        super(cause);
    }
}
