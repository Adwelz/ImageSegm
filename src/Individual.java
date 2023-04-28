import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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

import javax.imageio.ImageIO;

public class Individual {
    String[] genotype;
    String[] mstGenotype;
    List<List<Integer>> segments;
    double edgeValue;
    double connectivity;
    double overallDeviation;
    //int outOfSegmentRange;
    //double penalty = 10000;
    double fitness;

    public static int[] supprimer_k_plus_grand_arc(int[] parent, int k) {
        int[] parentCopy = parent.clone();

        List<int[]> arcs = new ArrayList<>();
        for (int i = 1; i < parent.length; i++) {
            int p = parentCopy[i];
            if (p != -1) {
                int[] arc = {i, p};
                arcs.add(arc);
            }
        }

        Collections.sort(arcs, (a, b) -> Double.compare(ImageUtility.getInstance().dist(b[0], b[1]), ImageUtility.getInstance().dist(a[0], a[1])));

        int[] keme_arc = arcs.remove(k-1);

        int enfant = keme_arc[0];
        parentCopy[enfant] = -1;

        return parentCopy;
    }

    public boolean hasSmallSegement(int minSize){
        for(List<Integer> segment : this.segments){
            if(segment.size()<minSize){
                return true;
            }
        }
        return false;
    }
    public boolean hasSmallSegement(List<List<Integer>> segments, int minSize){
        for(List<Integer> segment : segments){
            if(segment.size()<minSize){
                return true;
            }
        }
        return false;
    }

    public Individual(int nbrOfSegment) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        List<List<Integer>> adjList = imgUtil.getAdjList();

        Random rand = new Random();

        int firstVertice = rand.nextInt(adjList.size());

        long startTimePrim = System.nanoTime();
        
        int[] parent = imgUtil.prim(adjList, firstVertice);

        this.mstGenotype = imgUtil.parent2genotype(parent);

        //System.out.println(a_cycle(parent));

        long endTimePrim  = System.nanoTime();

        long totalTimePrim = endTimePrim - startTimePrim;
        System.out.println(String.format("Prim time : %s", totalTimePrim/Math.pow(10,9)));

        /* if(nbrOfSegment<4){
            this.outOfSegmentRange = 4 - nbrOfSegment;
        }
        else if(nbrOfSegment>41) {
            this.outOfSegmentRange = nbrOfSegment - 41;
        } */

        List<int[]> arcs = new ArrayList<>();
        for (int i = 1; i < parent.length; i++) {
            int p = parent[i];
            if (p != -1) {
                int[] arc = {i, p};
                arcs.add(arc);
            }
        }

        Collections.sort(arcs, (a, b) -> Double.compare(ImageUtility.getInstance().dist(b[0], b[1]), ImageUtility.getInstance().dist(a[0], a[1])));

        //int k=2000;

        this.segments = parent2segments(parent);

        for(int i=0;i<nbrOfSegment-1;i++){
        //int k=0;
        boolean smallSegment = true;
        while(smallSegment){
            int[] parentCopy = parent.clone();
            int[] keme_arc = arcs.remove(rand.nextInt(arcs.size()));//arcs.remove(k);
            int enfant = keme_arc[0];
            parentCopy[enfant] = -1;
            List<List<Integer>> segmentsCopy = parent2segments(parentCopy);
            smallSegment = hasSmallSegement(segmentsCopy, 100);
            if(!smallSegment){
                parent = parentCopy;
                this.segments = segmentsCopy;
            }
            //k++;
            }
        }

        /* for(int k=0;k<nbrOfSegment-1;k++){

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
        } */

        this.genotype = imgUtil.parent2genotype(parent);

        long startTimeGeno2segm = System.nanoTime();

        //System.out.println(a_cycle(parent));

        //this.segments = parent2segments(parent);

        long endTimeGeno2segm  = System.nanoTime();

        long totalTimeGeno2segm = endTimeGeno2segm - startTimeGeno2segm;
        System.out.println(String.format("Geno2segm time : %s", totalTimeGeno2segm/Math.pow(10,9)));

        Set<List<Integer>> adjPixels =  neighborPixelsNotInTheSameSegment(segments);

        long startTimeEdgeValue = System.nanoTime();
        this.edgeValue = edgeValue(adjPixels);
        long endTimeEdgeValue  = System.nanoTime();

        long totalTimeEdgeValue = endTimeEdgeValue - startTimeEdgeValue;
        System.out.println(String.format("EdgeValue time : %s", totalTimeEdgeValue/Math.pow(10,9)));

        long startTimeConnectivity = System.nanoTime();
        this.connectivity = connectivity(adjPixels);
        long endTimeConnectivity  = System.nanoTime();

        long totalTimeConnectivity = endTimeConnectivity - startTimeConnectivity;
        System.out.println(String.format("Connectivity time : %s", totalTimeConnectivity/Math.pow(10,9)));

