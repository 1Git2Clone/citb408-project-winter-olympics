package org.nbu.citb408.olympics.exception;

/** Thrown when an athlete is registered into the same competition twice. */
public class DuplicateRegistrationException extends CompetitionException {
    public DuplicateRegistrationException(String message) {
        super(message);
    }
}
