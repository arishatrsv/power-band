package powerband.repository;

import java.nio.ByteBuffer;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.Action;
import powerband.model.CommandRecord;
import powerband.model.Header;
import powerband.model.NameEntry;
import powerband.model.Params;
import powerband.model.SynthType;

//Clase de utilidades para convertir entre registros y bytes
public final class Records {
    private Records(){//No se puede instanciar
    } 

    //Convierte un arreglo de bytes en un registro de encabezado
    //Comando 48 bytes
    public static byte[] toBytes(CommandRecord c) {
        ByteBuffer b = ByteBuffer.allocate(Constants.RECORD_SIZE);
        b.put(Constants.TYPE_COMMAND).put(c.synth().code()).put(c.params().action().code()).put(Constants.PAD);
        b.putLong(c.timeMs());
        b.putInt(c.params().note()).putInt(c.params().durMs()).putInt(c.params().extra());
        return b.array();
    }

    public static CommandRecord toCommand(byte[] data) throws PowerBandException{
        ByteBuffer b = ByteBuffer.wrap(data);
        if (b.get() != Constants.TYPE_COMMAND) {
            throw new PowerBandException(Constants.MSG_CORRUPT);
        }
        SynthType synth = SynthType.fromCode(b.get());
        Action action = Action.fromCode(b.get());
        b.get();                                            // relleno
        long time = b.getLong();
        return new CommandRecord(time, synth, new Params(action, b.getInt(), b.getInt(), b.getInt()));
    }

    //Encabezado 48 bytes
    public static byte[] toBytes(Header h) {
        ByteBuffer b = ByteBuffer.allocate(Constants.RECORD_SIZE);
        b.put(Constants.TYPE_HEADER);
        b.position(Constants.TYPE_FIELD_BYTES);             // salta el tipo y su relleno
        putName(b, h.name());
        b.putLong(h.start()).putLong(h.end()).putInt(h.count());
        return b.array();
    }

    public static Header toHeader(byte[] data) throws PowerBandException {
        ByteBuffer b = ByteBuffer.wrap(data);
        if (b.get() != Constants.TYPE_HEADER) {
            throw new PowerBandException(Constants.MSG_CORRUPT);
        }
        b.position(Constants.TYPE_FIELD_BYTES);
        return new Header(readName(b), b.getLong(), b.getLong(), b.getInt());
    }

    //índice y lista 32 bytes
    public static byte[] toBytes(NameEntry e) {
        ByteBuffer b = ByteBuffer.allocate(Constants.ENTRY_SIZE);
        putName(b, e.name());
        b.putLong(e.value());
        return b.array();
    }

    public static NameEntry toNameEntry(byte[] data) {
        ByteBuffer b = ByteBuffer.wrap(data);
        return new NameEntry(readName(b), b.getLong());
    }

    //Nombres de tamaño fijo
    //Siempre se escriben NAME_CHARS caracteres se rellena con espacios
    private static void putName(ByteBuffer b, String name) {
        for (int i = 0; i < Constants.NAME_CHARS; i++) {
            b.putChar(i < name.length() ? name.charAt(i) : Constants.NAME_PAD);
        }
    }

    private static String readName(ByteBuffer b) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < Constants.NAME_CHARS; i++) {
            name.append(b.getChar());
        }
        return name.toString().stripTrailing();
    }

    //Quita espacios de los lados y exige de 1 a NAME_CHARS caracteres
    public static String validateName(String raw) throws PowerBandException {
        String name = raw == null ? "" : raw.strip(); //Quita espacios de los lados
        if (name.isEmpty() || name.length() > Constants.NAME_CHARS) { //si está vacío o largo, lanza excepción
            throw new PowerBandException(Constants.MSG_INVALID_NAME + raw); //Mensaje de error
        }
        return name;
    }
}
