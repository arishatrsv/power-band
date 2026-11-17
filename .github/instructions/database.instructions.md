---
applyTo: "src/main/java/**/*.java,src/test/java/**/*.java"
description: "Map of the implemented persistence (binary files) and the files to open before editing it"
---
# Database

There is no SQL database. Persistence is binary files with fixed-size records in the `data/` folder: `recordings.music` (48-byte records), `recordings.index` (32 bytes, sorted by name) and `recordings.list` (32 bytes, chronological).
Authoritative directories under `src/main/java/powerband/`: `repository` and `sort`.

## Implemented
- `repository/RecordFile`: one file of equal-size records with `seek` access (`read`, `write`, `append`, `count`, `truncate`).
- `repository/Records`: converts commands, headers and name entries to and from bytes with `ByteBuffer`; `validateName`.
- `repository/Recordings`: opens the three files; `find` (binary search on disk), `exists`, `register` (list + index + merge sort), `listAt`, `listCount`, `readHeader`, `openMusic`.
- `sort/IndexSorter.sort`: external Balanced Merge Sort with four temporary files; at most two records in memory.

Never load recordings into in-memory arrays or lists to search or sort them.
