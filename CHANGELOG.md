# v1.7.0 - ???

### Features:
- Added general collection tracking and session-based ink analytics.
- Added numerous ink-related achievements (Ink Obsessed, Pro Squisher, Squid Streak, etc.).
- Added Nessie to the sea creature tracking list.
- Added Lucky Clover Core to the rare drops list.
- Added Park Rain alert/sound.
- Added Ink display UI.
- Added Nessie SC
- Added a pet level up alert
- Added a reindrake alert
- Added a walkthrough upon joining the game with an acknowledgement

### Changes:
- Modified Rare SC display to filter sea creatures based on the current liquid being fished in.
- Enhanced `/rfureset` command to reset all session data, including fishing XP, ink tracking, and sea creature stats.
- Delayed the alerts sent upon joining the server by around 3s to not get burried.
- Made the zoom etherwarp have a cubic ease-in
- Reorganized the Öther tab in the settings
- RFUPF requires leader to queue the party.
- RFUPF now validates if you're already inside that party when trying to join.
- Added an auto-save to the data every 30 minutes and upon disconnecting

### Fixes:
- Fixed an issue where the Park was not correctly identified as a fishing location.
- Corrected Water Hydra being a Galatea SC.
- Flare Expired messages no longer trigger on normal fireworks.
- Corrected spooky festival having incorrect dates
- Fixed an issue where rare sea creature party messages would say "A The [Name]" instead of "The [Name]" for certain creatures.
- Made mayor fetching happen at least 25s after election is over to prevent wrong mayor fetching
- Made hotspot detection require less particles but also limited the range of particles to only 2 (Should make it more accurate, tell me if it didn't)
- Fixed professional downtimer achievement from triggering unintendedly.

### Back-end:
- Removed unnecessary debug messages and cleaned up unused imports.
- Reworked entire back-end to be websocket based (big change, though little impact, for now...)
- Refactored session logic to be more modular and easier to manage.
- Refactor coroutines to have a general utils module.
