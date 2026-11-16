package powerband.instrument;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.OptionalDouble;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import powerband.command.CommandBus;
import powerband.command.SynthCommand;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.Params;

/**
 * Audífonos - bajo. Se tararea una nota en el micrófono de los audífonos:
 *   - cada vez que el volumen sube después de silencio dispara una nota de bajo
 *   - el tono de la voz decide la nota (se pliega por octavas al rango del bajo).
 * Usa javax.sound.sampled (viene con Java). Elegir el micrófono: -Dpowerband.mic=parte-del-nombre
 */
public final class HeadphoneInstrument extends Instrument {

    private TargetDataLine line; // Línea de entrada que entrega el audio del micrófono
    private float sampleRate; // Frecuencia elegida para leer y analizar las muestras
    private Thread thread; // Hilo dedicado a capturar audio sin bloquear la aplicación
    private boolean silent = true; // Indica si el bloque anterior estaba en silencio
    private int loudBlocks; // Cantidad de bloques consecutivos que superan el umbral de sonido

    // Configura el instrumento como fuente de notas de bajo.
    public HeadphoneInstrument(CommandBus bus, SynthCommand command) {
        super(Constants.NAME_MIC, command, bus);
    }

    @Override
    protected void open() throws PowerBandException {
        // Prueba las frecuencias soportadas hasta encontrar una que acepte el micrófono.
        for (float rate : Constants.MIC_RATES) { // los micrófonos Bluetooth suelen dar 16 kHz
            try {
                line = openLine(rate);
                sampleRate = rate;
                line.start();
                thread = new Thread(this::capture);
                thread.setDaemon(true);
                thread.start();
                return;
            } catch (LineUnavailableException | IllegalArgumentException e) {
                line = null; // se prueba la siguiente frecuencia
            }
        }
        throw new PowerBandException(Constants.MSG_MIC);
    }

    // Abre el micrófono solicitado o, si no se especificó uno, la entrada predeterminada.
    private TargetDataLine openLine(float rate) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(rate, Constants.MIC_BITS, 1, true, false); // mono, con signo, little-endian
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        String wanted = System.getProperty(Constants.MIC_PROPERTY, "").toLowerCase();
        TargetDataLine opened = null;
        if (!wanted.isEmpty()) {
            // Busca un dispositivo cuyo nombre coincida con el filtro configurado.
            for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
                Mixer mixer = AudioSystem.getMixer(mixerInfo);
                if (mixerInfo.getName().toLowerCase().contains(wanted) && mixer.isLineSupported(info)) {
                    opened = (TargetDataLine) mixer.getLine(info);
                    break;
                }
            }
        }
        if (opened == null) {
            opened = (TargetDataLine) AudioSystem.getLine(info);// micrófono por defecto
        }
        opened.open(format); // Reserva la entrada con el formato de audio seleccionado
        return opened;
    }

    // Lee bloques de audio mientras el micrófono esté abierto y los entrega al detector de ataques.
    private void capture() {
        int samples = (int) (sampleRate * Constants.MIC_BLOCK_MS / 1000);
        byte[] bytes = new byte[samples * Constants.MIC_BITS / Byte.SIZE];
        while (line.isOpen()) {
            int read = line.read(bytes, 0, bytes.length);// bloquea hasta llenar el bloque
            if (read <= 0) {
                return;
            }
            // Interpreta cada par de bytes como una muestra PCM de 16 bits little-endian.
            var shorts = ByteBuffer.wrap(bytes, 0, read).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
            double[] block = new double[shorts.remaining()];
            for (int i = 0; i < block.length; i++) {
                block[i] = shorts.get(i) / Constants.SAMPLE_MAX;// normaliza a -1..1
            }
            onBlock(block, sampleRate);
        }
    }

    // Detecta un nuevo sonido, espera a que se estabilice y emite una nota de bajo.
    void onBlock(double[] block, float rate) {
        if (rms(block) < Constants.ONSET_RMS) {
            silent = true;                  // volvió el silencio: el próximo sonido será un ataque nuevo
            loudBlocks = 0;
            return;
        }
        if (silent) {
            silent = false;
            loudBlocks = 0;
        }
        loudBlocks++;
        if (loudBlocks == Constants.STABLE_BLOCKS) {          // se espera un poco a que el tono se estabilice
            OptionalDouble hz = PitchDetector.detect(block, rate);
            if (hz.isPresent()) {
                // Convierte el tono medido a nota MIDI y la ajusta al registro del bajo.
                emit(Params.bass(foldIntoBassRange(PitchDetector.hzToMidi(hz.getAsDouble())), Constants.BASS_NOTE_MS));
            }
        }
    }

    // Sube o baja la nota por octavas hasta ubicarla dentro del rango del bajo.
    static int foldIntoBassRange(int note) {
        int folded = note;
        while (folded > Constants.BASS_MAX_NOTE) {
            folded -= Constants.SEMITONES;
        }
        while (folded < Constants.BASS_MIN_NOTE) {
            folded += Constants.SEMITONES;
        }
        return folded;
    }

    // Calcula el RMS del bloque como medida de su volumen promedio.
    static double rms(double[] x) {
        double sum = 0;
        for (double v : x) {
            sum += v * v;
        }
        return Math.sqrt(sum / x.length);
    }

    @Override
    protected void close() {
        if (line != null) {
            line.stop(); // Detiene la captura antes de liberar la entrada
            line.close(); // el read() del hilo termina y el hilo sale
        }
    }
}
