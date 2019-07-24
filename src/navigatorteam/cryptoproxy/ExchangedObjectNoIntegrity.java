package navigatorteam.cryptoproxy;

public class ExchangedObjectNoIntegrity {

    public String encryptedRequest;
    public String encryptedSharedKey;


    public ExchangedObjectNoIntegrity(byte[] r, byte[] sk){
        encryptedRequest = new String(r);
        encryptedSharedKey = new String(sk);
    }

}
