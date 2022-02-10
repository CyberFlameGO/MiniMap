package net.pl3x.minimap.util;

import net.minecraft.world.World;
import net.pl3x.minimap.config.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Clock {
    public static final Clock INSTANCE = new Clock();

    private final Calendar calendar = new GregorianCalendar();

    private long lastChecked = 0L;
    private String time;

    private Clock() {
    }

    public String getTime(World world) {
        long now = System.currentTimeMillis();
        if (now - this.lastChecked > 500L) {
            updateTime(world);
            this.lastChecked = now;
        }
        return this.time;
    }

    private void updateTime(World world) {
        int hours, minutes;

        if (Config.getConfig().clockRealTime) {
            this.calendar.setTime(new Date());
            hours = this.calendar.get(Calendar.HOUR_OF_DAY);
            minutes = this.calendar.get(Calendar.MINUTE);
        } else {
            long daytime = world.getTimeOfDay() + 6000L;
            hours = (int) (daytime / 1000L) % 24;
            minutes = (int) ((daytime % 1000L) * 60 / 1000);
        }

        boolean pm = hours >= 12;

        this.time = Config.getConfig().clockFormat
                .replace("HH", String.format("%02d", hours))
                .replace("hh", String.valueOf((hours %= 12) == 0 ? 12 : hours))
                .replace("mm", String.format("%02d", minutes))
                .replace("a", pm ? "PM" : "AM");
    }
}
