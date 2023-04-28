import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

public class Individual {
    String[] genotype;
    String[] mstGenotype;
    List<List<Integer>> segments;
    double edgeValue;
    double connectivity;
    double overallDeviation;
    double fitness;

    public Individual(int nbrOfSegment, int minSize) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        List<List<Integer>> adjList = imgUtil.getAdjList();

        Random rand = new Random();

        int firstVertice = rand.nextInt(adjList.size());

        long startTimePrim = System.nanoTime();

        int[] parent = imgUtil.prim(adjList, firstVertice);

        this.mstGenotype = imgUtil.parent2genotype(parent);

        long endTimePrim = System.nanoTime();

        long totalTimePrim = endTimePrim - startTimePrim;
        System.out.println(String.format("Prim time : %s", totalTimePrim / Math.pow(10, 9)));

        List<int[]> arcs = new ArrayList<>();
        for (int i = 1; i < parent.length; i++) {
            int p = parent[i];
            if (p != -1) {
                int[] arc = { i, p };
                arcs.add(arc);
            }
        }

        Collections.sort(arcs, (a, b) -> Double.compare(ImageUtility.getInstance().dist(b[0], b[1]),
                ImageUtility.getInstance().dist(a[0], a[1])));

        long startTimeGeno2segm = System.nanoTime();

        this.segments = parent2segments(parent);

        long endTimeGeno2segm = System.nanoTime();

        long totalTimeGeno2segm = endTimeGeno2segm - startTimeGeno2segm;
        System.out.println(String.format("Geno2segm time : %s", totalTimeGeno2segm / Math.pow(10, 9)));

        for (int i = 0; i < nbrOfSegment - 1; i++) {
            boolean smallSegment = true;
            while (smallSegment) {
                int[] parentCopy = parent.clone();
                int[] keme_arc = arcs.remove(rand.nextInt(arcs.size()));
                int enfant = keme_arc[0];
                parentCopy[enfant] = -1;
                List<List<Integer>> segmentsCopy = parent2segments(parentCopy);
                smallSegment = hasSmallSegement(segmentsCopy, minSize);
                if (!smallSegment) {
                    parent = parentCopy;
                    this.segments = segmentsCopy;
                }
            }
        }

        this.genotype = imgUtil.parent2genotype(parent);

        Set<List<Integer>> adjPixels = neighborPixelsNotInTheSameSegment(segments);

        long startTimeEdgeValue = System.nanoTime();
        this.edgeValue = edgeValue(adjPixels);
        long endTimeEdgeValue = System.nanoTime();

        long totalTimeEdgeValue = endTimeEdgeValue - startTimeEdgeValue;
        System.out.println(String.format("EdgeValue time : %s", totalTimeEdgeValue / Math.pow(10, 9)));

        long startTimeConnectivity = System.nanoTime();
        this.connectivity = connectivity(adjPixels);
        long endTimeConnectivity = System.nanoTime();

        long totalTimeConnectivity = endTimeConnectivity - startTimeConnectivity;
        System.out.println(String.format("Connectivity time : %s", totalTimeConnectivity / Math.pow(10, 9)));

        long startTimeOverallDeviation = System.nanoTime();
        this.overallDeviation = overallDeviation(segments);
        long endTimeOverallDeviation = System.nanoTime();

        long totalTimeOverallDeviation = endTimeOverallDeviation - startTimeOverallDeviation;
        System.out.println(String.format("OverallDeviation time : %s", totalTimeOverallDeviation / Math.pow(10, 9)));

