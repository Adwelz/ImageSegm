import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {

        long startTime = System.nanoTime();

        Individual individual = new Individual();

        String[] geno = individual.getGenotype();
        System.out.println(geno[0]+" "+ geno[1]+" "+geno[2]);
        System.out.println(geno[0+ImageUtility.getInstance().width]+" "+ geno[1+ImageUtility.getInstance().width]+" "+geno[2+ImageUtility.getInstance().width]);
        System.out.println(geno[0+ImageUtility.getInstance().width*2]+" "+ geno[1+ImageUtility.getInstance().width*2]+" "+geno[2+ImageUtility.getInstance().width*2]);
        System.out.println(geno[0+ImageUtility.getInstance().width*3]+" "+ geno[1+ImageUtility.getInstance().width*3]+" "+geno[2+ImageUtility.getInstance().width*3]);
        System.out.println(geno[0+ImageUtility.getInstance().width*4]+" "+ geno[1+ImageUtility.getInstance().width*4]+" "+geno[2+ImageUtility.getInstance().width*4]);
        System.out.println(geno[0+ImageUtility.getInstance().width*5]+" "+ geno[1+ImageUtility.getInstance().width*5]+" "+geno[2+ImageUtility.getInstance().width*5]);

        System.out.println(individual.getSegments().get(0).size());
        //System.out.println(individual.getSegments()[0].length);
        //System.out.println(Arrays.toString(individual.genotype2parent()));
        /* int[] parent =individual.genotype2parent();
        for(int i =0;i<parent.length;i++){
            if(parent[i]==-1){
                System.out.println("OUI");
            }
        } */
        //System.out.println(Arrays.toString(individual.getGenotype()));
        //System.out.println(Arrays.asList(geno));

        long endTime   = System.nanoTime();

        long totalTime = endTime - startTime;
        System.out.println(totalTime/Math.pow(10,9));

    }
}
