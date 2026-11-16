package powerband.instrument;

import powerband.command.CommandBus;
import powerband.command.SynthCommand;
import powerband.exception.PowerBandException;
import powerband.model.Params;

/**
 * Patron bridge
 *
 * Un Instrument es un dispositivo físico que traduce lo que mide en parámetros musicales.
 * NO sabe qué synth suena: eso lo decide el SynthCommand que recibe (el "puente").
 *
 * start()/stop() son "template method": fijan el orden y cada hijo completa open()/close().
 */
public abstract class Instrument {

    private final String name; // Nombre visible que identifica el instrumento
    private final SynthCommand command; // Comando que traduce sus datos a una accion del sintetizador
    private final CommandBus bus; // Canal comun por el que se publican los comandos
    private boolean running; // Indica si el dispositivo ya fue abierto

    // Guarda los colaboradores necesarios para conectar el dispositivo con el bus de comandos.
    protected Instrument(String name, SynthCommand command, CommandBus bus) {
        this.name = name;
        this.command = command;
        this.bus = bus;
    }

    // Inicia el instrumento una sola vez y deja que cada subclase abra su dispositivo.
    public final synchronized void start() throws PowerBandException {
        if (!running) {
            open(); // Abre el dispositivo concreto; si falla, running permanece en false
            running = true; // Marca el instrumento como activo solo despues de abrirlo
        }
    }

    // Detiene el instrumento una sola vez y delega el cierre a su subclase.
    public final synchronized void stop() {
        if (running) {
            running = false; // Evita que otra llamada concurrente intente detenerlo de nuevo
            close(); // Libera el recurso concreto del dispositivo
        }
    }

    /** Abre el dispositivo y empieza a escuchar. */
    protected abstract void open() throws PowerBandException;

    /** Cierra el dispositivo y libera los recursos asociados. */
    protected abstract void close();

    /** Los hijos llaman esto cuando el dispositivo produce algo: el comando viaja por el bus. */
    protected final void emit(Params params) {
        bus.publish(command, params);
    }

    // Devuelve el nombre asignado al instrumento.
    public String name() {
        return name;
    }
}
