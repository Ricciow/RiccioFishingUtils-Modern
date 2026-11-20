# v1.20.3 - Minor

### Features
- The Bobbin' Time display now has the Magic find and Tracking bonuses and its configurable.
- Added official support for colors in party finder using &
- Added an option between Party and All Chat for hotspot sharing.
- Mute Annoying Sounds now silences the reindrake's gifts in Jerry's Workshop.
- Added an option to show the Bobbin' Time Magic Find bonus when a rare sea creature dies.
- Added Mute Non-RFU Sounds to silence game audio while keeping RFU alerts and the timer ding.

### Changes
- Made kill time and personal best messages only one message.
- Removed the Golden Dragon alert.
- Drops before mf update now have a small [!] warning in /rfudrophistory.
- Made the drop history text more aligned
- Changed Prince's Crown Jewel to just say Crown Jewel
- Made the Bobbin' Time requisite have more priority on the card (Appear first)
- RNG Manipulation achievement now requires 200 MF or more
- The “What is Magic Find?” achievement has been retired. Players who previously had it can find it under the Legacy tab.

### Fixes
- Fixed duplicate hotspot shares and alerts.
- Corrected the coordinates in shared hotspot messages.
- Automatic hotspot sharing now requires being fishing to trigger.
- Fixed thunder bottle message hiding not working anymore
- Fixed lootshared shards message hiding not working anymore
- Removed shadows from emoji icons on party finder.

### Backend
- Sea creature categories and islands now refresh through sea creature config sync.
- Sea creature config sync works independently of the Connect to Backend setting.
- Added Detekt checks
- Hotspot detection now reacts to entity updates, optimizing :dog:.