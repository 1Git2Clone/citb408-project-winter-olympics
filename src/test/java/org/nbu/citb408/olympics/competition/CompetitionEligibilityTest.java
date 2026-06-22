package org.nbu.citb408.olympics.competition;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.nbu.citb408.olympics.exception.DuplicateRegistrationException;
import org.nbu.citb408.olympics.exception.IneligibleAthleteException;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

class CompetitionEligibilityTest {

    /** Minimal concrete subclass for testing the abstract base. */
    static final class Dummy extends Competition {
        Dummy() {
            super("Dummy", Gender.MALE, 18, LocalDate.of(2026, 2, 1));
        }
        @Override
        public List<Result> finalRanking() {
            return List.of();
        }
    }

    private Athlete male(int id, int birthYear) {
        return new Athlete(id, "A" + id, "BUL", Gender.MALE, LocalDate.of(birthYear, 1, 1));
    }

    @Test
    void rejectsWrongGender() {
        Dummy c = new Dummy();
        Athlete female = new Athlete(1, "F", "BUL", Gender.FEMALE, LocalDate.of(2000, 1, 1));
        assertThrows(IneligibleAthleteException.class, () -> c.register(female));
    }

    @Test
    void rejectsUnderageAthlete() {
        Dummy c = new Dummy();
        // Born 2010 -> age 16 as of 2026-02-01, below minimum 18.
        assertThrows(IneligibleAthleteException.class, () -> c.register(male(2, 2010)));
    }

    @Test
    void acceptsEligibleAthlete() throws Exception {
        Dummy c = new Dummy();
        c.register(male(3, 2000));
        assertEquals(1, c.athletes().size());
    }

    @Test
    void rejectsDuplicateRegistration() throws Exception {
        Dummy c = new Dummy();
        c.register(male(4, 2000));
        assertThrows(DuplicateRegistrationException.class, () -> c.register(male(4, 2000)));
    }
}
