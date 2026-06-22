package org.nbu.citb408.olympics.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

/** Immutable athlete record. Equality is by {@code id}. */
public final class Athlete implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String country;
    private final Gender gender;
    private final LocalDate dateOfBirth;

    public Athlete(int id, String name, String country, Gender gender, LocalDate dateOfBirth) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.country = Objects.requireNonNull(country, "country");
        this.gender = Objects.requireNonNull(gender, "gender");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "dateOfBirth");
    }

    public int id() { return id; }
    public String name() { return name; }
    public String country() { return country; }
    public Gender gender() { return gender; }
    public LocalDate dateOfBirth() { return dateOfBirth; }

    /** Age in whole years as of {@code asOf}. */
    public int age(LocalDate asOf) {
        return Period.between(dateOfBirth, asOf).getYears();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Athlete other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "#" + id + " " + name + " (" + country + ")";
    }
}
