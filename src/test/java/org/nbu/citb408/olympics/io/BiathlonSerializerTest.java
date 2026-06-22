package org.nbu.citb408.olympics.io;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nbu.citb408.olympics.competition.Biathlon;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

class BiathlonSerializerTest {

    @Test
    void roundTripsBiathlon(@TempDir Path dir) throws Exception {
        LocalDate ref = LocalDate.of(2026, 2, 1);
        Biathlon b = new Biathlon("Men's Biathlon", Gender.MALE, 18, ref);
        Athlete a = new Athlete(1, "Ivan", "BUL", Gender.MALE, LocalDate.of(1995, 5, 5));
        b.register(a);
        b.recordResult(a, 1500.0, 2); // 1500 + 120 = 1620

        Path file = dir.resolve("biathlon.ser");
        BiathlonSerializer serializer = new BiathlonSerializer();
        serializer.save(b, file);
        Biathlon restored = serializer.load(file);

        assertEquals("Men's Biathlon", restored.name());
        List<Result> ranking = restored.finalRanking();
        assertEquals(1, ranking.size());
        assertEquals("Ivan", ranking.get(0).athlete().name());
        assertEquals(1620.0, ranking.get(0).finalTime(), 1e-9);
    }
}
