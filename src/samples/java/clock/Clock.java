/*
 * CyberUPnP for Java
 *
 * Copyright (C) Satoshi Konno 2002
 */

package clock;

import java.util.Calendar;


/**
 * Clock.
 *
 * @author Satoshi Konno
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (nsano)
 */
public class Clock {
    private Calendar calendar;

    public Clock(Calendar cal) {
        this.calendar = cal;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    //	Time

    public int getHour() {
        return getCalendar().get(Calendar.HOUR);
    }

    public int getMinute() {
        return getCalendar().get(Calendar.MINUTE);
    }

    public int getSecond() {
        return getCalendar().get(Calendar.SECOND);
    }

    //	paint

    public final static Clock getInstance() {
        return new Clock(Calendar.getInstance());
    }

    //	getDateString

    public final static String toClockString(int value) {
        if (value < 10) {
            return "0" + Integer.toString(value);
        }
        return Integer.toString(value);
    }

    private final static String[] MONTH_STRING = {
        "Jan", "Feb", "Mar", "Apr",
        "May", "Jun", "Jul", "Aug",
        "Sep", "Oct", "Nov", "Dec",
    };

    public final static String toMonthString(int value) {
        value -= Calendar.JANUARY;
        if (0 <= value && value < 12) {
            return MONTH_STRING[value];
        }
        throw new IllegalArgumentException(String.valueOf(value));
    }

    private final static String[] WEEK_STRING = {
        "Sun", "Mon", "Tue", "Wed",
        "Thu", "Fri", "Sat",
    };

    public final static String toWeekString(int value) {
        value -= Calendar.SUNDAY;
        if ((0 <= value) && (value < 7)) {
            return WEEK_STRING[value];
        }
        return "";
    }

    public String getDateString() {
        Calendar calendar = getCalendar();
        return toWeekString(calendar.get(Calendar.DAY_OF_WEEK)) + ", " +
               toMonthString(calendar.get(Calendar.MONTH)) + " " +
               Integer.toString(calendar.get(Calendar.DATE)) + ", " +
               toClockString(calendar.get(Calendar.YEAR) % 100);
    }

    //	getTimeString
    public String getTimeString() {
        Calendar calendar = getCalendar();
        return toClockString(calendar.get(Calendar.HOUR)) +
               (((calendar.get(Calendar.SECOND) % 2) == 0) ? ":" : " ") +
               toClockString(calendar.get(Calendar.MINUTE));
    }

    //	toString
    public String toString() {
        Calendar calendar = getCalendar();
        return getDateString() + ", " + toClockString(calendar.get(Calendar.HOUR)) +
               ":" + toClockString(calendar.get(Calendar.MINUTE)) + ":" +
               toClockString(calendar.get(Calendar.SECOND));
    }
}

/* */
