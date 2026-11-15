package powerband;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import powerband.command.CommandBus;
import powerband.command.SynthCommand;
import powerband.service.Player;
import powerband.service.Recorder;
import powerband.constants.Constants;
import powerband.exception.DuplicateNameException;
import powerband.exception.PowerBandException;
import powerband.model.Params;
import powerband.model.SynthType;
import powerband.repository.Recordings;
import powerband.synth.OscClient;

/** Grabar -> guardar -> indexar -> cargar -> reproducir idéntico. Usa un OscClient falso (sin SuperCollider). */
class RecordingFlowTest {

    private static final long TOLERANCE_MS = 60;

    /** OscClient que en vez de mandar UDP anota qué se pidió y cuándo. */
    private static final class FakeOsc extends OscClient {
        record Call(String path, Object first, long nanos) {
        }

        final List<Call> calls = Collections.synchronizedList(new ArrayList<>());

        FakeOsc() throws PowerBandException {
            super();
        }

        @Override
        protected void send(String path, Object... args) {
            calls.add(new Call(path, args[0], System.nanoTime()));
        }
    }

    private final CommandBus bus = new CommandBus();

    private void emit(SynthType type, Params params) {
        bus.publish(SynthCommand.of(type), params);
    }

    private static void waitUntilFinished(Player player) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5_000;
        while (player.isPlaying() && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        assertFalse(player.isPlaying(), "la reproducción no terminó");
    }

    @Test
    void playbackKeepsTheRecordedIntervalsEvenForASecondRecording(@TempDir Path dir) throws Exception {
        Recordings storage = new Recordings(dir);
        Recorder recorder = new Recorder(storage);
        bus.subscribe(recorder);

        recorder.start();                                   // grabación de relleno: la 2.ª NO empieza en el byte 0
        emit(SynthType.BASS, Params.bass(36, 100));
        recorder.stop();
        recorder.save("primera");

        recorder.start();
        emit(SynthType.PIANO, Params.piano(60, 500, true));
        Thread.sleep(200);
        emit(SynthType.BASS, Params.bass(36, 300));
        Thread.sleep(300);
        emit(SynthType.ORGAN, Params.organOn(64, 1));
        Thread.sleep(100);
        emit(SynthType.ORGAN, Params.organOff(64));
        recorder.stop();
        recorder.save("Segunda");

        FakeOsc osc = new FakeOsc();
        Player player = new Player(storage, osc);
        player.play("segunda");                              // sin importar mayúsculas
        waitUntilFinished(player);

        assertEquals(4, osc.calls.size());
        assertEquals(Constants.PIANO_DEF, osc.calls.get(0).first());
        assertEquals(Constants.OSC_SET, osc.calls.get(3).path(), "el noteOff apaga el órgano");
        long[] expectedGaps = {200, 300, 100};
        for (int i = 0; i < expectedGaps.length; i++) {
            long gap = (osc.calls.get(i + 1).nanos() - osc.calls.get(i).nanos()) / Constants.NANOS_PER_MILLI;
            assertTrue(Math.abs(gap - expectedGaps[i]) <= TOLERANCE_MS,
                    "intervalo " + i + ": esperado " + expectedGaps[i] + " ms, fue " + gap);
        }
    }

    @Test
    void duplicateNamesAreRejectedAndTheRecordingIsKept(@TempDir Path dir) throws Exception {
        Recordings storage = new Recordings(dir);
        Recorder recorder = new Recorder(storage);
        bus.subscribe(recorder);

        recorder.start();
        emit(SynthType.BASS, Params.bass(36, 100));
        recorder.stop();
        recorder.save("rock");

        recorder.start();
        emit(SynthType.BASS, Params.bass(40, 100));
        recorder.stop();
        assertThrows(DuplicateNameException.class, () -> recorder.save("ROCK"));
        assertThrows(PowerBandException.class, () -> recorder.save("   "));
        assertTrue(recorder.isPendingSave(), "sigue esperando un nombre válido");
        recorder.save("jazz");

        assertEquals(2, storage.listCount());
        assertEquals("rock", storage.listAt(0).name());
        assertTrue(storage.find("jazz").isPresent());
    }

    @Test
    void discardLeavesNoTraceAndCommandsAreIgnoredWhenNotRecording(@TempDir Path dir) throws Exception {
        Recordings storage = new Recordings(dir);
        Recorder recorder = new Recorder(storage);
        bus.subscribe(recorder);

        emit(SynthType.BASS, Params.bass(30, 100));            // no se está grabando: se ignora
        assertFalse(Files.exists(dir.resolve(Constants.MUSIC_FILE)));

        recorder.start();
        emit(SynthType.BASS, Params.bass(36, 100));
        recorder.stop();
        recorder.save("keep");
        long length = Files.size(dir.resolve(Constants.MUSIC_FILE));
        assertEquals(2L * Constants.RECORD_SIZE, length, "encabezado + 1 comando");

        recorder.start();
        emit(SynthType.BASS, Params.bass(40, 100));
        emit(SynthType.BASS, Params.bass(41, 100));
        recorder.stop();
        recorder.discard();
        assertEquals(length, Files.size(dir.resolve(Constants.MUSIC_FILE)));
        assertEquals(1, storage.listCount());
    }

    @Test
    void invalidStatesAndUnknownNamesAreRejected(@TempDir Path dir) throws Exception {
        Recordings storage = new Recordings(dir);
        Recorder recorder = new Recorder(storage);
        assertThrows(PowerBandException.class, recorder::stop);
        assertThrows(PowerBandException.class, () -> recorder.save("x"));
        assertThrows(PowerBandException.class, () -> new Player(storage, new FakeOsc()).play("nope"));
    }

    @Test
    void cannotPlayTwiceAtOnce(@TempDir Path dir) throws Exception {
        Recordings storage = new Recordings(dir);
        Recorder recorder = new Recorder(storage);
        bus.subscribe(recorder);
        recorder.start();
        emit(SynthType.BASS, Params.bass(36, 100));
        Thread.sleep(400);
        emit(SynthType.BASS, Params.bass(38, 100));
        recorder.stop();
        recorder.save("long");

        Player player = new Player(storage, new FakeOsc());
        player.play("long");
        assertThrows(PowerBandException.class, () -> player.play("long"));
        player.stop();
        waitUntilFinished(player);
    }
}
