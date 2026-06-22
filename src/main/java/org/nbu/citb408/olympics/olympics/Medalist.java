package org.nbu.citb408.olympics.olympics;

import org.nbu.citb408.olympics.model.Athlete;

/** An athlete who won a medal in a named competition. */
public record Medalist(Athlete athlete, Medal medal, String competitionName) {
}
