package ru.euphoria.doggy.common;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Stopwatch {
    private boolean isRunning;
    private long elapsed;
    private long startTick;

    public static Stopwatch createStarted() {
        return new Stopwatch().start();
    }

    public Stopwatch() {

    }

    public Stopwatch start() {
        isRunning = true;
        startTick = System.currentTimeMillis();
        return this;
    }

    public Stopwatch stop() {
        long tick = System.currentTimeMillis();
        isRunning = false;
        elapsed += (tick - startTick);
        return this;
    }

    public Stopwatch reset() {
        elapsed = 0;
        isRunning = false;
        return this;
    }

    @Override
    public String toString() {
        long ms = elapsed();

        TimeUnit unit = chooseUnit(ms);
        double value = (double) ms / MILLISECONDS.convert(1, unit);

        // Too bad this functionality is not exposed as a regular method call
        return String.format(Locale.ROOT, "%.4g", (value)) + " " + abbreviate(unit);
    }

    private static TimeUnit chooseUnit(long ms) {
        if (DAYS.convert(ms, MILLISECONDS) > 0) {
            return DAYS;
        }
        if (HOURS.convert(ms, MILLISECONDS) > 0) {
            return HOURS;
        }
        if (MINUTES.convert(ms, MILLISECONDS) > 0) {
            return MINUTES;
        }
        if (SECONDS.convert(ms, MILLISECONDS) > 0) {
            return SECONDS;
        }
        return MILLISECONDS;
    }

    private static String abbreviate(TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "\u03bcs"; // μs
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "min";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new AssertionError();
        }
    }

    private long elapsed() {
        return isRunning ? System.currentTimeMillis() - startTick + elapsed : elapsed;
    }

}
