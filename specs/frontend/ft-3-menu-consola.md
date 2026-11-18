---
feature: ft-3-grabar-reproducir-menu
layer: frontend
status: approved
---

# ft-3 – Menu de consola

## Alcance
Menu (paquete ui): el menu de consola del editor musical.

## Reglas
-Opciones: 1 iniciar grabacion, 2 detener y guardar, 3 cargar y reproducir, 4 detener reproduccion, 5 monitor on/off, 0 salir.
-Todos los textos y opciones del menu se definen en Constants; el menu no contiene literales de texto ni valores de opciones.
-Al detener la grabacion se solicita un nombre hasta que sea valido y unico. Si el nombre esta vacio, se descarta la grabacion.
-Al cargar una grabacion, se muestra la lista cronologica obtenida mediante recordings.list. El usuario selecciona una grabacion por numero y esta se reproduce utilizando su nombre y la busqueda binaria del almacenamiento.
-Los errores de tipo PowerBandException se muestran al usuario sin cerrar el menu ni interrumpir su ciclo de ejecucion.
-Una opcion invalida muestra un mensaje de error y permite volver a mostrar el menu.

## Criterios
-[ ] Las seis opciones funcionan correctamente.
-[ ] Un nombre repetido se rechaza y se vuelve a solicitar.
-[ ] Un nombre vacio descarta la grabacion.
-[ ] Las grabaciones se muestran en orden cronologico y se pueden seleccionar por numero.
-[ ] Una opcion invalida no cierra el programa.

## Notas tras implementar
-El monitor se activa y desactiva mediante un AtomicBoolean compartido con Main, permitiendo que el menu controle su estado y que el suscriptor del monitor consulte ese estado.