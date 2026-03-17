# v1.6.0 - ???

### Features:
- Added Highlight Hotspot feature with radius detection and buff-based coloring.
- Added option to make the ls range filled (on by default)
- Added Hotspot Hopper achievement.
- Added Solid Magma achievement.
- Added Buff Collector achievement.

### Changes:
- Merged XP/h and SC/h displays into a single Fish Tracking display.
- Unified fishing tracking settings and added a migration step.
- Made the "overall" part of the fishing tracker more compact.
- Made the lootshare range turn green when inside it.
- Made Rare SC party messages turn the preceding "a" into "an" if the sc name starts with a vowel
- Updated Sea Creature catch tracking to only increment/reset if the creature's conditions are met.

### Fixes:
- Fixed description/name of failed cast sound
- Improved Highlight Hotspot radius calculation stability to prevent overshooting at first.
- Fixed a typo in the Titanoboa catch message which would cause it to not be detected as a catch.
- Fixed not escaping the catch messages for the regex resulting in any sea creature with a reserved char not being detected
- Fixed dyes regex to work with treasure dye/aquamarine/Iceberg (Again :/)

### Back-end:
- Switched 3D sphere and disk overlays from a simple projection check to frustum-based culling.
- Added hotspot catch detection for sea creatures.
- Added HOT_SPOT achievement category.
- Expanded `SeaCreatures` enum with condition support.
- Updated `CatchHistory` and `CatchTracker` to support conditional catch recording.

### Other: