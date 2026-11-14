package powerband.command;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import powerband.exception.PowerBandException;
import powerband.model.Params;

/**
 * un solo punto donde pasan los comandos (patrón Observer) ***
 *
 * El editor conoce en todo momento los comandos que los instrumentos envían a SuperCollider.
 * Para garantizarlo, ningún instrumento le habla a SuperCollider directo: todos publican aquí
 * y se suscriben: quien hace sonar, el monitor y el grabador.
 *
 * CopyOnWriteArrayList: varios hilos publican a la vez y es segura para eso.
 */
public final class CommandBus {

    //Cualquiera que quiera enterarse de los comandos
    public interface Listener {
        void onCommand(SynthCommand command, Params params) throws PowerBandException;
    }

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(Listener listener) {
        listeners.add(listener);
    }

    //i un suscriptor falla, los demás igual reciben el comando
    public void publish(SynthCommand command, Params params) {
        for (Listener listener : listeners) {
            try {
                listener.onCommand(command, params);
            } catch (PowerBandException | RuntimeException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
