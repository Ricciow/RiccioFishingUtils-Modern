# v1.17.0 - Daily Fishing

### Features
- Added a daily streak and challenges system with scaling targets, HUD overlay, and /rfudailies GUI
- Extended the survivalist achievement with 4 new stages
- Added 4 new achievements
- Added a setting to adjust hotspot highlight border opacity
- Added a new Bloodshot requirement in Party Finder
- Added :pod: :silk: :hog: :exploding_head: :boom: emojis

### Fixes
- Fixed other messages being counted as trophy catches when they shouldn't
- Fixed togglewarp not auto re-joining the party
- Fixed an issue where pressing enter really fast would not complete the emoji properly
- Fixed hotspot sea creature counts not couting properly on torrhus
- Fixed the dye achievements not triggering on vincent menu

### Changes
- Reduced Squid Collection achievement to max out at 2M Collection
- Reduced Ink Obsessed achievement to cap out at 100k
- Added a 5s cooldown between creating party finder entries
- Made the party finder alerts off by default
  - Note: This was mostly meant for when there weren't many users of rfupf, since it is now somewhat relevant, this doesn't have much purpose anymore

### Back-end
- Added current equipment tracking
- Added automatic backups for config and data.
- Removed Client-side Eman 9 and Looting 5 validations