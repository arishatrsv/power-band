package powerband.constants;

public final class Constants {

    private Constants() {
    }

    // ===================== Tiempo =====================
    public static final long NANOS_PER_MILLI = 1_000_000L;

    // ===================== OSC / SuperCollider =====================
    public static final String OSC_HOST = "127.0.0.1";
    public static final int OSC_PORT = 57110;// puerto UDP de scsynth
    public static final String OSC_NEW = "/s_new";// crear un synth
    public static final String OSC_SET = "/n_set";// cambiar un control de un synth que suena
    public static final int OSC_ADD_TO_HEAD = 0;
    public static final int OSC_GROUP = 1;
    public static final int OSC_FIRST_NODE = 10_000;
    public static final int OSC_ALIGN = 4;// OSC: todo mide múltiplo de 4 bytes
    public static final String OSC_TAGS_PREFIX = ",";// los type tags empiezan con coma: ",ifs"
    public static final char OSC_TAG_INT = 'i';
    public static final char OSC_TAG_FLOAT = 'f';
    public static final char OSC_TAG_STRING = 's';

    // ===================== Synths (nombres de myband.sc) =====================
    public static final String PIANO_DEF = "classicPiano";
    public static final String BASS_DEF = "stringBass";
    public static final String ORGAN_DEF = "organ70s";
    public static final String CTRL_NOTE = "note";
    public static final String CTRL_DUR = "durMs";
    public static final String CTRL_THIRD = "third";
    public static final String CTRL_FREQ = "freq";
    public static final String CTRL_GATE = "gate";
    public static final String[] CTRL_ORGAN = {"r1", "r2", "r3", "a1", "a2", "a3"};
    public static final int GATE_CLOSED = 0;
    public static final int THIRD_MAJOR = 4;
    public static final int THIRD_MINOR = 3;
    public static final int MIDI_A4 = 69;
    public static final double HZ_A4 = 440.0;
    public static final int SEMITONES = 12;
    // Timbres del órgano: r1 r2 r3 (razones) y a1 a2 a3 (amplitudes). Se elige uno por índice
    public static final float[][] ORGAN_TIMBRES = {
        {1f, 2f, 3f, 1f, 0.5f, 0.25f},// cálido
        {1f, 2.01f, 4f, 1f, 0.6f, 0.3f},// medio
        {1f, 3f, 5f, 1f, 0.4f, 0.2f}};// brillante

    // ===================== Archivos =====================
    public static final String DATA_DIR = "data";
    public static final String TEMP_DIR = "tmp";
    public static final String MUSIC_FILE = "recordings.music";
    public static final String INDEX_FILE = "recordings.index";
    public static final String LIST_FILE = "recordings.list";
    public static final String FILE_MODE = "rw";// RandomAccessFile: leer y escribir (lo crea si no existe)
    public static final String[] SORT_TEMPS = {"sort-a.tmp", "sort-b.tmp", "sort-c.tmp", "sort-d.tmp"};

    //Nombre de una grabación: 12 caracteres (se rellena con espacios). Cada char ocupa 2 bytes
    public static final int NAME_CHARS = 12;
    public static final char NAME_PAD = ' ';
    public static final int NAME_BYTES = NAME_CHARS * Character.BYTES;

    /**
     * TODOS los registros de recordings.music miden lo mismo (48 bytes): encabezado y comandos.
     *   Encabezado: tipo+relleno(4) | nombre(24) | inicio(8) | fin(8) | cantidad(4)  = 48
     *   Comando:    tipo,synth,acción,relleno(4) | tiempo(8) | note,durMs,extra(12)  = 24 (+24 de ceros)
     */
    public static final int RECORD_SIZE = 48;
    public static final int TYPE_FIELD_BYTES = 4;
    public static final byte TYPE_HEADER = 0;
    public static final byte TYPE_COMMAND = 1;
    public static final byte PAD = 0;
    /** Registro de recordings.index y de recordings.list: nombre(24) + un long(8) = 32. */
    public static final int ENTRY_SIZE = NAME_BYTES + Long.BYTES;

    // ===================== Reproducción =====================
    public static final int BUFFER_SIZE = 64;// comandos que el hilo lector deja listos

    // ===================== Control PlayStation (calibrar con JInputGamepadReader.main) =====================
    public static final long GAMEPAD_POLL_MS = 10;
    public static final int[] GAMEPAD_NOTE_BUTTONS = {0, 1, 2, 3, 4, 5, 6, 7};
    public static final int GAMEPAD_MINOR_BUTTON = 10;// mantenido = acorde menor
    public static final int[] SCALE = {0, 2, 4, 5, 7, 9, 11, 12};// Do mayor
    public static final int GAMEPAD_BASE_NOTE = 48;
    public static final int GAMEPAD_NOTE_MS = 1_500;
    public static final float STICK_THRESHOLD = 0.5f;
    public static final String STICK_Y_AXIS = "y";
    public static final float BUTTON_ON = 0.5f;
    public static final String GAMEPAD_READER_CLASS = "powerband.instrument.JInputGamepadReader";

