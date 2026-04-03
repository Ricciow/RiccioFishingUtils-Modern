# v1.7.0 - ???

### Features:
- Added general collection tracking and session-based ink analytics.
- Added numerous ink-related achievements (Ink Obsessed, Pro Squisher, Squid Streak, etc.).
- Added Nessie to the sea creature tracking list.
- Added Lucky Clover Core to the rare drops list.
- Added Park Rain alert/sound.
- Added Ink display UI.

### Changes:
- Enhanced `/rfureset` command to reset all session data, including fishing XP, ink tracking, and sea creature stats.

### Fixes:
- Fixed an issue where the Park was not correctly identified as a fishing location.

### Back-end:
- Removed unnecessary debug messages and cleaned up unused imports.
- Reworked entire back-end to be websocket based (big change, though little impact, for now...)
- Refactored session logic to be more modular and easier to manage.
