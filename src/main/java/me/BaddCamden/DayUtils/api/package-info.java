/**
 * Public API for hook plugins integrating with DayUtils.
 *
 * <p>Acquire the API through Bukkit services and register a {@link me.BaddCamden.DayUtils.api.CustomDayType}
 * to schedule or trigger custom day segments. A minimal hook plugin could look like:
 *
 * <pre>{@code
 * public final class SampleHook extends JavaPlugin {
 *     @Override
 *     public void onEnable() {
 *         DayUtilsApi api = getServer().getServicesManager().load(DayUtilsApi.class);
 *         CustomDayType harvest = new CustomDayType("harvest", "Harvest Festival");
 *         api.registerCustomDayType(harvest, 48000L);
 *
 *         // Optionally trigger immediately in the first loaded world
 *         getServer().getWorlds().stream().findFirst()
 *                 .ifPresent(world -> api.triggerCustomDay(harvest.id(), world));
 *     }
 * }
 * </pre>
 *
 * <p>Event listeners can subscribe to {@link me.BaddCamden.DayUtils.api.event.TimeTickEvent},
 * {@link me.BaddCamden.DayUtils.api.event.DayPhaseChangeEvent}, or
 * {@link me.BaddCamden.DayUtils.api.event.CustomDayTriggerEvent} to respond to cycle updates.</p>
 *
 * <p>The {@link me.BaddCamden.DayUtils.api.DayInfoService} exposed by the API can be used to
 * check whether a world is currently experiencing day, night, or a custom day type.</p>
 */
package me.BaddCamden.DayUtils.api;
