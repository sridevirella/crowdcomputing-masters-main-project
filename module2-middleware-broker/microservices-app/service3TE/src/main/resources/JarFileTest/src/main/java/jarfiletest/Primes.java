package jarfiletest;
import java.util.ArrayList;
import java.util.List;

public class Primes {

    private String result;

    public Primes(int startIndex, int endIndex) {
        this.result = calculatePrimes(startIndex, endIndex);
    }

    public String calculatePrimes(int startIndex, int endIndex) {

        List<Integer> primes = new ArrayList<>();

        for (int i = startIndex; i < endIndex; i++) {
            boolean isPrimeNumber = true;

            for (int j = 2; j < i; j++) {
                if (i % j == 0) {
                    isPrimeNumber = false;
                    break;
                }
            }

            if (isPrimeNumber) {
                primes.add(i);
            }
        }
        return primes.toString();
    }

    public String getResult() { return result;}
}