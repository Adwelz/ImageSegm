import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class Ga {

    Properties config = new Properties();
    private final int nbrIndividuals;
    private final float crossover_rate;
    private final float mutation_rate;
    private int segmentsMinSize =100;

    List<Individual> population = new ArrayList<>();

    public Ga( int nbrIndividuals, float crossoverRate, float mutationRate) {
        this.nbrIndividuals = nbrIndividuals;
        this.crossover_rate = crossoverRate;
        this.mutation_rate = mutationRate;

        try {
            config.load(new FileInputStream("/Users/antoine/ImageSegm/ImageSegm/src/resources/config.properties"));

            this.segmentsMinSize = Integer.parseInt(config.getProperty("SegmentMinSize"));            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void init_population(){
        this.population = new ArrayList<>();
        for(int i=1;i<nbrIndividuals+1;i++){
            population.add(new Individual(i, this.segmentsMinSize));
        }
    }

    List<Individual> selectBest(List<Individual> individuals, int n) {

        List<Individual> individualsCopy = new ArrayList<>(individuals);

        individualsCopy.sort(Comparator.comparing(Individual::getFitness));
        
        Collections.reverse(individualsCopy);

        individualsCopy = individualsCopy.subList(Math.min(n, individualsCopy.size()),individualsCopy.size());

        return individualsCopy.subList(0, Math.min(n, individualsCopy.size()));
    }

    List<Individual> selectRandom(List<Individual> individuals, int n) {
        List<Individual> individualsCopy = new ArrayList<>(individuals);

        Collections.shuffle(individualsCopy);

        individualsCopy = individualsCopy.subList(Math.min(n, individualsCopy.size()),individualsCopy.size());

        return individualsCopy.subList(0, Math.min(n, individualsCopy.size()));
    }

    Individual[] crossover(Individual i1, Individual i2) {
        return i1.crossover(i2);
    }

    Individual run(int nbrOfCycle){
        init_population();
        int nbrParents = (int) (nbrIndividuals*crossover_rate);

        if(!(nbrParents % 2 == 0)){
            nbrParents-=1;
        }

        for(int k=0;k<nbrOfCycle;k++) {
            System.out.println(k+"/"+nbrOfCycle+"\n");
            
            List<Individual> parents = selectRandom(population, nbrParents);

            Collections.shuffle(parents);

            for (int i = 0; i < nbrParents; i += 2) {
                Individual[] childs = crossover(parents.get(i), parents.get(i + 1));

                Individual individual1;

                if(childs[0].getFitness()>parents.get(i).getFitness()){
                    individual1 = childs[0];
                }
                else {
                    individual1 = parents.get(i);
                }

                Individual individual2;

                if(childs[1].getFitness()>parents.get(i+1).getFitness()){
                    individual2 = childs[1];
                }
                else {
                    individual2 = parents.get(i+1);
                }

                individual1 = individual1.removeSmallSegments(segmentsMinSize);
                individual2 = individual2.removeSmallSegments(segmentsMinSize);

                if (Math.random() < mutation_rate) {
                    individual1 = individual1.mutation();
                }

                if (Math.random() < mutation_rate) {
                    individual2 = individual2.mutation();
                }

                population.add(individual1);
                population.add(individual2);
            }
            population.sort(Comparator.comparing(Individual::getFitness));
            Collections.reverse(population);
            Individual best = population.get(0);
            System.out.println(best.getFitness());
            System.out.println(best.getEdgeValue());
            System.out.println(best.getConnectivity());
            System.out.println(best.getOverallDeviation());
            System.out.println(best.getSegments().size());
            //System.out.println(Arrays.asList(best.getGenotype()));
        }
        Individual best = population.get(0);
        return best;
    }

}
