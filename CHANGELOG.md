# v1.11.0 - Pet Party

### Features:
- Added a keybind to share hotspot coordinates.
- Added Bestiary Display HUD (Requires the Bestiary Tablist to work).
- Added !coords party command to send your current coordinates (Aliases: !c, !xyz)
- Added !ptme alias to the !pt party command
- Improved !since command:
  - Added support for rare drops and dyes.
  - Added optional <username> parameter to filter who should respond.
  - Now displays related rare drops when checking a sea creature.
- Added best pets to level (/rfupets)
  - This is HUGE, i think :dog:

### Changes: 
- Made the party finder creation area not reset upon having the party deleted and made it update the island with the current player's island
- Made the downtime window minimum higher (1 minute -> 5 minutes)
  - Measurements get really inaccurate on smaller numbers
- Made the hotspot received messages only appear if player is actively hotspot fishing.
- Made !pt command transfer to imcomplete player names (e.g. !pt ric -> /p transfer ricciow) 

### Fixes:
- Fixed Fishing Session XP/h being inaccurate with repeated xp numbers.
- Made party commands not trigger on copied messages (e.g: Party > ricciow: Party > ricciow: !since vial)
- Fixed /rfuignore not hiding join requests.
- Made Hotspot pointer accurately point to the best hotspot if there are 2 hotspots nearby
- Made the party finder re-sync if it receives a duplicated party.
- Fixed not re-validating auth upon reconnecting to rfu websocket
- Fixed hotspot messages from feesh not being tracked
- Made togglewarp command only trigger if the user is leader

### Back-end
- Added HotSpotChangedEvent
- Optimized hotspot pointer code
- Added rfu user agent to requests
- Added exponential delay and jitter to reconnect to websocket client to prevent crashing the server as the number of users increases
