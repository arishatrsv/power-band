---
feature: ft-1-base-synths-bridge
layer: backend
status: approved
---

# ft-1 – Base, synths y Bridge

## Alcance
Constantes, excepciones propias, modelo, cliente OSC, comandos de synth, CommandBus e Instrument.

## Reglas
- Bridge: Instrument (abstraccion) separado de SynthCommand (implementacion); un synth nuevo no toca instrumentos y viceversa.
- Todo comando pasa por CommandBus (Observer); nadie le habla a SuperCollider directo.
- Ningun valor quemado: todo en constants.Constants.
- Params unico para los 3 synths (action, note, durMs, extra) para que todos los registros midan igual.
- noteOff del organo se graba como comando propio (Action.NOTE_OFF).

## Criterios
- [ ] Con los instrumentos activos, los 3 synths suenan pasando por el bus (se marca tras la prueba en la demo).
- [x] mvn clean test sigue en verde.
- [x] Agregar un synth = una clase nueva en command/.

## Notas tras implementar
- DemoMain era temporal y se elimino; la prueba de sonido ahora se hace con Main.
- Los textos, puertos, rutas y tamanos viven en Constants; solo quedan literales en los codigos de los enums Action y SynthType y en indices de arreglos.
