import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

public class Individual {
    String[] genotype;
    double edgeValue;
    double connectivity;
    double overallDeviation;
    double fitness;

    public Individual(int nbrOfSegment) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        List<List<Integer>> adjList = imgUtil.getAdjList();

        Random rand = new Random();

        int firstVertice = rand.nextInt(adjList.size());

        long startTimePrim = System.nanoTime();
        
        int[] parent = imgUtil.prim(adjList, firstVertice);

        //System.out.println(a_cycle(parent));

        long endTimePrim  = System.nanoTime();

        long totalTimePrim = endTimePrim - startTimePrim;
        System.out.println(String.format("Prim time : %s", totalTimePrim/Math.pow(10,9)));

        for(int k=0;k<nbrOfSegment-1;k++){

        double maxWeight = 0;

        int u = -1;

        for(int i=0;i<parent.length;i++){
            if(parent[i] != -1){
                if(imgUtil.dist(i,parent[i])>maxWeight){
                    maxWeight = imgUtil.dist(i,parent[i]);
                    u = i;
                }
            }
        }

        parent[u] = -1;
        }

        this.genotype = imgUtil.parent2genotype(parent);

        long startTimeGeno2segm = System.nanoTime();

        //System.out.println(a_cycle(parent));

        List<List<Integer>> segments = parent2segments(parent);

        long endTimeGeno2segm  = System.nanoTime();

        long totalTimeGeno2segm = endTimeGeno2segm - startTimeGeno2segm;
        System.out.println(String.format("Geno2segm time : %s", totalTimeGeno2segm/Math.pow(10,9)));

        long startTimeEdgeValue = System.nanoTime();
        this.edgeValue = edgeValue(segments);
        long endTimeEdgeValue  = System.nanoTime();

        long totalTimeEdgeValue = endTimeEdgeValue - startTimeEdgeValue;
        System.out.println(String.format("EdgeValue time : %s", totalTimeEdgeValue/Math.pow(10,9)));

        long startTimeConnectivity = System.nanoTime();
        this.connectivity = connectivity(segments);
        long endTimeConnectivity  = System.nanoTime();

        long totalTimeConnectivity = endTimeConnectivity - startTimeConnectivity;
        System.out.println(String.format("Connectivity time : %s", totalTimeConnectivity/Math.pow(10,9)));

        long startTimeOverallDeviation = System.nanoTime();
        this.overallDeviation = overallDeviation(segments);
        long endTimeOverallDeviation  = System.nanoTime();

        long totalTimeOverallDeviation = endTimeOverallDeviation - startTimeOverallDeviation;
        System.out.println(String.format("OverallDeviation time : %s", totalTimeOverallDeviation/Math.pow(10,9)));

        this.fitness = this.edgeValue - this.connectivity - this.overallDeviation;
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

        int[] parent = genotype2parent(genotype);

        List<List<Integer>> segments = parent2segments(parent);

        /* int sum=0;
        for(List<Integer> segment:segments){
            sum+=segment.size();
        }

        System.out.println(sum); */

