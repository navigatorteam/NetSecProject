package navigatorteam.cryptoproxy;

import java.math.BigInteger;

public class ExchangedObject {

    public String encryptedRequest;
    public String encryptedSharedKey;

    public ExchangedObject(byte[] r, byte[] sk){
        encryptedRequest = new String(r);
        encryptedSharedKey = new String(sk);
    }

}
