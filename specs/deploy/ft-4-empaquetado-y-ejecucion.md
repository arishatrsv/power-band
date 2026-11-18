---
feature: ft-4-instrumentos
layer: deploy
status: draft
---

# ft-4 – Empaquetado y ejecucion

## Alcance
pom.xml y la forma de ejecutar el programa.

## Reglas
-Maven con Java 21 y JUnit 5.
-JInput es la unica libreria externa; se declara el jar y el jar de librerias nativas (clasificador natives-all).
-maven-dependency-plugin copia las dependencias a target/lib y descomprime las nativas en target/natives.
-El jar declara el classpath con prefijo lib/ y la clase principal powerband.Main.
-Ejecucion: java "-Djava.library.path=target/natives" -jar target/power-band.jar, con SuperCollider abierto y ~myband.load; ejecutado.
-.gitignore excluye target/, data/, .DS_Store y los archivos generados de Android.

## Criterios
-[ ] mvn clean package termina en BUILD SUCCESS y deja target/power-band.jar, target/lib y target/natives.
-[ ] El programa arranca con el comando de ejecucion.
-[ ] Un clon nuevo del repositorio compila y corre sin pasos extra.