package powerband.instrument;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import powerband.constants.Constants;

/**
 * TEMPORAL (se borra en la etapa 5). Herramienta para calibrar el control: lista los dispositivos
 * que ve JInput y luego imprime cada componente que se mueve o presiona, con su número.
 */
public final class GamepadCalibration {

    private GamepadCalibration() {
    }

    public static void main(String[] args) throws InterruptedException {
        Controller pad = null;
        for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
            System.out.printf(Constants.CALIBRATION_LINE, c.getType(), c.getName());
            if (pad == null && (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK)) {
                pad = c;
            }
        }
        while (pad != null && pad.poll()) {
            for (Component c : pad.getComponents()) {
                if (Math.abs(c.getPollData()) > Constants.BUTTON_ON) {
                    System.out.printf(Constants.CALIBRATION_LINE, c.getIdentifier().getName(), c.getPollData());
                }
            }
            Thread.sleep(Constants.GAMEPAD_POLL_MS * 10);
        }
    }
}
