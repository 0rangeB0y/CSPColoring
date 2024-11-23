public class Result {
    String map; // Australia or America
    String algorithm; // DFS, DFS + FC, DFS + FC + Singleton
    boolean heuristicsUsed; // true if heuristics were used
    int runNumber; // Run number
    long duration; // Duration in milliseconds
    int backtracks; // Number of backtracks

    public Result(String map, String algorithm, boolean heuristicsUsed, int runNumber, long duration, int backtracks) {
        this.map = map;
        this.algorithm = algorithm;
        this.heuristicsUsed = heuristicsUsed;
        this.runNumber = runNumber;
        this.duration = duration;
        this.backtracks = backtracks;
    }
}
