import java.util.Arrays;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {

        long startTime = System.nanoTime();

        //Ga ga = new Ga(4, 0.5f, 0.2f);

        //Individual individual = ga.run(200);

        //individual.createImg();

        MOGa moGa =new MOGa(4, 0.2f);

        List<List<Individual>> fronts = moGa.run(30);

        for(List<Individual> front : fronts){
            System.out.println(front.size()+"\n");
            for(Individual i : front){
                System.out.println(i.getSegments().size());
            }
        }
        for(int i =0;i<fronts.get(0).size();i++){
            fronts.get(0).get(i).createImg(Integer.toString(i)+".png");
        }


        /* Individual parent1 = new Individual(5);
        Individual parent2 = new Individual(6);

        Individual[] childs = parent1.crossover(parent2);

        childs[0] = childs[0].removeSmallSegments();

        childs[0].createImg("child.png"); */

        //i.get(0).get(0).createImg();

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
        

        long endTime   = System.nanoTime();

        long totalTime = endTime - startTime;
        System.out.println(totalTime/Math.pow(10,9));

    }
}
