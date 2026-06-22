package org.nbu.citb408.olympics.competition;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

/** Two-run ski slalom. Final ranking is by the sum of both run times. */
public final class SkiSlalom extends Competition {

    private static final long serialVersionUID = 1L;

    private final Map<Athlete, SkiSlalomResult> results = new LinkedHashMap<>();

    public SkiSlalom(String name, Gender genderCategory, int minAge, LocalDate referenceDate) {
        super(name, genderCategory, minAge, referenceDate);
    }

    private SkiSlalomResult resultFor(Athlete athlete) {
        if (!athletes().contains(athlete)) {
            throw new IllegalArgumentException(athlete + " is not registered in " + name());
        }
        return results.computeIfAbsent(athlete, SkiSlalomResult::new);
    }

    public void recordRun1(Athlete athlete, double seconds) {
        resultFor(athlete).setRun1(seconds);
    }

    public void recordRun2(Athlete athlete, double seconds) {
        resultFor(athlete).setRun2(seconds);
    }

    /** Top {@code n} run-1 finishers, fastest first (DNF excluded). */
    public List<Athlete> qualifiers(int n) {
        return results.values().stream()
                .filter(SkiSlalomResult::run1Finished)
                .sorted(Comparator.comparingDouble(SkiSlalomResult::run1))
                .limit(n)
                .map(SkiSlalomResult::athlete)
                .toList();
    }

    @Override
    public List<Result> finalRanking() {
        List<Result> ranked = new ArrayList<>(results.values().stream()
                .filter(r -> !r.isDnf())
                .sorted(Comparator.comparingDouble(SkiSlalomResult::finalTime))
                .map(r -> (Result) r)
                .toList());
        return ranked;
    }
}
