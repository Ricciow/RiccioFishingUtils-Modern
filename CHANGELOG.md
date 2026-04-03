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
- Enhanced `/rfureset` command to reset all session data, including fishing XP, ink tracking, and sea creature stats.
- Delayed the alerts sent upon joining the server by around 3s to not get burried.
- Made the zoom etherwarp have a cubic ease-in
- Reorganized the Öther tab in the settings

### Fixes:
- Fixed an issue where the Park was not correctly identified as a fishing location.
- Corrected Water Hydra being a Galatea SC.
- Flare Expired messages no longer trigger on normal fireworks.
- Corrected fishing festival creatures not counting correctly

### Back-end:
- Removed unnecessary debug messages and cleaned up unused imports.
- Reworked entire back-end to be websocket based (big change, though little impact, for now...)
- Refactored session logic to be more modular and easier to manage.
- Refactor coroutines to have a general utils module.
