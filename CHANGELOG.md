# v1.11.0 - ???

### Features:
- Added Bestiary Display HUD (Requires the Bestiary Tablist to work).
- Added !coords party command to send your current coordinates (Aliases: !c, !xyz)
- Added !ptme alias to the !pt party command
- Improved !since command:
  - Added support for rare drops and dyes.
  - Added optional <username> parameter to filter who should respond.
  - Now displays related rare drops when checking a sea creature.
- Added best pets to level (/rfupets)

### Changes: 
- Made the party finder creation area not reset upon having the party deleted and made it update the island with the current player's island

### Fixes:
- Made party commands not trigger on copied messages (e.g: Party > ricciow: Party > ricciow: !since vial)
- Fixed /rfuignore not hiding join requests.
- Made Hotspot pointer accurately point to the best hotspot if there are 2 hotspots nearby
- Made the party finder re-sync if it receives a duplicated party.
- Fixed not re-validating auth upon reconnecting to rfu websocket

### Back-end
- Added HotSpotChangedEvent
- Optimized hotspot pointer code
- Added rfu user agent to requests
- Added exponential delay and jitter to reconnect to websocket client to prevent crashing the server as the number of users increases
