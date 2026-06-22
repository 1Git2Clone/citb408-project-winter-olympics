package org.nbu.citb408.olympics.olympics;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.nbu.citb408.olympics.competition.Competition;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Result;

/** Aggregates competitions and computes medals and statistics. */
public final class Olympics {

    private final String name;
    private final List<Competition> competitions = new ArrayList<>();

    public Olympics(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String name() { return name; }

    public void addCompetition(Competition competition) {
        competitions.add(Objects.requireNonNull(competition, "competition"));
    }

    public List<Competition> competitions() {
        return Collections.unmodifiableList(competitions);
    }

    /** Top-3 finishers of every competition, as medalists. */
    public List<Medalist> medalists() {
        List<Medalist> all = new ArrayList<>();
        Medal[] medals = Medal.values();
        for (Competition c : competitions) {
            List<Result> ranking = c.finalRanking();
            for (int i = 0; i < ranking.size() && i < medals.length; i++) {
                all.add(new Medalist(ranking.get(i).athlete(), medals[i], c.name()));
            }
        }
        return all;
    }

    /** Country -> number of medals won. */
    public Map<String, Long> medalCountByCountry() {
        return medalists().stream()
                .collect(Collectors.groupingBy(
                        m -> m.athlete().country(),
                        LinkedHashMap::new,
                        Collectors.counting()));
    }

    /** Average age (whole years) of all distinct registered athletes. */
    public double averageAge(LocalDate asOf) {
        Set<Athlete> distinct = new HashSet<>();
        for (Competition c : competitions) {
            distinct.addAll(c.athletes());
        }
        return distinct.stream()
                .mapToInt(a -> a.age(asOf))
                .average()
                .orElse(0.0);
    }

    /** The medalist with the latest date of birth, or null if no medals awarded. */
    public Medalist youngestMedalist(LocalDate asOf) {
        return medalists().stream()
                .max(Comparator.comparing(m -> m.athlete().dateOfBirth()))
                .orElse(null);
    }
}
