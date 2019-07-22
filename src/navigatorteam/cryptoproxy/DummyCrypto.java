package navigatorteam.cryptoproxy;

/**
 * Created on 2019-07-22.
 */
public class DummyCrypto implements CryptoServiceProvider<Object> {
    @Override
    public String encrypt(String input, Object key) {
        return input;
    }

    @Override
    public String decrypt(String input, Object key) {
        return input;
    }
}
