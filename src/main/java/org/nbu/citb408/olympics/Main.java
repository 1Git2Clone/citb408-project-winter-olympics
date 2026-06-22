package org.nbu.citb408.olympics;

import java.util.Arrays;
import java.util.Scanner;
import org.nbu.citb408.olympics.olympics.Olympics;

/** Entry point. Use {@code --demo} to print a demo report, otherwise run the menu. */
public final class Main {

    public static void main(String[] args) {
        Olympics olympics = DemoData.build();
        boolean demo = Arrays.asList(args).contains("--demo");

        if (demo) {
            runDemo(olympics);
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            new ConsoleApp(scanner, olympics).run();
        }
    }

    private static void runDemo(Olympics olympics) {
        System.out.println("Winter Olympics Manager \u2014 demo mode");
        olympics.competitions().forEach(c -> {
            System.out.println();
            System.out.println(c.name() + " (" + c.genderCategory() + ")");
            int place = 1;
            for (var r : c.finalRanking()) {
                System.out.printf(java.util.Locale.ROOT, "  %2d. %-28s %10.3f s%n",
                        place++, r.athlete().name(), r.finalTime());
            }
        });
        System.out.println();
        System.out.println("Medalists:");
        olympics.medalists().forEach(m ->
                System.out.printf("  %-6s %-22s %s%n",
                        m.medal(), m.athlete().name(), m.competitionName()));
    }
}
