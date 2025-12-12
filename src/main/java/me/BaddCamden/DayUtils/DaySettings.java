package me.BaddCamden.DayUtils;

import java.util.Collections;
import java.util.Map;

public final class DaySettings {
    private final long dayLengthTicks;
    private final long nightLengthTicks;
    private final Map<String, Long> customDayTypes;
    private final String reloadPermission;

    public DaySettings(long dayLengthTicks, long nightLengthTicks, Map<String, Long> customDayTypes, String reloadPermission) {
        this.dayLengthTicks = dayLengthTicks;
        this.nightLengthTicks = nightLengthTicks;
        this.customDayTypes = Collections.unmodifiableMap(customDayTypes);
        this.reloadPermission = reloadPermission;
    }

    public long dayLengthTicks() {
        return dayLengthTicks;
    }

    public long nightLengthTicks() {
        return nightLengthTicks;
    }

    public Map<String, Long> customDayTypes() {
        return customDayTypes;
    }

    public String reloadPermission() {
        return reloadPermission;
    }
}
