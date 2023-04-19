import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;

import javax.imageio.ImageIO;

public class ImageUtility {

    Properties config = new Properties();
    String imgPath;
    BufferedImage img;
    int width;
    int height;
    
    public ImageUtility(String configPath) {
        try {
            config.load(new FileInputStream(configPath));

            imgPath = config.getProperty("imgPath");

            img = ImageIO.read(new File(imgPath));

            width = img.getWidth();
            height = img.getHeight();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int[] getRGB(int x, int y){
        int rgb = img.getRGB(x,y);
        
        int red = (rgb >> 16) & 0xff;            
        int green = (rgb >> 8) & 0xff;            
        int blue = rgb & 0xff;

        return new int[]{red, green, blue};
    }

    public double dist(int i, int j){
        int xi = i % this.width;
        int yi = i / this.width;

        int xj = j % this.width;
        int yj = j / this.width;

        int[] RGBi = getRGB(xi, yi);
        int[] RGBj = getRGB(xj, yj);

        int dR = RGBi[0] - RGBj[0];
        int dG = RGBi[1] - RGBj[1];
        int dB = RGBi[2] - RGBj[2];

        return Math.sqrt( Math.pow(dR, 2) + Math.pow(dG, 2) + Math.pow(dB, 2));
    }

    public short[][] genAdjMatrix(){
        int n = this.width * this.height;

        short[][] graph = new short[n][n];

        for(int i=0;i<n;i++){
            if(i-1>=0){
                short weight = (short) dist(i, i-1);

                graph[i][i-1] = weight;
                graph[i-1][i] = weight;
            }
            if(i+1<n){
                short weight = (short) dist(i, i+1);

                graph[i][i+1] = weight;
                graph[i+1][i] = weight;
            } 
            if(i-this.width>=0){
                short weight = (short) dist(i, i-this.width);

                graph[i][i-this.width] = weight;
                graph[i-this.width][i] = weight;
            }
            if(i+this.width<n){
                short weight = (short) dist(i, i+this.width);

                graph[i][i+this.width] = weight;
                graph[i+this.width][i] = weight;
            }
        }

        return graph;
    } 

    public int[] prim(short[][] graph) {
        int n = graph.length;
        int[] parent = new int[n];
        short[] key = new short[n];
        boolean[] mstSet = new boolean[n];
        
        // Initialisation de key avec des valeurs infinies
        for (int i = 0; i < n; i++) {
            key[i] = Short.MAX_VALUE;
        }
        
        // Le sommet 0 est choisi comme point de départ
        key[0] = 0;
        parent[0] = -1;
        
        // Parcours des sommets pour construire l'arbre couvrant de poids minimal
        for (int i = 0; i < n-1; i++) {
            int u = minKey(key, mstSet);
            mstSet[u] = true;
            
            List<Integer> vList = new ArrayList<>();

            if(u>0){
                vList.add(u-1);
            }
            if(u>= this.width){
                vList.add(u-this.width);
            }
            if(u<n-1){
                vList.add(u+1);
            }
            if(u<n-this.width){
                vList.add(u+this.width);
            }

            for (int v : vList) {
                if (graph[u][v] != 0 && !mstSet[v] && graph[u][v] < key[v]) {
                    parent[v] = u;
                    key[v] = graph[u][v];
                }
            }
        }
        
        return parent;
    }
    
    public static int minKey(short[] key, boolean[] mstSet) {
        short min = Short.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < key.length; i++) {
            if (!mstSet[i] && key[i] < min) {
                min = key[i];
                minIndex = i;
            }
        }
        return minIndex;
    }

    public List<Edge> genGraph(){
        int n = this.width * this.height;

        List<Edge> graph = new ArrayList<>();

        for(int i=0;i<n;i++){
            if(i-1>=0){
                graph.add(new Edge(i, i-1, (short) dist(i, i-1)));
            }
            if(i+1<n){
                graph.add(new Edge(i, i+1, (short) dist(i, i+1)));
            } 
            if(i-this.width>=0){
                graph.add(new Edge(i, i-this.width, (short) dist(i, i-this.width)));
            }
            if(i+this.width<n){
                graph.add(new Edge(i, i+this.width, (short) dist(i, i+this.width)));
            }
        }

        return graph;
    } 

    public List<Edge> primsMST(List<Edge> edges, int firstVertice) {
        int numVertices = this.height * this.width;

        List<Edge> mst = new ArrayList<>();
        boolean[] visited = new boolean[numVertices];
        PriorityQueue<Edge> pq = new PriorityQueue<>();
        
        visited[firstVertice] = true;
        for (Edge e : edges) {
            if (e.u == firstVertice || e.v == firstVertice) {
                pq.add(e);
            }
        }
        
        while (!pq.isEmpty()) {
            Edge e = pq.poll();
            if (visited[e.u] && visited[e.v]) {
                continue;
            }
            mst.add(e);
            if (!visited[e.u]) {
                visited[e.u] = true;
                for (Edge neighbor : edges) {
                    if (neighbor.u == e.u || neighbor.v == e.u) {
                        pq.add(neighbor);
                    }
                }
            }
            if (!visited[e.v]) {
                visited[e.v] = true;
                for (Edge neighbor : edges) {
                    if (neighbor.u == e.v || neighbor.v == e.v) {
                        pq.add(neighbor);
                    }
                }
            }
        }
        
        return mst;
    }

    public static boolean containsFalse(boolean[] arr) {
        for (boolean b : arr) {
            if (!b) {
                return true;
            }
        }
        return false;
    }

    public String[] mstToGeno(List<Edge> edges, int firstVertice) {
        int numVertices = this.height * this.width;

        String[] geno = new String[numVertices];

        boolean[] visited = new boolean[numVertices];

        int currentVertice = firstVertice;

        while(containsFalse(visited)){
            visited[currentVertice] = true;

            int nextVertice = -1;

            for (Edge edge : edges) {
                if (edge.u == currentVertice ) {
                    nextVertice = edge.v;
                    break;
                }
                if (edge.v == currentVertice ) {
                    nextVertice = edge.u;
                    break;
                }
            }

            if(nextVertice == -1){
                geno[currentVertice] = "none";
                break;
            }

            if(nextVertice == currentVertice+1){
                geno[currentVertice] = "right";
            }
            if(nextVertice == currentVertice-1){
                geno[currentVertice] = "left";
            }
            if(nextVertice == currentVertice+this.width){
                geno[currentVertice] = "down";
            }
            if(nextVertice == currentVertice-this.width){
                geno[currentVertice] = "up";
            }

        }

        return geno;
        
    }
}
