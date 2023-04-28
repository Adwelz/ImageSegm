import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class MOGa {
    Properties config = new Properties();
    private final int nbrIndividuals;
    private final float mutation_rate;
    private int segmentsMinSize =100;

    List<Individual> population = new ArrayList<>();

    public MOGa( int nbrIndividuals, float mutationRate) {
        this.nbrIndividuals = nbrIndividuals;
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

    Individual[] crossover(Individual i1, Individual i2) {
        return i1.crossover(i2);
    }

    public List<List<Integer>> dominatesList(List<Individual> population){
        List<List<Integer>> dominatesList = new ArrayList<>();

        for(int i=0;i<population.size();i++){
            List<Integer> list = new ArrayList<>();
            dominatesList.add(list);
        }

        for(int i=0;i<population.size();i++){
            for(int j=0;j<population.size();j++){
                    if(population.get(i).dominate(population.get(j))){
                        dominatesList.get(i).add(j);
                    }
            }
        }

        return dominatesList;
    }

    public List<List<Individual>> fronts(List<Individual> list){
        List<List<Integer>> dominatesList = dominatesList(list);

        List<List<Individual>> fronts = new ArrayList<>();

        int nbrFront = 1;
        for(int i =0;i<dominatesList.size();i++){
            if(dominatesList.get(i).size()+1>nbrFront){
                nbrFront = dominatesList.get(i).size()+1;
            }
        }

        for(int i=0;i<nbrFront;i++){
            List<Individual> l = new ArrayList<>();
            fronts.add(l);
        }

        for(int i =0;i<dominatesList.size();i++){
            fronts.get(dominatesList.get(i).size()).add(list.get(i));
        }

        return fronts;
    }

    public List<Individual> selectParent(List<Individual> population, int nbrIndividuals){
        List<Individual> parent = new ArrayList<>();

        List<List<Individual>> fronts = fronts(population);

        for(List<Individual> front : fronts){
            if(front.size()+ parent.size() <= nbrIndividuals){
                parent.addAll(front);
            }
            if(parent.size() == nbrIndividuals){
                break;
            }
            if(front.size()+ parent.size() > nbrIndividuals){
                parent.addAll(selectByCrowdingDistance(front,nbrIndividuals - parent.size()));
                break;
            }
        }

        return parent;
    }

    public List<Individual> selectByCrowdingDistance(List<Individual> front, int i) {
        double[] crowdingDistance = crowdingDistance(front);

        List<Integer> indexes = new ArrayList<>();
        List<Double> liste = new ArrayList<>();
        for (double element : crowdingDistance) {
            liste.add(element);
        }
        List<Double> sortedListe = new ArrayList<>(liste);
        Collections.sort(sortedListe, Collections.reverseOrder());
        for (Double element : sortedListe) {
            int index = liste.indexOf(element);
            indexes.add(index);
            liste.set(index, null);
        }

        indexes = indexes.subList(0, i);

        List<Individual> individuals = new ArrayList<>();

        for(Integer index : indexes){
            individuals.add(front.get(index));
        }

        return individuals;
    }

    public static List<Integer> indexTriEdgeValue(List<Individual> list) {
        List<Integer> indexes = new ArrayList<>();
        List<Individual> sortedListe = new ArrayList<>(list);

        sortedListe.sort(Comparator.comparing(Individual::getEdgeValue));

        for (Individual element : sortedListe) {
            indexes.add(list.indexOf(element));
        }
        return indexes;
    }

    public static List<Integer> indexTriConnectivity(List<Individual> list) {
        List<Integer> indexes = new ArrayList<>();
        List<Individual> sortedListe = new ArrayList<>(list);

        sortedListe.sort(Comparator.comparing(Individual::getConnectivity));
        Collections.reverse(sortedListe);

        for (Individual element : sortedListe) {
            indexes.add(list.indexOf(element));
        }
        return indexes;
    }

    public static List<Integer> indexTriOverallDeviation(List<Individual> list) {
        List<Integer> indexes = new ArrayList<>();
        List<Individual> sortedListe = new ArrayList<>(list);

        sortedListe.sort(Comparator.comparing(Individual::getOverallDeviation));
        Collections.reverse(sortedListe);

        for (Individual element : sortedListe) {
            indexes.add(list.indexOf(element));
        }
        return indexes;
    }

    public double[] crowdingDistanceEdgeValue(List<Individual> list){
        double[] crowdingDistance = new double[list.size()];

        List<Integer> indexTriEdgeValue = indexTriEdgeValue(list);

        int indexFirst = indexTriEdgeValue.get(0);
        int indexLast = indexTriEdgeValue.get(indexTriEdgeValue.size()-1);


        crowdingDistance[indexFirst] = Double.POSITIVE_INFINITY;
        crowdingDistance[indexLast] = Double.POSITIVE_INFINITY;

        for(int j =1;j<list.size()-1;j++){
            int index = indexTriEdgeValue.get(j);
            int nextIndex = indexTriEdgeValue.get(j+1);
            int previousIndex = indexTriEdgeValue.get(j-1);
            crowdingDistance[index] = (list.get(nextIndex).getEdgeValue() - list.get(previousIndex).getEdgeValue())
                                    /(list.get(indexLast).getEdgeValue() - list.get(indexFirst).getEdgeValue());
        }

        return crowdingDistance;
    }

    public double[] crowdingDistanceConnectivity(List<Individual> list){
        double[] crowdingDistance = new double[list.size()];

        List<Integer> indexTriConnectivity = indexTriConnectivity(list);

        int indexFirst = indexTriConnectivity.get(0);
        int indexLast = indexTriConnectivity.get(indexTriConnectivity.size()-1);


        crowdingDistance[indexFirst] = Double.POSITIVE_INFINITY;
        crowdingDistance[indexLast] = Double.POSITIVE_INFINITY;

        for(int j =1;j<list.size()-1;j++){
            int index = indexTriConnectivity.get(j);
            int nextIndex = indexTriConnectivity.get(j+1);
            int previousIndex = indexTriConnectivity.get(j-1);
            crowdingDistance[index] = (list.get(nextIndex).getConnectivity() - list.get(previousIndex).getConnectivity())
                                    /(list.get(indexLast).getConnectivity() - list.get(indexFirst).getConnectivity());
        }

        return crowdingDistance;
    }

    public double[] crowdingDistanceOverallDeviation(List<Individual> list){
        double[] crowdingDistance = new double[list.size()];

        List<Integer> indexTriOverallDeviation = indexTriOverallDeviation(list);

        int indexFirst = indexTriOverallDeviation.get(0);
        int indexLast = indexTriOverallDeviation.get(indexTriOverallDeviation.size()-1);


        crowdingDistance[indexFirst] = Double.POSITIVE_INFINITY;
        crowdingDistance[indexLast] = Double.POSITIVE_INFINITY;

        for(int j =1;j<list.size()-1;j++){
            int index = indexTriOverallDeviation.get(j);
            int nextIndex = indexTriOverallDeviation.get(j+1);
            int previousIndex = indexTriOverallDeviation.get(j-1);
            crowdingDistance[index] = (list.get(nextIndex).getOverallDeviation() - list.get(previousIndex).getOverallDeviation())
                                    /(list.get(indexLast).getOverallDeviation() - list.get(indexFirst).getOverallDeviation());
        }

        return crowdingDistance;
    }

    public double[] crowdingDistance(List<Individual> front){
        double[] crowdingDistance = new double[front.size()];

        double[] crowdingDistanceEdgeValue = crowdingDistanceEdgeValue(front);
        double[] crowdingDistanceConnectivity = crowdingDistanceConnectivity(front);
        double[] crowdingDistanceOverallDeviation = crowdingDistanceOverallDeviation(front);

        for(int i =0;i<front.size();i++){
            crowdingDistance[i] = crowdingDistanceEdgeValue[i] + crowdingDistanceConnectivity[i] 
                                + crowdingDistanceOverallDeviation[i];
        }

        return crowdingDistance;
    }

    List<List<Individual>> run(int nbrOfCycle){
        init_population();

        for(int k=0;k<nbrOfCycle;k++) {
            System.out.println(k+"/"+nbrOfCycle+"\n");
            
            List<Individual> P = selectParent(population, nbrIndividuals/2 - nbrIndividuals/2 % 2);

            List<Individual> Q = new ArrayList<>();

            Collections.shuffle(P);

            for (int i = 0; i < P.size(); i += 2) {
                Individual parent1 = P.get(i);
                Individual parent2 = P.get(i+1);

                Individual[] childs = crossover(parent1,parent2);

                Individual individual1 = childs[0];
                Individual individual2 = childs[1];

                if (Math.random() < mutation_rate) {
                    individual1 = individual1.mutation();
                }

                if (Math.random() < mutation_rate) {
                    individual2 = individual2.mutation();
                }

                individual1 = individual1.removeSmallSegments(this.segmentsMinSize);

                individual2 = individual2.removeSmallSegments(this.segmentsMinSize);

                Q.add(individual1);
                Q.add(individual2);
            }
            List<Individual> R =new ArrayList<>();
            R.addAll(P);
            R.addAll(Q);
            population = R;

            Individual best = population.get(0);
            System.out.println(best.getFitness());
            System.out.println(best.getEdgeValue());
            System.out.println(best.getConnectivity());
            System.out.println(best.getOverallDeviation());
            System.out.println(best.getSegments().size());
            best = population.get(1);
            System.out.println(best.getFitness());
            System.out.println(best.getEdgeValue());
            System.out.println(best.getConnectivity());
            System.out.println(best.getOverallDeviation());
            System.out.println(best.getSegments().size());
            best = population.get(2);
            System.out.println(best.getFitness());
            System.out.println(best.getEdgeValue());
            System.out.println(best.getConnectivity());
            System.out.println(best.getOverallDeviation());
            System.out.println(best.getSegments().size());
            best = population.get(3);
            System.out.println(best.getFitness());
            System.out.println(best.getEdgeValue());
            System.out.println(best.getConnectivity());
            System.out.println(best.getOverallDeviation());
            System.out.println(best.getSegments().size());
        }
        return fronts(population);
    }

}
