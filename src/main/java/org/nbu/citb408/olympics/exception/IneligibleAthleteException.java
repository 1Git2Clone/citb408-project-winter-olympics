package org.nbu.citb408.olympics.exception;

/** Thrown when an athlete fails the gender or minimum-age eligibility rules. */
public class IneligibleAthleteException extends CompetitionException {
    public IneligibleAthleteException(String message) {
        super(message);
    }
}
