import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class MOGaTest {
    MOGa moGa =new MOGa(10, 0.2f);

    @Test
    public void testDominatesList() {
        moGa.init_population();
        System.out.println(moGa.dominatesList(moGa.population));
    }

    @Test
    public void testFronts() {
        moGa.init_population();
        System.out.println(moGa.fronts(moGa.population));
    }

    @Test
    public void testIndexTriEdgeValue() {
        moGa.init_population();
        List<Integer> indexes = moGa.indexTriEdgeValue(moGa.population);
        System.out.println(indexes);
        for(int i : indexes){
            System.out.println(moGa.population.get(i).getEdgeValue());
        }
    }

    @Test
    public void testIndexTriConnectivity() {
        moGa.init_population();
        List<Integer> indexes = moGa.indexTriConnectivity(moGa.population);
        System.out.println(indexes);
        for(int i : indexes){
            System.out.println(moGa.population.get(i).getConnectivity());
        }
    }

    @Test
    public void testIndexTriOverallDeviation() {
        moGa.init_population();
        List<Integer> indexes = moGa.indexTriOverallDeviation(moGa.population);
        System.out.println(indexes);
        for(int i : indexes){
            System.out.println(moGa.population.get(i).getOverallDeviation());
        }
    }

    @Test
    public void testCrowdingDistanceEdgeValue() {
        moGa.init_population();
        double[] crowdingDistanceEdgeValue = moGa.crowdingDistanceEdgeValue(moGa.population);
        System.out.println(Arrays.toString(crowdingDistanceEdgeValue));
    }

    @Test
    public void testCrowdingDistanceConnectivity() {
        moGa.init_population();
        double[] crowdingDistanceConnectivity = moGa.crowdingDistanceConnectivity(moGa.population);
        System.out.println(Arrays.toString(crowdingDistanceConnectivity));
    }

	@Test
	public void testCrowdingDistanceOverallDeviation() {
		moGa.init_population();
        double[] crowdingDistanceOverallDeviation = moGa.crowdingDistanceOverallDeviation(moGa.population);
        System.out.println(Arrays.toString(crowdingDistanceOverallDeviation));
	}

    @Test
    public void testCrowdingDistance() {
        moGa.init_population();
        double[] crowdingDistance = moGa.crowdingDistance(moGa.population);
        System.out.println(Arrays.toString(crowdingDistance));
    }

    @Test
    public void testSelectByCrowdingDistance() {
        moGa.init_population();
        List<Individual> individuals = moGa.selectByCrowdingDistance(moGa.population, 3);
        System.out.println(individuals);
    }

    @Test
    public void testRun() {
        List<List<Individual>> i = moGa.run(50);

        /* int j =0;
        boolean b = true;
        while(b){
            for(Individual individual : i.get(j)){
                int k =0;
                if(individual.getOutOfSegmentRange() == 0){
                    i.get(0).get(0).createImg(k);
                    k++;
                    b =false;
                }
            }
            j++;
        }  */
    }
}
