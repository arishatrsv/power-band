---
feature: ft-4-instrumentos
layer: deploy
status: approved
---

# ft-4 – Empaquetado y ejecucion

## Alcance
pom.xml y la forma de ejecutar el programa.

## Regl as
-Maven con Java 21 y JUnit 5.
-JInput es la unica libreria externa de ejecucion; se declaran sus dependencias y el jar de librerias nativas con el clasificador natives-all.
-maven-dependency-plugin copia las dependencias a target/lib y descomprime las librerias nativas en target/natives.
-El jar declara el classpath con el prefijo lib/ y la clase principal powerband.Main.
-Ejecucion: java "-Djava.library.path=target/natives" -jar target/power-band.jar, con SuperCollider abierto y el archivo myband.sc cargado mediante ~myband.load; ejecutado.
-.gitignore excluye target/, data/, .DS_Store y los archivos generados de Android.

## Criterios
-[ ] mvn clean package termina en BUILD SUCCESS y genera target/power-band.jar, target/lib y target/natives.
-[ ] El programa arranca correctamente con el comando de ejecucion y se conecta a SuperCollider.
-[ ] Un clon nuevo del repositorio puede compilarse y ejecutarse siguiendo las instrucciones del README, sin depender de archivos locales no versionados.