package powerband.ui;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import powerband.constants.Constants;
import powerband.exception.DuplicateNameException;
import powerband.exception.PowerBandException;
import powerband.model.NameEntry;
import powerband.repository.Recordings;
import powerband.service.Player;
import powerband.service.Recorder;

//Menú de consola del editor musical
public final class Menu {
    private final Recorder recorder;
    private final Player player;
    private final Recordings storage;
    private final AtomicBoolean monitor;
    private final Scanner in = new Scanner(System.in);

    public Menu(Recorder recorder, Player player, Recordings storage, AtomicBoolean monitor) {
        this.recorder = recorder;
        this.player = player;
        this.storage = storage;
        this.monitor = monitor;
    }

    public void run() {
        boolean exit = false; // Inicializa el estado del menú como activo
        while (!exit) { // Repite el menú hasta que se seleccione salir
            System.out.print(Constants.MENU); // Muestra las opciones disponibles
            try { // Ejecuta la opción y captura errores propios de la aplicación
                switch (in.nextLine().strip()) { //Lee la opción ingresada y elimina espacios al inicio y al final
                    case Constants.OPTION_START -> recorder.start(); //Inicia la grabación de comandos
                    case Constants.OPTION_STOP -> stopAndSave(); //Detiene la grabación y solicita guardarla
                    case Constants.OPTION_PLAY -> loadAndPlay(); //Carga una grabación y la reproduce
                    case Constants.OPTION_STOP_PLAY -> player.stop(); //Detiene la reproducción actual
                    case Constants.OPTION_MONITOR -> toggleMonitor(); //Activa o desactiva el monitor de comandos
                    case Constants.OPTION_EXIT -> exit = true; //Marca que se debe terminar el ciclo del menú
                    default -> System.out.println(Constants.BAD_OPTION); //Informa que la opción no es válida
                }
            } catch (PowerBandException e) {
                System.out.println(Constants.ERROR + e.getMessage()); //Muestra el error sin cerrar el menú
            }
        }
    }

    private void toggleMonitor() {
        monitor.set(!monitor.get());
        System.out.println(Constants.MONITOR_STATE + monitor.get());
    }

    //Detiene la captura y pide un nombre hasta que sea válido y único
    private void stopAndSave() throws PowerBandException {
        recorder.stop(); //Detiene la captura de comandos
        while (recorder.isPendingSave()) { //Repite mientras haya una grabación pendiente de guardar
            System.out.print(Constants.ASK_NAME); //Solicita el nombre para la grabación
            String name = in.nextLine(); //Lee el nombre ingresado
            if (name.isBlank()) { //Comprueba si el usuario dejó el nombre vacío
                recorder.discard(); //Descarta la grabación pendiente
                System.out.println(Constants.DISCARDED); // Informa que se descartó la grabación
                return; //Termina este método sin guardar
            }
            try { //Intenta guardar la grabación con el nombre ingresado
                recorder.save(name); //Guarda la grabación
                System.out.println(Constants.SAVED); //Confirma que la grabación se guardó
            } catch (DuplicateNameException e) { //Maneja el caso en que el nombre ya esté registrado
                System.out.println(Constants.ERROR + e.getMessage()); //se vuelve a pedir otro nombre
            } catch (PowerBandException e) { //Maneja otros errores ocurridos durante el guardado
                if (!recorder.isPendingSave()) { //Comprueba si ya no es posible reintentar el guardado
                    throw e; //error grave, ya no se puede reintentar
                }
                System.out.println(Constants.ERROR + e.getMessage()); //nombre inválido: se reintenta
            }
        }
    }

    //Lista cronológica (recordings.list), el usuario elige y se reproduce por nombre.
    private void loadAndPlay() throws PowerBandException {
        long count = storage.listCount(); //Obtiene la cantidad de grabaciones guardadas
        if (count == 0) { //Comprueba si no hay grabaciones disponibles
            System.out.println(Constants.NO_MUSIC); //Informa que no hay música guardada
            return; //Termina el método porque no hay nada para listar o reproducir
        }
        for (long i = 0; i < count; i++) { // Recorre las posiciones de la lista de grabaciones
            NameEntry entry = storage.listAt(i); // Lee del disco la entrada correspondiente a esta posición
            System.out.printf(Constants.LIST_LINE, i + 1, entry.name(), entry.value()); // Muestra el número, nombre y dato de la grabación
        }
        System.out.print(Constants.ASK_NUMBER); // Solicita el número de la grabación que se reproducirá
        try { // Intenta convertir la respuesta del usuario en un número
            long chosen = Long.parseLong(in.nextLine().strip()); // Lee la respuesta, elimina espacios y la convierte a long
            if (chosen < 1 || chosen > count) { // Comprueba que el número corresponda a una entrada de la lista
                System.out.println(Constants.BAD_OPTION); // Informa que el número está fuera del rango válido
                return; // Termina sin iniciar la reproducción
            }
            player.play(storage.listAt(chosen - 1).name()); //Obtiene el nombre elegido e inicia su reproducción(ocurre la busqueda binaria)
        } catch (NumberFormatException e) { //Maneja el caso en que la respuesta no sea un número válido
            System.out.println(Constants.BAD_OPTION); //Informa que la entrada no es válida
        }
    }
}
