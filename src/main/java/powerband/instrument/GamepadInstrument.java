package powerband.instrument;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import powerband.command.CommandBus;
import powerband.command.SynthCommand;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.Params;

/**
 * control-piano. (lee el control con la librería JInput).
 *   - Cada botón de nota toca el acorde de ese grado de la escala.
 *   - Mantener el botón "menor" (L1) vuelve el acorde menor.
 *   - Stick arriba/abajo = una octava más alta / más baja.
 * Solo dispara cuando el botón pasa de suelto a presionado (detección de flanco);
 * si no, mantenerlo repetiría el acorde 100 veces por segundo.
 * JInput ve el control como "componentes": botones "0", "1", "2"... y ejes "x", "y", "z"...
 */
public final class GamepadInstrument extends Instrument {

    private Controller controller; // Control físico seleccionado para leer sus componentes
    private ScheduledExecutorService scheduler; // Programa lecturas periódicas del control
    private Set<Integer> before = Set.of(); // Botones presionados en la lectura anterior

    // Configura el instrumento como controlador de notas de piano.
    public GamepadInstrument(CommandBus bus, SynthCommand command) {
        super(Constants.NAME_GAMEPAD, command, bus);
    }

    @Override
    protected void open() throws PowerBandException {
        // Usa el primer dispositivo reconocido como gamepad o joystick.
        for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
            if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                controller = c;
                scheduler = Executors.newSingleThreadScheduledExecutor();
                scheduler.scheduleAtFixedRate(this::poll, 0, Constants.GAMEPAD_POLL_MS, TimeUnit.MILLISECONDS);
                return;
            }
        }
        throw new PowerBandException(Constants.MSG_GAMEPAD_NONE);
    }

    // Lee el estado del control y evita que una excepción detenga las lecturas programadas.
    private void poll() {
        try {
            if (!controller.poll()) {// false = el control se desconectó
                System.err.println(Constants.MSG_GAMEPAD_NONE);
                scheduler.shutdown();
                return;
            }
            Set<Integer> pressed = new HashSet<>();
            float stickY = 0;
            // Separa el eje vertical de los botones numéricos reportados por JInput.
            for (Component c : controller.getComponents()) {
                String name = c.getIdentifier().getName();
                if (name.equals(Constants.STICK_Y_AXIS)) {
                    stickY = c.getPollData();
                } else if (name.chars().allMatch(Character::isDigit) && c.getPollData() > Constants.BUTTON_ON) {
                    pressed.add(Integer.parseInt(name));
                }
            }
            handle(pressed, stickY);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }

    // Interpreta botones y eje vertical; solo emite cuando una nota acaba de presionarse.
    void handle(Set<Integer> pressed, float stickY) {
        // El eje modifica la octava solo cuando supera el umbral configurado.
        int octave = stickY < -Constants.STICK_THRESHOLD ? Constants.SEMITONES
                : stickY > Constants.STICK_THRESHOLD ? -Constants.SEMITONES : 0;
        for (int i = 0; i < Constants.GAMEPAD_NOTE_BUTTONS.length; i++) {
            int button = Constants.GAMEPAD_NOTE_BUTTONS[i];
            if (pressed.contains(button) && !before.contains(button)) {
                int note = Constants.GAMEPAD_BASE_NOTE + Constants.SCALE[i] + octave;
                boolean major = !pressed.contains(Constants.GAMEPAD_MINOR_BUTTON);
                // El botón menor cambia la calidad del acorde antes de publicarlo en el bus.
                emit(Params.piano(note, Constants.GAMEPAD_NOTE_MS, major));
            }
        }
        before = new HashSet<>(pressed); // Guarda el estado actual para detectar el siguiente flanco
    }

    @Override
    protected void close() {
        scheduler.shutdownNow(); // Detiene las lecturas periódicas pendientes
        controller = null; // Descarta la referencia al dispositivo seleccionado
    }
}
