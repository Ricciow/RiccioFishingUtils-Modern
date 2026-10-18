# v1.19.4 - Minor

### Features
- Added middle-click bind to open each HUD element's settings and return to the HUD editor when closing them.
- Added a reminder when the mod considers the user is muted.
- Added a Party Finder block button.
- Added personal best kill time achievements for Jawbus and Thunder.
- Added the !pb party command.
- Added a message hiding option for the Stealth enchantment.

### Changes
- Improved feedback on the websocket isn't connected message.
- Changed RFU sound volumes to allow up to 500% and made it use integers instead of decimal numbers in numeric fields labeled in %.
- Made Party Finder cards more compact.
- Compact numbers now use up to three digits of precision.
- Improved cake timers a bit more with the tablist

### Fixes
- Fixed Party Finder's overflow field showing full numbers after saving or loading a party.
- Improved the hotspot sharing detection
- Fixed emoji suggestion backgrounds becoming too narrow when SkyHanni's Modify Words is enabled.
- Fixed other mute messages triggering the muted state in rfu, if you've been affected you can use /rfuunmuteme

### Backend
- Added a developer setting to suppress the texture errors/warning caused by hypixel.
- Made announcements use HTTP only while the WebSocket is disconnected.
