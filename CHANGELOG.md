# v1.15.0 - ???

### Features
- Added chat emoji autocompletion
- Added Vanquisher party messages.
- Added custom tooltip GUI scaling option (Just really missed that)
- Added full inventory alert option
- Added Personal Sea Creature Cap Display
- Added option to merge bossbars for duplicate sea creatures (like Scuttlers)
- Added /rfuchangelog command to view the mod changelog of your current version
- Added custom fishing keybind overrides for hotbar slots 1-9, left click, and right click
- Added tracking and HUD displays for power orb fluxes (Mana Flux, Overflux, Plasmaflux) with dynamic rarity coloring and stat boosts
- Added :angry:, :fire:, and :scream: emojis
- Added option to delete old server-sent resource packs when they're changed (This was expected behavior before, but it didn't work so now its fixed, and it's optional if you want to keep the old textures)

### Changes
- Bobber display, personal cap display, and blizzard timer now automatically hide when the fishing session is paused
- Reformatted preview party messages to simulate standard party chat prefix and colors.

### Fixes
- Fixed current equipped pet tracking when equipping pets through loadouts.
- Made item models and custom tooltip styles fall back to vanilla defaults if 
the resource pack is not active to prevent the missing textures with it not loaded
- Fixed infinite loading screen when downloading the server resource pack for the first time
- Fixed resource pack triggering a reload even though it wasn't needed

### Back-end
- Added internal tracking to link sea creatures to their originating fishing bobber
- Bundled the changelog file into the mod's built resources
- Refactored developer commands to centralize developer mode checks using Brigadier requirements
- Refactored configuration categories to use a unified reloadableBoolean helper to eliminate duplicate reloadScreen callbacks