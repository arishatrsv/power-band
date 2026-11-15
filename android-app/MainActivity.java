package cr.powerband.phone;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Representa el instrumento "Celular" de la aplicación Android.
// Envía datos por UDP cuando cambian los sensores o el usuario toca la pantalla.
// El servidor del editor recibe esos mensajes y los convierte en comandos de sonido.
public class MainActivity extends Activity implements SensorEventListener {
    private static final int SERVER_PORT = 9000; // Puerto UDP del servidor que recibe los datos
    private static final String SEPARATOR = ";"; // Separador entre los campos del mensaje
    private static final String MESSAGE_FORMAT = "%.2f" + SEPARATOR + "%.2f" + SEPARATOR + "%.2f" + SEPARATOR + "%d"; // Formato de los ejes y el estado táctil
    private static final int TOUCH_DOWN = 1; // Valor que indica que la pantalla está siendo tocada
    private static final int TOUCH_UP = 0; // Valor que indica que la pantalla no está siendo tocada

    private SensorManager sensorManager; // Administra el acceso a los sensores del teléfono
    private final ExecutorService network = Executors.newSingleThreadExecutor(); // Ejecuta operaciones de red fuera del hilo de interfaz
    private DatagramSocket socket; // Socket usado para enviar mensajes UDP
    private InetAddress target; // Dirección IP a la que se envían los mensajes

    private volatile float x, y, z; // Guarda las lecturas de los ejes, visibles entre hilos
    private volatile boolean touching; // Guarda si el usuario mantiene tocada la pantalla

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Ejecuta la inicialización de la actividad base
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // Obtiene el administrador de sensores del sistema

        // Construye la interfaz desde Java en vez de usar archivos de diseño XML.
        LinearLayout root = new LinearLayout(this); // Crea el contenedor principal de la pantalla
        root.setOrientation(LinearLayout.VERTICAL); // Organiza sus elementos de arriba hacia abajo

        EditText ipField = new EditText(this); // Crea el campo para ingresar la IP de la computadora
        ipField.setHint("IP de la computadora (ej. 192.168.1.15)"); // Muestra un ejemplo de IP mientras el campo está vacío
        Button connect = new Button(this); // Crea el botón para conectar con el servidor
        connect.setText("Conectar"); // Define el texto visible del botón
        TextView pad = new TextView(this); // Crea el área que recibirá los toques
        pad.setText("Mantén el dedo aquí e inclina el teléfono"); // Indica cómo interactuar con el instrumento
        pad.setBackgroundColor(Color.DKGRAY); // Distingue visualmente el área táctil
        pad.setTextColor(Color.WHITE); // Hace que el texto contraste con el fondo oscuro
        pad.setGravity(android.view.Gravity.CENTER); // Centra el texto dentro del área táctil

        root.addView(ipField); // Agrega el campo de IP al contenedor principal
        root.addView(connect); // Agrega el botón de conexión debajo del campo
        root.addView(pad, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)); // Agrega el área táctil y la expande al espacio restante
        setContentView(root); // Muestra el contenedor como interfaz de la actividad

        connect.setOnClickListener(v -> connectTo(ipField.getText().toString().trim())); // Conecta usando la IP ingresada, sin espacios laterales

        // Al tocar se activa el instrumento; al soltar o cancelar el toque, se desactiva.
        pad.setOnTouchListener((View view, MotionEvent event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touching = true; // Registra que el usuario comenzó a tocar el área
                    send(); // Envía inmediatamente el estado táctil actual
                    return true; // Indica que el evento fue manejado
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    touching = false; // Registra que el usuario dejó de tocar el área
                    send(); // Envía inmediatamente el estado de liberación
                    return true; // Indica que el evento fue manejado
                default:
                    return true; // Consume los demás eventos para mantener el control del área táctil
            }
        });
    }

    private void connectTo(String ip) {
        network.execute(() -> {
            try {
                socket = new DatagramSocket(); // Crea el socket local para enviar datagramas
                target = InetAddress.getByName(ip); // Resuelve la dirección IP ingresada
            } catch (Exception e) {
                socket = null; // IP inválida: no se envía nada
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME); // ~50 lecturas/s
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];
        send();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se usa.
    }

    //Arma el texto (con Locale.US: el punto decimal, no la coma) y lo manda por UDP en el hilo de red.
    private void send() {
        final String text = String.format(Locale.US, MESSAGE_FORMAT, x, y, z, touching ? TOUCH_DOWN : TOUCH_UP);
        network.execute(() -> {
            try {
                if (socket == null || target == null) {
                    return;
                }
                byte[] data = text.getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(data, data.length, target, SERVER_PORT));
            } catch (Exception ignored) {
                // UDP: si un mensaje se pierde no pasa nada, llegará el siguiente.
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        network.shutdownNow();
        if (socket != null) {
            socket.close();
        }
    }
}
