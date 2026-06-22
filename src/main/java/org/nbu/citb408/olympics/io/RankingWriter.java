package org.nbu.citb408.olympics.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.nbu.citb408.olympics.competition.Competition;
import org.nbu.citb408.olympics.model.Result;
import org.nbu.citb408.olympics.olympics.Olympics;

/** Writes the final rankings of all competitions to a text file. */
public final class RankingWriter {

    /**
     * Writes the rankings to {@code target}, creating parent directories if needed.
     *
     * @throws UncheckedIOException if writing fails
     */
    public void write(Olympics olympics, Path target) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(olympics.name()).append(" — Final Rankings ===")
          .append(System.lineSeparator()).append(System.lineSeparator());

        for (Competition c : olympics.competitions()) {
            sb.append(c.name())
              .append(" (").append(c.genderCategory()).append(")")
              .append(System.lineSeparator());
            List<Result> ranking = c.finalRanking();
            if (ranking.isEmpty()) {
                sb.append("  (no classified athletes)").append(System.lineSeparator());
            } else {
                int place = 1;
                for (Result r : ranking) {
                    sb.append(String.format(Locale.ROOT, "  %2d. %-28s %10.3f s%n",
                            place++, r.athlete().name(), r.finalTime()));
                }
            }
            sb.append(System.lineSeparator());
        }

        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.writeString(target, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write rankings to " + target, e);
        }
    }
}
