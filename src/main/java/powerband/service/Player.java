package powerband.service;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.LockSupport;
import powerband.command.SynthCommand;
import powerband.constants.Constants;
import powerband.exception.PowerBandException;
import powerband.model.CommandRecord;
import powerband.model.Header;
import powerband.model.NameEntry;
import powerband.repository.RecordFile;
import powerband.repository.Records;
import powerband.repository.Recordings;
import powerband.synth.OscClient;

//Reproduce una grabación por nombre
//1) Búsqueda binaria en el índice -> posición del encabezado -> se lee (inicio, fin).
//2) BUFFERING: un hilo LECTOR lee los comandos del disco y los pone en una cola.
//3) Un hilo REPRODUCTOR los saca y espera la hora exacta de cada uno.
public class Player {
    private static final CommandRecord END = new CommandRecord(-1, null, null);
    private final Recordings storage;
    private final OscClient osc;
    private Thread loader;
    private Thread player;
    private volatile boolean cancelled;

    public Player(Recordings storage, OscClient osc) {
        this.storage = storage;
        this.osc = osc;
    }
    
    //Reproduce en segundo plano
    public synchronized void play(String name) throws PowerBandException{
        if (isPlaying()){
            throw new PowerBandException(Constants.MSG_PLAYING); //Ya se está reproduciendo algo
        }
        NameEntry entry = storage.find(name) 
            .orElseThrow(() -> new PowerBandException(Constants.MSG_NOT_FOUND + name)); //No se encontró la grabación
        Header header = storage.readHeader(entry.value()); //Se lee el encabezado

        cancelled = false; //Se reinicia la bandera de cancelación
        loader = new Thread(() -> load(header, queue)); //Hilo Lector
        player = new Thread(() -> run(queue)); //Hilo Reproductor
        loader.setDaemon(true); //
        player.setDaemon(true); //Se detienen al cerrar la aplicación
        loader.start(); //Se inicia la lectura de comandos
        player.start(); //Se inicia la reproducción
    }

    public synchronized boolean isPlaying() {
        return player != null && player.isAlive();
    }

    public synchronized void stop() {
        cancelled = true;
        if (loader != null) {
            loader.interrupt();
        }
        if (player != null) {
            player.interrupt();
        }
    }

    //Hilo Lector (productor)
    private void load(Header header, BlockingQueue<CommandRecord> queue){
        try (RecordFile music = storage.openMusic()) {
            for (long offset = header.start(); offset < header.end(); offset += Constants.RECORD_SIZE) {
                queue.put(Records.toCommand(music.read(offset/ Constants.RECORD_SIZE)));
            }
        } catch (PowerBandException e){
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            return; //Se canceló: el reproductor también fue interrumpido
        }
        try {
            queue.put(END); //Avisa que no hay más (también si hubo un error de lectura)
        } catch (InterruptedException ignored) {
            // cancelado
        }
    }

    //Hilo Reproductor (consumidor)
    private void run(BlockingQueue<CommandRecord> queue) {
        try{
            CommandRecord record = queue.take();
            long base = System.nanoTime();  //Tiempo de inicio de la reproducción
            while (record != END && !cancelled) {
                waitUntil(base + record.timeMs() * Constants.NANOS_PER_MILLI);
                if (cancelled) {
                    break;
                }
                SynthCommand.of(record.synth()).execute(osc, record.params());
                record = queue.take();
            }
        } catch (InterruptedException e){
            //cancelado
        } catch (PowerBandException e) {
            System.err.println(e.getMessage());
        } finally {
            loader.interrupt();
            try {
                osc.stopAll(); // que no quede ningún sonido sonando
            } catch (PowerBandException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    //Espera hasta que se alcance el tiempo objetivo (en nanosegundos)
    private void waitUntil(long targetNanos){
        long remaining;
        while(!cancelled && (remaining = targetNanos - System.nanoTime()) > 0){
            LockSupport.parkNanos(remaining);
        }
    }
}
