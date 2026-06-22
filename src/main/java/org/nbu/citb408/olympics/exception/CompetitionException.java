package org.nbu.citb408.olympics.exception;

/** Base checked exception for domain rule violations. */
public class CompetitionException extends Exception {
    public CompetitionException(String message) {
        super(message);
    }
}
