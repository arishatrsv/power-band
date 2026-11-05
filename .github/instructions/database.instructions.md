---
applyTo: "src/main/java/**/*.java,src/test/java/**/*.java"
description: "Map of the implemented persistence (binary files) and the files to open before editing it"
---
# Database

There is no SQL database. Persistence is binary files with fixed-size records: `recordings.music`, `recordings.index`, `recordings.list`.
Authoritative directories under `src/main/java/powerband/`: `repository` (file access, binary search on disk) and `sort` (external Balanced Merge Sort).
Nothing implemented yet. Run `update-current-development` after each feature to refresh this map.
Never load recordings into in-memory arrays or lists to search or sort them.
