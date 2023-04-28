import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

public class IndividualTest {
    @Test
    public void testIndividual() {
        long startTime = System.nanoTime();
        Individual individual = new Individual(1);
        long endTime   = System.nanoTime();

        long totalTime = endTime - startTime;
        System.out.println(String.format("Test time : %s", totalTime/Math.pow(10,9)));
        System.out.println(individual.getEdgeValue());
        System.out.println(individual.getConnectivity());
        System.out.println(individual.getOverallDeviation());
        for(int i =38801-241;i<38801;i++){
            //if(i%241 ==0){
                System.out.println(individual.getGenotype()[i]=="down");
            //}
        }
        //System.out.println(Arrays.asList(individual.getGenotype()));
    }

    @Test
    public void testGeno2Individual() {
        Individual individual = new Individual(10);
        long startTime = System.nanoTime();
        //Individual individual2 = new Individual(individual.getGenotype());
        long endTime   = System.nanoTime();

        long totalTime = endTime - startTime;
        System.out.println(String.format("Test time : %s", totalTime/Math.pow(10,9)));
        for(int i =0;i<241;i++){
            //if(i%241 ==240){
                //System.out.println(individual2.getGenotype()[i]=="up");
            //}
        }
    }

    @Test
    public void testCrossover() {
        Individual individual1 = new Individual(1);
        Individual individual2 = new Individual(2);
        String[] geno = individual1.getGenotype();
        Individual[] childs = individual1.crossover(individual2);
        
        System.out.println(childs[0].getGenotype() ==geno);
        System.out.println(childs[0].getSegments().size());
    }

    @Test
    public void testDominate() {
        Individual individual = new Individual(1);
        System.out.println(individual.dominate(individual));
    }

    @Test
    public void testMutation() {
        Individual individual = new Individual(1);
        String[] geno = individual.getGenotype();
        Individual individual2 = individual.mutation();
        System.out.println(geno ==individual.getGenotype());
        System.out.println(geno ==individual2.getGenotype());
    }

    @Test
    public void testHueristicSegmentsSizeDiminution() {
        Individual individual = new Individual(2);
        Individual other = individual.hueristicSegmentsSizeDiminution(10);
    }

}
