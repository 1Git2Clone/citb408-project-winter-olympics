package org.nbu.citb408.olympics.olympics;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.nbu.citb408.olympics.competition.Biathlon;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;

class OlympicsTest {

    private static final LocalDate REF = LocalDate.of(2026, 2, 1);

    private Athlete woman(int id, String country, int birthYear) {
        return new Athlete(id, "W" + id, country, Gender.FEMALE, LocalDate.of(birthYear, 1, 1));
    }

    private Biathlon biathlonWith(Athlete... as) throws Exception {
        Biathlon b = new Biathlon("Women's Biathlon", Gender.FEMALE, 18, REF);
        double t = 1000.0;
        for (Athlete a : as) {
            b.register(a);
            b.recordResult(a, t, 0);
            t += 10.0; // each subsequent athlete is slower
        }
        return b;
    }

    @Test
    void medalistsAreTopThree() throws Exception {
        Athlete a1 = woman(1, "GER", 1996);
        Athlete a2 = woman(2, "BUL", 1998);
        Athlete a3 = woman(3, "ITA", 2000);
        Athlete a4 = woman(4, "FRA", 1995);
        Olympics o = new Olympics("Winter 2026");
        o.addCompetition(biathlonWith(a1, a2, a3, a4));

        List<Medalist> medalists = o.medalists();
        assertEquals(3, medalists.size());
        assertEquals(Medal.GOLD, medalists.get(0).medal());
        assertEquals(a1, medalists.get(0).athlete());
        assertEquals(Medal.SILVER, medalists.get(1).medal());
        assertEquals(a2, medalists.get(1).athlete());
        assertEquals(Medal.BRONZE, medalists.get(2).medal());
        assertEquals(a3, medalists.get(2).athlete());
    }

    @Test
    void medalCountByCountry() throws Exception {
        Olympics o = new Olympics("Winter 2026");
        o.addCompetition(biathlonWith(
                woman(1, "GER", 1996), woman(2, "GER", 1998), woman(3, "ITA", 2000)));
        Map<String, Long> counts = o.medalCountByCountry();
        assertEquals(2L, counts.get("GER"));
        assertEquals(1L, counts.get("ITA"));
    }

    @Test
    void averageAgeOverDistinctParticipants() throws Exception {
        Olympics o = new Olympics("Winter 2026");
        // born 1996 -> 30, born 2006 -> 20 ; average 25
        o.addCompetition(biathlonWith(woman(1, "GER", 1996), woman(2, "BUL", 2006)));
        assertEquals(25.0, o.averageAge(REF), 1e-9);
    }

    @Test
    void youngestMedalistHasLatestBirthDate() throws Exception {
        Athlete older = woman(1, "GER", 1990);
        Athlete youngest = woman(2, "BUL", 2004);
        Athlete mid = woman(3, "ITA", 1998);
        Olympics o = new Olympics("Winter 2026");
        // order by time: older fastest, youngest second, mid third -> all medal
        o.addCompetition(biathlonWith(older, youngest, mid));
        Medalist y = o.youngestMedalist(REF);
        assertEquals(youngest, y.athlete());
    }
}
