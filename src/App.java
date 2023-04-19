import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {

        long startTime = System.nanoTime();

        ImageUtility imgUtil = new ImageUtility("src/resources/config.properties");

        //System.out.println(imgUtil.dist(0, 241));


        short[][] adjMatrix = imgUtil.genAdjMatrix();

        System.out.println(Arrays.toString(imgUtil.prim(adjMatrix)));

        //List<Edge> graph = imgUtil.genGraph();
        
        //List<Edge> mst = imgUtil.primsMST(graph, 0);

        //String[] geno = imgUtil.mstToGeno(mst, 0);

        //System.out.println(geno[0]);
        System.out.println(241*161);

        long endTime   = System.nanoTime();

        long totalTime = endTime - startTime;
        System.out.println(totalTime/Math.pow(10,9));

    }
}
