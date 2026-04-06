# v1.8.0 - ???

### Features:
- Added a blizzard in a bottle timer/alert
- Added a hotspot expired alert
- Added a custom resourcepack that allows changing sounds. (READ "note.txt" if you're planning on using this!)
- Added a setting to make Rare SCs glow. (Off by default, Use at your own risk!)

### Changes:
- Removed the 30 minute to expire message in the party finder creation area.
- Made Reindrake not display the lootshare range

### Fixes:
- Made water hotspot particles even more strict for detections (When will this end?)
- Added Hotspot scs to jerry island.
- Fixed reindrake alert not working

### Back-end:
- Updated Stonecutter to 0.9 and modernized the codebase using the centralized property system and local code transformations.
- Changed some of the sound names for rfu.
- Automated sound registration
- Optimized Hotspot size calculation to use a median-based cache by coordinate for improved accuracy and consistency.