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

public class StructureWithoutHeuristics {
  int[][] nodes;
  String[] colors;
  String[] states;
  int numberOfBacktracks;

  public StructureWithoutHeuristics(int[][] nodes, String[] colors, String[] states) {
    this.nodes = nodes;
    this.colors = colors;
    this.states = states;
  }

  //  Checks if color is valid or not
  public boolean isValidColor(int node, int[] colorMap, int colorIndex) {
    for (int i = 0; i < nodes.length; i++) {
      if (nodes[node][i] == 1 && colorMap[i] == colorIndex) return false;
    }
    return true;
  }

  //  Generates the domain for all the states and returns the map.
  public Map<Integer, List<Integer>> generateDomainMap() {
    Map<Integer, List<Integer>> domainmap = new HashMap<>();
    for (int i = 0; i < this.nodes.length; i++) {
      List<Integer> list =
          IntStream.rangeClosed(0, colors.length - 1).boxed().collect(Collectors.toList());
      domainmap.put(i, list);
    }

    return domainmap;
  }

  //  Solves the coloring problem using backtracking
  public Map<String, String> backtrackColoring(int[] totalBackTrackCount) {
    int[] colorMap = new int[this.nodes.length];
    Arrays.fill(colorMap, -1);

    if (!backtrackColoringUtil(colorMap, 0)) return null;

    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < states.length; i++) {
      map.put(states[i], colors[colorMap[i]]);
    }

