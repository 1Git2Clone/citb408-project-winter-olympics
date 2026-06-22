package org.nbu.citb408.olympics.competition;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

class BiathlonTest {

    private Athlete woman(int id) {
        return new Athlete(id, "W" + id, "GER", Gender.FEMALE, LocalDate.of(1996, 1, 1));
    }

    private Biathlon newBiathlon() {
        // default 60s per miss
        return new Biathlon("Women's Biathlon", Gender.FEMALE, 18, LocalDate.of(2026, 2, 1));
    }

    @Test
    void finalTimeAddsPenaltyPerMiss() throws Exception {
        Biathlon b = newBiathlon();
        b.register(woman(1));
        b.recordResult(woman(1), 1200.0, 2); // 1200 + 2*60 = 1320
        List<Result> ranking = b.finalRanking();
        assertEquals(1, ranking.size());
        assertEquals(1320.0, ranking.get(0).finalTime(), 1e-9);
    }

    @Test
    void rankingByFinalTimeAscending() throws Exception {
        Biathlon b = newBiathlon();
        b.register(woman(1));
        b.register(woman(2));
        b.recordResult(woman(1), 1200.0, 3); // 1380
        b.recordResult(woman(2), 1300.0, 0); // 1300 -> wins
        List<Result> ranking = b.finalRanking();
        assertEquals(woman(2), ranking.get(0).athlete());
        assertEquals(woman(1), ranking.get(1).athlete());
    }

    @Test
    void dnfExcludedFromRanking() throws Exception {
        Biathlon b = newBiathlon();
        b.register(woman(1));
        b.register(woman(2));
        b.recordResult(woman(1), 1200.0, 1);
        b.markDnf(woman(2));
        List<Result> ranking = b.finalRanking();
        assertEquals(1, ranking.size());
        assertEquals(woman(1), ranking.get(0).athlete());
    }

    @Test
    void customPenaltyPerMiss() throws Exception {
        Biathlon b = new Biathlon("Custom", Gender.FEMALE, 18, LocalDate.of(2026, 2, 1), 30.0);
        b.register(woman(1));
        b.recordResult(woman(1), 1000.0, 4); // 1000 + 4*30 = 1120
        assertEquals(1120.0, b.finalRanking().get(0).finalTime(), 1e-9);
    }
}