        this.fitness = this.edgeValue - this.connectivity - this.overallDeviation;
    }

    public Individual(String[] genotype, String[] mstGenotype) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        int width = imgUtil.getWidth();

        for (int i = 0; i < genotype.length; i++) {
            if (i < width && "up".equals(genotype[i])) {
                genotype[i] = "none";
            }
            if (i > genotype.length - width && "down".equals(genotype[i])) {
                genotype[i] = "none";
            }
            if (i % width == 0 && "left".equals(genotype[i])) {
                genotype[i] = "none";
            }
            if (i % width == width - 1 && "right".equals(genotype[i])) {
                genotype[i] = "none";
            }
        }

        this.mstGenotype = mstGenotype;
        this.genotype = genotype;

        int[] parent = genotype2parent(genotype);

        this.segments = parent2segments(parent);

        Set<List<Integer>> adjPixels = neighborPixelsNotInTheSameSegment(segments);

        this.edgeValue = edgeValue(adjPixels);
        this.connectivity = connectivity(adjPixels);
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

    public List<List<Integer>> getSegments() {
        return segments;
    }

    public double getFitness() {
        return fitness;
    }

    public boolean hasSmallSegement(int minSize) {
        for (List<Integer> segment : this.segments) {
            if (segment.size() < minSize) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSmallSegement(List<List<Integer>> segments, int minSize) {
        for (List<Integer> segment : segments) {
            if (segment.size() < minSize) {
                return true;
            }
        }
        return false;
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
            if (parent[i] == -1) {
                endPoints.add(i);
            }
        }

        List<List<Integer>> cycles = getCycles(parent);

        for (List<Integer> cycle : cycles) {
            endPoints.add(cycle.get(0));
        }

        return endPoints;
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
        List<List<Integer>> c = new ArrayList<>();
        Set<Integer> segmentEndPoints = segmentEndPoints(parent);
        for (int i : segmentEndPoints) {
            List<Integer> segment = noeudConnecte(parent, i);
            c.add(segment);
        }

        return c;
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

    private double edgeValue(Set<List<Integer>> adjPixels) {
        this.edgeValue = 0;

        for (List<Integer> distinctsSegm : adjPixels) {
            int i = distinctsSegm.get(0);
            int j = distinctsSegm.get(1);
            edgeValue += 2 * ImageUtility.getInstance().dist(i, j);
        }

        return edgeValue;
    }

    private double connectivity(Set<List<Integer>> adjPixels) {
        this.connectivity = 0.;

        for (List<Integer> distinctsSegm : adjPixels) {
            ImageUtility imageUtility = ImageUtility.getInstance();

            int width = imageUtility.getWidth();

            int pixel1 = distinctsSegm.get(0);
            int pixel2 = distinctsSegm.get(1);

            int row1 = pixel1 / width;
            int col1 = pixel1 % width;

            int row2 = pixel2 / width;
            int col2 = pixel2 % width;

            if (row1 == row2) {
                connectivity += 1;
                connectivity += 1. / 2;
            }

            if (col1 == col2) {
                connectivity += 1. / 3;
                connectivity += 1. / 4;
            }

            if ((row2 == row1 + 1 && col2 == col1 + 1) || (row2 == row1 - 1 && col2 == col1 - 1)) {
                connectivity += 1. / 6;
                connectivity += 1. / 7;
            }

            if ((row2 == row1 + 1 && col2 == col1 - 1) || (row2 == row1 - 1 && col2 == col1 + 1)) {
                connectivity += 1. / 5;
                connectivity += 1. / 8;
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

    private double overallDeviation(List<List<Integer>> segments) {
        ImageUtility imgUtil = ImageUtility.getInstance();

        this.overallDeviation = 0.;

        for (int i = 0; i < segments.size(); i++) {
            List<Integer> segment = segments.get(i);

            int segmentMedian = segmentCentroid(segments.get(i));

            for (int j : segment) {
                overallDeviation += imgUtil.dist(j, segmentMedian);
            }
        }

        return overallDeviation;
    }

    public Individual mutation() {
        String[] genotypeCopy = this.genotype.clone();

        Random rand = new Random();

        int randomVertice = rand.nextInt(genotypeCopy.length);

        String[] directions = new String[] { "none", "left", "right", "up", "down" };

        int randomIndex = rand.nextInt(directions.length);

        String randomDirection = directions[randomIndex];

        genotypeCopy[randomVertice] = randomDirection;

        return new Individual(genotypeCopy, this.mstGenotype);
    }

    public Individual[] crossover(Individual other) {
        String[] genotype1 = this.genotype.clone();
        String[] genotype2 = other.getGenotype().clone();

        String[] childGenotype1 = new String[genotype1.length];
        String[] childGenotype2 = new String[genotype1.length];

        Random rand = new Random();

        for (int i = 0; i < genotype1.length; i++) {
            if (rand.nextBoolean()) {
                childGenotype1[i] = genotype1[i];
                childGenotype2[i] = genotype2[i];
            } else {
                childGenotype1[i] = genotype2[i];
                childGenotype2[i] = genotype1[i];
            }
        }

        String[] childMstGenotype1;
        String[] childMstGenotype2;

        if (rand.nextBoolean()) {
            childMstGenotype1 = this.mstGenotype;
            childMstGenotype2 = other.mstGenotype;
        } else {
            childMstGenotype2 = this.mstGenotype;
            childMstGenotype1 = other.mstGenotype;
        }

        Individual child1 = new Individual(childGenotype1, childMstGenotype1);
        Individual child2 = new Individual(childGenotype2, childMstGenotype2);

        Individual[] childs = new Individual[2];

        childs[0] = child1;
        childs[1] = child2;

        return childs;
    }

    public boolean dominate(Individual other) {
        if (this.edgeValue > other.getEdgeValue() && (this.connectivity < other.getConnectivity())
                && (this.overallDeviation < other.getOverallDeviation())) {
            return true;
        } else {
            return false;
        }
    }

    public void createImgs(String name) {
        ImageUtility imageUtility = ImageUtility.getInstance();

        int width = imageUtility.getWidth();

        int height = imageUtility.getHeight();

        List<Integer> borderPixels = borderPixels(segments);

        int[][] M = new int[height][width];

        for (int[] row : M) {
            Arrays.fill(row, 255);
        }

        Arrays.fill(M[0], 0);
        Arrays.fill(M[M.length - 1], 0);

        for (int i = 1; i < M.length - 1; i++) {
            M[i][0] = 0;
            M[i][M[i].length - 1] = 0;
        }

        for (int pixel : borderPixels) {
            int row1 = pixel / width;
            int col1 = pixel % width;

            M[row1][col1] = 0;
        }

        BufferedImage imageType2 = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        WritableRaster raster = imageType2.getRaster();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = M[y][x];
                raster.setSample(x, y, 0, value);
            }
        }

        try {
            ImageIO.write(imageType2, "png", new File(name+"Type2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage original = imageUtility.getImg();

        BufferedImage segmType1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = new Color(original.getRGB(x, y));
                
                Color newPixelColor = new Color(pixelColor.getRed(),  M[y][x]==255?pixelColor.getGreen():255, pixelColor.getBlue());

                segmType1.setRGB(x, y, newPixelColor.getRGB());
            }
        }

        try {
            ImageIO.write(segmType1, "png", new File(name+"Type1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Individual removeSmallSegments(int minSize) {
        String[] genotypeCopy = this.genotype.clone();
        boolean b = false;
        for (List<Integer> segment : this.segments) {
            if (segment.size() < minSize) {
                b = true;
                for (int i : segment) {
                    genotypeCopy[i] = this.mstGenotype[i];
                }
            }
        }
        if (b) {
            return new Individual(genotypeCopy, this.mstGenotype);
        } else {
            return this;
        }
    }
}
