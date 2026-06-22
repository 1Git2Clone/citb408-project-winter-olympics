package org.nbu.citb408.olympics.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AthleteTest {

    @Test
    void computesAgeAsOfGivenDate() {
        Athlete a = new Athlete(1, "Marco Rossi", "ITA", Gender.MALE,
                LocalDate.of(2000, 6, 15));
        assertEquals(25, a.age(LocalDate.of(2025, 6, 15)));
        assertEquals(24, a.age(LocalDate.of(2025, 6, 14)));
    }

    @Test
    void equalsAndHashCodeByIdOnly() {
        Athlete a = new Athlete(7, "A", "BUL", Gender.FEMALE, LocalDate.of(1999, 1, 1));
        Athlete b = new Athlete(7, "Different", "GER", Gender.MALE, LocalDate.of(1990, 2, 2));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void rejectsNullName() {
        assertThrows(NullPointerException.class,
                () -> new Athlete(1, null, "ITA", Gender.MALE, LocalDate.of(2000, 1, 1)));
    }
}
