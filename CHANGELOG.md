# v1.20.0 - Major

### Features
- Added Coords waypoints (off by default).
- Added Custom Lava feature (off by default).
- Added Sulphur Skitter range display.
- Added a world border on the Crimson Isle in the Spider's Den direction.
- Added customizable title alerts and added sound previews.
- Added alerts for fishing without Moby-Duck, without rain, without a Chum Bucket, or fishing outside a hotspot. (all off by default)
- Added a highlight setting for lava fishing itens to sell.
- Added an achievement for using Moby-Ducks.
- Added Show Actual Arrow Count, same as Show Actual Bait Count but for arrows.
- Added a mute annoying sounds feature under General Fishing to mute Banshees and Thunder Sparks.

### Changes
- Moved the Party Finder scrollbar slightly to the right.

### Fixes
- Fixed mobs without health, such as Puddle Jumpers, not being detected as SkyBlock entities.
- Fixed Hypixel detection when connecting through hypixel.fast (whatever this is)
- Improved the exp/h when under level 50 to not double count gains.
- Fixed the Jawbus death sound ignoring its toggle.
- Improved TPS tracking.

### Backend
- Reworked rare drop and dyes save to use independent counters.
- Added migrations for sea-creature settings.
- Added a slot rendering event manager.
- Added a world border rendering event manager.
- Updated Elementa and UniversalCraft, and removed the text bleed patch workaround.
- Added support for cubes, beacon beams, and player heads in the 3D rendering utilities.
