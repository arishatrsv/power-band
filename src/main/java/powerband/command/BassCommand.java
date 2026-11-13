package powerband.command;

import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.Params;
import powerband.model.SynthType;
import powerband.synth.OscClient;

//stringBass: una nota pulsada de durMs milisegundos
public final class BassCommand implements SynthCommand {

    @Override
    public SynthType type() {
        return SynthType.BASS;
    }

    @Override
    public void execute(OscClient osc, Params p) throws PowerBandException {
        osc.play(Constants.BASS_DEF,
                Constants.CTRL_NOTE, p.note(),
                Constants.CTRL_DUR, p.durMs());
    }
}
