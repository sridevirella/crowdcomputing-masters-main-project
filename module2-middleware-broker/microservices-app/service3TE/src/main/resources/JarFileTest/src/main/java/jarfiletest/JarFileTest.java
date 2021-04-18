package jarfiletest;

public class JarFileTest {

    static TaskImplementation taskImpInstance;

    public static void main(String[] args) {

        Results results = new Results();

        taskImpInstance = new TaskImplementation(results);
        System.out.println(results.getResult());
    }
}
