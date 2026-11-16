package powerband.instrument;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import powerband.command.CommandBus;
import powerband.command.SynthCommand;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.Params;

/**
 * El celular manda por UDP un texto "x;y;z;toque". Este instrumento es también el servidor que
 * pide el enunciado para los instrumentos externos: escucha el puerto en su propio hilo.
 *   - Tocar la pantalla = noteOn; soltar = noteOff de la misma nota.
 *   - Inclinar izquierda/derecha (x) elige la nota; adelante/atrás (y) elige el timbre.
 * UDP no tiene conexión: cada mensaje es un datagrama suelto. Sirve aquí porque un dato de
 * sensor viejo no vale la pena reenviarlo.
 */
public final class PhoneInstrument extends Instrument {

    private DatagramSocket socket;
    private boolean touching;
    private int sounding;// nota que está encendida (válida si touching)

    public PhoneInstrument(CommandBus bus, SynthCommand command) {
        super(Constants.NAME_PHONE, command, bus);
    }

    @Override
    protected void open() throws PowerBandException {
        try {
            socket = new DatagramSocket(Constants.PHONE_PORT);
        } catch (SocketException e) {
            throw new PowerBandException(Constants.MSG_PORT + Constants.PHONE_PORT, e);
        }
        Thread thread = new Thread(this::listen);
        thread.setDaemon(true);
        thread.start();
    }

    private void listen() {
        byte[] buffer = new byte[Constants.PHONE_PACKET_BYTES];
        while (!socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);// espera un mensaje
                handle(new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8));
            } catch (IOException | RuntimeException e) {// un mensaje malo no debe tumbar el servidor
                if (!socket.isClosed()) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    //Procesa un mensaje "x;y;z;toque". Lo llama un solo hilo (el del servidor)
    void handle(String text) {
        String[] f = text.strip().split(Constants.PHONE_SEPARATOR);
        if (f.length != Constants.PHONE_FIELDS) {
            throw new IllegalArgumentException(Constants.MSG_BAD_PHONE + text);
        }
        float x = Float.parseFloat(f[0]);
        float y = Float.parseFloat(f[1]);
        boolean down = Constants.PHONE_TOUCH.equals(f[3]);
        if (down && !touching) {
            sounding = Constants.ORGAN_BASE_NOTE + Constants.ORGAN_SCALE[bucket(x, Constants.ORGAN_SCALE.length)];
            emit(Params.organOn(sounding, bucket(y, Constants.ORGAN_TIMBRES.length)));
        } else if (!down && touching) {
            emit(Params.organOff(sounding));
        }
        touching = down;
    }

    //Reparte el rango -G..+G en "count" tramos y devuelve en cuál cae el valor (0..count-1)
    private static int bucket(float value, int count) {
        float fraction = (value + Constants.GRAVITY) / (2 * Constants.GRAVITY);
        return Math.max(0, Math.min(count - 1, (int) (fraction * count)));
    }

    @Override
    protected void close() {
        socket.close(); // despierta el receive() y el hilo termina
    }
}
