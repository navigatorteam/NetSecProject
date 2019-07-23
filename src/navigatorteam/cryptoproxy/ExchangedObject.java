package navigatorteam.cryptoproxy;

import java.math.BigInteger;

public class ExchangedObject {

    public String encryptedRequest;
    public String encryptedSharedKey;
    public String encryptedDigitalSignature;

    public ExchangedObject(byte[] r, byte[] sk, byte[] ds){
        encryptedRequest = new String(r);
        encryptedSharedKey = new String(sk);
        encryptedDigitalSignature = new String(ds);
    }

}
