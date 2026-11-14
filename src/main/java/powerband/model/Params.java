package powerband.model;

import powerband.constants.Constants;

/**
 * Los parámetros de UN comando, iguales para los 3 synths (así todos los registros miden lo mismo).
 *   piano:  note, durMs, extra = tercera (4 mayor / 3 menor)
 *   bajo:   note, durMs
 *   órgano: note, extra = número de timbre (índice en Constants.ORGAN_TIMBRES)
 * Lo que un synth no usa queda en 0. Se crean con los métodos estáticos de abajo.
 */
public record Params(Action action, int note, int durMs, int extra) {

    public static Params piano(int note, int durMs, boolean major) {
        return new Params(Action.PLAY, note, durMs, major ? Constants.THIRD_MAJOR : Constants.THIRD_MINOR);
    }

    public static Params bass(int note, int durMs) {
        return new Params(Action.PLAY, note, durMs, 0);
    }

    public static Params organOn(int note, int timbre) {
        return new Params(Action.NOTE_ON, note, 0, timbre);
    }

    public static Params organOff(int note) {
        return new Params(Action.NOTE_OFF, note, 0, 0);
    }
}
