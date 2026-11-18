---
feature: ft-4-instrumentos
layer: testing
status: draft
---

# ft-4 – Pruebas automaticas

## Alcance
Las pruebas JUnit 5 de las features ft-1 a ft-4 (19 pruebas).

## Reglas
-StorageTest: tamano fijo de los registros, validacion de nombres, Balanced Merge Sort con muchos tamanos y busqueda binaria.
-RecordingFlowTest: grabar, guardar, cargar y reproducir con los mismos intervalos (tolerancia de 60 ms), nombre repetido, descartar y reproduccion doble.
-InstrumentsTest: detector de tono, bajo, control (flancos, menor, octava) y celular (noteOn/noteOff y mensajes invalidos).
-MainTest: mensaje de arranque.
-Las pruebas no necesitan SuperCollider ni hardware: usan un OscClient falso y archivos temporales (@TempDir).

## Criterios
-[ ] mvn clean test termina en verde con 19 pruebas.