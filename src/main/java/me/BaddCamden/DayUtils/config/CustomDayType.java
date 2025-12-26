package me.BaddCamden.DayUtils.config;

/**
 * Represents a configured custom day type that should repeat on an interval.
 */
public class CustomDayType {
    private final String name;
    private final long intervalTicks;

    /**
     * Creates a custom day definition with a name and trigger interval.
     */
    public CustomDayType(String name, long intervalTicks) {
        this.name = name;
        this.intervalTicks = intervalTicks;
    }

    /**
     * @return display name of the custom day type
     */
    public String getName() {
        return name;
    }

    /**
     * @return interval in ticks between triggers of this custom day
     */
    public long getIntervalTicks() {
        return intervalTicks;
    }
}
