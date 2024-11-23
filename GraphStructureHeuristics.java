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

public class GraphStructureHeuristics {
  int[][] nodes;
  String[] colors;
  String[] states;
  int numberOfBacktracks;

  public GraphStructureHeuristics(int[][] nodes, String[] colors, String[] states) {
    this.nodes = nodes;
    this.colors = colors;
    this.states = states;
  }

  //    Checks if color is a valid to be assigned to the node.
  //    Returns true if valid else false
  public boolean isValidColor(int node, int[] colorMapping, int colorIndex) {
    for (int i = 0; i < nodes.length; i++) {
      if (nodes[node][i] == 1 && colorMapping[i] == colorIndex) return false;
    }
    return true;
  }

  //    Returns the adjacentRegions of a state
  List<Integer> getAdjacentRegions(int node) {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < this.nodes[node].length; i++) {
      if (nodes[node][i] == 1) list.add(i);
    }
    return list;
  }

  //    Computes the MRV Heuristic for next available state.
  public int computeMRV(Map<Integer, List<Integer>> domainMapping, int[] colorMapping) {
    int size = Integer.MAX_VALUE;
    int nextState = -1;
    for (int i = 0; i < this.nodes.length; i++) {
      if (colorMapping[i] == -1 && size > domainMapping.get(i).size()) {
        size = domainMapping.get(i).size();
        nextState = i;
      }
    }
    return nextState;
  }

  //    Computes the Degree Heuristic to compute next available state
  public int computeDegreeHeuristic(int[] colorMapping) {
    int maxDegree = -1;
    int nextState = -1;
    for (int i = 0; i < this.nodes.length; i++) {
      if (colorMapping[i] == -1 && getAdjacentRegions(i).size() > maxDegree) {
        maxDegree = getAdjacentRegions(i).size();
        nextState = i;
      }
    }
    return nextState;
  }

  //    Computes the Heuristic LCV and returns the next state
  public int computeLCV(int[] colorMapping) {
    int minimum = 0;
    int nextState = -1;
    int temp = 0;
    for (int i = 0; i < this.nodes.length; i++) {
      if (colorMapping[i] != -1) continue;
      for (int[] node : this.nodes) {
        if (node[i] == 0) {
          temp = temp + 1;
        }
      }
      if (temp > minimum) {
        minimum = temp;
        nextState = i;
      }
    }
    return nextState;
  }

  //    Returns the next state computed based on the three heuristics
  public int getNextState(Map<Integer, List<Integer>> domainMapping, int[] colorMapping) {
    int nextState = computeMRV(domainMapping, colorMapping);
    if (nextState == -1) nextState = computeDegreeHeuristic(colorMapping);
    if (nextState == -1) nextState = computeLCV(colorMapping);
    return nextState;
  }

  //    Generates the domain for all the regions.
  //    Returns the map
  public Map<Integer, List<Integer>> generateDomainMap() {
    Map<Integer, List<Integer>> domainMapping = new HashMap<>();
    for (int i = 0; i < this.nodes.length; i++) {
      List<Integer> list =
          IntStream.rangeClosed(0, colors.length - 1).boxed().collect(Collectors.toList());
      domainMapping.put(i, list);
    }
    return domainMapping;
  }

  //    Solves the coloring problem using backtracking.
  //    Returns true if successfully assigned else false.
  public Map<String, String> colorWithBacktracking(int[] totalBackTrackCount) {
    int[] colorMapping = new int[this.nodes.length];
    Arrays.fill(colorMapping, -1);
    int level = 0;
    Map<Integer, List<Integer>> domainMapping = generateDomainMap();
    if (!colorWithBacktrackingUtil(colorMapping, domainMapping, 0, level)) return null;
    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < states.length; i++) {
      map.put(states[i], colors[colorMapping[i]]);
    }
    totalBackTrackCount[0] = numberOfBacktracks;
    numberOfBacktracks = 0;
    return map;
  }

  //    This method is a helper utility function to backtrack and assign the colors to the regions.
  //    Returns true if successfully assigned else false
  public boolean colorWithBacktrackingUtil(
      int[] colorMapping, Map<Integer, List<Integer>> domainMapping, int node, int level) {
    if (level == this.nodes.length) return true;
    for (int i = 0; i < colors.length; i++) {
      if (isValidColor(node, colorMapping, i)) {
        colorMapping[node] = i;
        if (colorWithBacktrackingUtil(
            colorMapping, domainMapping, computeDegreeHeuristic(colorMapping), level + 1))
          return true;
        colorMapping[node] = -1;
      }
      numberOfBacktracks++;
    }
    return false;
  }

  //    Performs backtracking + FC to assign the hues to regions.
  //    Returns true if successfully assigned else false
  public Map<String, String> colorWithForwardCheck(int[] totalBackTrackCount) {
    int[] colorMapping = new int[this.nodes.length];
    Arrays.fill(colorMapping, -1);
    Map<Integer, List<Integer>> domainMapping = generateDomainMap();
    Map<InduceEffect, Integer> removedMap = new HashMap<>();
    int level = 0;
    if (!colorWithForwardCheckUtil(domainMapping, removedMap, colorMapping, 0, level)) return null;
    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < states.length; i++) {
      map.put(states[i], colors[colorMapping[i]]);
    }
    totalBackTrackCount[0] = numberOfBacktracks;
    numberOfBacktracks = 0;
    return map;
  }

  //    Helper utility function that does backtracking + FC
  public boolean colorWithForwardCheckUtil(
      Map<Integer, List<Integer>> domainMapping,
      Map<InduceEffect, Integer> removedMap,
      int[] colorMapping,
      int node,
      int level) {
    if (level == this.nodes.length) return true;
    for (int color : domainMapping.get(node)) {
      List<Integer> adjacentRegions = getAdjacentRegions(node);
      colorMapping[node] = color;
      int j;
      for (j = 0; j < adjacentRegions.size(); j++) {
        if (colorMapping[adjacentRegions.get(j)] == -1) {
          if (removedMap.containsKey(new InduceEffect(adjacentRegions.get(j), color))) continue;
          domainMapping.get(adjacentRegions.get(j)).remove(Integer.valueOf(color));
          removedMap.put(new InduceEffect(adjacentRegions.get(j), color), node);
          if (domainMapping.get(adjacentRegions.get(j)).isEmpty()) break;
        }
      }
      if (j == adjacentRegions.size())
        if (colorWithForwardCheckUtil(
            domainMapping,
            removedMap,
            colorMapping,
            getNextState(domainMapping, colorMapping),
            level + 1)) return true;
      numberOfBacktracks++;
      for (int neighbour : adjacentRegions) {
        if (colorMapping[neighbour] == -1) {
          if (removedMap.get(new InduceEffect(neighbour, color)) != null
              && removedMap.get(new InduceEffect(neighbour, color)) == node) {
            domainMapping.get(neighbour).add(color);
            removedMap.remove(new InduceEffect(neighbour, color));
            Collections.sort(domainMapping.get(neighbour));
          }
        }
      }
      colorMapping[node] = -1;
    }
    return false;
  }

  //    Performs backtracking + FC + singleton to assign the colors to states
  //    Returns true if successfully assigned else false
  public Map<String, String> colorWithSingleton(int[] totalBackTrackCount) {
    int[] colorMapping = new int[this.nodes.length];
    Arrays.fill(colorMapping, -1);
    int level = 0;
    Map<Integer, List<Integer>> domainMapping = generateDomainMap();
    if (!colorWithSingletonUtil(domainMapping, colorMapping, new HashSet<>(), 0, level))
      return null;
    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < states.length; i++) {
      map.put(states[i], colors[colorMapping[i]]);
    }
    totalBackTrackCount[0] = numberOfBacktracks;
    numberOfBacktracks = 0;
    return map;
  }

  //    Singleton propagation
  //    Returns true if successful propagation else false
  boolean avoidSingleton(
      int effected,
      Map<Integer, List<Integer>> domainMapping,
      Set<Integer> singletonVisited,
      int[] colorMapping) {

    int singletonColor = domainMapping.get(effected).getFirst();
    List<Integer> adjacentRegions = getAdjacentRegions(effected);
    singletonVisited.add(effected);
    for (int neighbour : adjacentRegions) {
      if (colorMapping[neighbour] != -1
          && domainMapping.get(neighbour).size() == 1
          && singletonColor == domainMapping.get(neighbour).getFirst()) return false;
      if (!singletonVisited.contains(neighbour) && colorMapping[neighbour] == -1) {
        domainMapping.get(neighbour).remove(Integer.valueOf(singletonColor));
        if (domainMapping.get(neighbour).size() == 1)
          avoidSingleton(neighbour, domainMapping, singletonVisited, colorMapping);
      }
    }
    return true;
  }

  //    This method undo the singleton propagation performed
  void undoSingleton(
      int effected,
      Map<Integer, List<Integer>> domainMapping,
      Set<Integer> singletonVisited,
      int[] colorMapping) {

    int singletonColor = domainMapping.get(effected).getFirst();
    List<Integer> adjacentRegions = getAdjacentRegions(effected);
    singletonVisited.remove(effected);
    for (int neighbour : adjacentRegions) {
      if (colorMapping[neighbour] == -1 && singletonVisited.contains(neighbour)) {
        undoSingleton(neighbour, domainMapping, singletonVisited, colorMapping);
        domainMapping.get(neighbour).add(singletonColor);
      }
    }
  }

  //    Utility function which does the backtracking + FC + Singleton
  //    Returns true if successful else false
  public boolean colorWithSingletonUtil(
      Map<Integer, List<Integer>> domainMapping,
      int[] colorMapping,
      Set<Integer> singletonVisited,
      int node,
      int level) {
    if (level == this.nodes.length) return true;
    try {
      for (int color : domainMapping.get(node)) {
        List<Integer> adjacentRegions = getAdjacentRegions(node);
        colorMapping[node] = color;
        int j;
        for (j = 0; j < adjacentRegions.size(); j++) {
          int neighbour = adjacentRegions.get(j);
          if (colorMapping[neighbour] == -1) {
            if (domainMapping.get(neighbour).size() == 1
                && domainMapping.get(neighbour).getFirst() == color) break;
            domainMapping.get(neighbour).remove(Integer.valueOf(color));
            if (domainMapping.get(neighbour).size() == 1
                && !avoidSingleton(neighbour, domainMapping, singletonVisited, colorMapping)) break;
          }
        }
        if (j == adjacentRegions.size()) {
          if (colorWithSingletonUtil(
              domainMapping,
              colorMapping,
              singletonVisited,
              getNextState(domainMapping, colorMapping),
              level + 1)) {
            return true;
          }
        }
        numberOfBacktracks++;
        for (int neighbour : adjacentRegions) {
          if (colorMapping[neighbour] == -1) {
            if (domainMapping.get(neighbour).size() == 1)
              undoSingleton(neighbour, domainMapping, singletonVisited, colorMapping);
            domainMapping.get(neighbour).add(color);
            Collections.sort(domainMapping.get(neighbour));
          }
        }
        colorMapping[node] = -1;
      }
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return false;
  }

  static class InduceEffect {

    Integer effectedNode;
    int removedDomain;

    public InduceEffect(Integer effectedNode, int removedDomain) {
      this.effectedNode = effectedNode;
      this.removedDomain = removedDomain;
    }

    @Override
    public int hashCode() {
      return effectedNode.hashCode() + removedDomain;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof GraphStructure.InduceEffect pp) {
        return (pp.effectedNode.equals(this.effectedNode)
            && pp.removedDomain == this.removedDomain);
      } else {
        return false;
      }
    }
  }
}
