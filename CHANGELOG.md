# v1.10.0 - Dog Edition

### Features:
- Removed 1.21.10 support
- Added Hotspot Pointer with customizable priority.
  - Use at your own risk! Off by default
- Added hotspot sharing
- Added /rfuignore to block from rfupf parties, hotspot sharing and party commands
- Added party commands
- Added /rfuscedit command to open the Sea Creature Edit window
- **Added Dog Emoji :dog: (I'm not addicted, okay?!)**
- Added manual pause keybind and optional auto-pause on downtime.
- Made it so rfupf accepts the invite automatically (when accepted into a party)

### Changes: 
- Made the report/delete button in rfupf last a little bit so it is easier to click
- Made the "name" field inside the sea creature settings be used in more places.
- Fixed some descriptions and changed the default settings a bit
- Changed "What is Mf" achievement to require 200 mf or less instead of 150 or less

### Fixes:
- Made party tracking also work with /stream command and upon inviting someone
- Moved party api requests to a different thread to prevent lag spikes
- Fixed pet level up title triggering on normal chat messages
- Delayed rare drop messages / dye achievements by 100ms so they appear below dye messages

### Back-end
- Unified pausing and downtime logic across all fishing trackers (XP, SC, and Ink).
- Updated Sea Creature system to separate internal IDs from display names, fixing validation bugs and allowing for better back-end synchronization.
- Added line rendering
- Refactored rare drop title and chat messages to unify them