package navigatorteam.cryptoproxy;

/**
 * Created on 2019-07-22.
 */
public interface CryptoServiceProvider {
    String encrypt(String input);
    String decrypt(String input);
}
