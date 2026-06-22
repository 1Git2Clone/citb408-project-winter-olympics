package org.nbu.citb408.olympics.competition;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

class SkiSlalomTest {

    private Athlete man(int id) {
        return new Athlete(id, "M" + id, "BUL", Gender.MALE, LocalDate.of(1995, 1, 1));
    }

    private SkiSlalom newSlalom() {
        return new SkiSlalom("Men's Slalom", Gender.MALE, 16, LocalDate.of(2026, 2, 1));
    }

    @Test
    void qualifiersAreTopNByRun1ExcludingDnf() throws Exception {
        SkiSlalom s = newSlalom();
        s.register(man(1));
        s.register(man(2));
        s.register(man(3));
        s.recordRun1(man(1), 50.0);
        s.recordRun1(man(2), 48.0);
        s.recordRun1(man(3), SkiSlalomResult.DNF);

        List<Athlete> top2 = s.qualifiers(2);
        assertEquals(2, top2.size());
        assertEquals(man(2), top2.get(0)); // fastest first
        assertEquals(man(1), top2.get(1));
    }

    @Test
    void finalRankingByCombinedTimeExcludesUnfinished() throws Exception {
        SkiSlalom s = newSlalom();
        s.register(man(1));
        s.register(man(2));
        s.recordRun1(man(1), 50.0);
        s.recordRun1(man(2), 48.0);
        s.recordRun2(man(1), 49.0); // total 99.0
        s.recordRun2(man(2), 52.5); // total 100.5

        List<Result> ranking = s.finalRanking();
        assertEquals(2, ranking.size());
        assertEquals(man(1), ranking.get(0).athlete()); // 99.0 wins
        assertEquals(99.0, ranking.get(0).finalTime(), 1e-9);
    }

    @Test
    void dnfInRun2ExcludedFromFinalRanking() throws Exception {
        SkiSlalom s = newSlalom();
        s.register(man(1));
        s.register(man(2));
        s.recordRun1(man(1), 50.0);
        s.recordRun1(man(2), 48.0);
        s.recordRun2(man(1), 49.0);
        s.recordRun2(man(2), SkiSlalomResult.DNF);

        List<Result> ranking = s.finalRanking();
        assertEquals(1, ranking.size());
        assertEquals(man(1), ranking.get(0).athlete());
    }

    @Test
    void recordingTimeForUnregisteredAthleteThrows() {
        SkiSlalom s = newSlalom();
        assertThrows(IllegalArgumentException.class, () -> s.recordRun1(man(9), 50.0));
    }
}
