package org.nbu.citb408.olympics.competition;

import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Result;

/** Ski slalom outcome: two run times. A run time of {@code -1} marks a DNF in that run. */
public final class SkiSlalomResult implements Result {

    public static final double DNF = -1.0;

    private final Athlete athlete;
    private double run1 = Double.NaN;
    private double run2 = Double.NaN;

    public SkiSlalomResult(Athlete athlete) {
        this.athlete = athlete;
    }

    @Override
    public Athlete athlete() { return athlete; }

    public void setRun1(double seconds) { this.run1 = seconds; }
    public void setRun2(double seconds) { this.run2 = seconds; }

    public double run1() { return run1; }
    public double run2() { return run2; }

    public boolean run1Recorded() { return !Double.isNaN(run1); }
    public boolean run1Finished() { return run1Recorded() && run1 != DNF; }
    public boolean run2Finished() { return !Double.isNaN(run2) && run2 != DNF; }

    @Override
    public boolean isDnf() {
        return !run1Finished() || !run2Finished();
    }

    /** Combined time of both runs; only meaningful when {@code !isDnf()}. */
    @Override
    public double finalTime() {
        return run1 + run2;
    }
}
