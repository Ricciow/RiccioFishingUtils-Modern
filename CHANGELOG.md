# v1.20.1 - Minor

### Features
- Added blacklist/whitelist for party commands.
- Added Custom Water under General Fishing settings.
- Added separate custom trophy fish and frog messages settings and {total} variable.

### Changes
- Custom Water and Custom Lava now have 3 tones, and opacity sliders.
- Removed {type} from custom trophy messages.

### Fixes
- Fixed invulnerability timers and other world text rendering behind entities on 26.1.
- Unusually Timed Fishing/Spooky Festivals are now detected from catch messages.
- Changed Begginer's Luck achievement to check the last used rod instead of the currently held item at the time of the catch.
- Fixed LF item achievements not having their number on the title
- Fixed hotspot sea creatures not being counted on Atoll.

### Backend
- Added a Kotlin wrapper for list config entries.