        this.edgeValue = edgeValue(segments);
        this.connectivity = connectivity(segments);
        this.overallDeviation = overallDeviation(segments);
        this.fitness = this.edgeValue - this.connectivity - this.overallDeviation;
    }

    public String[] getGenotype() {
        return genotype;
    }

    public double getEdgeValue() {
        return edgeValue;
    }

    public double getConnectivity() {
        return connectivity;
    }

    public double getOverallDeviation() {
        return overallDeviation;
    }

    public double getFitness() {
        return fitness;
    }

    public static List<List<Integer>> getCycles(int[] parent) {
        int n = parent.length;
        boolean[] visited = new boolean[n];
        List<List<Integer>> cycles = new ArrayList<>();
    
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                List<Integer> cycle = new ArrayList<>();
                int j = i;
                while (j != -1 && !visited[j]) {
                    visited[j] = true;
                    cycle.add(j);
                    j = parent[j];
                }
                if (cycle.contains(j)) {
                    int start = cycle.indexOf(j);
                    List<Integer> newCycle = new ArrayList<>(cycle.subList(start, cycle.size()));
                    cycles.add(newCycle);
                }
            }
        }
    
        return cycles;
    }
    
    private Set<Integer> segmentEndPoints(int[] parent) {
        Set<Integer> endPoints = new HashSet<>();
        for (int i = 0; i < parent.length; i++) {
            if (parent[i]==-1) {
                endPoints.add(i);
            }
        }

        List<List<Integer>> cycles = getCycles(parent);

        for(List<Integer> cycle : cycles){
            endPoints.add(cycle.get(0));
        }

        return endPoints;
    }

    public static List<Integer> dfs(int[] parent, int start) {
        List<Integer> visited = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(start);
    
        while (!stack.isEmpty()) {
            int node = stack.pop();
            if (!visited.contains(node)) {
                visited.add(node);
                for (int i = 0; i < parent.length; i++) {
                    if (parent[i] == node) {
                        stack.push(i);
                    }
                }
            }
        }
    
        return visited;
    }

    private List<List<Integer>> parent2segments(int[] parent) {
        List<List<Integer>> C = new ArrayList<>();
        Set<Integer> segmentEndPoints = segmentEndPoints(parent);
        for (int i : segmentEndPoints) {
            List<Integer> segment = dfs(parent, i);
            C.add(segment);
        }

        return C;
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

    public static Map<Integer, Set<Integer>> getSegments(int[] parent) {
        int n = parent.length;
        Map<Integer, Set<Integer>> arbres = new HashMap<>();
        boolean[] visited = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                visited[i] = true;
                int p = parent[i];
                while (p != -1 && !visited[p]) {
                    visited[p] = true;
                    p = parent[p];
                }
                Set<Integer> arbre = new HashSet<>();
                int root = i;
                while (root != p) {
                    arbre.add(root);
                    root = parent[root];
                }
                arbre.add(p);
                arbres.put(p, arbre);
            }
        }
        return arbres;
    }

    public static List<List<Integer>> trouver_groupes_connexes(int[] parent) {
        int n = parent.length;
        boolean[] visited = new boolean[n];
        List<List<Integer>> groupes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                visited[i] = true;
                List<Integer> groupe = new ArrayList<>();
                groupe.add(i);
                int p = parent[i];
                while (p != -1 && !visited[p]) {
                    visited[p] = true;
                    groupe.add(p);
                    p = parent[p];
                }
                groupes.add(groupe);
            }
        }
        return groupes;
    }

    public Set<List<Integer>> getAdjacentPixels(List<List<Integer>> segments) {
        ImageUtility imgUtil = ImageUtility.getInstance();
        int width = imgUtil.getWidth();
        Set<List<Integer>> adjacentPixels = new HashSet<>();
    
        for (int i = 0; i < segments.size(); i++) {
            List<Integer> pixels1 = segments.get(i);
            for (int pixel1 : pixels1) {
                int row1 = pixel1 / width;
                int col1 = pixel1 % width;
    
                for (int j = i + 1; j < segments.size(); j++) {
                    List<Integer> pixels2 = segments.get(j);
                    for (int pixel2 : pixels2) {
                        int row2 = pixel2 / width;
                        int col2 = pixel2 % width;
    
                        if ((Math.abs(row1 - row2) <= 1 && Math.abs(col1 - col2) <= 1)) {
                            adjacentPixels.add(Arrays.asList(pixel1, pixel2));
                        }
                    }
                }
            }
        }
    
        return adjacentPixels;
    }

    private double edgeValueXij(List<List<Integer>> segments, int i, int j) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        for (int k = 0; k < segments.size(); k++) {
            if ((segments.get(k).contains(i) && segments.get(k).contains(j))) {
                return 0;
            }
        }

        return imgUtil.dist(i,j);
    }

    private double edgeValue(List<List<Integer>> segments) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        int width = imgUtil.getWidth();

        double edgeValue = 0;

        Set<List<Integer>> adjPixels =  getAdjacentPixels(segments);

        for(List<Integer> distinctsSegm:adjPixels){
            int i = distinctsSegm.get(0);
            int j = distinctsSegm.get(1);
            edgeValue += 2 * imgUtil.dist(i,j);
        }

        /* for (int i = width; i < genotype.length-width; i++) {
            if(i % width != 0 && i % width != width -1){
                int[] Fi = { i + 1, i - 1, i - width, i + width, i - width + 1, i + width + 1, i - width - 1,
                    i + width - 1 };
                for (int j : Fi) {
                    if(j>=0 && j<genotype.length){
                        edgeValue += edgeValueXij(segments, i, j);
                    }
                }
            }
        } */

        return edgeValue;
    }

    private double connectivityXij(List<List<Integer>> segments, int i, int j, int FiValue) {
        for (int k = 0; k < segments.size(); k++) {
            if ((segments.get(k).contains(i) && segments.get(k).contains(j))) {
                return 0.;
            }
        }
        return 1. / FiValue;
    }

    private double connectivity(List<List<Integer>> segments) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        int width = imgUtil.getWidth();

        double connectivity = 0.;

        Set<List<Integer>> adjPixels =  getAdjacentPixels(segments);

        for(List<Integer> distinctsSegm:adjPixels){
            connectivity += 1./4;
        }

        /* for (int i = 0; i < genotype.length; i++) {
            if(i % width != 0 && i % width != width -1){
                int[] Fi = { i + 1, i - 1, i - width, i + width, i - width + 1, i + width + 1, i - width - 1,
                    i + width - 1 };
                // int[] FiValue = {1, 2, 3, 4, 5, 6, 7, 8};
                for (int k = 0; k < Fi.length; k++) {
                    if(Fi[k]>=0 && Fi[k]<genotype.length){
                        connectivity += connectivityXij(segments, i, Fi[k], k + 1);
                    }
                }
            }
        } */

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

    private double overallDeviation(List<List<Integer>> segments) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        double overallDeviation = 0.;

        for (int i =0;i<segments.size();i++) {
            List<Integer> segment = segments.get(i);

            int segmentMedian = segmentCentroid(segments.get(i));

            for (int j : segment) {
                overallDeviation += imgUtil.dist(j,segmentMedian);
            }
        }

        return overallDeviation;
    }

    public Individual mutation(){
        String[] genotype = this.genotype.clone();

        Random rand = new Random();

        int randomVertice = rand.nextInt(genotype.length);

        String[] directions = new String[]{"none","left","right","up","down"};

        int randomIndex = rand.nextInt(directions.length);

        String randomDirection = directions[randomIndex];

        genotype[randomVertice] = randomDirection;

        return new Individual(genotype);
    }

    public Individual[] crossover(Individual other){
        String[] genotype1 = this.genotype.clone();
        String[] genotype2 = other.getGenotype().clone();

        String[] childGenotype1 = new String[genotype1.length];
        String[] childGenotype2 = new String[genotype1.length];

        Random rand = new Random();

        for(int i = 0; i < genotype1.length; i++) {
            if(rand.nextBoolean()) {
                childGenotype1[i] = genotype1[i];
                childGenotype2[i] = genotype2[i];
            } else {
                childGenotype1[i] = genotype2[i];
                childGenotype2[i] = genotype1[i];
            }
        }

        Individual child1 = new Individual(childGenotype1);
        Individual child2 = new Individual(childGenotype2);

        Individual[] childs = new Individual[2];

        childs[0] = child1;
        childs[1] = child2;

        return childs;
    }


    public boolean dominate(Individual other){
        if(this.edgeValue>other.getEdgeValue()){
            if(this.connectivity<other.getConnectivity()){
                if(this.overallDeviation<other.getOverallDeviation()){
                    return true;
                }
            }
        }
        return false;
    }


}
