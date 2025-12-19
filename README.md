# DayUtils

DayUtils is a Paper/Spigot plugin for customising the Minecraft day/night cycle and firing bespoke "custom day" hooks. It exposes a small API so other plugins can react to or schedule day variants.

## Configuration

The plugin ships with a `config.yml` that is created on first boot. Key sections:

- `day.length` / `day.nightLength`: Tick lengths for day and night. Vanilla is 12000/12000.
- `day.speed`: Multiplier applied to both halves (e.g. `0.5` doubles the duration).
- `day.customTypes`: Map of `{id: intervalTicks}` entries used by hook plugins to define recurring custom days (for example `bloodmoon: 24000`).
- `commands`: Permission nodes for each subcommand.
- `messages`: All player-facing strings used by `/dayutils`, with placeholders like `{label}`, `{world}`, `{progress}`, and `{type}`.

After editing the file, run `/dayutils reload` to apply the changes in-game.

## API usage

Hook plugins can depend on DayUtils and request the API via Bukkit services:

```java
DayUtilsApi api = getServer().getServicesManager()
        .load(DayUtilsApi.class);

// Trigger a custom day immediately using a configured type id
api.triggerCustomDay(world, "my-event");

boolean isDay = api.isDay(world);
```

The API types live under `me.BaddCamden.DayUtils.api` and are documented with Javadocs in the source.

## Commands

- `/dayutils status [world]` — Show current cycle state or active custom day.
- `/dayutils setdaylength <ticks>` — Configure daytime length.
- `/dayutils setnightlength <ticks>` — Configure nighttime length.
- `/dayutils setspeed <multiplier>` — Apply a global speed multiplier.
- `/dayutils trigger <customType> [world]` — Manually fire a registered custom day.
- `/dayutils reload` — Reloads configuration from disk.

Tab completion is available for subcommands, worlds, and current lengths.
