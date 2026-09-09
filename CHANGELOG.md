# v1.18.1 - Qol & Polish Patches

### Features
- Added a requeue button to the party dequeued message and /rfurequeue command.
- Prevent sending party/chat messages when muted and added /rfuunmuteme command.
- Added Moby-Duck Wisdom buff HUD display.

### Fixes
- Fixed date input in /rfudrophistory add not working.
- Made hud not be hidden while on inventory
- Fixed party preset loading not validating and disable requirements not met by the player.
- Fixed mod not auto rejoining on togglewarp
- Fixed Plhlegblast alerts, boss health bar, and kill time tracking not working when mob health reaches billions, thank you Derpy.

### Back-end
- Added an event manager for fishing sessions and made fishing HUD elements update reactively.