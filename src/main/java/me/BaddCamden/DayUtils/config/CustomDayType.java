package me.BaddCamden.DayUtils.config;

/**
 * Represents a configured custom day type that should repeat on an interval.
 */
public class CustomDayType {
    private final String name;
    private final long intervalTicks;

    public CustomDayType(String name, long intervalTicks) {
        this.name = name;
        this.intervalTicks = intervalTicks;
    }

    public String getName() {
        return name;
    }

    public long getIntervalTicks() {
        return intervalTicks;
    }
}
