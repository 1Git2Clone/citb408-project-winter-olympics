package org.nbu.citb408.olympics;

import java.time.LocalDate;
import org.nbu.citb408.olympics.competition.Biathlon;
import org.nbu.citb408.olympics.competition.SkiSlalom;
import org.nbu.citb408.olympics.competition.SkiSlalomResult;
import org.nbu.citb408.olympics.exception.CompetitionException;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.olympics.Olympics;

/** Builds a sample Olympics with realistic data for the demo and tests. */
public final class DemoData {

    public static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 2, 1);

    private DemoData() {
    }

    /** Builds a populated Olympics. Wraps domain exceptions as IllegalStateException. */
    public static Olympics build() {
        try {
            return buildInternal();
        } catch (CompetitionException e) {
            throw new IllegalStateException("Demo data is invalid", e);
        }
    }

    private static Olympics buildInternal() throws CompetitionException {
        Olympics olympics = new Olympics("Winter Olympics 2026");

        // --- Men's Ski Slalom ---
        SkiSlalom slalom = new SkiSlalom("Men's Ski Slalom", Gender.MALE, 16, REFERENCE_DATE);
        Athlete m1 = new Athlete(1, "Marco Rossi", "ITA", Gender.MALE, LocalDate.of(1998, 3, 12));
        Athlete m2 = new Athlete(2, "Lukas Mayer", "AUT", Gender.MALE, LocalDate.of(2000, 7, 5));
        Athlete m3 = new Athlete(3, "Henrik Berg", "NOR", Gender.MALE, LocalDate.of(2004, 1, 20));
        Athlete m4 = new Athlete(4, "Pierre Blanc", "FRA", Gender.MALE, LocalDate.of(1996, 11, 2));
        slalom.register(m1);
        slalom.register(m2);
        slalom.register(m3);
        slalom.register(m4);
        slalom.recordRun1(m1, 52.310);
        slalom.recordRun1(m2, 51.870);
        slalom.recordRun1(m3, 53.005);
        slalom.recordRun1(m4, SkiSlalomResult.DNF); // does not finish run 1
        slalom.recordRun2(m1, 51.900);
        slalom.recordRun2(m2, 52.450);
        slalom.recordRun2(m3, 52.800);
        olympics.addCompetition(slalom);

        // --- Women's Biathlon ---
        Biathlon biathlon = new Biathlon("Women's Biathlon", Gender.FEMALE, 18, REFERENCE_DATE);
        Athlete w1 = new Athlete(5, "Anna Schmidt", "GER", Gender.FEMALE, LocalDate.of(1995, 5, 9));
        Athlete w2 = new Athlete(6, "Elin Olsen", "SWE", Gender.FEMALE, LocalDate.of(2003, 2, 14));
        Athlete w3 = new Athlete(7, "Maja Novak", "SLO", Gender.FEMALE, LocalDate.of(1999, 8, 30));
        Athlete w4 = new Athlete(8, "Petya Ivanova", "BUL", Gender.FEMALE, LocalDate.of(2001, 4, 1));
        biathlon.register(w1);
        biathlon.register(w2);
        biathlon.register(w3);
        biathlon.register(w4);
        biathlon.recordResult(w1, 1320.500, 1);
        biathlon.recordResult(w2, 1298.250, 3);
        biathlon.recordResult(w3, 1305.750, 0);
        biathlon.markDnf(w4);
        olympics.addCompetition(biathlon);

        return olympics;
    }
}
