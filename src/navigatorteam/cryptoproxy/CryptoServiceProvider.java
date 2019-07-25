package navigatorteam.cryptoproxy;

import java.math.BigInteger;

/**
 * Created on 2019-07-22.
 */
public interface CryptoServiceProvider {
    String encrypt(String input);

    String decrypt(String input) throws Throwable;

    String encryptToken(String token);

    String decryptToken(String token);
}
