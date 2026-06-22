package org.nbu.citb408.olympics.competition;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.nbu.citb408.olympics.exception.DuplicateRegistrationException;
import org.nbu.citb408.olympics.exception.IneligibleAthleteException;
import org.nbu.citb408.olympics.model.Athlete;
import org.nbu.citb408.olympics.model.Gender;
import org.nbu.citb408.olympics.model.Result;

/** Base class for a single competition run for one gender category. */
public abstract class Competition implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Gender genderCategory;
    private final int minAge;
    private final LocalDate referenceDate;
    private final List<Athlete> athletes = new ArrayList<>();

    protected Competition(String name, Gender genderCategory, int minAge, LocalDate referenceDate) {
        this.name = Objects.requireNonNull(name, "name");
        this.genderCategory = Objects.requireNonNull(genderCategory, "genderCategory");
        if (minAge < 0) {
            throw new IllegalArgumentException("minAge must be >= 0");
        }
        this.minAge = minAge;
        this.referenceDate = Objects.requireNonNull(referenceDate, "referenceDate");
    }

    public String name() { return name; }
    public Gender genderCategory() { return genderCategory; }
    public int minAge() { return minAge; }
    public LocalDate referenceDate() { return referenceDate; }

    /** Unmodifiable view of registered athletes. */
    public List<Athlete> athletes() {
        return Collections.unmodifiableList(athletes);
    }

    /**
     * Registers an athlete, enforcing gender, minimum age, and uniqueness.
     *
     * @throws IneligibleAthleteException if gender mismatches or athlete is underage
     * @throws DuplicateRegistrationException if already registered
     */
    public void register(Athlete athlete)
            throws IneligibleAthleteException, DuplicateRegistrationException {
        Objects.requireNonNull(athlete, "athlete");
        if (athlete.gender() != genderCategory) {
            throw new IneligibleAthleteException(
                    athlete + " gender " + athlete.gender()
                            + " does not match category " + genderCategory);
        }
        if (athlete.age(referenceDate) < minAge) {
            throw new IneligibleAthleteException(
                    athlete + " is below minimum age " + minAge);
        }
        if (athletes.contains(athlete)) {
            throw new DuplicateRegistrationException(athlete + " is already registered");
        }
        athletes.add(athlete);
    }

    /** Final classification: finishers only, best (lowest) time first. */
    public abstract List<Result> finalRanking();
}
