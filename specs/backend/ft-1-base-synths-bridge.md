---
feature: ft-1-base-synths-bridge
layer: backend
status: draft
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
- [ ] DemoMain hace sonar piano, bajo y organo pasando por el bus.
- [ ] mvn clean test sigue en verde.
- [ ] Agregar un synth = una clase nueva en command/.