        long startTimeOverallDeviation = System.nanoTime();
        this.overallDeviation = overallDeviation(segments);
        long endTimeOverallDeviation  = System.nanoTime();

        long totalTimeOverallDeviation = endTimeOverallDeviation - startTimeOverallDeviation;
        System.out.println(String.format("OverallDeviation time : %s", totalTimeOverallDeviation/Math.pow(10,9)));

        this.fitness = this.edgeValue  - this.connectivity - this.overallDeviation; // - outOfSegmentRange * penalty;
    }

    public Individual(String[] genotype, String[] mstGenotype) {
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

        this.mstGenotype = mstGenotype;
        this.genotype = genotype;

        int[] parent = genotype2parent(genotype);

        this.segments = parent2segments(parent);

        int nbrOfSegment = segments.size();

        /* if(nbrOfSegment<4){
            this.outOfSegmentRange = 4 - nbrOfSegment;
        }
        else if(nbrOfSegment>41) {
            this.outOfSegmentRange = nbrOfSegment - 41;
        } */

        /* int sum=0;
        for(List<Integer> segment:segments){
            sum+=segment.size();
        }

        System.out.println(sum); */

        Set<List<Integer>> adjPixels =  neighborPixelsNotInTheSameSegment(segments);

        this.edgeValue = edgeValue(adjPixels);
        this.connectivity = connectivity(adjPixels);
        this.overallDeviation = overallDeviation(segments);
        this.fitness = this.edgeValue - this.connectivity - this.overallDeviation; // - outOfSegmentRange * penalty;
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

    private List<Integer> noeudConnecte(int[] parent, int noeud) {
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

   /*  public static Map<Integer, Set<Integer>> getSegments(int[] parent) {
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
    } */

/*     public static List<List<Integer>> trouver_groupes_connexes(int[] parent) {
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
    } */

    public Set<List<Integer>> neighborPixelsNotInTheSameSegment(List<List<Integer>> segments) {
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

    public List<Integer> borderPixels(List<List<Integer>> segments) {
        ImageUtility imgUtil = ImageUtility.getInstance();
        int width = imgUtil.getWidth();
        List<Integer> borderPixels = new ArrayList<>();
    
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
    
                        if ((Math.abs(row1 - row2) == 1 && Math.abs(col1 - col2) == 0)) {
                            borderPixels.add(pixel1);
                        }
                        if ((Math.abs(row1 - row2) == 0 && Math.abs(col1 - col2) == 1)) {
                            borderPixels.add(pixel1);
                        }
                    }
                }
            }
        }
    
        return borderPixels;
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

    private double edgeValue(Set<List<Integer>> adjPixels) {
        double edgeValue = 0;

        for(List<Integer> distinctsSegm:adjPixels){
            int i = distinctsSegm.get(0);
            int j = distinctsSegm.get(1);
            edgeValue += 2 * ImageUtility.getInstance().dist(i,j);
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

    private double connectivity(Set<List<Integer>> adjPixels) {
        double connectivity = 0.;

        for(List<Integer> distinctsSegm:adjPixels){
            ImageUtility imageUtility = ImageUtility.getInstance();

            int width = imageUtility.getWidth();

            int pixel1 = distinctsSegm.get(0);
            int pixel2 = distinctsSegm.get(1);

            int row1 = pixel1 / width;
            int col1 = pixel1 % width;
            
            int row2 = pixel2 / width;
            int col2 = pixel2 % width;

            if(row1==row2){
                connectivity += 1;
                connectivity += 1./2;
            }

            if(col1==col2){
                connectivity += 1./3;
                connectivity += 1./4;
            }

            if((row2==row1+1 && col2==col1+1) || (row2==row1-1 && col2==col1-1)){
                    connectivity += 1./6;
                    connectivity += 1./7;
            }

            if((row2==row1+1 && col2==col1-1) || (row2==row1-1 && col2==col1+1)){
                    connectivity += 1./5;
                    connectivity += 1./8;
            }
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

        return new Individual(genotype, this.mstGenotype);
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

        String[] childMstGenotype1 = new String[this.mstGenotype.length];
        String[] childMstGenotype2 = new String[this.mstGenotype.length];

        if(rand.nextBoolean()) {
                childMstGenotype1 = this.mstGenotype;
                childMstGenotype2 = other.mstGenotype;
            }
        else{
                childMstGenotype2 = this.mstGenotype;
                childMstGenotype1 = other.mstGenotype;
            }

        Individual child1 = new Individual(childGenotype1, childMstGenotype1);
        Individual child2 = new Individual(childGenotype2, childMstGenotype2);

        Individual[] childs = new Individual[2];

        childs[0] = child1;
        childs[1] = child2;
        //System.out.println(child1.getGenotype()==this.getGenotype());

        return childs;
    }


    public boolean dominate(Individual other){
        if(this.edgeValue>other.getEdgeValue() && (this.connectivity<other.getConnectivity()) && (this.overallDeviation<other.getOverallDeviation())){
                    //if(this.outOfSegmentRange<other.getOutOfSegmentRange()){
                        return true;
                    //}
                
            
        }
        else{
            return false;
        }
    }

    public void createImg(String name){
        ImageUtility imageUtility = ImageUtility.getInstance();

        int width = imageUtility.getWidth();

        int height = imageUtility.getHeight();

        //int[] parent = genotype2parent(genotype);

        //List<List<Integer>> segments = parent2segments(parent);

        List<Integer> borderPixels =  borderPixels(segments);

        int[][] M = new int[height][width];

        for(int[] row : M){
            Arrays.fill(row, 255);
        }

        Arrays.fill(M[0],0);
        Arrays.fill(M[M.length-1],0);

        for(int i=1;i<M.length-1;i++){
            M[i][0] = 0;
            M[i][M[i].length-1] = 0;
        }

        for(int pixel : borderPixels){
            //int pixel1 = pixels.get(0);

            int row1 = pixel / width;
            int col1 = pixel % width;

            M[row1][col1] = 0;

            /* int pixel2 = pixels.get(1);

            int row2 = pixel2 / width;
            int col2 = pixel2 % width;

            M[row2][col2] = 0; */
        }

        /* try (PrintWriter writer = new PrintWriter(new File("matrix.csv"))) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < M.length; i++) {
                for (int j = 0; j < M[i].length; j++) {
                    sb.append(M[i][j]);
                    sb.append(",");
                }
                sb.append("\n");
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } */

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        WritableRaster raster = image.getRaster();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = M[y][x];
                raster.setSample(x, y, 0, value);
            }
        }

        try {
            ImageIO.write(image, "png", new File(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Individual hueristicSegmentsSizeDiminution(int minSize){

        int nbrOfLittleSegment = 0;
        for(List<Integer> segment : segments){
            if(segment.size()<minSize){
                nbrOfLittleSegment++;
            }
        }

        List<Integer> smallestSegment = segments.get(0);
        int min =smallestSegment.size();
        for(List<Integer> segment : segments){
            if(segment.size()<min){
                min = segment.size();
                smallestSegment = segment;
            }
        }

        Random rand = new Random();

        String[] directions = new String[]{"none","left","right","up","down"};

        String[] genotypeCopy = genotype.clone();

        for(int i : smallestSegment){
            int randomIndex = rand.nextInt(directions.length);

            String randomDirection = directions[randomIndex];

            genotypeCopy[i] = randomDirection;
        }

        Individual other = new Individual(genotypeCopy, this.mstGenotype);

        int nbrOfLittleSegmentOther = 0;
        for(List<Integer> segment : other.segments){
            if(segment.size()<minSize){
                nbrOfLittleSegmentOther++;
            }
        }
        
        if(nbrOfLittleSegmentOther<nbrOfLittleSegment){
            return other;
        }
        else{
            return this;
        }

    }

    public List<List<Integer>> getSegments() {
        return segments;
    }

    public Individual removeSmallSegments(int minSize){
        String[] genotypeCopy=this.genotype.clone();
        boolean b = false;
        for(List<Integer> segment : this.segments){
            if(segment.size()<minSize){
                b = true;
                for(int i:segment){
                    genotypeCopy[i] = this.mstGenotype[i];
                }
            }
        }
        if(b){
            return new Individual(genotypeCopy, this.mstGenotype);
        }
        else{
            return this;
        }
    }

    /* public void DeleteSmallSegements(List<List<Integer>> segments, List<List<Integer>> cycles, Set<List<Integer>> adjPixels){
        Set<Integer> movablePixels = new HashSet<>();
        for (int i = 0; i < genotype.length; i++) {
            if ("none".equals(genotype[i])) {
                movablePixels.add(i);
            }
        }
        for(List<Integer> cycle:cycles){
            movablePixels.addAll(cycle);
        }

        for(List<Integer> segment : segments){
            if(segment.size()<8){
                int pixel1 = -1;
                int pixel2 = - 1;
                for(int i : segment){
                    if(movablePixels.contains(i)){
                        for(List<Integer> pair : adjPixels){
                            if(pair.get(0)==i){
                                pixel1 = pair.get(0);
                                pixel2 = pair.get(1);
                                break;
                            }
                            else if(pair.get(1)==i){
                                pixel1 = pair.get(1);
                                pixel2 = pair.get(0);
                                break;
                            }
                        }
                        if(pixel2 !=-1 ){
                            movablePixels.remove(pixel1);
                            if(movablePixels.contains(pixel2)){
                                movablePixels.remove(pixel2);
                            }
                            if()

                            break;
                        }
                    }
                    
                }
            }
        }
    } */
}
