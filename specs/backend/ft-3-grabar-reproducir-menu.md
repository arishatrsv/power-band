---
feature: ft-3-grabar-reproducir-menu
layer: backend
status: draft
---

# ft-3 – Grabar, reproducir, menu y Main

## Alcance
Recorder y Player (service), Menu (ui) y Main (arranque).

## Reglas
- Recorder es suscriptor del CommandBus: guarda todos los comandos de todos los instrumentos solo mientras se graba.
- El encabezado se reserva al iniciar (nombre vacio) y se completa al guardar, porque el nombre se pide al terminar.
- Tiempo de cada comando relativo al inicio de la grabacion (nanoTime).
- Nombre unico (busqueda binaria en disco); si es invalido o repetido se vuelve a pedir y la grabacion sigue pendiente. Nombre vacio = descartar.
- Player: hilo lector (disco a cola) + hilo reproductor (hora absoluta base + tiempo relativo). Lo reproducido va directo a OscClient: no se regraba.
- Al salir del programa se descarta cualquier grabacion pendiente.
- Main es el unico que conoce las clases concretas.

## Criterios
- [ ] grabar, guardar, listar (orden cronologico), cargar y reproducir con los mismos intervalos.
- [ ] nombre repetido rechazado sin perder la grabacion.
- [ ] mvn clean test en verde (RecordingFlowTest).
