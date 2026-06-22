package org.nbu.citb408.olympics.io;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nbu.citb408.olympics.competition.Biathlon;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.olympics.Olympics;

class RankingWriterTest {

    @Test
    void writesRankingsToFile(@TempDir Path dir) throws Exception {
        LocalDate ref = LocalDate.of(2026, 2, 1);
        Biathlon b = new Biathlon("Women's Biathlon", Gender.FEMALE, 18, ref);
        Athlete a1 = new Athlete(1, "Anna", "GER", Gender.FEMALE, LocalDate.of(1996, 1, 1));
        Athlete a2 = new Athlete(2, "Bea", "BUL", Gender.FEMALE, LocalDate.of(1998, 1, 1));
        b.register(a1);
        b.register(a2);
        b.recordResult(a1, 1000.0, 0); // wins
        b.recordResult(a2, 1010.0, 1); // 1070

        Olympics o = new Olympics("Winter 2026");
        o.addCompetition(b);

        Path out = dir.resolve("rankings.txt");
        new RankingWriter().write(o, out);

        String content = Files.readString(out);
        assertTrue(content.contains("Women's Biathlon"), content);
        assertTrue(content.contains("Anna"), content);
        assertTrue(content.contains("1000.000"), content); // 3-decimal formatting
        // Anna (winner) appears before Bea
        assertTrue(content.indexOf("Anna") < content.indexOf("Bea"), content);
    }
}
