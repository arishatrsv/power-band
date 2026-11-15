package powerband.service;

import powerband.command.CommandBus;
import powerband.command.SynthCommand;
import powerband.constants.Constants;
import powerband.exception.DuplicateNameException;
import powerband.exception.PowerBandException;
import powerband.model.CommandRecord;
import powerband.model.Header;
import powerband.model.Params;
import powerband.repository.RecordFile;
import powerband.repository.Records;
import powerband.repository.Recordings;



public class Recorder implements CommandBus.Listener {
    private final Recordings storage;
    private RecordFile music;   //abierto mientras haya una grabación en curso o pendiente de nombre
    private boolean capturing;
    private long headerOffset;
    private long startNanos;
    private int count;

    public Recorder(Recordings storage) {
        this.storage = storage;
    }

    public synchronized void start() throws PowerBandException { //Inicia la grabación
        if (music != null) { //ya hay una grabacion en curso
            throw new PowerBandException(Constants.MSG_ALREADY_RECORDING); //ya se está grabando algo
        }
        music = storage.openMusic(); //Se abre el archivo de música para grabar
        headerOffset = music.length(); //Posición del encabezado en el archivo
        music.append(Records.toBytes(Header.placeholder(headerOffset + Constants.RECORD_SIZE))); //Se escribe un encabezado temporal
        count = 0; //Se reinicia contador
        startNanos = System.nanoTime(); //Se guarda el tiempo de inicio
        capturing = true; //Se activa la bandera de grabación
    }

    @Override
    public synchronized void onCommand(SynthCommand command, Params params) throws PowerBandException { //Se llama cada vez que se recibe un comando de síntesis
        if (!capturing) { //Si no se está grabando, se ignora el comando
            return;
        }
        //Tiempo relativo al inicio de la grabación.
        long ms = (System.nanoTime() - startNanos) / Constants.NANOS_PER_MILLI; //Se calcula el tiempo transcurrido desde el inicio de la grabación
        music.append(Records.toBytes(new CommandRecord(ms, command.type(), params))); //Se escribe el comando en el archivo de música
        count++;
    }

    public synchronized void stop() throws PowerBandException {
        if (!capturing) {
            throw new PowerBandException(Constants.MSG_NOT_RECORDING);
        }
        capturing = false;
    }

    //Si el nombre es inválido o repetido lanza excepción y la grabación SIGUE pendiente: se puede reintentar.
    public synchronized void save(String rawName) throws PowerBandException {
        if (!isPendingSave()) {  //Si no hay grabación pendiente de nombre
            throw new PowerBandException(Constants.MSG_NOTHING_TO_SAVE); //No hay grabación pendiente de nombre
        }
        String name = Records.validateName(rawName); //Se valida el nombre
        if (storage.exists(name)) { //si ya existe una grabación con ese nombre
            throw new DuplicateNameException(name); //Se informa que ya existe una grabación con ese nombre
        }
        Header header = new Header(name, headerOffset + Constants.RECORD_SIZE, music.length(), count); //Se construye el encabezado definitivo con el nombre, las posiciones y la cantidad de comandos
        music.write(headerOffset / Constants.RECORD_SIZE, Records.toBytes(header)); //Se reemplaza el encabezado temporal por el definitivo en el archivo
        music.close(); //Se cierra el archivo de música después de guardar el encabezado
        music = null; //Se libera la referencia al archivo para indicar que ya no hay nada pendiente de guardar
        storage.register(name, headerOffset); //Se registra el nombre y la posición inicial de la grabación en el almacenamiento
    }

    public synchronized void discard() throws PowerBandException { //Se descarta la grabación en curso
        if (music != null) { //Si hay una grabación en curso o pendiente de nombre
            capturing = false; //se desactiva la bandera de grabación
            music.truncate(headerOffset); //Se elimina la grabación en curso
            music.close(); //Se cierra el archivo de música
            music = null; //Se libera la referencia al archivo de música
        }
    }

    //Indica si el grabador está capturando comandos actualmente. 
    public synchronized boolean isCapturing() {
        return capturing; //Devuelve el estado actual de la captura
    }

    /** Indica si hay una grabación detenida que todavía espera ser guardada o descartada. */
    public synchronized boolean isPendingSave() {
        return music != null && !capturing; //Devuelve true si hay una grabación detenida pendiente de guardar
    }
}
