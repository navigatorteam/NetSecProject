package navigatorteam.cryptoproxy;

import java.util.Random;

public class TokenGenerator {

    public static String generateToken()
    {
        Random random = new Random(System.nanoTime());
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 15; i++) {
            sb.append(random.nextInt(9) + 1);
        }
        return sb.toString();
    }

}
