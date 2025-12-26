# DayUtils

DayUtils is a Paper/Spigot plugin that replaces vanilla daylight progression with a configurable scheduler. It lets you stretch or shrink day/night lengths, inject named "custom days" on repeating intervals, and expose all of that state to other plugins via a small API and rich Bukkit events.

## Highlights
- **Configurable pacing**: set independent day/night lengths and a global speed multiplier without touching gamerules yourself.
- **Named custom days**: declare recurring day types (for example `bloodmoon` every 24k ticks) that trigger Bukkit events other plugins can react to.
- **Per-tick insights**: subscribe to events that stream day/night and custom-day progress so you can sync gameplay systems to the sky clock.
- **World-safe**: DayUtils disables vanilla daylight cycling for managed worlds, persists nights-passed counts, and restores defaults when stopped.

## Installation
1. Drop the built jar into your server's `plugins/` directory.
2. Start the server; DayUtils will create `plugins/DayUtils/config.yml`.
3. Adjust settings (see below) and run `/dayutils reload` in-game or from console to apply.

## Configuration cheatsheet
- `day.length` / `day.nightLength`: Tick lengths for day and night. Vanilla is 12000/12000.
- `day.speed`: Multiplier applied to both halves (e.g., `0.5` doubles the duration).
- `day.customTypes`: Map of `{id: intervalTicks}` entries for recurring custom days (e.g., `bloodmoon: 24000`).
- `commands`: Permission nodes for each subcommand.
- `messages`: Player-facing strings used by `/dayutils`, with placeholders like `{label}`, `{world}`, `{progress}`, and `{type}`.

After editing the file, run `/dayutils reload` to apply the changes in-game.

## Usage examples

### Trigger a custom day manually
```java
DayUtilsApi api = getServer().getServicesManager().load(DayUtilsApi.class);

// Fire the configured "bloodmoon" type right now in the player's world
api.triggerCustomDay(player.getWorld(), "bloodmoon");
```

### React to the day/night switch
```java
@EventHandler
public void onPhaseChange(DayPhaseChangeEvent event) {
    if (event.getNewPhase() == DayPhaseChangeEvent.Phase.NIGHT) {
        event.getWorld().sendMessage(Component.text("Nightly bonus enabled!"));
    }
}
```

### Track fine-grained progress
```java
@EventHandler
public void onTick(TimeTickEvent event) {
    double percent = event.getCyclePercent();
    if (percent > 0.75) {
        // Last quarter of the cycle – ramp difficulty or visuals
    }
}
```

## Command reference
- `/dayutils status [world]` — Show current cycle state or active custom day.
- `/dayutils setdaylength <ticks>` — Configure daytime length.
- `/dayutils setnightlength <ticks>` — Configure nighttime length.
- `/dayutils setspeed <multiplier>` — Apply a global speed multiplier.
- `/dayutils trigger <customType> [world]` — Manually fire a registered custom day.
- `/dayutils reload` — Reloads configuration from disk.

Tab completion is available for subcommands, worlds, and current lengths.

## API surface for hook plugins
The following public types, fields, and methods are intended for other plugins to depend on. All classes live under `me.BaddCamden.DayUtils` unless noted otherwise.

### `DayUtilsApi`
- `static DayUtilsApi get()` — Access the singleton instance when already bootstrapped.
- `static DayUtilsApi bootstrap(Supplier<DayCycleManager> cycleManagerSupplier)` — Initializes the API when embedding.
- `static World world(String name)` — Convenience lookup by world name.
- `boolean isDay(World world)` / `boolean isNight(World world)` — Quick phase checks.
- `boolean isCustomDay(World world, String type)` — Determine if a world tracks progress for a custom type.
- `Double getDayPercent(World world)` / `Double getNightPercent(World world)` / `Double getCyclePercent(World world)` — Ratios of current progress (nullable if unmanaged).
- `Double getCustomDayPercent(World world, String type)` — Ratio for a specific custom day.
- `Long getNightsPassed(World world)` — Completed nights counter.
- `DaySettings getSettings()` — Current day/night configuration snapshot.
- `Map<String, CustomDayType> getCustomDayTypes()` — Unmodifiable view of configured custom days.
- `boolean triggerCustomDay(World world, String type)` — Immediately fire a custom day by id.
- `DayStatus status(World world)` / `Map<UUID, DayStatus> allStatuses()` — Aggregated status snapshots.
- `DayUtilsConfiguration configuration()` — Full configuration backing the cycle manager.

### `DayStatus`
Immutable holder returned by API methods:
- `boolean isDay()` / `boolean isNight()`
- `double getDayPercent()` / `double getNightPercent()` / `double getCyclePercent()`
- `Map<String, Double> getCustomPercent()` — Custom day progress by id.
- `DaySettings getSettings()` — Settings used to calculate the snapshot.
- `long getNightsPassed()` — Persisted night counter.

### Events (`me.BaddCamden.DayUtils.api.event`)
Register listeners with Bukkit's plugin manager.
- `TimeTickEvent` — Fired every tick with detailed progress. Useful fields: `getWorld()`, `isDay()`, `isNight()`, `getDayPercent()`, `getNightPercent()`, `getCyclePercent()`, `getCustomProgress()`, `getNightsPassed()`.
- `DayPhaseChangeEvent` — Emitted when switching between day and night. Fields: `getWorld()`, `getNewPhase()` (enum `DAY`/`NIGHT`), `getNightsPassed()`.
- `CustomDayTriggerEvent` — Raised when a `CustomDayType` reaches its interval. Fields: `getWorld()`, `getType()`.
- `CustomDayEvent` — Fired for start/end of a custom day. Fields: `world()`, `dayType()`, `phase()` (enum `START`/`END`).

### Configuration types
- `DaySettings` — Immutable lengths, speed multiplier, and custom types (`getDayLength()`, `getNightLength()`, `getSpeedMultiplier()`, `getCustomTypes()`).
- `CustomDayType` — Name and interval (`getName()`, `getIntervalTicks()`).
- `DayUtilsConfiguration` — Root configuration holder; available via `DayUtilsApi#configuration()`.

These types are stable entry points for plugin developers to extend or react to DayUtils behaviour.
