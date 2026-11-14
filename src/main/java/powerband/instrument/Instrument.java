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

    private final String name;
    private final SynthCommand command;
    private final CommandBus bus;
    private boolean running;

    protected Instrument(String name, SynthCommand command, CommandBus bus) {
        this.name = name;
        this.command = command;
        this.bus = bus;
    }

    public final synchronized void start() throws PowerBandException {
        if (!running) {
            open();
            running = true;
        }
    }

    public final synchronized void stop() {
        if (running) {
            running = false;
            close();
        }
    }

    /** Abre el dispositivo y empieza a escuchar. */
    protected abstract void open() throws PowerBandException;

    protected abstract void close();

    /** Los hijos llaman esto cuando el dispositivo produce algo: el comando viaja por el bus. */
    protected final void emit(Params params) {
        bus.publish(command, params);
    }

    public String name() {
        return name;
    }
}
