package powerband.sort;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.NameEntry;
import powerband.repository.Records;

//Para ordenar los indices de los registros de un archivo
//Se puede leer el archivo de registros, obtener los indices y ordenarlos
//Luego escribirlos en un nuevo archivo de indices
public class IndexSorter {
    //Para ordenar y para buscar, sin distinguir mayúsculas
    public static final Comparator<NameEntry> BY_NAME = 
        (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.name(), b.name());

    private IndexSorter() {
    }

    public static void sort(Path file, Path tmpDir) throws PowerBandException {
        Path[] t = new Path[Constants.SORT_TEMPS.length];
        for (int i = 0; i < t.length; i++) {
            t[i] = tmpDir.resolve(Constants.SORT_TEMPS[i]);
        }
        try {
            Files.createDirectories(tmpDir);
            long total = distribute(file, t[0], t[1]);
            if (total <= 1) {
                return;  //0 o 1 registros: ya está ordenado
            }
            Path in1 = t[0], in2 = t[1], out1 = t[2], out2 = t[3];
            for (long run = 1; run < total; run *= 2) {
                merge(in1, in2, out1, out2, run);
                Path s1 = in1, s2 = in2;  // intercambio de roles
                in1 = out1;
                in2 = out2;
                out1 = s1;
                out2 = s2;
            }
            Files.move(in1, file, StandardCopyOption.REPLACE_EXISTING);  //todo quedó en in1
        } catch (IOException e) {
            throw new PowerBandException(Constants.MSG_STORAGE, e);
        } finally {
            for (Path temp : t) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // limpieza de temporales
                }
            }
        }
    }

    //Paso1: Devuelve cuántos registros había
    private static long distribute(Path source, Path a, Path b) throws IOException, PowerBandException {
        long total = Files.size(source) / Constants.ENTRY_SIZE;
        try (Run in = new Run(source); OutputStream outA = out(a); OutputStream outB = out(b)) {
            boolean toA = true;
            while (in.hasMore()) {
                in.startRun(1);
                (toA ? outA : outB).write(Records.toBytes(in.take()));
                toA = !toA;
            }
        }
        return total;
    }

    //Paso2: Mezcla dos archivos de entrada en dos archivos de salida, alternando los runs
    private static void merge(Path in1, Path in2, Path out1, Path out2, long runLength)
            throws IOException, PowerBandException {
        try (Run r1 = new Run(in1); Run r2 = new Run(in2); OutputStream o1 = out(out1); OutputStream o2 = out(out2)) {
            boolean toFirst = true;
            while (r1.hasMore() || r2.hasMore()) {
                OutputStream out = toFirst ? o1 : o2;
                r1.startRun(runLength);
                r2.startRun(runLength);
                while (r1.runHasNext() || r2.runHasNext()) {          // siempre sale el menor de los dos del frente
                    boolean takeFirst = !r2.runHasNext()
                            || (r1.runHasNext() && BY_NAME.compare(r1.peek(), r2.peek()) <= 0);
                    out.write(Records.toBytes(takeFirst ? r1.take() : r2.take()));
                }
                toFirst = !toFirst;
            }
        }
    }

    //Abre un archivo de salida con buffer
    private static OutputStream out(Path path) throws IOException {
        return new BufferedOutputStream(Files.newOutputStream(path));
    }

    //Lee un archivo secuencialmente
    private static final class Run implements AutoCloseable {
        private final InputStream in;
        private long left;   //registros que faltan por leer del archivo
        private NameEntry head; // próximo registro (null si ya no hay)
        private long runLeft;   //cuántos registros más puede dar la corrida actual

        Run(Path path) throws IOException {
            this.in = new BufferedInputStream(Files.newInputStream(path));
            this.left = Files.size(path) / Constants.ENTRY_SIZE;
            advance();
        }

        private void advance() throws IOException {
            if (left > 0) {
                head = Records.toNameEntry(in.readNBytes(Constants.ENTRY_SIZE));
                left--;
            } else {
                head = null;
            }
        }

        boolean hasMore() {
            return head != null;
        }

        void startRun(long length) {
            runLeft = length;
        }

        boolean runHasNext() {
            return runLeft > 0 && head != null;
        }

        NameEntry peek() {
            return head;
        }

        NameEntry take() throws IOException {
            NameEntry current = head;
            runLeft--;
            advance();
            return current;
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

}
