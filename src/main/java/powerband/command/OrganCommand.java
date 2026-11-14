package powerband.command;

import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.Params;
import powerband.model.SynthType;
import powerband.synth.OscClient;

/**
 * organ70s: el único synth que NO se apaga solo. NOTE_ON lo enciende y NOTE_OFF lo apaga.
 * El comando no guarda estado (el OscClient recuerda qué synth está sonando en cada nota).
 */
public final class OrganCommand implements SynthCommand {

    @Override
    public SynthType type() {
        return SynthType.ORGAN;
    }

    @Override
    public void execute(OscClient osc, Params p) throws PowerBandException {
        switch (p.action()) {
            case NOTE_ON -> {
                float[] t = Constants.ORGAN_TIMBRES[p.extra()];
                Object[] controls = new Object[2 + 2 * t.length];
                controls[0] = Constants.CTRL_FREQ;
                controls[1] = midiToHz(p.note());
                for (int i = 0; i < t.length; i++) {          // r1 r2 r3 a1 a2 a3
                    controls[2 + 2 * i] = Constants.CTRL_ORGAN[i];
                    controls[3 + 2 * i] = t[i];
                }
                osc.startNote(p.note(), Constants.ORGAN_DEF, controls);
            }
            case NOTE_OFF -> osc.stopNote(p.note());
            default -> throw new PowerBandException(Constants.MSG_BAD_ACTION + p.action());
        }
    }

    //El órgano recibe frecuencia: f = 440 * 2^((nota - 69) / 12)
    static float midiToHz(int note) {
        return (float) (Constants.HZ_A4 * Math.pow(2.0, (note - Constants.MIDI_A4) / (double) Constants.SEMITONES));
    }
}
