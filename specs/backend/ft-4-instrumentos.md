---
feature: ft-4-instrumentos
layer: backend
status: approved
---

# ft-4 – Los tres instrumentos

## Alcance
GamepadInstrument (control PlayStation, piano), PhoneInstrument (celular, organo), HeadphoneInstrument (microfono de los audifonos, bajo), PitchDetector, app Android y Main completo.

## Reglas
- Un dispositivo distinto por synth. Ni teclado ni mouse.
- Bridge: Main decide el par instrumento-synth inyectando el SynthCommand en cada constructor; el instrumento solo traduce lo que mide a Params.
- Control: dispara al presionar (flanco), no al mantener. Stick = octava; L1 = acorde menor.
- Celular: la app manda por UDP texto x;y;z;toque al puerto 9000; PhoneInstrument actua como servidor del editor. Tocar = noteOn; soltar = noteOff de la misma nota.
- Audifonos: autocorrelacion para el tono; cada ataque (volumen sobre ONSET_RMS tras silencio) dispara una nota de bajo plegada a su rango.
- Si un dispositivo falla, Main avisa y el programa sigue con los demas.
- Lo que depende del hardware (botones, umbrales) se calibra en Constants.
- JInput necesita librerias nativas: el pom las baja (clasificador natives-all) y las descomprime en target/natives; se ejecuta con -Djava.library.path=target/natives.
- Main crea los instrumentos con una lista de Supplier y los arranca uno por uno; si uno falla imprime FAILED y sigue.

## Criterios
- [ ] Los tres instrumentos suenan por separado y a la vez.
- [ ] Lo que tocan aparece en el monitor y se graba/reproduce igual.
- [x] mvn clean test en verde (InstrumentsTest).

## Notas tras implementar
- Los criterios de hardware se marcan despues de probar cada dispositivo en la computadora de la demo.
- GamepadCalibration queda como herramienta de diagnóstico del control (se documenta en el README).