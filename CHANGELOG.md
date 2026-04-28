# v1.11.0 - ???

### Features:
- Added Bestiary Display HUD (Requires the Bestiary Tablist to work).
- Added !coords party command to send your current coordinates (Aliases: !c, !xyz)
- Added !ptme alias to the !pt party command
- Improved !since command:
  - Added support for rare drops and dyes.
  - Added optional <username> parameter to filter who should respond.
  - Now displays related rare drops when checking a sea creature.

### Changes: 
- 

### Fixes:
- Made party commands not trigger on copied messages (e.g: Party > ricciow: Party > ricciow: !since vial)
- Fixed /rfuignore not hiding join requests.
- Made Hotspot pointer accurately point to the best hotspot if there are 2 hotspots nearby

### Back-end
- Added HotSpotChangedEvent
- Optimized hotspot pointer code
- Added rfu user agent to requests
