# v1.5.1 - ???

### Features:
- Added 6 more achievements

### Changes:
- Added a 1-second cooldown to the achievement sound to prevent loudness.
- Improved order of achievements in /rfuachievements
  - normal -> secret -> completed (all sorted by difficulty)
- Made Sunny Day/Still Lava achievements require having caught atleast one
of their respective Sea Creatures beforehand.

### Fixes:
- Fixed MinMaxing achievement not working with skinned helmets
- Fixed Failure is expected... achievement having the same id as flask thief

### Back-end:
- Refactored achievement count and goal to use Long
- Refactored Achievements UI to compact numbers larger than 10k

### Other: