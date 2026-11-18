---
feature: ft-4-instrumentos
layer: frontend
status: draft
---

# ft-4 – App Android del celular

## Alcance
Proyecto PowerBandPhone (Android Studio, Java, paquete cr.powerband.phone): la app que convierte el celular en el instrumento del organo.

## Reglas
-Un campo para la IP de la computadora, un boton Conectar y un area para tocar.
-Manda por UDP al puerto 9000 un texto con el formato x;y;z;toque (decimales con punto, toque 1 o 0).
-Envia al cambiar el acelerometro y al tocar o soltar la pantalla.
-El manifest declara el permiso INTERNET.
-Solo Android. El celular y la computadora deben estar en la misma red.
-Los archivos generados (.gradle, build, local.properties) no se versionan.

## Criterios
-[ ] La app se instala y se ejecuta en un celular Android.
-[ ] PhoneInstrument recibe los mensajes y el organo suena al tocar y se apaga al soltar.