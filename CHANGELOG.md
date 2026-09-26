# v1.19.0 - ???

### Features
- Added an Auto accept players setting for Party Finder.
- Added a setting to toggle the daily streak party command
- Added personal best kill times for sea creatures
- Added message hiding options for Vanquisher spawns, treasure catches, and Blazetekk Radio
- Added Rare SC Alert Preset setting with {color} variable support and alert preview in /rfuscedit
- Added Party Requeue Alert to prompt requeuing when your party is no longer full
- Added TPS Hud display (disabled by default)
- Added Shard Hotspots support
- Added :aquamarine: :carmine: :midnight: and :treasure: emojis
- Added Remove Nether Fog setting (disabled by default)
- Added /kaboom command to send :kaboom: in party chat and leave the party
- Added Bait Count setting to replace the number on the 9th slot stack size
- Added No Tiki Mask Alert when fishing on a hotspot without a Tiki Mask
- Added Invulnerability Timer for rare sea creatures and Vanquishers
- Added a setting to customize the text shown when the rod timer ends
- Added individual extra duration settings for each alert title

### Changes
- The Charm message hiding option now also hides shard loot share messages
- Improved settings search to show full matching categories or sections
- Revoked previous backend acceptance to display privacy policy
- Added (ᵔᴥᵔ) as a trigger for the :dog: emoji
- Made the introduction walkthrough send as a single message upon joining SkyBlock instead of separate messages on Hypixel join
- Made inventory HUD elements be floating when the inventory is open
- Made daily streaks only reset if you logged in on a day and failed to complete it, preserving streaks across skipped days
- Updated hotspot share messages to include stat counts and shard names
- Updated Slugfish Timer to be server tick based
- Made Frog Blessing display save between sessions
- Made the Rare Drops/Sc displays hidden while trophy or treasure fishing
- Made the SC/h line on hud hidden when its rate is zero
- Made the daily streak command require daily streaks on

### Fixes
- Fixed hotspot highlights and tracking keeping the previous type
- Disabled Party Finder hover effects while using the peek keybind
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
