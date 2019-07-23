package navigatorteam.cryptoproxy;

import java.math.BigInteger;

/**
 * Created on 2019-07-22.
 */
public interface CryptoServiceProvider {
    String encrypt(String input);
    //per adesso decrypt returna una String, quando verrà implementata l'integrità con hash va messo a posto
    String decrypt(String input);
    void generateKeys();
}
