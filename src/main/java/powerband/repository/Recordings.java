package powerband.repository;

import java.nio.file.Path;
import java.util.Optional;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.Header;
import powerband.model.NameEntry;
import powerband.sort.IndexSorter;

//Los tres archivos de las grabaciones:
//   recordings.music  encabezados + comandos 
//   recordings.index  ordenado por nombre (32 bytes)
//   recordings.list   nombre, fecha/hora, en orden cronológico 
//Cada archivo se abre solo mientras se necesita.
public class Recordings {
    private final Path dir;

    public Recordings(Path dir){
        this.dir = dir;
    }

    private Path index() {
        return dir.resolve(Constants.INDEX_FILE);
    }

    public RecordFile openMusic() throws PowerBandException{
        return new RecordFile(dir.resolve(Constants.MUSIC_FILE), Constants.RECORD_SIZE);
    }

    //Búsqueda binaria en el disco. 
    public Optional<NameEntry> find(String name) throws PowerBandException {
        try (RecordFile file = new RecordFile(index(), Constants.ENTRY_SIZE)) { //Abre el archivo de índice, que tiene registros de tamaño fijo
            long low = 0;
            long high = file.count() - 1; //El último registro es count() - 1
            while (low <= high) {
                long middle = (low + high) >>> 1; //Divide entre 2 sin riesgo de overflow
                NameEntry entry = Records.toNameEntry(file.read(middle)); //Lee el registro en la posición middle
                int comparison = IndexSorter.BY_NAME.compare(entry, new NameEntry(name, 0)); //Compara el registro con el nombre buscado
                if (comparison == 0) {
                    return Optional.of(entry); //Si son iguales, devuelve el registro
                }
                if (comparison < 0) { //Si el registro es menor que el nombre buscado, busca en la mitad superior
                    low = middle + 1; //está en la mitad de arriba
                } else {
                    high = middle - 1; //está en la mitad de abajo
                }
            }
            return Optional.empty(); 
        }
    }

    //Verifica si un nombre ya existe en el índice
    public boolean exists(String name) throws PowerBandException {
        return find(name).isPresent();
    }

    //Registra una grabación nueva: agrega a la lista y al índice, y reordena el índice.
    public void register(String name, long headerOffset) throws PowerBandException {
        //Abre el archivo de lista, que tiene registros de tamaño fijo
        try (RecordFile list = new RecordFile(dir.resolve(Constants.LIST_FILE), Constants.ENTRY_SIZE)) { 
            list.append(Records.toBytes(new NameEntry(name, System.currentTimeMillis()))); //Agrega a la lista de grabaciones, en orden cronológico
        }
        //Abre el archivo de índice, que tiene registros de tamaño fijo
        try (RecordFile file = new RecordFile(index(), Constants.ENTRY_SIZE)) { 
            file.append(Records.toBytes(new NameEntry(name, headerOffset))); //Agrega al índice, en orden cronológico
        }                                    // el índice se cierra ANTES de ordenarlo
        IndexSorter.sort(index(), dir.resolve(Constants.TEMP_DIR)); //Ordena el índice por nombre, usando un directorio temporal
    }

    //Devuelve la cantidad de grabaciones registradas, leyendo el archivo de lista
    public long listCount() throws PowerBandException {
        //Abre el archivo de lista, que tiene registros de tamaño fijo
        try (RecordFile list = new RecordFile(dir.resolve(Constants.LIST_FILE), Constants.ENTRY_SIZE)) {
            return list.count(); //cantidad de registros en archivo de lista
        }
    }

    //La música número position en la lista de grabaciones, en orden cronológico
    public NameEntry listAt(long position) throws PowerBandException {
        //Abre el archivo de lista, que tiene registros de tamaño fijo
        try (RecordFile list = new RecordFile(dir.resolve(Constants.LIST_FILE), Constants.ENTRY_SIZE)) {
            return Records.toNameEntry(list.read(position)); //Lee el registro en la posicion 
        }
    }

    //Lee el encabezado de la música en la posición offset del archivo de música
    public Header readHeader(long offset) throws PowerBandException {
        try (RecordFile music = openMusic()) { //abre el archivo de música
            return Records.toHeader(music.read(offset / Constants.RECORD_SIZE)); //Lee el registro en la posicion
        }
    }
    
}
