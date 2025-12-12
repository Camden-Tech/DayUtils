package me.BaddCamden.DayUtils;

import java.util.Locale;
import java.util.Objects;
import me.BaddCamden.DayUtils.api.DayInfoService;
import org.bukkit.World;

class DayInfoServiceImpl implements DayInfoService {
    private static final long FULL_DAY_TICKS = 24000L;
    private static final long HALF_DAY_TICKS = FULL_DAY_TICKS / 2;

    private final CustomDayScheduler scheduler;

    DayInfoServiceImpl(CustomDayScheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    @Override
    public double getDayProgress(World world) {
        ActiveCustomDay active = scheduler.getActiveCustomDay(world);
        if (active != null) {
            return progressForActiveCustomDay(world, active);
        }

        if (!isDay(world)) {
            return 0.0D;
        }

        double time = normalizeTime(world);
        double elapsedDayTicks = time;
        double progress = elapsedDayTicks / HALF_DAY_TICKS;
        return clamp(progress);
    }

    @Override
    public double getNightProgress(World world) {
        ActiveCustomDay active = scheduler.getActiveCustomDay(world);
        if (active != null) {
            return progressForActiveCustomDay(world, active);
        }

        if (!isNight(world)) {
            return 0.0D;
        }

        double time = normalizeTime(world) - HALF_DAY_TICKS;
        double progress = time / HALF_DAY_TICKS;
        return clamp(progress);
    }

    @Override
    public double getFullCycleProgress(World world) {
        ActiveCustomDay active = scheduler.getActiveCustomDay(world);
        if (active != null) {
            return progressForActiveCustomDay(world, active);
        }

        double time = normalizeTime(world);
        double progress = time / FULL_DAY_TICKS;
        return clamp(progress);
    }

    @Override
    public boolean isDay(World world) {
        return scheduler.getActiveCustomDay(world) == null && normalizeTime(world) < HALF_DAY_TICKS;
    }

    @Override
    public boolean isNight(World world) {
        return scheduler.getActiveCustomDay(world) == null && normalizeTime(world) >= HALF_DAY_TICKS;
    }

    @Override
    public boolean isCustomDay(World world, String typeId) {
        Objects.requireNonNull(typeId, "typeId");
        ActiveCustomDay active = scheduler.getActiveCustomDay(world);
        return active != null
                && active.type().id().toLowerCase(Locale.ROOT).equals(typeId.toLowerCase(Locale.ROOT));
    }

    private double progressForActiveCustomDay(World world, ActiveCustomDay active) {
        long elapsed = Math.max(0L, world.getFullTime() - active.startTick());
        return clamp(elapsed / (double) Math.max(1L, active.durationTicks()));
    }

    private double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private double normalizeTime(World world) {
        return world.getTime() % FULL_DAY_TICKS;
    }
}
