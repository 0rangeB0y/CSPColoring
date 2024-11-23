import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConstraintSatisfactionSolver {

  public static void main(String[] args) {

    Scanner sc = new Scanner(System.in);
    System.out.println("Select:\n1 - Australia\n2 - United States of America:");
    int country = sc.nextInt();
    sc.nextLine(); // Consume the newline

    System.out.println("With heuristic?:\n1 - No\n2 - Yes");
    int heuristic = sc.nextInt();
    sc.nextLine(); // Consume the newline

    String mapName = (country == 1) ? "Australia" : "United States of America";
    boolean heuristicsUsed = (heuristic == 2);

    String[] methods =
        new String[] {
          "Depth First Search Only                                                       ",
          "Depth First Search + Forward Checking                                         ",
          "Depth First Search + Forward Checking + Propagation Through Singleton Domain  "
        };
    List<ExecutionResult> results = new ArrayList<>();

    // Running each configuration five times
    for (int i = 0; i < 5; i++) {

      int[] numberOfBacktracks = new int[1];
      int[][] stateGraphStructure;
      String[] colors;
      String[] states;
      Map<String, String> result;
      long startTime, endTime, duration;

      Map<String, Object> randomInputs;
      if (mapName.equals("Australia")) {
        randomInputs = RandomInputGenerator.getRandomInputGeneratorForAustralia();
        stateGraphStructure = (int[][]) randomInputs.get("adjacencyMatrix");
        states = (String[]) randomInputs.get("states");
        colors = RandomInputGenerator.getColorsAustralia();
      } else {
        randomInputs = RandomInputGenerator.getRandomInputGeneratorForUnitedStates();
        stateGraphStructure = (int[][]) randomInputs.get("adjacencyMatrix");
        states = (String[]) randomInputs.get("states");
        colors = RandomInputGenerator.getColorsUS();
      }

      // Running all three algorithms
      for (int j = 0; j < 3; j++) {
        String algorithm = methods[j];

        if (!heuristicsUsed) {
          // Without heuristics
          StructureWithoutHeuristics graph = new StructureWithoutHeuristics(stateGraphStructure, colors, states);

          startTime = System.currentTimeMillis();

          switch (j) {
            case 0:
              result = graph.backtrackColoring(numberOfBacktracks);
              break;
            case 1:
              result = graph.forwardCheckingColoring(numberOfBacktracks);
              break;
            default:
              result = graph.forwardCheckingSingletonColoring(numberOfBacktracks);
              break;
          }

        } else {
          // With heuristics
          StructureWithHeuristics graph =
              new StructureWithHeuristics(stateGraphStructure, colors, states);

          startTime = System.currentTimeMillis();

          switch (j) {
            case 0:
              result = graph.colorWithBacktracking(numberOfBacktracks);
              break;
            case 1:
              result = graph.colorWithForwardCheck(numberOfBacktracks);
              break;
            default:
              result = graph.colorWithSingleton(numberOfBacktracks);
              break;
          }
        }
        endTime = System.currentTimeMillis();
        duration = (endTime - startTime);

        // Add the result to the results list
        results.add(
            new ExecutionResult(
                mapName, algorithm, heuristicsUsed, i + 1, duration, numberOfBacktracks[0]));

        // Printing the coloring result for each run
        System.out.println("Run " + (i + 1) + " - " + algorithm + ":");
        System.out.println(result);
        System.out.println("Duration: " + duration + " milliseconds");
        System.out.println("Number of Backtracks: " + numberOfBacktracks[0]);
        System.out.println(
            "--------------------------------------------------------------------------------------------------------------");
      }
    }
    // Print the results in a table format
    printExecutionResultsTable(results, mapName, heuristicsUsed);
  }

  // Method to print the results in a table format
  public static void printExecutionResultsTable(
      List<ExecutionResult> results, String mapName, boolean heuristicsUsed) {
    String heuristicsText = heuristicsUsed ? "With Heuristics" : "Without Heuristics";
    System.out.println("\nExecutionResults for " + mapName + " (" + heuristicsText + ")");
    System.out.println(
        "--------------------------------------------------------------------------------------------------------------");
    System.out.printf(
        "%-8s %-78s %-15s %-15s\n", "Run No.", "Algorithm", "Duration (ms)", "Backtracks");
    System.out.println(
        "--------------------------------------------------------------------------------------------------------------");

    // Sort the results by run number and algorithm
    results.sort(
        Comparator.comparingInt((ExecutionResult r) -> r.runNumber)
            .thenComparing(r -> r.algorithm));

    // Print each result
    int count = 0;
    for (ExecutionResult r : results) {
      System.out.printf(
          "%-8d %-35s %-15d %-15d\n", r.runNumber, r.algorithm, r.duration, r.backtracks);
      count++;
      if (count % 3 == 0) {
        System.out.println(
            "--------------------------------------------------------------------------------------------------------------");
      }
    }
    System.out.println();
  }
}
