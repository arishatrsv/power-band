package powerband.instrument;

import java.util.OptionalDouble;
import powerband.constants.Constants;

/**
 * Detecta el tono (Hz) de un bloque de audio por autocorrelación.
 * un sonido con tono se repite cada periodo. Si se compara la señal consigo misma
 * desplazada L muestras, el parecido es máximo cuando L = el periodo. Entonces f = fs / L.
 * correlación(L) = suma(x[i] * x[i+L]) / raíz(energía1 * energía2)  (entre -1 y 1)
 */
public final class PitchDetector {

    private PitchDetector() {
    }

    // Busca el periodo dominante del bloque y devuelve la frecuencia fundamental si es confiable.
    public static OptionalDouble detect(double[] x, float sampleRate) {
        // Convierte el rango de frecuencias admitido a un rango de periodos medidos en muestras.
        int minLag = (int) Math.floor(sampleRate / Constants.PITCH_MAX_HZ);
        int maxLag = Math.min((int) Math.ceil(sampleRate / Constants.PITCH_MIN_HZ), x.length - 2);
        double[] corr = new double[maxLag + 2];
        double best = 0;
        // Calcula la correlación para cada periodo candidato y conserva el mejor parecido.
        for (int lag = minLag; lag <= maxLag; lag++) {
            corr[lag] = correlation(x, lag);
            best = Math.max(best, corr[lag]);
        }
        if (best < Constants.PITCH_MIN_CORRELATION) {
            return OptionalDouble.empty();//silencio, ruido o voz hablada: no hay tono
        }
        // Se toma el lag mas corto que sea un pico fuerte, si no, un múltiplo del periodo
        // daría una nota una octava más grave de lo real
        for (int lag = minLag + 1; lag <= maxLag; lag++) {
            boolean peak = corr[lag] >= corr[lag - 1] && corr[lag] >= corr[lag + 1];
            if (peak && corr[lag] >= best * Constants.PITCH_PEAK_TOLERANCE) {
                return OptionalDouble.of(sampleRate / lag);
            }
        }
        return OptionalDouble.empty();
    }

    // Compara el bloque con una copia desplazada y normaliza el resultado entre -1 y 1.
    private static double correlation(double[] x, int lag) {
        double sum = 0;
        double energyA = 0;
        double energyB = 0;
        for (int i = 0; i + lag < x.length; i++) {
            sum += x[i] * x[i + lag];
            energyA += x[i] * x[i];
            energyB += x[i + lag] * x[i + lag];
        }
        double norm = Math.sqrt(energyA * energyB);
        return norm == 0 ? 0 : sum / norm;
    }

    // Convierte una frecuencia en Hz a la nota MIDI más cercana usando La4 como referencia.
    public static int hzToMidi(double hz) {
        double semitones = Constants.SEMITONES * Math.log(hz / Constants.HZ_A4) / Math.log(2.0);
        return (int) Math.round(Constants.MIDI_A4 + semitones);
    }
}