    // ===================== Celular (UDP desde la app Android) =====================
    public static final int PHONE_PORT = 9000;
    public static final int PHONE_PACKET_BYTES = 256;
    public static final String PHONE_SEPARATOR = ";";
    public static final int PHONE_FIELDS = 4; // x;y;z;toque
    public static final String PHONE_TOUCH = "1";
    public static final float GRAVITY = 9.81f;
    public static final int ORGAN_BASE_NOTE = 48;
    public static final int[] ORGAN_SCALE = {0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23, 24};

    // ===================== Audífonos (micrófono) =====================
    public static final float[] MIC_RATES = {16_000f, 44_100f};// se prueba en este orden
    public static final int MIC_BITS = 16;
    public static final int MIC_BLOCK_MS = 64;
    public static final double SAMPLE_MAX = 32_768.0;
    public static final String MIC_PROPERTY = "powerband.mic";// -Dpowerband.mic=parte-del-nombre
    public static final double PITCH_MIN_HZ = 70.0;
    public static final double PITCH_MAX_HZ = 800.0;
    public static final double PITCH_MIN_CORRELATION = 0.6;
    public static final double PITCH_PEAK_TOLERANCE = 0.9;
    public static final double ONSET_RMS = 0.02;// volumen mínimo para considerar "ataque"
    public static final int STABLE_BLOCKS = 2;
    public static final int BASS_MIN_NOTE = 28;
    public static final int BASS_MAX_NOTE = 55;
    public static final int BASS_NOTE_MS = 600;

    // ===================== Textos =====================
    public static final String MSG_DUPLICATE = "Ya existe una grabación con el nombre: ";
    public static final String MSG_INVALID_NAME = "Nombre inválido (de 1 a 12 caracteres): ";
    public static final String MSG_NOT_FOUND = "No existe la grabación: ";
    public static final String MSG_STORAGE = "Error de entrada/salida en los archivos";
    public static final String MSG_CORRUPT = "Archivo dañado o con formato inesperado";
    public static final String MSG_OSC = "No se pudo enviar el mensaje a SuperCollider";
    public static final String MSG_ALREADY_RECORDING = "Ya hay una grabación en curso o pendiente de nombre";
    public static final String MSG_NOT_RECORDING = "No se está grabando";
    public static final String MSG_NOTHING_TO_SAVE = "No hay ninguna grabación por guardar";
    public static final String MSG_BAD_ACTION = "Acción no válida para este synth: ";
    public static final String MSG_RECORD_SIZE = "Registro de tamaño distinto al esperado: ";
    public static final String MSG_PLAYING = "Ya hay una reproducción en curso";
    public static final String MSG_MIC = "No se pudo abrir el micrófono de los audífonos";
    public static final String MSG_GAMEPAD_LIB = "Falta JInput: el control no se activó";
    public static final String MSG_GAMEPAD_NONE = "No se encontró un control conectado";
    public static final String MSG_PORT = "No se pudo abrir el puerto UDP ";
    public static final String MSG_BAD_PHONE = "Mensaje del celular inválido: ";
    public static final String NAME_GAMEPAD = "Control PlayStation (piano)";
    public static final String NAME_PHONE = "Celular (órgano)";
    public static final String NAME_MIC = "Audífonos (bajo)";
    public static final String STARTED = "Instrumento activo: ";
    public static final String FAILED = "Instrumento NO disponible: ";
    public static final String ERROR = "Error: ";
    public static final String STARTUP_MESSAGE = "Power Band";
    public static final String CALIBRATION_HEADER = "Control: ";
    public static final String CALIBRATION_LINE = "%s = %s%n";
    public static final String MENU = "\n=== POWER BAND ===\n1) Iniciar grabación\n2) Detener grabación\n"
            + "3) Cargar y reproducir\n4) Detener reproducción\n5) Monitor on/off\n0) Salir\n> ";
    public static final String OPTION_START = "1";
    public static final String OPTION_STOP = "2";
    public static final String OPTION_PLAY = "3";
    public static final String OPTION_STOP_PLAY = "4";
    public static final String OPTION_MONITOR = "5";
    public static final String OPTION_EXIT = "0";
    public static final String ASK_NAME = "Nombre de la música (vacío = descartar): ";
    public static final String ASK_NUMBER = "Número de la música: ";
    public static final String SAVED = "Guardada.";
    public static final String DISCARDED = "Descartada.";
    public static final String NO_MUSIC = "Todavía no hay músicas grabadas.";
    public static final String BAD_OPTION = "Opción no válida.";
    public static final String LIST_LINE = "%d) %s  (%tF %<tT)%n";
    public static final String MONITOR_STATE = "Monitor activado: ";
    public static final String MONITOR_LINE = "  [%s] %s nota=%d dur=%d%n";
}
