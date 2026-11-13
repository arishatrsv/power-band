package powerband.command;

import powerband.exception.PowerBandException;
import powerband.model.Params;
import powerband.model.SynthType;
import powerband.synth.OscClient;

/**
 * *** PATRÓN BRIDGE: ESTA ES LA "IMPLEMENTACIÓN" ***
 *
 * Dos jerarquías que cambian por separado:
 *   - Abstracción:    Instrument   (qué DISPOSITIVO toco)  -> paquete instrument
 *   - Implementación: SynthCommand (qué SYNTH suena)       -> este paquete
 * Un Instrument guarda un SynthCommand: ese es el "puente".
 * Synth nuevo = una clase nueva aquí, sin tocar ningún instrumento.
 * Dispositivo nuevo = un Instrument nuevo, sin tocar ningún comando.
 */
public interface SynthCommand {

    SynthType type();

    void execute(OscClient osc, Params params) throws PowerBandException;

    //Devuelve el comando de un tipo (lo usa el reproductor: del archivo sale un SynthType)
    static SynthCommand of(SynthType type) {
        return switch (type) {
            case PIANO -> new PianoCommand();
            case BASS -> new BassCommand();
            case ORGAN -> new OrganCommand();
        };
    }
}
