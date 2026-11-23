# SkinApiTest

A tiny Geyser extension for testing `GeyserConnection#sendSkin`.

All commands are Bedrock-only. `[player]` is the Bedrock or Java name of an online Geyser player and defaults to you.
The new skin is sent to every online Geyser connection, so all Bedrock players see it. Java players don't.

| Command | What it does |
|---|---|
| `/skinapitest skin <java name or uuid> [player]` | Applies a Java player's skin, fetched from Mojang, as a classic or slim model |
| `/skinapitest cone [player]` | Turns into a yellow cone |
| `/skinapitest pyramid [player]` | Turns into a white step pyramid |
| `/skinapitest sphere [player]` | Turns into a green sphere |
| `/skinapitest brainrot <sahur\|tralalero> [player]` | Turns into Tung Tung Tung Sahur (with animated eyes) or Tralalero Tralala |

The shapes are built at runtime from stacked cubes. Cones and spheres use octagonal slices.
Each shape has a solid-colour texture.

Sahur's animated eyes need skin animations, which the skin API can't send. For that one, the extension uses Geyser
internals (`AnimatedSkinSender`) to send the player list entry itself, so it depends on Geyser's `core`.

Build with `./gradlew build`. The jar is written to `build/libs/`; drop it into Geyser's `extensions` folder.
