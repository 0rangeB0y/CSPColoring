import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GraphHeuristics {
    int [][] vertices;
    String[] colors;
    String[] states;
    int numOfBacktracks;

    public GraphHeuristics(int[][] vertices, String[] colors, String[] states) {
        this.vertices = vertices;
        this.colors = colors;
        this.states = states;
    }

    class InduceEffect{

        Integer effectednode;
        int removeddomain;

        public InduceEffect(Integer effectednode, int removeddomain) {
            this.effectednode = effectednode;
            this.removeddomain = removeddomain;
        }

        @Override
        public int hashCode() {
            return effectednode.hashCode() + removeddomain;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Graph.InduceEffect pp) {
                return (pp.effectednode.equals(this.effectednode) && pp.removeddomain == this.removeddomain);
            } else {
                return false;
            }
        }
    }

    /**
     * This function if the color is a valid one to be assigned to the vertex.
     * @param vertex vertex to which the color needs to assign to
     * @param colormap colormap contains the map for assigned and unassigned colors
     * @param colorindex indicates the color
     * @return returns true if valid else false
     */
    public boolean isValidColor(int vertex, int[] colormap, int colorindex){
        for(int i =0; i < vertices.length; i++){
            if(vertices[vertex][i] == 1 && colormap[i] == colorindex)
                return false;
        }
        return true;
    }

    /**
     * This method returns the neighbors of a state
     * @param vertex vertex whose neighbors are computed
     * @return returns the list of neighbors
     */
    List<Integer> getneighbors(int vertex){
        List<Integer> list = new ArrayList<>();

        for(int i = 0; i < this.vertices[vertex].length; i++){
            if(vertices[vertex][i] == 1)
                list.add(i);
        }

        return list;
    }

    /**
     * This method computes the MRV Heuristic for next available state
     * @param domainmap states mapped to its domains
     * @param colormap colors assigned to states
     * @return returns the computed next state
     */
    public int MRV(Map<Integer, List<Integer>> domainmap, int[] colormap){
        int size = Integer.MAX_VALUE;
        int nextState = -1;

        for(int i = 0; i < this.vertices.length; i++){
            if(colormap[i] == -1 && size > domainmap.get(i).size()) {
                size = domainmap.get(i).size();
                nextState = i;
            }
        }

        return nextState;

    }

    /**
     * This method computes the Degree Heuristic to compute next available state
     * @param colormap colors assigned to states
     * @return returns the computed next state
     */
    public int DegreeHeuristic(int[] colormap){
        int maxDegree = -1;
        int nextState = -1;

        for(int i = 0; i < this.vertices.length; i++){
            if(colormap[i] == -1 && getneighbors(i).size() > maxDegree) {
                maxDegree = getneighbors(i).size();
                nextState = i;
            }
        }
        return nextState;
    }

    /**
     * Computes the Heuristic LCV and returns the next state
     * @param colormap colors assigned to states
     * @return returns the comouted next state
     */
    public int LCV(int[] colormap){
        int minimum = 0;
        int nextState = -1;
        int temp = 0;

        for(int i = 0; i < this.vertices.length; i++){
            if(colormap[i] != -1)
                continue;

            for (int[] vertice : this.vertices) {
                if (vertice[i] == 0) {
                    temp = temp + 1;
                }
            }

            if(temp > minimum){
                minimum = temp;
                nextState = i;
            }
        }

        return nextState;

    }

    /**
     * returns the next state computed based on the three heuristics
     * @param domainmap
     * @param colormap
     * @return
     */
    public int getNextState(Map<Integer, List<Integer>> domainmap, int[] colormap){
        int nextState = MRV(domainmap, colormap);
        if(nextState == -1)
            nextState = DegreeHeuristic(colormap);
        if(nextState == -1)
            nextState = LCV(colormap);
        return nextState;
    }

    /**
     * This function generates the domain for all the states
     * @return returns the map
     */
    public Map<Integer, List<Integer>> generateDomainMap(){
        Map<Integer, List<Integer>> domainmap = new HashMap<>();
        for(int i=0; i < this.vertices.length; i++){
            List<Integer> list = IntStream.rangeClosed(0, colors.length-1).boxed().collect(Collectors.toList());
            domainmap.put(i, list);
        }

        return domainmap;
    }

    /**
     * This method solves the coloring problem using backtracking
     * @param totalBackTrackCount returns the total counts of backtrack
     * @return returns true if successfully assigned else false
     */
    public Map<String, String> backtrackColoring(int[] totalBackTrackCount){
        int colormap[] = new int[this.vertices.length];
        Arrays.fill(colormap, -1);
        int level =0;
        Map<Integer, List<Integer>> domainmap = generateDomainMap();
        if(!backtrackColoringUtil(colormap, domainmap,0, level))
            return null;

        Map<String, String> map = new HashMap<>();
        for(int i=0; i < states.length; i++){
            map.put(states[i], colors[colormap[i]]);
        }

        totalBackTrackCount[0] = numOfBacktracks;
        numOfBacktracks = 0;
        return map;
    }

    /**
     * This method is a helper utlity funciton to backtrack and assign the ccolors to the states
     * @param colormap colormap contains the map for assigned and unassigned colors
     * @param vertex vertex to which the color needs to assign to
     * @return returns true if successfully assigned else false
     */
    public boolean backtrackColoringUtil(int[] colormap, Map<Integer, List<Integer>> domainmap, int vertex, int level){
        if(level == this.vertices.length)
            return true;

        for(int i = 0; i < colors.length; i++){

            if(isValidColor(vertex, colormap, i)){
                colormap[vertex] = i;
                if(backtrackColoringUtil(colormap, domainmap, DegreeHeuristic(colormap), level + 1))
                    return true;
                colormap[vertex] = -1;

            }

            numOfBacktracks++;

        }

        return false;
    }

    /**
     * This method performs backtracking + FC to assign the colors to states
     * @param totalBackTrackCount returns the total counts of backtrack
     * @return  returns true if successfully assigned else false
     */
    public Map<String, String> forwardCheckingColoring(int[] totalBackTrackCount){
        int colormap[] = new int[this.vertices.length];
        Arrays.fill(colormap, -1);

        Map<Integer, List<Integer>> domainmap = generateDomainMap();
        Map<InduceEffect, Integer> removedbyMap = new HashMap<>();

        int level = 0;
        if(!forwardCheckingColoringUtil(domainmap, removedbyMap, colormap, 0, level))
            return null;

        Map<String, String> map = new HashMap<>();
        for(int i=0; i < states.length; i++){
            map.put(states[i], colors[colormap[i]]);
        }

        totalBackTrackCount[0] = numOfBacktracks;
        numOfBacktracks = 0;
        return map;
    }

    /**
     * This method is a helper utility function that does bactracking + FC
     * @param domainmap states mapping to its domains
     * @param removedbyMap indicates which state eliminated which value from which neighbour
     * @param colormap colors assigned to states
     * @param vertex vertex to which colors needs to be assigned
     * @return
     */
    public boolean forwardCheckingColoringUtil(Map<Integer, List<Integer>> domainmap, Map<InduceEffect, Integer> removedbyMap, int[] colormap, int vertex, int level){
        if(level == this.vertices.length)
            return true;

        for (int color : domainmap.get(vertex)) {
            List<Integer> neighbors = getneighbors(vertex);
            colormap[vertex] = color;
            int j;

            for (j = 0; j < neighbors.size(); j++) {
                if (colormap[neighbors.get(j)] == -1) {
                    if(removedbyMap.containsKey(new InduceEffect(neighbors.get(j), color)))
                        continue;
                    domainmap.get(neighbors.get(j)).remove(Integer.valueOf(color));
                    removedbyMap.put(new InduceEffect(neighbors.get(j), color), vertex);
                    if (domainmap.get(neighbors.get(j)).size() == 0)
                        break;
                }
            }

            if (j == neighbors.size())
                if (forwardCheckingColoringUtil(domainmap, removedbyMap, colormap, getNextState(domainmap, colormap), level + 1))
                    return true;

            numOfBacktracks++;

            for (int neighbour : neighbors) {
                if(colormap[neighbour] == -1) {
                    if(removedbyMap.get(new InduceEffect(neighbour, color)) != null && removedbyMap.get(new InduceEffect(neighbour, color)) == vertex) {
                        domainmap.get(neighbour).add(color);
                        removedbyMap.remove(new InduceEffect(neighbour, color));
                        Collections.sort(domainmap.get(neighbour));
                    }
                }
            }

            colormap[vertex] = -1;
        }

        return false;
    }

    /**
     * This method performs backtracking + FC + singleton to assign the colors to states
     * @param totalBackTrackCount returns the total counts of backtrack
     * @return returns true if successfully assigned else false
     */
    public Map<String, String> forwardCheckingSingletonColoring(int[] totalBackTrackCount){
        int colormap[] = new int[this.vertices.length];
        Arrays.fill(colormap, -1);

        int level = 0;

        Map<Integer, List<Integer>> domainmap = generateDomainMap();
        if(!forwardCheckingSingletonColoringUtil(domainmap,colormap, new HashSet<>(),0, level))
            return null;

        Map<String, String> map = new HashMap<>();
        for(int i=0; i < states.length; i++){
            map.put(states[i], colors[colormap[i]]);
        }

        totalBackTrackCount[0] = numOfBacktracks;
        numOfBacktracks = 0;
        return map;
    }

    /**
     * This method does the singleton propogation
     * @param effected singleton state
     * @param domainmap states mapped to its domain values
     * @param singletonVisited set containing visited singleton states
     * @param colormap colors assigned to states
     * @return returns true if successful propaation else false
     */
    boolean avoidSingleton(int effected, Map<Integer, List<Integer>> domainmap, Set<Integer> singletonVisited, int[] colormap){

        int singletonColor = domainmap.get(effected).get(0);
        List<Integer> neighbors = getneighbors(effected);

        singletonVisited.add(effected);

        for(int neighbour : neighbors){
            if(colormap[neighbour] != -1 && domainmap.get(neighbour).size() == 1 && singletonColor == domainmap.get(neighbour).get(0))
                return false;

            if(!singletonVisited.contains(neighbour) && colormap[neighbour] == -1){
                domainmap.get(neighbour).remove(Integer.valueOf(singletonColor));
                if(domainmap.get(neighbour).size() == 1)
                    avoidSingleton(neighbour, domainmap, singletonVisited, colormap);
            }
        }

        return true;
    }

    /**
     * This method undo the singleton propogation performed
     * singleton state
     * @param domainmap states mapped to its domain values
     * @param singletonVisited set containing visited singleton states
     * @param colormap colors assigned to states
     */
    void undoSingleton(int effected, Map<Integer, List<Integer>> domainmap, Set<Integer> singletonVisited, int[] colormap){

        int singletonColor = domainmap.get(effected).get(0);
        List<Integer> neighbors = getneighbors(effected);

        singletonVisited.remove(effected);

        for(int neighbour : neighbors){
            if(colormap[neighbour] == -1 && singletonVisited.contains(neighbour)) {
                undoSingleton(neighbour, domainmap, singletonVisited, colormap);
                domainmap.get(neighbour).add(singletonColor);
            }
        }
    }

    /**
     * This method is a helper utlity function which does the backtracking + FC + Singleton
     * @param domainmap states mapped to its domain values
     * @param colormap colors assigned to states
     * @param singletonVisited set containing the visited singleton states
     * @param vertex vertex to which color needs to be assigned
     * @return returns true if successful else false
     */
    public boolean forwardCheckingSingletonColoringUtil(Map<Integer, List<Integer>> domainmap, int[] colormap, Set<Integer> singletonVisited, int vertex, int level){

        if(level == this.vertices.length)
            return true;

        try {

            for (int color : domainmap.get(vertex)) {
                List<Integer> neighbors  = getneighbors(vertex);
                colormap[vertex] = color;
                int j;

                for (j = 0; j < neighbors.size(); j++) {
                    int neighbour = neighbors.get(j);
                    if (colormap[neighbour] == -1) {
                        if (domainmap.get(neighbour).size() == 1 && domainmap.get(neighbour).get(0) == color)
                            break;
                        domainmap.get(neighbour).remove(Integer.valueOf(color));
                        if (domainmap.get(neighbour).size() == 1 && !avoidSingleton(neighbour, domainmap, singletonVisited, colormap))
                            break;
                    }
                }

                if (j == neighbors.size()) {
                    if (forwardCheckingSingletonColoringUtil(domainmap, colormap, singletonVisited, getNextState(domainmap, colormap), level + 1)) {
                        return true;
                    }
                }

                numOfBacktracks++;


                for (int neighbour : neighbors) {
                    if (colormap[neighbour] == -1) {
                        if (domainmap.get(neighbour).size() == 1)
                            undoSingleton(neighbour, domainmap, singletonVisited, colormap);
                        domainmap.get(neighbour).add(color);
                        Collections.sort(domainmap.get(neighbour));
                    }
                }

                colormap[vertex] = -1;
            }
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        return false;
    }
}
