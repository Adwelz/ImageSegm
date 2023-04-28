import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {

        long startTime = System.nanoTime();

        /* Individual individual = new Individual(2, 100);

        individual.createImgs("testGaI"); */

        /* Ga ga = new Ga(4, 0.5f, 0.2f);

        Individual individual = ga.run(20);

        individual.createImg(); */

        MOGa moGa =new MOGa(4, 0.2f);

        List<List<Individual>> fronts = moGa.run(30);

        for(List<Individual> front : fronts){
            System.out.println(front.size()+"\n");
            for(Individual i : front){
                System.out.println(i.getSegments().size());
            }
        }
        for(int i =0;i<fronts.get(0).size();i++){
            fronts.get(0).get(i).createImgs(Integer.toString(i)+"MOGA147091.png");
        }        

        long endTime   = System.nanoTime();

        long totalTime = endTime - startTime;
        System.out.println(totalTime/Math.pow(10,9));

    }
}
