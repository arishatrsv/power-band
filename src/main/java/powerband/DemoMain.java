package powerband;

import powerband.command.CommandBus;
import powerband.command.SynthCommand;
import powerband.model.Params;
import powerband.model.SynthType;
import powerband.synth.OscClient;

/**
 * PROGRAMA TEMPORAL: oír los 3 synths desde Java pasando por el bus (sin instrumentos todavía).
 * Antes: SuperCollider abierto y ~myband.load; ejecutado. Se puede borrar al final.
 */
public final class DemoMain {

    private static final long PAUSE_MS = 1_200;

    private DemoMain() {
    }

    public static void main(String[] args) throws Exception {
        CommandBus bus = new CommandBus();
        try (OscClient osc = new OscClient()) {
            bus.subscribe((command, params) -> command.execute(osc, params));
            bus.subscribe((command, params) -> System.out.println(command.type() + " " + params));

            bus.publish(SynthCommand.of(SynthType.PIANO), Params.piano(48, 1_000, true));
            Thread.sleep(PAUSE_MS);
            bus.publish(SynthCommand.of(SynthType.BASS), Params.bass(36, 600));
            Thread.sleep(PAUSE_MS);
            bus.publish(SynthCommand.of(SynthType.ORGAN), Params.organOn(60, 0));
            Thread.sleep(PAUSE_MS);
            bus.publish(SynthCommand.of(SynthType.ORGAN), Params.organOff(60));
            Thread.sleep(PAUSE_MS);
        }
    }
}
