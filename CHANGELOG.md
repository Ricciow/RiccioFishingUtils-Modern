# v1.x.x - ???

### Features:
- Added {dh} option to rare sc catch message
- Added option to boost polling rate when healthbar is active for more accurate tracking

### Fixes:
- Fixed dead entities sometimes not being counted as dead (hopefully)
- Fixed double hook messages not having color (when not in a party)
- Added a soon state to the flare timer (also prevents useless alerts)
- Fixed double hook not being tracked correctly in sc/h and catch history
- Made catch counts only update on their respective islands instead of all at once

### Back-end:
- Added debug entities command
- Prevent sending titles if they are irrelevant (e.g. SC died)

### Other:
- Improved description for Fishing Downtime Limit
- Made alerts show up for less time