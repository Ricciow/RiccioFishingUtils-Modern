# v1.18.0 - QoL & Polish

### Features
- Added the !sc party command with more detailed info about scs
- Added the !drop party command with detailed info about rare drops and dyes
- Added message hiding options for Charm and Hurricane in a Bottle messages
- Added 3 new daily challenges
- Added :carrot: :shark: :fish: :fhbt: :rolling_eyes: emojis
- Updated /rfudrophistory with pages and added /rfudrophistory add and /rfudrophistory remove commands
- Added a fix for failed casts
  - Uses Packet Canceling for this so use at your own risk! (Same as SkyHanni)
- Added Party Finder party presets system
- Added {mob} variable to rare drop chat messages and title alerts
- Added Rare Drops Display
- Added a better default HUD system and a reset button
- Added Trophy Sack filleting and donation display on sacks

### Fixes
- Added Elementa memory leak patch (toggleable in Other settings if it causes conflicts)
- Fixed Galatea stuff not being counted properly
- Fixed Soul Fish not being properly tracked
- Fixed renders moving when camera moves (Thanks Void)

### Changes
- Changed the !since command to display only simple info
- Removed Boost Polling rate setting and changed the display itself to update when the entity is updated
- Daily Streak HUD and Daily Challenge cards now format large progress numbers compactly
- Some HUD elements now have buttons on inventory
- Made party finder creation window save between sessions
- Made panic keybind for custom fishing keybinds not disable the feature entirely.
- Improved HUD editor snapping.

### Back-end
- Made the mob tracking event driven, making it faster and improving Time to Kill accuracy.
- Linked rare drops and dyes to mobs.
- Added exponential backoff to Mayor API requests on failure.
- Reworked the keybind system to be event-driven.