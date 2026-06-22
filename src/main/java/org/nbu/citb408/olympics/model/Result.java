package org.nbu.citb408.olympics.model;

/** A single athlete's outcome within a competition. */
public interface Result {
    Athlete athlete();

    /** Final classified time in seconds (lower is better). */
    double finalTime();

    /** True if the athlete did not finish and is excluded from ranking. */
    boolean isDnf();
}
