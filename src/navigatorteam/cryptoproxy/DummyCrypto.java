package navigatorteam.cryptoproxy;

import java.math.BigInteger;

/**
 * Created on 2019-07-22.
 */
public class DummyCrypto implements CryptoServiceProvider {

    @Override
    public String encrypt(String input) {
        return input;
    }

    @Override
    public String decrypt(String input) {
        return input;
    }

    @Override
    public void generateKeys() {
        //do nothing
    }

    @Override
    public AsymmetricKey getPublicKey() {
        return null;
    }

    @Override
    public void setOtherEntityPublicKey(AsymmetricKey key) {

    }
}
