package powerband.exception;

/**
 * Excepción propia de todo el programa. Es "checked": el compilador obliga a decidir qué hacer.
 * Cuando algo estándar falla (IOException, SocketException...) se envuelve aquí con "cause",
 * así no se pierde el error original.
 */
public class PowerBandException extends Exception {

    public PowerBandException(String message) {
        super(message);
    }

    public PowerBandException(String message, Throwable cause) {
        super(message, cause);
    }
}
