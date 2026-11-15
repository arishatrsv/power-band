package powerband;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import powerband.command.CommandBus;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.repository.Recordings;
import powerband.service.Player;
import powerband.service.Recorder;
import powerband.synth.OscClient;
import powerband.ui.Menu;

//Arranca y se conecta con SuperCollider, se activan los instrumentos y se entra
//al editor musical .
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
        try (OscClient osc = new OscClient()){
            Recorder recorder = new Recorder(storage);
            bus.subscribe((command, params) -> command.execute(osc, params)); //Sonido 
            bus.subscribe((command, params) -> { //monitor
                if (monitor.get()){
                    System.out.printf(Constants.MONITOR_LINE, command.type(), params.action(), params.note(), params.durMs());
                }
            });
            bus.subscribe(recorder); //grabador

            Player player = new Player(storage, osc);
            new Menu(recorder, player, storage, monitor).run(); //editor musical 

            player.stop(); //Detener cualquier reproducción que quede
            recorder.discard(); //Si se sale grabando, no queda un encabezado de grabación sin datos
        }catch (PowerBandException e) {
            System.err.println(Constants.ERROR + e.getMessage());
        }
    }
}
