---
applyTo: "src/main/java/**/*.java,src/test/java/**/*.java"
description: "Map of the implemented persistence (binary files) and the files to open before editing it"
---
# Database

There is no SQL database. Persistence is fixed-width binary storage in `data/`:
- `recordings.music`: 48-byte command/header records.
- `recordings.index`: 32-byte name/value entries sorted case-insensitively by name.
- `recordings.list`: 32-byte name/value entries in chronological registration order.

Authoritative directories: `src/main/java/powerband/repository/` and `src/main/java/powerband/sort/`.

## Implemented
- `repository.RecordFile(Path, int)`: fixed-size random access access; public `length()`, `count()`, `read(long)`, `write(long, byte[])`, `append(byte[])`, `truncate(long)`, `close()`.
- `repository.Records`: utility for command/header/name-entry conversion; public `toBytes(CommandRecord)`, `toCommand(byte[])`, `toBytes(Header)`, `toHeader(byte[])`, `toBytes(NameEntry)`, `toNameEntry(byte[])`, `validateName(String)`.
- `repository.Recordings(Path)`: public `openMusic()`, `find(String)`, `exists(String)`, `register(String, long)`, `listCount()`, `listAt(long)`, `readHeader(long)`.
- `sort.IndexSorter`: public `BY_NAME` comparator and static `sort(Path, Path)` Balanced Merge Sort. It uses four temporary files and keeps at most one/peeked index record per merge stream.

Never replace these files with `ObjectOutputStream` or load recording data into arrays/lists for searching/sorting. Search the index record-by-record with binary search. Merge and copy temporary files record-by-record. Header and command offsets are in bytes.
