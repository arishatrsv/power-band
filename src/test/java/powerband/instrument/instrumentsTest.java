package powerband.instrument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import powerband.command.CommandBus;
import powerband.command.SynthCommand;
import powerband.constants.Constants;
import powerband.model.Action;
import powerband.model.Params;
import powerband.model.SynthType;

/** Los 3 instrumentos SIN hardware: se les entregan datos "como si" vinieran del dispositivo. */
class InstrumentsTest {

    private final CommandBus bus = new CommandBus();
    private final List<Params> emitted = Collections.synchronizedList(new ArrayList<>());

    InstrumentsTest() {
        bus.subscribe((command, params) -> emitted.add(params));
    }

    // -------------------------------------------------------------- audífonos
    private static double[] tone(double hz, float rate, int length) {
        double[] block = new double[length];
        for (int i = 0; i < length; i++) {
            block[i] = 0.5 * Math.sin(2 * Math.PI * hz * i / rate);
        }
        return block;
    }

    @Test
    void pitchDetectorFindsTonesInTheVoiceRange() {
        float rate = 16_000f;
        for (double hz : new double[] {82.41, 110.0, 164.81, 220.0, 329.63, 440.0}) {
            OptionalDouble found = PitchDetector.detect(tone(hz, rate, 1024), rate);
            assertTrue(found.isPresent(), "no detectó " + hz);
            assertEquals(hz, found.getAsDouble(), hz * 0.02, "tono " + hz);
        }
        assertEquals(45, PitchDetector.hzToMidi(110.0));
    }

    @Test
    void silenceAndNoiseHaveNoPitch() {
        float rate = 16_000f;
        assertTrue(PitchDetector.detect(new double[1024], rate).isEmpty());
        Random random = new Random(1);
        double[] noise = new double[1024];
        for (int i = 0; i < noise.length; i++) {
            noise[i] = random.nextDouble() - 0.5;
        }
        assertTrue(PitchDetector.detect(noise, rate).isEmpty());
    }

    @Test
    void oneHummedNoteFiresExactlyOneBassCommand() {
        HeadphoneInstrument mic = new HeadphoneInstrument(bus, SynthCommand.of(SynthType.BASS));
        float rate = 16_000f;
        double[] quiet = new double[1024];
        double[] loud = tone(110.0, rate, 1024);
        for (double[] block : new double[][] {quiet, quiet, loud, loud, loud, loud, quiet, quiet}) {
            mic.onBlock(block, rate);
        }
        assertEquals(1, emitted.size(), "un ataque = una nota");
        assertEquals(45, emitted.get(0).note(), "110 Hz = La2 = MIDI 45");
        assertEquals(Constants.BASS_NOTE_MS, emitted.get(0).durMs());
    }

    @Test
    void anyNoteFoldsIntoTheBassRange() {
        for (int note = 0; note <= 127; note++) {
            int folded = HeadphoneInstrument.foldIntoBassRange(note);
            assertTrue(folded >= Constants.BASS_MIN_NOTE && folded <= Constants.BASS_MAX_NOTE);
            assertEquals(0, Math.floorMod(folded - note, Constants.SEMITONES), "misma nota, otra octava");
        }
    }

    // ---------------------------------------------------------------- control
    @Test
    void gamepadFiresOnPressNotWhileHeld() {
        GamepadInstrument pad = new GamepadInstrument(bus, SynthCommand.of(SynthType.PIANO));
        int first = Constants.GAMEPAD_NOTE_BUTTONS[0];
        pad.handle(Set.of(first), 0);
        pad.handle(Set.of(first), 0);      // sigue presionado
        pad.handle(Set.of(), 0);             // suelta
        pad.handle(Set.of(first), 0);      // vuelve a presionar
        assertEquals(2, emitted.size());
        assertEquals(Constants.GAMEPAD_BASE_NOTE, emitted.get(0).note());
    }

    @Test
    void gamepadMinorButtonAndOctaveStick() {
        GamepadInstrument pad = new GamepadInstrument(bus, SynthCommand.of(SynthType.PIANO));
        int second = Constants.GAMEPAD_NOTE_BUTTONS[1];
        pad.handle(Set.of(second, Constants.GAMEPAD_MINOR_BUTTON), 0);
        pad.handle(Set.of(), 0);
        pad.handle(Set.of(second), -1f);   // stick arriba = una octava más alta
        int expected = Constants.GAMEPAD_BASE_NOTE + Constants.SCALE[1];
        assertEquals(Constants.THIRD_MINOR, emitted.get(0).extra());
        assertEquals(expected, emitted.get(0).note());
        assertEquals(Constants.THIRD_MAJOR, emitted.get(1).extra());
        assertEquals(expected + Constants.SEMITONES, emitted.get(1).note());
    }

    // ---------------------------------------------------------------- celular
    @Test
    void phoneTouchTurnsOrganOnAndReleaseTurnsTheSameNoteOff() {
        PhoneInstrument phone = new PhoneInstrument(bus, SynthCommand.of(SynthType.ORGAN));
        phone.handle("-9.0;0.0;0.0;1");   // inclinado a la izquierda = nota más grave
        phone.handle("9.0;0.0;0.0;1");    // sigue tocando: NO dispara otra nota
        phone.handle("9.0;0.0;0.0;0");    // suelta
        assertEquals(2, emitted.size());
        assertEquals(Action.NOTE_ON, emitted.get(0).action());
        assertEquals(Action.NOTE_OFF, emitted.get(1).action());
        assertEquals(emitted.get(0).note(), emitted.get(1).note());
        assertEquals(Constants.ORGAN_BASE_NOTE, emitted.get(0).note());
    }

    @Test
    void phoneTiltChoosesNoteAndTimbreAndBadMessagesAreRejected() {
        PhoneInstrument phone = new PhoneInstrument(bus, SynthCommand.of(SynthType.ORGAN));
        phone.handle("9.81;-9.81;0;1");   // todo a un extremo
        int last = Constants.ORGAN_SCALE.length - 1;
        assertEquals(Constants.ORGAN_BASE_NOTE + Constants.ORGAN_SCALE[last], emitted.get(0).note());
        assertEquals(0, emitted.get(0).extra(), "timbre 0");
        assertThrows(IllegalArgumentException.class, () -> phone.handle("1;2;3"));
        assertThrows(NumberFormatException.class, () -> phone.handle("a;b;c;1"));
    }
}
