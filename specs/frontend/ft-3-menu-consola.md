---
feature: ft-3-grabar-reproducir-menu
layer: frontend
status: draft
---

# ft-3 – Menu de consola

## Alcance
Menu (paquete ui): el menu de consola del editor musical.

## Reglas
-Opciones: 1 iniciar grabacion, 2 detener y guardar, 3 cargar y reproducir, 4 detener reproduccion, 5 monitor on/off, 0 salir.
-Todos los textos y opciones viven en Constants; el menu no tiene literales.
-Al detener la grabacion se pide el nombre hasta que sea valido y unico. Nombre vacio = descartar.
-Cargar: lista cronologica desde recordings.list; el usuario elige por numero y se reproduce por nombre (busqueda binaria).
-Un error de PowerBandException se muestra sin cerrar el menu.

## Criterios
-[ ] Las seis opciones funcionan.
-[ ] Un nombre repetido se rechaza y se vuelve a pedir.
-[ ] Una opcion invalida no cierra el programa.

## Notas tras implementar
-El monitor se activa y desactiva con un AtomicBoolean compartido con Main.