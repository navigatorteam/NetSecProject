package navigatorteam.cryptoproxy;

/**
 * Created on 2019-07-22.
 */
public class DummyCrypto implements CryptoServiceProvider {

    @Override
    public String encrypt(String input) {
        return HexStrings.toHexString(input.getBytes());
    }

    @Override
    public String decrypt(String input) {
        return new String(HexStrings.fromHexStringToBuffer(input));
    }

    @Override
    public void generateKeys() {
        //do nothing
    }
}
