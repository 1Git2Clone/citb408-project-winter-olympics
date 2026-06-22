package org.nbu.citb408.olympics.competition;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

/** Biathlon: base ski time plus a fixed penalty per missed shot. */
public final class Biathlon extends Competition {

    private static final long serialVersionUID = 1L;

    /** Default penalty: one minute per missed shot. */
    public static final double DEFAULT_PENALTY_SECONDS = 60.0;

    private final double penaltyPerMissSeconds;
    private final Map<Athlete, BiathlonResult> results = new LinkedHashMap<>();

    public Biathlon(String name, Gender genderCategory, int minAge, LocalDate referenceDate) {
        this(name, genderCategory, minAge, referenceDate, DEFAULT_PENALTY_SECONDS);
    }

    public Biathlon(String name, Gender genderCategory, int minAge, LocalDate referenceDate,
                    double penaltyPerMissSeconds) {
        super(name, genderCategory, minAge, referenceDate);
        if (penaltyPerMissSeconds < 0) {
            throw new IllegalArgumentException("penalty must be >= 0");
        }
        this.penaltyPerMissSeconds = penaltyPerMissSeconds;
    }

    public double penaltyPerMissSeconds() { return penaltyPerMissSeconds; }

    private BiathlonResult resultFor(Athlete athlete) {
        if (!athletes().contains(athlete)) {
            throw new IllegalArgumentException(athlete + " is not registered in " + name());
        }
        return results.computeIfAbsent(athlete,
                a -> new BiathlonResult(a, penaltyPerMissSeconds));
    }

    public void recordResult(Athlete athlete, double baseTimeSeconds, int misses) {
        BiathlonResult r = resultFor(athlete);
        r.setBaseTime(baseTimeSeconds);
        r.setMisses(misses);
    }

    public void markDnf(Athlete athlete) {
        resultFor(athlete).markDnf();
    }

    @Override
    public List<Result> finalRanking() {
        return results.values().stream()
                .filter(r -> !r.isDnf())
                .sorted(Comparator.comparingDouble(BiathlonResult::finalTime))
                .map(r -> (Result) r)
                .toList();
    }
}
