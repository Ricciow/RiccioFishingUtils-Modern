# v1.9.0 - Partying Visuals

### Features:
- Added 26.1.2 support
- Added a littlefoot alert (Haven't tested it, thats Narga's job)
- Added volume sliders to all sounds that were missing it
- Added the ability to more deeply customize each sea creature
- Redesigned the party finder UI

### Changes:
- Added a stability check before resizing hotspots
- Updated flare radius and alerts logic (Timer only shows while in radius, alerts only on expiration while in radius)
- Made the rare sc display use the custom sc colors

### Fixes:
- Actually fixed hotspot particles being hid while highlight hotspot is off
- Fixed pet display not saving in between sessions
- Made mod sent messages not trigger event (Could cause an infinite loop that crashes game)
- Made deployable alert not trigger upon swapping islands
- Fixed the generated custom sound json not having the replace flag

### Back-end:
- Refactored sea creatures system to be data-driven via `sc-config.json`
- Refactored party system to use Hypixel Mod Api for party tracking