package powerband.instrument;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import powerband.constants.Constants;

/**
 * Herramienta temporal de diagnóstico para calibrar el control. Lista los dispositivos
 * que ve JInput y luego imprime cada componente que se mueve o presiona, con su identificador.
 */
public final class GamepadCalibration {

    private GamepadCalibration() {
    }

    // Muestra los controles detectados y monitorea el primero que sea gamepad o joystick.
    public static void main(String[] args) throws InterruptedException {
        Controller pad = null;
        // Imprime el tipo y nombre de cada dispositivo, y conserva el primer control utilizable.
        for (Controller c : ControllerEnvironment.getDefaultEnvironment().getControllers()) {
            System.out.printf(Constants.CALIBRATION_LINE, c.getType(), c.getName());
            if (pad == null && (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK)) {
                pad = c;
            }
        }
        // Sigue leyendo hasta que el control se desconecte o deje de responder.
        while (pad != null && pad.poll()) {
            // Informa los componentes activos para identificar botones y ejes en la configuracion.
            for (Component c : pad.getComponents()) {
                if (Math.abs(c.getPollData()) > Constants.BUTTON_ON) {
                    System.out.printf(Constants.CALIBRATION_LINE, c.getIdentifier().getName(), c.getPollData());
                }
            }
            Thread.sleep(Constants.GAMEPAD_POLL_MS * 10);
        }
    }
}
