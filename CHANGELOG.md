# v1.3.0 - Refined Reels

### Features:
- Added dye drops tracking with a separate configurable dye list (all dyes now supported, now also tracks mf for those)
- Added {dh} option to rare sc catch message
- Added option to boost polling rate when healthbar is active for more accurate tracking
- Added message hiding options
- Rod Timer now also hides the timer entity
- Added failed cast alert
- Added invite to party message on keywords
- Added overall rate option to sc/h display

### Fixes:
- Fixed dead entities sometimes not being counted as dead (hopefully)
- Fixed double hook messages not having color (when not in a party)
- Added a soon state to the flare timer (also prevents useless alerts)
- Fixed double hook not being tracked correctly in sc/h and catch history
- Made catch counts only update on their respective islands instead of all at once
- Fixed RodTimer not correctly centering the !!!

### Back-end:
- Extracted `Dyes` enum from `RareDrops` with full metadata (name, hex color, related sea creatures)
- Refactored `DropEvents` into separate `RareDropEventManager` and `DyeDropEventManager`
- Refactored `DropHistory` to track dye drops separately
- Added Gson deserializer to gracefully skip unknown enum entries on save load
- Added debug entities command
- Prevent sending titles if they are irrelevant (e.g. SC died)
- Added multiline support for hud elements

### Other:
- Improved description for Fishing Downtime Limit
- Made alerts show up for less time
- Added a queueing system for party messages to avoid sending them too fast
- /rfudyes now has the dye colors on the names