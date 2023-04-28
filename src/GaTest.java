import org.junit.Test;

public class GaTest {
    @Test
    public void testRun() {
        Ga ga = new Ga(4, 0.5f, 0.2f);

        Individual individual = ga.run(2);

        //individual.createImg();
    }
}
