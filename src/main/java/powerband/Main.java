package powerband;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import powerband.command.CommandBus;
import powerband.command.SynthCommand;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.instrument.GamepadInstrument;
import powerband.instrument.HeadphoneInstrument;
import powerband.instrument.Instrument;
import powerband.instrument.PhoneInstrument;
import powerband.model.SynthType;
import powerband.repository.Recordings;
import powerband.service.Player;
import powerband.service.Recorder;
import powerband.synth.OscClient;
import powerband.ui.Menu;

//Arranca y se conecta con SuperCollider, se activan los instrumentos y se entra
//al editor musical.
public final class Main {
    private Main() {
    }

    //Se conserva porque MainTest (del esqueleto) lo usa
    public static String startupMessage() {
        return Constants.STARTUP_MESSAGE;
    }

    public static void main(String[] args) {
        System.out.println(startupMessage());
        Recordings storage = new Recordings(Path.of(Constants.DATA_DIR));
        CommandBus bus = new CommandBus();
        AtomicBoolean monitor = new AtomicBoolean(true);

        //Al salir se cierra el socket y se apagan las notas del órgano
        try (OscClient osc = new OscClient()) {
            Recorder recorder = new Recorder(storage);
            bus.subscribe((command, params) -> command.execute(osc, params)); //Sonido
            bus.subscribe((command, params) -> { //monitor
                if (monitor.get()) {
                    System.out.printf(Constants.MONITOR_LINE, command.type(), params.action(), params.note(), params.durMs());
                }
            });
            bus.subscribe(recorder); //grabador

            //Bridge: Main es el único que decide qué dispositivo toca qué synth
            //(cada instrumento recibe el SynthCommand en su constructor).
            List<Supplier<Instrument>> instruments = List.of(
                    () -> new GamepadInstrument(bus, SynthCommand.of(SynthType.PIANO)),
                    () -> new HeadphoneInstrument(bus, SynthCommand.of(SynthType.BASS)),
                    () -> new PhoneInstrument(bus, SynthCommand.of(SynthType.ORGAN)));
            List<Instrument> active = startAll(instruments);

            try {
                Player player = new Player(storage, osc);
                new Menu(recorder, player, storage, monitor).run(); //editor musical

                player.stop(); //Detener cualquier reproducción que quede
                recorder.discard(); //Si se sale grabando, no queda un encabezado de grabación sin datos
            } finally {
                active.forEach(Instrument::stop); //Se liberan control, micrófono y puerto UDP
            }
        } catch (PowerBandException e) {
            System.err.println(Constants.ERROR + e.getMessage());
        }
    }

    //Activa cada instrumento. Si uno falla (control desconectado, micrófono ocupado, puerto en uso,
    //librería faltante) se avisa y el programa sigue con los demás.
    private static List<Instrument> startAll(List<Supplier<Instrument>> instruments) {
        List<Instrument> active = new ArrayList<>();
        for (Supplier<Instrument> create : instruments) {
            try {
                Instrument instrument = create.get();
                instrument.start();
                active.add(instrument);
                System.out.println(Constants.STARTED + instrument.name());
            } catch (PowerBandException | RuntimeException | LinkageError e) {
                System.out.println(Constants.FAILED + e.getMessage());
            }
        }
        return active;
    }
}
