package navigatorteam.cryptoproxy;

import java.math.BigInteger;

public class ExchangedObject {

    public String encryptedRequest;
    public String encryptedSharedKey;

    public ExchangedObject(String r, String sk){
        encryptedRequest = r;
        encryptedSharedKey = sk;
    }

}
