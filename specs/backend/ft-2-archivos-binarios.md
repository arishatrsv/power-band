---
feature: ft-2-archivos-binarios
layer: backend
status: draft
---

# ft-2 – Archivos binarios, busqueda binaria y merge sort externo

## Alcance
RecordFile, Records, Recordings (repository) e IndexSorter (sort).

## Reglas
- recordings.music: registros de 48 bytes (encabezado y comandos). recordings.index y recordings.list: registros de 32 bytes.
- Serializacion manual con ByteBuffer (ObjectOutputStream no garantiza tamano fijo).
- Nombre de longitud fija: 12 caracteres, rellenado con espacios; unico sin distinguir mayusculas.
- Busqueda binaria en disco: seek al registro del medio, se lee uno por paso. Nunca se carga el indice.
- Balanced Merge Sort con 4 archivos temporales, al registrar cada grabacion. Sin arreglos en memoria.
- recordings.list conserva el orden cronologico.

## Criterios
- [ ] Todo registro mide exactamente su tamano.
- [ ] El indice queda ordenado tras cada register().
- [ ] find() encuentra todos los nombres y rechaza los que no existen.
- [ ] mvn clean test en verde (StorageTest).
