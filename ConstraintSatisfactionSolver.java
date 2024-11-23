import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConstraintSatisfactionSolver {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter 1 for Australia and 2 for America:");
        int selectedMapOption = sc.nextInt();
        sc.nextLine(); // Consume the newline

        System.out.println("Enter 1 for No Heuristics and 2 for Heuristics:");
        int selectedHeuristicOption = sc.nextInt();
        sc.nextLine(); // Consume the newline

        String mapName = (selectedMapOption == 1) ? "Australia" : "America";
        boolean heuristicsUsed = (selectedHeuristicOption == 2);

        String[] methods = new String[]{"DFS", "DFS + FC", "DFS + FC + Singleton"};
        List<Result> results = new ArrayList<>();

        // Run each configuration five times
        for (int i = 0; i < 5; i++) {

            int[] numOfBacktracks = new int[1];
            int[][] stateGraph;
            String[] colors;
            String[] states;
            Map<String, String> result = new HashMap<>();
            long startTime, endTime, duration;

            Map<String, Object> randomInputs;
            if (mapName.equals("Australia")) {
                randomInputs = RandomInputs.getRandomInputsAustralia();
                stateGraph = (int[][]) randomInputs.get("adjacencyMatrix");
                states = (String[]) randomInputs.get("states");
                colors = RandomInputs.getColorsAustralia();
            } else {
                randomInputs = RandomInputs.getRandomInputsUS();
                stateGraph = (int[][]) randomInputs.get("adjacencyMatrix");
                states = (String[]) randomInputs.get("states");
                colors = RandomInputs.getColorsUS();
            }

            // Run all three algorithms
            for (int j = 0; j < 3; j++) {
                String algorithm = methods[j];

                if (!heuristicsUsed) {
                    // Without heuristics
                    Graph graph = new Graph(stateGraph, colors, states);

                    startTime = System.currentTimeMillis();

                    switch (j) {
                        case 0:
                            result = graph.backtrackColoring(numOfBacktracks);
                            break;
                        case 1:
                            result = graph.forwardCheckingColoring(numOfBacktracks);
                            break;
                        default:
                            result = graph.forwardCheckingSingletonColoring(numOfBacktracks);
                            break;
                    }

                    endTime = System.currentTimeMillis();
                    duration = (endTime - startTime);

                } else {
                    // With heuristics
                    GraphHeuristics graph = new GraphHeuristics(stateGraph, colors, states);

                    startTime = System.currentTimeMillis();

                    switch (j) {
                        case 0:
                            result = graph.backtrackColoring(numOfBacktracks);
                            break;
                        case 1:
                            result = graph.forwardCheckingColoring(numOfBacktracks);
                            break;
                        default:
                            result = graph.forwardCheckingSingletonColoring(numOfBacktracks);
                            break;
                    }

                    endTime = System.currentTimeMillis();
                    duration = (endTime - startTime);
                }

                // Add the result to the results list
                results.add(new Result(mapName, algorithm, heuristicsUsed, i + 1, duration, numOfBacktracks[0]));

                // Optional: Print the coloring result for each run
                System.out.println("Run " + (i + 1) + " - " + algorithm + ":");
                System.out.println(result);
                System.out.println("Duration: " + duration + " milliseconds");
                System.out.println("Number of Backtracks: " + numOfBacktracks[0]);
                System.out.println("--------------------------------------------------");
            }
        }

        // Print the results in a table format
        printResultsTable(results, mapName, heuristicsUsed);
    }

    // Method to print the results in a table format
    public static void printResultsTable(List<Result> results, String mapName, boolean heuristicsUsed) {
        String heuristicsText = heuristicsUsed ? "With Heuristics" : "Without Heuristics";
        System.out.println("\nResults for " + mapName + " (" + heuristicsText + ")");
        System.out.println("------------------------------------------------------------------------------------");
        System.out.printf("%-8s %-25s %-15s %-15s\n", "Run No.", "Algorithm", "Duration (ms)", "Backtracks");
        System.out.println("------------------------------------------------------------------------------------");

        // Sort the results by run number and algorithm
        results.sort(Comparator.comparingInt((Result r) -> r.runNumber).thenComparing(r -> r.algorithm));

        // Print each result
        for (Result r : results) {
            System.out.printf("%-8d %-25s %-15d %-15d\n", r.runNumber, r.algorithm, r.duration, r.backtracks);
        }
        System.out.println();
    }
}
