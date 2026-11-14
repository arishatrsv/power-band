package powerband.model;

/**
 * Un nombre + un número. Sirve para dos archivos:
 *   recordings.index -> value = posición (byte) del encabezado en recordings.music
 *   recordings.list  -> value = fecha y hora de la grabación (epoch en ms)
 */
public record NameEntry(String name, long value) {
}
