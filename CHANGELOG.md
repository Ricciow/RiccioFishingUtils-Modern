# v1.18.1 - Qol & Polish Patches

### Features
- Added a requeue button to the party dequeued message and /rfurequeue command.
- Prevent sending party/chat messages when muted and added /rfuunmuteme command.
- Added Moby-Duck Wisdom buff HUD display.

### Changes
- Reworked /rfuemojis to be more compact.
- Made Flare timer server tick based

### Fixes
- Fixed the camera spinning after taking an Essential screenshot.
- Fixed date input in /rfudrophistory add not working.
- Made hud not be hidden while on inventory
- Fixed party preset loading not validating and disable requirements not met by the player.
- Fixed mod not auto rejoining on togglewarp
- Fixed Plhlegblast alerts, boss health bar, and kill time tracking not working when mob health reaches billions, thank you Derpy.
- Fixed rare sea creature alert sometimes triggering when the creature dies.
- Fixed flare timer not reverting to older active flares when a newer flare despawns.
- Fixed party description text rendering vertically when opening the party finder creation menu or loading presets.

### Back-end
- Added an event manager for fishing sessions and made fishing HUD elements update reactively.