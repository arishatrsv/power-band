package powerband.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.CommandRecord;
import powerband.model.Header;
import powerband.model.NameEntry;
import powerband.model.Params;
import powerband.model.SynthType;
import powerband.sort.IndexSorter;

/** Registros de tamaño fijo, búsqueda binaria en disco y Balanced Merge Sort externo. */
class StorageTest {

    private static final int[] SIZES = {0, 1, 2, 3, 4, 5, 7, 8, 9, 100, 257};
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Test
    void everyRecordHasTheFixedSizeAndRoundTrips() throws Exception {
        CommandRecord command = new CommandRecord(98_765L, SynthType.ORGAN, Params.organOn(67, 2));
        assertEquals(Constants.RECORD_SIZE, Records.toBytes(command).length);
        assertEquals(command, Records.toCommand(Records.toBytes(command)));

        CommandRecord piano = new CommandRecord(5, SynthType.PIANO, Params.piano(60, 1500, false));
        assertEquals(piano, Records.toCommand(Records.toBytes(piano)));

        Header header = new Header("Mi cancion", 96, 4096, 62);
        assertEquals(Constants.RECORD_SIZE, Records.toBytes(header).length);
        assertEquals(header, Records.toHeader(Records.toBytes(header)));

        NameEntry entry = new NameEntry("Rock", 640);
        assertEquals(Constants.ENTRY_SIZE, Records.toBytes(entry).length);
        assertEquals(entry, Records.toNameEntry(Records.toBytes(entry)));
    }

    @Test
    void readingTheWrongKindOfRecordMeansCorruption() {
        byte[] commandBytes = Records.toBytes(new CommandRecord(0, SynthType.BASS, Params.bass(36, 100)));
        assertThrows(PowerBandException.class, () -> Records.toHeader(commandBytes));
    }

    @Test
    void nameValidation() throws Exception {
        assertEquals("Rock", Records.validateName("  Rock "));
        assertThrows(PowerBandException.class, () -> Records.validateName("   "));
        assertThrows(PowerBandException.class, () -> Records.validateName("x".repeat(Constants.NAME_CHARS + 1)));
        assertThrows(PowerBandException.class, () -> Records.validateName(null));
    }

    private static String randomName(Random random) {
        StringBuilder name = new StringBuilder();
        int length = 1 + random.nextInt(Constants.NAME_CHARS);
        for (int i = 0; i < length; i++) {
            name.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }
        return name.toString();
    }

    @Test
    void mergeSortOrdersFilesOfManySizesOnDisk(@TempDir Path dir) throws Exception {
        Random random = new Random(42);
        for (int size : SIZES) {
            Path file = dir.resolve("index-" + size);
            List<NameEntry> original = new ArrayList<>();
            try (RecordFile out = new RecordFile(file, Constants.ENTRY_SIZE)) {
                for (int i = 0; i < size; i++) {
                    NameEntry entry = new NameEntry(randomName(random), i * 48L);
                    original.add(entry);
                    out.append(Records.toBytes(entry));
                }
            }
            long lengthBefore = Files.size(file);

            IndexSorter.sort(file, dir.resolve("tmp"));

            assertEquals(lengthBefore, Files.size(file), "el tamaño no cambia (n=" + size + ")");
            List<NameEntry> expected = new ArrayList<>(original);
            expected.sort(IndexSorter.BY_NAME);       // solo para COMPARAR en la prueba
            try (RecordFile in = new RecordFile(file, Constants.ENTRY_SIZE)) {
                for (int i = 0; i < size; i++) {
                    assertEquals(expected.get(i), Records.toNameEntry(in.read(i)), "posición " + i + " (n=" + size + ")");
                }
            }
        }
        assertFalse(Files.exists(dir.resolve("tmp").resolve(Constants.SORT_TEMPS[0])), "temporales borrados");
    }

    @Test
    void binarySearchFindsEveryNameAndRejectsMissingOnes(@TempDir Path dir) throws Exception {
        Recordings storage = new Recordings(dir);
        String[] names = {"zebra", "Mango", "alpha", "kiwi", "Banana", "omega", "delta", "echo", "Charlie"};
        for (int i = 0; i < names.length; i++) {
            storage.register(names[i], i * 48L);      // register agrega a lista e índice y reordena
        }
        for (int i = 0; i < names.length; i++) {
            assertEquals(i * 48L, storage.find(names[i]).orElseThrow().value());
        }
        assertEquals(3 * 48L, storage.find("KIWI").orElseThrow().value(), "no distingue mayúsculas");
        assertFalse(storage.find("nope").isPresent());
        assertFalse(storage.find("aaa").isPresent());
        assertFalse(storage.find("zzzz").isPresent());
        assertEquals(names.length, storage.listCount());
        assertEquals("zebra", storage.listAt(0).name(), "la lista queda en orden cronológico");
        assertTrue(storage.listAt(1).value() >= storage.listAt(0).value());
    }
}
