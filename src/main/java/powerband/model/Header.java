package powerband.model;

/**
 * Encabezado de una grabación en recordings.music.
 * start = byte donde empieza el primer comando; end = byte donde termina el último (exclusivo).
 */
public record Header(String name, long start, long end, int count) {

    /** Encabezado vacío: se escribe al EMPEZAR a grabar (el nombre se pide al terminar). */
    public static Header placeholder(long start) {
        return new Header("", start, start, 0);
    }
}
