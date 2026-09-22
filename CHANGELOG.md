# v1.19.0 - ???

### Features
- Added Rare SC Alert Preset setting with {color} variable support and alert preview in /rfuscedit
- Added Party Requeue Alert to prompt requeuing when your party is no longer full
- Added TPS Hud display (disabled by default)
- Added Shard Hotspots support
- Added :aquamarine: :carmine: :midnight: and :treasure: emojis
- Added Remove Nether Fog setting (disabled by default)
- Added /kaboom command to send :kaboom: in party chat and leave the party
- Added Bait Count setting to replace the number on the 9th slot stack size

### Changes
- Revoked previous backend acceptance to display privacy policy
- Added (ᵔᴥᵔ) as a trigger for the :dog: emoji
- Made the introduction walkthrough send as a single message upon joining SkyBlock instead of separate messages on Hypixel join
- Made inventory HUD elements be floating when the inventory is open
- Made daily streaks only reset if you logged in on a day and failed to complete it, preserving streaks across skipped days
- Updated hotspot share messages to include stat counts and shard names
- Updated Slugfish Timer to be server tick based
- Made Frog Blessing display save between sessions

### Fixes
- Fixed sea creature glow only displaying one entity and invisible entities sometimes
- Fixed bossbar and merge health settings in /rfuscedit not properly applying to sea creatures
- Peek party finder keybind now closes the creation window / presets
- Prevented the session reset button on hud from triggering while editing
- Improved caching on the party finder window
- Fixed skeleton fish being categorized as a rare tfish instead of epic
- Fixed Moby-Duck timer disappearing when leaving SkyBlock or restarting the game
- Fixed texture memory leak when opening inventory with HUD hidden
- Fixed inventory HUD clicks conflicting with REI
- Fixed Hotspot Pointer pointing to lower or same hotspots
- Fixed hotspot rendering and radius calculations breaking with skyocean
- Fixed fishing speed color on frog blessing display

### Back-end
- Added 26.3 support
- Updated build dependencies
- Reworked entity system to accept more model entities
- Automatically disconnect from the RFU Back-end when AFK