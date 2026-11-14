package powerband.synth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;

/**
 * Habla con SuperCollider (scsynth) por UDP usando el protocolo OSC. Es BandClient.java
 * del profesor, ordenado y sin números quemados.
 *
 * Es synchronized porque tres instrumentos (tres hilos) lo usan a la vez y comparten
 * el contador de ids y el mapa de notas de órgano encendidas.
 */
public class OscClient implements AutoCloseable {

    private final DatagramSocket socket;
    private final InetAddress address;
    private final int port;
    private final Map<Integer, Integer> sustained = new HashMap<>();   // nota -> id del synth en scsynth
    private int nextNodeId = Constants.OSC_FIRST_NODE;

    public OscClient() throws PowerBandException {
        this(Constants.OSC_HOST, Constants.OSC_PORT);
    }

    public OscClient(String host, int port) throws PowerBandException {
        try {
            this.socket = new DatagramSocket();
            this.address = InetAddress.getByName(host);
        } catch (IOException e) {
            throw new PowerBandException(Constants.MSG_OSC, e);
        }
        this.port = port;
    }

    /** Synth que se apaga solo (piano, bajo). "controls" son pares nombre, valor. */
    public synchronized void play(String synthDef, Object... controls) throws PowerBandException {
        newNode(synthDef, controls);
    }

    /** Synth que suena hasta stopNote con la misma "key" (órgano). */
    public synchronized void startNote(int key, String synthDef, Object... controls) throws PowerBandException {
        stopNote(key);                         // si esa nota ya sonaba, se reinicia
        sustained.put(key, newNode(synthDef, controls));
    }

    /** Apaga el synth de esa nota (gate = 0). */
    public synchronized void stopNote(int key) throws PowerBandException {
        Integer node = sustained.remove(key);
        if (node != null) {
            send(Constants.OSC_SET, node, Constants.CTRL_GATE, Constants.GATE_CLOSED);
        }
    }

    public synchronized void stopAll() throws PowerBandException {
        for (Integer key : sustained.keySet().toArray(new Integer[0])) {   // copia: stopNote modifica el mapa
            stopNote(key);
        }
    }

    private int newNode(String synthDef, Object[] controls) throws PowerBandException {
        int node = nextNodeId++;
        Object[] args = new Object[4 + controls.length];
        args[0] = synthDef;
        args[1] = node;
        args[2] = Constants.OSC_ADD_TO_HEAD;
        args[3] = Constants.OSC_GROUP;
        System.arraycopy(controls, 0, args, 4, controls.length);
        send(Constants.OSC_NEW, args);
        return node;
    }

    /** Codifica y envía un mensaje OSC. Es protected para poder reemplazarlo en las pruebas. */
    protected void send(String path, Object... args) throws PowerBandException {
        byte[] data = encode(path, args);
        try {
            socket.send(new DatagramPacket(data, data.length, address, port));
        } catch (IOException e) {
            throw new PowerBandException(Constants.MSG_OSC, e);
        }
    }

    /**
     * Formato OSC:  ruta | "type tags" (ej. ",siif") | argumentos.
     * Los strings terminan en byte nulo y se rellenan a múltiplo de 4; int y float van en big endian.
     */
    static byte[] encode(String path, Object... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeString(out, path);
        StringBuilder tags = new StringBuilder(Constants.OSC_TAGS_PREFIX);
        for (Object arg : args) {
            tags.append(arg instanceof Integer ? Constants.OSC_TAG_INT
                    : arg instanceof Float ? Constants.OSC_TAG_FLOAT : Constants.OSC_TAG_STRING);
        }
        writeString(out, tags.toString());
        for (Object arg : args) {
            if (arg instanceof Integer value) {
                out.writeBytes(ByteBuffer.allocate(Integer.BYTES).putInt(value).array());
            } else if (arg instanceof Float value) {
                out.writeBytes(ByteBuffer.allocate(Float.BYTES).putFloat(value).array());
            } else {
                writeString(out, (String) arg);
            }
        }
        return out.toByteArray();
    }

    private static void writeString(ByteArrayOutputStream out, String text) {
        byte[] bytes = text.getBytes(StandardCharsets.US_ASCII);
        out.writeBytes(bytes);
        int padding = Constants.OSC_ALIGN - bytes.length % Constants.OSC_ALIGN;   // siempre al menos 1 nulo
        out.writeBytes(new byte[padding]);
    }

    @Override
    public synchronized void close() {
        try {
            stopAll();                        // que no quede ningún órgano sonando
        } catch (PowerBandException ignored) {
            // al cerrar no hay nada útil que hacer
        }
        socket.close();
    }
}
