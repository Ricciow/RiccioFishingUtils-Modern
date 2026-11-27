# v1.21.0 - Major

### Features
- The Bobbin' Time display now has the Magic find and Tracking bonuses and its configurable.
- Added official support for colors in party finder using &
- Added an option between Party and All Chat for hotspot sharing.
- Mute Annoying Sounds now silences the reindrake's gifts in Jerry's Workshop and Jade Dragon sounds in Galatea.
- Added an option to show the Bobbin' Time Magic Find bonus when a rare sea creature dies.
- Added Mute Non-RFU Sounds to silence game audio while keeping RFU alerts and the timer ding.
- Added /rfuemojis or /rfuemojiedit to browse, search, and manage emojis.
- Emojis now update automatically, with options in Other settings to turn off updates or use a custom source.
- Added setting that lets muted party members request a warp with Hypixel's muted private message.

### Changes
- Renamed !version command to !rfuversion
- Made kill time and personal best messages only one message.
- Removed the Golden Dragon alert.
- Drops before mf update now have a small [!] warning in /rfudrophistory.
- Made the drop history text more aligned
- Changed Prince's Crown Jewel to just say Crown Jewel
- Made the Bobbin' Time requisite have more priority on the card (Appear first)
- RNG Manipulation achievement now requires 200 MF or more
- Rebalanced LF drop dry streak achievements for the Magic Find update.
- The “What is Magic Find?” achievement has been retired. Players who previously had it can find it under the Legacy tab.

### Fixes
- Fixed trackers resetting when an individual tracker went quiet during an active fishing session.
- Fixed skill XP and chat/action bar tracking missing messages canceled by other mods or failing on maxed skills.
- Fiery Scuttlers now trigger a single alert and sound per group of up to six instead of triggering for each one.
- Fixed duplicate hotspot shares and alerts.
- Corrected the coordinates in shared hotspot messages.
- Automatic hotspot sharing now requires being fishing to trigger.
- Fixed thunder bottle message hiding not working anymore
- Fixed lootshared shards message hiding not working anymore
- Removed shadows from emoji icons on party finder.
- Fixed text shadows on Party Finder.
- Fixed Party Finder temporarily lowering the party player count during a !tw warp and letting other players join.
- Fixed Hypixel detection when the server address includes a port.
- Fixed Party Finder failing to load the party list when many parties are listed.
- Fixed dye drop detection.
- Fixed Render Chat Coords placing markers for max/min/avg tps messages.
- Fixed text moving up when selected in input fields.
- Fixed sea creature kill times now save if you die.

### Backend
- Isolated chat and skill event listeners so errors in one listener do not skip others.
- Sea creature categories and islands now refresh through sea creature config sync.
- Sea creature config sync works independently of the Connect to Backend setting.
- Added Detekt checks
- Hotspot detection now reacts to entity updates, optimizing :dog:.
- Added error chat messages when Developer Mode is enabled.
- Refactored event manager system
- Updated dependencies.