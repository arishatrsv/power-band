package powerband.model;

import powerband.constants.Constants;
import powerband.exception.PowerBandException;

/** Los tres synths. El "code" (1 byte) es lo que se guarda en recordings.music. */
public enum SynthType {
    PIANO((byte) 1), BASS((byte) 2), ORGAN((byte) 3);

    private final byte code;

    SynthType(byte code) {
        this.code = code;
    }

    public byte code() {
        return code;
    }

    public static SynthType fromCode(byte code) throws PowerBandException {
        for (SynthType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new PowerBandException(Constants.MSG_CORRUPT);
    }
}
