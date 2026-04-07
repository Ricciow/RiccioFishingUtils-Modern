# v1.8.0 - ???

### Features:
- Added 26.1 Support
- Added a blizzard in a bottle timer/alert
- Added a hotspot expired alert
- Added an alert when a rare sea creature is at low HP without a Golden Dragon equipped
- Added a rare drop title alert
- Added a custom resourcepack that allows changing sounds. (READ "note.txt" if you're planning on using this!)
- Added a setting to make Rare SCs glow. (Off by default, Use at your own risk!)
- Added customizable catch messages with context-aware placeholders like `{mob}` and `{mobs}`.
- Added the hability to preview custom messages/titles

### Changes:
- Removed the 30 minute to expire message in the party finder creation area.
- Made Reindrake not display the lootshare range
- Reorganized settings, added rare drops category

### Fixes:
- Made water hotspot particles even more strict for detections (When will this end?)
- Added Hotspot scs to jerry island.
- Fixed reindrake alert not working
- Made rfu websocket only connect while in hypixel.
- Transfering leadership now dequeues the party finder.
- Fixed party achievement not working with rfu websocket joins

### Back-end:
- Changed some of the sound names for rfu.
- Automated sound registration
- Optimized Hotspot size calculation to use a median-based cache by coordinate for improved accuracy and consistency.