/**
 * Public API for hook plugins integrating with DayUtils.
 *
 * <p>Acquire the API through Bukkit services to inspect or trigger cycle state. A minimal hook plugin could look like:
 *
 * <pre>{@code
 * public final class SampleHook extends JavaPlugin {
 *     @Override
 *     public void onEnable() {
 *         DayUtilsApi api = getServer().getServicesManager().load(DayUtilsApi.class);
 *         getServer().getWorlds().stream().findFirst()
 *                 .ifPresent(world -> {
 *                     if (api.isDay(world)) {
 *                         getLogger().info(\"Day cycle is active!\");
 *                     }
 *                     api.triggerCustomDay(world, \"bloodmoon\");
 *                 });
 *     }
 * }
 * </pre>
 *
 * <p>Event listeners can subscribe to {@link me.BaddCamden.DayUtils.api.event.TimeTickEvent},
 * {@link me.BaddCamden.DayUtils.api.event.DayPhaseChangeEvent}, or
 * {@link me.BaddCamden.DayUtils.api.event.CustomDayTriggerEvent} to respond to cycle updates. Both the API and events
 * expose the number of nights passed for each world so hooks can track session progression.</p>
 */
package me.BaddCamden.DayUtils.api;
