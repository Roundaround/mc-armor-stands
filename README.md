![Armor Stands](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/title-round-1.png)

![](https://img.shields.io/badge/Loader-Fabric%20|%20Forge%20|%20NeoForge-313e51?style=for-the-badge)
![](https://img.shields.io/badge/MC-26.1--26.1.2%20|%201.21%20|%201.20%20|%201.19-313e51?style=for-the-badge)
![](https://img.shields.io/badge/Side-Client+Server-313e51?style=for-the-badge)

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/armor-stands?style=flat&logo=modrinth&color=00AF5C)](https://modrinth.com/mod/armor-stands)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1295440?style=flat&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/armor-stands)
[![GitHub Repo stars](https://img.shields.io/github/stars/Roundaround/mc-armor-stands?style=flat&logo=github)](https://github.com/Roundaround/mc-armor-stands)

[![Support me on Ko-fi](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/donate/kofi-singular-alt_vector.svg)](https://ko-fi.com/roundaround)

Place, pose, and dress armor stands with an easy-to-use UI. Simply right click armor stands to edit them!

On servers, players either need OP permissions or need to be explicitly added with the command `/armorstands add playername`.

![Editing an armor stand with the Armor Stands UI](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/overview.png)

## Helpful tips

- When the armor stands UI is open, you can still move around! Use your normal keys to move and jump, and if you want to unlock your camera, simply hold the ALT key!
- The mod comes with clipboard and undo/redo systems. Typical keyboard shortcuts apply, or use the buttons at the top left.
- In case you're working with a bunch of stands at the same time, you can highlight the current one! The mod includes a keybinding (you'll need to assign it a key!) to highlight the stand while the UI is open.
- If a stand is inside a block with gravity turned on, it will fall through! This is a vanilla mechanic and in some cases the stand can fall all the way down through bedrock.
- The undo/redo function is especially helpful for testing out the features! Try hitting a button to see what it does, then simply _undo_ it to try something else!
- You can navigate between screens by clicking the buttons at the top right or using 1-6 on your keyboard.

## Configuration

As of version 2.0.0, the mod ships with configuration support! There are two potential locations for config files, depending on the environment.

### Single player/client side

There is a "global" config file for your client that applies to all your single player worlds. This will be found in `<minecraft directory>/config/armorstands.toml`. You can also modify it through the config screen if you have Mod Menu installed!

`requireSneakingToEdit`: `true|false` - Whether you open the edit GUI through just right click (`false`) or sneak + right click (`true`).

`nameRenderDistance`: `<Integer>` - How far away armor stand names should be visible. Set to 0 to fall back to default.

`directOnlyNameRender`: `true|false` - Only render armor stand names when targeting them. Note that "targeting" an entity in Minecraft only works within your "entity interaction range", which is 3 blocks by default. This can be changed by effects/mods.

`disableAutoSkinRefresh`: `true|false` - Globally turn off the periodic auto-refresh of mannequin skins. Overrides each stand's "Auto-refresh skin" toggle, so no mannequin re-fetches its skin automatically; the manual "Refresh skin" button still works.

`disableMannequins`: `true|false` - Disable mannequin rendering entirely on your client. Every armor stand renders as the normal model regardless of its mannequin settings, and no skins are ever looked up.

`disableMannequinAnimations`: `true|false` - Turn off the mannequin idle animation on your client, regardless of each stand's "Animations" toggle.

### Dedicated server

The server's configuration file will be located inside the world's directory, i.e. `<world>/config/armorstands.toml`. There are a few more configuration options for server side, particularly for controlling who has access to use the mod. If you previously used the `enforce-armor-stand-permissions` setting in the `server.properties` file or the `armorstandusers.json`, there are equivalents in the config file now, and the mod will automatically migrate the contents for you on first start!

`requireSneakingToEdit`: `true|false` - Whether your server's players open the edit GUI through just right click (`false`) or sneak + right click (`true`).

`enforcePermissions`: `true|false` - Whether the mod is open to everyone to use (`false`) or restricted (`true`). The config equivalent of the old `enforce-armor-stand-permissions` property in the `server.properties` for previous mod versions.

`opsHavePermissions`: `true|false` - Whether OPs automatically have permissions to use the mod. Only has any effect if `enforcePermissions` is `true`.

`allowedUsers`: `[UUID]` - List of players by their UUID permitted to use the mod. Only has any effect if `enforcePermissions` is `true`. Can also be modified from inside the game with the `/armorstands add <player>` and `/armorstands remove <player>`.

`allowAnyItemInHeadSlot`: `true|false` - Allow placing any item in an armor stand's head slot, not just helmets.

`enableMannequins`: `true|false` - Whether the mannequin feature is available on the server. When `false`, the feature is turned off server-wide: mannequin settings can't be changed and aren't sent to clients, so no stand renders as a mannequin.

## Screens

### Utilities

On the utilities screen, you'll find a lot of helpful tools to get you started with editing armor stands!

On the bottom left there are several quick-setup actions, many of which actually come from the book in Vanilla Tweaks! The one you'll likely use most often is "Character", which sets up the armor stand with a set of presets useful for creating characters around your world.

On the right is a list of toggles. Most of them are self-explanatory, but play around with them if you're not sure what they do!

![The utilities screen](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/utilities.png)

### Move

On the move page, you'll find tools to easily position your armor stand around your world.

At the top left there are two helpful blocks of text that resemble the F3 menu. These give the current position of both yourself and the stand you're editing in exact and block positions.

On the bottom left there are several quick actions. They all have tooltips to help you figure out what they can do.

On the right is a large wall of buttons. At the bottom, you'll see buttons for moving the armor stand by the specified amount and in the direction labelled on the left. Just above those is a helpful label showing which direction you are currently facing for reference. Above that are two buttons that let you change the parameters for how the stand will be moved.

![The move screen](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/move.png)

### Rotate

On the rotate page, you'll find tools to easily rotate your armor stand the direction you want it to face.

Similar to the move page, the left side features helpful text blocks and quick action buttons, and the right side has buttons to nudge the direction, plus a slider to quickly move it large amounts!

![The rotate screen](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/rotate.png)

### Pose

On the pose page, you'll find everything you'll need to tweak the armor stand's pose exactly how you want it.

On the left are several buttons to select the body part you wish to edit, and a button at the bottom to mirror the currently set up pose.

On the right, the current body part is displayed, and underneath it is a button that will change the sensitivity/range of the sliders below. Beneath that are the three sliders for the independent rotations that each body part can have, with a few buttons for each for getting the numbers _just_ right.

As of version 2.0.0, there is also a scale slider! Use this to adjust the size of your armor stand.

![The pose screen](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/pose.png)

### Presets

On the presets page, you'll find a list of all the pre-configured poses that come with the mod!

Each pose has a source (Vanilla (Java), Bedrock, Vanilla Tweaks, and some of my own custom ones), as well as a category (standing, sitting, etc). You can filter the list of presets using the two buttons at the top.

You can scroll through the pages of presets using the arrow buttons at the bottom of the screen or by using your mouse's scroll wheel while hovering over anywhere on the presets list.

![The presets screen](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/presets.png)

### Inventory

On the inventory page, you'll see a very familiar UI, where you can quickly arm your stand!

In addition to the inventory management, the bottom right includes two of the toggles from the utilities page relevant to the inventory for quick access.

![The inventory screen](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/inventory.png)

### Mannequin

On the mannequin page, you can turn your armor stand into a posable character that wears a real player's skin!

Mannequin mode is opt-in per stand and turned off by default, so your stands cost nothing until you enable it — no skin is ever looked up for a stand you haven't switched on. To get started, give the stand a custom name that matches a Minecraft player (for example with a name tag), then flip on the "Mannequin" toggle. The stand will render as that player's character, complete with their skin, any armor you've equipped, items held in its hands or worn on its head, and the player's cape. A subtle idle animation brings it to life, and the stand still follows everything else you've set up — its pose, scale, and the "small" option all carry over.

![An armor stand rendered as a player mannequin](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/mannequin-example.png)

The master "Mannequin" toggle sits on the left, above a live face preview of the matched player and a status line. The status line either reads "Current profile:" with the player's name once the skin has loaded, or some help text reminding you to name the stand after a real player and to enable Mannequin. If the name doesn't match a real account (or the skin is still downloading), the stand simply falls back to the normal armor stand model. When a profile is matched, a "Refresh skin" button appears beneath the preview so you can immediately re-fetch that player's current skin.

On the right are the rest of the toggles. "Animations" controls the idle motion, and the next several let you show or hide each part of the skin individually — Cape, Hat, Jacket, Left and Right Sleeve, Left and Right Pants — plus "Slim arms" to force the slimmer (Alex-style) arm model. There's also an "Auto-refresh skin" toggle: it's off by default (the stand keeps the skin it first loaded), but turn it on and that stand re-fetches the player's skin every 15 minutes to keep up with skin changes, with no visible interruption — it keeps showing the current skin until the new one finishes loading.

Prefer to control mannequins globally? The config files have you covered: clients can disable mannequin rendering or its animations entirely, or turn off all automatic skin refreshing, and servers can disable the whole feature. See the [Configuration](#configuration) section for details.

![The mannequin editor tab](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/mannequin-tab.png)

Like everything else in the mod, the mannequin settings are fully editor-backed: every change supports undo/redo and copy/paste, is shared with all players, and is saved with the world. And just like the rest of the mod, it works across Fabric, NeoForge, and Forge.

> **Note:** Mannequin rendering is driven by the server over the mod's own network channel, kept deliberately separate from vanilla armor-stand data so that players without the mod can still join the server (and simply see ordinary armor stands). Two things follow from that:
>
> - **The mod must be installed on the server.** Connecting to a server that doesn't have the mod with only a client-side install won't make stands render as mannequins — they'll appear as ordinary armor stands. The mannequin settings live on the server and are sent to clients from there.
> - **Replay mods won't show mannequins.** Because the mannequin data travels over the mod's channel rather than vanilla entity data, tools that play back recorded sessions (such as Replay Mod) don't reconstruct it, so stands render as plain armor stands in replays.

## Compatibility

### Dark UI

Those of you who use a dark UI mod or resource pack (i.e. the one from Vanilla Tweaks) can get full compatibility with this mod through the included built-in resource pack. If you go to the resource packs list in-game, you'll see an entry from the Armor Stands mod for Dark UI! Enable this to get the inventory screen to match the dark UI colors.

![A screen with the Dark UI resource pack enabled](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/dark-ui-1.png)
![The Dark UI built-in resource pack in the resource pack list](https://github.com/Roundaround/mc-armor-stands/raw/main/assets/screenshots/dark-ui-2.png)

### Stylish Effects mod

If you use the [Stylish Effects mod](https://modrinth.com/mod/stylish-effects), you can add an entry to the `rederers -> renderers.inventory_renderer -> menu_blacklist` list in your `stylisheffects-client.toml` config file with the text `"armorstands:*"`. This will exclude all of the armor stands screens from showing the status widgets.

### Replay mods

Replay mods (such as [Replay Mod](https://modrinth.com/mod/replaymod)) won't show armor stands in mannequin mode — they render as ordinary armor stands in the replay. The mannequin state travels over the mod's own network channel rather than vanilla entity data (kept separate so players without the mod can still join a server running it), and replay recordings don't reconstruct that channel. Everything else about the mod is client- or world-side and is unaffected.