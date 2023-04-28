import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

public class ImageUtility {

    Properties config = new Properties();
    String imgPath;
    BufferedImage img;
    int width;
    int height;
    List<List<Integer>> adjList;

    private static ImageUtility single_instance = null;

    public ImageUtility() {
        try {
            config.load(new FileInputStream("/Users/antoine/ImageSegm/ImageSegm/src/resources/config.properties"));

            imgPath = config.getProperty("imgPath");

            img = ImageIO.read(new File(imgPath));

            width = img.getWidth();
            height = img.getHeight();

            this.adjList = genAdjList();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized ImageUtility getInstance()
    {
        if (single_instance == null)
            single_instance = new ImageUtility();
            
        return single_instance;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BufferedImage getImg() {
        return img;
    }

    public List<List<Integer>> getAdjList() {
        return adjList;
    }

    public int[] getRGB(int x, int y) {
        int rgb = img.getRGB(x, y);

        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;

        return new int[] { red, green, blue };
    }

    public double dist(int i, int j) {
        int xi = i % this.width;
        int yi = i / this.width;

        int xj = j % this.width;
        int yj = j / this.width;

        int[] RGBi = getRGB(xi, yi);
        int[] RGBj = getRGB(xj, yj);

        int dR = RGBi[0] - RGBj[0];
        int dG = RGBi[1] - RGBj[1];
        int dB = RGBi[2] - RGBj[2];

        return Math.sqrt(Math.pow(dR, 2) + Math.pow(dG, 2) + Math.pow(dB, 2));
    }

    public List<List<Integer>> genAdjList(){
        int n = this.width * this.height;

        List<List<Integer>> adjList = new ArrayList<>(n);
        
        for (int i = 0; i < n; i++) {
            adjList.add(new ArrayList<Integer>());
        }

        for (int i = 0; i < n; i++) {
            if (i > 0 && i % width != 0) {
                if(!adjList.get(i).contains(i-1)){
                    adjList.get(i).add(i-1);
                }
                if(!adjList.get(i-1).contains(i)){
                    adjList.get(i-1).add(i);
                }
            }
            if (i + 1 < n && i % width != width - 1) {
                if(!adjList.get(i).contains(i+1)){
                    adjList.get(i).add(i+1);
                }
                if(!adjList.get(i+1).contains(i)){
                    adjList.get(i+1).add(i);
                }
            }
            if (i - this.width >= 0) {
                if(!adjList.get(i- this.width).contains(i)){
                    adjList.get(i- this.width).add(i);
                }
                if(!adjList.get(i).contains(i- this.width)){
                    adjList.get(i).add(i- this.width);
                }
            }
            if (i + this.width < n) {
                if(!adjList.get(i+ this.width).contains(i)){
                    adjList.get(i + this.width).add(i);
                }
                if(!adjList.get(i).contains(i+ this.width)){
                    adjList.get(i).add(i + this.width);
                }
            }
        }

        return adjList;
    }

    public int[] prim(List<List<Integer>> adjList, int firstVertice) {
        int n = adjList.size();
        int[] parent = new int[n];
        double[] key = new double[n];
        boolean[] mstSet = new boolean[n];

        for (int i = 0; i < n; i++) {
            key[i] = Double.MAX_VALUE;
        }

        key[firstVertice] = 0;
        parent[firstVertice] = -1;

        for (int i = 0; i < n - 1; i++) {
            int u = minKey(key, mstSet);
            mstSet[u] = true;

            List<Integer> vList = new ArrayList<>();

            if (u % width != 0 && u>0) {
                vList.add(u - 1);
            }
            if (u >= this.width) {
                vList.add(u - this.width);
            }
            if (i % width != width - 1 && u<n-1) {
                vList.add(u + 1);
            }
            if (u < n - this.width) {
                vList.add(u + this.width);
            }

            for (int v : vList) {
                if (adjList.get(u).contains(v) && !mstSet[v] && dist(u,v) < key[v]) {
                    parent[v] = u;
                    key[v] = dist(u,v);
                }
            }
        }

        return parent;
    }

    public static int minKey(double[] key, boolean[] mstSet) {
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        for (int i = 0; i < key.length; i++) {
            if (!mstSet[i] && key[i] < min) {
                min = key[i];
                minIndex = i;
            }
        }
        return minIndex;
    }

    public String[] parent2genotype(int[] parent) {
        int n = parent.length;
        String[] genotype = new String[n];
        for (int i = 0; i < n; i++) {
            if (parent[i] == -1) {
                genotype[i] = "none";
            } else {
                int x = parent[i];
                if (x == i - 1) {
                    genotype[i] = "left";
                } else if (x == i + 1) {
                    genotype[i] = "right";
                } else {
                    if (x == i - width) {
                        genotype[i] = "up";

                    } else {
                        genotype[i] = "down";
                    }
                }
            }
        }
        return genotype;
    }    
}