    totalBackTrackCount[0] = numberOfBacktracks;
    numberOfBacktracks = 0;
    return map;
  }

  //  Helper utility function to backtrack and assign the colors to the states
  //  Returns true if successfully assigned else false

  public boolean backtrackColoringUtil(int[] colorMap, int node) {
    if (node == this.nodes.length) return true;
    for (int i = 0; i < colors.length; i++) {
      if (isValidColor(node, colorMap, i)) {
        colorMap[node] = i;
        if (backtrackColoringUtil(colorMap, node + 1)) return true;
        colorMap[node] = -1;
      }
      numberOfBacktracks++;
    }
    return false;
  }

  //  Returns the list of neighbors
  List<Integer> getNeighbors(int node) {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < this.nodes[node].length; i++) {
      if (nodes[node][i] == 1) list.add(i);
    }
    return list;
  }

  //  Performs backtracking + FC to assign the colors to states
  public Map<String, String> forwardCheckingColoring(int[] totalBackTrackCount) {
    int[] colorMap = new int[this.nodes.length];
    Arrays.fill(colorMap, -1);
    Map<Integer, List<Integer>> domainMap = generateDomainMap();
    Map<InduceEffect, Integer> removedMap = new HashMap<>();
    if (!forwardCheckingColoringUtil(domainMap, removedMap, colorMap, 0)) return null;
    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < states.length; i++) {
      map.put(states[i], colors[colorMap[i]]);
    }
    totalBackTrackCount[0] = numberOfBacktracks;
    numberOfBacktracks = 0;
    return map;
  }

  //  Helper utility function that does backtracking + FC
  public boolean forwardCheckingColoringUtil(
      Map<Integer, List<Integer>> domainMap,
      Map<InduceEffect, Integer> removedMap,
      int[] colorMap,
      int node) {
    if (node == this.nodes.length) return true;

    for (int color : domainMap.get(node)) {
      List<Integer> neighbors = getNeighbors(node);
      colorMap[node] = color;
      int j;

      for (j = 0; j < neighbors.size(); j++) {
        if (colorMap[neighbors.get(j)] == -1) {
          if (removedMap.containsKey(new InduceEffect(neighbors.get(j), color))) continue;
          domainMap.get(neighbors.get(j)).remove(Integer.valueOf(color));
          removedMap.put(new InduceEffect(neighbors.get(j), color), node);
          if (domainMap.get(neighbors.get(j)).isEmpty()) break;
        }
      }

      if (j == neighbors.size())
        if (forwardCheckingColoringUtil(domainMap, removedMap, colorMap, node + 1)) return true;

      numberOfBacktracks++;

      for (int neighbour : neighbors) {
        if (colorMap[neighbour] == -1) {
          if (removedMap.get(new InduceEffect(neighbour, color)) != null
              && removedMap.get(new InduceEffect(neighbour, color)) == node) {
            domainMap.get(neighbour).add(color);
            removedMap.remove(new InduceEffect(neighbour, color));
            Collections.sort(domainMap.get(neighbour));
          }
        }
      }

      colorMap[node] = -1;
    }

    return false;
  }

  //  This method performs backtracking + FC + singleton to assign the colors to states
  //  returns true if successfully assigned else false
  public Map<String, String> forwardCheckingSingletonColoring(int[] totalBackTrackCount) {
    int[] colorMap = new int[this.nodes.length];
    Arrays.fill(colorMap, -1);

    Map<Integer, List<Integer>> domainMap = generateDomainMap();
    if (!forwardCheckingSingletonColoringUtil(domainMap, colorMap, new HashSet<>(), 0)) return null;

    Map<String, String> map = new HashMap<>();
    for (int i = 0; i < states.length; i++) {
      map.put(states[i], colors[colorMap[i]]);
    }

    totalBackTrackCount[0] = numberOfBacktracks;
    numberOfBacktracks = 0;
    return map;
  }

  //  This method does the singleton propagation
  //  Returns true if successful propagation else false
  boolean avoidSingleton(
      int effected,
      Map<Integer, List<Integer>> domainMap,
      Set<Integer> singletonVisited,
      int[] colorMap) {

    int singletonColor = domainMap.get(effected).getFirst();
    List<Integer> neighbors = getNeighbors(effected);

    singletonVisited.add(effected);

    for (int neighbour : neighbors) {
      if (colorMap[neighbour] != -1
          && domainMap.get(neighbour).size() == 1
          && singletonColor == domainMap.get(neighbour).getFirst()) return false;

      if (!singletonVisited.contains(neighbour) && colorMap[neighbour] == -1) {
        domainMap.get(neighbour).remove(Integer.valueOf(singletonColor));
        if (domainMap.get(neighbour).size() == 1)
          avoidSingleton(neighbour, domainMap, singletonVisited, colorMap);
      }
    }
    return true;
  }

  //  Undoes the singleton propagation performed on singleton state
  void undoSingleton(
      int effected,
      Map<Integer, List<Integer>> domainMap,
      Set<Integer> singletonVisited,
      int[] colorMap) {

    int singletonColor = domainMap.get(effected).getFirst();
    List<Integer> neighbors = getNeighbors(effected);

    singletonVisited.remove(effected);

    for (int neighbour : neighbors) {
      if (colorMap[neighbour] == -1 && singletonVisited.contains(neighbour)) {
        undoSingleton(neighbour, domainMap, singletonVisited, colorMap);
        domainMap.get(neighbour).add(singletonColor);
      }
    }
  }

  //  This method is a helper utility function which does the backtracking + FC + Singleton.
  //  Returns true if successful else false
  public boolean forwardCheckingSingletonColoringUtil(
      Map<Integer, List<Integer>> domainMap,
      int[] colorMap,
      Set<Integer> singletonVisited,
      int node) {

    if (node == this.nodes.length) return true;

    try {

      for (int color : domainMap.get(node)) {
        List<Integer> neighbors = getNeighbors(node);
        colorMap[node] = color;
        int j;

        for (j = 0; j < neighbors.size(); j++) {
          int neighbour = neighbors.get(j);
          if (colorMap[neighbour] == -1) {
            if (domainMap.get(neighbour).size() == 1
                && domainMap.get(neighbour).getFirst() == color) break;
            domainMap.get(neighbour).remove(Integer.valueOf(color));
            if (domainMap.get(neighbour).size() == 1
                && !avoidSingleton(neighbour, domainMap, singletonVisited, colorMap)) break;
          }
        }

        if (j == neighbors.size()) {
          if (forwardCheckingSingletonColoringUtil(
              domainMap, colorMap, singletonVisited, node + 1)) {
            return true;
          }
        }

        numberOfBacktracks++;

        for (int neighbour : neighbors) {
          if (colorMap[neighbour] == -1) {
            if (domainMap.get(neighbour).size() == 1)
              undoSingleton(neighbour, domainMap, singletonVisited, colorMap);
            domainMap.get(neighbour).add(color);
            Collections.sort(domainMap.get(neighbour));
          }
        }

        colorMap[node] = -1;
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
      if (obj instanceof StructureWithoutHeuristics.InduceEffect pp) {
        return (pp.effectedNode.equals(this.effectedNode)
            && pp.removedDomain == this.removedDomain);
      } else {
        return false;
      }
    }
  }
}
