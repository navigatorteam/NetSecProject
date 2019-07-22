package navigatorteam.cryptoproxy;

/**
 * Created on 2019-07-22.
 */
public interface CryptoServiceProvider<K> {
    String encrypt(String input, K key);
    String decrypt(String input, K key);
}
