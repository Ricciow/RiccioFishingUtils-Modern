# v1.4.0 - ???

### Features:
- Added Fishing Xp/h display (works like Sc/h but tracks fishing XP gained per hour from the action bar)
- Added Lootshare detection messages.
- Added option to hide "There are blocks in the way!" message
- Added option to hide Combo messages (Won't hide the MF ones)
- Added option to hide Lootshare Messages
- Added Zoom on etherwarp (I just really missed that)
- Added option to make the health bar blue when the mob is shurikened (On by default)
- Added a Dye Display HUD element

### Fixes:
- Made flare alert not proc when a firework is active
- Pet Display now updates the displayed level when your equipped pet levels up
- Fixed crash and disconnect caused by null entries in the drops save file (Gson null-safety bypass); both `drops` and `dyeDrops` lists are now purged of nulls before use

### Back-end:
- Added `safeExecution(mainThread, func)` to `AbstractEventManager` for try-catch wrapped execution with `RFULogger` error reporting
- Unified the execution of tasks on event managers

### Other:
- Added a star to the health bar whenever the mob is shurikened