# v1.18.0 - ???

### Features
- Added the !sc party command with more detailed info about scs
- Added the !drop party command with detailed info about rare drops and dyes
- Added message hiding options for Charm and Hurricane in a Bottle messages
- Added 3 new daily challenges
- Added :carrot: :shark: :fish: :fhbt: emojis
- Updated /rfudrophistory with pages and added /rfudrophistory add and /rfudrophistory remove commands
- Added a fix for failed casts
  - Uses Packet Canceling for this so use at your own risk! (Same as SkyHanni)
- Added Party Finder party presets system

### Fixes
- Added Elementa memory leak patch (toggleable in Other settings if it causes conflicts)
- Fixed Galatea stuff not being counted properly
- Fixed Soul Fish not being properly tracked

### Changes
- Changed the !since command to display only simple info
- Removed Boost Polling rate setting and changed the display itself to update when the entity is updated
- Daily Streak HUD and Daily Challenge cards now format large progress numbers compactly
- Renamed Galatea to Moonglade Marsh
- Made the emojis in party finder be sent in emoji form so it only occupies 1 char
- Made party finder creation window save between sessions

### Back-end
- Made the mob tracking event driven, making it faster and improving Time to Kill accuracy.