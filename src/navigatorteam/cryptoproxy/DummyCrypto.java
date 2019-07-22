package navigatorteam.cryptoproxy;

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
}
