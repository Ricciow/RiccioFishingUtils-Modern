# v1.5.0 - ???

### Features:
- Added the classic rare sc sound from old RFU (rfu:rare_sc)
- Added the jawbus death sound from old RFU (rfu:death)
- Added a sound for deployable expired (rfu:deployable_expired (event))
- Added a failed cast sound (rfu:failed_cast (event))

    (All of these sounds can be modified through a resourcepack, [Guide](https://minecraft.wiki/w/Tutorial:Creating_a_resource_pack))
- Added /rfuclearcakes commands to stop the alerts from cake expired
- Added achievements support (Currently 20! And more to come...)

### Changes:
- Merged Flare Timer and Umberella Timer into a single unified Deployables HUD with a per-type toggle; 
- Moved Rare Sea Creatures settings to its own category  

### Fixes:
- Fixed a bug where SlotClickedEvent would always return the air item;
- Fixed a PetDisplay regex that would not identify the display if doing a search
- Fixed jawbus hard mode not saving before the funny
- Removed a debug println that was left on last release
- Fixed a bug where eating a cake would trigger a cake expired message
- Fixed dyes regex to work with treasure dye/aquamarine/Iceberg
- Improved the party regex to work with cishers/fishers/inkers/inker/etc...

### Back-end:
- DeployableManager now owns its own scanning behind a generic `DeployableType` interface
- Added settings migration manager that is able to migrate settings upon updates.
- Added Sound Playing capabilities
- Renamed manager package to data

### Other:
- Made the invite to party not trigger on 'me' messages in public chat