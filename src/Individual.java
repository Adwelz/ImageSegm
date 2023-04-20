import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Individual {
    String[] genotype;
    float edgeValue;
    float connectivity;
    float overallDeviation;

    public Individual() {
        ImageUtility imgUtil = ImageUtility.getInstance();

        short[][] adjMatrix = imgUtil.genAdjMatrix();

        Random rand = new Random();

        int firstVertice = rand.nextInt(adjMatrix.length);

        int[] parent = imgUtil.prim(adjMatrix, firstVertice);

        this.genotype = imgUtil.parent2genotype(parent);

        List<List<Integer>> segments = genotype2segments(genotype);

        edgeValue = edgeValue(segments);
        connectivity = connectivity(segments);
        overallDeviation = overallDeviation(segments);
    }

    public Individual(String[] genotype) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        int width = imgUtil.getWidth();

        for(int i=0;i<genotype.length;i++){
            if(i < width && "up".equals(genotype[i])){
                genotype[i] = "none";
            }
            if(i > genotype.length - width && "down".equals(genotype[i])){
                genotype[i] = "none";
            }
            if(i % width == 0 && "left".equals(genotype[i])){
                genotype[i] = "none";
            }
            if(i % width == width - 1 && "right".equals(genotype[i])){
                genotype[i] = "none";
            }
        }

        this.genotype = genotype;

        List<List<Integer>> segments = genotype2segments(genotype);

        edgeValue = edgeValue(segments);
        connectivity = connectivity(segments);
        overallDeviation = overallDeviation(segments);
    }

    public String[] getGenotype() {
        return genotype;
    }

    public float getEdgeValue() {
        return edgeValue;
    }

    public float getConnectivity() {
        return connectivity;
    }

    public float getOverallDeviation() {
        return overallDeviation;
    }

    private List<Integer> segmentEndPoints() {
        List<Integer> endPoints = new ArrayList<>();
        for (int i = 0; i < genotype.length; i++) {
            if ("none".equals(genotype[i])) {
                endPoints.add(i);
            }
        }
        return endPoints;
    }

    private int[] genotype2parent(String[] genotype) {
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

    private List<Integer> noeudConnecte(String[] genotype, int noeud) {
        int[] parent = genotype2parent(genotype);
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

    private List<List<Integer>> genotype2segments(String[] genotype) {
        List<List<Integer>> C = new ArrayList<>();
        List<Integer> segmentEndPoints = segmentEndPoints();
        for (int i : segmentEndPoints) {
            List<Integer> segment = noeudConnecte(genotype, i);
            C.add(segment);
        }

        return C;
    }

    private float edgeValueXij(List<List<Integer>> segments, int i, int j) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        for (int k = 0; k < segments.size(); k++) {
            if ((segments.get(k).contains(i) && segments.get(k).contains(j))) {
                return 0f;
            }
        }

        return (float) imgUtil.dist(i, j);
    }

    private float edgeValue(List<List<Integer>> segments) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        int width = imgUtil.getWidth();

        float edgeValue = 0f;

        for (int i = 0; i < genotype.length; i++) {
            int[] Fi = { i + 1, i - 1, i - width, i + width, i - width + 1, i + width + 1, i - width - 1,
                    i + width - 1 };
            for (int j : Fi) {
                if(j>=0 && j<genotype.length){
                    edgeValue += edgeValueXij(segments, i, j);
                }
            }
        }

        return edgeValue;
    }

    private float connectivityXij(List<List<Integer>> segments, int i, int j, int FiValue) {
        for (int k = 0; k < segments.size(); k++) {
            if ((segments.get(k).contains(i) && segments.get(k).contains(j))) {
                return 0f;
            }
        }
        return 1f / FiValue;
    }

    private float connectivity(List<List<Integer>> segments) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        int width =imgUtil.getWidth();

        float connectivity = 0f;

        for (int i = 0; i < genotype.length; i++) {
            int[] Fi = { i + 1, i - 1, i - width, i + width, i - width + 1, i + width + 1, i - width - 1,
                    i + width - 1 };
            // int[] FiValue = {1, 2, 3, 4, 5, 6, 7, 8};
            for (int k = 0; k < Fi.length; k++) {
                if(Fi[k]>=0 && Fi[k]<genotype.length){
                    connectivity += connectivityXij(segments, i, Fi[k], k + 1);
                }
            }
        }

        return connectivity;
    }

    private int segmentCentroid(List<Integer> segment) {
        if (segment == null || segment.isEmpty()) {
            return 0;
        }
    
        int somme = 0;
        for (int valeur : segment) {
            somme += valeur;
        }
    
        return somme / segment.size();
    }

    private float overallDeviation(List<List<Integer>> segments) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        float overallDeviation = 0f;

        for (List<Integer> segment : segments) {
            int segmentMedian = segmentCentroid(segment);

            for (int i : segment) {
                overallDeviation += imgUtil.dist(i,segmentMedian);
            }
        }

        return overallDeviation;
    }

}
