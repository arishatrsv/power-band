package powerband.repository;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;

//Un archivo de registros de tamaño fijo
//Es AutoCloseable: con try-with-resources el archivo se cierra aunque haya una excepción.
public final class RecordFile implements AutoCloseable {
    private final RandomAccessFile file;
    private final int size;

    public RecordFile(Path path, int recordSize) throws PowerBandException{ //throws sirve para que el que llama sepa que puede haber una excepción
        try{
            if(path.getParent() != null){
                Files.createDirectories(path.getParent()); //Crea el directorio si no existe
            }
            this.file = new RandomAccessFile(path.toFile(), Constants.FILE_MODE);
        } catch (IOException e){
            throw new PowerBandException(Constants.MSG_STORAGE, e);
        }
        this.size = recordSize;
    }

    public long length() throws PowerBandException{
        try{
            return file.length();
        } catch (IOException e){
            throw new PowerBandException(Constants.MSG_STORAGE, e);
        }
    }

    //Cantidad de registros.
    public long count() throws PowerBandException{
        if (length() % size != 0){ //Si el largo no es múltiplo del tamaño, el archivo está dañado
            throw new PowerBandException(Constants.MSG_CORRUPT);
        }
        return length()/ size;
    }

    public byte[] read(long index) throws PowerBandException{ //Lee el registro en la posicion index
        byte[] data = new byte[size]; //Crea un arreglo de bytes del tamaño del registro
        try{
            file.seek(index * size); //Mueve el puntero del archivo a la posición index * size
            file.readFully(data); //Lee size bytes del archivo y los guarda en data
            return data;
        }catch (IOException e){
            throw new PowerBandException(Constants.MSG_STORAGE, e);
        }
    }

    public void write(long index, byte[] data) throws PowerBandException{
        check(data);
        try {
            file.seek(index * size); //Mueve el puntero del archivo a la posición index * size
            file.write(data); //Escribe los bytes de data en el archivo
        } catch (IOException e) {
            throw new PowerBandException(Constants.MSG_STORAGE, e); 
        }
    }

    public void append(byte[] data) throws PowerBandException {
        write(length() / size, data);
    }

    //Corta el archivo, para descartar una grabacion
    public void truncate(long newLength) throws PowerBandException {
        try {
            file.setLength(newLength);
        } catch (IOException e) {
            throw new PowerBandException(Constants.MSG_STORAGE, e);
        }
    }

    //Garantía del tamaño fijo
    private void check(byte[] data) {
        if (data.length != size) {
            //Si el tamaño del arreglo de bytes no es igual al tamaño del registro, lanza una excepción
            throw new IllegalArgumentException(Constants.MSG_RECORD_SIZE + data.length);
        }
    }

    @Override 
    public void close() throws PowerBandException{ //Cierra el archivo
        try{
            file.close();
        }catch (IOException e){
            throw new PowerBandException(Constants.MSG_STORAGE, e);
        }
    }
}
