package org.nbu.citb408.olympics.competition;

import java.io.Serializable;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Result;

/** Biathlon outcome: base ski time plus shooting penalties. */
public final class BiathlonResult implements Result, Serializable {

    private static final long serialVersionUID = 1L;

    private final Athlete athlete;
    private final double penaltyPerMissSeconds;
    private double baseTime = Double.NaN;
    private int misses = 0;
    private boolean dnf = false;

    public BiathlonResult(Athlete athlete, double penaltyPerMissSeconds) {
        this.athlete = athlete;
        this.penaltyPerMissSeconds = penaltyPerMissSeconds;
    }

    @Override
    public Athlete athlete() { return athlete; }

    public void setBaseTime(double seconds) { this.baseTime = seconds; }
    public void setMisses(int misses) {
        if (misses < 0) {
            throw new IllegalArgumentException("misses must be >= 0");
        }
        this.misses = misses;
    }
    public void markDnf() { this.dnf = true; }

    public double baseTime() { return baseTime; }
    public int misses() { return misses; }
    public double penaltySeconds() { return misses * penaltyPerMissSeconds; }

    @Override
    public boolean isDnf() {
        return dnf || Double.isNaN(baseTime);
    }

    @Override
    public double finalTime() {
        return baseTime + penaltySeconds();
    }
}
