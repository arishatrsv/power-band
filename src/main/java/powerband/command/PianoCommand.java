package powerband.command;

import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.Params;
import powerband.model.SynthType;
import powerband.synth.OscClient;

//classicPiano: un acorde de 4 notas que suena durMs y se apaga solo
public final class PianoCommand implements SynthCommand {

    @Override
    public SynthType type() {
        return SynthType.PIANO;
    }

    @Override
    public void execute(OscClient osc, Params p) throws PowerBandException {
        osc.play(Constants.PIANO_DEF,
                Constants.CTRL_NOTE, p.note(),
                Constants.CTRL_DUR, p.durMs(),
                Constants.CTRL_THIRD, p.extra());
    }
}
