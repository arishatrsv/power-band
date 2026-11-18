---
feature: ft-4-instrumentos
layer: testing
status: approved
---

# ft-4 – Pruebas automaticas

## Alcance
Las pruebas JUnit 5 de las features ft-1 a ft-4 (19 pruebas).

## Reglas
-StorageTest: verificar el tamaño fijo de los registros, la validación de nombres, el ordenamiento Balanced Merge Sort con listas de distintos tamaños y la búsqueda binaria de grabaciones existentes y no existentes.
-RecordingFlowTest: verificar el flujo completo de grabación, guardado, carga y reproducción; comprobar que se conserven los comandos y sus intervalos de tiempo (tolerancia de 60 ms); comprobar el rechazo de nombres repetidos, el descarte de grabaciones y la reproducción doble.
-InstrumentsTest: verificar la detección de tono, la generación de notas del bajo, la detección de flancos en el control, el reconocimiento de botones menores y de octava, y el envío de mensajes `noteOn` y `noteOff` del celular. También comprobar el rechazo de mensajes inválidos.
-MainTest: verificar que la aplicación muestre el mensaje de arranque esperado.
-Las pruebas deben ejecutarse sin depender de SuperCollider ni de dispositivos físicos. Para ello, utilizar un `OscClient` falso y archivos temporales mediante `@TempDir`.

## Criterios
- [ ] `mvn clean test` termina correctamente con las 19 pruebas aprobadas.
- [ ] Las pruebas no requieren hardware externo ni una instancia de SuperCollider.