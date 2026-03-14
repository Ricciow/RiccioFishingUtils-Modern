# v1.6.0 - ???

### Features:
- Added Highlight Hotspot feature with radius detection and buff-based coloring.
- Added option to make the ls range filled (on by default)

### Changes:
- Merged XP/h and SC/h displays into a single Fish Tracking display.
- Unified fishing tracking settings and added a migration step.
- Made the "overall" part of the fishing tracker more compact.
- Made the lootshare range turn green when inside it.

### Fixes:
- Fixed description of failed cast sound
- Improved Highlight Hotspot radius calculation stability to prevent overshooting at first.

### Back-end:
- Switched 3D sphere and disk overlays from a simple projection check to frustum-based culling.

### Other: