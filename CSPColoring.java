import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CSPColoring {

    public static void main(String[] args){

        String[] methods = new String[]{"DFS", "FC + DFS", "DFS + FC + Singleton"};
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter 1 for Australia and 2 for America");
        int selected = sc.nextInt();
        System.out.println("Enter 1 for Without Heuristics and 2 for With Heuristics");
        int selectedmethod = sc.nextInt();


        for(int i=0; i < 5; i++){

            int[] noOfBacktracks = new int[1];
            int[][] stateGraph;
            String[] colors;
            String[] states;
            Map<String, String> result = new HashMap<>();
            long startTime, endTime, duration;

            Map<String, Object> randomInputs;
            if(selected == 1){
                randomInputs = RandomInputs.getRandomInputsAustralia();
                stateGraph = (int[][]) randomInputs.get("adjacencyMatrix");
                states = (String[]) randomInputs.get("states");
                colors = RandomInputs.getColorsAustralia();
            }
            else {
                randomInputs = RandomInputs.getRandomInputsUS();
                stateGraph = (int[][]) randomInputs.get("adjacencyMatrix");
                states = (String[]) randomInputs.get("states");
                colors = RandomInputs.getColorsUS();
            }

            System.out.println("******************************************************");
            for(int j = 0; j < 3; j++) {
                if(selectedmethod == 1) {
                    Graph graph = new Graph(stateGraph, colors, states);

                    startTime = System.currentTimeMillis();

                    if (j == 0)
                        result = graph.backtrackColoring(noOfBacktracks);
                    else if (j == 1)
                        result = graph.forwardCheckingColoring(noOfBacktracks);
                    else
                        result = graph.forwardCheckingSingletonColoring(noOfBacktracks);

                    endTime = System.currentTimeMillis();
                    duration = (endTime - startTime);
                }
                else{
                    GraphHeuristics graph = new GraphHeuristics(stateGraph, colors, states);

                    startTime = System.currentTimeMillis();

                    if (j == 0)
                        result = graph.backtrackColoring(noOfBacktracks);
                    else if (j == 1)
                        result = graph.forwardCheckingColoring(noOfBacktracks);
                    else
                        result = graph.forwardCheckingSingletonColoring(noOfBacktracks);

                    endTime = System.currentTimeMillis();
                    duration = (endTime - startTime);
                }

                System.out.println("********** The Output of " + methods[j] + (selected == 1 ? ": Australia" : ": America") +  " **********");
                System.out.println(result);
                System.out.println("Duration of Run " + (i + 1) + " : " + duration);
                System.out.println("No of Back tracks of Run " + (i + 1) + " : " + noOfBacktracks[0]);
                System.out.println("***********************************");
            }
            System.out.println("*******************************************************");
            System.out.println("");

        }

    }
}
