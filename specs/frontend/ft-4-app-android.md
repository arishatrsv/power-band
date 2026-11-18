---
feature: ft-4-instrumentos
layer: frontend
status: approved
---

# ft-4 – App Android del celular

## Alcance
Proyecto PowerBandPhone (Android Studio, Java, paquete cr.powerband.phone): la app que convierte el celular en el instrumento del organo.

## Reglas
-Un campo para ingresar la IP de la computadora, un boton Conectar y un area para tocar.
-Envia por UDP al puerto 9000 mensajes de texto con el formato x;y;z;toque (decimales con punto y toque con valor 1 o 0).
-Envia los datos del acelerometro cuando cambia su lectura y el estado del toque cuando el usuario presiona o suelta la pantalla.
-El manifest declara el permiso INTERNET.
-La comunicacion funciona solo en Android. El celular y la computadora deben estar conectados a la misma red.
-Los archivos generados y de configuracion local (.gradle, build, local.properties) no se versionan.

## Criterios
-[ ] La app se instala y se ejecuta en un celular Android.
-[ ] La app permite conectarse a la computadora mediante su direccion IP.
-[ ] PhoneInstrument recibe los mensajes UDP y el organo suena al tocar la pantalla y se apaga al soltarla.