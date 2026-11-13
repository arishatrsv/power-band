package powerband.model;

import powerband.constants.Constants;
import powerband.exception.PowerBandException;

/**
 * Qué se le pide al synth. PLAY: piano y bajo (suenan solos durMs).
 * NOTE_ON / NOTE_OFF: órgano (suena hasta el noteOff, que se graba como comando propio).
 */
public enum Action {
    PLAY((byte) 0), NOTE_ON((byte) 1), NOTE_OFF((byte) 2);

    private final byte code;

    Action(byte code) {
        this.code = code;
    }

    public byte code() {
        return code;
    }

    public static Action fromCode(byte code) throws PowerBandException {
        for (Action action : values()) {
            if (action.code == code) {
                return action;
            }
        }
        throw new PowerBandException(Constants.MSG_CORRUPT);
    }
}
