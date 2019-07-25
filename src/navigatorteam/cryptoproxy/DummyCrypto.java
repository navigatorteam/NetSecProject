package navigatorteam.cryptoproxy;

import java.math.BigInteger;

/**
 * Created on 2019-07-22.
 */
public class DummyCrypto implements CryptoServiceProvider {


    //simple implementation that returns
    @Override
    public String encrypt(String input) {
        return input;
    }

    @Override
    public String decrypt(String input) {
        return input;
    }

    @Override
    public String encryptToken(String token) {
        return token;
    }

    @Override
    public String decryptToken(String token) {
        return token;
    }
}
