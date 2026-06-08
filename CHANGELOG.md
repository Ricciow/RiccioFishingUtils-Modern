# v1.12.0 - Atoll Patch

This also fixes the crash from the incorrect sc json

### Features
- Added Lotus Atoll to RFU Party Finder (Only appears to people on this version onwards)
- Added Lotus Atoll SCs

### Changes
- Removed normal scs from bayou

### Back-end
- Added an option to disable automatic Sea Creature configuration synchronization in Backend Settings.

### Fixes
- Improved Sea Creature configuration error handling to prevent malformed JSON entries from breaking the loading process aka crashing if incorrect.
- Fixed hotspots height calculation to correctly handle waterlogged blocks.
- Made the party finder treat unknown islands and display them
- Fixed the Trophy Chance Hotspots (was Trophy Fish Chance)