package powerband.instrument;

import java.util.HashMap;
import java.util.Map;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import powerband.constants.Constants;

//Herramienta de diagnóstico para calibrar el control. Lista TODOS los dispositivos que ve JInput
//(con su tipo y cuántos botones tienen), elige el mismo que usará GamepadInstrument y luego imprime
//cada botón o eje que cambia, con su identificador. Ese identificador es el número que va en Constants.
public final class GamepadCalibration {

    private GamepadCalibration() {
    }

    public static void main(String[] args) throws InterruptedException {
        Controller[] all = GamepadInstrument.listControllers();
        for (Controller c : all) {
            System.out.printf(Constants.CALIBRATION_DEVICE, c.getType(), c.getName(),
                    c.getComponents().length, GamepadInstrument.countButtons(c));
        }
        Controller pad = GamepadInstrument.pick(all);
        if (pad == null) {
            System.out.println(Constants.CALIBRATION_NONE);
            return;
        }
        System.out.printf(Constants.CALIBRATION_PICKED, pad.getName());
        System.out.println(Constants.CALIBRATION_WAIT);
        Map<String, Float> last = new HashMap<>();
        // Imprime solo cuando un componente cambia de valor (evita llenar la consola mientras se mantiene).
        while (pad.poll()) {
            for (Component c : pad.getComponents()) {
                String id = c.getIdentifier().getName();
                float value = c.getPollData();
                Float old = last.put(id, value);
                boolean useful = id.chars().allMatch(Character::isDigit) || id.equals(Constants.STICK_Y_AXIS);
                if (useful && Math.abs(value) > Constants.BUTTON_ON && (old == null || Math.abs(old - value) > Constants.BUTTON_ON)) {
                    System.out.printf(Constants.CALIBRATION_EVENT, id, c.getName(), value);
                }
            }
            Thread.sleep(Constants.GAMEPAD_POLL_MS);
        }
    }
}
