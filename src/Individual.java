import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Individual {
    String[] genotype;

    public Individual() {
        ImageUtility imgUtil = ImageUtility.getInstance();

        short[][] adjMatrix = imgUtil.genAdjMatrix();

        Random rand = new Random();

        int firstVertice = rand.nextInt(adjMatrix.length);

        int[] parent = imgUtil.prim(adjMatrix, firstVertice);

        this.genotype = imgUtil.parent2genotype(parent);
    }

    public String[] getGenotype() {
        return genotype;
    }

    public static boolean containsFalse(boolean[] arr) {
        for (boolean b : arr) {
            if (!b) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> segmentEndPoints() {
        List<Integer> endPoints = new ArrayList<>();
        for (int i = 0; i < genotype.length; i++) {
            if ("none".equals(genotype[i])) {
                endPoints.add(i);
            }
        }
        return endPoints;
    }

    public int[] genotype2parent() {
        int width = ImageUtility.getInstance().getWidth();

        int n = genotype.length;
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            String direction = genotype[i];
            if (direction.equals("none")) {
                parent[i] = -1;
            } else if (direction.equals("left")) {
                parent[i] = i - 1;
            } else if (direction.equals("right")) {
                parent[i] = i + 1;
            } else if (direction.equals("up")) {
                parent[i] = i - width;
            } else if (direction.equals("down")) {
                parent[i] = i + width;
            }
        }
        return parent;
    }

    public List<Integer> noeudConnecte(int noeud) {
        int[] parent = genotype2parent();
        Map<Integer, List<Integer>> adjList = new HashMap<>();
        for (int i = 0; i < parent.length; i++) {
            if (parent[i] != -1) {
                List<Integer> voisins = adjList.getOrDefault(parent[i], new ArrayList<>());
                voisins.add(i);
                adjList.put(parent[i], voisins);
            }
        }

        List<Integer> connectes = new ArrayList<>();
        dfs(noeud, connectes, adjList);

        return connectes;
    }

    private void dfs(int node, List<Integer> connectes, Map<Integer, List<Integer>> adjList) {
        connectes.add(node);
        if (adjList.containsKey(node)) {
            for (int voisin : adjList.get(node)) {
                if (!connectes.contains(voisin)) {
                    dfs(voisin, connectes, adjList);
                }
            }
        }
    }

    public List<List<Integer>> getSegments() {
        List<List<Integer>> C = new ArrayList<>();
        List<Integer> segmentEndPoints = segmentEndPoints();
        for (int i : segmentEndPoints) {
            List<Integer> segment = noeudConnecte(i);
            C.add(segment);
        }

        return C;
    }
}
