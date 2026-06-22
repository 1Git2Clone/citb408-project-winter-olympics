package org.nbu.citb408.olympics;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import org.nbu.citb408.olympics.competition.Competition;
import org.nbu.citb408.olympics.io.RankingWriter;
import org.nbu.citb408.olympics.model.Result;
import org.nbu.citb408.olympics.olympics.Medalist;
import org.nbu.citb408.olympics.olympics.Olympics;

/** Interactive console menu over a populated Olympics. */
public final class ConsoleApp {

    private final Scanner scanner;
    private final Olympics olympics;

    public ConsoleApp(Scanner scanner, Olympics olympics) {
        this.scanner = scanner;
        this.olympics = olympics;
    }

    /** Runs the menu loop until the user chooses to exit. */
    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "0";
            switch (choice) {
                case "1" -> showRankings();
                case "2" -> showMedalists();
                case "3" -> showMedalsByCountry();
                case "4" -> showStatistics();
                case "5" -> exportRankings();
                case "0" -> running = false;
                default -> System.out.println("Unknown option: " + choice);
            }
        }
        System.out.println("Goodbye.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("=== " + olympics.name() + " ===");
        System.out.println("1) Show rankings");
        System.out.println("2) Show medalists");
        System.out.println("3) Show medals by country");
        System.out.println("4) Show statistics");
        System.out.println("5) Export rankings to file");
        System.out.println("0) Exit");
        System.out.print("Choose: ");
    }

    private void showRankings() {
        for (Competition c : olympics.competitions()) {
            System.out.println();
            System.out.println(c.name() + " (" + c.genderCategory() + ")");
            List<Result> ranking = c.finalRanking();
            if (ranking.isEmpty()) {
                System.out.println("  (no classified athletes)");
            }
            int place = 1;
            for (Result r : ranking) {
                System.out.printf(Locale.ROOT, "  %2d. %-28s %10.3f s%n",
                        place++, r.athlete().name(), r.finalTime());
            }
        }
    }

    private void showMedalists() {
        System.out.println();
        for (Medalist m : olympics.medalists()) {
            System.out.printf("  %-6s %-22s %s%n",
                    m.medal(), m.athlete().name(), m.competitionName());
        }
    }

    private void showMedalsByCountry() {
        System.out.println();
        Map<String, Long> counts = olympics.medalCountByCountry();
        counts.forEach((country, n) -> System.out.printf("  %-5s %d%n", country, n));
    }

    private void showStatistics() {
        System.out.println();
        System.out.printf(Locale.ROOT, "  Average participant age: %.1f%n",
                olympics.averageAge(DemoData.REFERENCE_DATE));
        Medalist youngest = olympics.youngestMedalist(DemoData.REFERENCE_DATE);
        if (youngest != null) {
            System.out.println("  Youngest medalist: " + youngest.athlete().name()
                    + " (" + youngest.athlete().country() + ")");
        } else {
            System.out.println("  Youngest medalist: none");
        }
    }

    private void exportRankings() {
        Path out = Path.of("build", "rankings.txt");
        new RankingWriter().write(olympics, out);
        System.out.println("  Rankings written to " + out.toAbsolutePath());
    }
}
